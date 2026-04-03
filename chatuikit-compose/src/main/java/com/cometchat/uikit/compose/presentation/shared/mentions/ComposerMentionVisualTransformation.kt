package com.cometchat.uikit.compose.presentation.shared.mentions

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation that applies styling to mentions in a text field.
 * 
 * This transformation reads the tracked mentions from ComposeMentionInsertionState
 * and applies the appropriate SpanStyle to each mention range.
 * 
 * @param mentionInsertionState The state holder containing tracked mentions and their styles
 * @param defaultMentionStyle The default style to apply to mentions without a specific style
 */
class ComposerMentionVisualTransformation(
    private val mentionInsertionState: ComposeMentionInsertionState,
    private val defaultMentionStyle: SpanStyle
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val mentions = mentionInsertionState.getMentionsManager().getMentions()
        
        if (mentions.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        
        val styled = buildAnnotatedString {
            // Start with the original text and any existing styles
            append(text)
            
            // Apply mention styles
            val sortedMentions = mentions.sortedBy { it.spanStart }
            for (mention in sortedMentions) {
                // Validate span positions
                if (mention.spanStart >= 0 && mention.spanEnd <= rawText.length && mention.spanStart < mention.spanEnd) {
                    // Verify the text at position matches the prompt text
                    val textAtPosition = rawText.substring(mention.spanStart, mention.spanEnd)
                    val expectedText = mention.promptText.take(mention.spanEnd - mention.spanStart)
                    
                    if (textAtPosition == expectedText) {
                        addStyle(defaultMentionStyle, mention.spanStart, mention.spanEnd)
                        
                        // Add annotation for click handling
                        addStringAnnotation(
                            tag = "mention",
                            annotation = mention.id,
                            start = mention.spanStart,
                            end = mention.spanEnd
                        )
                    }
                }
            }
        }
        
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

/**
 * Combines multiple VisualTransformations into one.
 * 
 * The transformations are applied in order, with each transformation
 * receiving the output of the previous one.
 * 
 * @param transformations The list of transformations to combine
 */
class CombinedVisualTransformation(
    private val transformations: List<VisualTransformation>
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        if (transformations.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        
        var currentText = text
        var combinedMapping: OffsetMapping = OffsetMapping.Identity
        
        for (transformation in transformations) {
            if (transformation == VisualTransformation.None) continue
            
            val result = transformation.filter(currentText)
            currentText = result.text
            
            // Combine offset mappings
            val previousMapping = combinedMapping
            val newMapping = result.offsetMapping
            combinedMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return newMapping.originalToTransformed(previousMapping.originalToTransformed(offset))
                }
                override fun transformedToOriginal(offset: Int): Int {
                    return previousMapping.transformedToOriginal(newMapping.transformedToOriginal(offset))
                }
            }
        }
        
        return TransformedText(currentText, combinedMapping)
    }
}
