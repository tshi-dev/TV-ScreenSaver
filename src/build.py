#!/usr/bin/env python3
"""src/template.html -> two pages in dist/:

urtv.html           stand-alone: fonts and the bundled photos inlined, works offline from a double-click.
tv.html             for the Android app: same page, but fonts and bundled photos are URLs the app serves from
                    its own assets (PhotoCache.java). 2.2 MB less to parse, so the screensaver starts faster.
"""
import base64
import json
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FONTS = {
    "__F_LIGHT__": "Thmanyah-Light.woff2",
    "__F_REGULAR__": "Thmanyah-Regular.woff2",
    "__F_MEDIUM__": "Thmanyah-Medium.woff2",
    "__F_DISPLAY__": "ThmanyahDisplay-Bold.woff2",
}
# tv.html points here for fonts and bundled photos; the app answers from its assets, a browser from GitHub.
REPO = "https://raw.githubusercontent.com/tshi-dev/UrTV/master/"


BASMALA = "بسم الله الرحمن الرحيم"   # compared without harakat: the API orders them its own way


def plain(s):
    return re.sub("[\u064B-\u065F\u0670]", "", s)


def verses_for_screen():
    """assets/shared/verses.json as fetched from the Quran API (.github/tools/fetch_verses.py), made fit for the font:
    the pause marks and ۞ are left out (Thmanyah has no glyph for them); letters and harakat stay as fetched.
    The API puts the basmala in front of the first verse of a surah; it is not part of that verse, so it goes
    (Al-Fatiha's first verse is the basmala itself). Surah names are shown plain, without harakat."""
    path = ROOT / "assets/shared/verses.json"
    if not path.is_file():
        return []
    out = []
    for v in json.loads(path.read_text(encoding="utf-8")):
        text = " ".join(re.sub("[\u06D6-\u06DE\u06E9]", "", v["text"]).split())   # pause marks, ۞ and ۩
        words = text.split()
        if v["ayah"] == 1 and not v["ref"].startswith("1:") and plain(" ".join(words[:4])) == BASMALA:
            text = " ".join(words[4:])
        # NFC first: the API writes آ as ا + madda, which stripping the harakat would turn into ا (ال عمران)
        name = re.sub("[\u064B-\u065F\u0670\u06D6-\u06ED]", "", unicodedata.normalize("NFC", v["surah"]))
        name = name.replace("\u0671", "ا")   # ٱ: no glyph in Thmanyah
        out.append({"text": text, "surah": name, "ayah": v["ayah"]})
    return out


HADITH_MAX_WORDS = 70   # a longer hadith does not fit the card beside the clock; it stays in the data
ISNAD_MAX_WORDS = 35    # nor is one shown whose chain of narrators runs longer (.github/tools/verify_hadith.py)
HARAKAT = "[\u064B-\u0652\u0670]*"
SAW = re.compile(r"\s+".join(HARAKAT.join(w) + HARAKAT for w in "صلى الله عليه وسلم".split()))


def narration_parts(text):
    """A full narration as the book has it -> [["n", narration], ["q", the Prophet's words], ...].

    The books put his words between straight quotes; everything else (the isnad, who asked what, the compiler's
    note) is narration. Only typography changes: direction marks and the tatweel used as a dash go, «صلى الله
    عليه وسلم» becomes ﷺ, {verse} becomes ﴿verse﴾, and no space before a comma or full stop."""
    t = re.sub("[\u200e\u200f]", "", text)
    t = re.sub(r"\s+ـ\s+", " ", t)
    t = SAW.sub("ﷺ", t).replace("{", "﴿").replace("}", "﴾")
    t = re.sub(r"\s+([،,.:؛])", r"\1", t)
    parts = []
    for i, piece in enumerate(t.split('"')):
        piece = " ".join(piece.split()).strip(" .")
        if piece and piece not in ".،:":
            parts.append(["q" if i % 2 else "n", piece])
    return parts


LINK = re.compile(r"(?:^|(?<=[\s،,:]))(?:عن|سمعت|سمع|سمعا|حدثني|حدثنا|أخبرني|أخبرنا|أن|أنه|إن|يحدث|(?<=قال )قال)(?=[\s،,:])")
TRAIL = re.compile(r"(?:[\s،,:.]|\b(?:قال|قالت|يقول|سمعت|سمع|عن|أن|أنه|إن|أخبره|حدثه|لي|لنا)\b)+$")
SECOND_CHAIN = re.compile(r"ﷺ\s*\.\s*و?(?:عن|حدثنا|حدثني)\s")
NOT_A_NAME = {"أبيه", "أبيها", "جده", "أذناى", "أذناي", "أذني", "رجل", "رجلا"}
NOTE = re.compile(r"قال أبو عيسى|وفي الباب|حدثنا|حدثني|أخبرنا|وفي رواية|وفي حديث|وبهذا الإسناد|وزاد|نحوه|قال أبو داود|قال أبو معاوية")
PROPHET = re.compile(r"رسول الله|النبي")


