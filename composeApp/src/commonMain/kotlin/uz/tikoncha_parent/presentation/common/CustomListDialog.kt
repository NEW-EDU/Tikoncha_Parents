package uz.tikoncha_parent.presentation.common

import uz.tikoncha_parent.ui.rememberCachedImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import uz.tikoncha_parent.presentation.base.Loading
import uz.tikoncha_parent.ui.CloseButtonInnerPadding
import uz.tikoncha_parent.ui.CloseButtonSize
import uz.tikoncha_parent.ui.ContainerPadding
import uz.tikoncha_parent.ui.ItemHeight
import uz.tikoncha_parent.ui.NormalTextSize
import uz.tikoncha_parent.ui.SpaceMedium
import uz.tikoncha_parent.ui.TextFieldCornerRadius
import uz.tikoncha_parent.ui.TextFieldIconSize
import uz.tikoncha_parent.ui.TextFieldInnerPadding
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.close_circle
import tikoncha_parents.composeapp.generated.resources.ohirgi_faollik
import tikoncha_parents.composeapp.generated.resources.profile_hedgehog_img
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.base.coverShadow
import uz.tikoncha_parent.ui.DividerHorizontal
import uz.tikoncha_parent.ui.NormalLargeTextSize
import uz.tikoncha_parent.ui.PrimaryColor
import uz.tikoncha_parent.ui.SmallTextSize
import uz.tikoncha_parent.ui.SpaceSmall
import uz.tikoncha_parent.ui.SuccessColor
import uz.tikoncha_parent.ui.UltraSmallTextSize
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor

@Composable
fun <T>CustomListDialog(
    modifier: Modifier = Modifier,
    title:String,
    items:List<T>,
    show:Boolean = true,
    loading: Boolean = false,
    noChild: Boolean = false,
    emptyText: String = "",
    errorMessage: String = "",
    onItemSelected:(T) -> Unit,
    onDismiss:() -> Unit
) {

    var searchQuery by remember {
        mutableStateOf("")
    }
    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { it.toString().lowercase().trim().contains(searchQuery.trim().lowercase().toString(), ignoreCase = false) }
        }

    }



    if (show) {
        Dialog(
            onDismissRequest = onDismiss
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                val isCompact = maxWidth < 600.dp
                val dialogWidth = if (isCompact) maxWidth else minOf(maxWidth, 520.dp)
                val dialogMaxHeight = maxHeight * if (isCompact) 0.92f else 0.85f

                val headerHeightApprox = 110.dp
                val rowHeight = ItemHeight
                val availableForList =
                    (dialogMaxHeight - headerHeightApprox).coerceAtLeast(rowHeight)

                val maxItemsToShow = (availableForList / rowHeight).toInt().coerceAtLeast(1)
                val visibleCount = minOf(filteredItems.size, maxItemsToShow)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .coverShadow(
                            shape = RoundedCornerShape(TextFieldCornerRadius)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.extendedColor.backgroundColor
                    ),
                    shape = RoundedCornerShape(TextFieldCornerRadius)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.extendedColor.backgroundColor)
                            .padding(ContainerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomText(
                                text = title,
                                fontSize = NormalTextSize,
                                modifier = Modifier
                                    .weight(1f)
                            )
                            SpaceMedium()

                            IconButton(
                                onClick = {
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .size(CloseButtonSize)
                                    .padding(CloseButtonInnerPadding)
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.close_circle),
                                    contentDescription = "Close",
                                    colorFilter = ColorFilter.tint(MaterialTheme.extendedColor.titleColor)
                                )
                            }

                        }
                        SpaceMedium()

                        if (loading || errorMessage.isNotEmpty()) {
                            Loading(loading)
                            CustomText(
                                text = errorMessage,
                                fontSize = NormalTextSize,
                                modifier = Modifier
                            )
                        } else if (noChild) {
                            CustomText(
                                text = emptyText,
                                fontSize = NormalTextSize,
                                color = MaterialTheme.extendedColor.hintColor,
                                textAlign = TextAlign.Center
                            )
                        } else {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                            ) {
                                filteredItems.take(visibleCount).forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onItemSelected(item)
                                                onDismiss()
                                            },
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        val userInfo =
                                            if (item is UserInfo)
                                                item as UserInfo
                                        else
                                            null

                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, MaterialTheme.extendedColor.cardColor, CircleShape)
                                                .background(
                                                    MaterialTheme.extendedColor.backgroundColor,
                                                    CircleShape
                                                )
                                        ) {
                                            AsyncImage(
                                                model = rememberCachedImage(userInfo?.avatarUrl),
                                                contentDescription = "",
                                                error = painterResource(Res.drawable.profile_hedgehog_img),
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        }
                                        SpaceSmall()


                                        Column(
                                            modifier = Modifier.weight(1f),
                                        ) {

                                            Text(
                                                text = item.toString(),
                                                fontSize = NormalTextSize,
                                                fontWeight = FontWeight.W500,
                                                color = MaterialTheme.extendedColor.textColor
                                            )

                                            Text(
                                                text = stringResource(Res.string.ohirgi_faollik),
                                                fontSize = UltraSmallTextSize,
                                                color = MaterialTheme.extendedColor.textColor.copy(0.5f),
                                                lineHeight = UltraSmallTextSize * 1.0f,
                                                maxLines = 1
                                            )

                                            Text(
                                                text = userInfo?.last_seen.orEmpty(),
                                                fontSize = UltraSmallTextSize,
                                                color = MaterialTheme.extendedColor.textColor.copy(0.5f),
                                                lineHeight = UltraSmallTextSize * 1.0f,
                                                maxLines = 1
                                            )
                                        }

                                        val statusColor = if (userInfo?.subscription == "FREE") {
                                            PrimaryColor
                                        } else {
                                            SuccessColor
                                        }
                                        val statusText = if (userInfo?.subscription == "FREE") {
                                            ""
                                        } else {
                                            userInfo?.subscription
                                        }
                                        CustomText(
                                            text = statusText?:"",
                                            fontSize = SmallTextSize,
                                            color = statusColor,
                                        )
                                    }
                                    DividerHorizontal(
                                        color = MaterialTheme.extendedColor.hintColor.copy(0.5f),
                                        modifier = Modifier
                                            .padding(
                                                start = TextFieldInnerPadding + TextFieldIconSize + TextFieldInnerPadding,
                                                end = TextFieldInnerPadding,
                                                top = 5.dp,
                                                bottom = 5.dp
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ){
        CustomListDialog(
            title = "Tikoncha",
            items = listOf("Tikoncha", "Tikoncha", "Tikoncha", "Tikoncha", "Tikoncha"),
            show = true,
            loading = false,
            errorMessage = "",
            emptyText = "Farzand qo'shilmagan",
            onItemSelected = {},
            onDismiss = {}
        )
    }
}