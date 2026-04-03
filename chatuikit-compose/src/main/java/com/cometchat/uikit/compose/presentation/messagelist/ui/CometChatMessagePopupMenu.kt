package com.cometchat.uikit.compose.presentation.messagelist.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.emojikeyboard.style.CometChatEmojiKeyboardStyle
import com.cometchat.uikit.compose.presentation.emojikeyboard.ui.CometChatEmojiKeyboard
import com.cometchat.uikit.compose.presentation.messagelist.style.CometChatMessagePopupMenuStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.CometChatMessageOption

/**
 * Default quick reaction emojis displayed in the reactions bar.
 */
private val DEFAULT_REACTIONS = listOf("😍", "👍🏻", "🔥", "😊", "❤️")

/**
 * Captures the current activity's window content as a [Bitmap].
 * Uses the same approach as chatuikit-kotlin: decorView.draw(canvas).
 * Returns null if the activity is not available or capture fails.
 */
private fun captureScreenBitmap(activity: Activity): Bitmap? {
    return try {
        val decorView = activity.window.decorView
        val width = decorView.width
        val height = decorView.height
        if (width <= 0 || height <= 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        decorView.draw(canvas)
        bitmap
    } catch (_: Exception) {
        null
    }
}

/**
 * Applies a blur effect to a bitmap using RenderScript.
 * Applies multiple passes to achieve stronger blur (RenderScript max radius is 25f).
 * Returns a new blurred bitmap or null if blur fails.
 */
@Suppress("DEPRECATION")
private fun applyRenderScriptBlur(context: android.content.Context, sourceBitmap: Bitmap, passes: Int = 3): Bitmap? {
    return try {
        // Create a mutable copy to work with
        var bitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val rs = android.renderscript.RenderScript.create(context)
        
        // Apply multiple blur passes for stronger effect
        repeat(passes) {
            val input = android.renderscript.Allocation.createFromBitmap(rs, bitmap)
            val output = android.renderscript.Allocation.createTyped(rs, input.type)
            val script = android.renderscript.ScriptIntrinsicBlur.create(
                rs, android.renderscript.Element.U8_4(rs)
            )
            script.setRadius(25f) // Max radius
            script.setInput(input)
            script.forEach(output)
            output.copyTo(bitmap)
            input.destroy()
            output.destroy()
            script.destroy()
        }
        
        rs.destroy()
        bitmap
    } catch (_: Exception) {
        null
    }
}


/**
 * Full-screen popup overlay composable for message options and quick reactions.
 *
 * Displayed on long-press of a message bubble, this composable renders:
 * 1. A blurred/dimmed background overlay
 * 2. A quick reactions row with emoji chips and an "add reaction" button
 * 3. A read-only message bubble preview (via [messageBubbleContent] slot)
 * 4. A scrollable option list card
 *
 * Content is aligned to start for incoming messages and to end for outgoing messages,
 * determined by comparing [message] sender UID with the logged-in user and [messageAlignment].
 *
 * @param message The message being acted upon
 * @param menuItems List of [CometChatMessageOption] items to display in the option list
 * @param quickReactions List of emoji strings for the quick reactions bar
 * @param showQuickReactions Whether to show the quick reactions row
 * @param messageAlignment Controls alignment mode (STANDARD or LEFT_ALIGNED)
 * @param onOptionClick Callback invoked when an option is tapped
 * @param onReactionClick Callback invoked when a quick reaction emoji is tapped
 * @param onEmojiPickerClick Callback invoked when the "add reaction" button is tapped
 * @param onDismiss Callback invoked when the overlay should be dismissed (tap outside)
 * @param style Style configuration for the option list card
 * @param messageBubbleContent Composable slot for the read-only message bubble preview
 */
@Composable
fun CometChatMessagePopupMenu(
    message: BaseMessage,
    menuItems: List<CometChatMessageOption>,
    quickReactions: List<String> = DEFAULT_REACTIONS,
    showQuickReactions: Boolean = true,
    messageAlignment: UIKitConstants.MessageListAlignment = UIKitConstants.MessageListAlignment.STANDARD,
    onOptionClick: (CometChatMessageOption) -> Unit,
    onReactionClick: (String) -> Unit,
    onEmojiPickerClick: () -> Unit,
    onDismiss: () -> Unit,
    style: CometChatMessagePopupMenuStyle = CometChatMessagePopupMenuStyle.default(),
    messageBubbleContent: @Composable () -> Unit
) {
    // Determine alignment: left for incoming or LEFT_ALIGNED mode
    val isLeft = message.sender?.uid != CometChatUIKit.getLoggedInUser()?.uid
        || messageAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED

    // Effective reactions list (fall back to defaults when empty)
    val effectiveReactions = quickReactions.ifEmpty { DEFAULT_REACTIONS }

    // Animation state
    var visible by remember { mutableStateOf(false) }
    var showEmojiKeyboard by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val animationSpec = if (visible) {
        tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
    } else {
        tween<Float>(durationMillis = 200, easing = FastOutLinearInEasing)
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = animationSpec,
        label = "popup_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = animationSpec,
        label = "popup_alpha"
    )

    // Capture the screen bitmap before showing the dialog
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current
    
    // Capture screen bitmap synchronously before dialog shows
    val screenBitmap = remember { 
        activity?.let { act ->
            try {
                val decorView = act.window.decorView
                if (decorView.width > 0 && decorView.height > 0) {
                    val bitmap = Bitmap.createBitmap(decorView.width, decorView.height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    decorView.draw(canvas)
                    bitmap
                } else null
            } catch (_: Exception) { null }
        }
    }
    
    // For pre-S devices, pre-blur the bitmap using RenderScript
    // For S+ devices, we'll use Modifier.blur() which is more reliable
    val displayBitmap = remember(screenBitmap) {
        if (screenBitmap != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            applyRenderScriptBlur(context, screenBitmap, passes = 3) ?: screenBitmap
        } else {
            screenBitmap
        }
    }

    // Full-screen Dialog overlay — renders above the entire window (header, composer, etc.)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cometchat_message_popup_menu_overlay")
        ) {
            // Blurred background — screenshot with blur applied
            if (displayBitmap != null) {
                Image(
                    bitmap = displayBitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                // Use Compose blur modifier for API 31+
                                Modifier.blur(radius = 25.dp)
                            } else {
                                // Pre-S: bitmap is already blurred via RenderScript
                                Modifier
                            }
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onDismiss() }
                )
            } else {
                // Fallback when screenshot capture fails — simple dim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onDismiss() }
                )
            }

            // Content column with animation — vertically centered like the XML reference
            Column(
                modifier = Modifier
                    .align(
                        if (showEmojiKeyboard) Alignment.Center
                        else if (isLeft) Alignment.CenterStart
                        else Alignment.CenterEnd
                    )
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                horizontalAlignment = if (showEmojiKeyboard) Alignment.CenterHorizontally
                    else if (isLeft) Alignment.Start
                    else Alignment.End
            ) {
                if (showEmojiKeyboard) {
                    // Full emoji keyboard — shown when "add reaction" is tapped
                    // Centered in the overlay, matching the existing BottomSheetDialogFragment behavior
                    CometChatEmojiKeyboard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        style = CometChatEmojiKeyboardStyle.default(
                            backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
                            cornerRadius = 16.dp
                        ),
                        onClick = { emoji ->
                            onReactionClick(emoji)
                            onDismiss()
                        }
                    )
                } else {
                // Quick reactions row wrapped in a card (matches XML reaction_card)
                if (showQuickReactions) {
                    QuickReactionsCard(
                        reactions = effectiveReactions,
                        onReactionClick = onReactionClick,
                        onEmojiPickerClick = { showEmojiKeyboard = true },
                        isLeft = isLeft
                    )
                }

            Spacer(modifier = Modifier.height(8.dp))

            // Message bubble preview (read-only)
            Box(
                modifier = Modifier
                    .heightIn(max = 350.dp)
                    .testTag("cometchat_message_popup_menu_preview")
            ) {
                messageBubbleContent()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Option list card
            if (menuItems.isNotEmpty()) {
                OptionListCard(
                    menuItems = menuItems,
                    onOptionClick = onOptionClick,
                    style = style
                )
            }
                } // end else (not showing emoji keyboard)
        }
    }
    } // end Dialog
}


