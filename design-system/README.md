# Pardis Design System (KMP Reader)

Tokenised design for the mobile reader, using the **Pardis web palette only** (from `../pardis/src/lib/design/themes/neutral.ts` and tokens/).

Use only the Pardis palette and tokens.

## Structure
- `tokens.json`: Source of truth (semantic + values).
- `MDS.md`: Motion, components, scenarios (adapted for reader: library cards, page reader chrome, video transport, vocab callouts).
- `generated/`: Platform outputs (Android Kotlin object, iOS Swift struct) — use these in native UIs.
- Edit tokens here first; regenerate or manually sync generated.

## Pardis Palette (key values)
- Background: #FAF6EE (warm cream)
- Surface: #FFFFFF
- Saffron (accent): #F08A2D / deep #C46A12 / soft #FFE9D2
- Indigo/Lapis (brand): #2436A1 / deep #1A256E / soft #E8EBFB
- Mint: #2FA876 / soft #DEF5E9
- Lilac: #ECE6FB
- Ink: #14111B / soft #4B4760 / muted #8A8499
- etc. (full in neutral.ts)

All UI code must reference tokens, never raw hex or magic dp.

See parent web design-system and tokens for full contract. This is the mobile projection.
