---
name: verify-ios-without-xcode
description: Use when iOS (SwiftUI) code changed but the dev machine has no full Xcode (only Command Line Tools, no iOS SDK/simulator), so it can't be compiled locally. Verifies the build by triggering and reading the GitHub Actions macOS `ios-app` job. Also covers the false-positive SourceKit diagnostics to ignore.
---

# Verifying the iOS app without local Xcode

## When this applies

This dev Mac has **Command Line Tools only** — no `Xcode.app`, no `iphonesimulator` SDK, no
`simctl`. So SwiftUI for iOS **cannot be compiled or linked here**, and the local `swift` targets
macOS only. Confirm with:

```bash
xcode-select -p                                   # → /Library/Developer/CommandLineTools
xcrun --sdk iphonesimulator --show-sdk-path 2>&1  # → "SDK ... cannot be located"
```

Shared **Kotlin** still verifies locally (no Xcode): `./gradlew :PardisAndroidApp:compileDebugKotlin`
and `:shared:testAndroidHostTest`. Only the **SwiftUI app build** needs the macOS CI runner.

## The build already exists

`.github/workflows/kmp-build.yml` → job **`ios-app`** is the source of truth. It:
`xcodegen generate` (the `.xcodeproj` is gitignored, regenerated from `iosApp/project.yml`) →
`xcodebuild` for the simulator with `CODE_SIGNING_ALLOWED=NO`. The Kotlin `Shared.framework` is
built mid-`xcodebuild` by the `embedAndSignAppleFrameworkForXcode` pre-build script. Sibling jobs:
`android` (Linux) and `ios-framework` (Kotlin/Native link only).

## The ritual

1. **Make the change, push the branch.**
2. **Trigger the build on that branch.** It only auto-runs on `main` pushes/PRs, so either:
   - **PR (primary):** `gh pr create --draft --base main --head <branch> --title ... --body ...`
     — fires the `pull_request` jobs. Subsequent pushes to the branch auto-re-run them.
   - **On-demand:** `gh workflow run kmp-build.yml --ref <branch>` (works once the
     `workflow_dispatch:` trigger is present on `main`).
3. **Watch to completion** (cold runner ≈ 3–15 min). Background it so the turn isn't blocked:
   ```bash
   RUN=$(gh run list --branch <branch> --limit 1 --json databaseId --jq '.[0].databaseId')
   gh run watch "$RUN" --exit-status --interval 25 >/dev/null 2>&1
   gh run view "$RUN" --json jobs --jq '.jobs[] | "\(.name): \(.conclusion)"'
   gh run view "$RUN" --log-failed | tail -150     # compile errors, if any
   ```
4. **Fix loop:** read the `ios-app` failures → fix the Swift → push → CI re-runs → repeat until
   `ios-app: success`.

## Gotchas (already encoded in the workflow — don't re-break them)

- Build a **concrete arch**: `-sdk iphonesimulator -arch arm64`. A *generic* destination yields
  `ARCHS="arm64 x86_64"` + `CURRENT_ARCH=undefined_arch`, which the framework embed script can't
  resolve.
- `CODE_SIGNING_ALLOWED=NO` (no signing identity on CI).
- JDK 21 (zulu) + `gradle/actions/setup-gradle` for the Kotlin framework build/cache.
- `.xcodeproj` is **not committed** — always `xcodegen generate` in `iosApp/` first.

## SourceKit false positives to IGNORE locally

While editing iOS files with no build context, the editor surfaces diagnostics that are **not real**
— the generated token file and the Kotlin framework aren't in the loose SourceKit index, but they
resolve at build time. Do **not** chase these; trust the `ios-app` CI result instead:

- `No such module 'Shared'` (every file that `import Shared`)
- `Cannot find 'PardisColors' in scope` (it's in `design-system/generated/ios/PardisTokens.swift`)
- `Extraneous argument label 'hex:'` (the `Color(hex:)` extension lives in that same generated file)

## Visual proof via simulator screenshot

The `ios-app` job goes past compile: it boots the runner's iPhone simulator, installs + launches
the app (so a **launch-time crash fails the job**), and uploads a screenshot of the first screen as
the **`ios-screenshots`** artifact. Pull it down and actually look at it:

```bash
gh run download <run-id> -n ios-screenshots -D /tmp/ios-shots
# then Read /tmp/ios-shots/01-onboarding.png
```

This gives a real rendered frame (gradients, fonts, Persian, layout) without a local Xcode.

## Limits

A green `ios-app` proves it **compiles, links, and launches**, and the screenshot shows the **first
screen** (the onboarding gate). Capturing the other screens (You/Bedtime/Lullaby/Rewards/Character)
needs XCUITest UI automation to navigate + snapshot each — a larger, separate add-on.
