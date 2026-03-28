package com.antcashmanager.android.ui.components.text

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Text(
        text = annotatedString,
        maxLines = 1,
        modifier = modifier.padding(
            horizontal = 12.dp,
            vertical = 8.dp,
        ),
        style = TextStyle(fontSize = 16.sp),
    )
}

@Preview(showBackground = true)
@Composable
fun TextLinkPreview() {
    TextLink(
        modifier = Modifier,
        text = "Click here",
        onClick = { /* No-op for preview */ }
    )
}
