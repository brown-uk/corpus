#!/bin/env groovy

@Grab(group='org.apache.commons', module='commons-csv', version='1.9.0')

import groovy.transform.*
import java.util.regex.*
import java.io.OutputStreamWriter

import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.apache.commons.csv.CSVFormat.Builder


String osName = System.getProperty("os.name").toLowerCase();
if ( osName.contains("windows")) {
    if( ! "UTF-8".equals(System.getProperty("file.encoding")) ) {
        System.setOut(new PrintStream(System.out,true,"UTF-8"))
    }
}


@Field
String[] metaHeaders = ["group", "cat", "file", "author_surname", "author_name", "title", "publ_in", "url", "publ_part", "publ_place", "publisher", "year", "pages", "length", "alt_orth", "errors", "comments"]
@Field
def CATEGORIES = ["A": 25, "B": 3, "C": 7, "D": 7, "E": 3, "F": 5, "G": 10, "H": 15, "I": 25]
@Field
def CAT_DESC = [
    'A': 'Преса',
    'B': 'Релігійна література',
    'C': 'Професійно-популярна література',
    'D': '«Естетичні інформативні» тексти',
    'E': 'Адміністративні документи',
    'F': 'Науково-популярна література',
    'G': 'Наукова література',
    'H': 'Навчальна література',
    'I': 'Художні тексти'
]

@Field target = "-5" in args ? 5000 : 10000


@Field
def meta = readMeta()
@Field
boolean updateMeta = false

class Stats {
    def statMap = [:].withDefault{ 0 }
    int fileCount = 0
    int minWords = 1000000
    int maxWords = 0
}

Stats stats = new Stats()

println "Рахуємо для good..."

countInFolder("../data/good", stats)

println "\n==================="
println "Статистика для good (мета: ${target/10} тис.)"
printStats(stats)

if( ! ('-5' in args) ) {
    println "\nРахуємо для so-so..."

    countInFolder("../data/so-so", stats)

    println "\n==================="
    println "Статистика всього"
    printStats(stats)
}

if( updateMeta ) {
    writeMeta(meta)
}

def printStats(stats) {

    stats.statMap = stats.statMap.toSorted { e1, e2 -> e1.key <=> e2.key }

    println "\nКат Слів    Лишилося  Зроблено   Опис"
    println stats.statMap.collect{  cat, v ->
        def need = CATEGORIES[cat] * target
        def left = need - v
        def str = "$cat   $v".padRight(12) + left + " "
        str = str.padRight(22) + Math.round(v*100/need) + "%"
        str = str.padRight(32) + " " + CAT_DESC[cat]
    }.join('\n')

    println "\nВсього слів: " + stats.statMap.values().sum() + ", мін. слів: ${stats.minWords}, макс. слів: ${stats.maxWords}"
    println "Файлів: " + stats.fileCount
}


