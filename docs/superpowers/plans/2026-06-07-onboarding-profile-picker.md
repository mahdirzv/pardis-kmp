# Onboarding Profile Picker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the v2 "Who's reading tonight?" onboarding profile-picker (Android), backed by a shared `ChildProfile` contract that gates first launch, persists the pick via SQLDelight, and drives the You card / Today greeting / switch-profile flow.

**Architecture:** Standard layered KMP — `core/model` (data class) → `core/domain` (interface + use cases) → `core/data` (SQLDelight-backed impl, in-memory fallback) → `shared/profile` (ViewModel + Koin module) → Android UI consumes via `koinViewModel()`. iOS gets the shared contract; SwiftUI screen deferred.

**Tech Stack:** Kotlin Multiplatform, Koin DI, SQLDelight, Jetpack Compose (Android), kotlin-test + kotlinx-coroutines-test (commonTest).

---

## Conventions used in every task

**Android compile check:**
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :PardisAndroidApp:compileDebugKotlin --no-daemon -q
```

**Shared common-test run (host-runnable target on this Mac):**
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :shared:iosSimulatorArm64Test --no-daemon
```

Commit after each task. Co-author trailer on every commit:
```
Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>
```

---

## File structure

New files:
- `core/model/src/commonMain/kotlin/app/pardis/core/model/ChildProfile.kt` — model + `ProfileTone` enum + static roster
- `core/domain/src/commonMain/kotlin/app/pardis/core/domain/ProfileRepository.kt` — repo interface + 3 use-case interfaces
- `core/data/src/commonMain/kotlin/app/pardis/core/data/ProfileRepositoryImpl.kt` — impl + use-case impls
- `shared/src/commonMain/kotlin/app/pardis/shared/profile/ProfileViewModel.kt` — VM + `ProfileUiState` + `ProfileAction`
- `shared/src/commonMain/kotlin/app/pardis/shared/profile/ProfileModule.kt` — Koin module
- `shared/src/commonTest/kotlin/app/pardis/shared/profile/ProfileRepositoryImplTest.kt`
- `shared/src/commonTest/kotlin/app/pardis/shared/profile/ProfileViewModelTest.kt`
- `app/src/main/java/app/pardis/android/ui/PardisOnboardingScreen.kt` — Compose picker

Modified files:
- `core/database/src/commonMain/sqldelight/app/pardis/core/database/Pardis.sq` — add `app_setting`
- `core/di/src/commonMain/kotlin/app/pardis/core/di/CoreModules.kt` — register repo + use cases
- `shared/src/commonMain/kotlin/app/pardis/shared/SharedInit.kt` — add `profileModule`
- `shared/src/commonMain/kotlin/app/pardis/shared/ios/PardisViewModelProvider.kt` — add `profileViewModel()`
- `app/src/main/java/app/pardis/android/ui/PardisApp.kt` — gate, route, greeting, You card

---

## Task 1: `ChildProfile` model + static roster

**Files:**
- Create: `core/model/src/commonMain/kotlin/app/pardis/core/model/ChildProfile.kt`

- [ ] **Step 1: Write the model file**

```kotlin
package app.pardis.core.model

import kotlinx.serialization.Serializable

/**
 * A child reader profile. Demo roster for now (no backend profile system yet); exposed
 * through the shared contract so the picker, You card, and greeting share one source.
 */
@Serializable
data class ChildProfile(
    val id: String,
    val name: String,
    val tone: ProfileTone,
    val age: Int,
    val streak: Int,
)

/** Accent family for a profile. Mapped to PardisColors in the UI layer — no raw colors here. */
@Serializable
enum class ProfileTone { Saffron, Lapis, Lilac }

/** Static demo roster, mirroring the v2 design's data.js `profiles`. */
val pardisProfiles: List<ChildProfile> = listOf(
    ChildProfile(id = "roya", name = "Roya", tone = ProfileTone.Saffron, age = 7, streak = 7),
    ChildProfile(id = "darius", name = "Darius", tone = ProfileTone.Lapis, age = 9, streak = 3),
    ChildProfile(id = "mina", name = "Mina", tone = ProfileTone.Lilac, age = 5, streak = 0),
)
```

