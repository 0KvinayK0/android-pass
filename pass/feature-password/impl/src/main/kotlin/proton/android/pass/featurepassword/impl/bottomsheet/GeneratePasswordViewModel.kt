package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DRAFT_PASSWORD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetMode
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetModeValue
import proton.android.pass.featurepassword.impl.GeneratePasswordSnackbarMessage
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.password.api.PasswordGenerator
import proton.android.pass.preferences.PasswordGenerationMode
import proton.android.pass.preferences.PasswordGenerationPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.WordSeparator
import javax.inject.Inject

@HiltViewModel
class GeneratePasswordViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val draftRepository: DraftRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val mode = getMode()

    private val passwordGenerationPreference = userPreferencesRepository
        .getPasswordGenerationPreference()
        .distinctUntilChanged()

    private val passwordFlow: MutableStateFlow<String> = MutableStateFlow(getInitialPassword())

    val state: StateFlow<GeneratePasswordUiState> = combine(
        passwordGenerationPreference,
        passwordFlow
    ) { pref, password ->
        GeneratePasswordUiState(
            password = password,
            mode = mode,
            content = pref.toContent()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = run {
                val pref = runBlocking { getCurrentPreference() }
                GeneratePasswordUiState(
                    password = "",
                    mode = mode,
                    content = pref.toContent()
                )
            }
        )

    fun onLengthChange(value: Int) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(randomPasswordLength = value)
        updateAndRegenerate(updated)
    }

    fun onHasSpecialCharactersChange(value: Boolean) = viewModelScope.launch {
        val updated = getCurrentPreference().copy(randomHasSpecialCharacters = value)
        updateAndRegenerate(updated)
    }

    fun regenerate() = viewModelScope.launch {
        val current = getCurrentPreference()
        passwordFlow.update { generatePassword(current) }
    }

    fun onConfirm() = viewModelScope.launch {
        when (mode) {
            GeneratePasswordMode.CancelConfirm -> storeDraft()
            GeneratePasswordMode.CopyAndClose -> copyToClipboard()
        }
    }

    private suspend fun updateAndRegenerate(pref: PasswordGenerationPreference) {
        userPreferencesRepository.setPasswordGenerationPreference(pref)
        passwordFlow.update { generatePassword(pref) }
    }

    private fun storeDraft() {
        encryptionContextProvider.withEncryptionContext {
            draftRepository.save(DRAFT_PASSWORD_KEY, encrypt(state.value.password))
        }
    }

    private fun getInitialPassword(): String {
        val pref = runBlocking { getCurrentPreference() }
        return generatePassword(pref)
    }

    private suspend fun copyToClipboard() {
        clipboardManager.copyToClipboard(state.value.password, isSecure = true)
        snackbarDispatcher(GeneratePasswordSnackbarMessage.CopiedToClipboard)
    }

    private suspend fun getCurrentPreference(): PasswordGenerationPreference =
        userPreferencesRepository.getPasswordGenerationPreference().first()


    private fun getMode(): GeneratePasswordMode {
        val mode = savedStateHandle.get<String>(GeneratePasswordBottomsheetMode.key)
            ?: throw IllegalStateException("Missing ${GeneratePasswordBottomsheetMode.key} nav argument")

        return when (GeneratePasswordBottomsheetModeValue.valueOf(mode)) {
            GeneratePasswordBottomsheetModeValue.CancelConfirm -> GeneratePasswordMode.CancelConfirm
            GeneratePasswordBottomsheetModeValue.CopyAndClose -> GeneratePasswordMode.CopyAndClose
        }
    }

    companion object {

        private fun generatePassword(preference: PasswordGenerationPreference) =
            when (preference.mode) {
                PasswordGenerationMode.Random -> generateRandomPassword(preference)
                PasswordGenerationMode.Words -> PasswordGenerator.generateWordPassword(
                    spec = preference.toWordSpec()
                )
            }

        private fun generateRandomPassword(preference: PasswordGenerationPreference) =
            PasswordGenerator.generatePassword(
                length = preference.randomPasswordLength,
                option = when {
                    preference.randomHasSpecialCharacters -> PasswordGenerator.Option.LettersNumbersSymbols
                    else -> PasswordGenerator.Option.LettersAndNumbers
                }
            )


        private fun PasswordGenerationPreference.toWordSpec(): PasswordGenerator.WordPasswordSpec {
            return PasswordGenerator.WordPasswordSpec(
                count = wordsCount,
                separator = wordsSeparator.toDomain(),
                capitalise = wordsCapitalise,
                includeNumbers = wordsIncludeNumbers
            )
        }

        private fun WordSeparator.toDomain(): PasswordGenerator.WordSeparator = when (this) {
            WordSeparator.Hyphen -> PasswordGenerator.WordSeparator.Hyphen
            WordSeparator.Space -> PasswordGenerator.WordSeparator.Space
            WordSeparator.Period -> PasswordGenerator.WordSeparator.Period
            WordSeparator.Comma -> PasswordGenerator.WordSeparator.Comma
            WordSeparator.Underscore -> PasswordGenerator.WordSeparator.Underscore
            WordSeparator.Numbers -> PasswordGenerator.WordSeparator.Numbers
            WordSeparator.NumbersAndSymbols -> PasswordGenerator.WordSeparator.NumbersAndSymbols
        }

        private fun PasswordGenerationPreference.toContent(): GeneratePasswordContent = when (mode) {
            PasswordGenerationMode.Words -> GeneratePasswordContent.WordsPassword(
                count = wordsCount,
                wordSeparator = wordsSeparator.toDomain(),
                capitalise = wordsCapitalise,
                includeNumbers = wordsIncludeNumbers
            )

            PasswordGenerationMode.Random -> GeneratePasswordContent.RandomPassword(
                length = randomPasswordLength,
                hasSpecialCharacters = randomHasSpecialCharacters
            )
        }
    }
}