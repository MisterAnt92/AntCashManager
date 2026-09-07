package com.antcashmanager.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.antcashmanager.android.R

/**
 * Google Fonts provider for Poppins (Open Source, SIL Open Font License).
 */
val provider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs,
    )

val PoppinsFontName = GoogleFont("Poppins")

val PoppinsFontFamily =
    FontFamily(
        Font(googleFont = PoppinsFontName, fontProvider = provider, weight = FontWeight.Light),
        Font(googleFont = PoppinsFontName, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = PoppinsFontName, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = PoppinsFontName, fontProvider = provider, weight = FontWeight.SemiBold),
        Font(googleFont = PoppinsFontName, fontProvider = provider, weight = FontWeight.Bold),
    )

val AppTypography =
    Typography(
        headlineLarge =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 27.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
    )

/**
 * Returns a scaled version of [AppTypography] for the large-text accessibility setting.
 * Every font size and line height is multiplied by [factor] (default 1.25×).
 */
fun scaledTypography(factor: Float = 1.25f): Typography {
    fun TextStyle.scaled() =
        copy(
            fontSize = fontSize * factor,
            lineHeight = lineHeight * factor,
        )
    return Typography(
        headlineLarge = AppTypography.headlineLarge.scaled(),
        headlineMedium = AppTypography.headlineMedium.scaled(),
        headlineSmall = AppTypography.headlineSmall.scaled(),
        titleLarge = AppTypography.titleLarge.scaled(),
        titleMedium = AppTypography.titleMedium.scaled(),
        titleSmall = AppTypography.titleSmall.scaled(),
        bodyLarge = AppTypography.bodyLarge.scaled(),
        bodyMedium = AppTypography.bodyMedium.scaled(),
        bodySmall = AppTypography.bodySmall.scaled(),
        labelLarge = AppTypography.labelLarge.scaled(),
        labelMedium = AppTypography.labelMedium.scaled(),
        labelSmall = AppTypography.labelSmall.scaled(),
    )
}
