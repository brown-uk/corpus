# -*- coding: utf-8 -*-

import json, re

with open("visti_dp_ukr.json", "r") as f:
	lines = f.readlines()

data = []
for line in lines:
	data.append(json.loads(line[12:-2]))

sorted_data = sorted(data, key=lambda k: k['length'])

for article in sorted_data[-130:-1]:
#article = sorted_data[-1]
	title = re.sub(r"[:\? ]+", r"_", article["title"])
	if len(title) > 50:
		title = title[:50]
	body = re.sub(r"([\?!\.]), ", r"\1\n", article["body"].encode("utf-8"))
	with open("articles/A_" + title.encode("utf-8") + "_" + article["year"][-4:].encode("utf-8") + ".txt", "w") as f:
		f.write("<id>A</id>\n")
		f.write("<author_surname>" + article["author_name"].encode("utf-8") + "</author_surname>\n<author_name></author_name>\n")
		f.write("<title>" + article["title"].encode("utf-8") + "</title>\n<publ_in>Вісті Придніпров'я</publ_in>\n")
		f.write("<url>" + article["url"].encode("utf-8") + "</url>\n<publ_part></publ_part>\n<publ_place></publ_place>\n<publisher></publisher>\n")
		f.write("<year>" + article["year"][-4:].encode("utf-8") + "</year>\n<pages></pages>\n<length>" + str(article["length"]) + "</length>\n")
		f.write("<alt_orth>0</alt_orth>\n<errors></errors>\n<comments></comments>\n<body>\n")
		f.write(body)
		f.write("\n</body>")

