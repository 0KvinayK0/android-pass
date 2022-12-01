package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.model.ItemUiModel

class LoginRowPreviewProvider : PreviewParameterProvider<LoginRowParameter> {
    override val values: Sequence<LoginRowParameter>
        get() = sequenceOf(
            with(title = "Empty username", username = ""),
            with(title = "This is a login item", username = "the username"),
            with(
                title = "Very long text",
                username = "this is a very long username that should become " +
                    "ellipsized if the text does not fit properly"
            ),
            with(
                title = "Very long text",
                username = "this is a very long username that should " +
                    "highlight the word proton during the rendering",
                note = "this is a very long note that should " +
                    "highlight the word proton during the rendering",
                websites = listOf(
                    "https://somerandomwebsite.com/",
                    "https://proton.ch/",
                    "https://proton.me/",
                    "https://anotherrandomwebsite.com/"
                ),
                highlight = "proton"
            )
        )

    companion object {
        private fun with(
            title: String,
            username: String,
            note: String = "Note content",
            websites: List<String> = emptyList(),
            highlight: String = ""
        ): LoginRowParameter =
            LoginRowParameter(
                model = ItemUiModel(
                    id = ItemId("123"),
                    shareId = ShareId("345"),
                    name = title,
                    note = note,
                    itemType = ItemType.Login(
                        username = username,
                        password = "",
                        websites = websites
                    )
                ),
                highlight = highlight
            )

    }
}

data class LoginRowParameter(
    val model: ItemUiModel,
    val highlight: String
)