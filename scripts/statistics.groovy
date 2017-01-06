#!/bin/env groovy

def FRACTIONS = ["A": 25, "B": 3, "C": 7, "D": 7, "E": 3, "F": 5, "G": 10, "H": 15, "I": 25]
def stats = [:].withDefault{ 0 }

new File("../data/good").eachFile{ f ->
    def text = f.text

    def matcher = text =~ /<id>(.*)<\/id>/
    assert matcher, "No id found for " + f
    def cat = matcher[0][1]

    if( ! text.startsWith('<id>') ) {
        println "WARNING: <id> is not the first element in " + f
    }

    if( ! (cat in FRACTIONS) ) {
        println "WARNING: category \"$cat\" (U+0"+ Integer.toHexString(cat.charAt(0) as int) + ") is invalid (probably Cyrillic) in " + f
        if( cat == 'А' )    // TEMP: cyr A
            cat = 'A'
    }

    matcher = text =~ /<length>(.*)<\/length>/
    assert matcher, "No length found in " + f
    def count = matcher[0][1]

    if( ! count ) {
        println "WARNING: Empty legnth in " + f
        count = 0
    }

    stats[cat] += Integer.valueOf(count)
}

stats = stats.toSorted { e1, e2 -> e1.key <=> e2.key }

println "Кат Слів    Лишилося  Готово"
println stats.collect{  k,v -> 
    def need = FRACTIONS[k]*10000
    def left = need - v
    def str = "$k:  $v".padRight(12) + left + " "
    str = str.padRight(22) + Math.round(v*100/need) + "%"
}.join('\n')

println "\nВсього слів: " + stats.values().sum()
