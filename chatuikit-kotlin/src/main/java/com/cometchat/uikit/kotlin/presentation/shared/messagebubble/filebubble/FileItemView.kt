package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.cometchat.uikit.kotlin.R

/**
 * A helper view that represents a single file item within a file bubble.
 * Displays file icon, file name, file size/extension, and download button.
 */
internal class FileItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var fileIconContainer: LinearLayout
    private lateinit var fileIconImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var subtitleTextView: TextView
    private lateinit var downloadIconImageView: ImageView

    @ColorInt private var fileIconBackgroundColor: Int = Color.WHITE
    private var fileIconCornerRadius: Int = dpToPx(4)
    private var fileIconSize: Int = dpToPx(32)
    @ColorInt private var downloadIconTint: Int = Color.GRAY
    private var showDownloadIcon: Boolean = true

    private var onDownloadClickListener: (() -> Unit)? = null

    init {
        inflateAndInitializeView()
    }

    private fun inflateAndInitializeView() {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dpToPx(8), dpToPx(12), dpToPx(8), dpToPx(12))

        // File icon container with background
        fileIconContainer = LinearLayout(context).apply {
            layoutParams = LayoutParams(fileIconSize, fileIconSize)
            gravity = Gravity.CENTER
            setBackgroundColor(fileIconBackgroundColor)
        }
        updateFileIconBackground()

        fileIconImageView = ImageView(context).apply {
            layoutParams = LayoutParams(fileIconSize - dpToPx(8), fileIconSize - dpToPx(8))
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        fileIconContainer.addView(fileIconImageView)
        addView(fileIconContainer)

        // Spacer
        addView(View(context).apply {
            layoutParams = LayoutParams(dpToPx(12), 0)
        })

        // Text container
        val textContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        titleTextView = TextView(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 14f
            setTextColor(Color.BLACK)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        textContainer.addView(titleTextView)

        subtitleTextView = TextView(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 10f
            setTextColor(Color.GRAY)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        textContainer.addView(subtitleTextView)

        addView(textContainer)

        // Spacer
        addView(View(context).apply {
            layoutParams = LayoutParams(dpToPx(8), 0)
        })

        // Download icon
        downloadIconImageView = ImageView(context).apply {
            layoutParams = LayoutParams(dpToPx(24), dpToPx(24))
            setImageResource(R.drawable.cometchat_download_icon)
            setColorFilter(downloadIconTint)
            visibility = if (showDownloadIcon) View.VISIBLE else View.GONE
            setOnClickListener { onDownloadClickListener?.invoke() }
        }
        addView(downloadIconImageView)
    }

    private fun updateFileIconBackground() {
        val drawable = GradientDrawable().apply {
            setColor(fileIconBackgroundColor)
            cornerRadius = fileIconCornerRadius.toFloat()
        }
        fileIconContainer.background = drawable
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun setFileName(name: String) {
        titleTextView.text = name
    }

    fun setFileSize(sizeInBytes: Long, fileName: String?) {
        val sizeStr = formatFileSize(sizeInBytes)
        val extension = getFileExtension(fileName)
        subtitleTextView.text = if (extension.isNotEmpty()) {
            "$sizeStr • $extension"
        } else {
            sizeStr
        }
    }

    fun setFileIcon(@DrawableRes iconRes: Int) {
        fileIconImageView.setImageResource(iconRes)
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        titleTextView.setTextColor(color)
    }

    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            titleTextView.setTextAppearance(appearance)
        }
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        subtitleTextView.setTextColor(color)
    }

    fun setSubtitleTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            subtitleTextView.setTextAppearance(appearance)
        }
    }

    fun setFileIconBackgroundColor(@ColorInt color: Int) {
        fileIconBackgroundColor = color
        updateFileIconBackground()
    }

    fun setFileIconCornerRadius(@Dimension radius: Int) {
        fileIconCornerRadius = radius
        updateFileIconBackground()
    }

    fun setFileIconSize(@Dimension size: Int) {
        fileIconSize = size
        fileIconContainer.layoutParams = LayoutParams(size, size)
        fileIconImageView.layoutParams = LayoutParams(size - dpToPx(8), size - dpToPx(8))
    }

    fun setDownloadIconTint(@ColorInt color: Int) {
        downloadIconTint = color
        downloadIconImageView.setColorFilter(color)
    }

    fun setShowDownloadIcon(show: Boolean) {
        showDownloadIcon = show
        downloadIconImageView.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setCornerRadius(radii: IntArray) {
        val drawable = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            cornerRadii = floatArrayOf(
                radii[0].toFloat(), radii[0].toFloat(), // top-left
                radii[1].toFloat(), radii[1].toFloat(), // top-right
                radii[2].toFloat(), radii[2].toFloat(), // bottom-right
                radii[3].toFloat(), radii[3].toFloat()  // bottom-left
            )
        }
        background = drawable
    }

    fun setOnDownloadClickListener(listener: (() -> Unit)?) {
        onDownloadClickListener = listener
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)

        val size = sizeInBytes / Math.pow(1024.0, index.toDouble())
        return if (size == size.toLong().toDouble()) {
            "${size.toLong()} ${units[index]}"
        } else {
            String.format("%.1f %s", size, units[index])
        }
    }

    private fun getFileExtension(fileName: String?): String {
        if (fileName.isNullOrBlank()) return ""

        val lastDotIndex = fileName.lastIndexOf('.')
        if (lastDotIndex == -1 || lastDotIndex == fileName.length - 1) return ""

        val extension = fileName.substring(lastDotIndex + 1)
        val queryIndex = extension.indexOf('?')
        val cleanExtension = if (queryIndex != -1) extension.substring(0, queryIndex) else extension

        return cleanExtension.uppercase()
    }
}
