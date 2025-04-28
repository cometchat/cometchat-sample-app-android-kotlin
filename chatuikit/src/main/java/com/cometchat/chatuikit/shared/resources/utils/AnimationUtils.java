package com.cometchat.chatuikit.shared.resources.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

public class AnimationUtils {
    private static final String TAG = AnimationUtils.class.getSimpleName();

    public static void animateVisibilityVisible(final View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        final int targetHeight = view.getMeasuredHeight();

        // Set initial state: height = 0, invisible
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = 0;
        view.setLayoutParams(params);
        view.setVisibility(View.VISIBLE); // Set visible *after* height = 0

        // Animate height from 0 to measured height
        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.setDuration(300); // duration in ms
        animator.addUpdateListener(valueAnimator -> {
            params.height = (int) valueAnimator.getAnimatedValue();
            view.setLayoutParams(params);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Set to wrap_content to allow layout to adapt naturally afterward
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(params);
            }
        });

        animator.start();
    }

    public static void animateVisibilityGone(final View view) {
        // Set the initial height to the current height
        int initialHeight = view.getHeight();

        // Create an animation to reduce the height to zero
        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                // Update the height of the view
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = animatedValue;
                view.setLayoutParams(layoutParams);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Set the view visibility to GONE after the animation ends
                view.setVisibility(View.GONE);
            }
        });

        // Set the duration of the animation and start it
        animator.setDuration(300); // You can adjust the duration
        animator.start();
    }
}
