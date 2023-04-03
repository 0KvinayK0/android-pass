package proton.android.pass.featurehome.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ShareId

object Home : NavItem(baseRoute = "home", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
@Suppress("LongParameterList")
fun NavGraphBuilder.homeGraph(
    homeScreenNavigation: HomeScreenNavigation,
    onAddItemClick: (Option<ShareId>, ItemTypeUiState) -> Unit,
    onCreateVaultClick: () -> Unit,
    onEditVaultClick: (ShareId) -> Unit,
    onDeleteVaultClick: (ShareId) -> Unit
) {
    composable(Home) {
        NavHome(
            homeScreenNavigation = homeScreenNavigation,
            onAddItemClick = onAddItemClick,
            onCreateVaultClick = onCreateVaultClick,
            onEditVaultClick = onEditVaultClick,
            onDeleteVaultClick = onDeleteVaultClick
        )
    }
}
