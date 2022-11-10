package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.usersettings.presentation.compose.view.CrashReportSettingsToggleItem
import me.proton.core.usersettings.presentation.compose.view.TelemetrySettingsToggleItem
import me.proton.pass.presentation.R

@Composable
fun AppSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        ProtonSettingsHeader(title = stringResource(R.string.settings_app_section_title))
        TelemetrySettingsToggleItem()
        CrashReportSettingsToggleItem(divider = {})
    }
}