# -*- coding: utf-8 -*-

import os, sort

def add_books(name, books):
    f = open(name, "r")
    text = f.read()
    category = text[text.find("<id>") + 4:text.find("</id>")]
    author_ind = text.find("<author_surname_1>")
    if author_ind != -1:
        author = text[author_ind + 18:text.find("</author_surname_1>")]
    else:
        author_ind = text.find("<author_surname>")
        author = text[author_ind + 16:text.find("</author_surname>")]
    publ_in = text[text.find("<publ_in>") + 9:text.find("</publ_in>")]
    title = text[text.find("<title>") + 7:text.find("</title>")]
    if author != "":
        string = author.decode("utf-8") + ". "
    else:
        string = "Без автора".decode("utf-8") + ". "
    if publ_in != "":
        string += publ_in.decode("utf-8") + ": "
    string += title.decode("utf-8")
    books[mapping[category]].append(sort.Word(string.encode("utf-8")))
    f.close()

if __name__ == '__main__':

    CATS = "ABCDEFGHI"

    mapping = {"A":0, "B":1, "C":2, "D":3, "E":4, "F":5, "G":6, "H":7, "I":8}
    books = [[] for i in CATS]

    for name in os.listdir("."):
        if name.endswith(".txt") and name[0] in CATS and name[1] == "_":
            add_books(name, books)
        
    for name in os.listdir("Processed_good"):
        if name.endswith(".txt") and name[0] in CATS and name[1] == "_":
            add_books("Processed_good/" + name, books)

    for i in CATS:
        sort.quicksort(books[mapping[i]], 0, len(books[mapping[i]]) - 1)

    f = open("!_white_list.txt", "w")

    for i in CATS:
        f.write("-----------------------------")
        f.write("\n")
        if i == "A":
            f.write(u"Преса:".encode("utf-8"))
        elif i == "B":
            f.write(u"Релігійна література:".encode("utf-8"))
        elif i == "C":
            f.write(u"Професійно-популярна література:".encode("utf-8"))
        elif i == "D":
            f.write(u"«Естетичні інформативні» тексти:".encode("utf-8"))
        elif i == "E":
            f.write(u"Адміністративні документи:".encode("utf-8"))
        elif i == "F":
            f.write(u"Науково-популярна література:".encode("utf-8"))
        elif i == "G":
            f.write(u"Наукова література:".encode("utf-8"))
        elif i == "H":
            f.write(u"Навчальна література:".encode("utf-8"))
        elif i == "I":
            f.write(u"Художня література:".encode("utf-8"))
        f.write("\n")
        f.write("-----------------------------")
        f.write("\n")
        if len(books[mapping[i]]) > 0:
            f.write(books[mapping[i]][0].word)
            f.write("\n")
            for j in range(1, len(books[mapping[i]])):
                if j != 0 and books[mapping[i]][j].word != books[mapping[i]][j - 1].word:
                    f.write(books[mapping[i]][j].word)
                    f.write("\n")

    f.write("-----------------------------")

    f.close()
