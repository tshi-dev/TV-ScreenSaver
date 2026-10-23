#!/usr/bin/env bash
# شاشة تلفزيونك's settings, for agents: no remote, no menus.
#   optimize/saver.sh show                         current settings
#   optimize/saver.sh cities                       built-in Saudi cities by region
#   optimize/saver.sh city الرس                    a built-in city (Arabic name as listed)
#   optimize/saver.sh city 'اسم' 25.86 43.49       any place: name, latitude, longitude
#   optimize/saver.sh find-team الهلال             a club's id (Arabic or English name): id:Name | league
#   optimize/saver.sh teams 136013:Al-Hilal 150643:Al-Zulfi    followed teams in order (up to 10)
#   optimize/saver.sh card news                    beside the clock: news shows prayers forecast matches verse none
#   optimize/saver.sh home-card none               on the home screen (launcher): same choices, none = off
#   optimize/saver.sh genres Drama,Comedy          episodes card: Drama Comedy Action Crime Thriller Mystery
#                                                  Science-Fiction Fantasy Adventure Romance Family History Horror
#                                                  Anime Documentary Sports  ("" = all)
#   optimize/saver.sh langs Arabic,English         episodes card: English Arabic Korean Japanese Turkish Spanish
#                                                  French Hindi German  ("" = all)
#   optimize/saver.sh minrating 7                  episodes card: lowest rating shown: 0 6 6.5 7 7.5 8
#   optimize/saver.sh photos 15                    change the photo every N minutes: 5 10 15 30 60 (default 30)
#   optimize/saver.sh idle 10                      the TV starts the screensaver after N idle minutes: 1 2 5 10 15 30
#                                                  (the TV's own timer; left alone unless you run this)
#   optimize/saver.sh update                       download the latest screen now
# Only ADB can change these (sa.urtv.screensaver/.SaverReceiver needs the shell's DUMP).
set -euo pipefail
cd "$(dirname "$0")/.."
PY="${PY:-optimize/venv/bin/python}"
send() {   # $1 action, $2 extras; prints the receiver's answer (the "data" of am broadcast); exit 1 on "error:"
  local out
  out=$("$PY" -I optimize/tv.py "am broadcast -n sa.urtv.screensaver/.SaverReceiver -a sa.urtv.screensaver.$1 ${2:-}" \
    | sed -n '/data="/,$p' | sed '1s/.*data="//; $s/"$//')
  echo "$out"
  [[ "$out" != error:* ]]
}
quote() {  # one shell-safe argument for the TV's shell
  case "$1" in *"'"*) echo "no apostrophes please: $1" >&2; exit 1 ;; esac
  printf "'%s'" "$1"
}
case "${1:-}" in
  show)   send GET ;;
  cities) send CITIES ;;
  update) send UPDATE ;;
  city)
    [ $# -eq 2 ] || [ $# -eq 4 ] || { echo "usage: optimize/saver.sh city NAME [LAT LON]"; exit 1; }
    extra="--es city $(quote "$2")"
    [ $# -eq 4 ] && extra="$extra --es lat $(quote "$3") --es lon $(quote "$4")"
    send SET "$extra" ;;
  teams)
    shift
    [ $# -gt 0 ] || { echo "usage: optimize/saver.sh teams ID:EnglishName ..."; exit 1; }
    send SET "--es teams $(quote "$(IFS=,; echo "$*")")" ;;
  card)      send SET "--es panel $(quote "${2:?usage: optimize/saver.sh card prayers}")" ;;
  home-card) send SET "--es home_panel $(quote "${2:?usage: optimize/saver.sh home-card none}")" ;;
  genres)    send SET "--es genres $(quote "${2-}")" ;;
  langs)     send SET "--es langs $(quote "${2-}")" ;;
  minrating) send SET "--es minrating $(quote "${2:?usage: optimize/saver.sh minrating 7}")" ;;
  photos)    send SET "--es photos $(quote "${2:?usage: optimize/saver.sh photos 15}")" ;;
  idle)
    # the app may change the TV's timer only if the owner allowed it; the shell always may
    out=$(send SET "--es idle $(quote "${2:?usage: optimize/saver.sh idle 10}")") || true
    case "$out" in
      "error: idle not allowed"*)
        case "$2" in 1|2|5|10|15|30) ;; *) echo "error: idle is one of 1 2 5 10 15 30"; exit 1 ;; esac
        "$PY" -I optimize/tv.py "settings put system screen_off_timeout $(($2 * 60000))"
        echo "idle: $2 (set from the shell)" ;;
      error:*) echo "$out"; exit 1 ;;
      *) echo "$out" ;;
    esac ;;
  find-team) shift; "$PY" -I optimize/find-team.py "$@" ;;   # Arabic or English name
  *) sed -n '2,20p' "$0"; exit 1 ;;
esac
