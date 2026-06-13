# Pardis — Soft-Modern-App Redesign · Tokens & Codebase Mapping

How to apply the new visual system to the Next.js app without
touching component code. **Everything is token-level** — every
component reads from CSS vars, so direction A / B / C are
1-line reskins of the same code.

The current theme lives in **`src/lib/design/themes/neutral.ts`** —
a record of CSS variable names → hex/value strings, injected into
`:root` by `app/layout.tsx`. To re-skin, you change the values.

---

## 1. The new token table

These are the values used in `design-explorations.html`. All three
directions live under one brand world (**warm Persian** — lapis + saffron
+ cream); they differ only in surface, density, and one or two
type-scale values.

### Surfaces

| Var (new name)   | Current var               | A · Bento Soft | B · Stack & Hero | C · Cards Everywhere | Notes |
|---|---|---|---|---|---|
| `--c-bg`         | `--color-background`      | `#FAF6EE` | `#FFFBF2` | `#F0EEF8` | warm app background |
| `--c-bg-tinted`  | `--color-background-alt`  | `#F3EEDD` | `#F3EEDD` | `#E8E4F4` | section band |
| `--c-surface`    | `--color-surface`         | `#FFFFFF` | `#FFFFFF` | `#FFFFFF` | card base |
| `--c-surface-2`  | (new)                     | `#FDFAF0` | `#FDFAF0` | `#FDFAF0` | warm card |
| `--c-rule`       | `--color-border`          | `#ECE3D0` | `#ECE3D0` | `#DDD7EC` | hairline |

### Ink

| Var | Current | A / B / C | Notes |
|---|---|---|---|
| `--c-ink`        | `--color-ink`        | `#14111B` | near-black, warm-violet bias |
| `--c-ink-soft`   | `--color-ink-soft`   | `#4B4760` | body |
| `--c-ink-muted`  | `--color-ink-muted`  | `#8A8499` | meta |
| `--c-ink-on-dark`| `--color-ink-on-dark`| `#FFFFFF` | pure white on lapis/ink |

### Brand & accents

| Var | Current | New | Notes |
|---|---|---|---|
| `--c-lapis`        | `--color-indigo`         | `#2436A1` | Persian lapis (replaces electric `#5B5BFF`) |
| `--c-lapis-deep`   | `--color-indigo-deep`    | `#1A256E` | depth |
| `--c-lapis-soft`   | `--color-indigo-soft`    | `#E8EBFB` | wash |
| `--c-saffron`      | `--color-saffron`        | `#F08A2D` | warmer (replaces bright `#FF7A1A`) |
| `--c-saffron-deep` | `--color-saffron-deep`   | `#C46A12` | |
| `--c-saffron-soft` | `--color-saffron-soft`   | `#FFE9D2` | |
| `--c-mint`         | `--color-mint`           | `#34B57F` | tertiary, slightly less bright |
| `--c-mint-soft`    | `--color-mint-soft`      | `#DEF5E9` | |
| `--c-lilac`        | (new — replace `--color-berry`?) | `#8B6FE6` | new playful tertiary |
| `--c-lilac-soft`   | (new)                    | `#ECE6FB` | |
| `--c-sun`          | `--color-sun`            | `#F4B53A` | |
| `--c-sun-soft`     | `--color-sun-soft`       | `#FCEAB6` | |
| `--c-rose`         | `--color-berry`          | `#EC6A8A` | softer pink, less hot than `#EC4899` |
| `--c-rose-soft`    | `--color-berry-soft`     | `#FCDEE6` | |

> **The big shifts** vs your current `neutral.ts`:
> 1. Background warms from `#FFFCF7` → `#FAF6EE` (more amber, less paper-white).
> 2. Lapis darkens from electric `#5B5BFF` → Persian `#2436A1` — keeps the vibrancy but reads cultural rather than tech-blue.
> 3. Saffron deepens from `#FF7A1A` → `#F08A2D` — same energy, less neon.
> 4. Add `--c-lilac` (`#8B6FE6`) as a tertiary so the bento has 4-5 surfaces without leaning on rose.

### Gradients (new — used heavily for hero cards & CTAs)

