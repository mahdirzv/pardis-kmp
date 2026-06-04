---
applyTo: "shared/**,core/**,app/**,iosApp/**,design-system/**"
---

# KMP review rules (Pardis Reader)

The architecture contract is `docs/kmpSkill.md` (mirrors Codex cross-platform-app-engineering). **Read and apply it; do not duplicate rules here.** This is the review-time projection for Pardis KMP track.

Also bind to:
- `docs/skills/pardis-kmp-delivery/SKILL.md` (Pardis-specific: use only Pardis palette/tokens from web design, no Beforely anything in UI).
- `docs/code-rules.md` (cross-cutting).
- Top-level Pardis `AGENTS.md` (in sibling web project) for overall conventions.
- Parent design tokens: `../pardis/src/lib/design/` — tokenise with saffron, indigo, mint, lilac, #FAF6EE bg, etc.

## How to read during review

Walk these in order for any diff touching KMP layers or UI.

| Contract section (from kmpSkill.md) | Covers | Severity |
|-------------------------------------|--------|----------|
| §3 Module Map · Hard rules | Layering (shared/core no UI, shells thin), no platform leakage | **P0** for leakage or wrong layer |
| §4-6 Shared VM / UiState / Action | Single uiState StateFlow, onAction only, callback nav, no platform types in shared, no mutableStateOf in VM | P1 |
| §7 Error handling | No swallow, VM maps errors to UiState, UI renders mapped only | P1 |
| §8 DI | core/di core only, SharedInit + platformModules, qualified bindings | P1 (raw Any without qualifier = runtime fail) |
| §9 Android Compose | koinViewModel only in Route, collectAsStateWithLifecycle, Route/Screen split, callbacks for nav, @Serializable NavKeys if using Nav3 | P1 |
| §10 iOS SwiftUI | @Observable adapters (iOS 17+), .task + for await, apply(state), thin adapters, platform nav only | P1 |
| §11 SKIE | Plugin only on shared (framework module), compat versions, typed AsyncSequence | P0 on build break, P1 otherwise |
| §12 Design Tokens / Loc | Platform themes/tokens only (Pardis palette), semantic keys in UiState, no inline hex/dp/strings in UI | P1 |
| §13 Data/Domain | Domain in core/domain, impl in core/data, no DTOs/entities above data | P1 (P0 if leaks to shared) |
| §14 Testing | Tests in owning layer (commonTest for shared/core logic) | P1 |
| §15 Checklist | Full list | Any unchecked on substantive change = P1 |

Use `docs/severity.md` for P0/P1/P2/P3. APPROVED = zero P0/P1 outstanding.

## Pardis-specific review heuristics

- **Palette**: Must use Pardis tokens (saffron #F08A2D, indigo #2436A1, mint #34B57F, lilac, warm cream #FAF6EE, ink #14111B from web neutral.ts). No Beforely colors or "bf-" tokens. If new visual, add to design-system/ first.
- **Content fidelity**: Models match web (Story + StoryPage with paragraphs, illustrationUrl, narration* + durations, vocab). MP4 path only when videoReady (use cues from durations like web VideoReader). Fallback page audio + images.
- **Offline**: Asset manifests + SQLDelight cache for full story bundles (prose + media). Download UX in native shells.
- **Child/PIN**: Auth + RLS for family data; local secure PIN in shells.
- **No shared UI**: Even for reader chrome (transport, page dots, bilingual toggle) — implement twice, native, using shared state + tokens.
- **Supabase**: Public reads via Ktor or supabase-kt in core/network. Anon key only for reads; user features (progress, children) go through proper auth.
- **Bilingual/FA**: FA prose primary in many places; handle RTL in native UIs.

## Common traps (Kotlin → Swift, design, Pardis)

- Force-cast `as! Skie...` or raw internals — use typed for await / onEnum. P1.
- Old ObservableObject on iOS 17+ — use @Observable. P1.
- onAppear { Task } instead of .task — leaks. P1.
- Magic colors/spacing in Compose/SwiftUI — use generated PardisTokens. P1.
- Inline user strings in shared UiState or shells — semantic keys + platform resolve. P1.
- Direct VM construction in UI — must come from DI/provider with scope. P1.
- Room/SQLDelight entities or network DTOs referenced from shared — P0 layering.
- Not consuming state-derived nav with stable id guard — repeats on recompose. P1.
- SKIE version out of sync with Kotlin — build break (P0).
- Using Beforely design concepts or palette — P1 (project rule).

## Library / common forms

Valid to have small adapter files in iosApp/ and thin Route files in app/ as long as logic stays shared.

For design-system changes: also run any token lint if added (like Beforely's verifyMdsTokenRules).

Update this file + the delivery skill when the contract evolves.

See full `docs/skills/pardis-kmp-delivery/SKILL.md` for workflow, verification commands, and Pardis content specifics (MP4 cues, public Supabase, offline bundles, child gate).

`source-of-truth` for severity and cross rules lives in the docs/ files and parent Pardis AGENTS.md.