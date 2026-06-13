# Handoff: Rivana — Persian-Myth Storyteller App (KMP / Compose Multiplatform)

## Overview
**Rivana** (ریوانا) is a children's bilingual storytelling app — Persian myth & Shahnameh
tales rendered as illustrated, read-along storybooks with English + Farsi text, word-by-word
narration ("karaoke" highlighting), tappable vocabulary, bedtime lullabies, and a rewards/
collection layer. The mock in this bundle is the **iOS phone form factor** but the design is
platform-agnostic and intended to ship on **Android + iOS from one Compose Multiplatform UI**.

The visual language is a **warm Persian storybook system**: lapis blue + saffron + cream,
Bricolage Grotesque display type, soft layered shadows, generously rounded "bento" cards, and
hand-built gradient scene illustrations. It is deliberately *not* a stock-iOS look — everything
is custom-styled, which is exactly why a single shared Compose UI is the right call.

---

## About the Design Files
The files in this bundle are **design references created in HTML/React** — prototypes that show
the intended look, layout, and behavior. **They are not production code to copy.** The React/CSS
is a fidelity reference only.

Your task is to **recreate these designs natively in Kotlin Multiplatform using Compose
Multiplatform** (`androidx.compose` via JetBrains CMP, targeting Android + iOS), following the
architecture and token mapping described below. Where this doc and the HTML disagree, **the HTML
is the source of truth for visuals**; this README is the source of truth for *how to structure
the Kotlin*.

## Fidelity
**High-fidelity.** Colors, type, spacing, radii, shadows, and interactions are all final and
exact. Recreate the UI pixel-faithfully. Every value you need is enumerated in the **Design
Tokens** section and in `app/app.css` (the canonical token sheet).

---

## Recommended architecture

### UI strategy: Compose Multiplatform (shared UI)
Build **one shared Compose UI** in `commonMain`. This app is content/card-driven, not
dependent on platform-native controls, so a shared UI gives you Android + iOS for one
implementation with identical fidelity. Reserve `expect/actual` strictly for platform gaps
(listed below).

### Module layout
```
rivana/
├─ shared/
│  ├─ commonMain/kotlin/
│  │  ├─ design/         # tokens: RivanaColors, RivanaRadii, RivanaType, RivanaShadows, gradients
│  │  ├─ model/          # Story, Character, Lullaby, Badge, Word, Profile, Collection
│  │  ├─ data/           # in-memory repository (port of data.js) → later a real backend
│  │  ├─ ui/
│  │  │  ├─ components/   # Cover, StoryRow, Chip, Tag, CTAButton, IconBox, Progress, Sheet, TabBar…
│  │  │  ├─ scenes/       # gradient "StoryArt" scene renderers (Brush + Canvas)
│  │  │  └─ screens/      # Today, Library, Detail, Reader, Finish, Bedtime, Lullaby, Rewards, Character, You, Parent, Onboarding
│  │  ├─ nav/            # navigation (Decompose or Voyager) + back stack
│  │  └─ state/          # ViewModels / state holders (Coroutines + StateFlow)
│  ├─ androidMain/kotlin/   # actual: fonts, blur, haptics, audio, share
│  └─ iosMain/kotlin/       # actual: fonts, blur, haptics, audio, share
├─ androidApp/               # thin Activity hosting the shared root composable
└─ iosApp/                   # thin SwiftUI app hosting ComposeUIViewController
```

### Suggested libraries
- **Navigation:** Decompose (recommended for typed component trees + back stack) or Voyager.
- **State:** plain `ViewModel` + `StateFlow` + Coroutines. No Redux needed; state is local & small.
- **Resources/fonts:** Compose Multiplatform Resources (`org.jetbrains.compose.components.resources`).
- **Images:** none required — all artwork is procedural gradient scenes (see "Story scenes").
- **Blur (tab bar / overlays):** Haze (`dev.chrisbanes.haze`) for cross-platform backdrop blur,
  or fall back to a translucent solid fill (see "Platform gaps").

