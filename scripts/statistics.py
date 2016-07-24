# -*- coding: utf-8 -*-

import os, collections

if __name__ == '__main__':

    CATS = "ABCDEFGHI"
    FRACTIONS = {"A": 25, "B": 3, "C": 7, "D": 7, "E": 3, "F": 5, "G": 10, "H": 15, "I": 25}
    PATH_IN = "../data/good/"
    PATH_OUT = "../aux/"

    categories = collections.defaultdict(int)

    for name in os.listdir(PATH_IN):
        if name.endswith(".txt") and name[0] in CATS and name[1] == "_":
            with open(PATH_IN + name, "r") as f:
                text = f.read()
                cat = text[text.find("<id>") + 4]
                if cat in CATS:
                    length = text[text.find("<length>") + 8:text.find("</length>")]
                    categories[cat] += int(length)
                else:
                    print "Invalid category in " + name + ": " + cat

    with open(PATH_OUT + "statistics.txt", "w") as out:
        out.write("Category\tFraction\t# of collected words\t# of words to collect\n")
        all_cats = 0
        for cat in CATS:
            s = "%-8s\t%-8s\t%-20s\t%s\n" % (cat, str(FRACTIONS[cat]) + "%",
                                             str(categories[cat]),
                                             str(int(10000 * FRACTIONS[cat]
                                                  - categories[cat])))
            all_cats += categories[cat]
            out.write(s)
        out.write("%-8s\t%-8s\t%-20s\t%s\n" % ("All", "100%", str(all_cats),
                                             str(1000000 - all_cats)))