```css
--grad-lapis-deep: linear-gradient(135deg, #2436A1 0%, #4F2EB5 50%, #1A256E 100%);
--grad-saffron:    linear-gradient(135deg, #F08A2D 0%, #F4B53A 60%, #FFD08A 100%);
--grad-dawn:       linear-gradient(135deg, #FFE9D2 0%, #ECE6FB 50%, #E8EBFB 100%);
--grad-mint:       linear-gradient(135deg, #34B57F 0%, #6AD0AB 60%, #C6F0DB 100%);
--grad-night:      linear-gradient(180deg, #1A256E 0%, #2436A1 70%, #4F2EB5 100%);
--grad-warm:       linear-gradient(135deg, #FAF6EE 0%, #FFF4E5 100%);
```

### Typography

| Var | Current | New | Notes |
|---|---|---|---|
| `--font-display` | `'Bricolage Grotesque', …`  | **unchanged** | already in the project — the soft-modern direction wants exactly this |
| `--font-body`    | `'Plus Jakarta Sans', …`    | **unchanged** | |
| `--font-mono`    | `'JetBrains Mono', …`       | **unchanged** | |
| `--font-farsi`   | `'Vazirmatn', …`            | **unchanged** | |
| `--t-display`    | `--font-size-6xl` (`4.5rem`) | `96px` (A/C) · `112px` (B) | |
| `--t-h1`         | `--font-size-5xl` (`3.5rem`) | `72px` (A/C) · `84px` (B)  | |
| `--t-h2`         | `--font-size-4xl` (`2.5rem`) | `44px` | |
| `--t-h3`         | `--font-size-3xl`            | `28px` | |

> **Display font does NOT change.** Bricolage Grotesque is already
> exactly the right typeface for soft-modern-app. Headings only need
> tighter tracking (`-0.032em`) and heavier weight (800).

### Radius — significantly bumped

| Var | Current | New |
|---|---|---|
| `--r-sm`   | `--radius-sm` (`10px`)   | `10px` |
| `--r`      | `--radius`    (`16px`)   | **`18px`** |
| `--r-md`   | (new)                    | **`24px`** |
| `--r-lg`   | `--radius-lg` (`28px`)   | **`32px`** |
| `--r-xl`   | `--radius-xl` (`36px`)   | **`44px`** |
| `--r-pill` | `--radius-full`          | `999px` (unchanged) |

> The current scale tops out at 36px; the new one tops at 44px on
> hero cards. **Buttons stay full-pill** (`999px`).

### Spacing & density

| Var | Current | A · Bento | B · Stack | C · Cards |
|---|---|---|---|---|
| `--gutter` | `px-5 md:px-8` (~32px) | `40px` | `40px` | `40px` |
| `--section-y` | py-20–24 | `72px` | `96px` | `72px` |
| `--gap`    | gap-5 / 6     | `24px` | `24px` | `24px` |

### Shadows — soft, layered, warm

```css
--sh-sm: 0 1px 2px rgb(20 17 27 / .04), 0 2px 6px rgb(20 17 27 / .04);
--sh:    0 1px 2px rgb(20 17 27 / .04), 0 8px 24px -8px rgb(20 17 27 / .10);
--sh-md: 0 2px 4px rgb(20 17 27 / .05), 0 20px 40px -16px rgb(20 17 27 / .16);
--sh-lg: 0 4px 8px rgb(20 17 27 / .06), 0 40px 80px -24px rgb(20 17 27 / .22);

/* Coloured accent shadows (used on primary CTAs / hero cards) */
--sh-lapis:   0 20px 40px -12px rgb(36 54 161 / .35);
--sh-saffron: 0 20px 40px -12px rgb(240 138 45 / .35);
--sh-mint:    0 20px 40px -12px rgb(52 181 127 / .30);
```

Keep `--shadow-saffron / -indigo / -mint` from your current theme —
just update the colour values to the new lapis/saffron hex codes.

---

## 2. Codebase plumbing — exact files to touch

### a. Replace token values in `src/lib/design/themes/neutral.ts`

```diff
- '--color-background':       '#FFFCF7',
+ '--color-background':       '#FAF6EE',

- '--color-background-alt':   '#FAF5EE',
+ '--color-background-alt':   '#F3EEDD',

- '--color-border':           '#EFE9DC',
+ '--color-border':           '#ECE3D0',

- '--color-ink':              '#0F172A',
+ '--color-ink':              '#14111B',

- '--color-ink-soft':         '#475569',
+ '--color-ink-soft':         '#4B4760',

- '--color-ink-muted':        '#94A3B8',
+ '--color-ink-muted':        '#8A8499',

- '--color-indigo':           '#5B5BFF',
+ '--color-indigo':           '#2436A1',

- '--color-indigo-deep':      '#3F3DEB',
+ '--color-indigo-deep':      '#1A256E',

- '--color-indigo-soft':      '#E0E0FF',
+ '--color-indigo-soft':      '#E8EBFB',

- '--color-saffron':          '#FF7A1A',
+ '--color-saffron':          '#F08A2D',

- '--color-saffron-deep':     '#C2410C',
+ '--color-saffron-deep':     '#C46A12',

- '--color-saffron-soft':     '#FFE4D2',
+ '--color-saffron-soft':     '#FFE9D2',

- '--color-berry':            '#EC4899',
+ '--color-berry':            '#EC6A8A',

- '--radius':                 '16px',
+ '--radius':                 '18px',

- '--radius-lg':              '28px',
+ '--radius-lg':              '32px',

- '--radius-xl':              '36px',
+ '--radius-xl':              '44px',
```

