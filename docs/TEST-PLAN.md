# Test plan — شاشة تلفزيونك

For a tester (human or agent) with a Linux/macOS machine, `adb`, and an Android TV on the same network.
Every step has an exact action and the expected result. Record **PASS / FAIL + note + screenshot** for each ID.

Screenshots: `adb exec-out screencap -p > ID.png`
Logs on failure: `adb logcat -d -t 300 | grep -iE "urtv|chromium|AndroidRuntime" > ID.log`

---

## 0. Setup

| ID | Action | Expected |
|---|---|---|
| S1 | On the TV: Settings → Device preferences → About → press Build 7× → Developer options → enable **USB/Network debugging**. Note the TV's IP. | Debugging on |
| S2 | `adb connect <TV_IP>:5555` then `adb devices` | Device listed as `device` |
| S3 | `curl -L -o urtv-screensaver.apk https://github.com/tshi-dev/UrTV/releases/download/apk-latest/urtv-screensaver.apk` | ~2–5 MB file. To test the `next` branch, use the tag `apk-next` in the URL instead of `apk-latest` (with the scripts: `APK_TAG=apk-next optimize/install.sh saver`) |
| S4 | `adb uninstall sa.yourtv.screensaver` and `adb uninstall com.abdullah.tvsaver` (ignore "not installed") | The pre-rename app is gone (it would show twice) |
| S5 | `adb install urtv-screensaver.apk` | `Success` |
| S6 | `adb shell dumpsys package sa.urtv.screensaver \| grep versionName` | `versionName=3.<number>` |

## 1. Settings screen

Open: `adb shell am start -n sa.urtv.screensaver/.SettingsActivity`
Navigate with the remote, or `adb shell input keyevent DPAD_DOWN|DPAD_UP|DPAD_CENTER|BACK`.

| ID | Action | Expected |
|---|---|---|
| A1 | Open the app from the TV launcher | Banner «شاشة تلفزيونك»; settings list opens, right-to-left, first row focused |
| A2 | Move through every row with D-pad | Every row focusable and highlighted; nothing unreachable |
| A3 | المدينة → open | First item «بحث عن مدينة أو قرية…», then 13 regions with counts |
| A4 | Region «منطقة الرياض» → pick «الخرج» | Dialog closes, row shows «المدينة: الخرج» |
| A5 | المدينة → search → type `ابها` or `Abha` | Results list from the internet; pick one → row updates |
| A6 | المدينة → search → type `zzzzqq` | Toast «لا توجد نتائج» |
| A7 | الفرق المتابعة → «تصفح الدوريات…» → each league one by one, and «المنتخبات» | Each opens at once, even offline, with every club of the last two seasons in Arabic (MLS: 30). **Record any league that is short or shows «لا توجد نتائج»** |
| A8 | الفرق المتابعة → «تصفح الدوريات…» → دوري روشن → pick «النصر» | Toast «تمت متابعة النصر» right away (no network needed), row shows «النصر» |
| A9 | الفرق المتابعة → «بحث عن أي فريق أو منتخب…» → `Real Madrid`, then `الاهلي`, `العربي`, `atletico` | Results at once: «ريال مدريد»; the Saudi «الأهلي» first, then «الأهلي المصري»; the Saudi «العربي» before «العربي القطري»; «أتلتيكو مدريد». Pick one → row shows its Arabic name |
| A10 | الفرق المتابعة → دوري يلو | Every club of the league, «الزلفي» among them |
| A11 | التصنيفات → tick دراما + كوميديا → حفظ | Row lists both |
| A12 | اللغات → tick الإنجليزية → حفظ | Row shows it |
| A13 | مسلسلات مختارة → «إضافة مسلسل» → `The Last of Us` → pick | Toast «تمت إضافة …», row lists the show (English title) |
| A14 | مسلسلات مختارة → untick it → حفظ | Removed |
| A15 | أقل تقييم → 8 | Row shows 8 |
| A16 | التحديث التلقائي → press | Toggles يعمل/متوقف; set back to يعمل |
| A17 | تحديث الشاشة الآن | Toast «تم التحديث» or «الصفحة محدثة» (not «لا يوجد اتصال») |
| A17b | The lines under «تحديث الشاشة الآن» (focus that row: the list scrolls to them) | Plain text, in no button: «الإصدار الحالي: ٣٫N», «الإصدار المتاح: …» (the number, «لا يوجد أحدث» or «لم يُفحص بعد»), «طريقة التحديث: …حدّث تلفزيوني…»; the row itself says only «ينزّل أحدث نسخة من الشاشة» |
| A18 | بدء شاشة التوقف بعد (TV without «Modify system settings» for the app, the usual case) | The row shows the TV's current delay (e.g. «١٠ دقائق»); pressing it shows the toast «غيّرها من إعدادات التلفاز، أو بأمر من مساعدك» and opens the TV's screensaver settings; the delay is unchanged |
| A18b | بدء شاشة التوقف بعد, after `adb shell appops set sa.urtv.screensaver WRITE_SETTINGS allow` | Pressing it lists دقيقة واحدة, دقيقتان, ٥ دقائق, ١٠ دقائق, ١٥ دقيقة, ٣٠ دقيقة with the current one selected; pick ٥ دقائق → the row says «٥ دقائق» and `adb shell settings get system screen_off_timeout` prints `300000`; set it back afterwards |
| A18c | الأرقام → عربية 123, then preview; then back to هندية ١٢٣ | With 123 the clock, both dates, prayer times, countdowns, weather, scores and the home screen card show 0-9; with ١٢٣ they show ٠-٩ as before; the row shows the choice |
| A18d | شكل البطاقة → each of الحالية, داكنة, زجاجية, داكنة بإطار, زجاجية بإطار, then preview and wait over a minute | الحالية is the card as before. The others change the card's tint and corners; داكنة has a thin gold line on the side facing the clock; the «بإطار» ones have an inner gold frame with a diamond top and bottom and the header centred. For the first minute (the photo is still drifting) the card is a plain tint; after it the photo behind the card is blurred, lined up with the photo around it, and nothing on screen moves or flickers. The home screen card keeps its own look |
| A18c | تبديل الصورة كل | Row shows «٣٠ دقيقة» on a fresh install; the list has ٥ دقائق, ١٠ دقائق, ١٥ دقيقة, ٣٠ دقيقة, ٦٠ دقيقة; pick ٥ دقائق → the row shows it, and in the preview the photo changes at the next five-minute mark (hh:00, :05, …) |
| A19 | Force-close and reopen: `adb shell am force-stop sa.urtv.screensaver` then A1 | All choices from A4–A15 kept |