/**
 * Card container wrapping the horizontal row of quick reaction emoji chips
 * and an "add reaction" button. Matches the XML reference's reaction_card
 * MaterialCardView with rounded corners containing a HorizontalScrollView.
 * Each chip animates in with a staggered delay for a cascading entrance effect.
 */
@Composable
private fun QuickReactionsCard(
    reactions: List<String>,
    onReactionClick: (String) -> Unit,
    onEmojiPickerClick: () -> Unit,
    isLeft: Boolean
) {
    val cardShape = RoundedCornerShape(50)

    // Card-level entrance animation (matches XML's translationX + alpha + scale animation)
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(reactions.size * 40L)
        cardVisible = true
    }
    val cardScale by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "reaction_card_scale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "reaction_card_alpha"
    )

    Surface(
        modifier = Modifier
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                alpha = cardAlpha
                translationX = if (cardVisible) 0f else if (isLeft) -100f else 100f
            }
            .testTag("cometchat_message_popup_menu_reactions"),
        shape = cardShape,
        color = CometChatTheme.colorScheme.backgroundColor1
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            reactions.forEachIndexed { index, emoji ->
                // Per-chip staggered animation
                var chipVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 40L)
                    chipVisible = true
                }
                val chipScale by animateFloatAsState(
                    targetValue = if (chipVisible) 1f else 0.6f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "chip_scale_$index"
                )
                val chipAlpha by animateFloatAsState(
                    targetValue = if (chipVisible) 1f else 0f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "chip_alpha_$index"
                )

                Box(
                    modifier = Modifier
                        .padding(
                            start = if (index == 0) 0.dp else 2.dp,
                            end = 2.dp
                        )
                        .graphicsLayer {
                            scaleX = chipScale
                            scaleY = chipScale
                            alpha = chipAlpha
                            translationY = if (chipVisible) 0f else 20f
                        }
                        .clip(RoundedCornerShape(50))
                        .clickable { onReactionClick(emoji) }
                        .testTag("cometchat_reaction_chip_$emoji")
                ) {
                    Text(
                        text = emoji,
                        modifier = Modifier.padding(4.dp),
                        fontSize = 18.sp
                    )
                }
            }

            // "Add reaction" icon chip
            var addVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(reactions.size * 40L)
                addVisible = true
            }
            val addScale by animateFloatAsState(
                targetValue = if (addVisible) 1f else 0.6f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "add_chip_scale"
            )
            val addAlpha by animateFloatAsState(
                targetValue = if (addVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "add_chip_alpha"
            )

            Box(
                modifier = Modifier
                    .padding(start = 2.dp)
                    .graphicsLayer {
                        scaleX = addScale
                        scaleY = addScale
                        alpha = addAlpha
                        translationY = if (addVisible) 0f else 20f
                    }
                    .clip(RoundedCornerShape(50))
                    .clickable { onEmojiPickerClick() }
                    .testTag("cometchat_add_reaction_chip")
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cometchat_add_reaction),
                    contentDescription = "Add reaction",
                    tint = CometChatTheme.colorScheme.iconTintSecondary,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

/**
 * Card containing the scrollable option list.
 */
@Composable
private fun OptionListCard(
    menuItems: List<CometChatMessageOption>,
    onOptionClick: (CometChatMessageOption) -> Unit,
    style: CometChatMessagePopupMenuStyle
) {
    val shape = RoundedCornerShape(style.cornerRadius)

    Surface(
        modifier = Modifier
            .widthIn(min = 128.dp)
            .border(
                width = style.strokeWidth,
                color = style.strokeColor,
                shape = shape
            )
            .testTag("cometchat_message_popup_menu_options"),
        shape = shape,
        color = style.backgroundColor,
        shadowElevation = style.elevation,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            menuItems.forEach { option ->
                OptionRow(
                    option = option,
                    onClick = { onOptionClick(option) },
                    style = style
                )
            }
        }
    }
}

/**
 * Single option row with optional start icon, label, and optional end icon.
 */
@Composable
private fun OptionRow(
    option: CometChatMessageOption,
    onClick: () -> Unit,
    style: CometChatMessagePopupMenuStyle
) {
    val textColor = if (option.titleColor != 0) Color(option.titleColor) else style.textColor
    val iconTint = if (option.iconTintColor != 0) Color(option.iconTintColor) else style.startIconTint

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { contentDescription = option.title },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Start icon
        if (option.icon != 0) {
            Icon(
                painter = painterResource(id = option.icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Label text
        Text(
            text = option.title,
            color = textColor,
            style = style.textStyle
        )
    }
}
