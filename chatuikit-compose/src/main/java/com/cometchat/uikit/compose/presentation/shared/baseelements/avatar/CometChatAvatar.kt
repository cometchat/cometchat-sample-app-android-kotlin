package com.cometchat.uikit.compose.presentation.shared.baseelements.avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * CometChatAvatar is a composable that displays a user avatar.
 * It can display an image from a URL or show initials based on the user's name.
 * The component supports emoji detection, placeholder images, and comprehensive styling options.
 *
 * Features:
 * - Displays avatar image from URL with automatic fallback to initials
 * - Generates 2-letter initials from user name
 * - Handles emoji-only names properly
 * - Supports custom placeholder images
 * - Fully customizable styling through AvatarStyle
 * - Uses Coil for efficient image loading
 *
 * @param modifier Modifier for the avatar container
 * @param name The name to display as initials if no image is available or image fails to load
 * @param avatarUrl Optional URL of the avatar image to display
 * @param placeholder Optional placeholder painter to show while image is loading
 * @param style Styling configuration for the avatar. Use AvatarStyle.default() for theme-based defaults
 *
 * @sample
 * ```
 * // Avatar with image URL and default styling
 * CometChatAvatar(
 *     name = "John Doe",
 *     avatarUrl = "https://example.com/avatar.jpg",
 *     style = AvatarStyle.default()
 * )
 *
 * // Avatar with initials only
 * CometChatAvatar(
 *     name = "Jane Smith",
 *     style = AvatarStyle.default()
 * )
 *
 * // Avatar with custom styling
 * CometChatAvatar(
 *     name = "Bob Wilson",
 *     avatarUrl = "https://example.com/bob.jpg",
 *     style = AvatarStyle.default(
 *         backgroundColor = Color.Blue,
 *         borderColor = Color.White,
 *         borderWidth = 2.dp,
 *         cornerRadius = 8.dp
 *     )
 * )
 *
 * // Avatar with a Painter (drawable)
 * CometChatAvatar(
 *     painter = painterResource(R.drawable.avatar),
 *     style = AvatarStyle.default()
 * )
 *
 * // Avatar from User object
 * CometChatAvatar(
 *     user = user,
 *     style = AvatarStyle.default()
 * )
 *
 * // Avatar from Group object
 * CometChatAvatar(
 *     group = group,
 *     style = AvatarStyle.default()
 * )
 * ```
 */
