# Pardis KMP Code Rules

Cross-cutting hard rules for the Pardis KMP reader track. Bind to `docs/kmpSkill.md`, `docs/skills/pardis-kmp-delivery/SKILL.md`, `.github/instructions/kmp.instructions.md`, and the parent Pardis web `AGENTS.md` (../pardis/AGENTS.md) for overall.

Severity from `docs/severity.md` (P0/P1 block).

## 1. No hardcoded user-facing text
User-visible strings from localization. **P1**.

- Android: strings.xml or approved shared.
- iOS: String Catalog.
- Shared: semantic copy keys in UiState only; shells resolve.
Exempt: logs, tests, design contracts.

## 2. No magic numbers
Numeric literals without name **P1** (P2 isolated test).

Acceptable:
- Design-system tokens (Pardis tokens from design-system/, generated platform files — saffron, indigo, mint, spacing from web tokens).
- Named domain consts.
- Named timeouts/retries.

Use tokens for colors, dp, durations, alpha, etc. No inline #hex, Color(0x), .dp, tween(N) in UI code.

## 3. No error swallowing
Catch that does nothing or just logs/continues is **P0** on network/persist/contract, **P1** else.

Must: re-throw, or explicit error state in VM/UiState, or structured log + mark degraded (with comment).

Empty catch(_: Exception){} always P0. Fake empty data for UI is P0.

## 4. No leftover debug
No println, Log.d, print, debugger without guard. **P1**.

## 5. No commented-out code
Delete. **P2**.

## 6. No bare TODO/FIXME
// TODO requires ticket handle (e.g. // TODO(PARDIS-KMP-123)). **P2**.
// FIXME without ticket **P1**.

## 7. No defensive theater
Don't re-validate what type system/contract guarantees. **P2** (P1 if hides bug).

Examples: null check on non-null, try/catch on non-throwing, re-parse deserialized.

Trust internals; validate at boundaries (user input, external APIs, persisted on read).

## 8. No untested producer/consumer boundary changes
DTO ↔ domain, schema ↔ store: requires round-trip test against real-shaped fixture. **P1**.

## 9. No platform leakage in shared
`shared/`, `commonMain`, `core/` must not import Android, iOS, Compose, SwiftUI. **P0**.

See kmpSkill.md §3/§6.

## 10. Design tokenisation (Pardis specific)
All visual (colors, spacing, radii, motion, typography) must come from `design-system/` Pardis tokens (adapted from web neutral.ts: saffron #F08A2D, indigo #2436A1, mint #34B57F, lilac, #FAF6EE bg, #14111B ink, etc.).

- Edit tokens first.
- Use generated PardisTokens.* in shells.
- No magic in UI.
Lint or review will catch.

## 11. Content model fidelity
Models in core/model must stay aligned with web (Story + StoryPage with paragraphs_fa/en, illustrationUrl, narration_fa/en + durations, vocab with audioUrl, couplet).

When videoReady: prefer MP4 + cues (build from durations + intro/outro like web).

Public Supabase is source for reader content.

## 12. Other Pardis web cross-rules
Follow parent AGENTS.md gotchas where applicable (e.g. prompt safety not relevant here, but data integrity, page_number for assets, auth provider runtime, etc.).

No secrets in code.

This list + the kmpSkill checklist are the binding rules for KMP track. Update when contract changes.