### Theming: do NOT use MaterialTheme defaults
Define your own theme object exposed via `CompositionLocal`. This preserves the design's
**"one-line reskin"** property (the HTML swaps accent/theme by writing CSS vars at the root):

```kotlin
@Immutable
data class RivanaTheme(
    val colors: RivanaColors,
    val radii: RivanaRadii,
    val type: RivanaType,
    val shadows: RivanaShadows,
    val gradients: RivanaGradients,
    val gutter: Dp = 22.dp,
)

val LocalRivana = staticCompositionLocalOf<RivanaTheme> { error("No RivanaTheme") }

@Composable
fun RivanaThemeProvider(
    accent: Accent = Accent.Saffron,   // Saffron | Lapis | Mint | Lilac | Rose
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    val theme = remember(accent, dark) { buildTheme(accent, dark) }
    CompositionLocalProvider(LocalRivana provides theme, content = content)
}
```
Read everything as `LocalRivana.current.colors.lapis`, etc. The Tweaks panel (accent / dark mode /
text size / pace) becomes a swap of the value passed in — **no screen re-implementation**.

---

## Design Tokens

> Canonical source: `app/app.css` (`:root` block). All hex below is the **light** theme; the
> dark theme override block is in the same file under `[data-theme="dark"]` and is reproduced in
> "Dark theme" further down.

### Surfaces
| Token | Hex | Use |
|---|---|---|
| `bg` | `#FAF6EE` | warm app background |
| `bgTint` | `#F3EEDD` | section band / inset chip track |
| `surface` | `#FFFFFF` | card base |
| `surface2` | `#FDFAF0` | warm card variant |
| `rule` | `#ECE3D0` | hairline border |
| `ruleSoft` | `#F2ECDD` | faint divider |
| `ruleStrong` | `#DDD2BC` | stronger divider |

### Ink (text)
| Token | Hex | Use |
|---|---|---|
| `ink` | `#14111B` | headings / near-black (warm-violet bias) |
| `inkSoft` | `#4B4760` | body |
| `inkMuted` | `#8A8499` | meta / captions |
| `inkFaint` | `#B6B0C0` | disabled / unspoken words |
| `onDark` | `#FFFFFF` | text on lapis/ink/scene |

### Brand & accents (each has base / deep / soft / tint)
| Family | base | deep | soft | tint |
|---|---|---|---|---|
| **lapis** (primary) | `#2436A1` | `#1A256E` | `#E8EBFB` | `#F0F2FC` |
| (lapis-darker) | `#0F1849` | | | |
| **saffron** (default accent) | `#F08A2D` | `#C46A12` | `#FFE9D2` | `#FFF4E5` |
| **mint** | `#2FA876` | `#1F7A52` | `#DEF5E9` | `#EAF8F1` |
| **lilac** | `#8B6FE6` | `#5235B6` | `#ECE6FB` | `#ECE6FB` |
| **rose** | `#E1547A` | `#B83A5E` | `#FCDEE6` | — |
| **sun** | `#F4B53A` | `#9A6B12` | `#FCEAB6` | — |

**Accent system:** one of {saffron, lapis, mint, lilac, rose} is the live `--accent` (default
**saffron**). It maps to `accent / accentDeep / accentSoft / accentTint` plus an `accentRgb`
triplet used for translucent fills. Switching accent is a Tweak. Model this as an `enum class
Accent` that resolves to the four roles.

### Gradients (use `Brush.linearGradient`)
| Token | Definition |
|---|---|
| `gradNight` | linear 180°: `#1A256E` 0% → `#2436A1` 70% → `#4F2EB5` 100% |
| `gradLapis` | linear 135°: `#2436A1` 0% → `#4F2EB5` 55% → `#1A256E` 100% |
| `gradSaffron` | linear 135°: `#F08A2D` 0% → `#F4B53A` 60% → `#FFD08A` 100% |
| `gradDawn` | linear 135°: `#FFE9D2` 0% → `#ECE6FB` 50% → `#E8EBFB` 100% |

