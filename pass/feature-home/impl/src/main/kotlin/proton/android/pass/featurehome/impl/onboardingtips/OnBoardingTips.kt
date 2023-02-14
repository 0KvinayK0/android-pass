package proton.android.pass.featurehome.impl.onboardingtips

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun OnBoardingTips(
    modifier: Modifier = Modifier,
    viewModel: OnBoardingTipsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnBoardingTipContent(
        modifier = modifier,
        tipsSetToShow = state.tipsToShow,
        onClick = viewModel::onClick,
        onDismiss = viewModel::onDismiss
    )
}