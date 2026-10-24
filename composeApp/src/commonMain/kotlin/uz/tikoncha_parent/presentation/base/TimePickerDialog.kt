
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime
import uz.tikoncha_parent.presentation.base.CustomOutlinedButton
import uz.tikoncha_parent.ui.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.*
import uz.tikoncha_parent.presentation.base.CustomButton
import uz.tikoncha_parent.presentation.base.CustomText
import uz.tikoncha_parent.presentation.base.coverShadow
import uz.tikoncha_parent.ui.theme.ThemeMode
import uz.tikoncha_parent.ui.theme.TikonchaParentTheme
import uz.tikoncha_parent.ui.theme.extendedColor


//val WHEEL_HEIGHT = 74.dp
//val WHEEL_WIDTH = 140.dp
//val ITEM_HEIGHT = 24.dp
//val EDGE_PADDING = 25.dp

val ITEM_HEIGHT = 24.dp
val VISIBLE_ITEMS = 5
val WHEEL_HEIGHT = ITEM_HEIGHT * VISIBLE_ITEMS
val WHEEL_WIDTH = 140.dp
val EDGE_PADDING = (WHEEL_HEIGHT - ITEM_HEIGHT) / 2f
@Composable
fun TimePickerDialog(
    show: Boolean,
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }

    if (show) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .coverShadow(
                        radius = CardCornerRadius
                    ),
                shape = RoundedCornerShape(CardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.extendedColor.backgroundColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomText(
                        text = stringResource(Res.string.tugash_vaqtini_belgilang),
                        fontSize = NormalTextSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TimeColumn(
                            range = 0..23,
                            selected = selectedHour,
                            onSelected = { selectedHour = it }
                        )
                        Spacer(Modifier.size(5.dp))
                        TimeColumn(
                            range = 0..59,
                            selected = selectedMinute,
                            onSelected = { selectedMinute = it }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {

                        CustomOutlinedButton(
                            text = stringResource(Res.string.bekor_qilish),
                            onClick = onDismiss,
                            modifier = Modifier
                                .height(DialogButtonHeight)
                                .fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        CustomButton(
                            onClick = {
                                onTimeSelected(LocalTime(selectedHour, selectedMinute))
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(DialogButtonHeight),
                            text = stringResource(Res.string.saqlash),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeColumn(
    range: IntRange,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    val items = remember(range) { range.toList() }
    val listState = rememberLazyListState()
    var isFirstLaunch by remember { mutableStateOf(true) }

    // Markazga kelgan itemni aniqlab, selected qilib turamiz
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val centerY = info.viewportStartOffset + info.viewportSize.height / 2

            info.visibleItemsInfo.firstOrNull { item ->
                centerY in item.offset..(item.offset + item.size)
            }?.index ?: info.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs((item.offset + item.size / 2) - centerY)
            }?.index
        }
            .filterNotNull()
            .distinctUntilChanged()
            .map { idx -> range.elementAtOrNull(idx) }
            .filterNotNull()
            .collect { value -> onSelected(value) }
    }

    // start position
    LaunchedEffect(Unit) {
        if (isFirstLaunch) {
            // NOTE: range 0..23 / 0..59 bo‘lgani uchun index = value ishlaydi
            listState.scrollToItem(index = selected)
            onSelected(selected)
            isFirstLaunch = false
        }
    }

    // === RASMDAGI WHEEL LOOK ===
    Box(
        modifier = Modifier
            .width(WHEEL_WIDTH)
            .height(WHEEL_HEIGHT)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.extendedColor.cardColor) // ichki fon
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = EDGE_PADDING),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(items.size) { index ->
                val value = items[index]
                val isSelected = value == selected

                Box(
                    modifier = Modifier
                        .height(ITEM_HEIGHT)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CustomText(
                        text = value.toString().padStart(2, '0'),
                        fontSize = if (isSelected) 18.sp else 14.sp,
                        fontWeight = if (isSelected) FontWeight.W600 else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.extendedColor.onBackgroundColor
                        else
                            MaterialTheme.extendedColor.onBackgroundColor.copy(alpha = 0.35f)
                    )
                }
            }
        }

        // Markazdagi highlight bar (rasmdagi "pill")
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
                .height(ITEM_HEIGHT)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.extendedColor.onBackgroundColor.copy(alpha = 0.10f))
                .zIndex(1f)
        )

        // Tepaga/pastga fade (rasmdagi xiralashish)
        val bg = MaterialTheme.extendedColor.cardColor
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bg, bg.copy(alpha = 0f))
                    )
                )
                .zIndex(2f)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bg.copy(alpha = 0f), bg)
                    )
                )
                .zIndex(2f)
        )
    }
}

