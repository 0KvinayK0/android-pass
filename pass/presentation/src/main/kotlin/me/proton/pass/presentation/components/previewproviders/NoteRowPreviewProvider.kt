package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.model.ItemUiModel

class NoteRowPreviewProvider : PreviewParameterProvider<NoteRowParameter> {
    override val values: Sequence<NoteRowParameter>
        get() = sequenceOf(
            with(title = "Empty note", text = ""),
            with(title = "This is a note item", text = "the note"),
            with(
                title = "Very long text",
                text = "this is a very long note that should become " +
                    "ellipsized if the text does not fit properly"
            ),
            with(
                title = "Very long text",
                text = "this is a very long note that should " +
                    "highlight the word monkey during the rendering",
                highlight = "monkey"
            )
        )

    companion object {
        private fun with(title: String, text: String, highlight: String = ""): NoteRowParameter =
            NoteRowParameter(
                model = ItemUiModel(
                    id = ItemId("123"),
                    shareId = ShareId("345"),
                    name = title,
                    note = "Note content",
                    itemType = ItemType.Note(text = text)
                ),
                highlight = highlight
            )
    }
}

data class NoteRowParameter(
    val model: ItemUiModel,
    val highlight: String
)
