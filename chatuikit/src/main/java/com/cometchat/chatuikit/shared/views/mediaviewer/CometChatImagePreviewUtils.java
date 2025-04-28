package com.cometchat.chatuikit.shared.views.mediaviewer;

import android.view.ViewGroup;
import android.widget.ImageView;

public class CometChatImagePreviewUtils {

    public static CometChatImagePreview createImagePreview(ImageView imageView, ViewGroup container) {
        return CometChatImagePreview.create(imageView, container);
    }

    public static void setOnScaleChangedListener(CometChatImagePreview cometChatImagePreview, OnScaleChangedListener onScaleChangedListener) {
        cometChatImagePreview.setOnScaleChangedListener(onScaleChangedListener::onScaleChange);
    }

    public static void setOnViewTranslateListener(CometChatImagePreview cometChatImagePreview, OnViewTranslateListener onViewTranslateListener) {
        cometChatImagePreview.setOnViewTranslateListener(new CometChatImagePreview.OnViewTranslateListener() {
            @Override
            public void onStart(ImageView view) {
                onViewTranslateListener.onStart(view);
            }

            @Override
            public void onViewTranslate(ImageView view, float amount) {
                onViewTranslateListener.onViewTranslate(view, amount);
            }

            @Override
            public void onDismiss(ImageView view) {
                onViewTranslateListener.onDismiss(view);
            }

            @Override
            public void onRestore(ImageView view) {
                onViewTranslateListener.onRestore(view);
            }
        });
    }

    // Listener interface for scale change
    public interface OnScaleChangedListener {
        void onScaleChange(float scaleFactor, float focusX, float focusY);
    }

    // Listener interface for view translation events
    public interface OnViewTranslateListener {
        void onStart(ImageView view);

        void onViewTranslate(ImageView view, float amount);

        void onRestore(ImageView view);

        void onDismiss(ImageView view);
    }
}