def bare_map(text):
    """text without harakat, and for each of its characters the index in text (to cut the original there)."""
    keep = [(c, i) for i, c in enumerate(text) if not re.match(HARAKAT[:-1], c)]
    return "".join(c for c, _ in keep), [i for _, i in keep] + [len(text)]


def simple_parts(parts):
    """The «Riyad as-Salihin» form: from the companion who narrated it, «عن أبي هريرة …», then the hadith as the book
    has it. The narrators before the companion, and the compiler's notes after the Prophet's last words, go."""
    if not parts or parts[0][0] != "n":
        return parts
    head = parts[0][1]
    bare, at = bare_map(head)
    second = list(SECOND_CHAIN.finditer(bare))   # «… عن النبي ﷺ. وحدثنا …»: the book gives a second chain, use it
    if second:
        head = head[at[second[-1].end()]:]
        bare, at = bare_map(head)
    m = PROPHET.search(bare)
    if not m:
        return None
    pre = TRAIL.sub("", bare[:m.start()].rstrip())
    links = list(LINK.finditer(pre))
    if not links:
        return None
    pick = -1   # «عن أبيه», «عن جده», «سمعت أذناي», «قال رجل»: start a narrator earlier until it says who
    while pre[links[pick].end():].strip(" ،,:").split(" ")[0].strip("،,") in NOT_A_NAME and len(links) > -pick:
        pick -= 1
    start = links[pick].end()
    while start < len(bare) and bare[start] in " ،,:":
        start += 1
    rest = head[at[start]:].strip()
    rest = re.sub("^" + HARAKAT.join("أبا") + "|^" + HARAKAT.join("أبو"), "أبي", rest)   # after «عن»: أبي هريرة
    rest = re.sub(r"\s" + HARAKAT.join("أخبره") + HARAKAT + "(?:" + HARAKAT.join("ما") + "|م)?" + HARAKAT + r"(?=\s)", "", rest)   # «… أخبره أن …»
    rest = re.sub("^([^\\s،,]{3,}?)" + "ا" + "[\u064B\u064E]*" + r"(?=[\s،,])", r"\1", rest)   # «سمعت أنسا» -> «عن أنس»
    out = [["n", "عن " + rest]]
    for k, t in parts[1:]:   # stop at the compiler's notes or a further chain between his words
        if k == "n" and NOTE.search(re.sub(HARAKAT, "", t)):
            break
        out.append([k, t])
    while out and out[-1][0] == "n":
        out.pop()
    return out


WEAK = re.compile(r"da.?if|weak|munkar|mawdu|fabricat|shadh|mudtarib", re.I)   # as .github/tools/verify_hadith.py
ALBANI_OK = {"sahih", "hasan", "hasan sahih"}   # as .github/tools/verify_hadith.py
GRADE_AR = {"sahih": "صحيح", "hasan": "حسن", "hasan sahih": "حسن صحيح", "sahih lighairihi": "صحيح لغيره",
            "hasan lighairihi": "حسن لغيره"}


def scholars(grades):
    """The four Sunan: Al-Albani's grade decides when the scholars differ; with no Al-Albani, all of them."""
    albani = [g for g in grades or [] if "albani" in g.get("name", "").lower()]
    return albani or grades or []


def weak_by_scholars(grades):
    return any(WEAK.search(g.get("grade", "")) for g in scholars(grades))


def albani_accepts(book, grades):
    """Bukhari and Muslim: yes. The four Sunan: only if Al-Albani grades the narration Sahih, Hasan or Hasan Sahih;
    one he grades weak in any form, or does not grade, is never shown."""
    if book in ("صحيح البخاري", "صحيح مسلم"):
        return True
    albani = [g.get("grade", "") for g in grades or [] if "albani" in g.get("name", "").lower()]
    return bool(albani) and all(" ".join(re.sub(r"[^a-z ]", " ", a.lower()).split()) in ALBANI_OK for a in albani)


def albani_grade(grades):
    for g in grades or []:
        if "albani" in g.get("name", "").lower():
            ar = GRADE_AR.get(re.sub(r"[^a-z ]", "", g.get("grade", "").lower()).strip())
            return f"{ar} عند الألباني" if ar else ""
    return ""


def tirmidhi_grade(text):
    bare = re.sub(HARAKAT, "", text)
    if "قال أبو عيسى" not in bare:
        return ""
    g = re.search("حسن صحيح|صحيح حسن|حسن غريب|صحيح غريب|حسن|صحيح", bare[bare.index("قال أبو عيسى"):])
    return g.group(0) if g else ""


