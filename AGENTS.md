# Pardis KMP Agent Contract

This is the KMP reader sub-project of Pardis (web at ../pardis).

**Primary references (read in order):**
1. Top-level Pardis `AGENTS.md` (../pardis/AGENTS.md) — overall architecture, Next.js rules, agent fleet, gotchas, design tokens.
2. This project's `docs/skills/pardis-kmp-delivery/SKILL.md` — KMP delivery workflow, Pardis-specific (MP4 only, public Supabase, tokenised with web palette).
3. `docs/kmpSkill.md` — canonical native-UI shared-logic architecture (Beforely/Codex pattern; do not deviate).
4. `.github/instructions/kmp.instructions.md` + `docs/code-rules.md` + `docs/severity.md` — review rules.
5. `design-system/README.md` + `MDS.md` — Pardis tokens only (saffron, indigo, mint... from web neutral.ts; no Beforely).

## Structure
Follow Beforely-style:
- core/* for models/domain/data/network/db/di
- shared/ for VMs, UiState, Actions, init, bridges
- app/ : Android native Compose shell
- iosApp/ : iOS native SwiftUI shell

See settings.gradle.kts for modules.

## Design
Tokenised with Pardis palette exclusively. See design-system/ and web src/lib/design/.

All new visuals: add token first.

## Roadmap
See docs/ROADMAP.md.

## MCP / Skills
- mcp/ for local editor integration (extend codex-cursor).
- Skills in docs/skills/.

When dispatching agents for KMP work, provide this AGENTS.md + the delivery skill.

External contributors: follow parent CONTRIBUTING if present, plus PR template.

`main` protected; PRs only.

Update this file when adding skills or major structure.