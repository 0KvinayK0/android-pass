package me.proton.pass.autofill.ui.autofill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.android.pass.preferences.ThemePreference
import javax.inject.Inject

@HiltViewModel
class AutofillAppViewModel @Inject constructor(
    preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val themeState: Flow<ThemePreference> =
        preferenceRepository.getThemePreference().distinctUntilChanged()

    private val biometricLockState: Flow<BiometricLockState> =
        preferenceRepository.getBiometricLockState().distinctUntilChanged()

    val state: StateFlow<AutofillAppUiState> = combine(
        themeState, biometricLockState
    ) { theme, fingerprint ->
        AutofillAppUiState(
            theme = theme,
            isFingerprintRequired = fingerprint is BiometricLockState.Enabled
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AutofillAppUiState.Initial
        )

}