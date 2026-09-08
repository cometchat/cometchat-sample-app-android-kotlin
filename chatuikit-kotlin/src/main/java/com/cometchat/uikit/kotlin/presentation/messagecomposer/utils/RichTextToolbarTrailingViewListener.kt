package com.cometchat.uikit.kotlin.presentation.messagecomposer.utils

import android.content.Context
import android.view.View
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.formatter.ComposerInputController

/**
 * Listener for supplying a custom view at the trailing end of the message composer's rich-text
 * formatting toolbar, after the built-in formatting buttons and a UIKit-owned separator.
 *
 * Unlike [MessageComposerViewHolderListener], this hands back a live [ComposerInputController] so
 * the custom view can read and mutate the composer input (insert/replace text, toggle a built-in
 * format). Wrap multiple buttons in a single container view.
 *
 * Usage example:
 * ```kotlin
 * messageComposer.setRichTextToolbarTrailingViewListener(
 *     object : RichTextToolbarTrailingViewListener {
 *         override fun createView(
 *             context: Context, user: User?, group: Group?, input: ComposerInputController
 *         ): View = ImageButton(context).apply {
 *             setOnClickListener { input.insertAtCursor(":)") }
 *         }
 *     }
 * )
 * ```
 */
interface RichTextToolbarTrailingViewListener {
    /**
     * Creates the custom view appended to the trailing end of the rich-text toolbar.
     *
     * @param context The Android context for creating views.
     * @param user The current user being messaged, or null if messaging a group.
     * @param group The current group being messaged, or null if messaging a user.
     * @param input Live handle to read and mutate the composer input while the composer is mounted.
     * @return A View appended after the built-in formatting buttons and a UIKit-owned separator.
     */
    fun createView(context: Context, user: User?, group: Group?, input: ComposerInputController): View
}
