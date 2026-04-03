package com.cometchat.sampleapp.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme

/**
 * Theme Customization Examples for CometChat UI Kit.
 *
 * This file demonstrates how to customize the CometChat UI Kit theme
 * to match your app's branding. The CometChat UI Kit uses a theming system
 * that allows you to customize:
 *
 * 1. **Colors**: Primary, neutral, alert, background, text, and icon colors
 * 2. **Typography**: Font families, sizes, and weights
 * 3. **Light/Dark Mode**: Separate color schemes for each mode
 *
 * ## How CometChat Theming Works:
 *
 * CometChat UI Kit components use `CometChatTheme` which provides:
 * - `CometChatTheme.colorScheme`: Access to all theme colors
 * - `CometChatTheme.typography`: Access to all text styles
 *
 * ## Customization Approaches:
 *
 * ### Approach 1: Use Default Theme (Recommended for Quick Start)
 * ```kotlin
 * CometChatTheme {
 *     // Your content - uses default CometChat colors
 * }
 * ```
 *
 * ### Approach 2: Customize Primary Color Only
 * ```kotlin
 * CometChatTheme(
 *     colorScheme = lightColorScheme(primary = Color(0xFF6851D6))
 * ) {
 *     // Your content with custom primary color
 * }
 * ```
 *
 * ### Approach 3: Full Customization with Light/Dark Mode
 * ```kotlin
 * CustomCometChatTheme {
 *     // Your content with full customization
 * }
 * ```
 *
 * Validates: Requirements 13.2, 13.3, 13.4, 13.5, 13.6
 */

// =============================================================================
// EXAMPLE 1: Custom Primary Colors
// =============================================================================

/**
 * Example: Custom brand colors.
 *
 * Replace these with your brand's primary colors.
 * The primary color is used for:
 * - Action buttons
 * - Selected states
 * - Links and interactive elements
 * - Highlights and accents
 *
 * Validates: Requirement 13.3
 */
object CustomBrandColors {
    // Your brand's primary color
    val Primary = Color(0xFF6851D6)  // CometChat purple
    val PrimaryDark = Color(0xFF5A45C0)

    // Alternative brand colors (examples)
    val BluePrimary = Color(0xFF2196F3)
    val GreenPrimary = Color(0xFF4CAF50)
    val OrangePrimary = Color(0xFFFF9800)
    val RedPrimary = Color(0xFFF44336)
}

// =============================================================================
// EXAMPLE 2: Custom Light Color Scheme
// =============================================================================

/**
 * Example: Custom light mode color scheme.
 *
 * This demonstrates how to create a custom color scheme for light mode
 * by overriding the primary color. The `lightColorScheme()` function
 * automatically generates extended primary colors and sets appropriate
 * defaults for all other colors.
 *
 * Validates: Requirements 13.3, 13.5
 */
val CustomLightColorScheme = lightColorScheme(
    // Override the primary color - extended colors are auto-generated
    primary = CustomBrandColors.Primary
)

// =============================================================================
// EXAMPLE 3: Custom Dark Color Scheme
// =============================================================================

/**
 * Example: Custom dark mode color scheme.
 *
 * This demonstrates how to create a custom color scheme for dark mode.
 * Dark mode typically uses lighter text on darker backgrounds.
 *
 * Validates: Requirements 13.3, 13.5
 */
val CustomDarkColorScheme = darkColorScheme(
    // Override the primary color for dark mode
    primary = CustomBrandColors.PrimaryDark
)

// =============================================================================
// EXAMPLE 4: Custom Typography
// =============================================================================

/**
 * Example: Custom typography.
 *
 * This demonstrates how to customize the typography used in your app.
 * You can change font families, sizes, weights, and letter spacing.
 *
 * Note: CometChat components use their own typography system internally,
 * but you can use this for your app's custom UI elements.
 *
 * Validates: Requirement 13.4
 */
val CustomTypography = Typography(
    // Display styles - for large headlines
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),

    // Headline styles - for section headers
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),

    // Title styles - for card titles and list items
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles - for main content
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles - for buttons and captions
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// =============================================================================
// EXAMPLE 5: Theme with Light/Dark Mode Support
// =============================================================================

/**
 * Example: Theme composable with automatic light/dark mode support.
 *
 * This demonstrates how to create a theme that automatically switches
 * between light and dark color schemes based on system settings.
 *
 * ## Usage:
 * ```kotlin
 * @Composable
 * fun MyApp() {
 *     CustomCometChatTheme {
 *         // Your app content
 *     }
 * }
 * ```
 *
 * Validates: Requirements 13.2, 13.5
 */
@Composable
fun CustomCometChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Select color scheme based on dark mode setting
    val colorScheme = if (darkTheme) {
        CustomDarkColorScheme
    } else {
        CustomLightColorScheme
    }

    // Apply CometChat theme with custom colors
    CometChatTheme(
        colorScheme = colorScheme
    ) {
        content()
    }
}

// =============================================================================
// USAGE NOTES
// =============================================================================

/**
 * ## Quick Start Guide:
 *
 * 1. **To use default CometChat theme:**
 *    ```kotlin
 *    CometChatTheme {
 *        // Your content
 *    }
 *    ```
 *
 * 2. **To customize primary color only:**
 *    ```kotlin
 *    CometChatTheme(
 *        colorScheme = lightColorScheme(primary = Color(0xFF6851D6))
 *    ) {
 *        // Your content
 *    }
 *    ```
 *
 * 3. **To use custom theme with light/dark mode:**
 *    ```kotlin
 *    CustomCometChatTheme {
 *        // Your content
 *    }
 *    ```
 *
 * ## Best Practices:
 *
 * - Always test both light and dark modes
 * - Ensure sufficient color contrast for accessibility
 * - Use semantic color names (primary, secondary, etc.)
 * - Keep color palettes consistent across the app
 *
 * ## CometChat Color Roles:
 *
 * | Color | Usage |
 * |-------|-------|
 * | primary | Main brand color, buttons, links, highlights |
 * | backgroundColor1 | Main screen background |
 * | backgroundColor2 | Card backgrounds, elevated surfaces |
 * | textColorPrimary | Main text content |
 * | textColorSecondary | Subtitles, timestamps |
 * | successColor | Success messages, online status |
 * | errorColor | Error messages, delete actions |
 * | warningColor | Warning messages, blocked status |
 * | infoColor | Information messages, links |
 */
