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
import kotlinx.coroutines.Job
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

    private var selectJob: Job? = null

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
            is ProfileAction.Select -> {
                // Cancel any in-flight selection so rapid taps can't race to a stale result.
                selectJob?.cancel()
                selectJob = viewModelScope.launch {
                    val selected = _uiState.value.profiles.firstOrNull { it.id == action.id }
                    // Persistence is best-effort: the repo's in-memory fallback already holds the
                    // selection for this session, so apply it optimistically instead of crashing
                    // the scope if the (very unlikely) store write throws.
                    runCatching { selectProfile(action.id) }
                    _uiState.update { it.copy(selectedProfile = selected) }
                }
            }
        }
    }
}
