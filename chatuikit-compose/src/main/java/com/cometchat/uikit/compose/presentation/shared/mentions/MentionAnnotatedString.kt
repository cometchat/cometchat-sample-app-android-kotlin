package com.cometchat.uikit.compose.presentation.shared.mentions

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import java.util.regex.Pattern

/**
 * Represents a mention range within text.
 *
 * @property start The start index of the mention in the text
 * @property end The end index of the mention in the text
 * @property userId The user ID associated with this mention
 * @property userName The display name of the mentioned user
 * @property isSelf Whether this mention refers to the current logged-in user
 * @property isMentionAll Whether this is a "mention all" (@all) mention
 */
data class MentionRange(
    val start: Int,
    val end: Int,
    val userId: String,
    val userName: String,
    val isSelf: Boolean = false,
    val isMentionAll: Boolean = false
)

/**
 * Result of parsing mentions from text.
 *
 * @property displayText The text with mention patterns replaced by display names
 * @property mentions List of MentionRange objects representing each mention
 */
data class ParsedMentionsResult(
    val displayText: String,
    val mentions: List<MentionRange>
)

/**
 * Builds an AnnotatedString with styled mentions.
 *
 * This function takes plain text and a list of mention ranges, then creates
 * an AnnotatedString with the appropriate styling and annotations for each mention.
 * The annotations can be used for click handling via ClickableText.
 *
 * @param text The text to annotate
 * @param mentions List of MentionRange objects representing mentions in the text
 * @param mentionStyle The SpanStyle to apply to mention text
 * @param selfMentionStyle Optional SpanStyle for self-mentions (defaults to mentionStyle if null)
 * @return AnnotatedString with styled mentions and "MENTION" annotations
 */
fun buildMentionAnnotatedString(
    text: String,
    mentions: List<MentionRange>,
    mentionStyle: SpanStyle,
    selfMentionStyle: SpanStyle? = null
): AnnotatedString {
    if (mentions.isEmpty()) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        append(text)
        mentions.forEach { mention ->
            // Validate bounds
            if (mention.start >= 0 && mention.end <= text.length && mention.start < mention.end) {
                // Apply style based on whether it's a self-mention
                val style = if (mention.isSelf && selfMentionStyle != null) {
                    selfMentionStyle
                } else {
                    mentionStyle
                }
                addStyle(style, mention.start, mention.end)
                
                // Add annotation for click handling
                // Use "MENTION_ALL" tag for mention all, "MENTION" for regular mentions
                val tag = if (mention.isMentionAll) MENTION_ALL_TAG else MENTION_TAG
                addStringAnnotation(
                    tag = tag,
                    annotation = mention.userId,
                    start = mention.start,
                    end = mention.end
                )
            }
        }
    }
}

/**
 * Parses mention patterns from text and returns the display text with mention ranges.
 *
 * This function recognizes two mention patterns:
 * - User mentions: `<@uid:userId>` - replaced with `@userName`
 * - Mention all: `<@all:all>` - replaced with `@mentionAllLabel`
 *
 * @param text The text containing mention patterns
 * @param mentionedUsers Map of user IDs to user names for resolving mentions
 * @param loggedInUserId The current logged-in user's ID (for identifying self-mentions)
 * @param mentionAllId The ID used for mention all (default: "all")
 * @param mentionAllLabel The display label for mention all (default: "all")
 * @param trackingCharacter The character used for mentions (default: '@')
 * @return ParsedMentionsResult containing the display text and list of MentionRange objects
 */
fun parseMentionsFromText(
    text: String,
    mentionedUsers: Map<String, String>,
    loggedInUserId: String? = null,
    mentionAllId: String = "all",
    mentionAllLabel: String = "all",
    trackingCharacter: Char = '@'
): ParsedMentionsResult {
    if (text.isEmpty()) {
        return ParsedMentionsResult(text, emptyList())
    }

    val mentions = mutableListOf<MentionRange>()
    val resultText = StringBuilder()
    
    // Patterns for mention detection
    val userMentionPattern = Pattern.compile("<$trackingCharacter" + "uid:(.*?)>")
    val mentionAllPattern = Pattern.compile("<$trackingCharacter" + "all:$mentionAllId>")
    
    // Collect all matches with their positions
    data class MatchInfo(
        val start: Int,
        val end: Int,
        val userId: String,
        val userName: String,
        val isMentionAll: Boolean
    )
    
    val allMatches = mutableListOf<MatchInfo>()
    
    // Find mention all patterns
    val mentionAllMatcher = mentionAllPattern.matcher(text)
    while (mentionAllMatcher.find()) {
        allMatches.add(MatchInfo(
            start = mentionAllMatcher.start(),
            end = mentionAllMatcher.end(),
            userId = mentionAllId,
            userName = "$trackingCharacter$mentionAllLabel",
            isMentionAll = true
        ))
    }
    
    // Find user mention patterns
    val userMatcher = userMentionPattern.matcher(text)
    while (userMatcher.find()) {
        val userId = userMatcher.group(1) ?: continue
        val userName = mentionedUsers[userId] ?: continue
        allMatches.add(MatchInfo(
            start = userMatcher.start(),
            end = userMatcher.end(),
            userId = userId,
            userName = "$trackingCharacter$userName",
            isMentionAll = false
        ))
    }
    
    // Sort matches by start position
    allMatches.sortBy { it.start }
    
    // Build result text and mention ranges
    var lastEnd = 0
    for (match in allMatches) {
        // Skip overlapping matches
        if (match.start < lastEnd) continue
        
        // Append text before this match
        if (match.start > lastEnd) {
            resultText.append(text.substring(lastEnd, match.start))
        }
        
        // Record the start position in the result text
        val mentionStart = resultText.length
        
        // Append the display name
        resultText.append(match.userName)
        
        // Record the end position in the result text
        val mentionEnd = resultText.length
        
        // Create MentionRange
        mentions.add(MentionRange(
            start = mentionStart,
            end = mentionEnd,
            userId = match.userId,
            userName = match.userName,
            isSelf = match.userId == loggedInUserId,
            isMentionAll = match.isMentionAll
        ))
        
        lastEnd = match.end
    }
    
    // Append remaining text
    if (lastEnd < text.length) {
        resultText.append(text.substring(lastEnd))
    }
    
    return ParsedMentionsResult(resultText.toString(), mentions)
}

/**
 * Annotation tag used for regular user mentions.
 */
const val MENTION_TAG = "MENTION"

/**
 * Annotation tag used for "mention all" mentions.
 */
const val MENTION_ALL_TAG = "MENTION_ALL"
