<p align="center"><img src=".github/brand/hero.png" alt="تلفزيونك · UrTV" width="100%"></p>

<h1 align="center">تلفزيونك · UrTV</h1>

<p align="center" dir="rtl">واجهة رئيسية عربية وشاشة توقف سعودية لتلفزيونات Android TV و Google TV، وتسريع آمن للتلفزيون.<br>يركّبها لك مساعد ذكاء اصطناعي بأمر واحد.</p>

<p align="center">An Arabic home screen and a Saudi screensaver for Android TV / Google TV, and a safe speed-up.<br>Your AI agent sets it all up.</p>

<p align="center"><img src=".github/screens/before-after-home.jpg" alt="Before: Google TV home. After: the تلفزيونك home screen" width="100%"></p>
<p align="center"><img src=".github/screens/before-after-screensaver.jpg" alt="Before: the Google screensaver. After: شاشة تلفزيونك" width="100%"></p>

<div dir="rtl">

## ركّبه في 3 خطوات

### ① التلفزيون
الإعدادات ← حول ← اضغط OK على **Build** سبع مرات. بعدها روح إلى **خيارات المطوّر** ← شغّل **USB debugging**.

### ② كمبيوتر على نفس الواي فاي، وعليه واحد من هذي

| **Codex** (OpenAI) | **Claude Code** (Anthropic) |
|---|---|
| مجاني بحساب ChatGPT | يحتاج اشتراك Claude Pro (20$ بالشهر) |
| حمّل تطبيق Codex | حمّل تطبيق Claude ← تبويب **Code** |
| الموافقات: اختر **«Approve for me»** | الموافقات: **Allow** للمهمة كلها |

<sub>ويندوز: ركّب Git وPython أول (`winget install Git.Git Python.Python.3.12`). Gemini CLI مجاني وناس قالوا يشتغل؛ ما جرّبناه.</sub>

### ③ الصق هذا

> ركّب تلفزيوني من https://github.com/tshi-dev/UrTV: انسخ المستودع واتبع `docs/SETUP-PROMPT.md` خطوة بخطوة. أنت على كمبيوتر متصل بنفس شبكة الواي فاي حقت التلفزيون.

#### وش يصير بعد اللصق؟
يسألك عن **مدينتك** و**أنديتك**، والتلفزيون يسألك **Allow** مرة وحدة، وبعدها خلّه يشتغل: 40 دقيقة وكم إعادة تشغيل.

#### بترجعه مثل ما كان؟
نعم، بأي وقت: تطبيقاتنا تنحذف، واللي أطفأناه يرجع، وشاشة التلفزيون الأصلية ترجع.
<sub>`optimize/reset.sh`</sub>

#### نزلت نسخة جديدة؟
حدّث التطبيقين وإعداداتك كلها باقية. الشاشة نفسها تتحدّث لحالها. وصف «تحديث الشاشة الآن» في الإعدادات يخبرك متى يوجد إصدار جديد.
<sub>`optimize/install.sh all`</sub>

#### نزل تحديث للنظام وقفل تطبيقنا؟
أعد الإعداد كله بأمر واحد، وإعداداتك باقية.
<sub>`optimize/run.sh again`</sub>

</div>

## Set it up in 3 steps

### ① The TV
Settings → About → press OK on **Build** 7 times. Then go to **Developer options** → turn on **USB debugging**.

### ② A computer on the same Wi-Fi, with one of these

| **Codex** (OpenAI) | **Claude Code** (Anthropic) |
|---|---|
| Free with a ChatGPT account | Needs Claude Pro ($20/month) |
| Download the Codex app | Download the Claude app → **Code** tab |
| Approvals: choose **«Approve for me»** | Approvals: **Allow** for the whole task |

<sub>Windows: install Git and Python first (`winget install Git.Git Python.Python.3.12`). Gemini CLI is free and reported to work; not tested by us.</sub>

