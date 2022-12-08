#!/bin/env groovy

@groovy.transform.SourceURI
def SOURCE_URI

def BASE_DIR = new File(SOURCE_URI).parent


println "looking in " + new File("$BASE_DIR/../data/good").absolutePath
println "Listing untagged files"

int doneCnt = 0
int doneSize = 0
int doneWords = 0
int todoCnt = 0
int todoSize = 0
int todoWords = 0

def fileMap = [:]

new File("$BASE_DIR/../data/good").eachFile{ f ->
    if( ! f.name.endsWith('.txt') )
        return

    def w = f.text =~ /(?ui)[а-яіїєґ][а-яіїєґ'\u2019\u02BC–-]*/
    int wordCount = w.size()
        
    def xmlName = f.name.replaceFirst(/\.txt$/, '.xml')
    if( new File("$BASE_DIR/../data/disambig", xmlName).isFile() ) {
        doneCnt++
        doneSize += f.size()
        doneWords += wordCount
        return
    }

    todoCnt++
    todoSize += f.size()
    todoWords += wordCount

    fileMap[f.name] = wordCount //[f.size(), wordCount]
}

fileMap
    .toSorted { it -> it.value }
    .each { k, v ->
        println "$k, size: $v"
    }

//println "Done: $doneCnt (words: $doneWords, size: $doneSize), todo: $todoCnt (words: $todoWords, size: $todoSize)"
println "Done: $doneCnt (words: $doneWords), todo: $todoCnt (words: $todoWords)"
