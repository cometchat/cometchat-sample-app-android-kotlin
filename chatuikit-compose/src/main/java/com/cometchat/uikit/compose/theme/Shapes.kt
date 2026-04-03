package com.cometchat.uikit.compose.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
class Shapes(
    // Shapes None and Full are omitted as None is a RectangleShape and Full is a CircleShape.
    val extraSmall: CornerBasedShape = ShapeDefaults.ExtraSmall,
    val small: CornerBasedShape = ShapeDefaults.Small,
    val medium: CornerBasedShape = ShapeDefaults.Medium,
    val large: CornerBasedShape = ShapeDefaults.Large,
    val extraLarge: CornerBasedShape = ShapeDefaults.ExtraLarge,
) {
    /** Returns a copy of this Shapes, optionally overriding some of the values. */
    fun copy(
        extraSmall: CornerBasedShape = this.extraSmall,
        small: CornerBasedShape = this.small,
        medium: CornerBasedShape = this.medium,
        large: CornerBasedShape = this.large,
        extraLarge: CornerBasedShape = this.extraLarge,
    ): Shapes =
        Shapes(
            extraSmall = extraSmall,
            small = small,
            medium = medium,
            large = large,
            extraLarge = extraLarge,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Shapes) return false
        if (extraSmall != other.extraSmall) return false
        if (small != other.small) return false
        if (medium != other.medium) return false
        if (large != other.large) return false
        if (extraLarge != other.extraLarge) return false
        return true
    }

    override fun hashCode(): Int {
        var result = extraSmall.hashCode()
        result = 31 * result + small.hashCode()
        result = 31 * result + medium.hashCode()
        result = 31 * result + large.hashCode()
        result = 31 * result + extraLarge.hashCode()
        return result
    }

    override fun toString(): String {
        return "Shapes(" +
            "extraSmall=$extraSmall, " +
            "small=$small, " +
            "medium=$medium, " +
            "large=$large, " +
            "extraLarge=$extraLarge)"
    }
}

/** Contains the default values used by [Shapes] */
object ShapeDefaults {
    /** Extra small sized corner shape */
    val ExtraSmall: CornerBasedShape = ShapeTokens.CornerExtraSmall

    /** Small sized corner shape */
    val Small: CornerBasedShape = ShapeTokens.CornerSmall

    /** Medium sized corner shape */
    val Medium: CornerBasedShape = ShapeTokens.CornerMedium

    /** Large sized corner shape */
    val Large: CornerBasedShape = ShapeTokens.CornerLarge

    /** Extra large sized corner shape */
    val ExtraLarge: CornerBasedShape = ShapeTokens.CornerExtraLarge
}

internal object ShapeTokens {
    val CornerExtraLarge = RoundedCornerShape(28.0.dp)
    val CornerExtraLargeTop =
        RoundedCornerShape(
            topStart = 28.0.dp,
            topEnd = 28.0.dp,
            bottomEnd = 0.0.dp,
            bottomStart = 0.0.dp
        )
    val CornerExtraSmall = RoundedCornerShape(4.0.dp)
    val CornerExtraSmallTop =
        RoundedCornerShape(
            topStart = 4.0.dp,
            topEnd = 4.0.dp,
            bottomEnd = 0.0.dp,
            bottomStart = 0.0.dp
        )
    val CornerFull = CircleShape
    val CornerLarge = RoundedCornerShape(16.0.dp)
    val CornerLargeEnd =
        RoundedCornerShape(
            topStart = 0.0.dp,
            topEnd = 16.0.dp,
            bottomEnd = 16.0.dp,
            bottomStart = 0.0.dp
        )
    val CornerLargeTop =
        RoundedCornerShape(
            topStart = 16.0.dp,
            topEnd = 16.0.dp,
            bottomEnd = 0.0.dp,
            bottomStart = 0.0.dp
        )
    val CornerMedium = RoundedCornerShape(12.0.dp)
    val CornerNone = RectangleShape
    val CornerSmall = RoundedCornerShape(8.0.dp)
}
internal enum class ShapeKeyTokens {
    CornerExtraLarge,
    CornerExtraLargeTop,
    CornerExtraSmall,
    CornerExtraSmallTop,
    CornerFull,
    CornerLarge,
    CornerLargeEnd,
    CornerLargeTop,
    CornerMedium,
    CornerNone,
    CornerSmall,
}

/** Helper function for component shape tokens. Used to grab the top values of a shape parameter. */
internal fun CornerBasedShape.top(): CornerBasedShape {
    return copy(bottomStart = CornerSize(0.0.dp), bottomEnd = CornerSize(0.0.dp))
}

/**
 * Helper function for component shape tokens. Used to grab the bottom values of a shape parameter.
 */
internal fun CornerBasedShape.bottom(): CornerBasedShape {
    return copy(topStart = CornerSize(0.0.dp), topEnd = CornerSize(0.0.dp))
}

/**
 * Helper function for component shape tokens. Used to grab the start values of a shape parameter.
 */
internal fun CornerBasedShape.start(): CornerBasedShape {
    return copy(topEnd = CornerSize(0.0.dp), bottomEnd = CornerSize(0.0.dp))
}

/** Helper function for component shape tokens. Used to grab the end values of a shape parameter. */
internal fun CornerBasedShape.end(): CornerBasedShape {
    return copy(topStart = CornerSize(0.0.dp), bottomStart = CornerSize(0.0.dp))
}

/**
 * Helper function for component shape tokens. Here is an example on how to use component color
 * tokens: ``MaterialTheme.shapes.fromToken(FabPrimarySmallTokens.ContainerShape)``
 */
internal fun Shapes.fromToken(value: ShapeKeyTokens): Shape {
    return when (value) {
        ShapeKeyTokens.CornerExtraLarge -> extraLarge
        ShapeKeyTokens.CornerExtraLargeTop -> extraLarge.top()
        ShapeKeyTokens.CornerExtraSmall -> extraSmall
        ShapeKeyTokens.CornerExtraSmallTop -> extraSmall.top()
        ShapeKeyTokens.CornerFull -> CircleShape
        ShapeKeyTokens.CornerLarge -> large
        ShapeKeyTokens.CornerLargeEnd -> large.end()
        ShapeKeyTokens.CornerLargeTop -> large.top()
        ShapeKeyTokens.CornerMedium -> medium
        ShapeKeyTokens.CornerNone -> RectangleShape
        ShapeKeyTokens.CornerSmall -> small
    }
}

/**
 * Converts a shape token key to the local shape provided by the theme The color is subscribed to
 * [LocalShapes] changes
 */
internal val ShapeKeyTokens.value: Shape @Composable @ReadOnlyComposable get() = CometChatTheme.shapes.fromToken(this)

/** CompositionLocal used to specify the default shapes for the surfaces. */
val LocalShapes = staticCompositionLocalOf { Shapes() }
