package proton.android.pass.featurecreateitem.impl.alias

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.R
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalLifecycleComposeApi::class)
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateAlias(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    viewModel: UpdateAliasViewModel = hiltViewModel()
) {
    val viewState by viewModel.aliasUiState.collectAsStateWithLifecycle()
    AliasContent(
        modifier = modifier,
        uiState = viewState,
        topBarActionName = stringResource(id = R.string.action_save),
        canEdit = false,
        isUpdate = true,
        isEditAllowed = viewState.isLoadingState == IsLoadingState.NotLoading,
        onUpClick = onUpClick,
        onAliasCreated = { shareId, itemId, _ -> onSuccess(shareId, itemId) },
        onAliasDraftCreated = { _, _ -> },
        onSubmit = { viewModel.updateAlias() },
        onSuffixChange = { viewModel.onSuffixChange(it) },
        onMailboxesChanged = { viewModel.onMailboxesChanged(it) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onAliasChange = { viewModel.onAliasChange(it) },
        onVaultSelect = {}
    )
}
