package app.pardis.profile

import app.pardis.core.domain.GetProfilesUseCase
import app.pardis.core.domain.GetSelectedProfileUseCase
import app.pardis.core.domain.SelectProfileUseCase
import app.pardis.core.model.ChildProfile
import app.pardis.core.model.pardisProfiles
import app.pardis.shared.profile.ProfileAction
import app.pardis.shared.profile.ProfileViewModel
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
        assertEquals(pardisProfiles.size, state.profiles.size)
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
