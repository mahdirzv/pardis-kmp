# Rivana Reskin — Slice 1: Foundation + Reskin (Android) — Design Spec

**Date:** 2026-06-06
**Status:** Approved (design); pending implementation plan
**Platform:** Android (Compose) only. Light theme only.
**Branding:** Keep the "Pardis" name. Apply the Rivana *visual language* only.

---

## 1. Background & goal

The "Rivana" handoff (`~/Downloads/Rivana Storyteller App (standalone).html`) is a high-fidelity
HTML/CSS/React prototype of a redesigned Persian-myth storyteller app: a full design system plus
~9 screens across a 5-tab IA. It is far too large for one spec, so it was decomposed into shippable
slices in dependency order:

1. **Slice 1 (this spec) — Foundation + reskin:** expand the design tokens and restyle the *existing*
   Library + Reader screens to the Rivana look. No new data, no new tabs.
2. Slice 2 — App shell (5-tab nav) + Today hub + richer Story Detail.
3. Slice 3 — Rewards + Bedtime/Lullaby + You/Parent/Onboarding (each needs new data + backend).

**Goal of slice 1:** ship a visibly-redesigned, fully-working Android app (Library + Reader) in the
Rivana visual language, with zero functional regressions, and a token/component foundation that
slices 2–3 build on.

**Key context:** the current `tokens.json` is already ~90% of the Rivana palette (it has
`saffronSoft/Tint`, `indigoSoft/Tint`, `surfacePeach`, `mintDeep`, `lilacDeep`, `inkFaint`,
`borderStrong`, etc.). The largest net-new piece is **typography** — the app currently bundles **no
fonts** and renders everything (including Farsi) in the system font.

---

## 2. Architecture & approach

**Execution approach:** token-first, screen-by-screen, with a *small* shared Compose component layer
extracted as we go (only primitives the reskin actually needs). The 792-line `PardisApp.kt` is split
into focused files. On-device verification on a Galaxy A32 after each step.

**No token generator exists** — `design-system/tokens.json` is the source of truth and the generated
Kotlin is **hand-synced**. There are two identical Android copies that must both be updated:
- `design-system/generated/android/PardisTokens.kt` (canonical generated output)
- `app/src/main/java/app/pardis/design/PardisTokens.kt` (what the app imports; package `app.pardis.design`)

The iOS Swift token file (`design-system/generated/ios/PardisTokens.swift`) is **out of scope** for this
slice (Android-first); it may drift and will be re-synced when iOS is reskinned.

### Target file structure (under `app/src/main/java/app/pardis/android/ui/`)

```
ui/
  PardisApp.kt           // NavHost + RTL provider only (slim)
  theme/
    PardisTypography.kt  // named Compose TextStyles + FontFamily wiring
  components/            // small shared layer (reused by slices 2–3)
    Buttons.kt           // PardisButton: accent / ink / lapis / soft / ghost variants, sizes
    Chips.kt             // Chip (filter, is-active), Tag (colored variants)
    Cards.kt             // PardisCard; Cover (story cover w/ art + pattern + progress + tags)
    Pattern.kt           // Persian paisley overlay as a Painter/vector
    Progress.kt          // thin progress bar
    SectionHead.kt       // EN title + FA subtitle header row
  library/
    LibraryScreen.kt
    StoryCard.kt         // (Cover-based)
  reader/
    ReaderScreen.kt
    PageDots.kt
    NarrationDock.kt
    VocabSheet.kt
```

Only primitives the reskin needs get extracted — no speculative components.

---

## 3. Tokens (additive only)

All changes are **additive** so existing code keeps compiling. Edit `tokens.json`, then hand-sync both
Android Kotlin copies.

### 3.1 Fonts (new `fonts` block + `font` role on typography)
- `display` = **Bricolage Grotesque** (SIL OFL)
- `body` = **Plus Jakarta Sans** (SIL OFL)
- `fa` = **Vazirmatn** (SIL OFL) — bundled explicitly for consistent Farsi rendering across devices
- `mono` = **JetBrains Mono** — **only if** a `micro`/`eyebrow` style actually needs it; otherwise omit (YAGNI)

Each `typography` scale entry gains a `font` role (e.g. `3xl`/`2xl` → display, `base`/`sm` → body).

### 3.2 Colors
Add only what reskinned screens use:
- `lilacTint` (prototype `--lilac-soft` reused as tint)
- `rose`, `sun` families — **only if** a reskinned screen uses them. The Library "By age" tiles use
  mint/saffron/lapis (already present), so rose/sun are likely **not** needed → add only on demand.

### 3.3 Radii
Extend to match the rounder Rivana look (current `xl` = 24):
- `xl2: 28`, `xl3: 34`

### 3.4 Shadows
- Already have `saffron`, `indigo` glows. Add `mint` glow **only if** used.

### 3.5 Gradients (new `gradients` block)
- `gradNight`, `gradLapis`, `gradSaffron`, `gradDawn` — used by reader illustration scrims (and future
  bedtime). Represented as Compose `Brush` helpers derived from tokens.

### 3.6 Explicitly NOT added this slice
- Swappable `[data-accent]` system — **single fixed accent = saffron**.
- Dark-theme token values — defined structure may allow it later, but **not** populated/wired now.

---

## 4. Typography object

New `PardisTypography` (Compose) exposing named styles built from tokens, each wired to the right
`FontFamily`:
`display`, `h1`, `h2`, `h3`, `lead`, `body`, `small`, `micro`, `eyebrow`, `fa`.

Screens stop hand-rolling `TextStyle`s and use these named styles. Fonts live in
`app/src/main/res/font/` as `.ttf`/`.otf`, referenced via `FontFamily`.

---

