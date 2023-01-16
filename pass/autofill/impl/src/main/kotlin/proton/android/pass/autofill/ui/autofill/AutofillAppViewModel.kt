package proton.android.pass.autofill.ui.autofill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AutofillAppViewModel @Inject constructor(
    preferenceRepository: UserPreferencesRepository,
    private val biometryManager: BiometryManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    private val itemSelectedState: MutableStateFlow<AutofillItemSelectedState> =
        MutableStateFlow(AutofillItemSelectedState.Unknown)

    private val themeState: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .asResultWithoutLoading()
        .map { getThemePreference(it) }
        .distinctUntilChanged()

    private val biometricLockState: Flow<BiometricLockState> = preferenceRepository
        .getBiometricLockState()
        .asResultWithoutLoading()
        .map { getBiometricLockState(it) }
        .distinctUntilChanged()

    val state: StateFlow<AutofillAppUiState> = combine(
        themeState,
        biometricLockState,
        itemSelectedState,
        snackbarMessageRepository.snackbarMessage
    ) { theme, fingerprint, itemSelected, snackbarMessage ->
        val fingerprintRequired = when (biometryManager.getBiometryStatus()) {
            BiometryStatus.CanAuthenticate -> fingerprint is BiometricLockState.Enabled
            else -> false
        }

        AutofillAppUiState(
            theme = theme,
            isFingerprintRequired = fingerprintRequired,
            itemSelected = itemSelected,
            snackbarMessage = snackbarMessage.value()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AutofillAppUiState.Initial
        )

    fun onAutofillItemClicked(state: AutofillAppState, autofillItem: AutofillItem) = viewModelScope.launch {
        updateAutofillItemState(state, autofillItem)
    }

    fun onItemCreated(state: AutofillAppState, item: ItemUiModel) = viewModelScope.launch {
        encryptionContextProvider.withEncryptionContext {
            onAutofillItemClicked(state, item.toAutoFillItem(this@withEncryptionContext))
        }
    }

    fun onSnackbarMessageDelivered() = viewModelScope.launch {
        snackbarMessageRepository.snackbarMessageDelivered()
    }

    private fun updateAutofillItemState(state: AutofillAppState, autofillItem: AutofillItem) {
        val response = ItemFieldMapper.mapFields(
            item = autofillItem,
            androidAutofillFieldIds = state.androidAutofillIds,
            autofillTypes = state.fieldTypes
        )
        itemSelectedState.update { AutofillItemSelectedState.Selected(response) }
    }

    private fun getBiometricLockState(state: Result<BiometricLockState>): BiometricLockState =
        when (state) {
            Result.Loading -> BiometricLockState.Disabled
            is Result.Success -> state.data
            is Result.Error -> {
                val message = "Error getting BiometricLockState"
                PassLogger.w(TAG, state.exception ?: Exception(message))
                BiometricLockState.Disabled
            }
        }

    private fun getThemePreference(state: Result<ThemePreference>): ThemePreference =
        when (state) {
            Result.Loading -> ThemePreference.System
            is Result.Success -> state.data
            is Result.Error -> {
                val message = "Error getting ThemePreference"
                PassLogger.w(TAG, state.exception ?: Exception(message))
                ThemePreference.System
            }
        }

    companion object {
        private const val TAG = "AutofillAppViewModel"
    }
}