#!/bin/env groovy

@Grab(group='org.apache.commons', module='commons-csv', version='1.10.0')

import groovy.transform.CompileStatic
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.NodeChild

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord


@groovy.transform.SourceURI
URI SOURCE_URI
String SCRIPT_DIR=new File(SOURCE_URI).parent

def origHeaders = ["group", "cat", "file", "author_surname", "author_name", "title", "publ_in", "url", "publ_part", "publ_place", "publisher", "year", "pages", "length", "alt_orth", "errors", "comments"]
def newHeaders = []
for(int i=1; i<11; i++) {
    newHeaders << "author_surname_$i"
    newHeaders << "author_name_$i"
}

def headers = origHeaders + newHeaders

def csvFormat = CSVFormat.Builder.create()
    .setHeader()
    .setSkipHeaderRecord(true)
    .setIgnoreSurroundingSpaces(true)
    .setAllowMissingColumnNames(true)
    .build()

CSVParser metaParser = csvFormat.parse(new FileReader("meta/meta.csv"))
List<CSVRecord> records = []
for(CSVRecord record: metaParser) {
    records << record
}
def metaFiles = records.collect{ r -> r['file'] }

CSVPrinter printer = new CSVPrinter(new FileWriter("meta_so-so.csv"), CSVFormat.EXCEL)
printer.printRecord(headers);


def dirNames = ["good", "so-so"]

dirNames.each { dirName ->

    //def metaSuffix = dirName == "good" ? "" : "_${dirName}"

    println "Working on category: $dirName"

    def dir = "../data/$dirName"
    File txtFolder = new File(SCRIPT_DIR, dir)

    def files = txtFolder.listFiles().findAll { it.name.endsWith('.txt') }.sort{ it.name }
    def xmlFiles = new File(SCRIPT_DIR, "../data/disambig").listFiles().collect { it.name }.toSorted()

    def wrongExtension = xmlFiles.findAll{ ! it.endsWith('.xml') }
    if (wrongExtension) {
        println "Wrong extensions in xml folder:\n${wrongExtension.join('\n')}"
    }

    files.each { File file->
        File txtFile = new File(txtFolder, file.name)
        String text = file.getText('utf-8')

        if( dirName == "good" ) {
            if( ! xmlFiles.contains(file.name.replaceFirst(/\.txt$/, '.xml')) ) {
                println "xml missing for ${file.name}"
            }
        }

        def trueLength = countWords(text)
        
        if( trueLength < 10 ) {
            System.err.println "ERROR: word count too small ${trueLength}: ${file.name}"
        }

        def record = records.find{ r -> r['file'] == file.name }
        if( ! record ) {
            println "ERROR: File not in the meta: ${file.name}"
        }
        else {
            if( Math.abs(trueLength - (record['length'] as int)) > 5 ) {
                println "Actual word count ${trueLength} does not match meta: ${record['length']} for: ${file.name}"
            }
            metaFiles.remove(file.name)
        }

        if( text.contains("<body>") ) {
            if( ! text.trim().endsWith("</body>") )
                println "closing </body> missing - ${file.name}"

            String metaXml = text.replaceAll(/(?s)(.*?)<body>.*/, '<text><meta>$1</meta></text>')
            metaXml = metaXml.replace('&', '&amp;')

            try {
                GPathResult xml = new groovy.xml.XmlSlurper().parseText(metaXml)
    
                def metaItems = xml.children().getAt(0).children().collect { GPathResult it -> it.name() }
    
                metaItems -=  headers
                metaItems -= "id"
                if( metaItems ) {
                    println "Unknown meta items for ${file.name}: $metaItems"
                }

                xml.meta.with {
                    def values = [dirName, id, file.name, author_surname, author_name, title, publ_in, url, publ_part, publ_place, publisher, year, pages, trueLength, alt_orth, errors, comments]
                    newHeaders.each {
                        values << xml.meta[it]
                    }
                    printer.printRecord(values)
                }
            }
            catch(e) {
                System.err.println("Failed to parse: ${file.name}")
                throw e
            }

            if( dirName == "good" ) {
                println "WARNING: Txt file still has meta: ${file.name}"
            }
        }
        else {
        }
    }
}

println "ERROR: Meta records without files (${metaFiles.size()}):\n${metaFiles.toSorted().join('\n')}"

printer.flush()


@CompileStatic
int countWords(String text) {
    def pureText = text.replaceFirst(/.*<body>/, '')
    pureText = pureText.replaceAll(/([0-9])[:,.-]([0-9])/, '$1$2').trim()
    def words = pureText =~ /(?ui)[а-яіїєґ][а-яіїєґa-z0-9\u0301'’ʼ\/\u2013-]*/
    return words.size()
}