//@Composable
//fun TimeColumn(
//    range: IntRange,
//    selected: Int,
//    onSelected: (Int) -> Unit
//) {
//    val items = range.toList()
//    val listState = rememberLazyListState()
//
//    var isFirstLaunch by remember { mutableStateOf(true) }
//
//    LaunchedEffect(listState) {
//        snapshotFlow {
//            val subscription_info = listState.layoutInfo
//            val centerY = subscription_info.viewportStartOffset + subscription_info.viewportSize.height / 2
//            // Markaz CHIZIG'INI ichiga olgan itemni aniq topamiz:
//            subscription_info.visibleItemsInfo.firstOrNull { item ->
//                val mid = item.offset + item.size / 2
//                // item markazga eng yaqin va markaz chizig'i itemning vertikal chegaralari ichida
//                centerY in item.offset..(item.offset + item.size)
//            }?.index
//            // Agar yuqoridagi topilmasa (masalan, layout o'zgarishi), "eng yaqin"iga qaytamiz:
//                ?: subscription_info.visibleItemsInfo.minByOrNull { item ->
//                    kotlin.math.abs((item.offset + item.size / 2) - centerY)
//                }?.index
//        }
//            .filterNotNull()
//            .distinctUntilChanged()                // har BIR indeks markazga kelganda 1 marta
//            .map { idx -> range.elementAtOrNull(idx) }
//            .filterNotNull()
//            .collect { value ->
//                onSelected(value)                  // <<— EQUAL CHECK OLIB TASHLANDI
//            }
//    }
//
//
//    // Boshlanishda to'g'ridan-to'g'ri scroll qilish
//    LaunchedEffect(Unit) {
//        if (isFirstLaunch) {
//            listState.scrollToItem(index = selected)
//            onSelected(selected)
//            isFirstLaunch = false
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .width(140.dp)
//            .height(WHEEL_HEIGHT)
//            .border(
//                1.dp,
//                MaterialTheme.extendedColor.primaryColor,
//                RoundedCornerShape(TextFieldCornerRadius)
//            )
//    ) {
//        LazyColumn(
//            state = listState,
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            contentPadding = PaddingValues(vertical = EDGE_PADDING),
//            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
//        ) {
//            items(items.size) { index ->
//                val value = items[index]
//                val isSelected = value == selected
//                Box(Modifier.height(ITEM_HEIGHT), contentAlignment = Alignment.Center) {
//                    CustomText(
//                        text = value.toString().padStart(2, '0'),
//                        fontSize = if (isSelected) 16.sp else 12.sp,
//                        fontWeight = if (isSelected) FontWeight.W500 else FontWeight.Normal,
//                        color = if (isSelected) MaterialTheme.extendedColor.onBackgroundColor else MaterialTheme.extendedColor.hintColor,
//                        modifier = Modifier.padding(vertical = 0.dp)
//                    )
//                }
//            }
//        }
//
//        // Markazni ko‘rsatish uchun fon
//        Box(
//            modifier = Modifier
//                .align(Alignment.Center)
//                .fillMaxWidth()
//                .height(ITEM_HEIGHT)
////                .background(
////                    color = LightGrayColor.copy(alpha = 0.3f),
////                    shape = RoundedCornerShape(6.dp)
////                )
//                .zIndex(1f)
//        )
//    }
//}


@Preview
@Composable
private fun Previewdasd() {
    TikonchaParentTheme(
        ThemeMode.DARK
    ){
        TimePickerDialog(
            show = true,
            initialTime = LocalTime(12, 0),
            onDismiss = {},
            onTimeSelected = {}
        )
    }
}






