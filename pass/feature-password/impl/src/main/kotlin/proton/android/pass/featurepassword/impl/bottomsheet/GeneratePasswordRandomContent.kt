package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R

@Composable
fun ColumnScope.GeneratePasswordRandomContent(
    modifier: Modifier = Modifier,
    content: GeneratePasswordContent.RandomPassword,
    onLengthChange: (Int) -> Unit,
    onSpecialCharactersChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(SLIDER_TEXT_WEIGHT),
            text = stringResource(R.string.character_count, content.length),
            color = PassTheme.colors.textNorm,
            style = PassTypography.body3Regular,
            fontSize = 16.sp
        )

        var sliderPosition by remember { mutableStateOf(content.length.toFloat()) }
        val valueRange = remember { 4.toFloat()..64.toFloat() }
        Slider(
            modifier = Modifier.weight(SLIDER_CONTENT_WEIGHT),
            value = sliderPosition,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = PassTheme.colors.loginInteractionNormMajor1,
                activeTrackColor = PassTheme.colors.loginInteractionNormMajor1,
                inactiveTrackColor = PassTheme.colors.loginInteractionNormMinor1
            ),
            onValueChange = { newLength ->
                if (sliderPosition.toInt() != newLength.toInt()) {
                    sliderPosition = newLength
                    onLengthChange(newLength.toInt())
                }
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.special_characters),
            color = PassTheme.colors.textNorm,
            style = PassTypography.body3Regular,
            fontSize = 16.sp
        )
        Switch(
            checked = content.hasSpecialCharacters,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PassTheme.colors.loginInteractionNormMajor1,
            ),
            onCheckedChange = { onSpecialCharactersChange(it) }
        )
    }
}

@Preview
@Composable
fun GeneratePasswordRandomContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            Column {
                GeneratePasswordRandomContent(
                    content = GeneratePasswordContent.RandomPassword(
                        length = 12,
                        hasSpecialCharacters = true
                    ),
                    onLengthChange = {},
                    onSpecialCharactersChange = {}
                )
            }
        }
    }
}


private const val SLIDER_CONTENT_WEIGHT = 0.7f
private const val SLIDER_TEXT_WEIGHT = 0.3f