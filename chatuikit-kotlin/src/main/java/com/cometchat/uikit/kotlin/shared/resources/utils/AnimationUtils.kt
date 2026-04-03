package com.cometchat.uikit.kotlin.shared.resources.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup

/**
 * Utility class for view animations.
 * Provides smooth height-based visibility animations for views.
 * 
 * Reference: chatuikit/src/main/java/com/cometchat/chatuikit/shared/resources/utils/AnimationUtils.java
 */
object AnimationUtils {

    private const val DEFAULT_DURATION = 300L

    /**
     * Animates a view to become visible by expanding its height from 0 to its target height.
     * The view will smoothly expand vertically with a 300ms animation.
     * 
     * Supports both fixed height views (e.g., 296dp) and wrap_content views.
     *
     * @param view The view to animate to visible state
     * @param duration Animation duration in milliseconds (default: 300ms)
     */
    fun animateVisibilityVisible(view: View, duration: Long = DEFAULT_DURATION) {
        val params = view.layoutParams
        
        // Store original height to restore after animation
        val originalHeight = params.height
        
        // First, set view visible with original height to allow proper measurement
        params.height = originalHeight
        view.layoutParams = params
        view.visibility = View.VISIBLE
        
        // Force a measure pass to ensure child views are properly laid out
        view.measure(
            View.MeasureSpec.makeMeasureSpec((view.parent as View).width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        
        // Get target height for animation
        val targetHeight: Int = if (originalHeight > 0) {
            // Fixed height - use it directly
            originalHeight
        } else {
            // wrap_content - use measured height
            view.measuredHeight
        }

        // Now set height to 0 to start animation
        params.height = 0
        view.layoutParams = params

        // Animate height from 0 to target height
        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.duration = duration
        animator.addUpdateListener { valueAnimator ->
            params.height = valueAnimator.animatedValue as Int
            view.layoutParams = params
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Restore original height (preserves fixed height like 296dp)
                params.height = originalHeight
                view.layoutParams = params
            }
        })

        animator.start()
    }

    /**
     * Animates a view to become gone by collapsing its height from current height to 0.
     * The view will smoothly collapse vertically with a 300ms animation.
     *
     * @param view The view to animate to gone state
     * @param duration Animation duration in milliseconds (default: 300ms)
     */
    fun animateVisibilityGone(view: View, duration: Long = DEFAULT_DURATION) {
        val params = view.layoutParams
        
        // Store original height to restore after animation
        val originalHeight = params.height
        
        // Get the actual current height for animation
        val initialHeight = view.height

        // Create an animation to reduce the height to zero
        val animator = ValueAnimator.ofInt(initialHeight, 0)
        animator.duration = duration
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            params.height = animatedValue
            view.layoutParams = params
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Set the view visibility to GONE after the animation ends
                view.visibility = View.GONE
                // Restore original height for next show animation
                params.height = originalHeight
                view.layoutParams = params
            }
        })

        animator.start()
    }
}
