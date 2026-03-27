package com.antcashmanager.android.ui.components.text

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// AppText non serve qui - usa Text di Material3 direttamente se necessario

enum class InputType {
    Text,
    Number,
    Decimal,
    Email,
    Phone,
    Password
}

/**
 * AppTextField - composable TextField riutilizzabile con valori di default coerenti al tema
 * Parametri principali:
 * - value: il valore corrente del campo
 * - onValueChange: callback per cambiamenti del valore
 * - modifier: Modifier per il layout
 * - label: composable per l'etichetta
 * - placeholder: composable per il placeholder
 * - leadingIcon, trailingIcon: icone
 * - inputType: tipo di input per impostare automaticamente keyboardOptions
 * - keyboardOptions, visualTransformation: per personalizzazioni avanzate
 * - shape: forma del campo, default RoundedCornerShape(8.dp)
 * - textStyle: stile del testo di input, default TextStyle.Default
 * - fontWeight: peso del font per il testo di input, sovrascrive textStyle se specificato
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    inputType: InputType = InputType.Text,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(8.dp),
    textStyle: TextStyle = TextStyle.Default,
    fontWeight: FontWeight? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 15,
    isPassword: Boolean = false,
) {
    var passwordVisibility by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val actualKeyboardOptions = if (keyboardOptions != KeyboardOptions.Default) {
        keyboardOptions
    } else {
        when (inputType) {
            InputType.Text -> KeyboardOptions.Default
            InputType.Number -> KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )

            InputType.Decimal -> KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            )

            InputType.Email -> KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            )

            InputType.Phone -> KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            )

            InputType.Password -> KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        }
    }

    val actualVisualTransformation = if (isPassword || inputType == InputType.Password) {
        if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()
    } else {
        visualTransformation
    }

    val actualTrailingIcon: @Composable (() -> Unit)? =
        if (isPassword || inputType == InputType.Password) {
            {
                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                    Icon(
                        imageVector = if (passwordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            trailingIcon
        }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.focusRequester(focusRequester),
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = actualTrailingIcon,
        keyboardOptions = actualKeyboardOptions,
        visualTransformation = actualVisualTransformation,
        shape = shape,
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = textStyle.copy(fontWeight = fontWeight ?: textStyle.fontWeight)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAppTextField() {
    AppTextField(
        value = "Sample Text",
        onValueChange = {},
        label = { androidx.compose.material3.Text("Label") },
        placeholder = { androidx.compose.material3.Text("Placeholder") }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAppTextFieldPassword() {
    AppTextField(
        value = "password123",
        onValueChange = {},
        label = { androidx.compose.material3.Text("Password") },
        isPassword = true
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAppTextFieldWithStyle() {
    AppTextField(
        value = "Styled Text",
        onValueChange = {},
        label = { androidx.compose.material3.Text("Styled Label") },
        textStyle = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold
    )
}
