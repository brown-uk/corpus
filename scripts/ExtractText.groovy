#!/bin/env groovy


new File('txt').mkdirs()

new File('../data/good').eachFile{ f ->
    if( f.name.startsWith('.') )
        return

    println "Витягуємо текст з ${f.name}..."

    def text = f.text

    def pureText = text.replaceFirst(/(?s).*<body>/, '').replaceFirst(/(?s)<\/body>.*$/, '')
    pureText = pureText.replaceAll(/<\/?[^>]+>/, '')
    pureText = pureText.replace('\r\n', '\n').replaceAll("\r", "\n");

    new File("txt/${f.name}").text = pureText.trim()
}