def weak_in_tirmidhi(text):
    """al-Tirmidhi grades each hadith after it («قال أبو عيسى …»). Not shown: one he grades only غريب or ضعيف,
    or says is more correctly a companion's own words (موقوف, «ولم يرفعه»)."""
    bare = re.sub(HARAKAT, "", text)
    if "قال أبو عيسى" not in bare:
        return False
    note = bare[bare.index("قال أبو عيسى"):]
    return not re.search("حديث حسن|حديث صحيح|حسن صحيح", note) or "ولم يرفعه" in bare or "موقوفا" in bare


def hadith_for_screen():
    """assets/shared/hadith.json -> each hadith from its companion on (simple_parts) if it fits the card, with the
    book, its number and a grade: al-Tirmidhi's own, or for the other Sunan Al-Albani's. One a scholar grades weak,
    one from the four Sunan Al-Albani does not grade Sahih, Hasan or Hasan Sahih, or one al-Tirmidhi does not grade
    حسن or صحيح, is not shown."""
    digits = str.maketrans("0123456789", "٠١٢٣٤٥٦٧٨٩")
    out = []
    for h in json.loads((ROOT / "assets/shared/hadith.json").read_text(encoding="utf-8")):
        full = h.get("full")
        if not (h.get("found") and full):
            continue
        whole = narration_parts(full["text"])
        if whole and whole[0][0] == "n" and len(whole[0][1].split()) > ISNAD_MAX_WORDS:
            continue
        parts = simple_parts(whole)
        if not parts:
            continue
        words = sum(len(p[1].split()) for p in parts)
        if weak_in_tirmidhi(full["text"]) or weak_by_scholars(full.get("grades")) \
                or not albani_accepts(full["book"], full.get("grades")):
            continue
        if any(k == "q" for k, _ in parts) and words <= HADITH_MAX_WORDS:
            grade = tirmidhi_grade(full["text"]) or albani_grade(full.get("grades"))
            source = f"{full['book']} · {str(full['number']).translate(digits)}" + (f" · {grade}" if grade else "")
            out.append({"parts": parts, "source": source})
    return out


def data_uri(path, mime):
    return f"data:{mime};base64," + base64.b64encode(path.read_bytes()).decode("ascii")


def js_json(value):
    """json.dumps for a <script>: a "</script>" inside a text would end it, "<\\/" is the same string in JSON."""
    return json.dumps(value, ensure_ascii=False).replace("</", "<\\/")


def main():
    html = (ROOT / "src/template.html").read_text(encoding="utf-8")
    # 2 KB, so inlined in both pages
    html = html.replace("__F_MARKS__", data_uri(ROOT / "assets/fonts/NotoNaskhArabic-Marks.woff2", "font/woff2"))
    teams = json.loads((ROOT / "assets/shared/teams-ar.json").read_text(encoding="utf-8"))
    html = html.replace("__TEAMS_AR__", js_json(teams))
    # by TheSportsDB id: "Al-Faisaly" and "Al Faisaly" are different clubs under one name key
    clubs = json.loads((ROOT / "assets/shared/teams.json").read_text(encoding="utf-8"))
    by_id = {c["id"]: c["ar"] for c in clubs if c.get("id") and c.get("ar")}
    html = html.replace("__TEAMS_BY_ID__", js_json(by_id))
    html = html.replace("__VERSES__", js_json(verses_for_screen()))
    html = html.replace("__HADITH__", js_json(hadith_for_screen()))   # checked by CI
    prayer_path = ROOT / "assets/shared/benghazi-prayers-2026.json"
    html = html.replace("__BENGHAZI_PRAYERS_2026__", js_json(json.loads(prayer_path.read_text(encoding="utf-8"))))

    bg_dir = ROOT / "assets/backgrounds"
    bgs = json.loads((bg_dir / "backgrounds.json").read_text(encoding="utf-8"))
    for name, inline in (("urtv.html", True), ("tv.html", False)):
        page = html
        for token, font in FONTS.items():
            page = page.replace(token, data_uri(ROOT / "assets/fonts" / font, "font/woff2") if inline
                                else REPO + "assets/fonts/" + font)
        entries = []
        for bg in bgs:
            u = data_uri(bg_dir / bg["file"], "image/jpeg") if inline else REPO + "assets/backgrounds/" + bg["file"]
            entries.append(f'  {{u:"{u}", c:{js_json(bg["credit"])}, d:{bg["brightness"]}}}')
        out = ROOT / "dist" / name
        out.parent.mkdir(exist_ok=True)
        out.write_text(page.replace("__BG_SET__", ",\n".join(entries)), encoding="utf-8")
        print(f"wrote {out.relative_to(ROOT)} ({out.stat().st_size / 1e6:.1f} MB)")


if __name__ == "__main__":
    main()
