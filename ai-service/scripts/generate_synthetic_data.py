#!/usr/bin/env python3
"""
Generate 500 synthetic fault reports for the EVN AI classifier.
Output: data/synthetic_faults.csv  (UTF-8, Cyrillic-safe)
"""
import random
import sys
from pathlib import Path

import pandas as pd

# Allow Cyrillic output on Windows terminals that default to cp1252
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

random.seed(42)

# ── Shared component pools ─────────────────────────────────────────
MK_STREETS = [
    "Партизанска", "Илинденска", "Македонија", "Гоце Делчев",
    "Орце Николов", "Питу Гули", "Никола Карев", "Климент Охридски",
    "Максим Горки", "Даме Груев", "Јован Котески", "Страшо Пинџур",
    "Видое Смилевски", "Мито Хаџивасилев", "Скупи", "Цветан Димов",
    "Борис Кидрич", "Васил Ѓоргов", "Кочо Рацин", "Ѓуро Стругар",
]
NEIGHBORHOODS = [
    "Аеродром", "Карпош", "Центар", "Гази Баба", "Чаир",
    "Ѓорче Петров", "Бутел", "Шуто Оризари", "Сарај", "Кисела Вода",
    "Лисиче", "Ново Лисиче", "Визбегово", "Влае", "Дексион",
]
SUBSTATIONS = [
    "Т-301", "Т-409", "Т-117", "Т-208", "Т-512",
    "ТС Скопје Север", "ТС Аеродром", "ТС Карпош", "ТС Центар", "ТС Гази Баба",
    "ТС Чаир", "ТС Бутел", "ТС Влае",
]
VILLAGES = [
    "Бардовци", "Студеничани", "Арачиново", "Блаце", "Сопишта",
    "Нерези", "Горно Лисиче", "Бразда", "Радишани", "Побожје",
]
HOURS = [
    "05:30", "07:00", "08:30", "10:00", "12:00",
    "13:30", "15:00", "17:00", "18:00", "19:30",
    "21:00", "22:30", "01:00", "03:30",
]
TIMES_MK = [
    "утрово", "попладне", "ноќва", "синоќа", "наутро",
    "вечерва", "пред некој час", "уште од вчера", "од денес наутро",
]
TIMES_EN = [
    "this morning", "this afternoon", "last night", "yesterday evening",
    "a few hours ago", "since noon", "since early morning", "overnight",
]
HOURS_DURATION = [1, 2, 3, 4, 5, 6, 8, 10, 12, 18, 24]
NUM_PEOPLE = [5, 10, 15, 20, 30, 50, 80, 100, 150, 200, 350, 500]
BUILDINGS_MK = [
    "зградата", "станот", "куќата", "деловниот објект",
    "блокот", "зграда бр.", "ламелата",
]
SENSORS = ["T-409", "S-201", "M-118", "R-305", "D-007", "A-440", "B-212", "C-099", "F-771"]
METERS_MK = [
    "бројач 4471203", "бројач 9920145", "мерило 30241",
    "паметен бројач №8812", "бројач 1100445",
]
ANIMALS_MK = ["птица", "куче", "мачка", "гулаб", "јаребица", "лисица", "глушец"]
SMELLS_MK = ["горено", "чад", "горено масло", "горена пластика", "хемикалии"]

def s(lst):
    return random.choice(lst)

def num(lo, hi):
    return random.randint(lo, hi)

TYPO_MAP = {
    "трафо": "трафор",
    "трансформатор": "трансфоматор",
    "електрична": "елктрична",
    "струја": "стуја",
    "напон": "нпон",
    "сензор": "сензо",
    "outage": "outge",
    "transformer": "transfomer",
    "voltage": "voltge",
    "electricity": "electricty",
    "sensor": "sensr",
    "connection": "conection",
    "dashboard": "dahboard",
    "communication": "communicaton",
}

def inject_typo(text):
    if random.random() > 0.08:
        return text
    for word, bad in TYPO_MAP.items():
        if word in text:
            return text.replace(word, bad, 1)
    return text


# Safety keywords — safety_risk=True iff any of these appear in the description
_SAFETY_KW = [
    # Macedonian
    "пожар", "искри", "искра", "експлозија", "чад", "гола жица",
    "паднати водови", "паднат вод", "жива жица", "жива струја",
    "пламен", "gori", "горе!", "гори!", " гори", "гориш",
    "под напон", "животоподдржувач",
    # English
    "fire", "spark", "explosion", "smoke", "live wire",
    "downed wire", "electrocution", "exposed wire", "energized",
]

