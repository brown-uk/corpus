#!/bin/env groovy

@groovy.transform.SourceURI
def SOURCE_URI

def BASE_DIR = new File(SOURCE_URI).parent


println "looking in " + new File("$BASE_DIR/../data/good").absolutePath
println "Listing untagged files"

int doneCnt = 0
int doneSize = 0
int todoCnt = 0
int todoSize = 0

new File("$BASE_DIR/../data/good").eachFile{ f ->
    if( ! f.name.endsWith('.txt') )
        return

    def xmlName = f.name.replaceFirst(/\.txt$/, '.xml')
    if( new File("$BASE_DIR/../data/disambig", xmlName).isFile() ) {
        doneCnt++
        doneSize += f.size()
        return
    }

    todoCnt++
    todoSize += f.size()
    def text = f.text

    println "${f.name}, size: ${text.length()}"
}

println "Done: $doneCnt (size: $doneSize), todo: $todoCnt (size: $todoSize)"