# Onboarding Profile Picker — Design

_Date: 2026-06-07_
_Status: Approved (pending spec review)_

## Goal

Add the v2 "Who's reading tonight?" onboarding profile-picker (Android), backed
by a real shared profile contract. The picker gates the app on first launch,
the selection persists across launches, and the active profile drives the You
profile card, the Today greeting, and a Switch-profile flow.

Design source of truth: `~/Downloads/design_handoff_rivana_kmp 2/app/screen-you.jsx`
(`ScreenOnboarding`) and `data.js` (`profiles`).

## Decisions (resolved during brainstorming)

- **Launch gate:** Gate once, then persist. App opens on onboarding until a
  profile is chosen; remembered across launches.
- **State location:** Shared profile contract in `core`/`shared` (not Android-
  local), following the existing layered KMP pattern.
- **Profile drives:** You profile card, Today greeting, and a Switch-profile
  entry from You. (Gate + persistence are the baseline.)
- **Stubs:** "Add child" and "I'm a parent" are visible but disabled no-ops
  (brief "Coming soon" snackbar). Honest placeholders.
- **Persistence mechanism:** Reuse SQLDelight (already wired, optional-driver
  fallback). No new `multiplatform-settings` dependency.

## Architecture

Follows the established layering: `core/model` → `core/domain` (interfaces +
use cases) → `core/data` (impls, SQLDelight-backed with in-memory fallback) →
`shared/<feature>` (ViewModel + Koin `Module.kt`) → consumed via `koinViewModel()`.

### core/model — `ChildProfile`

```kotlin
data class ChildProfile(
    val id: String,
    val name: String,
    val tone: ProfileTone,
    val age: Int,
    val streak: Int,
)

enum class ProfileTone { Saffron, Lapis, Lilac }
```

`ProfileTone` maps to `PardisColors` accents in the UI layer — no raw colors in
the model. Roster is the v2 demo data, defined as a static `val` in the impl
(honestly decorative, like `rivanaLullabies` / `rivanaCharacters`), but exposed
through the shared contract:

- Roya — Saffron — age 7 — streak 7
- Darius — Lapis — age 9 — streak 3
- Mina — Lilac — age 5 — streak 0

### core/domain — `ProfileRepository` + use cases

```kotlin
interface ProfileRepository {
    fun profiles(): List<ChildProfile>
    suspend fun selectedProfileId(): String?
    suspend fun setSelectedProfile(id: String)
}
```

Thin use cases mirroring `GetStoriesUseCase` / `SaveProgressUseCase`:

- `GetProfilesUseCase` → `List<ChildProfile>`
- `GetSelectedProfileUseCase` → `ChildProfile?` (resolves id against roster)
- `SelectProfileUseCase(id: String)` → persists selection

"Onboarded" is derived: `selectedProfileId() != null`. No separate flag.

### core/database — generic `app_setting` table

```sql
CREATE TABLE app_setting (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

setSetting:
INSERT OR REPLACE INTO app_setting(key, value) VALUES(?, ?);

getSetting:
SELECT value FROM app_setting WHERE key = ?;
```

Generic key-value so it serves future settings too. Stores
`selected_profile_id`.

### core/data — `ProfileRepositoryImpl`

```kotlin
class ProfileRepositoryImpl(private val db: PardisDatabase? = null) : ProfileRepository
```

- `profiles()` returns the static roster constant.
- `setSelectedProfile` / `selectedProfileId` persist via `app_setting` when a
  driver is present; fall back to an in-memory `var` when null — mirrors
  `StoryRepositoryImpl`'s optional-DB pattern.

### shared/profile — `ProfileViewModel`

One ViewModel; the gate, greeting, and You card all observe the same source.

```kotlin
data class ProfileUiState(
    val profiles: List<ChildProfile> = emptyList(),
    val selectedProfile: ChildProfile? = null,
    val isLoading: Boolean = true,
)

sealed interface ProfileAction {
    data class Select(val id: String) : ProfileAction
}
```

- `uiState: StateFlow<ProfileUiState>`
- `onAction(ProfileAction.Select)` persists then updates state.
- `ProfileModule.kt` registers `viewModel { ProfileViewModel(get(), get(), get()) }`.
- Repository + use cases registered in `core/di/CoreModules.kt` (repo gets the
  optional `SqlDriver`-built database, like `StoryRepository`).
- `PardisViewModelProvider` gains `profileViewModel()` (iOS-ready; SwiftUI UI
  deferred to the iOS-parity backlog item).

