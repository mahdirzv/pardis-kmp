# Pardis MDS (Mobile KMP)

Adapted for reader. Source tokens from web neutral theme.

## Colors (Pardis only)
Use generated/PardisTokens.kt / .swift

Primary brand: Indigo #2436A1 + Saffron #F08A2D accents.
Surfaces warm cream.

## Spacing / Radii
From web tokens (e.g. 8dp base, 12, 16, 24).

## Motion
Subtle, Persian-inspired but modern soft. Use named tokens.

## Reader Components
- Library card: cover art + title fa/en + age/min/vocab meta.
- Reader chrome: bilingual toggle, progress dots or scrubber, transport (play/pause, rate, lang).
- Page: full-bleed illustration + bottom overlay text (fa large, en smaller).
- Vocab: tappable chips or inline highlight → bottom sheet with translit + en + audio.
- Video mode: native player + synced subtitles (page cues).

Use Pardis-specific components for the reader (library cards, page reader, etc.).

Full alignment with web reader at /read/stories/[slug].

See design-system/components/ for JSON descriptions if expanded.