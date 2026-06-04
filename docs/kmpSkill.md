---
name: cross-platform-app-engineering
description: Apply Kotlin Multiplatform native-UI shared-logic architecture guidance for Android Compose and iOS SwiftUI apps. Use when working on existing or new cross-platform mobile projects that should share domain, data, DI, ViewModel, UiState, Action, testing, and interop logic while keeping platform UI native.
source-of-truth: ~/.codex/skills/cross-platform-app-engineering/SKILL.md
---

> **This file is a mirror. Do not edit directly. Edit the upstream and re-mirror.**
>
> The single canonical source for the KMP architecture contract is the Codex skill at
> `~/.codex/skills/cross-platform-app-engineering/SKILL.md`. If this file goes
> out of sync with the upstream, trust the upstream, re-mirror from it, and
> continue. See the Document Rules section in `AGENTS.md` for the external-
> mirror convention.

# KMP Coding Skill — Native UI, Shared Logic

> This guide defines a reusable Kotlin Multiplatform pattern for apps that share logic but keep Android and iOS UI native.
> Shared code owns business logic and feature-scoped presentation state.
> Android owns Compose UI. iOS owns SwiftUI UI.
> Do not build shared UI layers unless a repo explicitly chooses Compose Multiplatform UI sharing.

Default baseline for new KMP work: Kotlin 2.0+ when using current SKIE, Swift 5.9+/iOS 17+ when using `@Observable`, and a modern AGP compatible with the repo toolchain. Existing repos may keep their established floor if the chosen libraries support it.

---

## 1. Purpose

Use this guide when the project architecture is:

- shared domain, data, and presentation logic in KMP
- Android UI in native Compose
- iOS UI in native SwiftUI
- no shared Compose or shared SwiftUI screen layer

Resources alone, such as strings or drawables, may be shared through Compose Multiplatform Resources when a repo chooses that, but shared resources must not become a shared UI framework layer or leak Compose resource/UI types into shared `UiState`.

If a repo already has established naming, preserve it. The examples below use generic names such as:

- `androidApp/` for the Android shell
- `iosApp/` for the iOS shell
- `shared/` for shared feature logic and bridges
- `core/*` for reusable shared layers

---

## 2. Architecture Summary

Recommended top-level split:

- `shared/` for shared feature logic, shared `ViewModel`s, shared `UiState`, `Action`, optional non-navigation one-off effects, DI assembly, and platform bridges
- `androidApp/` for Android-only Compose UI, theme, previews, navigation, and Android bootstrap
- `iosApp/` for iOS-only SwiftUI views, navigation, adapters, and iOS bootstrap
- `core/*` for reusable model, domain, data, database, network, and storage layers

Shared code is consumed by both platforms.
UI code is never shared between Android and iOS in this pattern.

---

## 3. Module Map

```text
root/
├── androidApp/                # Android shell: Activity, Application, Compose UI, theme, nav
├── iosApp/                    # iOS shell: SwiftUI app, views, navigation
├── shared/                    # Shared feature logic + shared VMs + DI bootstrap + platform bridges
└── core/
    ├── model/                 # Pure shared models
    ├── domain/                # Use cases + repository interfaces
    ├── data/                  # Repository implementations + mappers
    ├── database/              # Room entities / DAO / database (or SQLDelight)
    ├── datastore/             # Typed DataStore
    ├── network/               # Ktor clients / DTOs
    └── di/                    # Core-only DI modules
```

### Dependency direction

```text
androidApp  → shared
iosApp      → Shared.framework

shared      → core/di, core/domain, core/model
core/di     → core/data, core/domain, core/database, core/datastore, core/network
core/data   → core/domain, core/model, core/database, core/datastore, core/network
core/domain → core/model
core/*      → no platform UI modules
```

### Hard rules

- Android shell modules must not contain repositories, use cases, or business rules.
- iOS shell modules must not reimplement business logic that already exists in shared.
- `shared` must not depend on Compose UI, SwiftUI, Android `Context`, or UIKit.
- Shared presentation contracts must not hardcode platform resource identifiers, colors, dimensions, animation specs, or localized UI strings.
- `core/di` exports only core modules. Feature/shared presentation modules are assembled in `shared`.
- If a repo uses feature modules, they should be logic-only in this architecture unless the repo explicitly chooses a different rule.

---

## 4. Shared Responsibilities

Put these in `shared/src/commonMain`:

