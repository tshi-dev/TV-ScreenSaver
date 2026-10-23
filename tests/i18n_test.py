#!/usr/bin/env python3
"""The four dictionaries in src/i18n.json, and the page labels that use them.
Every language has the Arabic keys, the same placeholders and the same weather codes; the Latin
languages carry no Arabic script; and every key the page asks for is translated in all four.
Run: python3 tests/i18n_test.py"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANGS = ("ar", "en", "fr", "es")
ARABIC = re.compile(r"[\u0600-\u06FF]")
PLACEHOLDER = re.compile(r"\{(\w+)\}")

failed = 0


def check(name, ok, detail=""):
    global failed
    print(("PASS " if ok else "FAIL ") + name + ("" if ok else f"  -> {ascii(detail)}"))
    failed += not ok


data = json.loads((ROOT / "src/i18n.json").read_text(encoding="utf-8"))
check("src/i18n.json has ar, en, fr and es", set(data) == set(LANGS), sorted(data))

base = set(data["ar"])
for lang in LANGS[1:]:
    check(f"{lang}: the Arabic keys, no more and no fewer", set(data[lang]) == base,
          f"missing {sorted(base - set(data[lang]))}, extra {sorted(set(data[lang]) - base)}")
    check(f"{lang}: the same weather codes", set(data[lang]["wx"]) == set(data["ar"]["wx"]),
          sorted(set(data[lang]["wx"]) ^ set(data["ar"]["wx"])))
    check(f"{lang}: no empty strings",
          not [k for k, v in data[lang].items() if isinstance(v, str) and not v.strip()],
          [k for k, v in data[lang].items() if isinstance(v, str) and not v.strip()])
    check(f"{lang}: no Arabic script", not any(ARABIC.search(v) for v in data[lang].values() if isinstance(v, str)),
          [k for k, v in data[lang].items() if isinstance(v, str) and ARABIC.search(v)])

for key in sorted(base):
    if not isinstance(data["ar"][key], str):
        continue
    want = set(PLACEHOLDER.findall(data["ar"][key]))
    for lang in LANGS[1:]:
        got = set(PLACEHOLDER.findall(data[lang][key]))
        check(f"{key}: {lang} placeholders {sorted(want)}", got == want, sorted(got))

# digits decide the percent sign, not the language: Arabic with Latin digits shows %, as the page tests expect
check("market.percent: Arabic percent sign", data["ar"]["market.percent"] == "٪", data["ar"]["market.percent"])
for lang in LANGS[1:]:
    check(f"market.percent: % in {lang}", data[lang]["market.percent"] == "%", data[lang]["market.percent"])

# the national day says the number alone; the founding day keeps its unit
check("national.unit is gone from every language",
      all("national.unit" not in data[lang] for lang in LANGS),
      [lang for lang in LANGS if "national.unit" in data[lang]])

for code in data["ar"]["wx"]:
    for lang in LANGS:
        check(f"weather {code} in {lang}", bool(data[lang]["wx"][code].strip()), data[lang]["wx"][code])

# every key the page writes literally, or marks with data-i18n, is in every dictionary
tpl = (ROOT / "src/template.html").read_text(encoding="utf-8")
keys = set(re.findall(r"\bt\(\s*['\"]([^'\"]+)['\"]", tpl))
keys |= set(re.findall(r'data-i18n="([^"]+)"', tpl))
for key in sorted(keys):
    if key.endswith("."):
        continue   # a prefix built by concatenation, checked with its family below
    missing = [lang for lang in LANGS if key not in data[lang]]
    check(f"page key {key}", not missing, missing)

# the families the page builds by concatenation: one check that each member exists everywhere
families = ["slot.matches", "slot.weather", "slot.fasting", "slot.countdown", "slot.markets"]
families += ["national.name", "national.short", "national.greeting",
             "founding.name", "founding.short", "founding.greeting", "founding.unit"]
families += [f"unit.{u}.{f}" for u in ("day", "hour", "min") for f in ("one", "two", "few", "many", "other")]
families += [f"occ.{o}.{s}" for o in ("ramadan", "eid_fitr", "dhul_hijjah", "arafah", "eid_adha", "newyear", "ashura")
             for s in ("name", "today")]
families += [f"fast.{k}" for k in ("ramadan", "arafah", "tasua", "ashura", "white", "mon", "thu", "shawwal")]
families += [f"f1.{k}" for k in ("gp", "sprintQuali", "sprint", "practice", "quali", "race")]
for key in families:
    missing = [lang for lang in LANGS if key not in data[lang]]
    check(f"built key {key}", not missing, missing)

# the settings app carries the same choices, and the page always receives the effective pair
prefs = (ROOT / "android/app/src/main/java/sa/urtv/screensaver/Prefs.java").read_text(encoding="utf-8")
for value in ("auto", "ar", "en", "fr", "es"):
    check(f"Prefs.UI_LANGS has {value}", f'"{value}"' in prefs, "")
for value in ("auto", "hindi", "latin"):
    check(f"Prefs.DIGITS has {value}", f'"{value}"' in prefs, "")
check("Prefs sends lang= in the fragment", '"&lang=" + lang()' in prefs, "")
check("Prefs sends the effective digits in the fragment", '"&digits=" + digitsEffective()' in prefs, "")
check("Prefs.digitsEffective follows the language for auto",
      '"ar".equals(lang()) ? "hindi" : "latin"' in prefs, "")

sys.exit(1 if failed else 0)
