package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

/**
 * Sentinel value indicating a style property (color, resource ID) has not been set.
 * When a property has this value, it should fall back to the parent style's value.
 */
const val STYLE_NOT_SET: Int = Int.MIN_VALUE

/**
 * Sentinel value indicating a dimension property has not been set.
 * When a property has this value, it should fall back to the parent style's value.
 */
const val DIMENSION_NOT_SET: Float = Float.MIN_VALUE

/**
 * Resolves a color property value.
 * If the child value is the sentinel [STYLE_NOT_SET], returns the base value.
 * Otherwise, returns the child value.
 *
 * @param childValue The value from the child style
 * @param baseValue The fallback value from the base style
 * @return The resolved color value
 */
fun resolveStyleColor(childValue: Int, baseValue: Int): Int {
    return if (childValue == STYLE_NOT_SET) baseValue else childValue
}

/**
 * Resolves a dimension property value.
 * If the child value is the sentinel [DIMENSION_NOT_SET], returns the base value.
 * Otherwise, returns the child value.
 *
 * @param childValue The value from the child style
 * @param baseValue The fallback value from the base style
 * @return The resolved dimension value
 */
fun resolveStyleDimension(childValue: Float, baseValue: Float): Float {
    return if (childValue == DIMENSION_NOT_SET) baseValue else childValue
}

/**
 * Resolves a style resource ID property value.
 * If the child value is the sentinel [STYLE_NOT_SET], returns the base value.
 * Otherwise, returns the child value.
 *
 * @param childValue The value from the child style
 * @param baseValue The fallback value from the base style
 * @return The resolved resource ID value
 */
fun resolveStyleRes(childValue: Int, baseValue: Int): Int {
    return if (childValue == STYLE_NOT_SET) baseValue else childValue
}