## 2. Preview / screen content

Open: «معاينة الشاشة» row, or `adb shell am start -n sa.urtv.screensaver/.MainActivity`. Wait 60 s for data.

| ID | Check | Expected |
|---|---|---|
| B1 | Overall | Full screen, photo background, no white flash, no scrollbars, no cursor, all text Arabic except show titles and «S&P 500» |
| B2 | Clock | 12-hour Arabic digits, no two digits touching (e.g. ٤٨), gold colon, small gold ص/م at the **upper left**; centred between the card and the right edge; matches the real Riyadh time |
| B3 | Under clock | «بتوقيت <chosen city>» |
| B4 | Top right | Hijri date (with هـ) over Gregorian date (Gregorian month name, e.g. أكتوبر) |
| B5 | Top left (no boxes) | Temperature ° + condition word (or «غبار» in gold); city · high/low; gold line from the left edge; تاسي / S&P 500 / بيتكوين each with ▲ green or ▼ red (or no arrow — never a dash) |
| B5b | Settings → الأسواق → pick 4 (e.g. تاسي, الذهب, الفضة, بيتكوين) and «عرض الأسواق» → السعر والنسبة, then preview | The four in the order of the list, each with its price (index and bitcoin with no decimals; gold and silver per gram with 2 decimals in ريال), ▲ green / ▼ red and the day's % change; under them «آخر تحديث ٩:١٥ م». Picking a fifth is refused with a message. Offline the last values stay; a market the TV cannot read shows no number and no arrow |
| B6 | Markets accuracy | Compare arrows with today's TASI, S&P 500 and BTC 24h direction on any finance site |
| B7 | هذا الأسبوع (set «بجانب الساعة» to المسلسلات first: the default card is the verse card) | Up to 5 shows matching A11–A15 filters, with season/episode, rating, weekday. Changes page every 25 s |
| B7b | Settings → بجانب الساعة → each option, then preview | مواقيت الصلاة: 6 rows, next prayer gold, past ones dim · طقس الأيام القادمة: اليوم, غداً, then weekdays with condition and high/low · مباريات فرقك: every followed team's games this week in time order, four to a page; an upcoming game has «بعد ٣ أيام» on the left and under its teams, small, «السبت ٢٤ أكتوبر · ٩:١٦ م» (fits with every card style) · آية أو حديث اليوم: a verse with surah and ayah, or a hadith with its source; no empty boxes (a long verse or hadith is set a step smaller, never cut) · حكمة اليوم: a proverb with its source (مثل عربي), a new one each day · لا شيء: no card |
| B7c | Settings → بجانب الساعة → أذكار الصباح والمساء, then preview (needs the adhkar checked by CI: `apply_adhkar_report.py`) | Between Fajr and Dhuhr: «أذكار الصباح» with a sun icon and «حتى الظهر»; between Asr and Maghrib: «أذكار المساء», a moon and «حتى المغرب». Each dhikr 20 s + 0.1 s per character (at most 60 s), then the next with the soft page turn; footer: source on the right, the count in words in the middle, dots (the current one gold) on the left. Any other time: the verse or hadith of the day, never an empty card. Works with every card style |
| B7d | `optimize/saver.sh card stocks`, then `optimize/saver.sh stocks 2222 1120 AAPL NVDA`, then preview | «الأسهم»: a row for each, Arabic names for Saudi codes (أرامكو, الراجحي), the ticker for US ones; the price, ▲ green / ▼ red with the day's %, and a small line of the day. «السوق مغلق» in the header when every market listed is closed (Tadawul Sun–Thu 10:00–15:00, the US Mon–Fri 16:30–23:00, Riyadh time), the last close shown; with a mix, the closed rows say «مغلق». More than 5 stocks turn pages every 25 s. Fits with every card style. Settings → الأسهم → قائمة الأسهم edits the list |
| B8 | الصلاة القادمة | Next prayer name + time + «بعد …». Compare with https://www.ummulqura.org.sa or the Aladhan site for the chosen city: within 1–2 min |
| B9 | المناسبة القادمة panel (middle of the bottom row) | The next occasion's name, days left, and its expected date with «متوقّع» (عاشوراء adds «ويُستحب صيام التاسع»). On the day itself «اليوم …», during Eid «عيد مبارك», no countdown. (In Ramadan: الإفطار/الإمساك instead) |
| B10a | الخانة اليسرى (`slot`) | `matches` as B10 (with no team followed: the weather, «الطقس», the next three days). `weather`: «الجمعة ٣٤° · السبت ٣٣° · الأحد ٣٥°» as three small blocks with their weather words. `fasting` «الصيام»: «صيام الخميس» / «غدًا الخميس», «الأيام البيض» / «بعد ٣ أيام», «يوم عرفة» / «اليوم»; never the two Eids or أيام التشريق; in Ramadan «صيام رمضان» / «اليوم ٧ من رمضان». `countdown` «عدّ تنازلي»: «٤٠ يوم» and the name, «اليوم» on the day, the weather after it. `none`: the slot is gone and the other two panels share the row |
| B10b | المناسبات الوطنية (`national`) | Fake the date or wait for the days. 21–25 September (Riyadh): the gold accents are Saudi green, a small «اليوم الوطني السعودي ٩٦» (year − 1930) under the dates at the top right; on 23 September the middle panel says «كل عام والوطن بخير» instead of the countdown. 20–24 February: the same with «يوم التأسيس ٢٩٩ عامًا» (year − 1727), the greeting on the 22nd. No flag anywhere. Other days, or `national off`: nothing changes |
| B10 | المباريات | Team 1 on the right, team 2 on the left, side by side, divider between. Slot hidden if no match within 7 days. Both hidden → «لا شيء هذا الأسبوع»; an upcoming match shows «بعد ٣ أيام» and under it, smaller, its date and time: «السبت ٢٤ أكتوبر · ٩:١٦ م» |
| B11 | Team names | Arabic for all clubs. A non-preset club may show a rough spelling for a few seconds, then the official Arabic name |
| B12 | No credit on screen | Photo credits are only in docs/PHOTO-CREDITS.md |
| B13 | Any remote key | Preview closes |
| B14 | Change city to جدة (A4/A5), reopen preview | Prayer times, weather and «بتوقيت جدة» change |

