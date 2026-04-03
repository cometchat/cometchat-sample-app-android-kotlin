package com.cometchat.uikit.kotlin.presentation.shared.shimmer

import android.content.Context
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R

/**
 * CometChatShimmerUtils provides utility methods for managing shimmer effects.
 */
object CometChatShimmerUtils {

    private val TAG = CometChatShimmerUtils::class.java.simpleName

    /**
     * Shows the shimmer effect on the given shimmer frame layout.
     *
     * @param shimmerFrameLayout The shimmer frame layout to show the effect on.
     */
    fun showShimmer(shimmerFrameLayout: CometChatShimmerFrameLayout) {
        showShimmerWithItems(shimmerFrameLayout, null, 0, 0)
    }

    /**
     * Shows the shimmer effect with custom items.
     *
     * @param shimmerFrameLayout The shimmer frame layout to show the effect on.
     * @param shimmer Optional custom shimmer configuration.
     * @param itemLayout The layout resource for each shimmer item.
     * @param itemCount The number of shimmer items to display.
     */
    fun showShimmerWithItems(
        shimmerFrameLayout: CometChatShimmerFrameLayout,
        shimmer: CometChatShimmer? = null,
        @LayoutRes itemLayout: Int = 0,
        itemCount: Int = 0
    ) {
        val context = shimmerFrameLayout.context

        if (itemLayout != 0 && itemCount > 0) {
            val recyclerView = RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = CometChatShimmerAdapter(itemCount, itemLayout)
            }
            shimmerFrameLayout.addView(recyclerView)
        }

        val shimmerConfig = shimmer ?: CometChatShimmer.ColorHighlightBuilder()
            .setBaseAlpha(1f)
            .setTilt(1f)
            .build()

        shimmerFrameLayout.setShimmer(shimmerConfig)
        shimmerFrameLayout.startShimmer()
    }

    /**
     * Hides the shimmer effect on the given shimmer frame layout.
     *
     * @param shimmerFrameLayout The shimmer frame layout to hide the effect on.
     */
    fun hideShimmer(shimmerFrameLayout: CometChatShimmerFrameLayout) {
        shimmerFrameLayout.stopShimmer()
        shimmerFrameLayout.visibility = View.GONE
    }

    /**
     * Gets the default CometChat shimmer configuration.
     *
     * @param context The context to get resources from.
     * @return A configured CometChatShimmer instance.
     */
    fun getCometChatShimmerConfig(context: Context): CometChatShimmer {
        return CometChatShimmer.ColorHighlightBuilder()
            .setBaseAlpha(1f)
            .setTilt(1f)
            .setBaseColor(context.resources.getColor(R.color.cometchat_shimmer_base_color, context.theme))
            .setHighlightColor(context.resources.getColor(R.color.cometchat_shimmer_base_highlight_color, context.theme))
            .build()
    }
}
