package com.antcashmanager.android.util

private const val CHANNEL_MASK = 0xFF
private const val CHANNEL_MAX = 255f
private const val LUMA_R = 0.299f
private const val LUMA_G = 0.587f
private const val LUMA_B = 0.114f
const val DEFAULT_LIGHT_LUMINANCE_THRESHOLD = 0.5f

/**
 * True when an ARGB color packed in a [Long] is perceptually light (Rec. 601 luma ≥ [threshold]),
 * i.e. dark text should be drawn on top of it.
 */
fun Long.isPerceptuallyLight(threshold: Float = DEFAULT_LIGHT_LUMINANCE_THRESHOLD): Boolean {
    val r = (this shr 16 and CHANNEL_MASK.toLong()) / CHANNEL_MAX
    val g = (this shr 8 and CHANNEL_MASK.toLong()) / CHANNEL_MAX
    val b = (this and CHANNEL_MASK.toLong()) / CHANNEL_MAX
    return LUMA_R * r + LUMA_G * g + LUMA_B * b >= threshold
}
