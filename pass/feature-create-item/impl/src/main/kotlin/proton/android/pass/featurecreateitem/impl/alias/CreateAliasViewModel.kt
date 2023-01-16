package proton.android.pass.featurecreateitem.impl.alias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asResult
import proton.android.pass.common.api.map
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.featurecreateitem.impl.alias.AliasSnackbarMessage.InitError
import proton.android.pass.featurecreateitem.impl.alias.AliasSnackbarMessage.ItemCreationError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.AliasOptions
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

@HiltViewModel
class CreateAliasViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val aliasRepository: AliasRepository,
    private val createAlias: CreateAlias,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    savedStateHandle: SavedStateHandle
) : BaseAliasViewModel(snackbarMessageRepository, savedStateHandle) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private var titleAliasInSync = true
    private var _aliasOptions: AliasOptionsUiModel? = null

    private val mutableCloseScreenEventFlow: MutableStateFlow<CloseScreenEvent> =
        MutableStateFlow(CloseScreenEvent.NotClose)
    val closeScreenEventFlow: StateFlow<CloseScreenEvent> = mutableCloseScreenEventFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CloseScreenEvent.NotClose
        )

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            if (_aliasOptions != null) return@launch
            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null && shareId is Some) {
                aliasRepository.getAliasOptions(userId, shareId.value)
                    .asResult()
                    .collect { onAliasOptions(it) }
            } else {
                PassLogger.i(TAG, "Empty User Id")
                snackbarMessageRepository.emitSnackbarMessage(InitError)
                mutableCloseScreenEventFlow.update { CloseScreenEvent.Close }
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    override fun onTitleChange(value: String) {
        aliasItemState.update { aliasItem ->
            val alias = if (titleAliasInSync) {
                AliasUtils.formatAlias(value)
            } else {
                aliasItem.alias
            }
            aliasItem.copy(
                title = value,
                alias = alias,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = alias,
                    suffix = aliasItemState.value.selectedSuffix
                )
            )
        }
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply { remove(AliasItemValidationErrors.BlankTitle) }
        }
    }

    override fun onAliasChange(value: String) {
        if (value.contains(" ") || value.contains("\n")) return
        aliasItemState.update {
            it.copy(
                alias = value,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = value,
                    suffix = aliasItemState.value.selectedSuffix
                )
            )
        }
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply {
                    remove(AliasItemValidationErrors.BlankAlias)
                    remove(AliasItemValidationErrors.InvalidAliasContent)
                }
        }
        titleAliasInSync = false
    }

    fun createAlias(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val aliasItem = aliasItemState.value
        if (aliasItem.selectedSuffix == null) return@launch

        val mailboxes = aliasItem.mailboxes.filter { it.selected }.map { it.model }
        val aliasItemValidationErrors = aliasItem.validate()
        if (aliasItemValidationErrors.isNotEmpty()) {
            aliasItemValidationErrorsState.update { aliasItemValidationErrors }
            return@launch
        }

        if (isDraft) {
            isAliasDraftSavedState.tryEmit(AliasDraftSavedState.Success(shareId, aliasItem))
        } else {
            isLoadingState.update { IsLoadingState.Loading }
            performCreateAlias(shareId, aliasItem, aliasItem.selectedSuffix, mailboxes)
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    private suspend fun performCreateAlias(
        shareId: ShareId,
        aliasItem: AliasItem,
        aliasSuffix: AliasSuffixUiModel,
        mailboxes: List<AliasMailboxUiModel>
    ) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            createAlias(
                userId = userId,
                shareId = shareId,
                newAlias = NewAlias(
                    title = aliasItem.title,
                    note = aliasItem.note,
                    prefix = aliasItem.alias,
                    suffix = aliasSuffix.toDomain(),
                    mailboxes = mailboxes.map(AliasMailboxUiModel::toDomain)
                )
            )
                .onSuccess { item ->
                    val generatedAlias =
                        getAliasToBeCreated(aliasItem.alias, aliasSuffix) ?: ""
                    isAliasSavedState.update { AliasSavedState.Success(item.id, generatedAlias) }
                }
                .onError { onCreateAliasError(it) }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
        }
    }

    private suspend fun onAliasOptions(result: Result<AliasOptions>) {
        result
            .map(::AliasOptionsUiModel)
            .onSuccess { aliasOptions ->
                _aliasOptions = aliasOptions

                val mailboxes = aliasOptions.mailboxes.mapIndexed { idx, model ->
                    SelectedAliasMailboxUiModel(model = model, selected = idx == 0)
                }
                val mailboxTitle = mailboxes.first { it.selected }.model.email

                aliasItemState.update {
                    val selectedSuffix = aliasOptions.suffixes.first()
                    val aliasToBeCreated = if (it.alias.isNotBlank()) {
                        getAliasToBeCreated(it.alias, selectedSuffix)
                    } else {
                        null
                    }
                    it.copy(
                        aliasOptions = aliasOptions,
                        selectedSuffix = selectedSuffix,
                        mailboxes = mailboxes,
                        mailboxTitle = mailboxTitle,
                        aliasToBeCreated = aliasToBeCreated
                    )
                }
                isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
            }
            .onError {
                val defaultMessage = "Could not get alias options"
                PassLogger.e(TAG, it ?: Exception(defaultMessage), defaultMessage)
                snackbarMessageRepository.emitSnackbarMessage(InitError)
                mutableCloseScreenEventFlow.update { CloseScreenEvent.Close }
            }
    }

    private suspend fun onCreateAliasError(cause: Throwable?) {
        if (cause is CannotCreateMoreAliasesError) {
            snackbarMessageRepository.emitSnackbarMessage(AliasSnackbarMessage.CannotCreateMoreAliasesError)
        } else {
            val defaultMessage = "Create alias error"
            PassLogger.e(TAG, cause ?: Exception(defaultMessage), defaultMessage)
            snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
        }
    }

    companion object {
        private const val TAG = "CreateAliasViewModel"
    }
}