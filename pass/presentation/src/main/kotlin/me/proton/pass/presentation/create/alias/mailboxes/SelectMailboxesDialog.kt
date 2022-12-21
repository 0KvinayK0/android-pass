package me.proton.pass.presentation.create.alias.mailboxes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.presentation.create.alias.AliasMailboxUiModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SelectMailboxesDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    mailboxes: List<AliasMailboxUiModel>,
    onMailboxesChanged: (List<AliasMailboxUiModel>) -> Unit,
    onDismiss: () -> Unit,
    viewModel: SelectMailboxesDialogViewModel = hiltViewModel()
) {
    if (!show) return

    LaunchedEffect(Unit) {
        viewModel.setMailboxes(mailboxes)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SelectMailboxesDialogContent(
        modifier = modifier,
        state = uiState,
        onConfirm = {
            onMailboxesChanged(uiState.mailboxes)
        },
        onDismiss = onDismiss,
        onMailboxToggled = { viewModel.onMailboxChanged(it) }
    )
}