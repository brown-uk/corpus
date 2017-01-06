#!/bin/env groovy

import groovy.transform.*


@Field
def CATEGORIES = ["A": 25, "B": 3, "C": 7, "D": 7, "E": 3, "F": 5, "G": 10, "H": 15, "I": 25]
@Field
def CAT_DESC = [
    'A': 'Преса',
    'B': 'Релігійні література',
    'C': 'Професійно-популярна література',
    'D': '«Естетичні інформативні» тексти',
    'E': 'Адміністративні документи',
    'F': 'Науково-популярна література',
    'G': 'Наукова література',
    'H': 'Навчальна література',
    'I': 'Художні тексти'
]

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
println "Статистика для good"
printStats(stats)


println "Рахуємо для so-so..."

countInFolder("../data/so-so", stats)

println "\n==================="
println "Статистика всього"
printStats(stats)


def printStats(stats) {

    stats.statMap = stats.statMap.toSorted { e1, e2 -> e1.key <=> e2.key }

    println "\nКат Слів    Лишилося  Зроблено   Опис"
    println stats.statMap.collect{  cat, v ->
        def need = CATEGORIES[cat]*10000
        def left = need - v
        def str = "$cat   $v".padRight(12) + left + " "
        str = str.padRight(22) + Math.round(v*100/need) + "%"
        str = str.padRight(32) + " " + CAT_DESC[cat]
    }.join('\n')

    println "\nВсього слів: " + stats.statMap.values().sum() + ", мін. слів: ${stats.minWords}, макс. слів: ${stats.maxWords}"
    println "Файлів: " + stats.fileCount

}


def countInFolder(folderName, stats) {

  new File(folderName).eachFile{ f ->
    def text = f.text

    def matcher = text =~ /<id>(.*)<\/id>/
    assert matcher, "No id found for " + f
    def cat = matcher[0][1]

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
    def count = matcher[0][1]

    count = count.trim()

    if( ! count ) {
        println "WARNING: Empty legnth in " + f
        count = 0
    }
    else {
        count = count as int

        if( count < stats.minWords )
            stats.minWords = count
    }

    if( count > stats.maxWords )
        stats.maxWords = count

    if( text =~ /[а-яіїєґА-ЯІЇЄҐ'][a-zA-Z]|[a-zA-Z]['а-яіїєґА-ЯІЇЄҐ]/ ) {
        println "WARNING: Latin/Cyrillic mix in " + f
    }
    else {
        def words = text.replaceFirst(/(?s).*<body>/, '') =~ /[0-9а-яіїєґА-ЯІЇЄҐ'ʼ’-]+/

        if( '-c' in args && words.count != count ) {
            println "WARNING: Length $count does not match real word count ${words.count} in " + f
            //new File("x1").text = words.collect{it}.join('\n')
            //new File("x2").text = text.replaceFirst(/(?s).*<body>/, '')
        }
    }


    stats.statMap[cat] += Integer.valueOf(count)
    stats.fileCount += 1
  }
}