### b. Add new tokens (don't replace, append)

```ts
'--color-lilac':       '#8B6FE6',
'--color-lilac-soft':  '#ECE6FB',

'--radius-md':         '24px',          // new mid-tier

// Soft layered shadows (replacing the existing single-line ones)
'--shadow-sm':         '0 1px 2px rgb(20 17 27 / .04), 0 2px 6px rgb(20 17 27 / .04)',
'--shadow-md':         '0 1px 2px rgb(20 17 27 / .04), 0 8px 24px -8px rgb(20 17 27 / .10)',
'--shadow-lg':         '0 2px 4px rgb(20 17 27 / .05), 0 20px 40px -16px rgb(20 17 27 / .16)',
'--shadow-xl':         '0 4px 8px rgb(20 17 27 / .06), 0 40px 80px -24px rgb(20 17 27 / .22)',
'--shadow-saffron':    '0 20px 40px -12px rgb(240 138 45 / .35)',
'--shadow-indigo':     '0 20px 40px -12px rgb(36 54 161 / .35)',
'--shadow-mint':       '0 20px 40px -12px rgb(52 181 127 / .30)',

// Gradients
'--gradient-lapis':    'linear-gradient(135deg, #2436A1 0%, #4F2EB5 50%, #1A256E 100%)',
'--gradient-saffron':  'linear-gradient(135deg, #F08A2D 0%, #F4B53A 60%, #FFD08A 100%)',
'--gradient-dawn':     'linear-gradient(135deg, #FFE9D2 0%, #ECE6FB 50%, #E8EBFB 100%)',
'--gradient-night':    'linear-gradient(180deg, #1A256E 0%, #2436A1 70%, #4F2EB5 100%)',
```

### c. Replace the body background pattern

**`src/app/globals.css`** — the three coloured radial gradients still
work conceptually but use the *old* hex codes. Update them to the new
lapis/saffron values, OR drop them in favour of a single warmer wash:

```diff
  body {
    background-color: var(--color-background);
    color: var(--color-ink);
    font-family: var(--font-sans);
    …
-   background-image:
-     radial-gradient(900px circle at 0% 0%,   rgb(91 91 255 / 0.10), transparent 50%),
-     radial-gradient(700px circle at 100% 30%, rgb(255 122 26 / 0.10), transparent 50%),
-     radial-gradient(800px circle at 50% 100%, rgb(236 72 153 / 0.06), transparent 55%);
+   background-image:
+     radial-gradient(900px circle at 0% 0%,    rgb(36 54 161 / 0.08), transparent 50%),
+     radial-gradient(700px circle at 100% 30%, rgb(240 138 45 / 0.10), transparent 50%),
+     radial-gradient(800px circle at 50% 100%, rgb(139 111 230 / 0.08), transparent 55%);
    background-attachment: fixed;
  }
```

### d. Card primitive — minimal addition

The existing `<Card variant="default | glow | flat" tone="…">` already
reads from CSS vars. Add `card-dark` for the dark-gradient hero card
used on every storefront direction. In
**`src/components/ui/primitives.tsx`**, extend the `Card` variant
union with `'dark'`:

```diff
- variant?: 'default' | 'glow' | 'flat'
+ variant?: 'default' | 'glow' | 'flat' | 'dark'
```

```diff
  const variantClass =
+   variant === 'dark'
+     ? 'shadow-[var(--shadow-indigo)] border-0 text-white'
+     :
    variant === 'glow'
      ? 'shadow-[var(--shadow-lg)]'
      : variant === 'flat'
        ? 'border border-[var(--color-border)]'
        : 'shadow-[var(--shadow-md)] border border-[var(--color-border)]'
```

