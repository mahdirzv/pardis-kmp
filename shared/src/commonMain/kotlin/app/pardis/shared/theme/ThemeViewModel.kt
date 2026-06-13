package app.pardis.shared.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import app.pardis.core.domain.GetThemePreferenceUseCase
import app.pardis.core.domain.SetThemePreferenceUseCase
import app.pardis.core.model.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ThemeUiState(
    val preference: ThemePreference = ThemePreference.SYSTEM,
    val isLoading: Boolean = true,
)

sealed interface ThemeAction {
    data class SetPreference(val preference: ThemePreference) : ThemeAction
}

/**
 * Owns the app-wide theme preference. Native shells observe [uiState] to resolve the effective
 * light/dark appearance; navigation/UI stays platform-owned. App-lifetime on both platforms.
 */
class ThemeViewModel(
    private val getThemePreference: GetThemePreferenceUseCase,
    private val setThemePreference: SetThemePreferenceUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(preference = getThemePreference(), isLoading = false) }
        }
    }

    fun onAction(action: ThemeAction) {
        when (action) {
            is ThemeAction.SetPreference -> {
                // Apply optimistically; persistence is best-effort (in-memory fallback already holds it).
                _uiState.update { it.copy(preference = action.preference) }
                viewModelScope.launch { runCatching { setThemePreference(action.preference) } }
            }
        }
    }

    /**
     * Cancels in-flight work, mirroring what Android's ViewModelStore does via the internal
     * `clear()`. iOS has no ViewModelStore, so the Swift adapter calls this from its `deinit`.
     */
    fun dispose() {
        viewModelScope.cancel()
    }
}
