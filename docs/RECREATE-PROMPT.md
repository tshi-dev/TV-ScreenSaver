# شاشة تلفزيونك (UrTV screensaver) — recreate prompt

Paste everything below the line into any AI that can write a single HTML file. It will rebuild the screen from scratch.

---

## Goal

Build ONE self-contained HTML file (no build step, no external JS libraries) that runs full-screen on a 16:9 TV at 1920×1080 as an always-on screensaver. Arabic only, right-to-left, Riyadh. It must look like a premium Saudi editorial broadcast graphic — photographic and calm, never like a generic dark "dashboard".

## Layout (1920×1080)

One grid with three rows — `grid-template-rows: auto 1fr auto`, stage padding `3.1vh 3.6vw 5.4vh`, row gap `1.5vh`.

**Top row — two ends, nothing in between:**
- Left: weather and markets set as type, mirroring the date block on the right — no boxes, no pills.
  - Big line (same size as the Hijri date): the temperature `٣٤°` in the display font, then a short Arabic condition word (`صافي`, `غائم جزئياً`…; `غبار` in gold when PM10 is high).
  - Small line (same size as the Gregorian date): the city `·` high / low `٣٧° / ٢٤°`.
  - A short gold hairline starting at the left screen edge and fading toward the middle.
  - Under it, one quiet line: `تاسي ▼ · S&P 500 ▲ · بيتكوين ▲` — the name plus ONE direction marker only, `▲` `#79D6A0`, `▼` `#EE8B70`. No price, no percentage. If a quote cannot be loaded, hide its marker (never a dash, never a hard-coded direction).
- Right: Hijri date (large, sand `#F6EFE0`) over the Gregorian date (smaller, muted `#C6BBA4`), then a short gold hairline starting at the right screen edge and fading toward the middle.

**Middle row — card left, clock centred in the space between the card and the right edge:**
- Clock `font-size: clamp(120px, min(17vw, 31vh), 345px)` (≈300px tall), `line-height .92`. 12-hour always, Arabic-Indic digits, gold colon, and a small gold `ص`/`م` at `.28em` at the upper left of the time. Under it a tiny muted line `بتوقيت الرياض`.
  - Browsers ignore `letter-spacing` on Arabic script, digits included, and the display font's `٤` reaches into the next digit: put every digit in its own inline box with a `.09em` margin between boxes.
- The card on the left: translucent panel (`rgba(4,14,9,.46)`, 1px gold border, radius 12, backdrop blur), width `min(760px, 48vw)`. Its content is a setting (`#panel=`): new episodes (below, the default), today's prayer times (next one gold, past ones dim), a 5-day forecast, every followed team's games this week, Saudi and Medina news refreshed every five minutes, a verse one day and a hadith the next, or no card.
  - Header `هذا الأسبوع` with a gold rule fading out to the left.
  - Exactly 5 rows, each a single flex line: show name (right, `flex:0 1 auto; max-width:68%`, ellipsis), then `الموسم ٣ · ح ٧`, then the rating `٨٫١`, then the weekday anchored at the far left (`flex:1 1 auto; text-align:left`). Hairline under every row.
  - Pool of up to 50 titles, 5 per page, a new page every 25 seconds with a staggered fade-up (`opacity` + `translateY(8px)`, 60ms per row) and wraparound at the end.

**Bottom row — three panels under one gold hairline, `grid-template-columns: 1fr .9fr 1.3fr`, `align-items:start` (the three labels on one line):**
All three are built identically — small gold label, big value, small sub-line — and must read as one set: same sizes, same baseline.
- Right: `الصلاة القادمة` → `الفجر ٥:٣٥ ص` as one big value, sub-line `بعد ٣ ساعات و ٣٧ دقيقة`; between Fajr's iqama window and sunrise show the sunrise countdown, and for 30 minutes after sunrise show its time and elapsed duration before returning to the Dhuhr countdown.
- Centre: `رمضان ١٤٤٨` → the day count at `1.3em` with `يوم` at `.4em` in gold beside it, sub-line the estimated start date.
  - This is the classic bug: keep number and unit in one inline line and give the unit `display:inline-block; transform:translateY(-.8em)` so it lands beside the number instead of dropping underneath and colliding with the sub-line.
- Left: `المباريات` → `الهلال × الحزم` big, sub-line `بعد ٢٠ ساعة · ٩:٠٠ م`. Nothing inside the coming week → `لا شيء هذا الأسبوع` in muted sand at the same size, never an empty box.

## Visual direction

