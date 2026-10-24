package uz.tikoncha_parent.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.notification
import tikoncha_parents.composeapp.generated.resources.read_notification
import uz.tikoncha_parent.data.mapper.toUi
import uz.tikoncha_parent.domain.model.news.News
import uz.tikoncha_parent.presentation.base.CustomHeader
import uz.tikoncha_parent.presentation.domain.model.LanguageType
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.LargeIconButtonPadding
import uz.tikoncha_parent.ui.LargeIconButtonSize
import uz.tikoncha_parent.ui.PrimaryColor
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor
import uz.tikoncha_parent.ui.theme.rememberScreenSystemBars

class NotificationScreen : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current

        val viewModel = koinScreenModel<NotificationViewModel>()
        val state = viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit){viewModel.load()}

        NotificationUi(
            items = state.value.items,
            navigator = navigator,
            markAll = {
                viewModel.markAllReadOptimistic()
            },
            markRead = {
                viewModel.markReadOptimistic(it)
            }
        )
    }
}

@Composable
fun NotificationUi(
    items: List<News>,
    navigator: Navigator?,
    markAll: () -> Unit,
    markRead: (Long) -> Unit
) {
    val rootNavigator = navigator?.parent
    val navigator = rootNavigator ?: LocalNavigator.currentOrThrow

    val language = remember {
        if (LanguagePrefs.loadOrDefault().isUzbekFamily) "uz" else "ru"  // yangiliklar faqat uz/ru
    }

    val uiItems = remember(items, language) {
        items.map { it.toUi(language) }
    }

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
            title = stringResource(Res.string.notification),
            onBackClick = {
                navigator.pop()
            },
            showBackButton = true,
            trailingIcon = {
                FilledTonalIconButton(
                    modifier = Modifier
                        .size(LargeIconButtonSize),
                    onClick = {
                        markAll()
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.extendedColor.cardColor,
                        contentColor = MaterialTheme.extendedColor.textColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.read_notification),
                        contentDescription = "Barchsi öqildi",
                        tint = PrimaryColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(LargeIconButtonPadding)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ContainerPadding)
        ) {
            items(uiItems, key = { it.id }) { item ->
                NotificationItem(
                    item = item,
                    onClick = {
                        markRead(item.id)
                        navigator.push(NotificationVerifyScreen(newsId = item.id))
                    }
                )
            }
        }
    }
}


@Preview
@Composable
private fun Preview(){
    TikonchaParentTheme {
        NotificationUi(
            items = listOf(),
            navigator = null,
            markRead = {},
            markAll = {}
        )
    }
}