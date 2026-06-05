# Pardis MDS (Mobile KMP)

Adapted for reader. Source tokens from web neutral theme.

## Colors (Pardis only)
Use generated/PardisTokens.kt / .swift

Primary brand: Indigo #2436A1 + Saffron #F08A2D accents.
Surfaces warm cream.
Added: semantic error/success/warning, surface variants, lilac, more tints.

## Typography
Font sizes (12-30pt), weights (400-700), line heights (1.2-1.7).
Bilingual support: prefer system sans + Vazirmatn-like for Farsi where possible.

## Spacing / Radii
From web tokens (4-40dp base scale, radii up to 24+).

## Motion
Subtle, Persian-inspired but modern soft. fast=150ms, normal=250ms, slow=400ms, with easeOut and easeSpring.

## Shadows / Elevation
sm/md/lg + branded saffron/indigo shadows for depth.

## Reader Components (use tokens + new components)
- Library card: cover art + title fa/en + age/min/vocab meta. (PardisCard)
- Reader chrome: bilingual toggle, progress, transport (play/pause, rate, lang). (PardisTransport)
- Page: full-bleed illustration + bottom overlay text (fa large, en smaller).
- Vocab: tappable chips or inline highlight → bottom sheet with translit + en + audio. (PardisVocabSheet)
- Video mode: native player + synced subtitles (page cues).

Use Pardis-specific components for the reader (library cards, page reader, etc.).

Full alignment with web reader at /read/stories/[slug].

See design-system/components/ for JSON descriptions if expanded (to be added).