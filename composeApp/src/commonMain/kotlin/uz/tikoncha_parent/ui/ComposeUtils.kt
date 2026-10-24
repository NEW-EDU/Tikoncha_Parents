package uz.tikoncha_parent.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import tikoncha_parents.composeapp.generated.resources.Res
import uz.tikoncha_parent.ui.theme.extendedColor

val ButtonHeight = 50.dp
val DialogButtonHeight = 40.dp
val SmallButtonHeight = 30.dp

val LinearProgressIndicatorHeight = 10.dp

val UltraLargeIconButtonSize = 50.dp
val LargeIconButtonSize = 45.dp
val NormalIconButtonSize = 35.dp
val SmallIconButtonSize = 30.dp
val UltraSmallIconButtonSize = 25.dp
val LargeIconButtonPadding = 12.dp
val NormalIconButtonPadding = 8.dp
val ChatHeaderAvatarSize = 44.dp
val SmallIconButtonPadding = 5.dp

val ButtonCornerRadius = 24.dp
val ButtonDialogCornerRadius = 12.dp

val ChatMessageCornerRadius = 20.dp
val CardCornerRadius = 24.dp
val LargeCardCornerRadius = 32.dp
val CardCornerPadding = 20.dp
val ContainerCornerRadius = 15.dp
val PaddingCornerRadius = 15.dp
val MainCornerRadius = 16.dp
val ShapeCornerRadius = 10.dp
val ItemElevation = 2.dp

val SmallCardCornerRadius = 18.dp

val TextFieldCornerRadius = 12.dp
val CoinsCornerRadius = 8.dp
val TextFieldHeight = 45.dp
val TextFieldHeightDialogOrButtonSheet = 40.dp

val CoinTextFieldWidth = 90.dp

val TextFieldInnerPadding = 15.dp
val TextFieldIconSize = 20.dp

val NormalTextSizeSp = 16.sp
val ContainerPadding = 16.dp

val HeaderHeight = 56.dp
/** Bosh sahifa bloki balandligi — ixchamroq, ekranga ko'proq blok sig'sin (avval 130). */
val HomeItemHeight = 106.dp
val TextFieldTextStyle: TextStyle @Composable get() = MaterialTheme.typography.titleMedium
val ChatTextSize:TextUnit @Composable get() =  14.sp //.responsiveSp()
val UltraLargeTextSize:TextUnit @Composable get() =  20.sp //.responsiveSp()
val SmallTextSize:TextUnit @Composable get() =  12.sp //.responsiveSp()
val NormalTextSize:TextUnit @Composable get() =  14.sp //.responsiveSp()
val NormalTextLineHeight:TextUnit @Composable get() =  20.sp //.responsiveSp()
val LargeTextSize:TextUnit @Composable get() =  18.sp //.responsiveSp()
val UltraSmallTextSize:TextUnit @Composable get() =  10.sp //.responsiveSp()
val NormalLargeTextSize:TextUnit @Composable get() =  18.sp //.responsiveSp()

val CloseButtonSize: Dp = 30.dp

val ChatTextFieldCornerRadius = 24.dp
val CloseButtonInnerPadding: Dp = 5.dp

val ItemHeight: Dp = 50.dp

val LargeImageHeight: Dp = 170.dp

val AppIconSize: Dp = 45.dp
val AppIconInnerPadding: Dp = 10.dp
val AppItemHeight: Dp = 60.dp
val SmallIconSize: Dp = 18.dp
val HomeIconSize: Dp = 76.dp
val NormalIconSize: Dp = 20.dp
val LargeIconSize: Dp = 28.dp

val ProfileImageSize: Dp = 100.dp

val ProfileStatsContainerHeight: Dp = 85.dp

val ParentsInformationContainerHeight: Dp = 260.dp

@Composable
fun SpaceLarge() {
    Spacer(Modifier.size(20.dp))
}
@Composable
fun SpaceMedium() {
    Spacer(Modifier.size(15.dp))
}
@Composable
fun SpaceSmall() {
    Spacer(Modifier.size(10.dp))
}

@Composable
fun Space(size: Dp) {
    Spacer(Modifier.size(size))
}



@Composable
fun SpaceUltraSmall() {
    Spacer(Modifier.size(5.dp))
}

@Composable
fun DividerHorizontal(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.extendedColor.hintColor.copy(alpha = 0.1f)
) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
        color = color
    )
}

@Composable
fun MyFontFamily() = uz.tikoncha_parent.ui.theme.NunitoFontFamily()