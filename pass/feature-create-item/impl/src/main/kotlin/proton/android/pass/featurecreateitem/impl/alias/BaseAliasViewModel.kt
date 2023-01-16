package proton.android.pass.featurecreateitem.impl.alias

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.pass.domain.ShareId

abstract class BaseAliasViewModel(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    protected val shareId: Option<ShareId> =
        savedStateHandle.get<String>(CommonNavArgId.ShareId.key)
            .toOption()
            .map { ShareId(it) }
    private val title: Option<String> = savedStateHandle
        .get<String>(AliasOptionalNavArgId.Title.key)
        .toOption()
    protected val isDraft: Boolean = requireNotNull(
        savedStateHandle.get<Boolean>(AliasOptionalNavArgId.IsDraft.key)
    )

    private val shareIdState: Flow<Option<ShareId>> = MutableStateFlow(shareId)

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val aliasItemState: MutableStateFlow<AliasItem> = MutableStateFlow(
        AliasItem(
            title = title.value() ?: "",
            alias = AliasUtils.formatAlias(title.value() ?: "")
        )
    )
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.Loading)
    protected val isAliasSavedState: MutableStateFlow<AliasSavedState> =
        MutableStateFlow(AliasSavedState.Unknown)
    protected val isAliasDraftSavedState: MutableStateFlow<AliasDraftSavedState> =
        MutableStateFlow(AliasDraftSavedState.Unknown)
    protected val aliasItemValidationErrorsState: MutableStateFlow<Set<AliasItemValidationErrors>> =
        MutableStateFlow(emptySet())
    protected val isApplyButtonEnabledState: MutableStateFlow<IsButtonEnabled> =
        MutableStateFlow(IsButtonEnabled.Disabled)

    private val aliasItemWrapperState = combine(
        aliasItemState,
        aliasItemValidationErrorsState
    ) { aliasItem, aliasItemValidationErrors ->
        AliasItemWrapper(aliasItem, aliasItemValidationErrors)
    }

    private data class AliasItemWrapper(
        val aliasItem: AliasItem,
        val aliasItemValidationErrors: Set<AliasItemValidationErrors>
    )

    private val aliasSavedEventWrapperState = combine(
        isAliasSavedState,
        isAliasDraftSavedState
    ) { isAliasSaved, isAliasDraftSaved ->
        AliasSavedEventWrapper(isAliasSaved, isAliasDraftSaved)
    }

    private data class AliasSavedEventWrapper(
        val isAliasSaved: AliasSavedState,
        val isAliasDraftSaved: AliasDraftSavedState
    )

    val aliasUiState: StateFlow<CreateUpdateAliasUiState> = combine(
        shareIdState,
        aliasItemWrapperState,
        isLoadingState,
        aliasSavedEventWrapperState,
        isApplyButtonEnabledState
    ) { shareId, aliasItemWrapper, isLoading, isAliasSavedEvent, isButtonEnabled ->
        CreateUpdateAliasUiState(
            shareId = shareId.value(),
            aliasItem = aliasItemWrapper.aliasItem,
            isDraft = isDraft,
            errorList = aliasItemWrapper.aliasItemValidationErrors,
            isLoadingState = isLoading,
            isAliasSavedState = isAliasSavedEvent.isAliasSaved,
            isAliasDraftSavedState = isAliasSavedEvent.isAliasDraftSaved,
            isApplyButtonEnabled = isButtonEnabled
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateAliasUiState.Initial
        )

    abstract fun onTitleChange(value: String)
    abstract fun onAliasChange(value: String)

    open fun onNoteChange(value: String) {
        aliasItemState.update { it.copy(note = value) }
    }

    fun onSuffixChange(suffix: AliasSuffixUiModel) {
        aliasItemState.update {
            it.copy(
                selectedSuffix = suffix,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = aliasItemState.value.alias,
                    suffix = suffix
                )
            )
        }
    }

    open fun onMailboxesChanged(mailboxes: List<SelectedAliasMailboxUiModel>) {
        val atLeastOneSelected = mailboxes.any { it.selected }
        if (!atLeastOneSelected) return

        aliasItemState.update {
            it.copy(
                mailboxes = mailboxes,
                mailboxTitle = getMailboxTitle(mailboxes)
            )
        }
    }

    fun onEmitSnackbarMessage(snackbarMessage: AliasSnackbarMessage) =
        viewModelScope.launch {
            snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
        }

    protected fun getMailboxTitle(mailboxes: List<SelectedAliasMailboxUiModel>): String {
        val allSelectedMailboxes = mailboxes.filter { it.selected }
        if (allSelectedMailboxes.isEmpty()) return ""
        val mailboxTitle = buildString {
            allSelectedMailboxes.forEachIndexed { idx, mailbox ->
                if (idx > 0) append(",\n")
                append(mailbox.model.email)
            }
        }

        return mailboxTitle
    }

    protected fun getAliasToBeCreated(alias: String, suffix: AliasSuffixUiModel?): String? {
        if (suffix != null && alias.isNotBlank()) {
            return "$alias${suffix.suffix}"
        }
        return null
    }
}