- Background: a real dusk desert photograph (sand dunes, sun low, empty sky, deep shadows), not CSS gradients. Darken with a vertical scrim top and bottom, a soft vignette, and film grain at 4–5% opacity. 120-second ultra-slow scale-and-drift on the photo.
- Palette: sand `#F6EFE0`, warm gold `#C8A45C` / `#D6AF6B`, near-black green `#04120C`, muted text `#C6BBA4`.
- Heavy soft drop shadows behind the clock and big values so they stay legible over the photo.
- No English labels on screen, no emoji, no box around the clock.

## Typography

- Use **Thmanyah** (free, https://font.thmanyah.com). Download the ZIP and embed the weights as base64 `@font-face` so the file works offline: **Sans Light + Regular + Medium** for labels and body, **Serif Display Bold** for the clock, the big values and the empty-match state.
- If Thmanyah is unavailable: body in a low-contrast humanist Arabic sans, clock and numbers in an Arabic display serif with tall, high-contrast digits.
- Arabic-Indic numerals everywhere (`٦٧٧٢٧٣٦٤٧٢`), including the Gregorian date, temperatures, ratings and countdowns. Decimal separator `٫` (U+066B).
- Arabic counted-noun grammar: `١ ساعة`, `ساعتين`, `٣ ساعات` … `١١ ساعة`; same pattern for `يوم` and `دقيقة`.
- All sizes in `clamp()` with `vw`/`vh` — it must still hold together at 1280×720.

## Data (all free, no API keys)

- **Prayer times** — Aladhan, method 4 (Umm al-Qura):
  `https://api.aladhan.com/v1/timings/DD-MM-YYYY?latitude=24.7136&longitude=46.6753&method=4&timezonestring=Asia/Riyadh`
  Only Fajr, Dhuhr, Asr, Maghrib, Isha. Refresh every 6 hours; recompute the countdown every second.
- **Ramadan** — never hard-code the date: walk forward day by day with
  `Intl.DateTimeFormat('en-u-ca-islamic-umalqura',{timeZone:'Asia/Riyadh',month:'numeric'})` until the month equals 9.
  Cache the answer per calendar day — that 400-day search must not run once per second.
  The centre panel follows the next occasion the same way (OCCASIONS in the page: Ramadan, عيد الفطر, عشر ذي الحجة,
  يوم عرفة, عيد الأضحى, رأس السنة الهجرية, عاشوراء): days left and the expected date («متوقّع»), «اليوم …» or
  «عيد مبارك» on its days.
- **Hijri date** — `Intl.DateTimeFormat('ar-SA-u-ca-islamic-umalqura',{timeZone:'Asia/Riyadh',weekday:'long',day:'numeric',month:'long',year:'numeric'})`, strip a trailing `هـ` before appending your own. The Ramadan panel's year must also be derived, never a literal `١٤٤٨`.
- **Markets** — Yahoo chart API:
  `https://query1.finance.yahoo.com/v8/finance/chart/{^TASI.SR|^GSPC}?interval=1d&range=5d`
  Compare `regularMarketPrice` with the previous close; only the sign matters. Refresh every 15 minutes.
  Yahoo sends no CORS headers, so a browser cannot call it directly: try it once, and on failure fetch the same URL through `https://api.allorigins.win/get?url=<encoded>` and `JSON.parse(response.contents)`. If both fail, hide the arrow. Allow up to ~30 seconds on first load.
  Bitcoin uses CoinGecko instead (it does send CORS headers):
  `https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd&include_24hr_change=true`
  Mind the spelling: the query parameter is `include_24hr_change` but the response key is `usd_24h_change` (no `r`) — accept both.
- **Weather** — Open-Meteo:
  `https://api.open-meteo.com/v1/forecast?latitude=24.7136&longitude=46.6753&current=temperature_2m,weather_code&timezone=Asia/Riyadh`
  Map the WMO `weather_code` to a short Arabic phrase. Refresh every 30 minutes.
- **Premieres** — TVMaze schedules:
  `https://api.tvmaze.com/schedule?country=US&date=YYYY-MM-DD` and `https://api.tvmaze.com/schedule/web?date=YYYY-MM-DD`, for each of the next 7 days.
  Keep `rating.average >= 7`, drop soaps (`genres` includes `Soap`), drop seasons above 15 and any entry without an episode number, dedupe by show name, cap the pool at 50, premieres first then by date.
- **Fixtures** — TheSportsDB test key `123`:
  `eventsnext.php?id=136013` (Al-Hilal) and `?id=136137` (Saudi national team); `eventslast.php?id=…` for results.
  `strTimestamp` has no offset — append `Z`, then render in `Asia/Riyadh`.

## Behaviour

- **Match result window:** once a game ends (status `FT`/`AET`/`PEN`, or kickoff + 2.5h), show the final score (`الهلال ٣ - ٢ الاتحاد`, sub-line `النتيجة النهائية · الأحد`) for 24 hours, then the next fixture. Keep (and save) the score seen at the whistle: the free API's "last games" list lags and "next game" moves on at once.
- **Verse and hadith:** never typed by hand. Verse texts come from an authoritative Quran API; each hadith must be found word for word in the six Sunni books, with those books as its source line. Leave out pause marks your font has no glyph for; take ﷺ and ﴿﴾ from a font that has them.
- **Ramadan mode:** when the Hijri month is 9 the centre panel stops counting down and becomes `الإفطار` with the Maghrib time and the time left; between Maghrib and Fajr it becomes `الإمساك` with the Fajr time.
- **Night dimming:** a full-screen black overlay, transparent from 07:00 to 21:00, ramping over two hours either side, sitting at 46% between 23:00 and 05:00. Recompute every minute.
- **Burn-in protection:** shift the content layer a few pixels through a fixed 7-step sequence every 4 minutes, with a 3-second transition.
- **Background:** a hosted set of licensed Saudi landscapes (1920×1080); every 4 days a seeded draw picks 5, rotating every 30 minutes, with a few bundled photos as the offline fallback. Give each its own brightness factor (0.70–1.0) so bright sand or water never hurts the white text. No photo credit on screen; credit the photographers in the repo instead.
- **Start:** keep the page hidden until the clock, the last saved panels and the photo are ready, then fade in once — never a half-built frame.
- **Arabic descenders:** any line clipped with `overflow:hidden` + ellipsis needs room under the baseline (`padding-bottom:.3em; margin-bottom:-.3em`), or the dots under ي and ج are cut off.
- **Midnight rollover:** at Riyadh 00:00 reload the day-dependent data (prayers, week list, matches, weather). A 6-hour interval alone leaves yesterday's premieres on screen.
- Clock ticks every second (one Intl call); dates, prayer, Ramadan and countdowns refresh every 15 seconds. Zero layout shift. Hide the cursor, block scrolling and selection. `F` toggles fullscreen.

## Hard requirements

- Every fetch in `try/catch` with a fallback. With no network the screen still shows the correct clock, both dates, the Ramadan countdown, a prayer countdown and the known fixture — never a blank cell, never an uncaught error.
- Never fabricate data. If a live value is missing, hide it — never present a hard-coded number as if it were current.
- On a failed refresh keep the last known data (week pool, fixtures, results) instead of blanking the panel.
- Numbers read left-to-right: build the clock from separate spans inside a `direction:ltr` container, or Arabic-Indic digits render in reverse.
- Every API-sourced name (teams, show titles) needs `nowrap` + ellipsis and a `min-width:0` parent, so a long title can never push its neighbour or the panel edge.
- Every `Intl.DateTimeFormat` that puts a weekday or date on screen must pass `timeZone:'Asia/Riyadh'`.
- Do countdown maths from `Intl.DateTimeFormat(...).formatToParts()` (hour × 60 + minute + second/60) instead of rebuilding a `Date` from wall-clock parts — the device timezone must not matter.
- A `CONFIG` object at the top of the script: city, latitude, longitude, timezone, prayer method, team ids, keys.
- Final deliverable: one `.html` with fonts and image inlined as base64, openable by double-click on a TV browser with no server.

## The Android app (what we actually shipped)

- A `DreamService` screensaver (`sa.urtv.screensaver/.ScreenDream`) started by the TV's own idle timer, plus a full-screen preview activity; any remote key closes it.
- The WebView loads a light build of the page (`dist/tv.html`, 0.3 MB); the app answers its font and photo requests from the APK and a photo cache on disk (`shouldInterceptRequest`), so nothing waits on the network.
- Settings: 194 Saudi cities and towns in the 13 regions plus an online place search; up to 20 teams (browse leagues or search TheSportsDB; Arabic names from a table, then Wikidata, then transliteration); the card beside the clock; episode filters. They reach the page as a URL fragment (`#city=..&lat=..&lon=..&teams=id,id&panel=..`) — a `?query` on `file://` fails in WebView.
- Agents set everything over ADB through shell-only broadcast receivers (`optimize/saver.sh`, `optimize/dock.sh`).
- Auto-update: when the screensaver starts, and at most every 6 hours, the app downloads the page from the repo (background thread, one at a time) and uses it if it is newer than the APK.
- TCL quirk: after a power cut the app may stay frozen until opened once; a boot receiver plus a persisted job (which only starts the process, no network) bring it back on most sets.
- Built by Gradle in GitHub Actions on every push and published to the `apk-latest` release.
