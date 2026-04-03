package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatPollBubbleStyle
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.models.PollData
import com.cometchat.uikit.core.models.PollOption
import com.cometchat.uikit.core.models.VoterInfo
import com.cometchat.uikit.core.models.calculateVotePercentage
import com.cometchat.uikit.core.models.extractPollData
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * A composable that displays an interactive poll message bubble.
 *
 * This component renders poll messages with:
 * - Poll question as the title
 * - List of poll options with radio buttons or progress bars
 * - Vote counts and percentages for each option
 * - Highlighting of the user's selected option
 *
 * The poll displays differently based on whether the user has voted:
 * - **Before voting**: Shows unselected radio buttons for each option
 * - **After voting**: Shows progress bars with vote percentages and counts
 *
 * Example usage:
 * ```kotlin
 * CometChatPollBubble(
 *     message = customMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatPollBubbleStyle.incoming(),
 *     onVote = { optionIndex -> /* Handle vote */ }
 * )
 * ```
 *
 * @param message The [CustomMessage] containing poll data
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance. Since CometChatPollBubbleStyle
 *              extends CometChatMessageBubbleStyle, all wrapper properties (backgroundColor,
 *              cornerRadius, strokeWidth, strokeColor) are directly accessible on the style.
 * @param onVote Callback when a poll option is clicked with (CustomMessage, optionText, position).
 *               If null, the default behavior submits the vote to CometChat.
 */