### ③ Paste this

> Set up my TV with https://github.com/tshi-dev/UrTV: clone it and follow `docs/SETUP-PROMPT.md` step by step. You are on a computer on the same Wi-Fi as the TV.

#### What happens after pasting?
It asks your **city** and your **clubs**, the TV asks **Allow** once, then leave it: 40 minutes, a few restarts.

#### Can I put it back as it was?
Yes, anytime: our apps are removed, what we turned off comes back on, the TV's own screen returns.
<sub>`optimize/reset.sh`</sub>

#### A new version came out?
Update both apps; all your settings stay. The screen itself updates on its own. The settings row tells you when one exists.
<sub>`optimize/install.sh all`</sub>

#### The TV updated its system and locked our app out?
Redo the whole setup with one command, settings kept.
<sub>`optimize/run.sh again`</sub>

**Safe:** nothing is uninstalled; AirPlay, Chromecast and voice are never touched; a 5-minute safety switch puts the
stock home back if ours doesn't come up. The apps are built and signed by GitHub Actions from this repo (version
3.N = the build number). The full prompt: [docs/SETUP-PROMPT.md](docs/SETUP-PROMPT.md). To build a different
screen: [docs/RECREATE-PROMPT.md](docs/RECREATE-PROMPT.md).

## What you get