- feature-scoped `ViewModel`s
- feature `UiState` and `Action` types
- optional one-off effect types only for non-navigation side effects that do not belong in `UiState`
- feature DI modules
- platform-neutral formatting and presentation mapping needed by both UIs
- authoritative validation / resolution of external user input such as postcode, VIN, or deep-link identifiers
- shared app initialization entry points such as `SharedInit`
- iOS bridge helpers or adapters that are still platform-neutral

Shared `UiState` may expose semantic copy keys, error categories, interpolation arguments, and raw values. Platform UIs resolve those into localized Android/iOS strings.

Typical feature package shape:

```text
shared/src/commonMain/kotlin/<package>/shared/<feature>/
  FeatureAction.kt
  FeatureUiState.kt
  FeatureViewModel.kt
  FeatureModule.kt
```

This keeps feature boundaries without creating shared UI modules.

---

## 5. Platform UI Responsibilities

### Android shell

Put these in the Android shell:

- Navigation3 keys and back stack wiring
- `Route`, `Screen`, and reusable Compose components
- Material 3 theme, shapes, spacing, previews, screenshot tests
- `koinViewModel()` calls
- Android-only platform modules

Android shell code observes shared state and forwards shared actions.

### iOS shell

Put these in the iOS shell:

- SwiftUI `App`, root navigation, tabs, and screens
- `@Observable` adapters around shared `ViewModel`s on iOS 17+ targets, or `ObservableObject` adapters on pre-iOS-17 targets
- SwiftUI theme/tokens
- iOS-only previews and visual tests
- iOS-only platform modules

iOS observes shared `StateFlow`s and sends shared actions back into the shared `ViewModel`.

---

## 6. Shared ViewModel Pattern

Use the KMP lifecycle `ViewModel`:

```kotlin
class FeatureViewModel(
    private val observeSomethingUseCase: ObserveSomethingUseCase,
    private val updateSomethingUseCase: UpdateSomethingUseCase,
) : ViewModel() {

    private val phase = MutableStateFlow<FeaturePhase>(FeaturePhase.Idle)
    private val error = MutableStateFlow<String?>(null)
    val uiState: StateFlow<FeatureUiState> = combine(
        phase,
        observeSomethingUseCase(),
        error,
    ) { currentPhase, data, currentError ->
        FeatureUiState(
            phase = currentPhase,
            data = data,
            errorMessage = currentError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeatureUiState.Empty,
    )
}
```

### Rules

- Prefer `androidx.lifecycle:lifecycle-viewmodel` for AndroidX KMP `ViewModel`s unless the repo has standardized on JetBrains Compose Multiplatform lifecycle artifacts.
- `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` remains acceptable for repos aligned around Compose Multiplatform lifecycle/ViewModel integration.
- Expose exactly one public `uiState: StateFlow<T>` per screen `ViewModel`
- Do not expose `LiveData` or `MutableSharedFlow` as screen state. `StateFlow` is the durable state contract.
- Do not expose a `Channel` / `events` flow for navigation. Navigation stays platform-owned and callback-based.
- `Channel` / `receiveAsFlow()` is only for truly stateless one-off side effects (snackbar text, haptic, etc.) that do not belong in `UiState`.
- If an async workflow should move the user forward, expose an explicit success state and let the Route invoke a navigation callback from that transition.
- Do not navigate from broad render states such as generic `Loaded`.
- No Android or iOS platform types in shared `ViewModel`s
- No Compose state holders such as `mutableStateOf` in shared `ViewModel`s
- Use `viewModelScope` for shared `ViewModel` work. Background work belongs in use cases or repositories through injected dispatchers; do not scatter `withContext(Dispatchers.IO)` inside `ViewModel` bodies.
- Prefer `SharingStarted.WhileSubscribed(5_000)` unless the repo uses another established default

---

## 7. UiState / Action / Event Rules

Keep shared presentation contracts platform-neutral:

```kotlin
sealed interface FeatureAction {
    data class QueryChanged(val query: String) : FeatureAction
    data object SubmitClicked : FeatureAction
    data object RetryClicked : FeatureAction
    data object ErrorDismissed : FeatureAction
}
```

