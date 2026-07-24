package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videosbubble

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.cometchat.chat.models.Attachment
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagesbubble.CometChatImagesBubble
import com.google.android.material.card.MaterialCardView
import java.util.Locale

/**
 * ENG-36737 — per-type multi-attachment bubble that renders every video attachment of a message as a
 * grid (same layout as [CometChatImagesBubble]) with a play badge + duration pill on each tile.
 * Duration is read on-the-fly from the video URL (iOS-compatible; no metadata) on a background
 * thread. Replaces the single CometChatVideoBubble when `enableMultipleAttachments` is on.
 */
class CometChatVideosBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CometChatImagesBubble(context, attrs, defStyleAttr) {

    init {
        setRenderAsVideo(true)
    }

    override fun decorateTile(card: MaterialCardView, imageView: ImageView, attachment: Attachment) {
        // Centered play badge (dark circle + white glyph).
        val circle = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0x73000000)
        }
        val playBadge = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(dp(40), dp(40)).apply { gravity = Gravity.CENTER }
            background = circle
        }
        val playIcon = ImageView(context).apply {
            setImageResource(R.drawable.cometchat_play_icon)
            setColorFilter(0xFFFFFFFF.toInt())
            layoutParams = FrameLayout.LayoutParams(dp(24), dp(24)).apply { gravity = Gravity.CENTER }
        }
        playBadge.addView(playIcon)
        card.addView(playBadge)

        // Duration pill (bottom-start), populated asynchronously.
        val durationView = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.START
                setMargins(dp(6), 0, 0, dp(6))
            }
            setBackgroundColor(0x8C000000.toInt())
            setPadding(dp(6), dp(2), dp(6), dp(2))
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 11f
            visibility = View.GONE
        }
        card.addView(durationView)
        loadDurationAsync(attachment.fileUrl, durationView)
    }

    private fun loadDurationAsync(url: String?, target: TextView) {
        if (url.isNullOrEmpty()) return
        Thread {
            val ms = try {
                val retriever = MediaMetadataRetriever()
                // Release in finally so a throwing setDataSource() (common for slow/expired remote
                // URLs) doesn't leak the native retriever.
                try {
                    retriever.setDataSource(url, HashMap<String, String>())
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                } finally {
                    retriever.release()
                }
            } catch (e: Exception) {
                null
            }
            if (ms != null && ms > 0) {
                post {
                    target.text = formatDuration(ms)
                    target.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }
}
