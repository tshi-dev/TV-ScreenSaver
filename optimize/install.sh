#!/usr/bin/env bash
# Download the latest APKs from GitHub and install them on the TV.
#   optimize/install.sh saver      شاشة تلفزيونك: install + make it the screensaver
#   optimize/install.sh launcher   the home screen: install + make it the default Home
#                                  (it carries the «تلاوة» screens: the Quran recitations and the radio)
#   optimize/install.sh extras     two free apps from their own GitHub releases: TV Bro (a TV web browser,
#                                  github.com/truefedex/tv-bro) and SmartTube (YouTube without ads,
#                                  github.com/yuliskov/SmartTube), the build that fits the TV's processor
#   optimize/install.sh all        both (saver, launcher)
# The first install of the screensaver replaces an APK signed with another key: uninstall the old one first
# (pm uninstall sa.urtv.screensaver) — this script does that only if a plain install fails.
set -euo pipefail
cd "$(dirname "$0")/.."
PY="${PY:-optimize/venv/bin/python}"
REL=https://github.com/tshi-dev/UrTV/releases/download/${APK_TAG:-apk-latest}   # APK_TAG=v3.N: that numbered build
what="${1:?usage: optimize/install.sh saver|launcher|extras|all}"
case "$what" in saver|launcher|extras|all) ;; *) echo "usage: optimize/install.sh saver|launcher|extras|all" >&2; exit 2;; esac
failed=0   # an extra that did not install: the rest are still tried, then run.sh must not count the step as done

# curl with up to 3 tries. A Windows curl (Schannel) that can't reach the certificate revocation server fails every
# download; on that error, and only if this curl has the flag, the next try skips just that check.
curl_get() {
  local i rc=0 errf extra=()
  errf=$(mktemp)
  for i in 1 2 3; do
    rc=0; curl "${extra[@]+"${extra[@]}"}" "$@" 2> "$errf" || rc=$?
    [ "$rc" = 0 ] && { rm -f "$errf"; return 0; }
    [ "$rc" = 22 ] && break   # the server answered with an error (no such file): another try gives the same
    if [ "${#extra[@]}" = 0 ] && grep -qiE 'revocation|CRYPT_E_' "$errf" \
       && curl -V 2> /dev/null | grep -qi schannel && curl --help all 2> /dev/null | grep -q ssl-revoke-best-effort; then
      echo "Windows could not check the certificate revocation list: trying again without that check." >&2
      extra=(--ssl-revoke-best-effort)
    fi
    [ "$i" = 3 ] || sleep $((i * 2))
  done
  cat "$errf" >&2; rm -f "$errf"
  return "$rc"
}

# Play Protect can refuse an ADB install (INSTALL_FAILED_VERIFICATION_FAILURE, seen on a TCL Android 14): its ADB
# check is switched off for that install only, and put back as it was, whatever happens (see the EXIT trap).
VERIFIER=verifier_verify_adb_installs
verifier_old=; verifier_set=0
restore_verifier() {
  [ "$verifier_set" = 1 ] || return 0
  verifier_set=0
  if [ -z "$verifier_old" ] || [ "$verifier_old" = null ]; then
    "$PY" -I optimize/tv.py "settings delete global $VERIFIER" > /dev/null 2>&1 || true
  else
    "$PY" -I optimize/tv.py "settings put global $VERIFIER $verifier_old" > /dev/null 2>&1 || true
  fi
}
trap restore_verifier EXIT