- Actions represent user intent, not widget callbacks.
- **Navigation is always callback-based, never event-based.** Pass `onNavigateToX: (Payload) -> Unit` into the Route and/or Screen.
- UI-local regex, masks, and text heuristics are only for input shaping. If a user-entered identifier must be authoritatively validated or resolved against an external source, do that in shared/core through a use case.
- For direct user navigation, call the navigation callback directly from the relevant UI interaction.
- Only derive navigation from observed state for explicit async transitions such as submit success, auth completion, or deep-link resolution.
- Do not navigate from generic content states such as `Loaded`.
- State-derived navigation must be consumed, reset, or guarded by a stable route-local consumed id so route recreation does not repeat navigation from a sticky success state.
- Do not use `Channel` or `Event` types for navigation. They add indirection with no benefit for a use-case that has a perfectly clear state representation.
- `Channel` / `Event` is reserved for truly stateless one-off effects that have no natural place in `UiState`: e.g. "show snackbar with this text", "trigger a haptic pulse".
- `UiState` should expose raw values that both platforms can render.
- Do not bake Compose-only or SwiftUI-only types into `UiState`.
- Keep screen contracts small and explicit.

### Error handling rules

Errors must stay informative until the shared `ViewModel` deliberately maps them into screen state.

- Data and domain layers may classify errors into typed domain failures, but they must not decide UI behavior.
- Repositories and use cases must not swallow exceptions, return fake empty data, or collapse distinct failures into `null`, `false`, or an empty list.
- If an error is wrapped, preserve the original cause and any safe machine-readable detail needed for debugging or retry decisions.
- Empty state is not an error state. Model empty data, validation failure, offline failure, auth failure, and unexpected failure separately when behavior differs.
- The shared `ViewModel` is the presentation boundary that decides how a real or typed error affects `UiState`, retry affordances, dismissal, and optional non-navigation effects.
- UI layers render the mapped state and forward actions. Compose and SwiftUI screens must not inspect raw exceptions or duplicate error policy.
- Unknown errors may map to a generic user-facing message, but the informative error must still be logged or preserved behind the boundary where safe.
- Do not amputate useful error information just to make a type convenient. Prefer explicit typed failures, sealed error models, or preserved causes over lossy strings.

---

## 8. DI Assembly

`core/di` owns only core modules:

```kotlin
val coreModules: List<Module> = listOf(
    databaseModule,
    dataStoreModule,
    networkModule,
    dataModule,
    domainModule,
)
```

`shared` assembles core modules plus shared feature modules:

```kotlin
const val platformContextQualifier = "platformContext"

val sharedFeatureModules: List<Module> = listOf(
    featureOneModule,
    featureTwoModule,
)

object SharedInit {
    fun init(platformModules: List<Module> = emptyList()) {
        val koinContext = KoinPlatformTools.defaultContext()
        if (koinContext.getOrNull() != null) return

        startKoin {
            modules(platformModules + coreModules + sharedFeatureModules)
        }
    }
}
```

### Platform bootstrap

Android:

```kotlin
class AndroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedInit.init(
            platformModules = listOf(
                module {
                    single<Context> { this@AndroidApplication }
                    single<Any>(named(platformContextQualifier)) { applicationContext }
                },
            ),
        )
    }
}
```

iOS:

- Start DI from the SwiftUI app startup path
- Provide any iOS-only platform modules there

### Platform dependency contracts

- Shared and core common code must not assume Android or iOS platform types directly.
- If a shared/core factory needs a platform object such as Android `Context`, pass it through an explicit qualified binding such as `platformContextQualifier`.
- Do not rely on raw `Any` resolution without a qualifier. It can compile and still fail at runtime.
- Keep platform-only DI bindings in the platform app, not in `shared`.

---

## 9. Android Compose Rules

Compose lives only in the Android shell in this pattern.

- If the Android shell owns Navigation3 `NavKey` types, those key types must be `@Serializable`.
- The Gradle module that declares those nav keys must apply the Kotlin serialization compiler plugin.
- Keep nav keys in the Android shell unless a repo has a very specific reason to share them.
- For Navigation 3 details, verify against the official Android Navigation 3 docs and the Android `navigation-3` skill recipes (no build-time dependency): https://github.com/android/skills/tree/main/navigation/navigation-3
- Prefer typed `NavKey` objects over string routes. Each saveable key should implement `NavKey` and be `@Serializable`.
- Use `rememberNavBackStack` for back stacks that should persist across configuration changes and process death.
- Use `NavDisplay` with an `entryProvider` to map keys to entries. Keep that mapping in Android navigation code, not in shared KMP logic.
- Scope Android `ViewModel`s to `NavEntry`s when the state should live only while that destination is on the back stack.
- Use separate retained back stacks for top-level tab/bottom-navigation flows when each tab needs its own history.
- Parse Android deep links into typed navigation keys and, when needed, a synthetic back stack so Up/back behavior is correct.
- Animation specs for Navigation 3 transitions must come from the Android design system or named motion tokens, not inline arbitrary durations/easings.

