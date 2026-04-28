package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.aiassistantbubble

import io.noties.prism4j.Prism4j

/**
 * Simplified GrammarLocator for Prism4j syntax highlighting.
 *
 * Returns null for all languages — Prism4j handles this gracefully
 * by rendering code blocks without syntax highlighting when no grammar
 * is available. This avoids bundling custom language definitions while
 * still supporting the SyntaxHighlightPlugin pipeline.
 */
class GrammarLocator : io.noties.prism4j.GrammarLocator {

    override fun grammar(prism4j: Prism4j, language: String): Prism4j.Grammar? = null

    override fun languages(): Set<String> = emptySet()
}