pm_install() {   # $1 file name in /data/local/tmp, $2 package: pm's answer. A big APK can install silently for over a
  # minute, which ADB takes for a dead TV: it runs in the background on the TV and is polled (tv.py --bg).
  # Some operator TVs have a system service that removes apps installed from ADB seconds later; installed with the
  # Play Store as the installer they stay. So: still there after 3 s? If not, install once more that way, and say so.
  local out
  out=$("$PY" -I optimize/tv.py --bg install "pm install -r /data/local/tmp/$1")
  if grep -q Success <<< "$out"; then
    sleep 3
    if ! "$PY" -I optimize/tv.py "pm list packages $2" | tr -d '\r' | grep -qx "package:$2"; then
      echo "$2 was removed right after the install (a service of this TV does that to apps installed over ADB): installing it again as if from the Play Store." >&2
      out=$("$PY" -I optimize/tv.py --bg install "pm install -i com.android.vending -r /data/local/tmp/$1")
      sleep 3
      if grep -q Success <<< "$out" && ! "$PY" -I optimize/tv.py "pm list packages $2" | tr -d '\r' | grep -qx "package:$2"; then
        out="Failure [$2 is removed by this TV right after every install]"
      fi
    fi
  fi
  printf '%s\n' "$out"
}

push_install() {   # $1 apk name, $2 package
  if ! curl_get -fsSL -o "optimize/$1" "$REL/$1"; then
    echo "!! Could not download $REL/$1 (tag ${APK_TAG:-apk-latest}): no such build, or no internet"; exit 1
  fi
  "$PY" -I optimize/tv.py --push-b64 "optimize/$1" "/data/local/tmp/$1"
  local out
  out=$(pm_install "$1" "$2")
  if grep -q INSTALL_FAILED_VERIFICATION_FAILURE <<< "$out"; then
    verifier_old=$("$PY" -I optimize/tv.py "settings get global $VERIFIER" | tr -d '\r\n' || true)
    echo "Play Protect refused the install over ADB: its ADB check is off for this install only, and put back right after (was: ${verifier_old:-not set})."
    verifier_set=1
    "$PY" -I optimize/tv.py "settings put global $VERIFIER 0" > /dev/null || true
    out=$(pm_install "$1" "$2")
  fi
  if ! grep -q Success <<< "$out"; then
    # only a signing-key clash justifies removing the installed app (and its settings); anything else stops here
    if grep -qE 'UPDATE_INCOMPATIBLE|signatures do not match' <<< "$out"; then
      echo "An older $2 signed with another key is installed: removing it, then installing again."
      # removing the home app while the stock one is off would leave the TV with no home at all
      [ "$2" = sa.urtv.home ] && "$PY" -I optimize/tv.py "pm enable ${STOCK%%/*}" > /dev/null || true
      "$PY" -I optimize/tv.py "pm uninstall $2" || true
      out=$(pm_install "$1" "$2")
      grep -q Success <<< "$out" || { echo "!! Install of $1 failed again: $out"; exit 1; }
    else
      echo "!! Install of $1 failed: $out"; exit 1
    fi
  fi
  restore_verifier
  "$PY" -I optimize/tv.py "rm -f /data/local/tmp/$1"
}

