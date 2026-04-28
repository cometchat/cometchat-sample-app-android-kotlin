package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import android.graphics.Rect as AndroidRect
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import com.cometchat.uikit.core.formatter.RichTextFormat

/**
 * Custom [TextToolbar] that shows the default Cut/Copy/Paste/Select All items
 * plus rich text formatting options (Bold, Italic, Strikethrough, Code).
 *
 * Uses [ActionMode.Callback2] with [onGetContentRect] so the floating menu
 * positions itself directly above the selected text.
 */
internal class RichTextSelectionToolbar(
    private val view: View,
    private val onFormatClick: (RichTextFormat) -> Unit
) : TextToolbar {

    private var actionMode: ActionMode? = null
    private var selectionRect: Rect = Rect.Zero
    private var onCutRequested: (() -> Unit)? = null
    private var onCopyRequested: (() -> Unit)? = null
    private var onPasteRequested: (() -> Unit)? = null
    private var onSelectAllRequested: (() -> Unit)? = null

    override val status: TextToolbarStatus
        get() = if (actionMode != null) TextToolbarStatus.Shown else TextToolbarStatus.Hidden

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        this.selectionRect = rect
        this.onCutRequested = onCutRequested
        this.onCopyRequested = onCopyRequested
        this.onPasteRequested = onPasteRequested
        this.onSelectAllRequested = onSelectAllRequested

        if (actionMode != null) {
            actionMode?.invalidateContentRect()
            return
        }

        actionMode = view.startActionMode(
            object : ActionMode.Callback2() {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    // Default system items first
                    if (onCutRequested != null) menu.add(0, MENU_CUT, 0, android.R.string.cut)
                    if (onCopyRequested != null) menu.add(0, MENU_COPY, 1, android.R.string.copy)
                    if (onPasteRequested != null) menu.add(0, MENU_PASTE, 2, android.R.string.paste)
                    if (onSelectAllRequested != null) menu.add(0, MENU_SELECT_ALL, 3, android.R.string.selectAll)
                    // Formatting items after defaults
                    menu.add(0, MENU_BOLD, 10, "Bold")
                    menu.add(0, MENU_ITALIC, 11, "Italic")
                    menu.add(0, MENU_STRIKETHROUGH, 12, "Strikethrough")
                    menu.add(0, MENU_INLINE_CODE, 13, "Code")
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                    when (item.itemId) {
                        MENU_CUT -> this@RichTextSelectionToolbar.onCutRequested?.invoke()
                        MENU_COPY -> this@RichTextSelectionToolbar.onCopyRequested?.invoke()
                        MENU_PASTE -> this@RichTextSelectionToolbar.onPasteRequested?.invoke()
                        MENU_SELECT_ALL -> this@RichTextSelectionToolbar.onSelectAllRequested?.invoke()
                        MENU_BOLD -> onFormatClick(RichTextFormat.BOLD)
                        MENU_ITALIC -> onFormatClick(RichTextFormat.ITALIC)
                        MENU_STRIKETHROUGH -> onFormatClick(RichTextFormat.STRIKETHROUGH)
                        MENU_INLINE_CODE -> onFormatClick(RichTextFormat.INLINE_CODE)
                        else -> return false
                    }
                    mode.finish()
                    return true
                }

                override fun onDestroyActionMode(mode: ActionMode) {
                    actionMode = null
                }

                override fun onGetContentRect(mode: ActionMode, view: View, outRect: AndroidRect) {
                    val r = selectionRect
                    outRect.set(r.left.toInt(), r.top.toInt(), r.right.toInt(), r.bottom.toInt())
                }
            },
            ActionMode.TYPE_FLOATING
        )
    }

    override fun hide() {
        actionMode?.finish()
        actionMode = null
    }

    private companion object {
        const val MENU_CUT = 0
        const val MENU_COPY = 1
        const val MENU_PASTE = 2
        const val MENU_SELECT_ALL = 3
        const val MENU_BOLD = 100
        const val MENU_ITALIC = 101
        const val MENU_STRIKETHROUGH = 102
        const val MENU_INLINE_CODE = 103
    }
}
