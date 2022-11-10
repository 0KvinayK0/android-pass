package me.proton.pass.presentation.components.common.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.presentation.R
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.model.ItemUiModel

@Composable
internal fun LoginRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    itemType: ItemType.Login,
    highlight: Option<String>
) {
    ItemRow(
        icon = R.drawable.ic_proton_key,
        title = item.name.highlight(highlight),
        subtitle = itemType.username.highlight(highlight),
        modifier = modifier
    )
}