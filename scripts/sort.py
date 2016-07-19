# -*- coding: utf-8 -*-

import os
import nltk

class Word:
    def __init__(self, word):
        self.word = word

    def __str__(self):
        return self.word

    def get_index(self, letter):
        alphabet = nltk.defaultdict(lambda: 0)
        alphabet.update({u"А": 1, u"Б": 2, u"В": 3, u"Г": 4, u"Ґ": 5, u"Д": 6, u"Е": 7,
                         u"Є": 8, u"Ж": 9, u"З": 10, u"И": 11, u"І": 12, u"Ї": 13, u"Й": 14,
                         u"К": 15, u"Л": 16, u"М": 17, u"Н": 18,  u"О": 19, u"П": 20, u"Р": 21,
                         u"С": 22, u"Т": 23, u"У": 24, u"Ф": 25, u"Х": 26, u"Ц": 27, u"Ч": 28,
                         u"Ш": 29, u"Щ": 30, u"Ь": 31, u"Ю": 32, u"Я": 33, u"а": 34, u"б": 35, 
                         u"в": 36, u"г": 37, u"ґ": 38, u"д": 39, u"е": 40, u"є": 41, u"ж": 42,
                         u"з": 43, u"и": 44, u"і": 45, u"ї": 46, u"й": 47, u"к": 48, u"л": 49, 
                         u"м": 50, u"н": 51,  u"о": 52, u"п": 53, u"р": 54, u"с": 55, u"т": 56, 
                         u"у": 57, u"ф": 58, u"х": 59, u"ц": 60, u"ч": 61, u"ш": 62, u"щ": 63,
                         u"ь": 64, u"ю": 65, u"я": 66})
        return alphabet[letter]

    def __cmp__(self, word2):
        if (len(self.word.decode("utf-8")) < len(word2.word.decode("utf-8"))):
            minimum = self.word.decode("utf-8")
            rest = word2.word.decode("utf-8")
            our = True
        else:
            minimum = word2.word.decode("utf-8")
            rest = self.word.decode("utf-8")
            our = False
        for i in range(len(minimum) - 1):
            ind1 = self.get_index(minimum[i])
            ind2 = self.get_index(rest[i])
            if (ind1 < ind2):
                if our == True : return -1
                else: return 1
            elif (ind1 > ind2):
                if our == True : return 1
                else: return -1
        if our == True : return -1
        else: return 1

def partition(list, start, end):
    pivot = list[end]
    bottom = start-1
    top = end
    done = 0
    while not done:
        while not done:
            bottom = bottom+1
            if bottom == top:
                done = 1
                break
            if list[bottom] > pivot:
                list[top] = list[bottom]
                break
        while not done:
            top = top-1            
            if top == bottom:
                done = 1
                break
            if list[top] < pivot:
                list[bottom] = list[top]
                break
    list[top] = pivot
    return top

def quicksort(list, start, end):
    if start < end:
        split = partition(list, start, end)
        quicksort(list, start, split-1)
        quicksort(list, split+1, end)
    else:
        return