> In Compose, a CSS `135deg` gradient ≈ `Brush.linearGradient(start = Offset(0,0), end =
> Offset(w,h))`; `180deg` ≈ top→bottom. Build helpers that take the size so angles stay correct.

### Typography
Fonts (bundle all four as resources):
- **Bricolage Grotesque** — display/headings (weights 400–800; opsz variable, use 12–96)
- **Plus Jakarta Sans** — body/UI (400/500/600/700/800)
- **JetBrains Mono** — micro labels / eyebrows / timecodes (500/600)
- **Vazirmatn** — Farsi text (400/500/600/700) — **required for all Persian content**

| Style | Family | Size (px) | Weight | Letter-spacing | Line-height |
|---|---|---|---|---|---|
| `display` | Bricolage | (per-use, large) | 800 | -0.038em | 0.98 |
| `h1` | Bricolage | 30 | 800 | -0.034em | 1.02 |
| `h2` | Bricolage | 23 | 700 | -0.028em | 1.08 |
| `h3` | Bricolage | 18 | 700 | -0.02em | 1.18 |
| `lead` | Jakarta | 15.5 | 400 | — | 1.55 |
| `body` | Jakarta | 14.5 | 400 | — | 1.6 |
| `small` | Jakarta | 13 | 400 | — | 1.5 |
| `micro` | JetBrains Mono | 10.5 | 600 | 0.14em, UPPERCASE | 1.5 |
| `eyebrow` | JetBrains Mono | 11 | 600 | 0.16em, UPPERCASE, color=`accentDeep` | — |
| `fa` (Farsi) | Vazirmatn | (per-use) | 500 | — | 1.7, **RTL** |

> px → sp/dp: treat the px values as `.sp` for text on a 402-px-wide reference. Compose scales
> with density automatically; keep the relative scale. **Never go below ~13sp for body text.**

### Radii
| Token | dp |
|---|---|
| `xs` | 8 |
| `sm` | 12 |
| `r` (base) | 16 |
| `md` | 20 |
| `lg` | 26 |
| `xl` | 34 |
| `pill` | 999 (use `RoundedCornerShape(percent = 50)` / `CircleShape`) |

### Shadows (the soft-modern signature — needs custom work in Compose)
Compose's `Modifier.shadow()` only draws a single neutral elevation shadow and **cannot
reproduce these layered/colored shadows**. Implement a custom `Modifier.softShadow(...)` (draw
blurred offset layers into a `drawBehind`/graphicsLayer, or use Compose 1.7+ `dropShadow`). Values:

| Token | Definition |
|---|---|
| `shXs` | `0 1 2 rgb(20 17 27 / .05)` |
| `shSm` | `0 1 2 / .05` + `0 4 10 -4 / .08` |
| `sh` | `0 2 4 / .05` + `0 12 28 -10 / .16` |
| `shMd` | `0 4 8 / .06` + `0 24 48 -16 / .22` |
| `shLg` | `0 8 16 / .08` + `0 44 80 -28 / .30` |
| `shSaffron` | `0 12 28 -8 rgb(240 138 45 / .42)` |
| `shLapis` | `0 12 28 -8 rgb(36 54 161 / .40)` |
| `shMint` | `0 12 28 -8 rgb(47 168 118 / .36)` |
| `shAccent` | `0 12 28 -8 rgb(accentRgb / .42)` — colored CTA glow, follows live accent |

Format above: `offsetX offsetY blur [spread] color`. Colored shadows are used on primary CTAs
and hero cards — they're important to the look; budget time for the custom modifier.

### Motion
| Token | Value |
|---|---|
| `ease` | cubic-bezier(.22, 1, .36, 1) — standard ease-out |
| `easeBack` | cubic-bezier(.34, 1.56, .64, 1) — overshoot (active tab chip, trophy pop) |
| `dur` | 240ms default |
| Screen-in | 340ms, fade + translateY(8px→0) |
| Push-in | 340ms, fade + translateX(28px→0) |
| Page turn (reader) | 420ms, fade + translateX(22px→0) |
| Sheet up | 340ms, translateY(100%→0) |
| `:active` press | scale(0.97) on buttons, scale(0.96) chips, scale(0.92) icon buttons |
| Reveal stagger | list items fade+rise 500ms, delays 40/90/140/180/210/240ms |