- [ ] **Step 2: Compile**

Run the Android compile check. Expected: BUILD SUCCESSFUL (model is in commonMain, picked up transitively).

- [ ] **Step 3: Commit**

```bash
git add core/model/src/commonMain/kotlin/app/pardis/core/model/ChildProfile.kt
git commit -m "feat(model): add ChildProfile + ProfileTone + demo roster

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 2: `app_setting` key-value table

**Files:**
- Modify: `core/database/src/commonMain/sqldelight/app/pardis/core/database/Pardis.sq` (append at end)

- [ ] **Step 1: Append the table + queries**

Add to the end of `Pardis.sq`:

```sql
-- Generic key-value app settings (e.g. selected_profile_id). Reusable beyond profiles.
CREATE TABLE app_setting (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

setSetting:
INSERT OR REPLACE INTO app_setting(key, value) VALUES(?, ?);

getSetting:
SELECT value FROM app_setting WHERE key = ?;
```

- [ ] **Step 2: Compile (generates SQLDelight queries)**

Run the Android compile check. Expected: BUILD SUCCESSFUL; SQLDelight generates `setSetting`/`getSetting` on `pardisQueries`.

- [ ] **Step 3: Commit**

```bash
git add core/database/src/commonMain/sqldelight/app/pardis/core/database/Pardis.sq
git commit -m "feat(db): add generic app_setting key-value table

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 3: `ProfileRepository` + use-case interfaces

**Files:**
- Create: `core/domain/src/commonMain/kotlin/app/pardis/core/domain/ProfileRepository.kt`

- [ ] **Step 1: Write the interfaces**

```kotlin
package app.pardis.core.domain

import app.pardis.core.model.ChildProfile

interface ProfileRepository {
    fun profiles(): List<ChildProfile>
    suspend fun selectedProfileId(): String?
    suspend fun setSelectedProfile(id: String)
}

/** Returns the full demo roster. */
interface GetProfilesUseCase {
    operator fun invoke(): List<ChildProfile>
}

/** Resolves the persisted selected id against the roster; null if none / unknown. */
interface GetSelectedProfileUseCase {
    suspend operator fun invoke(): ChildProfile?
}

/** Persists the selected profile id. */
interface SelectProfileUseCase {
    suspend operator fun invoke(id: String)
}
```

- [ ] **Step 2: Compile**

Run the Android compile check. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add core/domain/src/commonMain/kotlin/app/pardis/core/domain/ProfileRepository.kt
git commit -m "feat(domain): add ProfileRepository + profile use cases

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 4: `ProfileRepositoryImpl` + use-case impls (TDD)

**Files:**
- Create: `core/data/src/commonMain/kotlin/app/pardis/core/data/ProfileRepositoryImpl.kt`
- Test: `shared/src/commonTest/kotlin/app/pardis/shared/profile/ProfileRepositoryImplTest.kt`

Note: `shared` has `api(project(":core:data"))`, so the impl (public) is visible from `shared/commonTest`. We test the in-memory fallback path (db = null), which is deterministic and needs no driver.

- [ ] **Step 1: Write the failing test**

```kotlin
package app.pardis.shared.profile

import app.pardis.core.data.ProfileRepositoryImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileRepositoryImplTest {

    @Test
    fun profiles_returnsRoster() {
        val repo = ProfileRepositoryImpl(db = null)
        assertEquals(3, repo.profiles().size)
        assertEquals("roya", repo.profiles().first().id)
    }

    @Test
    fun selectedProfileId_isNull_beforeAnySelection() = runTest {
        val repo = ProfileRepositoryImpl(db = null)
        assertNull(repo.selectedProfileId())
    }

    @Test
    fun setSelectedProfile_thenSelectedProfileId_roundTrips() = runTest {
        val repo = ProfileRepositoryImpl(db = null)
        repo.setSelectedProfile("darius")
        assertEquals("darius", repo.selectedProfileId())
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :shared:iosSimulatorArm64Test --no-daemon
```
Expected: FAIL — `ProfileRepositoryImpl` is unresolved (does not exist yet).

- [ ] **Step 3: Write the implementation**

```kotlin
package app.pardis.core.data

import app.pardis.core.database.PardisDatabase
import app.pardis.core.domain.GetProfilesUseCase
import app.pardis.core.domain.GetSelectedProfileUseCase
import app.pardis.core.domain.ProfileRepository
import app.pardis.core.domain.SelectProfileUseCase
import app.pardis.core.model.ChildProfile
import app.pardis.core.model.pardisProfiles

private const val KEY_SELECTED_PROFILE = "selected_profile_id"

/**
 * Profile repository. Roster is static demo data; the selected id persists via the generic
 * app_setting table when a SQLDelight driver is present, with an in-memory fallback when not
 * (mirrors StoryRepositoryImpl's optional-db pattern).
 */
class ProfileRepositoryImpl(
    private val db: PardisDatabase? = null,
) : ProfileRepository {

    private var inMemorySelectedId: String? = null

    override fun profiles(): List<ChildProfile> = pardisProfiles

    override suspend fun selectedProfileId(): String? =
        db?.pardisQueries?.getSetting(KEY_SELECTED_PROFILE)?.executeAsOneOrNull()
            ?: inMemorySelectedId

    override suspend fun setSelectedProfile(id: String) {
        inMemorySelectedId = id
        db?.pardisQueries?.setSetting(key = KEY_SELECTED_PROFILE, value = id)
    }
}

class GetProfilesUseCaseImpl(
    private val repository: ProfileRepository,
) : GetProfilesUseCase {
    override fun invoke(): List<ChildProfile> = repository.profiles()
}

class GetSelectedProfileUseCaseImpl(
    private val repository: ProfileRepository,
) : GetSelectedProfileUseCase {
    override suspend fun invoke(): ChildProfile? {
        val id = repository.selectedProfileId() ?: return null
        return repository.profiles().firstOrNull { it.id == id }
    }
}

class SelectProfileUseCaseImpl(
    private val repository: ProfileRepository,
) : SelectProfileUseCase {
    override suspend fun invoke(id: String) = repository.setSelectedProfile(id)
}
```

- [ ] **Step 4: Run the test to verify it passes**

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :shared:iosSimulatorArm64Test --no-daemon
```
Expected: PASS (3 tests in `ProfileRepositoryImplTest`).

- [ ] **Step 5: Commit**

```bash
git add core/data/src/commonMain/kotlin/app/pardis/core/data/ProfileRepositoryImpl.kt \
        shared/src/commonTest/kotlin/app/pardis/shared/profile/ProfileRepositoryImplTest.kt
git commit -m "feat(data): ProfileRepositoryImpl + use-case impls with in-memory fallback

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 5: `ProfileViewModel` + state/action + module (TDD)

**Files:**
- Create: `shared/src/commonMain/kotlin/app/pardis/shared/profile/ProfileViewModel.kt`
- Create: `shared/src/commonMain/kotlin/app/pardis/shared/profile/ProfileModule.kt`
- Test: `shared/src/commonTest/kotlin/app/pardis/shared/profile/ProfileViewModelTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package app.pardis.shared.profile

import app.pardis.core.domain.GetProfilesUseCase
import app.pardis.core.domain.GetSelectedProfileUseCase
import app.pardis.core.domain.SelectProfileUseCase
import app.pardis.core.model.ChildProfile
import app.pardis.core.model.pardisProfiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

private class FakeProfileUseCases {
    var selectedId: String? = null

    val getProfiles = object : GetProfilesUseCase {
        override fun invoke(): List<ChildProfile> = pardisProfiles
    }
    val getSelected = object : GetSelectedProfileUseCase {
        override suspend fun invoke(): ChildProfile? =
            selectedId?.let { id -> pardisProfiles.firstOrNull { it.id == id } }
    }
    val select = object : SelectProfileUseCase {
        override suspend fun invoke(id: String) { selectedId = id }
    }
}

class ProfileViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initialLoad_noSelection_exposesProfiles_andNullSelected() = runTest {
        val fakes = FakeProfileUseCases()
        val vm = ProfileViewModel(fakes.getProfiles, fakes.getSelected, fakes.select)
        val state = vm.uiState.value
        assertEquals(3, state.profiles.size)
        assertNull(state.selectedProfile)
        assertFalse(state.isLoading)
    }

    @Test
    fun selectAction_setsSelectedProfile() = runTest {
        val fakes = FakeProfileUseCases()
        val vm = ProfileViewModel(fakes.getProfiles, fakes.getSelected, fakes.select)
        vm.onAction(ProfileAction.Select("mina"))
        assertEquals("mina", vm.uiState.value.selectedProfile?.id)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :shared:iosSimulatorArm64Test --no-daemon
```
Expected: FAIL — `ProfileViewModel` / `ProfileAction` unresolved.

- [ ] **Step 3: Write the ViewModel**

```kotlin
package app.pardis.shared.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pardis.core.domain.GetProfilesUseCase
import app.pardis.core.domain.GetSelectedProfileUseCase
import app.pardis.core.domain.SelectProfileUseCase
import app.pardis.core.model.ChildProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profiles: List<ChildProfile> = emptyList(),
    val selectedProfile: ChildProfile? = null,
    val isLoading: Boolean = true,
)

sealed interface ProfileAction {
    data class Select(val id: String) : ProfileAction
}

class ProfileViewModel(
    private val getProfiles: GetProfilesUseCase,
    private val getSelectedProfile: GetSelectedProfileUseCase,
    private val selectProfile: SelectProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    profiles = getProfiles(),
                    selectedProfile = getSelectedProfile(),
                    isLoading = false,
                )
            }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.Select -> viewModelScope.launch {
                selectProfile(action.id)
                _uiState.update { it.copy(selectedProfile = getSelectedProfile()) }
            }
        }
    }
}
```

- [ ] **Step 4: Write the Koin module**

```kotlin
package app.pardis.shared.profile

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    viewModel { ProfileViewModel(get(), get(), get()) }
}
```

- [ ] **Step 5: Run the test to verify it passes**

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :shared:iosSimulatorArm64Test --no-daemon
```
Expected: PASS (both `ProfileViewModelTest` tests + Task 4's tests).

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/app/pardis/shared/profile/ProfileViewModel.kt \
        shared/src/commonMain/kotlin/app/pardis/shared/profile/ProfileModule.kt \
        shared/src/commonTest/kotlin/app/pardis/shared/profile/ProfileViewModelTest.kt
git commit -m "feat(shared): ProfileViewModel + state/action + Koin module

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 6: DI wiring (CoreModules + SharedInit + iOS provider)

**Files:**
- Modify: `core/di/src/commonMain/kotlin/app/pardis/core/di/CoreModules.kt`
- Modify: `shared/src/commonMain/kotlin/app/pardis/shared/SharedInit.kt`
- Modify: `shared/src/commonMain/kotlin/app/pardis/shared/ios/PardisViewModelProvider.kt`

- [ ] **Step 1: Register repo + use cases in CoreModules**

Add these imports near the other `app.pardis.core.data.*` / `app.pardis.core.domain.*` imports:

```kotlin
import app.pardis.core.data.ProfileRepositoryImpl
import app.pardis.core.data.GetProfilesUseCaseImpl
import app.pardis.core.data.GetSelectedProfileUseCaseImpl
import app.pardis.core.data.SelectProfileUseCaseImpl
import app.pardis.core.domain.ProfileRepository
import app.pardis.core.domain.GetProfilesUseCase
import app.pardis.core.domain.GetSelectedProfileUseCase
import app.pardis.core.domain.SelectProfileUseCase
```

Inside the `module { ... }` block (after the `StoryRepository` `single`, so the DB-build pattern is adjacent), add:

```kotlin
        // Profile layer — selected profile persists via the shared SQLDelight DB when available.
        single<ProfileRepository> {
            val database = getOrNull<SqlDriver>()?.let(::createPardisDatabase)
            ProfileRepositoryImpl(database)
        }
        single<GetProfilesUseCase> { GetProfilesUseCaseImpl(get()) }
        single<GetSelectedProfileUseCase> { GetSelectedProfileUseCaseImpl(get()) }
        single<SelectProfileUseCase> { SelectProfileUseCaseImpl(get()) }
```

- [ ] **Step 2: Add `profileModule` to SharedInit**

Add the import:
```kotlin
import app.pardis.shared.profile.profileModule
```

Add `profileModule` to the `pardisSharedModules` list:
```kotlin
val pardisSharedModules: List<Module> = listOf(
    libraryModule,
    readerModule,
    detailModule,
    finishModule,
    analyticsModule,
    offlineModule,
    profileModule,
)
```

- [ ] **Step 3: Add `profileViewModel()` to the iOS provider**

In `PardisViewModelProvider.kt`, add the import:
```kotlin
import app.pardis.shared.profile.ProfileViewModel
```
and the accessor inside the object:
```kotlin
    fun profileViewModel(): ProfileViewModel =
        KoinPlatform.getKoin().get()
```

- [ ] **Step 4: Compile**

Run the Android compile check. Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add core/di/src/commonMain/kotlin/app/pardis/core/di/CoreModules.kt \
        shared/src/commonMain/kotlin/app/pardis/shared/SharedInit.kt \
        shared/src/commonMain/kotlin/app/pardis/shared/ios/PardisViewModelProvider.kt
git commit -m "feat(di): wire ProfileRepository, use cases, profileModule, iOS accessor

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 7: `PardisOnboardingScreen` (Android UI)

**Files:**
- Create: `app/src/main/java/app/pardis/android/ui/PardisOnboardingScreen.kt`

No unit test (Compose visual surface); verified by compile here and on-device in Task 9. There is no `Plus` icon in `PardisIconKind`, so the "Add child" affordance uses a styled `Text("+")` rather than adding a new icon asset (out of scope). The back affordance uses `PardisIconKind.Back`.

- [ ] **Step 1: Write the screen**

```kotlin
package app.pardis.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.pardis.core.model.ChildProfile
import app.pardis.core.model.ProfileTone
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import app.pardis.design.PardisSpacing

/** Maps a profile tone to its avatar gradient using design tokens (no raw colors). */
private fun toneGradient(tone: ProfileTone): Brush = when (tone) {
    ProfileTone.Saffron -> Brush.linearGradient(listOf(PardisColors.saffron, PardisColors.saffronDeep))
    ProfileTone.Lapis -> Brush.linearGradient(listOf(PardisColors.indigo, PardisColors.indigoDeep))
    ProfileTone.Lilac -> Brush.linearGradient(listOf(PardisColors.lilac, PardisColors.lilacDeep))
}

/**
 * "Who's reading tonight?" profile picker. Used both as the first-launch gate and, with
 * [isSwitch] = true, as a switch-profile screen reached from the You tab (shows a back chevron).
 */
@Composable
internal fun PardisOnboardingScreen(
    profiles: List<ChildProfile>,
    isSwitch: Boolean,
    onSelect: (ChildProfile) -> Unit,
    onBack: () -> Unit,
    onComingSoon: () -> Unit,
) {
    if (isSwitch) BackHandler { onBack() }

    Box(Modifier.fillMaxSize().background(PardisColors.background)) {
        PardisPatternOverlay(
            motif = PardisMotif.Paisley,
            color = PardisColors.indigo,
            alpha = 0.05f,
            fade = PardisPatternFade.Top,
            modifier = Modifier.fillMaxWidth().height(420.dp).align(Alignment.TopCenter),
        )

        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            if (isSwitch) {
                Box(Modifier.padding(start = PardisSpacing.lg, top = PardisSpacing.sm)) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(PardisRadius.full))
                            .background(PardisColors.surface).clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        PardisIcon(PardisIconKind.Back, contentDescription = "Back", tint = PardisColors.ink, size = 20.dp)
                    }
                }
            }

            // Wordmark + heading
            Column(
                Modifier.fillMaxWidth().padding(top = if (isSwitch) 4.dp else 36.dp, start = PardisSpacing.lg, end = PardisSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Rivana", style = MaterialTheme.typography.headlineSmall, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
                    PersianReaderInline("ریوانا", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkSoft)
                }
                Spacer(Modifier.height(22.dp))
                Text("Who's reading tonight?", style = MaterialTheme.typography.headlineMedium, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                PersianReaderInline("امشب کی قصه می‌خواند؟", style = MaterialTheme.typography.bodyLarge, color = PardisColors.inkSoft)
            }

            Spacer(Modifier.height(30.dp))

            // Profile grid + add-child stub
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = PardisSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ProfilePickCard(profile = profile, onClick = { onSelect(profile) })
                }
                item {
                    AddChildCard(onClick = onComingSoon)
                }
            }

            // "I'm a parent" footer
            Box(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = PardisSpacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    Modifier.clickable(onClick = onComingSoon).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    PardisIcon(PardisIconKind.Shield, contentDescription = null, tint = PardisColors.indigo, size = 17.dp)
                    Text("I'm a parent", style = MaterialTheme.typography.labelLarge, color = PardisColors.inkSoft, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfilePickCard(profile: ChildProfile, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.xl))
            .background(PardisColors.surface)
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.xl))
            .clickable(onClick = onClick).padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(80.dp).clip(RoundedCornerShape(PardisRadius.full)).background(toneGradient(profile.tone)),
            contentAlignment = Alignment.Center,
        ) {
            Text(profile.name.take(1), style = MaterialTheme.typography.displayLarge, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(profile.name, style = MaterialTheme.typography.titleMedium, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
            Text("AGE ${profile.age}", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkSoft)
        }
    }
}