@Composable
fun CometChatPollBubble(
    message: CustomMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatPollBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatPollBubbleStyle.outgoing()
        else -> CometChatPollBubbleStyle.incoming()
    },
    onVote: ((CustomMessage, String, Int) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val pollData = remember(message.id, message.updatedAt) {
        extractPollData(message)
    }
    
    val scope = rememberCoroutineScope()
    
    // Default vote behavior: submit vote to CometChat
    val defaultOnVote: (CustomMessage, String, Int) -> Unit = { msg, _, position ->
        scope.launch {
            try {
                // Extract poll ID from customData, fallback to message ID
                val pollId = msg.customData?.optString("id")?.takeIf { it.isNotEmpty() }
                    ?: msg.id.toString()
                
                // Construct vote payload
                val votePayload = JSONObject().apply {
                    put("vote", position)
                    put("id", pollId)
                }
                
                // Submit vote via CometChat extension API
                CometChat.callExtension(
                    "polls",
                    "POST",
                    "/v2/vote",
                    votePayload,
                    object : CometChat.CallbackListener<JSONObject>() {
                        override fun onSuccess(response: JSONObject?) {
                            // Vote submitted successfully
                        }
                        
                        override fun onError(e: CometChatException?) {
                            // Log error but don't crash
                            Log.e("CometChatPollBubble", "Failed to submit vote: ${e?.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("CometChatPollBubble", "Error submitting vote: ${e.message}")
            }
        }
    }
    
    // Use provided callback or default
    val effectiveOnVote = onVote ?: defaultOnVote

    if (pollData != null) {
        CometChatPollBubbleContent(
            message = message,
            pollData = pollData,
            modifier = modifier,
            style = style,
            onVote = effectiveOnVote,
            onLongClick = onLongClick
        )
    } else {
        // Fallback for invalid poll data
        PollErrorBubble(
            modifier = modifier,
            style = style
        )
    }
}

/**
 * Overload for displaying poll bubble with direct PollData.
 *
 * @param pollData The poll data containing question, options, and vote information
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance. Since CometChatPollBubbleStyle
 *              extends CometChatMessageBubbleStyle, all wrapper properties (backgroundColor,
 *              cornerRadius, strokeWidth, strokeColor) are directly accessible on the style.
 * @param onVote Callback when a poll option is clicked with the option index (1-indexed)
 */
@Composable
fun CometChatPollBubble(
    pollData: PollData,
    modifier: Modifier = Modifier,
    style: CometChatPollBubbleStyle = CometChatPollBubbleStyle.default(),
    onVote: ((Int) -> Unit)? = null
) {
    val shape = RoundedCornerShape(style.cornerRadius)
    
    // Determine if user has voted (any option is selected)
    val hasUserVoted = remember(pollData) {
        pollData.options.any { it.isSelected }
    }

    Column(
        modifier = modifier
            .width(240.dp)
            .padding(start = 12.dp, top = 12.dp, end = 12.dp)
            .semantics {
                contentDescription = "Poll: ${pollData.question}"
            }
    ) {
        // Poll question (title)
        Text(
            text = pollData.question,
            style = style.titleTextStyle,
            color = style.titleTextColor,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Poll options
        pollData.options.forEachIndexed { index, option ->
            PollOptionItem(
                option = option,
                totalVotes = pollData.totalVotes,
                hasUserVoted = hasUserVoted,
                style = style,
                onClick = {
                    // Pass 1-indexed position to match the API expectation
                    onVote?.invoke(index + 1)
                }
            )
            
            if (index < pollData.options.size - 1) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Internal composable for displaying poll bubble with message context.
 * This is used when we have a CustomMessage and need to support the full callback signature.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CometChatPollBubbleContent(
    message: CustomMessage,
    pollData: PollData,
    modifier: Modifier = Modifier,
    style: CometChatPollBubbleStyle = CometChatPollBubbleStyle.default(),
    onVote: (CustomMessage, String, Int) -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(style.cornerRadius)
    
    // Determine if user has voted (any option is selected)
    val hasUserVoted = remember(pollData) {
        pollData.options.any { it.isSelected }
    }
    
    // Track the user's current selected option position
    val currentSelectedPosition = remember(pollData) {
        pollData.options.indexOfFirst { it.isSelected }
    }
    
    // Track loading state for each option
    var loadingOptionIndex by remember { mutableStateOf<Int?>(null) }

    // Reset loading state when poll data updates (vote was processed)
    LaunchedEffect(pollData) {
        loadingOptionIndex = null
    }

    Column(
        modifier = modifier
            .width(240.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = onLongClick
            )
            .padding(start = 12.dp, top = 12.dp, end = 12.dp)
            .semantics {
                contentDescription = "Poll: ${pollData.question}"
            }
    ) {
        // Poll question (title)
        Text(
            text = pollData.question,
            style = style.titleTextStyle,
            color = style.titleTextColor,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Poll options
        pollData.options.forEachIndexed { index, option ->
            PollOptionItem(
                option = option,
                totalVotes = pollData.totalVotes,
                hasUserVoted = hasUserVoted,
                isLoading = loadingOptionIndex == index,
                style = style,
                onClick = {
                    // Allow voting if not loading and either:
                    // 1. User hasn't voted yet, OR
                    // 2. User is clicking a different option than currently selected (re-voting)
                    if (loadingOptionIndex == null && currentSelectedPosition != index) {
                        loadingOptionIndex = index
                        // Pass 1-indexed position to match the API expectation
                        onVote(message, option.text, index + 1)
                    }
                }
            )
            
            if (index < pollData.options.size - 1) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * A composable that displays a single poll option item.
 *
 * This component renders differently based on whether the user has voted:
 * - **Before voting**: Shows a radio button (unselected) with option text
 * - **After voting**: Shows a progress bar with percentage and vote count
 *
 * @param option The poll option data
 * @param totalVotes The total number of votes in the poll
 * @param hasUserVoted Whether the logged-in user has voted
 * @param isLoading Whether this option is currently being voted on
 * @param style Style configuration for the option appearance
 * @param onClick Callback when the option is clicked
 */
@Composable
internal fun PollOptionItem(
    option: PollOption,
    totalVotes: Int,
    hasUserVoted: Boolean,
    isLoading: Boolean = false,
    style: CometChatPollBubbleStyle,
    onClick: () -> Unit
) {
    val percentage = remember(option.voteCount, totalVotes) {
        calculateVotePercentage(option.voteCount, totalVotes)
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (hasUserVoted || option.voteCount > 0) percentage / 100f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            // Allow clicking when not loading - user can re-vote on different options
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isLoading
            ) { onClick() }
            .semantics {
                contentDescription = buildString {
                    append("Option: ${option.text}")
                    if (hasUserVoted || option.voteCount > 0) {
                        append(", $percentage percent, ${option.voteCount} votes")
                        if (option.isSelected) {
                            append(", your vote")
                        }
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio button indicator or loading indicator
            PollRadioButton(
                isSelected = option.isSelected,
                hasUserVoted = hasUserVoted,
                isLoading = isLoading,
                style = style
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Option text
            Text(
                text = option.text,
                style = style.optionTextStyle,
                color = style.optionTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Voter avatars and count (shown when there are votes)
            if (option.voteCount > 0) {
                VoterAvatarsRow(
                    voters = option.voters,
                    voteCount = option.voteCount,
                    style = style
                )
            }
        }

        // Custom rounded progress bar (shown when there are votes or user has voted)
        if (hasUserVoted || option.voteCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            val progressColor = if (option.isSelected) style.progressColor
                else style.progressColor.copy(alpha = 0.6f)
            val backgroundColor = style.progressBackgroundColor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp) // Align with text after radio button
                    .height(8.dp)
                    .drawBehind {
                        val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        // Draw background
                        drawRoundRect(
                            color = backgroundColor,
                            size = size,
                            cornerRadius = cornerRadius
                        )
                        // Draw progress
                        if (animatedProgress > 0f) {
                            drawRoundRect(
                                color = progressColor,
                                size = Size(size.width * animatedProgress, size.height),
                                cornerRadius = cornerRadius
                            )
                        }
                    }
            )
        }
    }
}

/**
 * A composable that displays overlapping voter avatars with a vote count.
 *
 * Shows up to 3 voter avatars with an overlapping effect (-8dp offset per avatar)
 * and the total vote count next to them.
 *
 * @param voters List of voter information (up to 3 displayed)
 * @param voteCount Total vote count for this option
 * @param style Style configuration for the avatars
 * @param modifier Modifier for the row container
 */
@Composable
internal fun VoterAvatarsRow(
    voters: List<VoterInfo>,
    voteCount: Int,
    style: CometChatPollBubbleStyle,
    modifier: Modifier = Modifier
) {
    if (voteCount == 0) return
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display up to 3 avatars with overlapping effect
        val displayVoters = voters.take(3)
        val avatarSize = 20.dp
        val overlapOffset = 8.dp
        
        // Calculate total width needed for overlapping avatars
        val totalAvatarsWidth = if (displayVoters.isNotEmpty()) {
            avatarSize + (overlapOffset * (displayVoters.size - 1).coerceAtLeast(0))
        } else {
            0.dp
        }
        
        Box(
            modifier = Modifier.width(totalAvatarsWidth)
        ) {
            displayVoters.forEachIndexed { index, voter ->
                CometChatAvatar(
                    name = voter.name,
                    avatarUrl = voter.avatarUrl,
                    modifier = Modifier
                        .size(avatarSize)
                        .offset(x = overlapOffset * index)
                        .zIndex((displayVoters.size - index).toFloat()),
                    style = style.optionAvatarStyle
                )
            }
        }
        
        // Vote count text
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = voteCount.toString(),
            style = style.voteCountTextStyle,
            color = style.voteCountTextColor
        )
    }
}

/**
 * A composable that displays a radio button indicator for poll options.
 *
 * For selected state, displays the filled check icon (circle with checkmark).
 * For unselected state, displays a bordered circle.
 *
 * @param isSelected Whether this option is selected by the user
 * @param hasUserVoted Whether the user has voted
 * @param isLoading Whether this option is currently being voted on
 * @param style Style configuration for the radio button appearance
 */
@Composable
private fun PollRadioButton(
    isSelected: Boolean,
    hasUserVoted: Boolean,
    isLoading: Boolean = false,
    style: CometChatPollBubbleStyle
) {
    Box(
        modifier = Modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // Show loading indicator when voting
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = style.progressIndeterminateTint
            )
        } else if (isSelected && hasUserVoted) {
            // Selected state: Show filled check icon (circle with checkmark)
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_filled_check),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = style.selectedIconTint
            )
        } else {
            // Unselected state: Show bordered circle
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(
                        width = style.unselectedRadioButtonStrokeWidth,
                        color = style.unselectedRadioButtonStrokeColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * A composable that displays an error state when poll data cannot be parsed.
 *
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance
 */
@Composable
private fun PollErrorBubble(
    modifier: Modifier = Modifier,
    style: CometChatPollBubbleStyle
) {
    Box(
        modifier = modifier
            .width(240.dp)
            .padding(start = 12.dp, top = 12.dp, end = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.cometchat_unable_to_load_poll),
            style = style.optionTextStyle,
            color = style.optionTextColor.copy(alpha = 0.6f)
        )
    }
}

/**
 * Overload for displaying poll bubble with direct parameters.
 *
 * This is useful for previews and testing where you want to provide
 * poll data directly without a CustomMessage.
 *
 * @param question The poll question text
 * @param options List of poll options with their data
 * @param totalVotes The total number of votes in the poll
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance. Since CometChatPollBubbleStyle
 *              extends CometChatMessageBubbleStyle, all wrapper properties (backgroundColor,
 *              cornerRadius, strokeWidth, strokeColor) are directly accessible on the style.
 * @param onVote Callback when a poll option is clicked with the option index (1-indexed)
 */
@Composable
fun CometChatPollBubble(
    question: String,
    options: List<PollOption>,
    totalVotes: Int,
    modifier: Modifier = Modifier,
    style: CometChatPollBubbleStyle = CometChatPollBubbleStyle.default(),
    onVote: ((Int) -> Unit)? = null
) {
    CometChatPollBubble(
        pollData = PollData(
            id = "",
            question = question,
            options = options,
            totalVotes = totalVotes
        ),
        modifier = modifier,
        style = style,
        onVote = onVote
    )
}
