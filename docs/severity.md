# Severity Scale (Pardis KMP)

Canonical for this track. Matches parent Pardis style and Beforely reference.

- **P0**: Blocks everything. Build break, data loss, security, layering violation that will cause runtime crash or total failure, major security/privacy (e.g. leaking child data), platform leakage into shared that prevents compile or correct behavior.
- **P1**: Blocks merge. Incorrect behavior, bad UX, missing contract, untested boundary, magic numbers in UI, error swallowing, no tokens, wrong DI scoping, navigation bugs, iOS VM lifetime leaks.
- **P2**: Should fix. Dead code, TODO without ticket, minor style, commented code, small duplication.
- **P3**: Nice to have. Minor polish, docs, test coverage gaps on happy path.

APPROVED PRs have zero outstanding P0 and zero P1.

Use with `docs/code-rules.md`, `docs/kmpSkill.md` §15 checklist, and `docs/skills/pardis-kmp-delivery/SKILL.md`.

For cross-project, defer to top-level Pardis AGENTS.md severity where defined.