@Composable
private fun AddChildCard(onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().heightIn(min = 168.dp).clip(RoundedCornerShape(PardisRadius.xl))
            .border(2.dp, PardisColors.border, RoundedCornerShape(PardisRadius.xl))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.backgroundAlt),
            contentAlignment = Alignment.Center,
        ) {
            Text("+", style = MaterialTheme.typography.headlineSmall, color = PardisColors.inkSoft, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Text("Add child", style = MaterialTheme.typography.labelLarge, color = PardisColors.inkSoft, fontWeight = FontWeight.Bold)
    }
}
```

- [ ] **Step 2: Token names (already verified)**

These tokens all exist in `app/src/main/java/app/pardis/design/PardisTokens.kt` and are used above:
`PardisColors.background`, `PardisColors.backgroundAlt`, `PardisColors.surface`, `PardisColors.border`, `PardisColors.ink`, `PardisColors.inkSoft`, `PardisColors.inkOnDark`, `PardisColors.indigo`, `PardisColors.indigoDeep`, `PardisColors.saffron`, `PardisColors.saffronDeep`, `PardisColors.lilac`, `PardisColors.lilacDeep`; radii `PardisRadius.xl` / `PardisRadius.full`; spacing `PardisSpacing.lg` / `.sm`. No raw colors introduced. (Sanity grep if desired: `grep -n "val background\|val inkSoft" app/src/main/java/app/pardis/design/PardisTokens.kt`.)

- [ ] **Step 3: Compile**

Run the Android compile check. Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/PardisOnboardingScreen.kt
git commit -m "feat(onboarding): Who's-reading-tonight profile picker (v2 design)

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 8: Wire onboarding into `PardisApp` (gate, route, greeting, You card)

**Files:**
- Modify: `app/src/main/java/app/pardis/android/ui/PardisApp.kt`

This task threads the active profile through the app. Read the current `PardisApp()` (~line 73), `RootShellRoute` (~line 152), `TodayScreen` (~line 213) + its greeting (~line 335), and `YouProfileCard` (~line 1026) before editing.

- [ ] **Step 1: Add the launch gate in `PardisApp()`**

At the top of `fun PardisApp()`, resolve the ViewModel and branch before the existing `NavHost`. Add imports:
```kotlin
import app.pardis.shared.profile.ProfileViewModel
import app.pardis.shared.profile.ProfileAction
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
```
(skip any already imported.)

Wrap the existing content. The current body builds a `navController` + `NavHost`. Change `fun PardisApp()` so that, before rendering the shell, it consults the profile gate:

```kotlin
@Composable
fun PardisApp(
    profileViewModel: ProfileViewModel = koinViewModel(),
) {
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

    when {
        profileState.isLoading -> {
            // brief blank gate while the persisted selection loads
            Box(Modifier.fillMaxSize().background(PardisColors.background))
        }
        profileState.selectedProfile == null -> {
            PardisOnboardingScreen(
                profiles = profileState.profiles,
                isSwitch = false,
                onSelect = { profileViewModel.onAction(ProfileAction.Select(it.id)) },
                onBack = {},
                onComingSoon = {},
            )
        }
        else -> {
            PardisAppShell(
                activeProfile = profileState.selectedProfile!!,
                profiles = profileState.profiles,
                onSelectProfile = { profileViewModel.onAction(ProfileAction.Select(it.id)) },
            )
        }
    }
}
```

Rename the existing `NavHost`-owning body into a new `private fun PardisAppShell(activeProfile: ChildProfile, profiles: List<ChildProfile>, onSelectProfile: (ChildProfile) -> Unit)` — move the current `navController` + `NavHost` code into it unchanged except for the additions in the following steps. Add import:
```kotlin
import app.pardis.core.model.ChildProfile
```

- [ ] **Step 2: Add the switch-profile route inside the shell's `NavHost`**

Inside `PardisAppShell`'s `NavHost`, add a composable route alongside the existing ones (e.g. next to `lullaby/{index}`):

```kotlin
                composable("onboarding") {
                    PardisOnboardingScreen(
                        profiles = profiles,
                        isSwitch = true,
                        onSelect = {
                            onSelectProfile(it)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() },
                        onComingSoon = {},
                    )
                }
