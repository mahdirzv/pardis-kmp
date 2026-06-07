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
