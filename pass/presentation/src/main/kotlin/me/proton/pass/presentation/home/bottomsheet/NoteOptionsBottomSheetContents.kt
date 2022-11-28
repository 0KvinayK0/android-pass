package me.proton.pass.presentation.home.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemIcon
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemList
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemRow
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemSubtitle
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemTitle
import me.proton.pass.presentation.components.common.item.icon.NoteIcon
import me.proton.pass.presentation.components.model.ItemUiModel

@ExperimentalMaterialApi
@Composable
fun NoteOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel?
) {
    Column(modifier) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = itemUiModel?.name ?: "") },
            subtitle = {
                val processedText = (itemUiModel?.itemType as? ItemType.Note)
                    ?.text
                    ?.replace("\n", " ")
                    ?: ""
                BottomSheetItemSubtitle(
                    text = processedText
                )
            },
            icon = { NoteIcon() }
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItemList(
            items = listOf(
                copyNote(),
                edit(),
                moveToTrash()
            )
        )
    }
}

private fun copyNote(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_note)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
    override val onClick: () -> Unit
        get() = { }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun NoteOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            NoteOptionsBottomSheetContents(Modifier, null)
        }
    }
}