Honor `prefers-reduced-motion` → on Android/iOS, respect the OS "reduce motion" setting and skip
entrance animations.

### Spacing & layout
- **Gutter:** `22dp` horizontal page padding (`--gutter`) — used everywhere; make it a theme value.
- **Reference frame:** 402×874 (iPhone). Build responsively; this is the design width.
- **Min tap target:** 44dp (icon buttons are 44×44; primary play button 58×58).
- **Tab bar bottom inset:** `safeArea.bottom + 20dp`.

---

## Dark theme
Off-black, warm-violet bias. Override these when `dark = true` (full list in `app/app.css`
`[data-theme="dark"]`):

| Token | Dark hex |
|---|---|
| `bg` | `#141019` |
| `bgTint` | `#1D1825` |
| `surface` | `#211C2B` |
| `surface2` | `#2A2435` |
| `rule` | `#322C40` |
| `ink` | `#F4F1FA` |
| `inkSoft` | `#C3BDD2` |
| `inkMuted` | `#8E87A0` |
| `inkFaint` | `#5F596F` |

In dark mode the **soft** accent backgrounds become deep tints and the **deep** text colors
become light (e.g. `lapisSoft → #1F2547`, `lapisDeep → #AEB9F4`). Shadows get stronger/blacker.
The phone screen also gets a subtle radial brand glow background. Reproduce per the CSS.

---

## Components (build these primitives FIRST, then assemble screens)

| Component | Spec |
|---|---|
| **CTAButton** (`.btn`) | pill, height 50dp (lg 56, sm 40), Jakarta 700 / 15sp, gap 8dp, icon+label centered. Variants: `accent` (accent bg, white text, `shAccent`), `ink` (ink bg, white), `lapis` (lapis bg, white, `shLapis`), `soft` (accentSoft bg, accentDeep text), `ghost` (transparent, 1.5dp `rule` inset border, inkSoft text). Press scale 0.97. |
| **Chip** (`.chip`) | pill, height 38dp, padding 0/16, 13.5sp/600, surface bg + 1dp rule. Active: ink bg, surface text. Press scale 0.96. |
| **Tag** (`.tag`) | pill, padding 4/10, 11.5sp/700. Tonal variants: `lapis/saffron/mint/lilac/rose/sun` → `{tone}Soft` bg + `{tone}Deep` text. Optional leading dot. |
| **Card** (`.card`) | surface bg, radius `lg` (26dp), `shSm`, 1dp rule, clip children. |
| **Avatar** | circle, accent/tone gradient fill, Bricolage 700 white initial. Sizes: sm 30, base 40, lg 64, xl 92. |
| **Progress** | 6dp pill track `rgb(20 17 27 / .10)` (on-dark: white /.18), accent fill. |
| **IconBox** | rounded backplate behind an icon (used in rewards/lists), tonal. |
| **Cover** (story cover) | portrait, aspect 1:1.32, default width 150dp, radius `md`. Layers: StoryArt scene → paisley motif overlay (white, 10% op) → optional NEW tag (top-left) + duration pill (top-right, translucent blur) + progress overlay (bottom). Title (h3 15.5sp) + Farsi subtitle below. |
| **StoryRow** | horizontal list item: 60×78 scene thumb (radius 14) + title/Farsi/tag+meta + chevron. |
| **Segmented** | inline pill track (bgTint or white/10% on dark), 32dp tall buttons, active = surface fill + `shXs`. Used for EN/Both/فا language toggle. |
| **TabBar** | floating bottom bar, left/right 16dp, bottom safe+20dp. Padding 7/8, radius 26dp, **translucent blurred chrome** (`color-mix surface 82%` + blur 28 saturate 180) + 1dp rule + layered shadow. 5 tabs. Active tab: accentDeep label + 46×28 pill chip behind icon (accent /.14 fill + accent /.30 inset ring, translateY -1, easeBack). Dark variant uses explicit `rgba(26,26,46,.64)` chrome. |
| **PushBar** | top bar for pushed screens: 44dp back icon-button (chevron) / centered title (h3 16sp) / optional action. Transparent+blur variant over scenes. |
| **Sheet** (bottom sheet) | scrim `rgba(15,12,20,.42)` + blur 2; sheet surface, radius 28/28/0/0, 38×5 grip, slide-up 340ms. |
| **SectionHead** | row: h3 title + optional Farsi subtitle / right-aligned action ("See all" + chevron, accentDeep). |