### Route / Screen split

```text
Route (injects shared VM)
  └── Screen (stateless)
        └── Component (stateless)
```

Rules:

- `koinViewModel()` only in Route composables
- Screen composables receive `uiState`, `onAction`, and explicit navigation callbacks when needed
- Components receive plain data and callbacks
- Previews stay in Android shell files
- Use `collectAsStateWithLifecycle()`, not `collectAsState()`, in Android Compose code observing shared `StateFlow`s.
- Keep direct user navigation in Route and Screen callbacks instead of pushing it into shared `Event`s
- If a Route must navigate after an async operation, observe an explicit transition state and invoke the callback once from there. Consume, reset, or locally guard the transition so recreated routes do not navigate again from stale success state.

Example — callback-based navigation with an optional async success transition:

```kotlin
@Composable
fun FeatureRoute(
    onNavigateToDetail: (String) -> Unit,
    viewModel: FeatureViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FeatureScreen(
        state = state,
        onAction = viewModel::onAction,
        onItemClick = onNavigateToDetail,
    )

    val createdId = (state.phase as? FeaturePhase.SubmitSucceeded)?.createdId
    var lastNavigatedCreatedId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(createdId) {
        val id = createdId ?: return@LaunchedEffect
        if (id == lastNavigatedCreatedId) return@LaunchedEffect

        lastNavigatedCreatedId = id
        onNavigateToDetail(id)
    }
}
```

---

## 10. iOS SwiftUI Rules

SwiftUI lives only in the iOS shell in this pattern.

Rules:

- SwiftUI views observe shared state; they never duplicate shared business logic.
- Bridging adapters stay thin: observe a shared `StateFlow`, republish into Swift-observable state, and forward actions back to the shared `ViewModel`.
- Navigation stays platform-native (`NavigationStack`, `NavigationPath`, etc.).
- For iOS 16+ navigation, prefer `NavigationStack` for single-column flows and `NavigationSplitView` for adaptive multi-column flows. Do not use deprecated `NavigationView` for new code.
- Prefer value-based navigation with typed `Hashable` route values and `navigationDestination(for:)`.
- Use `[Route]` for homogeneous paths and `NavigationPath` only when heterogeneous paths are genuinely needed.
- Avoid putting full domain model objects in navigation paths. Prefer small route values containing stable identifiers and payloads needed to resolve the screen.
- If iOS navigation state must be restored or deep-linked, make route values serializable where appropriate and map incoming links into platform route values.
- Keep iOS route enums/structs in the iOS shell. Shared code may validate or resolve deep-link/user input, but it must not own SwiftUI navigation paths.
- Platform formatting stays on the iOS side unless it must be shared.
- On iOS 17+ targets, prefer Swift's native `@Observable` macro over `ObservableObject` + `@Published`.
- Activate flow consumption inside `.task { ... }`, which auto-cancels on view dismiss. Do not use `onAppear { Task { ... } }`.
- `.task` cancels Swift flow collection, but it does not by itself clear the Kotlin `ViewModel`. SwiftUI adapters must be backed by a lifecycle owner, `ViewModelStoreOwner`, or approved library wrapper that clears the Kotlin `ViewModel` scope when the screen is destroyed.

Recommended flow:

```text
SwiftUI View
  → sends FeatureAction
  → shared FeatureViewModel updates uiState (+ optional non-navigation one-off effects)
  → Swift adapter exposes uiState as @Observable state
  → SwiftUI renders natively from that state
```

Modern SwiftUI adapter shape (iOS 17+, Swift 5.9+):

