package me.proton.android.pass.composecomponents.impl.dialogs

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.commonuimodels.api.ItemUiModel

@Composable
fun ConfirmItemDeletionDialog(
    state: ItemUiModel?,
    @StringRes title: Int,
    @StringRes message: Int,
    onDismiss: () -> Unit,
    onConfirm: (ItemUiModel) -> Unit
) {
    val item = state ?: return
    ConfirmDialog(
        title = stringResource(title),
        message = stringResource(message, item.name),
        state = state,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}