package uz.tikoncha_parent.presentation.base

import uz.tikoncha_parent.ui.theme.NunitoFontFamily
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import org.jetbrains.compose.resources.Font
import uz.tikoncha_parent.ui.NormalTextSize

import org.jetbrains.compose.ui.tooling.preview.Preview
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.nunito_bold
import uz.tikoncha_parent.ui.theme.extendedColor

@Composable
fun CustomText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.extendedColor.onBackgroundColor,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: TextUnit = TextUnit.Unspecified,
    softWrap: Boolean = true,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle = FontStyle.Normal,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    fontFamily: FontFamily = NunitoFontFamily()
) {
    Text(
        overflow = overflow,
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        minLines = minLines,
        fontWeight = fontWeight,
        fontSize = fontSize,
        softWrap = softWrap,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        fontStyle = fontStyle,
        fontFamily = fontFamily
    )
}


@Composable
fun CustomText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium.copy(),
    color: Color = MaterialTheme.extendedColor.onBackgroundColor,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: TextUnit = NormalTextSize,
    softWrap: Boolean = true,
    lineHeight: TextUnit = fontSize * 1.4f,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle = FontStyle.Normal,
    textAlign: TextAlign = TextAlign.Start,
    textDecoration: TextDecoration = TextDecoration.None,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    fontFamily: FontFamily = NunitoFontFamily()
) {
    Text(
        overflow = overflow,
        textAlign = textAlign,
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        minLines = minLines,
        fontWeight = fontWeight,
        fontSize = fontSize,
        softWrap = softWrap,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        fontStyle = fontStyle,
        textDecoration = textDecoration,
        fontFamily = fontFamily
    )
}


@Preview
@Composable
private fun Pre() {

}