### Iconography
The mock uses **Lucide** icons (~20). Bake them in as Compose `ImageVector`s (convert the SVGs,
or use a Lucide-for-Compose port). Names in use: `House/home`, `Library/book`, `MoonStar/moon`,
`Trophy`, `UserRound/user`, `play`, `pause`, `prev/next` (skip), `chevL/chevR/chevD`, `bookmark`,
`volume`, `translate`, `feather`, `flame`/`flameF`, `star`, `sparkle`, `plus`, `check`, `clock`,
`compass`, `crown`, `mic`. Default stroke width 2 (2.3 when active), round caps/joins, 24×24 box.

### Story scenes (procedural artwork — no image assets)
All illustrations are **CSS gradient + clip-path compositions**, not bitmaps. Recreate each as a
small `@Composable` that draws with `Brush` backgrounds + `Canvas`/`drawPath` shapes. Scene kinds
(see `.scene-*` in `app/app.css` for exact gradients/shapes):
- `night` — lapis night gradient, saffron moon, layered mountain silhouettes, scattered stars.
- `dawn` — warm sunrise gradient + saffron sun + dark cypress silhouette.
- `vase` — lapis gradient + saffron Persian vase (clip-path) + stem/leaves.
- `hills` — dawn-pink gradient + lapis/lilac rounded hills + bird.
- `sea` — gradient sky→sea + sailboat (sail/mast/hull clip-paths).
- `flame` — lapis night + glowing saffron→rose flame shape.
- `lullaby`/`lull` — soft violet→lapis gradient, white moon, star field.

These carry the whole brand mood — get the gradients and silhouettes faithful. The exact
gradient stops and `clip-path` polygons are all in `app/app.css`.

### Persian motif overlays (tileable mask textures)
Separate from the scene illustrations, the app dusts surfaces with **single-colour Persian motif
tiles** used as repeating alpha masks (`Modifier.background(color)` clipped by a tiling vector, or
a `ShaderBrush`/tiled `Painter`). Each lives as a stroke-only SVG in `app/patterns/` (viewBox
120×120, `currentColor`, ~1.4 stroke) and is tinted + faded per use (opacity ~0.05–0.18, often
with a directional fade). Convert each to an `ImageVector` / tiled painter:
- `paisley` — boteh teardrop · `rosette` — shamseh · `star8` — khatam 8-point star · `vine` — eslimi scroll (200×100 band)
- `girih` — interlaced 8-point star strapwork (seamless) · `scallop` — Seljuk fish-scale (seamless)
- `arch` — pointed-arch arcade · `tulip` — laleh floral trellis · `cypress` — bent-sarv grove · `nightsky` — crescents & stars
Tile sizes are in `app/app.css` under `.pattern.*`. Tint with `onDark` white on dark surfaces,
`ink`/accent-deep on light ones.

### Persian / RTL specifics (important)
- All Farsi strings render in **Vazirmatn** with **RTL** layout direction. Wrap Farsi regions in
  `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`.
- Use `start/end` (not left/right) padding everywhere so the UI mirrors correctly.
- In the reader, glossary words inside the Farsi line are **tappable** — rendered lapis, 600
  weight, with a dotted lapis underline; tapping opens the WordCard sheet.
- The "karaoke" English narration colors each word: `unspoken` (inkFaint) → `active`
  (accentDeep text on accentSoft bg, radius 5dp) → `spoken` (ink).