```swift
import Observation

@MainActor
@Observable
final class FeatureViewModelAdapter {
    private let kotlin: FeatureViewModel
    var uiState: FeatureUiState = .empty

    init(kotlin: FeatureViewModel) {
        self.kotlin = kotlin
    }

    func activate() async {
        for await state in kotlin.uiState {
            self.uiState = state
        }
    }

    func onAction(_ action: FeatureAction) {
        kotlin.onAction(action: action)
    }
}

struct FeatureScreen: View {
    @State private var vm: FeatureViewModelAdapter

    init(kotlin: FeatureViewModel) {
        _vm = State(initialValue: FeatureViewModelAdapter(kotlin: kotlin))
    }

    var body: some View {
        Content(state: vm.uiState, onAction: vm.onAction)
            .task { await vm.activate() }
    }
}
```

Notes:

- The `FeatureViewModel` passed into the adapter should come from the app's screen-scoped DI/lifecycle owner, not an unscoped direct constructor by default.
- `@Observable` replaces the older `ObservableObject` + `@Published` pattern for new iOS 17+ code.
- View holds the adapter as `@State`, not `@StateObject`, when using `@Observable`.
- `for await` over `kotlin.uiState` assumes SKIE's typed `AsyncSequence` bridge. See section 11.
- An equivalent `ObservableObject` form is acceptable on pre-iOS-17 targets; the `for await` body and `.task` lifecycle stay the same.

---

## 11. SKIE for Swift Interop

If a repo exports a Kotlin framework to Swift through Xcode framework integration, CocoaPods, or Swift Package Manager, prefer SKIE unless the repo has already standardized on another interop layer such as KMP-NativeCoroutines, plain ObjC headers, or Kotlin Swift export.

Use SKIE in this way:

- Apply `co.touchlab.skie` only in the KMP module that builds the framework consumed by Xcode or SwiftPM.
- Keep `mavenCentral()` available in both plugin repositories (`pluginManagement.repositories` in `settings.gradle.kts`) and dependency repositories.
- Pin a SKIE version compatible with the repo's Kotlin version. Bumping one without the other can break the framework build.
- Prefer SKIE's default Swift concurrency interop: consume `Flow` and `StateFlow` as typed `AsyncSequence` in Swift.
- In SwiftUI, prefer `.task` plus `for await` loops and thin `@Observable` adapters over custom bridge layers.
- Do not make SKIE preview APIs the default architecture. `Observing` and `collect` are preview features and should be opt-in only.

Minimal install shape:

```kotlin
plugins {
    id("co.touchlab.skie") version "<current-compatible-version>"
}
```

Swift consumption shape:

```swift
@MainActor
func activate() async {
    for await value in viewModel.uiState {
        self.uiState = value
    }
}
```

Optional SwiftUI preview helpers:

```kotlin
skie {
    features {
        enableSwiftUIObservingPreview = true
    }
}
```

SKIE notes:

- SKIE automatically bridges supported `Flow` types to Swift `AsyncSequence`, preserving the element type `T`.
- SKIE bridges Kotlin enums and sealed classes into Swift enum-like forms for exhaustive `switch`; use `onEnum(of:)` for sealed hierarchies.
- SKIE converts suspend functions to Swift async functions with bidirectional cancellation.
- SKIE can generate Swift-callable overloads for Kotlin default arguments only when that feature is explicitly enabled. Do not assume default-argument overloads are always present.
- Avoid force-casting SKIE flow wrapper types in Swift.
- Avoid hand-rolled Swift enums that mirror Kotlin sealed classes; use SKIE's generated shape.
- If a repo needs Combine or another stream API, bridge from SKIE's `AsyncSequence` output rather than replacing SKIE with another flow bridge by default.
- For Swift 6 strict-concurrency targets, design shared `UiState`, `Action`, and model types to be sendable-like: immutable, free of platform references, and free of shared mutable collections. Handle remaining strict-concurrency warnings at the Swift boundary with `@MainActor`, value snapshots, or carefully audited wrappers.

---

## 12. Localization / Design Tokens

Do not share Compose theme code or SwiftUI theme code through KMP.

Default rule:

- Android theme/tokens live in the Android shell
- iOS theme/tokens live in the iOS shell
- shared only carries semantic enums or model values if both platforms truly need the same primitive contract
- User-visible strings live in platform localization resources: Android string resources or an approved shared resource catalog, and iOS String Catalogs / localized strings.
- Shared code should expose semantic text identifiers, typed errors, raw values, and formatting arguments, not already-localized UI copy. Server-provided display content and test fixtures are exceptions.
- Do not hardcode one-off UI strings in Compose or SwiftUI. Add or reuse localized resources, including accessibility labels and error copy.
- Do not hardcode arbitrary UI numbers. Spacing, sizes, radii, elevation, alpha, z-index, durations, delays, easing, spring parameters, and animation specs should come from named design-system tokens.
- Do not hardcode one-off colors or gradients. Use semantic design-system color tokens. If no token exists, add the token in the platform design-system layer before use.
- Algorithmic constants in shared/core are allowed only when named, unit-bearing where useful, and explained by domain behavior rather than visual taste.

