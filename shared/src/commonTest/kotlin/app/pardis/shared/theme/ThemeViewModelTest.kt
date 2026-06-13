package app.pardis.shared.theme

import app.pardis.core.domain.GetThemePreferenceUseCase
import app.pardis.core.domain.SetThemePreferenceUseCase
import app.pardis.core.model.ThemePreference
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

private class FakeThemeUseCases(initial: ThemePreference = ThemePreference.SYSTEM) {
    var stored: ThemePreference = initial

    val get = object : GetThemePreferenceUseCase {
        override suspend fun invoke(): ThemePreference = stored
    }
    val set = object : SetThemePreferenceUseCase {
        override suspend fun invoke(preference: ThemePreference) { stored = preference }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initialLoad_exposesStoredPreference_andClearsLoading() = runTest {
        val fakes = FakeThemeUseCases(initial = ThemePreference.DARK)
        val vm = ThemeViewModel(fakes.get, fakes.set)
        val state = vm.uiState.value
        assertEquals(ThemePreference.DARK, state.preference)
        assertFalse(state.isLoading)
    }

    @Test
    fun setPreference_updatesState_andPersists() = runTest {
        val fakes = FakeThemeUseCases(initial = ThemePreference.SYSTEM)
        val vm = ThemeViewModel(fakes.get, fakes.set)
        vm.onAction(ThemeAction.SetPreference(ThemePreference.DARK))
        assertEquals(ThemePreference.DARK, vm.uiState.value.preference)
        assertEquals(ThemePreference.DARK, fakes.stored)
    }
}