## Android UI

### PardisOnboardingScreen.kt (new file)

Ports v2 `ScreenOnboarding`:

- Light background + paisley top-fade pattern overlay (`PardisColors.lapis`,
  low alpha, top fade).
- "Rivana / ریوانا" wordmark.
- `h1` "Who's reading tonight?" + Farsi subtitle "امشب کی قصه می‌خواند؟"
  (Farsi via `PersianReaderInline`).
- 2-column grid of profile cards: tone-gradient avatar with name initial, name,
  "Age N". Tapping selects and enters the shell (or pops back when `isSwitch`).
- Dashed "Add child" stub card (disabled → "Coming soon" snackbar).
- "I'm a parent" footer link with shield icon (disabled → "Coming soon"
  snackbar).
- `isSwitch: Boolean` param: when true, shows a back chevron and selecting pops
  back to the shell instead of being a launch gate.

All visuals use `PardisColors` / `PardisGradients` / spacing-radius tokens — no
hardcoded colors or ad hoc dimensions (per `docs/kmpSkill.md` §12).

### PardisApp.kt wiring

- Resolve `ProfileViewModel` via `koinViewModel()` at the top of `PardisApp()`.
- **Gate** before the existing `NavHost`:
  - `isLoading` → blank/splash
  - `selectedProfile == null` → `PardisOnboardingScreen` (gate; no back)
  - else → existing tab shell. Selecting a profile flips state into the shell.
- Add an `"onboarding"` route rendered with `isSwitch = true`, reachable from
  the You tab's existing "Switch reader" pill (back-enabled).
- **Today greeting** (`PardisApp.kt` ~line 335): append the active name →
  "Good evening, Roya". Thread `selectedProfile` into the Today composite.
- **You profile card** (`YouProfileCard`, ~line 1026): drive name / initial /
  tone gradient / "Age N · N-night streak" from `selectedProfile` instead of
  the hardcoded "Roya"; wire "Switch reader" to the onboarding(isSwitch) route.

### Tone → color mapping

A small `when (tone)` in the UI layer maps `ProfileTone` to existing
`PardisColors` accents (saffron / lapis / lilac) and their gradients. Lives in
the Android UI only; the model stays color-free.

## Tests

Proportionate unit tests (commonTest):

- `ProfileRepositoryImpl`: select → `selectedProfileId` round-trip using the
  in-memory fallback (no driver); unknown-id resolves to `null` profile.
- `ProfileViewModel`: `Select` action reduces `selectedProfile` and clears the
  initial loading state.

## Scope guardrails (YAGNI)

Explicitly out of scope:

- "Add child" creation flow (stub only).
- Parent / Parents'-corner screen and PIN (separate backlog item; link is a
  stub).
- iOS SwiftUI onboarding screen (shared contract is provided; UI deferred to
  the iOS-parity item).
- `multiplatform-settings` or any new persistence dependency.
- Per-profile data partitioning (progress, downloads remain global for now).

## Files

New:

- `core/model/.../ChildProfile.kt`
- `core/domain/.../ProfileRepository.kt`
- `core/domain/.../GetProfilesUseCase.kt`, `GetSelectedProfileUseCase.kt`, `SelectProfileUseCase.kt`
- `core/data/.../ProfileRepositoryImpl.kt`
- `core/data/.../GetProfilesUseCaseImpl.kt`, `GetSelectedProfileUseCaseImpl.kt`, `SelectProfileUseCaseImpl.kt`
  (or grouped, following the existing Save/Get-Progress file grouping)
- `shared/.../profile/ProfileViewModel.kt`, `ProfileUiState.kt`/`ProfileAction.kt`, `ProfileModule.kt`
- `app/.../ui/PardisOnboardingScreen.kt`
- tests under `core/data` commonTest + `shared` commonTest

Modified:

- `core/database/.../Pardis.sq` (add `app_setting`)
- `core/di/.../CoreModules.kt` (register repo + use cases)
- `shared/.../SharedInit.kt` (`pardisSharedModules` — add `profileModule`)
- `shared/.../ios/PardisViewModelProvider.kt` (add `profileViewModel()`)
- `app/.../ui/PardisApp.kt` (gate, route, greeting, You card)

## Rules to keep (from prior handoff)

- No UI in `shared/`; no platform types in shared `UiState`; no navigation
  events in shared ViewModels.
- Pardis tokens only; no raw colors / ad hoc dimensions.
- Persian RTL scoped to Persian content.
