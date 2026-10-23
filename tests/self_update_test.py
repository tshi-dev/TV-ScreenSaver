#!/usr/bin/env python3
"""Checks for the home screen's self-update: latest.json, the same signing key, PackageInstaller.

    python3 tests/self_update_test.py

No network and no Android SDK needed. tests/run.sh runs it with the other checks.
"""
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MODULE = ROOT / "android/launcher"
APP = MODULE / "src/main"
JAVA = APP / "java/sa/urtv/home"
A = "{http://schemas.android.com/apk/res/android}"
failures = []


def check(name, ok, detail=""):
    print(("PASS " if ok else "FAIL ") + name + ("" if ok else f"  -> {detail}"))
    if not ok:
        failures.append(name)


def text(path):
    return Path(path).read_text(encoding="utf-8")


updater = text(JAVA / "SelfUpdater.java")
check("the update reads latest.json", "latest.json" in updater)
check("the update checks the signing key", "fingerprint" in updater and "getApkContentsSigners" in updater)
check("the update installs through PackageInstaller", "PackageInstaller" in updater)
check("a local build can point the updater elsewhere",
      'name="update_base"' in text(APP / "res/values/config.xml"))
check("the pill appears from the updater's state", "UPDATE_PILL_ID" in text(JAVA / "MainActivity.java"))

manifest = ET.parse(APP / "AndroidManifest.xml").getroot()
permissions = {p.get(A + "name") for p in manifest.findall("uses-permission")}
check("the installer permission is declared", "android.permission.REQUEST_INSTALL_PACKAGES" in permissions)
receivers = {r.get(A + "name"): r.get(A + "exported") for r in manifest.iter("receiver")}
check("the installer's answer goes to a private receiver", receivers.get(".UpdateReceiver") == "false",
      receivers)

workflow = text(ROOT / ".github/workflows/android.yml")
check("CI publishes latest.json", workflow.count("latest.json") >= 3, workflow.count("latest.json"))
check("CI writes the build number into latest.json",
      '"run": %s' in workflow and "$GITHUB_RUN_NUMBER" in workflow)
install = text(ROOT / "optimize/install.sh")
check("install grants the install-permission appop", "REQUEST_INSTALL_PACKAGES allow" in install)
check("install still knows the two apps", "urtv-home.apk" in install and "urtv-screensaver.apk" in install)

if failures:
    print(f"\n{len(failures)} self-update check(s) failed.")
    sys.exit(1)
print("\nAll self-update checks passed.")