from_github() {   # $1 owner/repo, $2 package, $3 asset pattern ({abi} = the TV's processor types, best first)
  local abis url name out code body
  abis=$("$PY" -I optimize/tv.py "getprop ro.product.cpu.abilist" | tr -d '\r')
  body=$(curl_get -sSL -w '\n%{http_code}' "https://api.github.com/repos/$1/releases/latest") || body=$'\n000'
  code=${body##*$'\n'}; body=${body%$'\n'*}
  if [ "$code" = 403 ] || [ "$code" = 429 ]; then
    echo "!! GitHub rate limit, try later: $1 skipped"; failed=1; return 0
  elif [ "$code" != 200 ]; then
    echo "!! GitHub did not list the releases of $1 (HTTP $code): skipped"; failed=1; return 0
  fi
  url=$("$PY" -I -c '
import json, re, sys
assets = {a["name"]: a["browser_download_url"] for a in json.load(sys.stdin)["assets"]}
for abi in [a for a in sys.argv[2].split(",") if a] + ["universal", ""]:
    pat = re.compile(sys.argv[1].replace("{abi}", re.escape(abi)) + "$")
    hit = sorted(n for n in assets if pat.search(n))
    if hit:
        print(assets[hit[0]]); break
' "$3" "$abis" <<< "$body") || true
  if [ -z "$url" ]; then echo "!! No build of $1 for this TV ($abis): skipped"; return 0; fi
  name=$(basename "$url")
  if ! curl_get -fsSL -o "optimize/$name" "$url"; then echo "!! Could not download $url"; failed=1; return 0; fi
  "$PY" -I optimize/tv.py --push-b64 "optimize/$name" "/data/local/tmp/$name"
  out=$(pm_install "$name" "$2")
  "$PY" -I optimize/tv.py "rm -f /data/local/tmp/$name" > /dev/null
  rm -f "optimize/$name"
  if grep -q Success <<< "$out"; then echo "Installed $name"
  elif grep -qE 'UPDATE_INCOMPATIBLE|signatures do not match' <<< "$out"; then
    echo "$2 is already installed from somewhere else: left as it is (with its settings)."   # never removed
  else echo "!! Install of $name failed: $out"; failed=1; fi
}

if [ "$what" = extras ]; then
  from_github truefedex/tv-bro com.phlox.tvwebbrowser 'tvbro-[0-9.]+-generic-geckoExcluded\.apk'   # light: the TV's WebView
  from_github yuliskov/SmartTube org.smarttube.stable 'SmartTube_stable_[0-9.]+_{abi}\.apk'
  [ "$failed" = 0 ] || exit 1
fi

if [ "$what" = saver ] || [ "$what" = all ]; then
  push_install urtv-screensaver.apk sa.urtv.screensaver
  printf '%s\n' \
    "settings put secure screensaver_enabled 1" \
    "settings put secure screensaver_components sa.urtv.screensaver/.ScreenDream" \
    "settings put secure screensaver_default_component sa.urtv.screensaver/.ScreenDream" \
    "am start -n sa.urtv.screensaver/.SettingsActivity" \
    | "$PY" -I optimize/tv.py --batch
  echo "Screensaver installed and selected. Its settings screen is open on the TV."
fi

if [ "$what" = launcher ] || [ "$what" = all ]; then
  # Which stock home does this TV have? Google TV sets: launcherx. Older Android TV: tvlauncher.
  if "$PY" -I optimize/tv.py "pm list packages com.google.android.apps.tv.launcherx" | grep -q launcherx; then
    STOCK=com.google.android.apps.tv.launcherx/com.google.android.apps.tv.launcherx.home.HomeActivity
  else
    STOCK=com.google.android.tvlauncher/.MainActivity
  fi
  echo "Stock home: $STOCK"

  # 1. The new home must be installed and working BEFORE anything is disabled.
  push_install urtv-home.apk sa.urtv.home
  "$PY" -I optimize/tv.py "pm grant sa.urtv.home android.permission.ACCESS_FINE_LOCATION" || true
  # exact alarms for the adhan (Android 12 and newer; older TVs need nothing)
  "$PY" -I optimize/tv.py "appops set sa.urtv.home SCHEDULE_EXACT_ALARM allow" || true
  # The launcher updates itself: without this it can download a build but Android never lets it install.
  "$PY" -I optimize/tv.py "appops set sa.urtv.home REQUEST_INSTALL_PACKAGES allow" || true

  # Steps 2-4 as one function, so an ADB drop in the middle (TCL G08: Wi-Fi fell during the batch) can be retried.
  # "verify" as $1: only step 4 (the switch is armed already and the batch may have run).
  # Returns 0: the new home is on screen and the switch is disarmed; 1: tv.py failed (connection lost);
  # 2: Home did not open the new launcher; 3: the safety switch fired first.
  switch_home() {
    local top res ours=no _
    if [ "${1:-}" != verify ]; then
      # 2. Arm the safety switch on the TV: it restores the stock home in 300 s unless we confirm (slow ADB needs time).
      "$PY" -I optimize/tv.py --push-b64 optimize/deadman.sh /data/local/tmp/deadman.sh || return 1
      printf '%s\n' \
        "rm -f /data/local/tmp/home_ok" \
        "nohup sh /data/local/tmp/deadman.sh $STOCK 300 > /dev/null 2>&1 &" \
        | "$PY" -I optimize/tv.py --batch || return 1
      echo "Safety switch armed: the stock home comes back by itself in 5 minutes unless the new home works."

      # 3. Switch: the HOME role (Android 10+; the classic set-home-activity for older sets and as a backup), then
      #    the setup app off, then the stock home (this order avoided the black screen).
      printf '%s\n' \
        "cmd role add-role-holder --user 0 android.app.role.HOME sa.urtv.home" \
        "cmd package set-home-activity --user 0 sa.urtv.home/.MainActivity" \
        "pm disable-user --user 0 com.google.android.tungsten.setupwraith" \
        "pm disable-user --user 0 ${STOCK%%/*}" \
        "cmd package set-home-activity --user 0 sa.urtv.home/.MainActivity" \
        "am force-stop com.android.tv.settings" \
        "input keyevent KEYCODE_HOME" \
        | "$PY" -I optimize/tv.py --batch || return 1
    fi
    # 4. Verify Home really opens the new launcher (a few tries: a slow TV), then disarm the switch.
    for _ in 1 2 3; do
      sleep 6
      top=$("$PY" -I optimize/tv.py "dumpsys activity activities | grep -E 'mResumedActivity|topResumedActivity'") || return 1
      if grep -q sa.urtv.home <<< "$top"; then ours=yes; break; fi
      "$PY" -I optimize/tv.py "input keyevent KEYCODE_HOME" > /dev/null || return 1
    done
    [ "$ours" = yes ] || return 2
    "$PY" -I optimize/tv.py "touch /data/local/tmp/home_ok" || return 1
    # the switch may have fired already if ADB was very slow: then the stock home is back
    res=$("$PY" -I optimize/tv.py "cmd package resolve-activity --brief -c android.intent.category.HOME -a android.intent.action.MAIN") || return 1
    grep -q sa.urtv.home <<< "$res" || return 3
  }

  rc=0; switch_home || rc=$?
  if [ "$rc" = 1 ]; then   # ADB dropped: wait for the TV, see what Home is now, and go on or start the switch again
    back=no
    for _ in $(seq 36); do   # every 10 s, about 6 minutes
      if "$PY" -I optimize/tv.py "getprop ro.serialno" > /dev/null 2>&1; then back=yes; break; fi
      sleep 10
    done
    if [ "$back" = no ]; then
      echo "!! The TV did not come back within 6 minutes. Check it is on and on Wi-Fi: the safety switch restores the stock home."
      echo "   Then run the same command again."
      exit 1
    fi
    res=$("$PY" -I optimize/tv.py "cmd package resolve-activity --brief -c android.intent.category.HOME -a android.intent.action.MAIN" 2> /dev/null) || res=""
    rc=0
    if grep -q sa.urtv.home <<< "$res"; then
      echo "ADB dropped during the switch; the TV is back and the new home is already set: checking it"
      switch_home verify || rc=$?
    else
      echo "ADB dropped during the switch; the TV is back, trying the switch again"
      switch_home || rc=$?   # arms the switch again first
    fi
  fi
  case "$rc" in
    0) ;;
    2) echo "!! Home did not open the new launcher. Leave the TV alone: the stock home returns within 5 minutes."
       echo "   Some TCL firmware pins Home to the stock launcher; on those sets keep the stock home."
       exit 1 ;;
    *) echo "!! The safety switch put the stock home back (ADB was too slow). Nothing is broken: run the same command again."
       exit 1 ;;
  esac
  # 5. Only now: the stock home's helpers are dead weight (restore.sh brings them back).
  grep -vE '^\s*(#|$)' optimize/lists/home-replaced.txt | awk '{print "pm disable-user --user 0 " $1}' \
    | "$PY" -I optimize/tv.py --batch > /dev/null || true
  echo "Launcher is home. Press Home from inside an app to double-check."
  echo "Undo everything: optimize/reset.sh"
fi