def countInFolder(folderName, stats) {

  def files = []

  new File(folderName).eachFile{ f ->
    if( f.name.startsWith('.') )
        return
        
    files << f
  }
  
  files = files.toSorted()
  
  files.each { f ->
    def text = f.getText('utf-8')

    if( ! text.trim() ) {
        System.err.println("Empty file: ${f.name}")
        System.exit(1) 
    }
        
    if( text[0] == '\ufeff' ) {
        text = text[1..-1]
//        println "WARNING: deprecated BOM marker (U+FEFF) in $f"
    }

    def count
    def cat
    
    if( f.name in meta ) {
        count = meta[f.name]['length'] as int
        cat = meta[f.name]['cat']
    }
    else {
        def matcher = text =~ /<id>(.*)<\/id>/
        assert matcher, "No id found for " + f
        cat = matcher[0][1]

        if( ! text.startsWith('<id>') ) {
            println "WARNING: <id> is not the first element in " + f
        }

        if( ! cat ) {
            println "ERROR: id value empty in " + f
            return
        }

        if( ! (cat in CATEGORIES) ) {
            def hex = cat ? "U+0"+ Integer.toHexString(cat.charAt(0) as int) : ""
            println "WARNING: category \"$cat\" ($hex) is invalid in " + f
            if( cat == 'А' || cat.startsWith('A') )    // TEMP: cyr A
                cat = 'A'
        }

        matcher = text =~ /<length>(.*)<\/length>/
        assert matcher, "No length found in " + f
        count = matcher[0][1]

        count = count.trim()

        if( ! count ) {
            println "WARNING: Empty legnth in " + f
            count = 0
        }
        else {
            count = count as int
        }
    }
    
    if( count < stats.minWords )
        stats.minWords = count

    if( count > stats.maxWords )
        stats.maxWords = count

    def text0 = text.replaceAll('СхідSide|ГолосUA|Фirtka', '')
    def latCyrMix = text0 =~ /[а-яіїєґА-ЯІЇЄҐ]['ʼ’]?[a-zA-Z]|[a-zA-Z]['ʼ’]?[а-яіїєґА-ЯІЇЄҐ]/
    if( latCyrMix ) {
        println "WARNING: Latin/Cyrillic mix in " + f + ": " + latCyrMix[0]
    }
    else {
        if( '-c' in args /*&& folderName =~ 'good'*/ ) {
            def pureText = text
            pureText = pureText.replaceAll(/([0-9])[:,.-]([0-9])/, '$1$2').trim()
            if( ! ('-l' in args) ) {
                pureText = pureText.replace(' - ', ' a ')
            }
//             def words = pureText.split(/[ \t,]+/).findAll { it =~ /(?ui)[а-яіїєґa-z0-9]/ }
            def words = pureText =~ /(?ui)[а-яіїєґ][а-яіїєґa-z0-9\u0301'’ʼ\/\u2013-]*/

            if( Math.abs(words.size() - count) > 5 ) {
                println "WARNING: Length $count does not match real word count ${words.size()} (delta: ${words.size()-count}) in $f"
                  if( new File("cnt").isDirectory() ) {
                   new File("cnt/${f.name}_cnt.txt").text = (words as List).join('\n')
                   new File("cnt/${f.name}_xx.txt").text = pureText
                }
                
                if( meta[f.name] ) {
                    meta[f.name]['length'] = words.size()
                    updateMeta = true
                }
            }
        }
    }

    stats.statMap[cat] += Integer.valueOf(count)
    stats.fileCount += 1
  }
}



@CompileStatic
def readMeta() {
    def meta = [:]
    
    FileReader csv = new FileReader("../meta/meta.csv")
    CSVFormat csvFormat = CSVFormat.EXCEL.withHeader(metaHeaders)
    
    Iterable<CSVRecord> records = csvFormat.parse(csv)
        
    for (CSVRecord record : records) {
        String file = record.get("file");
        if( file == "file" )
            continue
        
        meta[file] = record.toMap()
    }

    // ----------------- 
    // TODO: temporary until we merge stats
    csv = new FileReader("../meta/meta_so-so.csv")
    csvFormat = CSVFormat.EXCEL.withHeader(metaHeaders)
    
    records = csvFormat.parse(csv)
        
    for (CSVRecord record : records) {
        String file = record.get("file");
        if( file == "file" )
            continue
        
        meta[file] = record.toMap()
    }
        
    println "Found meta info for ${meta.size()} files"

    return meta    
}

@CompileStatic
def writeMeta(Map<String, Map<String, String>> meta) {
    println "Updating meta.csv"
    
    FileWriter csv = new FileWriter("../meta/meta.csv")
    def csvFormat = CSVFormat.EXCEL.withHeader(metaHeaders)
    
    csv.withWriter { wr ->
        def printer = new CSVPrinter(wr, csvFormat)
    
        meta.each { e ->
            printer.printRecord(e.getValue().values())
        }
    }
}