---

## Screens / Views

Navigation = a **tab root** (5 tabs) + a **push stack** for detail/reader/etc. Onboarding gates
entry (before `started`).

1. **Onboarding** — profile picker / start. No tab bar. Sets active profile → enters app.
2. **Today (home hub)** — tab `today`. Greeting ("Salâm, {name}" + Farsi), streak strip
   (nights + words collected, tinted cards), **Continue-reading hero** (full-bleed scene, 230dp,
   gradient scrim, title + play button + page progress → pushes Reader), Tonight's-bedtime card
   (`gradNight`, → Lullaby), "New this week" horizontal shelf of Covers, **Word of the day**
   (lilac card, big Farsi word + transliteration + example), "Explore collections" shelf.
3. **Library** — tab `library`. Filter chip row (collections / age), grid or shelves of Covers,
   search. Tapping a cover → Detail.
4. **Detail** — pushed. Story hero (scene), title/Farsi, meta (collection, age, minutes, pages,
   level), blurb (EN + Farsi), character avatars, vocabulary preview, primary CTA → Reader.
5. **Reader** ⭐ *signature screen* — pushed. Top bar (down-chevron close / title / page count /
   bookmark) + page dots. Scrollable page: illustration (290dp, radius xl), EN prose with
   word-by-word karaoke highlighting, Farsi line with tappable glossary words, hint line.
   Bottom **narration dock** (surface, rounded top): scrubber (timecode / progress / timecode),
   EN·Both·فا Segmented, prev / **58dp play-pause** / next. Swipe left/right turns pages.
   Tapping a Farsi word opens **WordCard** sheet (big word, transliteration, "from {story}",
   "Hear it" + "Add to garden"). Auto-advances; last page → Finish.