```

- [ ] **Step 3: Thread the active profile name into the Today greeting**

`RootShellRoute` (~line 152) renders `TodayScreen`. Pass the active name down. In `PardisAppShell`, where `RootShellRoute(...)` is invoked, add an `activeName = activeProfile.name` argument; add the parameter `activeName: String` to `RootShellRoute` and forward it to `TodayScreen(activeName = activeName, ...)`. Add `activeName: String` to `TodayScreen`'s signature.

Then at the greeting (~line 335-343), change:
```kotlin
        Text("$greeting · $weekday".uppercase(), ...)
```
to greet by name:
```kotlin
        Text("$greeting, $activeName".uppercase(), style = MaterialTheme.typography.labelSmall, color = PardisColors.saffronDeep)
```
(Keep the existing style/color arguments.)

- [ ] **Step 4: Drive the You profile card + Switch-reader from the active profile**

Pass `activeProfile` and an `onSwitchProfile: () -> Unit` down to `YouScreen` → `YouProfileCard`. In `PardisAppShell`, the `PardisRootTab.You -> YouScreen(...)` branch gains `activeProfile = activeProfile` and `onSwitchProfile = { navController.navigate("onboarding") }`. Add matching params to `YouScreen` and `YouProfileCard`.

In `YouProfileCard` (~line 1026), replace the hardcoded values:
- avatar gradient: `Brush.linearGradient(listOf(PardisColors.saffron, PardisColors.saffronDeep))` → use a tone map. Add at top of the file (or reuse the one in `PardisOnboardingScreen` by making it `internal`): for this task, inline a local `when (activeProfile.tone)` returning the same token pairs as `toneGradient` in Task 7.
- initial: `Text("R", ...)` → `Text(activeProfile.name.take(1), ...)`
- name: `Text("Roya", ...)` → `Text(activeProfile.name, ...)`
- subtitle: `Text("Age 7 · 7-night streak", ...)` → `Text("Age ${activeProfile.age} · ${activeProfile.streak}-night streak", ...)`
- the "Switch reader" pill `Row(...)`: add `.clickable(onClick = onSwitchProfile)` to its `Modifier`.

To avoid duplicating `toneGradient`, change its visibility in `PardisOnboardingScreen.kt` from `private` to `internal` and call it from `YouProfileCard`.

- [ ] **Step 5: Compile**

Run the Android compile check. Expected: BUILD SUCCESSFUL. Fix any signature-threading mismatches surfaced by the compiler (the additions in steps 3-4 must match across caller and callee).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/PardisApp.kt \
        app/src/main/java/app/pardis/android/ui/PardisOnboardingScreen.kt
git commit -m "feat(onboarding): gate launch, switch-profile route, greeting + You card by profile

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 9: Full build + on-device verification

**Files:** none (verification only)

- [ ] **Step 1: Assemble the debug APK**

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :PardisAndroidApp:assembleDebug --no-daemon
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Run the shared tests once more (regression)**

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :shared:iosSimulatorArm64Test --no-daemon
```
Expected: PASS (all `ProfileRepositoryImplTest` + `ProfileViewModelTest`).