## 3. Time-based behaviour

Use the TV's clock or wait for the real moment; take a screenshot at each.

| ID | When | Expected |
|---|---|---|
| C1 | Within 25 min after any adhan (10 min after Maghrib) | Prayer panel glows gold: «حان الآن», prayer name, «الإقامة بعد … دقيقة» |
| C2 | Friday before Dhuhr | Prayer name «الجمعة»; no iqama countdown after it |
| C3 | Between 21:00 and 07:00 | Screen gradually dimmer; darkest 23:00–05:00 |
| C4 | During a live match of a chosen team | Score + «مباشر · الدقيقة …», updates within ~2 min of real goals |
| C5 | Within 24 h after that match | «النتيجة النهائية · <day>» with the final score |
| C6 | After midnight | New background photo; dates, prayers, shows refreshed |
| C7 | Leave it running 2+ hours | Text shifts a few pixels every 4 min (burn-in protection); no freeze; clock still correct |

## 4. Screensaver (dream)

| ID | Action | Expected |
|---|---|---|
| D1 | TV Settings → screensaver → choose «شاشة تلفزيونك», or `adb shell settings put secure screensaver_components sa.urtv.screensaver/.ScreenDream` | Selected |
| D2 | Start now: `adb shell am start -n "com.android.systemui/.Somnambulator"` (or wait for the idle timer) | The screen appears full screen |
| D3 | Press any remote key | Screensaver exits |
| D4 | Leave the TV idle for the timer set in TV settings | Starts by itself |

