package me.proton.pass.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.pass.biometry.BiometryManager
import me.proton.android.pass.biometry.BiometryStatus
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.presentation.uievents.IsButtonEnabled
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferenceRepository,
    private val biometryManager: BiometryManager
) : ViewModel() {

    private val biometricLockState: Flow<BiometricLockState> =
        preferencesRepository.getBiometricLockState().distinctUntilChanged()
    private val themeState: Flow<ThemePreference> =
        preferencesRepository.getThemePreference().distinctUntilChanged()

    val state: StateFlow<SettingsUiState> = combine(
        biometricLockState,
        themeState
    ) { biometricLock, theme ->
        val fingerprintSection = when (biometryManager.getBiometryStatus()) {
            BiometryStatus.NotEnrolled -> FingerprintSectionState.NoFingerprintRegistered
            BiometryStatus.NotAvailable -> FingerprintSectionState.NotAvailable
            BiometryStatus.CanAuthenticate -> {
                val available = when (biometricLock) {
                    BiometricLockState.Enabled -> IsButtonEnabled.Enabled
                    BiometricLockState.Disabled -> IsButtonEnabled.Disabled
                }
                FingerprintSectionState.Available(available)
            }
        }
        SettingsUiState(
            fingerprintSection = fingerprintSection,
            themePreference = theme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState.Initial
    )

    fun onFingerPrintLockChange(state: IsButtonEnabled) = viewModelScope.launch {
        val lockState = when (state) {
            IsButtonEnabled.Enabled -> BiometricLockState.Enabled
            IsButtonEnabled.Disabled -> BiometricLockState.Disabled
        }
        PassLogger.d(TAG, "Changing BiometricLock to $lockState")
        preferencesRepository.setBiometricLockState(lockState).collect()
    }

    fun onThemePreferenceChange(theme: ThemePreference) = viewModelScope.launch {
        PassLogger.d(TAG, "Changing theme to $theme")
        preferencesRepository.setThemePreference(theme).collect()
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}