- [ ] **Step 3: Install on device**

```bash
~/Library/Android/sdk/platform-tools/adb -s RFCR11CB9JM install -r app/build/outputs/apk/debug/app-debug.apk
```
Expected: `Success`. (If the device serial differs, run `adb devices` and use the listed serial.)

- [ ] **Step 4: Manual on-device checks**

Launch the app (`adb shell am start -n app.pardis.reader/.MainActivity` or tap the icon) and verify:
  1. **First launch** shows the onboarding picker ("Who's reading tonight?") with 3 profiles + "Add child" + "I'm a parent".
  2. Tapping a profile (e.g. Darius) enters the tab shell.
  3. **Today** greeting reads "GOOD EVENING, DARIUS" (or morning/afternoon per time).
  4. **You** tab profile card shows that child's name / initial / tone color / age / streak.
  5. Tapping **Switch reader** opens the picker with a back chevron; picking a different child returns and updates Today + You.
  6. **"Add child"** and **"I'm a parent"** do nothing harmful (no-op).
  7. **Kill and relaunch** the app → it skips onboarding and opens straight to the shell with the last-picked child still active (persistence).
  8. System **back** on the switch-profile screen returns to the shell.

Take an `adb` screenshot of the picker and the Today greeting for the record. Remember: screenshot coords are device-space 720×1600.

- [ ] **Step 5: Final commit (if any verification tweaks were needed)**

Only if step 4 surfaced fixes. Otherwise the feature is complete on `main` through Task 8.

---

## Self-review notes

- **Spec coverage:** model (T1), app_setting persistence (T2), domain contract (T3), impl + fallback (T4), ViewModel/state/action/module (T5), DI + iOS accessor (T6), Android picker with stubs (T7), gate + persist + route + greeting + You card (T8), tests (T4/T5), on-device verify (T9). All spec sections mapped.
- **Persistence "gate once, then persist":** T4 persists via `app_setting`; T8's gate reads `selectedProfile` (loaded from persistence in the VM `init`) → relaunch skips onboarding. Covered by T9 step 7.
- **No raw colors:** tone map uses only `PardisColors` tokens; T7 step 2 guards token names.
- **Type consistency:** `ProfileAction.Select(id)`, `ProfileUiState{profiles, selectedProfile, isLoading}`, use-case signatures, and `ProfileRepositoryImpl(db)` are identical across T3–T8.
