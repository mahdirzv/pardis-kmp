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

## Two workflows: fast gate vs. on-demand screenshots

Split intentionally so routine iteration is cheap:

- **`.github/workflows/kmp-build.yml` → `ios-app`** — the fast **compile gate (~3 min)**: `xcodegen
  generate` (the `.xcodeproj` is gitignored, regenerated from `iosApp/project.yml`) → `xcodebuild …
  build` for the simulator, no signing. The `Shared.framework` is built mid-build by the
  `embedAndSignAppleFrameworkForXcode` pre-build script. Sibling jobs: `android`, `ios-framework`.
  **This is the iteration loop** — runs on push/PR to main and `workflow_dispatch`.
- **`.github/workflows/ios-screenshots.yml`** — the **slow visual tour (~15–20 min)**: `xcodebuild
  test` runs the UI tour and uploads the `ios-screenshots` artifact. Runs **on-demand**
  (`workflow_dispatch`) and on PRs to main. Don't put this in the per-push loop — `xcodebuild test`
  roughly 7×'d the build time (3 min → 20 min) when it lived in `ios-app`.

Rule of thumb: iterate against the ~3-min compile gate; fire the screenshot tour only when you
actually need to see the screens.

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

## Visual proof: per-screen screenshots via the UI-test tour

The `ios-app` job goes past compile. It boots the runner's iPhone simulator and runs
`xcodebuild test`, which executes **`iosAppUITests/ScreenshotTests.swift`** — a tour that launches
the app, navigates each screen, and saves a screenshot attachment per screen. CI extracts the
attachments from the `.xcresult` with **xcparse** and uploads them as the **`ios-screenshots`**
artifact. Pull them down and look:

```bash
gh run download <run-id> -n ios-screenshots -D /tmp/ios-tour
ls /tmp/ios-tour      # 01-onboarding…, 02-today…, …, 08-character… — then Read each PNG
```

This gives real rendered frames (gradients, fonts, Persian, layout) without a local Xcode, and a
launch/navigation crash fails the test (visible in the step log).

**Adding a screen to the tour:** add a `snap(app, "NN-name")` in `ScreenshotTests.swift` after
navigating to it. Use stable selectors — set `.accessibilityLabel(...)` on the SwiftUI control and
match it with `app.buttons["…"]` / `app.tabBars.buttons["…"]`. The test uses `waitForExistence` +
guarded taps, so a missing element skips rather than failing the whole tour.

## Limits

Green proves it **compiles, links, launches, and the toured screens render**. Screens behind remote
data (Reader/Detail/Finish need story content loaded) may show empty/loading states in CI. Pixel
nuance still benefits from a real device, but the tour catches layout/crash regressions cheaply.
