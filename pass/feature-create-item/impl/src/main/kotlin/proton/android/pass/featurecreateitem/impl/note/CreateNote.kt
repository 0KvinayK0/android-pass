package proton.android.pass.featurecreateitem.impl.note

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featurecreateitem.impl.R

@OptIn(ExperimentalLifecycleComposeApi::class)
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateNote(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CreateNoteViewModel = hiltViewModel()
) {
    val noteUiState by viewModel.noteUiState.collectAsStateWithLifecycle()

    NoteContent(
        modifier = modifier,
        uiState = noteUiState,
        topBarActionName = stringResource(R.string.title_create_note),
        canDelete = false,
        onUpClick = onUpClick,
        onSuccess = { _, _ -> onSuccess() },
        onSubmit = { shareId -> viewModel.createNote(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onDelete = {}, // We cannot delete a note that has not been created
        onVaultSelect = { viewModel.changeVault(it) }
    )
}
