package com.cometchat.uikit.kotlin.presentation.shared.snackbar

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.snackbar.style.CometChatSnackBarStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import java.lang.ref.WeakReference

/**
 * The Views transient banner — the CometChat equivalent of the iOS `CometChatSnackBar`. Shows a
 * compact message with a trailing close (✕) button, pinned just above the composer, slides up +
 * fades in and auto-dismisses after [displayDurationMillis]. The default look is the theme's error
 * red with white text; everything is overridable through a [CometChatSnackBarStyle], the same
 * pattern as every other CometChat Views component.
 *
 * A bar already on screen is replaced by the next [show] call.
 */
object CometChatSnackBar {

    /** Seconds the bar stays on screen before auto-dismissing (milliseconds). */
    var displayDurationMillis: Long = 4000L

    private var current: WeakReference<PopupWindow>? = null

    /**
     * Shows a themed snackbar pinned just above [anchor] (typically the composer root).
     *
     * @param anchor The view the bar is pinned above; also defines its width.
     * @param message The text to show (up to two lines).
     * @param style Optional style overrides; unset properties fall back to CometChat theme tokens.
     */
    @JvmStatic
    @JvmOverloads
    fun show(
        anchor: View,
        message: CharSequence,
        style: CometChatSnackBarStyle? = null
    ) {
        val context = anchor.context
        dismissCurrent()

        val density = context.resources.displayMetrics.density
        val sidePadding = (8f * density).toInt() // p2

        val content = buildContent(context, message, style) { dismissCurrent() }

        val popupWidth = (anchor.width - sidePadding * 2).coerceAtLeast(0)
        if (popupWidth == 0) return // composer not laid out yet — nothing to anchor to

        val popup = PopupWindow(content, popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            isFocusable = false // never steal focus from the input / keyboard
            isOutsideTouchable = false
            elevation = 6f * density
        }
        current = WeakReference(popup)

        // Measure at the fixed popup width so we can offset it fully above the anchor.
        content.measure(
            View.MeasureSpec.makeMeasureSpec(popupWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val contentHeight = content.measuredHeight
        val yOffset = -(anchor.height + contentHeight + sidePadding)
        popup.showAsDropDown(anchor, sidePadding, yOffset, Gravity.START or Gravity.TOP)

        // iOS-style entrance: rise from below while fading in.
        content.alpha = 0f
        content.translationY = 12f * density
        content.animate().alpha(1f).translationY(0f).setDuration(250L).start()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (current?.get() === popup) dismissCurrent()
        }, displayDurationMillis)
    }

    /** Dismisses the bar currently on screen, if any. */
    @JvmStatic
    fun dismissCurrent() {
        current?.get()?.let { runCatching { it.dismiss() } }
        current = null
    }

    private fun buildContent(
        context: Context,
        message: CharSequence,
        style: CometChatSnackBarStyle?,
        onClose: () -> Unit
    ): View {
        val density = context.resources.displayMetrics.density
        val innerPadding = (12f * density).toInt() // p3
        val gap = (8f * density).toInt() // p2

        val backgroundColor = style?.getBackgroundColor()?.takeIf { it != 0 }
            ?: CometChatTheme.getErrorColor(context)
        val cornerRadius = style?.getCornerRadius()?.takeIf { it >= 0f } ?: (8f * density)
        val textColor = style?.getTextColor()?.takeIf { it != 0 }
            ?: CometChatTheme.getColorWhite(context)
        val closeTint = style?.getCloseIconTint()?.takeIf { it != 0 }
            ?: CometChatTheme.getColorWhite(context)
        val textAppearance = style?.getTextAppearance()?.takeIf { it != 0 }
            ?: CometChatTheme.getTextAppearanceCaption1Medium(context)

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(innerPadding, innerPadding, innerPadding, innerPadding)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                this.cornerRadius = cornerRadius
                setColor(backgroundColor)
            }
        }

        val messageView = TextView(context).apply {
            text = message
            TextViewCompat.setTextAppearance(this, textAppearance)
            setTextColor(textColor)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val closeSize = (20f * density).toInt()
        val closeView = ImageView(context).apply {
            setImageResource(R.drawable.cometchat_ic_close)
            setColorFilter(closeTint)
            contentDescription = context.getString(R.string.cometchat_close)
            layoutParams = LinearLayout.LayoutParams(closeSize, closeSize).apply {
                marginStart = gap
            }
            setOnClickListener { onClose() }
        }

        row.addView(messageView)
        row.addView(closeView)
        return row
    }
}