6. **Finish (chapter complete)** ⭐ — pushed, dark (`gradNight`, `nightsky` motif overlay). A
   signature celebration moment: a one-shot **confetti burst** (mixed sparkles/discs falling once),
   a circular **hero seal medallion** (the story's scene art in a 142dp circle, slow-rotating dashed
   gold ring + white ring, a 50dp gold check-seal badge overlapping the bottom, radial glow behind),
   a **3-star earned** row (gold, centre star raised, pop-stagger), eyebrow `Chapter complete ·
   {collection}`, "Âfarin, you did it!" + Farsi + "You finished {title}", three reward **stat cards**
   (+1 night streak / +N new words / +20 stars), a **character cameo** card (lead character avatar +
   "{name} is proud of you {nameFa}" + an encouraging quote), a **"new words in your garden"** chip
   set (Farsi + transliteration), and `Next story` (→ next story's Detail) / `Done` (→ home) CTAs.
   All sections fade/rise in on a stagger; entrance + confetti respect reduced-motion. See
   `ScreenFinish` in `app/screen-reader.jsx`.
7. **Bedtime** — tab `bedtime`, dark. Lullaby list (night scenes), sleep-timer affordance.
8. **Lullaby (player)** — pushed, dark. Now-playing scene, title, waveform/scrubber, transport,
   sleep timer.
9. **Rewards** — tab `rewards`. Badges grid (earned vs in-progress with ring), streak, stats.
10. **Character** — pushed. Character portrait (scene + avatar), name/Farsi, role, bio, stories
    they appear in, collected state.
11. **You** — tab `you`. Profile, settings, the vocabulary "garden" (collected words + mastery),
    profile switcher, link to Parent.
12. **Parent** — pushed. Parent/settings zone (controls, time limits, etc).

Exact copy, data, and per-screen structure live in `app/data.js` and the `app/screen-*.jsx`
files — read those for the literal strings and layout.

---

## Interactions & Behavior
- **Tab switching** resets the push stack and swaps the active tab content (screen-in animation).
- **Push/pop** navigation with push-in (translateX) / reverse on back.
- **Reader narration:** a ticker advances one word every `pace` ms (slow 360 / normal 260 /
  fast 180 — a Tweak). At end of page: 650ms pause, then auto-advance; last page → push Finish.
  Play/pause toggles; pressing play at the end restarts the page. Swipe ±45px turns pages.
- **Tappable Farsi word** → WordCard bottom sheet; "Add to garden" flips to a mint confirmation.
- **Sleep timer** on lullabies (bedtime).
- **Reduced motion:** disable entrance/sheet animations.

## State Management
Small, local. Key state:
- `started: Boolean` (onboarding gate), `activeProfile`, `tab`, push `stack: List<{screen, params}>`.
- Tweaks (persisted): `accent`, `textSize` (small/medium/large → 20/22/25sp story text),
  `pace` (slow/normal/fast), `covers` (show cover badges), `theme` (light/dark).
- Reader: `pageIdx`, `playing`, `spoken` (word count), `lang` (en/both/fa), selected `word`.
- Collected words / earned badges / streak — currently static in `data.js`; back these with a
  repository + persistence (DataStore/SQLDelight) when wiring real data.

Model the content types from `data.js`: `Story`, `Character`, `Lullaby`, `Badge`, `Word`,
`Profile`, `Collection`, plus `ReaderPage { art, en, fa, glossary: [{fa, tr, en}] }`.

## Tweaks → settings
The HTML "Tweaks" panel is the **design-exploration control**, not a shipped feature — but its
options map to real in-app settings you should support: **accent color**, **story text size**,
**narration pace**, **cover badges on/off**, **dark mode**. Each is a one-value theme/state swap.

---

## Platform gaps (use `expect/actual`)
| Concern | Android | iOS |
|---|---|---|
| **Backdrop blur** (tab bar, overlays, pills over scenes) | Haze, or `RenderEffect.createBlurEffect` (API 31+) / translucent fallback | Haze, or `UIVisualEffectView` bridge / translucent fallback |
| **Fonts** | Compose Resources `Font(...)` | same (CMP resources) |
| **Audio** (narration TTS / lullaby playback) | ExoPlayer / TextToSpeech | AVFoundation |
| **Haptics** (press feedback) | `HapticFeedback` | Core Haptics |
| **Share / sleep timer** | platform APIs | platform APIs |
| **Safe-area insets** | `WindowInsets.safeDrawing` | same |

If true backdrop blur is too costly early on, ship the **translucent-solid fallback** (e.g.
`surface.copy(alpha = 0.82f)`) — the design degrades gracefully.

---

## Build order (recommended)
1. Tokens + `RivanaTheme` + `CompositionLocal` (light & dark, all 5 accents).
2. Custom `Modifier.softShadow` + gradient `Brush` helpers.
3. Type system (load the 4 fonts) + RTL Farsi text helper.
4. Primitives: CTAButton, Chip, Tag, Card, Avatar, Progress, Segmented, Sheet, IconBox, TabBar, PushBar.
5. StoryArt scene composables (procedural).
6. Cover + StoryRow + SectionHead.
7. Screens, starting with **Today** and **Reader** (the two that prove the system).
8. Navigation + state, then remaining screens.
9. Tweaks-as-settings + dark mode + reduced motion.

---

## Files in this bundle
- `Rivana Storyteller App.standalone.html` — the full working prototype, single self-contained
  file. **Open this to see and click the real design.**
- `app/app.css` — canonical token sheet (colors, type, radii, shadows, motion, component CSS,
  scene gradients). Your single most useful reference.
- `app/data.js` — content model + all literal copy (stories, characters, lullabies, badges,
  words, profiles, collections, reader pages).
- `app/*.jsx` — per-screen and shared-component source (React) for layout/behavior reference:
  `shell.jsx` (frame, TabBar, Cover, StoryRow, Segmented, PushBar), `app.jsx` (nav + tweaks),
  `screen-*.jsx`, `art.jsx` (scenes), `icons.jsx`, `tweaks-panel.jsx`.
- `tokens.md` — the original token rationale / web-codebase mapping notes (background context).

> Note: the `.jsx` files are loaded via Babel in the browser prototype. They are **reference
> only** — reimplement their structure in Compose, don't transpile them.