## 5. Offline

| ID | Action | Expected |
|---|---|---|
| E1 | Disconnect the TV from Wi-Fi/Ethernet, open preview | Clock, both dates, prayer countdown (computed on device), Ramadan panel all shown. Markets arrows hidden, no dash; weather shows only the city; no error text |
| E2 | Reconnect, wait 2 min, reopen | Live data returns |

## 6. Auto-update

| ID | Action | Expected |
|---|---|---|
| F1 | Press A17 twice in a row | First «تم التحديث», second «الصفحة محدثة» (the page is cached and unchanged) |
| F2 | Ask the developer to push a visible change (or note the latest commit), then A17 | Toast «تم التحديث»; preview shows the change |
| F3 | `adb shell dumpsys jobscheduler \| grep -A3 urtv` | A periodic, persisted job listed (it only starts the app; it does no network work) |
| F4 | Idle TV, network traffic watched (router or `adb shell dumpsys netstats`): leave it 7 hours without the screensaver starting | No request to GitHub from the app; the 6-hour job runs and ends at once |
| F5 | With the last check 6 hours old or more, start the screensaver (preview or the idle timer), watching the network; then start it again | One request to GitHub at the first start, none at the second; a start 6 hours after the first checks again |
| F6 | Turn A16 to متوقف, wait out 6 hours, start the screensaver | No page request; A17 still works |
| F7 | With a newer home build published (or a local `latest.json`), reopen the home screen | A gold «تحديث جاهز» pill appears by itself under the clock, at the right edge of the screen («تحديث N٪» while downloading); `files/update/home.apk` exists |
| F8 | Press «تحديث جاهز», then confirm Android's installer screen | Android asks to install, the app restarts on the new `versionCode`, the pill disappears; an APK signed with another key is refused before the installer opens |

## 7. Power cycle (TCL)

| ID | Action | Expected |
|---|---|---|
| G1 | Unplug the TV for 30 s, power on, **do not open the app**, wait for the idle timer | Screensaver starts. If not: note it, open the app once, retest D4 |
| G2 | `adb logcat -d \| grep -iE "dream\|urtv" > G1.log` after G1 | Attach the log |

## 8. Browser (no app)

| ID | Action | Expected |
|---|---|---|
| H1 | On a computer open `https://raw.githack.com/tshi-dev/UrTV/master/dist/urtv.html` or download `dist/urtv.html` and double-click | Same screen in the browser; `F` toggles full screen |
| H2 | Add `#city=جدة&lat=21.5433&lon=39.1728&teams=136013` to the URL | Jeddah times; only one match slot |
| H3 | Resize the window to 1280×720 | Layout holds, nothing overlaps |

## 9. Home-screen card (launcher)