def is_safety_risk(text: str) -> bool:
    low = text.lower()
    return any(kw in low for kw in _SAFETY_KW)

def urg_mk():
    return random.choice(["", "", "", " ИТНО!!!", " молам брзо!", " ургентно!", " помогнете!"])

def urg_en():
    return random.choice(["", "", "", " URGENT!", " please help!", " emergency!"])


# ─────────────────────────────────────────────────────────────────
# ELECTRICAL  (140 rows)
# ─────────────────────────────────────────────────────────────────
def gen_electrical(severity):
    lang = "mk" if random.random() < 0.70 else "en"
    st = s(MK_STREETS); hood = s(NEIGHBORHOODS); sub = s(SUBSTATIONS)
    hr = s(HOURS); t_mk = s(TIMES_MK); t_en = s(TIMES_EN)
    n = num(1, 150); bldg = s(BUILDINGS_MK)
    res = s(NUM_PEOPLE); hrs = s(HOURS_DURATION)

    if severity == "CRITICAL":
        pool_mk = [
            f"Експлозија на трансформаторот {sub} – гори! Пожарникари веднаш!!!",
            f"Пожар во трафо станицата кај {hood}, чад и пламен, евакуација!",
            f"Гола жица паднала на ул. {st} бр.{n}, деца играат близу, ИТНО!!!",
            f"Трансформаторот {sub} дава искри и чад, пожарот може да избие секој момент!",
            f"Болницата нема струја веќе {hrs} часа – животоподдржувачи на батерии! ИТНО!",
            f"Жица скинала и лежи на земја на {st}, жива струја, минувачи во опасност!!!",
            f"Паднат вод допира автомобил на {st}, возачот не може да излезе!",
            f"Голем пожар кај трафостаницата {sub} – пожарникарите се веќе на пат!",
            f"Чад и пламен излегуваат од ел. ормарот во {bldg} на {st}, ИТНО!",
            f"Трафо {sub} gori, искри летаат, соседите бегаат!!!",
        ]
        pool_en = [
            f"Explosion at substation {sub}, fire spreading fast, emergency services called!",
            f"Live wire down on {st} street, car touching it, occupant cannot exit, EMERGENCY",
            f"Transformer fire at {hood} substation, residents evacuating now",
            f"Hospital on backup power {hrs}h, main feed dead, ICU patients at risk",
            f"Downed live wire on {st}, sparks flying, pedestrians close by, URGENT",
            f"Substation {sub} on fire, smoke visible, fire brigade en route, EMERGENCY",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), True

    elif severity == "HIGH":
        pool_mk = [
            # no immediate safety keyword → safety_risk=False via scanner
            f"Нема струја во цело маало {hood} од {hr}ч, повеќе блока засегнати",
            f"Струјата исклучена во повеќе блокови во {hood} – {res} семејства засегнати",
            f"Трансформаторот {sub} прави силна врева и мириса на горено",
            f"Нема напон на целата ул. {st} – исклучено {t_mk}",
            f"Масовен прекин на струја во {hood} по невремето, повеќе часа трае",
            f"Нема струја на цела ул. {st}, над {res} станари пријавуваат",
            f"Неколку блока без струја во {hood} веќе {hrs} часа",
            f"Напон нема на {st} бр.{n} до бр.{n + num(5,30)}, многу семејства",
            f"По невремето синоќа, струјата не се вратила во {hood}",
            # safety-keyword templates → safety_risk=True via scanner
            f"Голема искра од трафо станицата {sub} на ул. {st}",
            f"Чад излегува од трафот {sub} на {st}, дојдете да проверите",
            f"Искри летаат од ел. ормарот во подрумот на {bldg} на {st}",
            f"Трафо {sub} испушта чад, комшиите се плашат, пријавено {t_mk}",
            f"Искри и чад од кабелскиот ормар во {hood}, оштетен трансформатор",
            f"Трафостаница {sub} испушта чад и искри, опасно!",
            f"Гола жица виси од дистрибутивниот ормар на {st}, ИТНО!",
            f"Искра скокнала при менување на осигурувачот, можен пожар",
        ]
        pool_en = [
            f"Major power outage in {hood} district since {hr}, many residents affected",
            f"Entire {st} street without electricity since {t_en}",
            f"Multiple buildings in {hood} lost power after last night's storm",
            f"Transformer {sub} buzzing loudly and smells of burning",
            f"Neighbourhood {hood} without power for {hrs} hours, no update",
            # safety-keyword templates
            f"Sparks from transformer {sub} on {st}, large area dark",
            f"Smoke from electrical cabinet near {st}, around {res} alarmed residents",
            f"Smoke and sparks from transformer {sub}, large area affected",
            f"Exposed wire hanging from distribution cabinet on {st}, urgent",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text + (urg_mk() if lang == "mk" else urg_en())), False  # safety set by scanner

    elif severity == "MEDIUM":
        pool_mk = [
            f"Нема струја во {bldg} на {st} бр.{n}",
            f"Светлото трепери во станот, не престанува веќе {hrs} часа",
            f"Паѓа напонот секое вечер околу {hr}ч, уредите се исклучуваат",
            f"Бројачот за струја прави чуден звук кај нас",
            f"Осигурувачот постојано гори во {bldg} на {st}",
            f"Кабелот во подрумот е ороден, треба замена",
            f"Напонот е нестабилен, телевизорот и фрижидерот скокаат",
            f"Прекинувачот кај {st} бр.{n} не работи правилно",
            f"Неколку приклучоци мртви во станот, останатите работат",
            f"Струјата се исклучува и вклучува неколку пати на час",
            f"Нема струја во {bldg} на {st} од {hr}ч, веќе {hrs} часа",
            f"Напонот паѓа кога ке вклучам клима или шпорет",
            f"Осигурувачот скока, нова гума не помага, повторува",
            f"Прекин кај {res} домаќинства на {st}, средна зона",
            f"Струја доаѓа и оди на секои неколку минути – {bldg} на {st}",
            f"Напонот многу низок, сијалиците светат слабо",
        ]
        pool_en = [
            f"No electricity in apartment on {st} number {n}",
            f"Voltage drops whenever using heavy appliances on {st}",
            f"Power outage in single building since {t_en}",
            f"Circuit breaker keeps tripping, replaced fuse twice already",
            f"Partial power loss in building on {st}, some sockets dead",
            f"Lights flickering all evening, cannot identify cause",
            f"Electricity cuts in and out every few minutes",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False

    else:  # LOW
        pool_mk = [
            f"Уличното светло не работи на ул. {st} бр.{n}",
            f"Светилката пред влезот на {bldg} е изгорена",
            f"Едно уличното светло трепери на {st}, не е итно",
            f"Жарулјата во ходникот на {bldg} изгорела, ситница е",
            f"Бројачот покажува малку повисока потрошувачка овој месец",
            f"Светлото во подземна гаража не работи, може полека",
            f"Уличната ламба на {st} изгасена веќе неколку дена",
            f"Еден приклучок не работи, останатите добри",
            f"Мал проблем со осветлувањето во ходникот, не е итно",
            f"Светло малку трепери попладне, не е сериозно",
            f"Еден светилник на {st} не работи, само информирам",
            f"Прекинувачот во гаражата паднал, не е голем проблем",
            f"Слабо светло во скалите, жарулата треба замена",
        ]
        pool_en = [
            f"Streetlight out on {st} near number {n}, minor issue",
            f"One flickering lamp on {st}, nothing urgent",
            f"Slightly higher meter reading than usual this month",
            f"Hallway light not working in building, can wait",
            f"One socket dead in apartment, all others fine",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False


# ─────────────────────────────────────────────────────────────────
# MECHANICAL  (110 rows)
# ─────────────────────────────────────────────────────────────────
def gen_mechanical(severity):
    lang = "mk" if random.random() < 0.70 else "en"
    st = s(MK_STREETS); hood = s(NEIGHBORHOODS)
    village = s(VILLAGES); t_mk = s(TIMES_MK); t_en = s(TIMES_EN)
    n = num(1, 120); hrs = s(HOURS_DURATION)

    if severity == "CRITICAL":
        pool_mk = [
            f"Жива жица паднала на {st} по бурата, луѓе наоколу, ИТНО!!!",
            f"Столб падна на автомобил на {st}, жицата под напон!",
            f"Дрво урна електричен столб на {st} – жица на земја, горена!",
            f"Паднати водови по невремето кај {village}, жица лежи на патот!",
            f"Столб се заканува да падне на училиштето, евакуирајте веднаш!",
            f"Жицата скинала по ветерот, виси многу ниско над {st} – опасно!",
            f"Бандерата падна на пешачка зона кај {hood}, повредени присутни!",
            f"Жица паднала на playground во {hood}, деца евакуирани, ИТНО!!!",
        ]
        pool_en = [
            f"Live wire down after storm on {st}, people nearby, EMERGENCY",
            f"Fallen pole landed on car on {st}, wire energized, call emergency now",
            f"Tree brought down power pole on {st}, wire lying on road – live",
            f"Collapsed pole near school on {st}, immediate danger to children",
            f"Wire snapped in storm, lying on playground near {hood}, EMERGENCY",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), True

    elif severity == "HIGH":
        pool_mk = [
            f"Падна столб од далекуводот по бурата кај {village}",
            f"Дрво паднало на жиците кај {village}, струјата прекината",
            f"Скршена бандера на {st} по сообраќајка – жива жица виси!",
            f"Жица виси ниско над улицата {st}, опасно за сообраќај",
            f"Столб се наклонил опасно по силниот ветер кај {hood}",
            f"По невремето синоќа, три столба скршени на {st}",
            f"Дрво притиска на жиците кај {village}, можен прекин",
            f"Кабелот виси ниско кај {st} бр.{n}, автобусите тешко минуваат",
            f"Столб оштетен во сообраќајка на {st}, жиците нестабилни",
            f"По силниот ветер, жива жица скинала на {st}, лежи на тротоар",
            f"Гранка дрво удрила во жицата, може да падне под напон",
            f"Бандера накривена, бурата е виновна кај {village}",
            f"Жица паднала на патот кај {village}, под напон, опасно!",
            f"Live wire on road after storm near {village}, traffic stopped",
            f"Downed wire on {st}, possibly energized, people staying away",
        ]
        pool_en = [
            f"Pole down after storm near {village}, power line on road",
            f"Tree branch hit power line on {st}, line sagging dangerously low",
            f"Broken pole after accident on {st}, live wire hanging loose",
            f"Power line very low over {st}, trucks and buses cannot pass",
            f"Three poles damaged in last night's wind storm near {hood}",
            f"Downed wire on {st}, possibly energized, area cordoned off",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text + (urg_mk() if lang == "mk" else urg_en())), False  # safety set by scanner

    elif severity == "MEDIUM":
        pool_mk = [
            f"Столбот е накривен на {st} бр.{n}, не е паднат уште",
            f"Скршена скоба на конзолата на бандерата кај {hood}",
            f"Жицата виси малку пониско отколку обично кај {st}",
            f"Дрво гранка се потпира на жица кај {village}",
            f"Бандерата е оштетена, боја одрнала, треба проверка",
            f"Трансформаторската кутија е физички оштетена на {st}",
            f"Кабелскиот ормар е со скршена врата, изложени жици внатре",
            f"Анкерот на столбот е ослабнат по последните дождови",
            f"Столбот е малку поместен – можеби го удрило возило",
            f"Жицата потегнато кај гранката на дрвото кај {hood}",
            f"Трансформаторот на {st} вибрира повеќе отколку обично",
            f"Покривот ми го оштетил кабелот при реновирање",
        ]
        pool_en = [
            f"Pole slightly tilted near {village}, needs inspection",
            f"Cable bracket damaged on pole on {st}",
            f"Tree branch resting on wire near {hood}, could snap in wind",
            f"Transformer casing visibly cracked on {st}",
            f"Cable box door broken, wiring exposed inside",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False

    else:  # LOW
        pool_mk = [
            f"Бандерата на {st} бр.{n} малку изгорена, не е итно",
            f"Трансформаторот малку вибрира, не е нова состојба",
            f"Кабелот малку влажен по дождот, само пријавувам",
            f"Ситна оштетување на кутијата на {st}, козметичко",
            f"Рѓа на столбот, само козметичка штета, нема ризик",
            f"Нешто виси на жицата, изгледа пластична торба",
            f"Жицата прави мал звук при ветер, обично е тивка",
            f"Дрвото расте блиску до жицата кај {village}, само информирам",
        ]
        pool_en = [
            f"Minor rust on pole on {st}, not urgent",
            f"Something hanging from wire near {hood}, looks like a bag",
            f"Transformer vibrating slightly more than usual, low concern",
            f"Cable looks slightly worn near {village}, just for records",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False


# ─────────────────────────────────────────────────────────────────
# NETWORK  (80 rows)
# ─────────────────────────────────────────────────────────────────
def gen_network(severity):
    lang = "mk" if random.random() < 0.55 else "en"
    sensor = s(SENSORS); sub = s(SUBSTATIONS); meter = s(METERS_MK)
    hrs = s(HOURS_DURATION); t_mk = s(TIMES_MK); t_en = s(TIMES_EN)
    hood = s(NEIGHBORHOODS); n_units = num(5, 50)

    if severity == "CRITICAL":
        pool_mk = [
            f"SCADA системот е целосно недостапен – диспечерите слепи!",
            f"Изгубена комуникација со сите {num(6,15)} подстаници, системот слеп!",
            f"RTU на {sub} не одговара воопшто, немаме никаква телеметрија!",
            f"Целосен пад на комуникациската мрежа, ниту еден сензор не јавува!",
            f"Диспечерскиот центар без податоци веќе {hrs} часа – ИТНО!",
        ]
        pool_en = [
            f"Complete SCADA failure, dispatchers operating blind, all substations unreachable",
            f"All RTUs offline simultaneously, zero telemetry from any point in the grid",
            f"Communications network total loss, {num(5,20)} substations dark",
            f"Dispatch center without any grid data for {hrs} hours, EMERGENCY",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False

    elif severity == "HIGH":
        pool_mk = [
            f"Не примам податоци од сензор {sensor} на трафо {sub} веќе {hrs} часа",
            f"Комуникациски проблем со SCADA, повеќе уреди недостапни",
            f"Telemetry врска изгубена со {sub}, не можеме да мониторираме",
            f"Повеќе паметни бројачи во {hood} не испраќаат податоци",
            f"RTU на подстаницата {sub} не одговара, нема статус",
            f"Сите мерила во {hood} офлајн – {n_units} бројачи недостапни",
            f"Комуникацијата со {sub} прекината по надградбата {t_mk}",
            f"Сензор {sensor} испраќа грешни вредности – дефект верувам",
            f"Загубивме читање од {n_units} бројачи во {hood}, неколку часа без податок",
        ]
        pool_en = [
            f"Lost telemetry from substation {sub} for {hrs} hours, cannot monitor load",
            f"Multiple smart meters in {hood} not reporting for {hrs}h, gap in data",
            f"RTU at {sub} unresponsive, cannot send commands",
            f"Sensor {sensor} sending garbage data, suspected hardware failure",
            f"SCADA connection to {sub} dropped after firmware update {t_en}",
            f"Several sensors offline in {hood}, {n_units} units not reporting",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False

    elif severity == "MEDIUM":
        pool_mk = [
            f"Паметниот бројач {meter} не испраќа податоци {hrs} дена",
            f"Сензорот {sensor} имаше неколку исклучувања синоќа",
            f"Комуникацијата со {sub} е бавна, одговорите задоцнуваат",
            f"Читањата на {meter} не се ажурираат редовно",
            f"Поврзувањето со {sub} паѓа и се враќа на секој час",
            f"Сензорот {sensor} праќа двојно помали вредности – веројатно дефект",
            f"Загуба на пакети кај RTU на {sub}, мрежата нестабилна",
            f"Паметниот бројач не ги праќа месечните читања",
            f"Телеметријата со {sub} го губи пакет еднаш на час",
        ]
        pool_en = [
            f"Smart meter {meter} not reporting data for {hrs} days",
            f"Sensor {sensor} had multiple dropouts last night",
            f"Communication with {sub} slow, high latency on all commands",
            f"Meter readings not updating, suspected connection issue",
            f"RTU at {sub} dropping packets intermittently since {t_en}",
            f"Monthly readings not transmitted from smart meter",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False

    else:  # LOW
        pool_mk = [
            f"Бројачот {meter} не синхронизирал {hrs} дена, ситница",
            f"Мала задоцнетост во телеметријата, не е итно",
            f"Сензорот {sensor} еднаш пројавил грешка, потоа само",
            f"Паметниот бројач еднаш пропуштил читање – еднократно",
            f"Комуникациска грешка, сама се поправила, само пријавувам",
        ]
        pool_en = [
            f"Meter sync delay, minor and intermittent",
            f"Occasional telemetry lag on sensor {sensor}, not impacting ops",
            f"Smart meter missed one reading last week, has not recurred",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False


# ─────────────────────────────────────────────────────────────────
# SOFTWARE  (70 rows)
# ─────────────────────────────────────────────────────────────────
def gen_software(severity):
    lang = "mk" if random.random() < 0.55 else "en"
    hrs = s(HOURS_DURATION); t_mk = s(TIMES_MK); t_en = s(TIMES_EN)

    if severity == "CRITICAL":
        pool_mk = [
            f"Диспечерскиот dashboard целосно не работи, не можеме да управуваме!",
            f"Системот за SCADA паднал – диспечерите не можат да работат!",
            f"Апликацијата за управување со кризи е недостапна – итно!",
            f"Целосен пад на диспечерскиот систем, преминуваме на рачно!",
            f"Автоматскиот систем за рекапсулирање не реагира – опасност!",
        ]
        pool_en = [
            f"Dispatcher dashboard completely down, operators cannot manage the grid",
            f"Crisis management application unavailable during active incident",
            f"Full dispatch system crash, falling back to manual operations",
            f"Auto-reclosing software unresponsive, grid safety at risk",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False

    elif severity == "HIGH":
        pool_mk = [
            f"Грешка во dispatcher dashboard, не се вчитува воопшто",
            f"Не можам да се логирам во системот – вратата блокирана",
            f"Апликацијата за пријавување дефекти падна по ажурирањето",
            f"Системот не дозволува отварање нови тикети – блокирани сме",
            f"Модулот за извештаи фрла грешка 500, ништо не работи",
            f"Сите корисники не можат да пристапат – серверска грешка",
            f"Порталот за пријава не прима барања {t_mk}, сите испорачувачи блокирани",
        ]
        pool_en = [
            f"Fault reporting application crashed after recent update",
            f"Cannot log in to system, authentication error 403 for all users",
            f"Dispatch interface not loading, blank screen after login",
            f"Ticket creation blocked by server error, operators cannot log faults",
            f"Reports module returning HTTP 500 consistently",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False

    elif severity == "MEDIUM":
        pool_mk = [
            f"Порталот за клиенти замрзнува на копчето за поднесување",
            f"Апликацијата паѓа кога ќе се филтрираат дефекти по датум",
            f"Листата на дефекти не се прикажува правилно, само 10 редови",
            f"Извозот на PDF не работи, добивам празен документ",
            f"Формата за пријава не праќа е-маил потврда",
            f"Картата во апликацијата не се вчитува, само врти тркало",
            f"Лозинката не може да се смени, копчето не реагира",
            f"Известувањата не пристигнуваат, иако сè е поставено точно",
            f"При прикачување фото на пријавата, апликацијата паѓа",
        ]
        pool_en = [
            f"Customer portal freezes on the submit button, cannot file reports",
            f"Application crashes when filtering faults by date range",
            f"Fault list only shows 10 rows regardless of filter settings",
            f"PDF export produces a blank document every time",
            f"Registration confirmation email not being sent to users",
            f"Map view stuck on loading spinner, never loads",
            f"Password reset link expired immediately after generation",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False

    else:  # LOW
        pool_mk = [
            f"Апликацијата е малку побавна отколку обично {t_mk}",
            f"Едно копче е на погрешно место – UI проблем, не итно",
            f"Датумот во извештајот е во погрешен формат",
            f"Преводот на македонски е погрешен на некои места",
            f"Менито не се затвора автоматски – ситна грешка",
            f"Иконата за известување скриена зад друг елемент",
        ]
        pool_en = [
            f"App slightly slower than usual, minor performance issue",
            f"Button visually misaligned in UI, cosmetic only",
            f"Date format incorrect in exported reports, wrong locale",
            f"Dropdown menu not closing automatically after selection",
            f"Minor translation error on the settings page",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False


# ─────────────────────────────────────────────────────────────────
# OTHER  (100 rows)
# ─────────────────────────────────────────────────────────────────
def gen_other(severity):
    lang = "mk" if random.random() < 0.70 else "en"
    st = s(MK_STREETS); hood = s(NEIGHBORHOODS)
    animal = s(ANIMALS_MK); smell = s(SMELLS_MK)
    t_mk = s(TIMES_MK); t_en = s(TIMES_EN)
    n = num(1, 120)

    if severity == "CRITICAL":
        pool_mk = [
            f"Силен мирис на {smell} кај трансформаторот, пожар можен – ИТНО!",
            f"Чад излегува од кабелскиот ормар на {st}, не знам причина, ИТНО!",
            f"Гола жица изложена во ормарот на {st}, деца присутни, ИТНО!!!",
            f"Гори нешто кај далекуводот, не знам точно, дојдете итно!!!",
            f"Жива жица виси до детско игралиште кај {hood}, итно!!!",
            f"Мирис на горење и чад, не знам дали е струја, ве молам итно!",
        ]
        pool_en = [
            f"Strong burning smell near cable cabinet on {st}, possible fire URGENT",
            f"Smoke from unknown source near substation, cannot identify cause, EMERGENCY",
            f"Exposed wire inside cabinet on {st}, children nearby, URGENT",
            f"Fire visible near power line on {st}, unidentified origin, please send crew",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), True

    elif severity == "HIGH":
        pool_mk = [
            f"Мириса на {smell} кај ормарот за струја на {st}, непознат извор",
            f"Вандализам на трафостаницата кај {hood} – скршена врата, изложена внатрешност",
            f"Лице беше видено да манипулира со ормар на {st} навечер",
            f"Чуден силен звук од трафото {s(SUBSTATIONS)} – непознат извор",
            f"Непознат предмет фрлен на жиците на {st}, виси и се заканува",
            f"Вандали кршеле опрема кај {hood}, треба хитна проверка",
            f"Трудница падна поради лоша тротоарска плоча до столб на {st}",
        ]
        pool_mk_safety = [
            f"Чад и мирис на {smell} кај ормарот за струја на {st}, непознат извор",
            f"Искри и чад кај трафото {s(SUBSTATIONS)}, непознат извор, пријавувам",
            f"Чад излегува од подземен кабел на {st}, ве молам проверете!",
        ]
        pool_en = [
            f"Smoke and smell of {smell} near electrical cabinet on {st}",
            f"Vandalism at substation near {hood}, door forced open, equipment exposed",
            f"Unknown person tampering with electrical cabinet on {st} at night",
            f"Very loud strange noise from transformer, cannot identify cause",
            f"Foreign object thrown onto power lines near {hood}, hanging dangerously",
            f"Sparks observed near cable box on {st}, source unclear",
        ]
        # Mix safety-keyword templates into the pool so keyword scanner picks them up
        pool_mk = [
            f"Мириса на {smell} кај ормарот за струја на {st}, непознат извор",
            f"Вандализам на трафостаницата кај {hood} – скршена врата, изложена внатрешност",
            f"Лице беше видено да манипулира со ормар на {st} навечер",
            f"Чуден силен звук од трафото {s(SUBSTATIONS)} – непознат извор",
            f"Непознат предмет фрлен на жиците на {st}, виси и се заканува",
            f"Вандали кршеле опрема кај {hood}, треба хитна проверка",
            f"Трудница падна поради лоша тротоарска плоча до столб на {st}",
        ] + pool_mk_safety
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text + (urg_mk() if lang == "mk" else urg_en())), False  # safety set by scanner

    elif severity == "MEDIUM":
        pool_mk = [
            f"Мириса на нешто чудно кај ормарот за струја на {st}",
            f"Животно заглавено кај трансформаторот ({animal}?), не знам",
            f"Гулаб направил гнездо во ормарот, дали е проблем?",
            f"Соседите се жалат на некоја бучава, не сум сигурен на што",
            f"Нешто чудно кај мерачот, не знам дали е нормално",
            f"Вода навлегла во кабелскиот ормар по вчерашниот дожд",
            f"Стар кабел видлив на {st}, не знам дали е под напон",
            f"Детето ми рекло дека видело искра кај ормарот, но не сум сигурен",
            f"Гранки растат блиску до жиците на {st}, може да е проблем",
            f"Чуден мирис {t_mk} кај трансформаторот – не знам причина",
            f"Возило удрило во ограда на трафостаница, нема видлива штета",
        ]
        pool_en = [
            f"Strange smell near electrical cabinet on {st}, source unclear",
            f"Animal ({animal}?) found stuck near transformer at {hood}",
            f"Bird built nest inside open cabinet near {st}",
            f"Neighbours complaining about noise, unsure of source",
            f"Water inside cable cabinet after heavy rain on {st}",
            f"Old cable visible at {st} bр.{n}, unsure if it is energized",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False

    else:  # LOW
        pool_mk = [
            f"Не сум сигурен дека ова е проблем, но пријавувам – чуден звук",
            f"Нешто изгледа различно кај ормарот на {st}, не знам точно",
            f"Дрвото расте блиску до жицата кај {hood}, само информирам",
            f"Видов возило да паркира до трансформаторот, дали е ОК?",
            f"Вандализам на знакот кај трафостаницата – само боја, нема штета",
            f"Контејнер паркиран блиску до ормарот, ограничен пристап",
            f"Мала пукнатина на бетонската основа на столбот, козметичко",
            f"Рѓа на вратата на ормарот, само козметика",
            f"Мирис на нешто, но слаб, можеби ништо",
            f"Случаен минувач запрашал за столбот – само пријавувам",
        ]
        pool_en = [
            f"Not sure if this is an issue, reporting just in case – odd sound near {st}",
            f"Something looks different near cabinet on {st}, cannot tell what",
            f"Tree growing close to wire near {hood}, just flagging it",
            f"Vehicle parked right next to transformer, is that a problem?",
            f"Graffiti on sign near substation, just paint, no structural damage",
            f"Minor crack in base of pole on {st}, cosmetic only",
        ]
        text = s(pool_mk if lang == "mk" else pool_en)
        return inject_typo(text), False


# ─────────────────────────────────────────────────────────────────
# Generation plan  (exact counts sum to 500)
# ─────────────────────────────────────────────────────────────────
PLAN = [
    # (category, generator, [(severity, count), ...])
    ("ELECTRICAL", gen_electrical, [
        ("CRITICAL", 14), ("HIGH", 35), ("MEDIUM", 56), ("LOW", 35),
    ]),
    ("MECHANICAL", gen_mechanical, [
        ("CRITICAL", 11), ("HIGH", 27), ("MEDIUM", 44), ("LOW", 28),
    ]),
    ("NETWORK", gen_network, [
        ("CRITICAL",  8), ("HIGH", 20), ("MEDIUM", 32), ("LOW", 20),
    ]),
    ("SOFTWARE", gen_software, [
        ("CRITICAL",  7), ("HIGH", 18), ("MEDIUM", 28), ("LOW", 17),
    ]),
    ("OTHER", gen_other, [
        ("CRITICAL", 10), ("HIGH", 25), ("MEDIUM", 40), ("LOW", 25),
    ]),
]

rows = []
for category, gen_fn, sev_dist in PLAN:
    for severity, count in sev_dist:
        for _ in range(count):
            description, _hint = gen_fn(severity)
            description = description.strip()
            # Keyword scanner is the single source of truth for safety_risk
            safety_risk = is_safety_risk(description)
            rows.append({
                "description": description,
                "category": category,
                "severity": severity,
                "safety_risk": safety_risk,
            })

random.shuffle(rows)

out_dir = Path(__file__).parent.parent / "data"
out_dir.mkdir(exist_ok=True)
out_path = out_dir / "synthetic_faults.csv"

df = pd.DataFrame(rows)
df.to_csv(out_path, index=False, encoding="utf-8-sig")

# ── Verification ──────────────────────────────────────────────────
print(f"\nSaved to: {out_path}")
print(f"Total rows: {len(df)}")

print("\nCategory distribution:")
for cat, cnt in df["category"].value_counts().items():
    print(f"  {cat:12s}: {cnt}")

print("\nSeverity distribution:")
for sev, cnt in df["severity"].value_counts().items():
    pct = cnt / len(df) * 100
    print(f"  {sev:8s}: {cnt:3d}  ({pct:.1f}%)")

sr_count = int(df["safety_risk"].sum())
print(f"\nsafety_risk=True: {sr_count} ({sr_count/len(df)*100:.1f}%)")

print("\n--- 10 random sample rows ---")
sample = df.sample(10, random_state=99)
for _, row in sample.iterrows():
    flag = " [SAFETY]" if row["safety_risk"] else ""
    cat_sev = f"[{row['category']}/{row['severity']}]"
    print(f"{cat_sev:<28}{flag}")
    print(f"  {row['description'][:110]}")
    print()
