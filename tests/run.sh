#!/usr/bin/env bash
# Every check in one go: static checks, then the screen in a real browser. See tests/check.py and page.test.mjs.
#   pip install fonttools brotli pillow && npm install --no-save playwright@1.56.1 && npx playwright install chromium
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tests/check.py
python3 tests/tilawah_test.py
python3 tests/upload_test.py
python3 tests/share_test.py
python3 tests/install_retry_test.py
python3 tests/run_lock_test.py
python3 tests/reboot_test.py
python3 tests/tv_identity_test.py
python3 tests/dock_names_test.py
python3 tests/dock_sleep_test.py
python3 tests/self_update_test.py
python3 tests/find_tv_test.py
python3 tests/find_team_test.py
python3 tests/team_search_test.py
python3 tests/team_ids_test.py
python3 tests/city_match_test.py
python3 tests/home_card_test.py
python3 tests/league_fixtures_test.py
python3 tests/adhkar_tool_test.py
python3 tests/saver_tasks_test.py
python3 tests/saver_off_test.py
python3 tests/task_voice_test.py
python3 tests/photo_prep_test.py
python3 tests/photos_sh_test.py
python3 tests/greeting_test.py
python3 tests/card_box_test.py
python3 tests/adhan_bar_test.py
node tests/page.test.mjs