| ID | Action | Expected |
|---|---|---|
| L1 | Fresh install of both apps, press Home | No card on the home screen (the screensaver's card choice is «لا شيء» by default) |
| L2 | Screensaver settings → «بطاقة على الشاشة الرئيسية» → مواقيت الصلاة, then Home | Card under the pills on the left: today's prayers, next one gold. D-pad never lands on it |
| L3 | Repeat L2 with each option, then «لا شيء» | Card follows the choice; «لا شيء» removes it |
| L4 | On the home screen, look at the top bar and the app rows, then press ▶ and ◀ on the pills and on the favourites | Everything reads from the right (المصادر، واي فاي، الإعدادات، شاشة التوقف، تلاوة; the first app of a row on the right); the right arrow always moves the highlight to the right and the left arrow to the left |
| L6 | Look at the clock in the top corner | The time on the right and the «ص» / «م» on its left (١٢:٣٣ ص) |
| L7 | Build the launcher with another key (a test build) and press Home | The card on the home screen falls back to «مواقيت اليوم» for the default city, because the screensaver's settings cannot be read |
| L8 | Long-press OK on any app → «ترتيب المفضلة…» (also: screensaver settings → «ترتيب التطبيقات المفضلة») | List of favourites, numbered, first one focused |
| L9 | OK on the 3rd app, ▲ twice, OK; then Back | It turns gold, moves to the top, drops; home row shows it first |
| L10 | «إزالة» beside an app (▶/◀ to reach it); then «+» on an app under «إضافة تطبيق» | Removed / added at the end; never below 5 or above 10 (a message says so) |
| L12 | `optimize/saver.sh tasks add 'اشتري خبز'`, then press Home and the «المهام» pill | The pill is there; the list shows «اشتري خبز» with a gold dot; OK on it → «تم: اشتري خبز», it turns to ✓ |
| L13 | In «المهام» press «تكلّم» and say «أضف مهمة موعد الطبيب» (a TV with a speech recognizer) | «أضيف «موعد الطبيب»؟» with نعم / لا; «لا» changes nothing, «نعم» adds it. Then «خلصت الطبيب» and «احذف الطبيب» each ask first |
| L14 | Same on a TV with no recognizer | No «تكلّم» button; «الإدخال الصوتي غير متاح في هذا التلفزيون» once; OK and «حذف» still work |
| L5 | `optimize/dock.sh apps`, then `optimize/dock.sh set <3 packages from that list>`, then `optimize/dock.sh show` | Home screen redraws at once with those three first, topped up to 5; `show` lists them |
| L6 | `optimize/dock.sh set com.not.installed` | `error: none of these apps are on this TV's home screen`; row unchanged |
| L7 | `optimize/dock.sh reset` | Row back to the defaults |
| L4 | Open any app with the card on, then `adb shell top -n 1 \| grep urtv.home` | Launcher idle while the app is in front (card paused) |
| L11 | An app with only a phone launcher activity (an OEM store or settings app, Aptoide): `adb shell cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.LAUNCHER` lists it, the LEANBACK_LAUNCHER query doesn't | It is in the grid once, opens with OK, and can be added to the favourites; TV apps are not listed twice; the stock home and its setup app are not in the grid |

## 10. Command-line tools (agents; run in the UrTV folder, after `optimize/run.sh`)

| ID | Action | Expected |
|---|---|---|
| T1 | `optimize/dock.sh hidden` | One `package \| reason` line per app the home screen leaves out, or «nothing left out» |
| T2 | `optimize/dock.sh hide <pkg of an app in the grid>`, look at the home screen, then `optimize/dock.sh show <pkg>` | `hidden: <pkg>` and the app leaves the grid; `shown: <pkg>` and it is back |
| T3 | `optimize/check-home.sh` | «OK: Home opens تلفزيونك»; if no app can be opened: a `!!` line and exit 1 |
| T4 | `optimize/share.sh --send` first (no report yet), then `optimize/share.sh --preview --agent "tester" "test note"`, then `optimize/share.sh --send` | The first stops with «Nothing to send»; `--preview` prints the report and «NOT sent» (nothing reaches the author); `--send` then gives «Sent to the author», or a QR code opens; `optimize/run/share.md` holds no IP address and no user folder (`grep` for the TV's IP and your user name prints nothing) |
| T5 | `optimize/saver.sh find-team الأهلي` | The Saudi club first, then «الأهلي المصري»; each line `id:Name \| league` |
| T6 | `optimize/saver.sh minrating 7`, then `optimize/saver.sh show` | `saved`; `show` lists `minrating: 7` |
| T7 | `optimize/selftest.sh` | `OK upload`, `OK Arabic`, then the screensaver and the app counts; no `FAILED` |
| T8 | `optimize/venv/bin/python -I optimize/random-clubs.py 8 1` | `seed: 1` and 8 names; the same on every run |
| T9 | `optimize/reset.sh`, then `optimize/venv/bin/python -I optimize/tv.py pm list packages sa.urtv` and `optimize/venv/bin/python -I optimize/tv.py settings get secure screensaver_enabled` | The package list is empty; `screensaver_enabled` is the value in `optimize/run-*/backup.txt` (under «== screensaver»: the components line, then this one; `null` means unset) |
| T10 | Put a `!!` line in `optimize/run/report.md` and run `optimize/run.sh` | It ends with «Done, with problems:» and that line, exit code 1; no «All steps done» |
| T11 | `optimize/saver.sh photos 15`, then `optimize/saver.sh show`; then `optimize/saver.sh photos 7` | `saved`, `show` lists `photos: 15`; the second prints `error: photos is one of 5 10 15 30 60` and changes nothing; put it back with `photos 30` |
| T12 | `optimize/saver.sh idle 10`, then `optimize/saver.sh show` and `optimize/venv/bin/python -I optimize/tv.py settings get system screen_off_timeout`; then `optimize/saver.sh idle 3` | `idle: 10` (set by the app, or «set from the shell» when the app may not), `show` lists `idle: 10`, the setting prints `600000`; `idle 3` prints `error: idle is one of 1 2 5 10 15 30`; put the TV's old value back afterwards |
| T13 | `optimize/saver.sh digits latin`, then `optimize/saver.sh show`; then `optimize/saver.sh digits arabic` | `saved`, `show` lists `digits: latin`; the second prints `error: digits is one of hindi latin` and changes nothing; put it back with `digits hindi` |
| T13b | `optimize/saver.sh national off`, then `optimize/saver.sh show`; then `optimize/saver.sh national always` | `saved`, `show` lists `national: off`; the second prints `error: national is one of auto off` and changes nothing; put it back with `national auto`. In the app, «المناسبات الوطنية» shows تلقائي / إيقاف |
| T14 | `optimize/saver.sh cardstyle glass1`, then `optimize/saver.sh show`; then `optimize/saver.sh cardstyle shiny` | `saved`, `show` lists `cardstyle: glass1`; the second prints `error: cardstyle is one of current dark1 glass1 dark3 glass3` and changes nothing; put it back with `cardstyle current` |
| T15 | `optimize/saver.sh markets tasi,gold,btc` and `optimize/saver.sh mkstyle prices`, then `optimize/saver.sh show`; then `optimize/saver.sh markets tasi,tasi` and `optimize/saver.sh markets foo` | `saved`, `show` lists `markets: tasi,gold,btc` and `mkstyle: prices`; the last two print `error:` and change nothing; put it back with `markets tasi,spx,btc` and `mkstyle arrows` |
| T16 | `optimize/saver.sh stocks 2222 1120 AAPL`, then `optimize/saver.sh show`; then `optimize/saver.sh stocks 2222 2222` and `optimize/saver.sh stocks 12345` | `saved`, `show` lists `stocks: 2222,1120,AAPL`; the last two print `error:` and change nothing |
| T17 | `optimize/saver.sh tasks add 'اختبار'`, `tasks list`, `tasks done 1`, `tasks del 1`, `tasks del 9`, `tasks clear` | `saved` and the numbered list; `1. [done] اختبار`; `no tasks`; then `error: no task number 9 …` |
| T18 | `optimize/saver.sh slot fasting`, then `optimize/saver.sh show`; then `optimize/saver.sh slot tennis` | `saved`, `show` lists `slot: fasting`; the second prints `error: slot is one of matches weather fasting countdown none` and changes nothing; put it back with `slot matches` |
| T19 | `optimize/saver.sh countdown 2026-12-01 'رحلة الصيف'`, then `optimize/saver.sh show`; then `countdown 2026-02-30`, then `countdown clear` | `show` lists `countdown: 2026-12-01 رحلة الصيف`; the second prints `error: countdown is a date like 2026-12-01, or clear` and changes nothing; `clear` gives `countdown: none`. With `slot countdown` the settings row «العدّ التنازلي» shows the date and name and offers «مسح» |

## 11. The «تلاوة» screen — the Quran, the radio and the adhan (inside the home app)

Install: `optimize/install.sh launcher` (or `all`). «تلاوة» is the leftmost pill in the home screen's
top bar (the row reads from the right: المصادر، واي فاي، الإعدادات، شاشة التوقف، تلاوة); it is not
in «كل التطبيقات». Full reference: [TILAWAH.md](TILAWAH.md).

| ID | Action | Expected |
|---|---|---|
| W1 | Press «تلاوة» in the top bar | The «تلاوة» screen opens with a tab bar read from the right — «القرّاء» (active, on the right), «الإذاعات», «الأذان» (on the left); the title is on the right; the first reciter is focused, not the top row |
| W2 | Compare the five times with the screensaver's prayer panel | Same times (within a minute); Isha = Maghrib + 90 min (120 in Ramadan) |
| W3 | Left/Right from the list, then Up from the first row, then Down | The section changes and its list starts at the top; the right arrow always moves the highlight to the right and the left arrow to the left; Up reaches the tab bar (solid gold on the active tab), Down returns to the first row; the selected row always has a solid dark card, thick gold frame and gold chevron |
| W4 | OK on a reciter, then OK on الفاتحة | 114-surah grid in reading order; the player shows the surah and reciter; audio plays; Back returns |
| W5 | Play, press Home, wait for the screensaver | Audio continues; the media play/pause key controls it; «تلاوة» shows «متابعة التلاوة» again |
| W6 | OK on «إذاعة القرآن الكريم - السعودية» | Live player, long title fits, audio plays, no error (this also proves the scoped radiojar cleartext redirect) |
| W7 | OK on a prayer row (e.g. المغرب) | That adhan plays now through the player screen («الأذان» + the muezzin's name); the previous/next buttons are gone and Left/Right do nothing; Back leaves it playing |
| W8 | The «الأذان» tab, untouched after a fresh install | «الأذان التلقائي» reads «متوقف» (off by default); OK switches it to «مفعّل» and back, and the state survives a reboot |
| W9 | OK on «المؤذن» | 21 recordings by name (الحرم المكي، المسجد الأقصى، عبدالملك الملا…), the chosen one in gold with its city; OK picks one and plays 12 seconds of it; the list scrolls and the selected row keeps its gold frame at the bottom |
| W10 | Play a surah, then `optimize/music.sh adhan now 3` | The surah pauses, the adhan plays, then the surah resumes at the same position (status: `source=adhan` during it, `adhan_last` updated) |
| W11 | `optimize/music.sh adhan voice 7` then `adhan now` | Voice 7 plays; `optimize/music.sh status` shows `voice=7` |
| W12 | Set the TV clock a few minutes before a prayer time, wait for the alarm; then reboot the TV and check `optimize/music.sh status` | The adhan plays by itself at the exact minute with no app on screen; after the reboot `adhan_next` is a future timestamp (the alarm was put back) |
| W13 | Note the TV's volume, play a prayer adhan manually, then end it (OK on the player) | The adhan plays clearly at 30% whatever the TV was on; the TV's volume is back to what it was afterwards (check with the TV's volume bar) |
| W14 | OK on «صوت الأذان» a few times, then `optimize/music.sh adhan now` | The value cycles ٣٠٪ → ٤٠٪ … → ١٠٠٪ → ١٠٪ and the adhan plays at the chosen level; put it back to ٣٠٪ afterwards |
| W15 | Put the TV in standby (power key) a minute before a prayer time, leave it off through the prayer, then wake it | Nothing plays at the prayer time and nothing plays when the TV wakes: no catch-up of a missed adhan |
| W16 | Start an adhan manually, then press the power key (screen off) | The adhan stops (a paused surah resumes only if it was playing before); nothing keeps playing to a dark screen |
| W17 | `optimize/music.sh status`, `surah sudais 1`, `pause`, `radio 0`, `sleep 1`, `stop` | Status line has `playing`, `source`, names, `pos`, `dur`, `sleep`, `adhan`, `voice`, `adhan_next`, `error`; each command acts on the TV |

---

## Report format

```
ID | PASS/FAIL | note | screenshot file
A7 | FAIL | "الدوري الهولندي" shows لا توجد نتائج | A7.png
```

Send back the table plus every FAIL screenshot/log.