@Composable
fun CometChatAvatar(
    modifier: Modifier = Modifier,
    name: String,
    avatarUrl: String? = null,
    placeholder: Painter? = null,
    style: AvatarStyle = AvatarStyle.default()
) {
    // State to track if image loading failed
    var imageLoadFailed by remember(avatarUrl) { mutableStateOf(false) }
    
    // Use style values directly - defaults are now in AvatarStyle.default()
    val bgColor = style.backgroundColor ?: CometChatTheme.colorScheme.extendedPrimaryColor500
    val txtColor = style.textColor ?: CometChatTheme.colorScheme.primaryButtonIconTint
    val txtStyle = style.textStyle ?: CometChatTheme.typography.bodyBold
    
    // Generate initials from name
    val initials = remember(name) { getInitials(name) }
    
    // Determine shape - use CircleShape when cornerRadius is >= half the minimum dimension for truly circular avatars
    val shape = if (style.cornerRadius >= 50.dp) {
        CircleShape
    } else {
        RoundedCornerShape(style.cornerRadius)
    }
    
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (style.borderWidth > 0.dp && style.borderColor != null) {
                    Modifier.border(style.borderWidth, style.borderColor, shape)
                } else {
                    Modifier
                }
            )
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        // Show image if URL is provided and hasn't failed
        if (!avatarUrl.isNullOrEmpty() && !imageLoadFailed) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar for $name",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                placeholder = placeholder,
                error = placeholder,
                onError = {
                    imageLoadFailed = true
                },
                onSuccess = {
                    imageLoadFailed = false
                }
            )
        }
        
        // Show initials if no image URL or image failed to load
        if (avatarUrl.isNullOrEmpty() || imageLoadFailed) {
            Text(
                text = initials.uppercase(),
                style = txtStyle,
                color = txtColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * CometChatAvatar overload that displays an avatar from a Painter (drawable).
 * This is useful when you have a local drawable resource to display.
 *
 * @param modifier Modifier for the avatar container
 * @param painter The Painter to display as the avatar image
 * @param contentDescription Accessibility description for the image
 * @param style Styling configuration for the avatar
 *
 * @sample
 * ```
 * CometChatAvatar(
 *     painter = painterResource(R.drawable.my_avatar),
 *     contentDescription = "User avatar",
 *     style = AvatarStyle.default()
 * )
 * ```
 */
@Composable
fun CometChatAvatar(
    modifier: Modifier = Modifier,
    painter: Painter,
    contentDescription: String? = null,
    style: AvatarStyle = AvatarStyle.default()
) {
    val bgColor = style.backgroundColor ?: CometChatTheme.colorScheme.extendedPrimaryColor500
    
    val shape = if (style.cornerRadius >= 50.dp) {
        CircleShape
    } else {
        RoundedCornerShape(style.cornerRadius)
    }
    
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (style.borderWidth > 0.dp && style.borderColor != null) {
                    Modifier.border(style.borderWidth, style.borderColor, shape)
                } else {
                    Modifier
                }
            )
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * CometChatAvatar overload that displays an avatar from a User object.
 * Extracts the name and avatar URL from the User model.
 *
 * @param modifier Modifier for the avatar container
 * @param user The CometChat User object
 * @param placeholder Optional placeholder painter to show while image is loading
 * @param style Styling configuration for the avatar
 *
 * @sample
 * ```
 * CometChatAvatar(
 *     user = currentUser,
 *     style = AvatarStyle.default()
 * )
 * ```
 */
@Composable
fun CometChatAvatar(
    modifier: Modifier = Modifier,
    user: User,
    placeholder: Painter? = null,
    style: AvatarStyle = AvatarStyle.default()
) {
    CometChatAvatar(
        modifier = modifier,
        name = user.name ?: "",
        avatarUrl = user.avatar,
        placeholder = placeholder,
        style = style
    )
}

/**
 * CometChatAvatar overload that displays an avatar from a Group object.
 * Extracts the name and icon URL from the Group model.
 *
 * @param modifier Modifier for the avatar container
 * @param group The CometChat Group object
 * @param placeholder Optional placeholder painter to show while image is loading
 * @param style Styling configuration for the avatar
 *
 * @sample
 * ```
 * CometChatAvatar(
 *     group = currentGroup,
 *     style = AvatarStyle.default()
 * )
 * ```
 */
@Composable
fun CometChatAvatar(
    modifier: Modifier = Modifier,
    group: Group,
    placeholder: Painter? = null,
    style: AvatarStyle = AvatarStyle.default()
) {
    CometChatAvatar(
        modifier = modifier,
        name = group.name ?: "",
        avatarUrl = group.icon,
        placeholder = placeholder,
        style = style
    )
}

/**
 * Generates initials from a name string.
 * 
 * Rules:
 * - If name contains only emojis, returns the first emoji
 * - If name has 2+ words, returns first character of first two words
 * - If name has 1 word, returns first two characters
 * 
 * @param name The name to generate initials from
 * @return A string containing the initials (1-2 characters)
 */
private fun getInitials(name: String): String {
    if (name.isEmpty()) return ""
    
    val trimmedName = name.trim()
    
    // Check if name contains only emojis
    if (containsOnlyEmojis(trimmedName)) {
        return getFirstCodePoint(trimmedName)
    }
    
    // Split name by whitespace
    val nameParts = trimmedName.split("\\s+".toRegex())
    
    return when {
        nameParts.size >= 2 -> {
            // Get first character from first two parts
            getFirstCodePoint(nameParts[0]) + getFirstCodePoint(nameParts[1])
        }
        else -> {
            // Get first two characters from the single part
            getFirstCodePoint(nameParts[0]) + getNextCodePoint(nameParts[0], 1)
        }
    }
}

/**
 * Checks if a string contains only emoji characters.
 * 
 * @param input The string to check
 * @return true if the string contains only emojis, false otherwise
 */
private fun containsOnlyEmojis(input: String): Boolean {
    if (input.isEmpty()) return false
    
    var i = 0
    while (i < input.length) {
        val codePoint = input.codePointAt(i)
        if (!isEmoji(codePoint)) {
            return false
        }
        i += Character.charCount(codePoint)
    }
    return true
}

/**
 * Gets the first code point (character) from a string.
 * Properly handles Unicode characters including emojis.
 * 
 * @param input The string to extract from
 * @return A string containing the first character
 */
private fun getFirstCodePoint(input: String): String {
    if (input.isEmpty()) return ""
    val firstCodePoint = input.codePointAt(0)
    return String(Character.toChars(firstCodePoint))
}

/**
 * Gets the code point at a specific position in the string.
 * Properly handles Unicode characters including emojis.
 * 
 * @param input The string to extract from
 * @param count The position of the code point to retrieve (0-indexed)
 * @return A string containing the character at the specified position, or empty string if out of bounds
 */
private fun getNextCodePoint(input: String, count: Int): String {
    if (input.isEmpty()) return ""
    
    var codePointIndex = 0
    for (i in 0 until count) {
        codePointIndex = input.offsetByCodePoints(codePointIndex, 1)
        if (codePointIndex >= input.length) {
            break
        }
    }
    
    return if (codePointIndex < input.length) {
        val nextCodePoint = input.codePointAt(codePointIndex)
        String(Character.toChars(nextCodePoint))
    } else {
        ""
    }
}

/**
 * Checks if a code point represents an emoji character.
 * 
 * @param codePoint The Unicode code point to check
 * @return true if the code point is an emoji, false otherwise
 */
private fun isEmoji(codePoint: Int): Boolean {
    return (codePoint in 0x1F600..0x1F64F) || // Emoticons
            (codePoint in 0x1F300..0x1F5FF) || // Misc Symbols and Pictographs
            (codePoint in 0x1F680..0x1F6FF) || // Transport and Map
            (codePoint in 0x2600..0x26FF) ||   // Misc symbols
            (codePoint in 0x2700..0x27BF) ||   // Dingbats
            (codePoint in 0xFE00..0xFE0F) ||   // Variation Selectors
            (codePoint in 0x1F900..0x1F9FF) || // Supplemental Symbols and Pictographs
            (codePoint in 0x1FA70..0x1FAFF)    // Symbols and Pictographs Extended-A
}