## 5. Library reskin (`LibraryScreen.kt`)

**Preserve all current functionality:** search, age-band filter, cached-only toggle, per-card download
progress/controls, cover caching, refresh. Restyle to Rivana:

- **Header:** `Library` (display font) + Farsi subtitle `کتابخانه‌ی قصه‌ها` (`inkMuted`). Right side: a
  **grid/list segmented toggle** (new, pure Compose UI state; grid default).
- **Search:** the functional search field styled as a rounded pill (search icon + text). **Drop the mic
  icon** (no voice search).
- **Filter chips:** the real **age-band chips + a "Cached" toggle**, styled as Rivana chips
  (active = ink fill). **Drop** the prototype's `collection` filters (no data).
- **Cards → `Cover`:** cover image (local preferred), rounded with a subtle paisley overlay, EN + FA
  titles, small tags (age band · minutes · vocab count), and existing download state
  (progress / downloaded-size / failed-retry). **Tap → opens the Reader** (no Detail screen in slice 1).
- **"By age" helper:** `SectionHead` ("By age" / "بر اساس سن") + colored tiles generated from the derived
  `ageBands`; tapping a tile applies that age filter (uses existing data).
- Soft **paisley/vine page background**, faded at top.

No changes to `LibraryViewModel` / `LibraryUiState` / `LibraryAction` behavior. The grid/list and any
purely-visual toggles are local Compose state in the screen, not shared state.

---

## 6. Reader reskin (`ReaderScreen.kt` + components)

**Visual reskin + light chrome.** Keep the existing narration + video + vocab-chip model. Restyle and
add only the lightweight chrome agreed:

- **Top bar:** back chevron (icon button), centered title (display, ellipsized) + `Page X of N`
  micro-label, and the right icon button = the existing **video-mode toggle** (shown when `videoReady`).
  *(Replaces the prototype's non-functional bookmark button — we have no bookmark feature.)*
- **Animated page-dots** (`PageDots.kt`) under the title — active dot elongates; tappable to jump pages.
- **Illustration card:** ~290dp tall, large radius, image (local preferred) with paisley overlay +
  a `n / N` page badge.
- **Prose:** new **EN / FA / both** display toggle (default *both*; **local Compose UI state**, not shared).
  EN in display font, FA in Vazirmatn RTL. **Vocab stays as chips** below the prose, with the "tap a word
  to learn it" hint. (No inline dotted words this slice.)
- **Narration dock** (`NarrationDock.kt`): bottom surface card (rounded top, border, soft shadow) holding
  a thin progress bar (bound to **page position**, `currentPage/pageCount`) + the existing transport
  (prev / play-pause / next) + **rate** and **narration-lang** controls, restyled into the dock. No new
  playback behavior.
- **Swipe-to-turn:** left/right horizontal-drag gesture changes page (over existing next/prev actions).
- **Vocab sheet** (`VocabSheet.kt`): restyled bottom sheet — grip, 28dp top radius, big FA term,
  translit / EN / context, pronounce button. Same data.
- **Video mode:** keep functionally; restyle the player container + captions to the Rivana look.

No changes to `ReaderViewModel` / `ReaderUiState` / `ReaderAction` behavior. New visual toggles
(text-display lang, grid/list) are local screen state.

**Not in this slice:** Finish screen, karaoke word-sync, inline dotted Farsi words.

---

## 7. Out of scope (deferred / YAGNI)

- Dark mode and any theme switch (tokens *allow* it later; not wired).
- Swappable accents and the "Tweaks" design playground.
- 5-tab nav and all net-new screens: Today, Detail, Bedtime, Lullaby, Rewards, Character, You, Parent,
  Onboarding (slices 2–3).
- Finish screen, karaoke word-sync, inline dotted words (Reader, later).
- New data-model fields (badges, characters, lullabies, profiles, streaks).
- Collection filters, `new` flag, voice/mic search, bookmark feature.
- CSS "scenes" — keep using real `illustrationUrl`/`coverUrl` images.
- iOS reskin and the Rivana rename (Android-first; name stays "Pardis").

---

## 8. Verification & testing

Mostly presentational, so verification is layered:

- **Unit tests (TDD)** for the bits with real logic: token-sync correctness (generated `PardisTokens.kt`
  matches `tokens.json` keys/values), the age-tile → filter mapping, and any pure formatters/helpers.
  Existing shared-layer logic (download, narration, video, progress) is untouched → its tests stay green.
- **Compose `@Preview`** for each new component (`Buttons`, `Chips`, `Cover`, `SectionHead`, `PageDots`,
  `NarrationDock`) and for the Library + Reader screens — fast visual iteration without a device.
- **Build gate:** `./gradlew :app:assembleDebug` compiles after each step.
- **On-device check** (Galaxy A32 via `adb`): walk Library (search, filter, cached toggle, grid/list,
  download a story) and Reader (page-dots, swipe, EN/FA/both toggle, narration dock, vocab sheet, video
  mode) against the prototype — confirm **no functional regressions**.
- **CI** (already green) keeps validating the Android build on every push.

---

## 9. Success criteria

- Library and Reader render in the Rivana visual language (palette, display/body/FA fonts, rounded cards,
  chips, dock, page-dots) in light theme.
- All current functionality works unchanged: search, age-band filter, cached toggle, downloads, narration
  (rate + lang), video mode, vocab sheet, progress save/resume.
- New chrome works: grid/list toggle, EN/FA/both text toggle, swipe-to-turn, animated page-dots.
- Tokens are expanded additively and both Android Kotlin copies are in sync with `tokens.json`.
- `PardisApp.kt` is split into the focused files above; the extracted `components/` layer is reusable.
- `./gradlew :app:assembleDebug` passes; CI green; verified on-device.
