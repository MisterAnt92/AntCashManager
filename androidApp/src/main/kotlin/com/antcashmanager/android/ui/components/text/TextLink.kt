package com.antcashmanager.android.ui.components.text

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@Composable
fun TextLink(
    modifier: Modifier,
    text: String,
    onClick: () -> Unit,
) {
    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = Color.Blue,
            fontSize = 14.sp,
            textDecoration = TextDecoration.Underline,
        ),
    )
    val annotatedString = buildAnnotatedString {
        withLink(
            LinkAnnotation.Clickable(
                tag = "URL",
                styles = linkStyles,
                linkInteractionListener = { onClick() },
            ),
        ) {
            append(text)
        }
    }

    AppText(
        text = annotatedString,
        maxLines = 1,
        modifier = modifier.padding(
            horizontal = 12.dp,
            vertical = 8.dp,
        ),
        style = TextStyle(fontSize = 16.sp),
    )
}

@Preview(name = "TextLink - Light", showBackground = true)
@Composable
private fun TextLinkPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        TextLink(
            modifier = Modifier,
            text = stringResource(R.string.common_click_here),
            onClick = {},
        )
    }
}

@Preview(
    name = "TextLink - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TextLinkPreviewDark() {
    TextLinkPreviewLight()
}

@Preview(name = "TextLink - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun TextLinkPreviewAccessibility() {
    TextLinkPreviewLight()
}
