package uz.tikoncha_parent.presentation.profile.about_us

import uz.tikoncha_parent.ui.SegmentGap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.biz_haqimizda
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars


// AboutUsScreen.kt
class AboutUsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        AboutUsUi(navigator = navigator)
    }
}

@Composable
fun AboutUsUi(navigator: Navigator?) {
    val systemBars = rememberScreenSystemBars(
        statusBarColor = AppColors.bg.secondary,
        navigationBarColor = AppColors.bg.secondary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(systemBars.modifier)
            .background(AppColors.bg.secondary)
    ) {
        CustomHeader(
            title = stringResource(Res.string.biz_haqimizda),
            showBackButton = true,
            onBackClick = { navigator?.pop() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ContainerPadding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AboutUsContent) { block ->
                when (block) {
                    is AboutBlock.Paragraph -> AboutParagraph(block.text)
                    is AboutBlock.Heading -> AboutHeading(block.text)
                    is AboutBlock.BulletList -> {
                        Column(verticalArrangement = Arrangement.spacedBy(SegmentGap)) {
                            block.items.forEachIndexed { i, item -> AboutBullet(item, i, block.items.size) }
                        }
                    }
                    is AboutBlock.Quote -> AboutQuote(block.text)
                    AboutBlock.Spacing -> Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun PRE() {
    TikonchaParentTheme(
        ThemeMode.LIGHT
    ) {
        AboutUsUi(
            navigator = null
        )
    }
}