| | | |
|---|---|---|
| **شاشة تلفزيونك** · screensaver | Clock, Hijri date, prayer times and iqama, Ramadan, your teams' matches, weather, markets, and beside the clock your choice of Arabic general news, new episodes, today's prayers, the 5-day forecast, all your teams' games, or a verse/hadith of the day, over Saudi landscapes | `urtv-screensaver.apk` |
| **تلفزيونك** · home screen | Clean Arabic launcher: favourites row (5–10 apps) arranged in «ترتيب المفضلة», all apps, inputs, Wi-Fi, settings; optional card from the screensaver (off by default) | `urtv-home.apk` |
| **Two free apps** | [TV Bro](https://github.com/truefedex/tv-bro), a web browser for the TV, and [SmartTube](https://github.com/yuliskov/SmartTube), YouTube without ads, from their own releases (skip with `SKIP_EXTRAS=1`) | `optimize/install.sh extras` |
| **Speed-up** | Telemetry, ads and demos off (reversibly), closed apps frozen, Play Store kept from running in the background, faster animations | `optimize/` |

**Download the apps yourself:** [releases/apk-latest](https://github.com/tshi-dev/UrTV/releases/tag/apk-latest)
(install with `adb install`; the agent route above also sets everything up).
Every build is also kept as a numbered release (`v3.N`, the apps' version), to go back to one:
`APK_TAG=v3.N optimize/install.sh all`.

## For agents and scripts

Everything is scriptable over ADB, made for slow Wi-Fi (retries, resumable uploads, every step remembered):

| | |
|---|---|
| `optimize/find-tv.sh` | `--check`: can this terminal reach the home network at all (not from a cloud or sandbox) · then finds the TV (Windows, Mac, Linux) |
| `optimize/run.sh` | the whole setup; run again after any failure to continue · `status` · `again` after a TV system update (redo all, settings kept) |
| `optimize/saver.sh` | screensaver settings: `show`, `cities`, `city`, `find-team`, `teams`, `card`, `home-card`, `genres`, `langs`, `minrating`, `photos`, `idle`, `update` |
| `optimize/dock.sh` | favourites row: `show`, `apps`, `set`, `reset`; `hidden`, `hide PKG`, `show PKG` for apps left off the home screen |
| `optimize/measure.sh` · `shot.sh` | memory before/after (restart + settle) · screenshots without navigating |
| `optimize/keep.sh` | turn back on anything protected (AirPlay, casting, voice, updates…) that something turned off |
| `docs/TVS.md` | what was seen on each TV model (ADB port, home switch, what to watch out for) |
| `optimize/reset.sh` | back to stock: lists re-enabled, stock home and screensaver back, our apps removed |
| `optimize/share.sh` | if the owner wants: how the setup went (TV model, time per step, what failed, memory before/after; nothing personal), sent anonymously to the author (from their phone by a QR code if this computer is offline) |

What we learned on real TCL sets, and what must stay on: [docs/OPTIMIZE.md](docs/OPTIMIZE.md). Manual test plan:
[docs/TEST-PLAN.md](docs/TEST-PLAN.md).

## For developers

```
src/template.html     the screen (HTML/CSS/JS, no framework); edit this, then run src/build.py
src/build.py          builds the two pages in dist/
dist/urtv.html        stand-alone page, fonts and photos inlined: open in any browser (F = full screen), works offline
dist/tv.html          light page the Android app loads (0.3 MB); the app serves fonts and photos from disk
android/app           the screensaver app (sa.urtv.screensaver): DreamService, settings, auto-update
android/launcher      the home screen (sa.urtv.home)
assets/               fonts, bundled photos, Arabic team names, verse and hadith texts
photos/               the hosted photo set (5 shown per 4 days, rotating every 30 min)
optimize/             TV tools (Python ADB client, no adb binary needed); lists/ says what may be turned off
docs/                 the agent prompts (setup, recreate), what we learned on real TVs, test plan, photo credits
.github/              CI: APK builds, checks, data tools (verses, hadith, clubs, photos), README images
tests/run.sh          every check: build, data, fonts, scripts, private data, flaky-ADB upload, the screen in a browser
```

A push to `master` builds both APKs in GitHub Actions and publishes them to `apk-latest` and a numbered `v3.N`
release; a push to `next` publishes `apk-next` instead (a test build). `tests/run.sh` runs on `master`, `next`,
`content` and pull requests. Local Android build (needs the SDK): `python3 src/build.py && gradle -p android assembleRelease`
(signed with the debug key; the release key lives only in the repository's secrets).

**Screen settings** come from the URL fragment (works on `file://` and in WebView):

```
urtv.html#city=جدة&lat=21.5433&lon=39.1728&tz=Asia/Riyadh&teams=136013,136137&panel=prayers
```

`teams`: up to 10 TheSportsDB ids in priority order. `panel`: the card beside the clock: `news`, `shows`, `prayers`,
`forecast`, `matches`, `verse` or `none`. Also `genres`, `langs`, `shows`, `minrating` for the episodes card.

**Data** (free, no keys): Aladhan (prayer times, Umm al-Qura), Open-Meteo (weather), Yahoo and CoinGecko (markets),
TVMaze (episodes), TheSportsDB (matches), and Google News RSS in Arabic for Saudi Arabia (with publisher attribution). Everything fails soft: offline, the clock, dates, prayer and Ramadan
countdowns still work and the last good data stays on screen. A finished game's score stays up for a day.

**Verse and hadith texts are never typed by hand:** verses come from the Quran API via CI
(`.github/tools/fetch_verses.py`); each hadith is checked by CI (`.github/tools/verify_hadith.py`) against the
Arabic text of the six Sunni books, and only those found are shown, with the books as the source line.

**The Android apps** update the screen page from this repo every 6 hours (so it must stay public), start fast
(the page shows once the clock, saved panels and photo are ready), and come back after a power cut.

## Credits

Photos: Unsplash photographers, listed in [docs/PHOTO-CREDITS.md](docs/PHOTO-CREDITS.md). Fonts: Thmanyah, and
Noto Naskh Arabic for ﷺ ﴿﴾ (OFL, `assets/fonts/`).
Home screen adapted from [mihirgupta0900/tv-home](https://github.com/mihirgupta0900/tv-home) (MIT,
`android/launcher/LICENSE-tv-home`). Package ratings from [UAD-ng](https://github.com/Universal-Debloater-Alliance/universal-android-debloater-next-generation).