Keep these platform-local:

- Compose `ColorScheme`
- Compose `Shapes`, `Typography`, `Dp` token objects
- Compose animation specs and motion tokens
- SwiftUI `Color`, `Font`, `CGFloat` token wrappers
- SwiftUI animation specs and motion tokens

If both platforms need the same named semantic concept, share the semantic name, not the UI framework type.

---

## 13. Data / Domain Rules

These remain stable:

- domain interfaces in `core/domain`
- repository implementations in `core/data`
- Room entities, DAO, and database in `core/database` (or SQLDelight equivalent)
- DTOs in `core/network`
- typed `DataStore<T>` only in `core/datastore`
- no Room entities or network DTOs above the data layer

Use cases stay single-purpose and are called from shared `ViewModel`s.

---

## 14. Testing Rules

Put tests where the responsibility lives:

- shared `ViewModel` and use case tests in `shared/src/commonTest`
- repository, mapper, and scoring tests in `core/*/commonTest`
- Android screenshot and UI tests in the Android shell
- iOS visual and UI tests in the iOS shell

Never use Mockito or MockK in shared tests by default. Prefer fake implementations.

For shared `ViewModel` tests:

- use `kotlinx.coroutines.test`
- use Turbine for `StateFlow` and `Flow` assertions
- keep fakes deterministic

---

## 15. Review Checklist

- [ ] Shared business logic is in `shared` or `core/*`, not in platform apps
- [ ] Android Compose code exists only in the Android shell
- [ ] SwiftUI code exists only in the iOS shell
- [ ] Shared `ViewModel` exposes one `uiState`; optional `events` are reserved for non-navigation one-off effects
- [ ] Direct user navigation stays callback-based instead of flowing through shared actions or events
- [ ] State-derived navigation is only used for explicit async transition states, not generic `Loaded` rendering states, and it is consumed/reset or guarded against repeat navigation
- [ ] Android Compose observes shared `StateFlow`s with `collectAsStateWithLifecycle()`
- [ ] Repositories and use cases do not swallow errors or convert failures into fake empty states
- [ ] Known failures are typed or otherwise preserved with enough information for the `ViewModel` to make the correct presentation decision
- [ ] The shared `ViewModel` owns error-to-`UiState` mapping; platform UIs only render mapped state
- [ ] `core/di` exports only core modules
- [ ] Shared initialization assembles core modules plus shared feature modules and is safe to call repeatedly
- [ ] Every shared `ViewModel` is registered in a shared DI module and reachable from the app bootstrap path
- [ ] Shared/core platform dependencies use explicit qualified bindings instead of implicit raw `Any` resolution
- [ ] Navigation3 key types are `@Serializable`, and the module declaring them applies the Kotlin serialization plugin
- [ ] Android Navigation 3 uses typed `NavKey`s, `rememberNavBackStack` where persistence is required, `NavDisplay`/`entryProvider` mapping in Android shell code, and scoped `NavEntry` ViewModels where appropriate
- [ ] iOS navigation uses platform-native typed routes with `NavigationStack`/`NavigationSplitView`; shared code does not own SwiftUI navigation paths
- [ ] If SKIE is used: plugin only on the framework-producing module; Swift consumes flows through typed `AsyncSequence`; default-argument overloads are not assumed unless enabled
- [ ] On iOS 17+ targets, SwiftUI adapters use `@Observable`; all SwiftUI adapters activate collection from `.task` and clear Kotlin `ViewModel` lifetime through a scoped owner/wrapper
- [ ] No shared UI framework code or shared theme objects
- [ ] User-visible strings are localized through platform resources or approved shared resource catalogs, not hardcoded in UI or shared state
- [ ] Colors, spacing, typography, radii, elevations, and motion values come from design-system tokens rather than inline one-off literals
- [ ] Shared tests live in `shared/src/commonTest`
- [ ] Platform apps stay thin and primarily render shared state

This file should remain generic. Project-specific product rules belong in separate project docs, not in this skill.
