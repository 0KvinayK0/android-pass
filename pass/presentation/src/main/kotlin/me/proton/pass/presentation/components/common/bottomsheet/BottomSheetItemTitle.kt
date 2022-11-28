package me.proton.pass.presentation.components.common.bottomsheet

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default

@Composable
fun BottomSheetItemTitle(
    modifier: Modifier = Modifier,
    text: String,
    textcolor: Color = Color.Unspecified
) {
    Text(
        modifier = modifier,
        text = text,
        style = ProtonTheme.typography.default,
        color = textcolor
    )
}