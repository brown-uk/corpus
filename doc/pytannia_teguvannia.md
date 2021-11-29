### Загальний опис тегування

Тегування виконується засобами nlp_uk, що в свою чергу використовує словник ВЕСУМ та україснький модуль аналізу LanguageTool.

Загальний опис тегів: https://github.com/brown-uk/dict_uk/blob/master/doc/tags.txt

Окрім того nlp_uk проставляє наступні теги:
* punct - для української пунктуації
* symb - для символів
* unkown - для невідомих, але потенційно українських слів
* unclass - все інше, зокрема слова латиницею, російські слова тощо

Відкриті питання:
* тегування прикм. та загальних іменн., що є власною назвою: Золота Орда, Кривий Ріг, (вул.) Зелена, (вул.) Вернадського тощо
Деякі топонімні прикм. мають лему з великої літери (бо не вживаються з загальними іменн.): 
* Заверхня /adj g=f  # Заверхня Кичера
* Бирючий /adj
Чи треба тег prop для власних назв, що складаються з загальних:
* Ліга Націй
* Оперний

### Тегування з омонімією
Деякі токени маю незнімну омонімію: автор(к)и (автори + авторки), або «отримав поранення» (одн. або мн.) в такому випадку елемент <token> 
матиме дочірній елемент <alts> з іншими значеннями.

### Нормалізація тексту

- всі апострофи: U+2019, U+02BC тощо приводимо до прямого (U+0027)
- всі лапки приводимо до ялинок (« та »), вкладені лапки - вживаємо „ та “
- всі наголоси прибираємо (за винятком, де це є важливим) ??
- нерозривні пробіли (U+00AD) ??
- дефіс «-», коротке тире «–» та довге тире «—» - лишаємо, як є, інші приводимо до цих трьох (напр. U+2012 до довгого)
- трикрапки: ... та … - лишаємо, як є ??
- параграфи відділяються порожнім рядком
