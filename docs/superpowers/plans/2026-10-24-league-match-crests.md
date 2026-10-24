# Selected Full-League Match Crests Implementation Plan

> **For agentic workers:** Implement this plan inline, one task at a time, and keep each change reviewable.

**Goal:** Show available club crests beside team names in match rows on both the launcher home screen and the screensaver.

**Architecture:** Reuse the existing badge URLs in league fixtures, shared crest renderer, 48 px resize/cache, and match-row fitting logic. The screensaver currently disables this shared feature with a widget-only gate; widening that gate enables the existing renderer without adding a provider, polling loop, or packaged images.

**Tech Stack:** Plain HTML/CSS/JavaScript, Python page builder, existing Node browser-rendering regression harness.

## Global Constraints

- Reuse existing badge URLs, team IDs, crest fetch/resize helpers, and local cache; do not add another logo provider or bundle club images into the APK.
- Enable crests wherever match data is rendered, including the screensaver and launcher widget.
- Preserve compact RTL placement, name-only fallback for unavailable badges, and the existing row-fit sequence.
- Keep the existing fixture refresh and crest cache behavior; add no polling loop.

---

### Task 1: Enable and document crests across match surfaces

**Files:**
- Modify: `src/template.html:2427-2433`
- Modify: `tests/page.test.mjs:1194-1270`
- Modify: `docs/FEATURES.md:43`

**Interfaces:**
- Consumes: fixture badge fields `strHomeTeamBadge`, `strAwayTeamBadge`, plus `idHomeTeam`, `idAwayTeam`.
- Produces: the existing `crestImg(ev, side)` and `loadCrests()` path runs for every page that uses match data.

- [x] Change the renderer gate to:

```js
const CRESTS_ON = USES('matches');
```

Update the surrounding comment from launcher-only to both match surfaces. Keep the existing cache, image fallback, CSS sizes, and fit functions unchanged.

- [x] Replace the screensaver test block that asserts there are no crests with an assertion that mocked available badges render on screensaver match rows and a missing away badge keeps its team name visible. Reuse `CREST_GAMES(SHORT_GAMES)`, `crestNet(seen)`, and the existing `#week-list img.cr` selectors; do not add a new test file.

- [x] Update the Arabic feature note in `docs/FEATURES.md` so it says crests appear on the launcher home screen and screensaver.

- [x] Build the page with `optimize/venv/bin/python src/build.py` from the repository root. Confirm the command completes and creates `dist/tv.html`.

- [x] Inspect the changed files and `git diff --check`. Do not run the automated test suite unless the user specifically asks for it.