And in the `toneBg` map, when `variant === 'dark'`, ignore tone and
use `style={{ background: 'var(--gradient-night)' }}` instead.

### e. CTAButton — add `grad` variants

Same pattern. Add `grad` (saffron gradient) and `grad-cool` (lapis
gradient) to the variant map so storefront heroes can use them.

```diff
  type CTAVariant = 'saffron' | 'indigo' | 'ink' | 'outline' | 'ghost'
+                 | 'grad' | 'grad-cool'

  const CTA_VARIANT: Record<CTAVariant, string> = {
    …
+   grad:      'bg-[var(--gradient-saffron)] text-white shadow-[var(--shadow-saffron)] hover:brightness-110 active:scale-[0.98]',
+   grad-cool: 'bg-[var(--gradient-lapis)]   text-white shadow-[var(--shadow-indigo)]  hover:brightness-110 active:scale-[0.98]',
  }
```

### f. Floating pill nav (one new component)

**`src/components/site/site-header.tsx`** — wrap the existing inner
nav in a `nav-inner` container with the floating-pill style. The
markup is one extra div; CSS is in `globals.css`:

```css
.nav-inner {
  display: flex; align-items: center; gap: 16px;
  background: color-mix(in srgb, var(--color-surface) 75%, transparent);
  backdrop-filter: blur(12px);
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 10px 16px;
  box-shadow: var(--shadow-sm);
}
```

That's the single biggest visual change to the nav and it's pure CSS.

### g. Iconography

You currently use `lucide-react` (it's in `package.json`). Continue
using it. The 15-icon set this mock uses is all standard Lucide
names: `book-open`, `play`, `moon`, `sparkles`, `arrow-right`, `mic`,
`globe`, `heart`, `eye`, `shield`, `check`, `audio-waveform`,
`music`. Wrap them in a small `<IconBox tone="lapis|saffron|mint">`
component that draws the rounded 44×44 backplate.

---

## 3. Per-direction overrides (the DRY win)

Once `neutral.ts` holds direction A's values, B and C are 6-line
overrides. Drop two more theme files and register them:

**`src/lib/design/themes/stack.ts`** (direction B)
```ts
import { neutralTheme } from './neutral'
export const stackTheme = {
  ...neutralTheme,
  '--color-background': '#FFFBF2',
  '--font-size-6xl':    '112px',  // bigger display
  '--font-size-5xl':    '84px',
}
```

**`src/lib/design/themes/cards.ts`** (direction C)
```ts
import { neutralTheme } from './neutral'
export const cardsTheme = {
  ...neutralTheme,
  '--color-background': '#F0EEF8',
  '--color-border':     '#DDD7EC',
}
```

Register in `themes/index.ts`, add to `ThemePresetName` in
`src/config/index.ts`, and `THEME_PRESET=stack` in `.env.local`
swaps the whole site over without a single component edit.

---

## 4. Tweaks panel (next phase, when you pick one)

Once a direction is locked, the prototype ships a Tweaks panel that
writes to these vars at runtime:

| Tweak           | Vars written                                                  | Options |
|---|---|---|
| Color palette   | `--color-background`, `--color-indigo`, `--color-saffron`     | Warm · Cool · Mono · Vivid (4 curated swatches) |
| Type pairing    | `--font-display`, `--font-sans`                               | Bricolage+Jakarta (default) · GT Sectra+Söhne · Söhne+IBM Plex · Manrope+Inter |
| Radius scale    | `--radius`, `--radius-md`, `--radius-lg`                      | Sharp (8 / 12 / 16) · Default (18 / 24 / 32) · Soft (24 / 32 / 44) |
| Density         | `--gutter`, `--section-y`, `--gap`                            | Compact · Default · Spacious |
| Hero layout     | data-attr on body                                             | A / B / C |
| Dark mode       | swaps `--color-background ↔ --color-ink` + ink colours        | toggle |

Every Tweak is a one-line write to `:root`; components don't re-mount.

---

## 5. What this redesign does NOT touch

- Component public APIs (`<Card>`, `<Pill>`, `<CTAButton>`, `<Heading>` — same exports, same props)
- Auth, MCP, agent fleet, scheduler
- Routes, layouts, `proxy.ts`
- The `vivid.ts` preset (leave alone)

The migration is **pure value edits** to `neutral.ts` (one file, ~30
lines changed) + a small CSS additions block in `globals.css` (one
new utility class for the floating nav, plus the gradient/shadow
tokens above) + two ~6-line theme files for directions B and C.
No component refactor required.
