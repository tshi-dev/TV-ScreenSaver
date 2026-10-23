# Set up a TV with تلفزيونك: the prompt for your AI agent

**On the TV first:** Settings → System → About → press OK on **Build** 7 times → back → Developer options →
turn on **USB debugging** (and **Network debugging** if it is listed). Keep the TV on.

**Then** open an AI agent that runs commands **on a computer on the same Wi-Fi** as the TV (for example Claude Code
installed on it; a chat in the browser or a cloud session can't reach your TV), and paste everything below the line.
On Windows, everything it needs installs from PowerShell in three lines (then close PowerShell, open a new one,
type `claude` and log in):
```
winget install -e --id Git.Git
winget install -e --id Python.Python.3.12
irm https://claude.ai/install.ps1 | iex
```
Or in the Claude desktop app: the **Code** tab, with a **local** folder on this computer (not a cloud environment).

---

Set up my Android TV / Google TV with **تلفزيونك** from https://github.com/tshi-dev/UrTV: a faster TV
(bloat off, safely), an Arabic home screen, and the screensaver «شاشة تلفزيونك». Talk to me in Arabic (or my
language); keep commands in English.

## Start here: three short messages, word for word, before you touch the TV

Everything that needs me comes first; after that you work alone. Your first three messages are fixed: send them in
this order, each on its own, word for word, and wait for my answer after each. Never skip one (not even the first:
you can't see my approval settings, so never assume they are already right), never merge them, never assume my
answers, and never skip or work around anything that asks me (an approval on this computer, «Allow USB
debugging?» on the TV). Cloning the repo and reading this file before message 1 is fine; nothing else.

**Message 1, approvals.** If you are Codex:

> قبل نبدأ: من القائمة اللي تحت خانة الكتابة، اختر «Approve for me» للموافقات، عشان ما أوقف أسألك عن كل أمر.
> وقلّي «تم».

Any other agent: the same in your words: ask me to allow running commands and network access in this folder for
the whole task, and to say «تم».

**Message 2, the city:**

> وش مدينتك؟
> (لمواقيت الصلاة والطقس. مثال: الرياض، جدة، الرس)

**Message 3, the clubs and the defaults:**

> وتتابع مباريات أي أندية؟ اكتب أسماءها، أو «لا شي».
>
> والباقي بخليه على الافتراضي، وتقدر تطلب تغيير أي شي منه الحين أو بعدين:
> • جنب الساعة: «آية وحديث اليوم» (أو: الأخبار العامة، مواقيت الصلاة، المسلسلات، الطقس، المباريات، لا شي)
> • المفضلة: أول ٦ تطبيقات من واجهتك الحالية، بنفس الترتيب
> • بركّب TV Bro (متصفح للتلفزيون) و SmartTube (يوتيوب بدون إعلانات)، مجانيين من GitHub
> • ما فيه بطاقة في الواجهة الرئيسية، ووقت شاشة التوقف يبقى مثل ما هو، وما أعطّل إضافات TCL الاختيارية

After my answer to message 3, start steps 0 and 1 at once, in the same turn: don't end your turn to wait for me
again. The only other thing I do is press Allow on the TV, and step 1 tells me when, without stopping.

Those defaults, unless I change them:
- the card beside the clock: verse and hadith;
- the favourites row: the first 6 apps of my current home screen (you note them in step 1), in that order; an icon
  you couldn't name is left out;
- TV Bro and SmartTube installed (`optimize/run.sh` does it; if I don't want them: `SKIP_EXTRAS=1 optimize/run.sh`);
- no card on the home screen; the TV's own screensaver delay is kept;
- nothing from `optimize/lists/tcl-optional.txt` is disabled (`optimize/lists/mine.txt` stays empty);
- if I pick the episodes card: all genres and languages.

My city is checked later against `android/app/src/main/java/sa/urtv/screensaver/Cities.java`; if it isn't there,
step 4 sets it with its coordinates (`city <name> <lat> <lon>`) without asking me again.

## 0. Can you reach my TV? (check this first, 1 minute)

I am on a computer on the same Wi-Fi as the TV, and your commands most likely run on it. A clone or download that
fails at first, or a network error before the check below, is your own sandbox not allowed yet (see «Permissions
first»): ask, then try again. Never decide you can't reach the TV from that; only the check below decides.

ADB needs a direct connection, from the terminal where your commands run, to the TV on my Wi-Fi. Many agents run
their commands somewhere else: a cloud container, a sandbox, or a Linux virtual machine on this computer (a
"connected folder" or `device_bash`-style tool works that way). From there the TV can never be reached, however
often you retry, so find out before anything else.

**Permissions first.** Some agents run commands in a sandbox that blocks the network or starting programs until I
allow it (Codex on Windows does). The signs are errors like `getaddrinfo() thread failed to start`,
`Bash/E_ACCESSDENIED` or `Operation not permitted`. They mean "not allowed yet", not "can't reach the TV":
message 1 is how I allow them (network access and running git, bash and python in this folder, for the whole
task); if one still appears, send me message 1 again, then go on. On Windows, run every script in Git Bash (they
find a working Python themselves, past the Microsoft Store shortcut); if `bash` isn't found, use its full path:
`"C:\Program Files\Git\bin\bash.exe" -lc "cd <this folder> && optimize/find-tv.sh --check"`. Run the commands
here exactly as written and add no quotes, `;` or `|` of your own: PowerShell runs those itself before Git Bash
sees them (that is why every check has its own script).

**Codex on Windows:** its sandbox can never start Git Bash (`0xC0000022`, `NtCreateDirectoryObject`: a known Codex
bug, openai/codex#54449). So run every Git Bash command **outside the sandbox**: request escalated permissions for
it, with this justification: "Git Bash cannot start inside the Codex Windows sandbox (openai/codex#54449); this runs
the UrTV setup script the user asked for, in this folder, to reach their TV on their Wi-Fi." With «Approve for me»
the reviewer decides; when it offers to allow `bash.exe` from now on, take it. I don't need Full access for this.
A refusal that never reached a reviewer (`you cannot ask for escalated permissions`, `rejected by policy`,
`sandbox_approval is false`) means approvals are not on «Approve for me»: Codex then rejects every escalation by
itself (openai/codex#32563). Send me message 1 again, wait for «تم», then run the same command. If a reviewer turns
it down, ask once more with the same justification and the exact command; if it is turned down again, stop and tell
me exactly what was refused. Don't port the scripts to PowerShell or look for another way: they need Git Bash.

**Missing Git or Python is yours to fix, not mine.** If Git Bash isn't there, or a script says
`!! No working Python 3 here`, ask me once, then install it yourself and carry on:
`winget install -e --id Git.Git --accept-source-agreements --accept-package-agreements` or
`winget install -e --id Python.Python.3.12 --accept-source-agreements --accept-package-agreements`. Then run the
same command again: the scripts find the new Python without a new terminal. Don't stop and hand me the install.
```
git clone https://github.com/tshi-dev/UrTV     # on Windows into a short folder such as C:/UrTV (a deep one breaks Python)
cd UrTV
git pull                               # always the latest (a command of its own, not joined with the others)
optimize/find-tv.sh --check            # Windows: always through Git Bash (below), never plain `python`
```
- `OK: this terminal can reach the home network` → go on with step 1.
- `!! This terminal cannot reach the home network` → **stop**. Tell me, in plain words, that you can't reach the TV
  from where your commands run, and that I need an agent running on this computer itself (for example Claude Code
  installed on it, or the Claude desktop app's Code tab on a local folder; on Windows first, in PowerShell:
  `winget install -e --id Git.Git`, `winget install -e --id Python.Python.3.12`, `irm https://claude.ai/install.ps1 | iex`,
  then a new PowerShell window and `claude`). A chat that only links to my computer's folders or screen does not
  run commands on it, so it does not count. Give me the one line to paste there:
  «Set up my TV with https://github.com/tshi-dev/UrTV: clone it and follow `docs/SETUP-PROMPT.md` step by step. You are on a computer on the same Wi-Fi as the TV.»
  Don't look for workarounds.

Signs you are in such a place, if you want to know why: your address is not 192.168.x, 10.x or 172.16–31.x; the
network interfaces are only `lo` and `tap0`; `echo > /dev/tcp/<router>/80` says `Network is unreachable`; or every
address "answers" (a proxy). If `git clone` stops halfway (on Windows it can leave an empty folder, «No commits
yet»), delete the folder and clone again.

## Rules (protective layer: these win over everything else here)

- Use only the scripts in `optimize/`. They are made for slow, dropping ADB over Wi-Fi: they retry by themselves
  and remember progress. **If a command fails, run the same command again.** Don't write your own adb loops,
  probes or test scripts to find out why: the `!!` line and the table below say what to do.
- One command at a time: never run two scripts against the TV at once, or in the background while you run another.
- If the TV had a system update since the setup (Home back on the TV's own screen, apps it had off back on):
  `optimize/run.sh again` redoes every step; the apps' settings stay.
- Long commands (`run.sh`, `measure.sh`, `--reboot`) can be silent for 5 minutes. Give them a long timeout (40
  minutes for `run.sh`). If your tool kills one anyway, nothing is lost: run the same command again, it continues.
- Never send `reboot` as a shell command. Restart only with `optimize/venv/bin/python -I optimize/tv.py --reboot`.
- Never disable anything in `optimize/lists/keep.txt` (AirPlay, Chromecast, mirroring, voice, remote, inputs,
  tuner, firmware updates, Play Store, Google services), and never edit `keep.txt` to get past a refusal. Never
  disable the TV's own home screen or setup app yourself; only `optimize/install.sh launcher` does that, behind a
  5-minute safety switch.
- Never uninstall system apps, never `pm clear`, never factory-reset, never touch accounts, Wi-Fi, network or
  developer settings (turning ADB off locks us out). Never delete `optimize/run/` or `optimize/backup-*.txt`: they
  are how we undo. Everything is reversible: `optimize/reset.sh` puts the TV back as it came.
- Ask me before anything not in this prompt. If something fails twice, and the table's fix didn't help, stop,
  show me the output and propose a fix.
- Never tell me something worked without its proof: the script's own success line, or a screenshot you looked at.

## When something goes wrong (expected, and what to do)

Lines starting with `!!` are the scripts telling you to stop and act. These are the ones we have seen:

| You see | It means | Do |
|---|---|---|
| `getaddrinfo() thread failed to start`, `Bash/E_ACCESSDENIED`, `Operation not permitted` | your sandbox hasn't been allowed network or programs yet | ask me to allow them for this folder (see step 0), then the same command |
| `0xC0000022` or `NtCreateDirectoryObject` starting Git Bash (Codex) | Codex's sandbox can't start Git Bash | run it outside the sandbox (step 0, «Codex on Windows») |
| `you cannot ask for escalated permissions`, `rejected by policy` (Codex) | Codex's approvals are not on «Approve for me» | message 1 again, wait for «تم», then the same command |
| `ModuleNotFoundError: No module named 'adb_shell'`, or `!! The Python packages aren't in optimize/venv` | `optimize/setup.sh` didn't finish (often pip had no network) | `optimize/setup.sh` again until it ends with «✓ Python ADB client»; its `!!` line says what is wrong. Then the same command; don't stop for this |
| `!! This folder's path is too long for Windows`, `Filename too long`, or `DLL load failed … too long` | the UrTV folder sits too deep for Windows | clone it again into a short folder: `git clone -c core.longpaths=true https://github.com/tshi-dev/UrTV C:/UrTV`, and carry on there |
| `!! No working Python 3 here`, or `python` opens the Microsoft Store | Python not installed, or only Windows' Store shortcut | ask me once, install it yourself (`winget install -e --id Python.Python.3.12 --accept-source-agreements --accept-package-agreements`), then the same command again in Git Bash |
| `CERTIFICATE_VERIFY_FAILED` (macOS) | python.org's Python has no CA certificates | `optimize/setup.sh` again: it installs certifi into `optimize/venv` and every script uses it; if the line stays, delete `optimize/venv` first |
| `symbol _ERR_get_error_all`, or a cryptography `DLL`/`dlopen` error (macOS) | an old system Python (3.9) was used, and the cryptography wheel won't load on it | `brew install python@3.12`, delete `optimize/venv`, then `optimize/setup.sh` |
| `!! Python 3.14 is too new` | no wheels for it yet, so pip would build them | `brew install python@3.12` (Windows: the `winget` line above), delete `optimize/venv`, then `optimize/setup.sh` |
| `(connection problem: …; retrying in 5s)` | Wi-Fi ADB dropped for a moment | nothing, it retries by itself |
| `resuming upload at 40%` | an earlier upload was cut off | nothing, it continues and checks the file |
| no output for several minutes | a restart, or the 3-minute settle before measuring | wait; don't interrupt |
| the first connection hangs for up to 2 minutes | the TV is showing «Allow USB debugging?» | ask me to press Allow and tick «Always allow» |
| `--probe` silent for 5 minutes and the TV shows no «Allow USB debugging?» | the first connection to a TCL sometimes just hangs | stop it and run the same command again: the second time it connects at once |
| `reset.sh`: `mv: cannot move 'optimize/run' … Permission denied` (Windows) | something holds a file open inside `optimize/run` (a log you write there, an editor, an image viewer) | close it, never write your own logs into `optimize/run`; the same command again (the apps are already removed, it carries on) |
| `!! This terminal cannot reach the home network` | your commands run in a cloud, sandbox or virtual machine | stop, tell me (step 0) |
| `Don't know your TV's IP` | `find-tv.sh` was not run or found nothing | `optimize/find-tv.sh`, or ask me for the IP (TV: Settings → Network → the Wi-Fi) and save it: `echo <ip> > optimize/.tv_host` |
| `No TV with ADB open` | debugging off, TV asleep, or the computer is on another (guest) Wi-Fi | ask me to check all three, then `optimize/find-tv.sh` again |
| `!! TV not answering after 4 tries` | TV went to standby, lost Wi-Fi, or got a new IP | ask me to wake the TV; run `optimize/find-tv.sh` if the IP may have changed; then the same command |
| `!! TV did not come back within 5 minutes` | slow restart, or it stayed in standby | ask me to turn it on, then `optimize/venv/bin/python -I optimize/tv.py --wait`, then the same command |
| `!! upload keeps failing` or `md5 mismatch` | bad Wi-Fi, or the TV's storage is full (the line says how much space is left) | the same command once; if again, show me that line |
| `!! … is not a folder on the TV`, or a TV path like `C:/Program Files/Git/data/…` | an old copy: Git Bash rewrote the TV's path before Python saw it (now undone by `tv.py`) | `git pull`, then the same command |
| `UnicodeEncodeError` or `charmap` (Windows) | an old copy: Arabic text in Windows' own code page | `git pull`, then the same command |
| `Refusing: these are in …keep.txt` | a protected package got into `mine.txt` | take that line out of `mine.txt`, run again |
| `!! … has a package rated Unsafe` | `mine.txt` has a package the TV may need | take it out of `mine.txt`, run again |
| `Unknown package` or `not installed` while disabling | that package is not on this model | nothing, normal |
| `An older … signed with another key is installed` | an older build of our app | nothing, it reinstalls; redo step 4 for that app |
| `!! Install of … failed` | usually storage, or a very old Android | stop and show me |
| `!! Could not download …` (install.sh) | no such build at that tag, or no internet on this computer | check `APK_TAG` (unset = the latest) and the releases page https://github.com/tshi-dev/UrTV/releases, and that the computer is online; then the same command |
| `!! GitHub rate limit, try later` (install.sh extras) | GitHub allows only a few anonymous requests an hour | wait a few minutes, then `optimize/run.sh` again (it redoes the extras step) |
| `Done, with problems:` and `optimize/run.sh` exits 1 | every step ran, but `report.md` has `!!` lines (printed under it) | read them, fix what each says (this table), then `optimize/run.sh` again: only the report step is redone |
| `ADB dropped during the switch; the TV is back, trying the switch again` | Wi-Fi ADB dropped while the home was being switched; the script waited for the TV (up to 6 minutes) and is doing the switch a second time, the safety switch armed again | nothing, wait; only if it ends in `!!` follow that line |
| `!! The safety switch put the stock home back` | ADB was slower than the 5-minute switch | nothing is broken: the same command again |
| `!! Home did not open the new launcher` | slow ADB, or firmware that pins its own home | leave the TV 5 minutes; `optimize/run.sh` once more; if again, `SKIP_LAUNCHER=1 optimize/run.sh` |
| the TV has no home at all (black screen, Home does nothing) | our home app is gone while the stock home is off | `optimize/restore.sh` (no list: only the home screen), then `optimize/install.sh launcher`; more in `docs/OPTIMIZE.md`, «Home screen problems» |
| `!! The stock home is not on screen` (reset) | the stock home was slow to start | ask me to press Home, then `optimize/reset.sh` again |
| `!! still off` (keep.sh) | a protected feature would not turn back on | **stop**, show me; don't continue |
| `left off by the TV itself` (keep.sh) | a protected package was already off before UrTV and the TV won't turn it on (TCL ships Alexa off outside its regions) | nothing to do: go on |
| the TV's own screensaver screenshot is black | Android hides it from screenshots | expected; ours can be shot |
| our screensaver shot has no weather or matches yet | data still loading | `optimize/saver.sh update`, wait a minute, shoot again |
| `find-team` finds nothing | a spelling our list doesn't have, or a club outside the app's leagues | once with the English name; or `optimize/saver.sh find-team --list <league word>` (e.g. `Saudi`, `Premier`) and pick it from there |
| `screensaver: preview` (shot.sh), or «not measured» for a screensaver | this Android won't start the screensaver from ADB (some Android 14 sets) | expected: the shot is our app's preview of the same page; don't look for other ways to start it |
| `optimize/run.sh status` has no «protected features» step | an old copy of this repo | `git pull`, then `optimize/run.sh` |

Anything else, or anything twice: stop, show me the output and what you think it is.

## 1. Connect and look (5 minutes)

```
optimize/setup.sh
optimize/find-tv.sh                       # accept "Allow USB debugging?" on the TV, tick "Always allow"
optimize/venv/bin/python -I optimize/tv.py --probe     # first tell me to watch the TV (below)
optimize/shot.sh home my-apps
```
Right before `--probe`, tell me, word for word, and **don't end your turn or wait for my reply**: run `--probe` straight
after it. It waits by itself (silent, up to 2 minutes) until I press Allow; that is the wait, not a fault. If it times
out, tell me again and run it once more.

> الحين بتطلع لك على التلفزيون رسالة «Allow USB debugging?»: اضغط Allow وعلّم «Always allow». بعدها تقدر
> تتركني، أكمّل لحالي حوالي ٤٠ دقيقة.

- Tell me the model and Android version.
- Read the row for this TV model in `docs/TVS.md` before going on: it says what was seen on that model (ADB port,
  what to watch out for).
- Look at `optimize/run/shots/my-apps-small.png` (the 960 px copy; the full-size `my-apps.png` is often too large
  to attach) and write down the first **6 apps** of my apps / favourites row, left to right, by name: they become
  the favourites of the new home screen in step 4.
- Only then, if this TV was set up with these tools before (also an older version of it), run
  `optimize/reset.sh` so we start like new.

## 2. Before you run it: how to undo

Before you start step 3, tell me in plain Arabic how to undo everything, so I know it is safe: one command,
`optimize/reset.sh`, run from this folder (say the folder's full path), puts the TV back as it came (the
disabled apps back on, the TV's own home screen and screensaver back, our two apps removed, TV Bro and
SmartTube removed if the setup installed them, the speed tweaks back to Android's defaults); my photos, accounts and other apps are not touched. Any agent on this computer can
run it for me later.

## 3. Run it (20–30 minutes, the TV restarts twice)

```
optimize/run.sh
```
It saves the TV's state, measures memory and the TV's own screensaver, disables telemetry/ads/demos and my picks,
applies the speed and memory tweaks, installs the screensaver and the home screen (behind the safety switch) and
TV Bro and SmartTube (from their own GitHub releases, the build for this TV's processor),
measures again and writes `optimize/run/report.md`. If it stops for any reason: `optimize/run.sh` again (it
continues), and `optimize/run.sh status` shows where it is.

If the home-screen step says Home did not switch, leave the TV alone for 5 minutes (the stock home comes back by
itself) and tell me. Some TCL firmware keeps its own home: then `SKIP_LAUNCHER=1 optimize/run.sh`.

## 4. Apply my choices (5 minutes; no remote needed)

```
optimize/saver.sh cities                          # find my city, then: optimize/saver.sh city <name>   (not listed: city <name> <lat> <lon>)
optimize/saver.sh find-team <club as I wrote it>  # Arabic is fine (الهلال): one call per club, Saudi clubs first;
                                                  # then: optimize/saver.sh teams <id:Name> <id:Name> ...  (my order)
optimize/saver.sh card <one of: verse, news, shows, prayers, forecast, matches, none>   # beside the clock (verse unless I said otherwise)
optimize/saver.sh home-card none                  # home screen: no card
optimize/dock.sh apps                             # match my 6 apps to packages, then: optimize/dock.sh set <pkg> ... (in that order)
optimize/dock.sh hidden                           # apps the home screen leaves out, and why
optimize/saver.sh photos <5, 10, 15, 30 or 60>   # ONLY if I asked how often the photo changes (default 30 minutes: leave it)
optimize/saver.sh idle <1, 2, 5, 10, 15 or 30>   # ONLY if I asked when the screensaver starts after the TV idles (the TV's own timer: leave it)
optimize/saver.sh update
optimize/saver.sh show                            # read both back to me
optimize/dock.sh show
```
The home screen lists what the TV's own home listed: next to a Google home only TV apps, so the maker's helpers
stay out. Change that only by these rules: `optimize/dock.sh show <pkg>` for an app it left out that is one of
mine (on the step 1 screenshot) or that I asked for; `optimize/dock.sh hide <pkg>` for an app it shows that is in
`optimize/lists/tcl-safe.txt` or `tcl-optional.txt`; never hide one in `keep.txt`. Any other you are unsure about:
list it for me in step 7 instead of choosing.

Take every id from `find-team`'s output, which comes from our list of every club in the app's leagues and the
national teams; never guess one. If it gives several, pick the one I meant (a Saudi club before a namesake abroad).
If I said «لا شي» to clubs, skip `find-team` and `teams`. `optimize/saver.sh` without arguments lists every option.

## 5. Check (5 minutes)

```
optimize/shot.sh home
optimize/shot.sh screensaver
```
Look at both screenshots (in `optimize/run/shots/`; open the `-small.png` ones, 960 px wide: the full-size files are often too large for an agent to attach) and describe them to me. Then check Home from inside an app
yourself (I may have left): open one, press Home, and the new home must be in front:
```
optimize/check-home.sh          # opens YouTube (else my first favourite), presses Home; or: optimize/check-home.sh <another app of mine>
```

## 6. Before you tell me it's done

Check each, and fix or report what fails:
```
optimize/keep.sh                                  # must end with «Protected features: all on.»
optimize/run.sh status                            # every step ✓, and «report: no !! lines»
```
- `report.md` shows Home as `sa.urtv.home` (or the stock home if we used `SKIP_LAUNCHER=1`) and the screensaver
  as `sa.urtv.screensaver`.
- You looked at the step 5 screenshots yourself and they show my city, my card and my favourites.

## 7. Tell me

- The before/after table from `optimize/run/report.md` and its feature check.
- What was disabled, my settings, and anything that looked wrong or slow.
- When I'm at the TV, to try voice from the remote and casting (AirPlay or Cast) from a phone once, and to tell
  you if either doesn't work (`optimize/keep.sh` turns them back on).
- In one line, what I can still ask you to change any time: the favourites, the card on the home screen, the
  screensaver delay, and the optional TCL extras (the TCL tab and channels, game bar, T-Cast…).
- How to undo, again: the folder's full path and the exact command to paste there
  (`optimize/reset.sh`; on Windows in Git Bash), what it brings back, and that if I ever want it, I can open an
  agent on this computer and say «رجّع التلفزيون مثل ما كان: شغّل optimize/reset.sh في مجلد UrTV».

## 8. Ask me to share how it went (the very last thing)

Only after everything above, undo included, and nowhere earlier: your last message, on its own, word for word:

> آخر شي: تبغى ترسل لصاحب المشروع ملخص كيف مشى التركيب؟ يساعده يحسّنه للناس. فيه بس نوع التلفزيون ونسخة
> أندرويد، وكم أخذت كل خطوة ووش اللي تعطّل، والذاكرة قبل وبعد. ما فيه أي شي شخصي: لا مدينة ولا أندية ولا
> تطبيقات ولا عناوين. وإذا عندك كلمة تبغى توصله (عجبك؟ شي ضايقك؟)، اكتبها مع ردك.

If I say yes, run it with: your name and model as the agent; my words if I wrote any (as I wrote them, minus
anything personal); and your own notes: what failed or was slow, how long it cost and what fixed it, in two to five
plain lines, with no names, addresses or paths:
```
optimize/share.sh --agent "<you and your model>" --owner "<my words, or leave it out>" "<your notes>"
```
This is the one command here that needs quotes: the agent name and the notes are several words (no apostrophes
in them). On Windows, inside the Git Bash `-lc "…"` string use single quotes for each:
`-lc "cd <this folder> && optimize/share.sh --agent '<you and your model>' --owner '<my words>' '<your notes>'"`.
It sends the summary to the author by itself (anonymously, no account) and prints `Sent to the author`: show me the
text and tell me it went. If it couldn't send it, it opens a QR code on this computer's screen instead
(`optimize/run/share-qr.png`; if it didn't open, open it for me): tell me to scan it with my phone's camera, and the
page that opens has a button that sends it. Never run it if I say no.
