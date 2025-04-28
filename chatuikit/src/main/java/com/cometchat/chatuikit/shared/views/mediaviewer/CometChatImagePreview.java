package com.cometchat.chatuikit.shared.views.mediaviewer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.OverScroller;

import androidx.core.view.ViewCompat;

import java.lang.ref.WeakReference;

public class CometChatImagePreview implements View.OnTouchListener, View.OnLayoutChangeListener {
    public static final float DEFAULT_MAX_ZOOM = 5.0f;
    public static final long DEFAULT_ANIM_DURATION = 250L;
    public static final long DEFAULT_ANIM_DURATION_LONG = 375L;
    public static final float DEFAULT_VIEW_DRAG_FRICTION = 1f;
    public static final float DEFAULT_DRAG_DISMISS_DISTANCE_IN_VIEW_HEIGHT_RATIO = 0.5f;
    public static final int DEFAULT_DRAG_DISMISS_DISTANCE_IN_DP = 96;
    public static final float MAX_FLING_VELOCITY = 8000f;
    public static final float MIN_FLING_VELOCITY = 1500f;
    public static final float DEFAULT_DOUBLE_TAP_ZOOM_SCALE = 0.5f;
    public static final DecelerateInterpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();
    // max zoom (> 1f)
    private final float maxZoom = CometChatImagePreview.DEFAULT_MAX_ZOOM;
    // use fling gesture for dismiss
    private final boolean useFlingToDismissGesture = true;
    // flag to enable or disable drag to dismiss
    private final boolean useDragToDismiss = true;
    // duration millis for dismiss animation
    private final long dismissAnimationDuration = CometChatImagePreview.DEFAULT_ANIM_DURATION;
    // duration millis for restore animation
    private final long restoreAnimationDuration = CometChatImagePreview.DEFAULT_ANIM_DURATION;
    // duration millis for image animation
    private final long flingAnimationDuration = CometChatImagePreview.DEFAULT_ANIM_DURATION;
    // duration millis for double tap scale animation
    private final long scaleAnimationDuration = CometChatImagePreview.DEFAULT_ANIM_DURATION_LONG;
    // duration millis for over scale animation
    private final long overScaleAnimationDuration = CometChatImagePreview.DEFAULT_ANIM_DURATION_LONG;
    // duration millis for over scrolling animation
    private final long overScrollAnimationDuration = CometChatImagePreview.DEFAULT_ANIM_DURATION;
    // view drag friction for swipe to dismiss (1f: drag distance == view move distance. Smaller value, view moves slower)
    private final float viewDragFriction = CometChatImagePreview.DEFAULT_VIEW_DRAG_FRICTION;
    // drag distance threshold in dp for swipe to dismiss
    private final int dragDismissDistanceInDp = CometChatImagePreview.DEFAULT_DRAG_DISMISS_DISTANCE_IN_DP;
    private final Interpolator dismissAnimationInterpolator = CometChatImagePreview.DEFAULT_INTERPOLATOR;
    private final Interpolator restoreAnimationInterpolator = CometChatImagePreview.DEFAULT_INTERPOLATOR;
    private final Interpolator flingAnimationInterpolator = CometChatImagePreview.DEFAULT_INTERPOLATOR;
    private final Interpolator doubleTapScaleAnimationInterpolator = new AccelerateDecelerateInterpolator();
    private final Interpolator overScaleAnimationInterpolator = CometChatImagePreview.DEFAULT_INTERPOLATOR;
    private final Interpolator overScrollAnimationInterpolator = CometChatImagePreview.DEFAULT_INTERPOLATOR;
    private final float doubleTapZoomScale = CometChatImagePreview.DEFAULT_DOUBLE_TAP_ZOOM_SCALE; // 0f~1f
    private final float minimumFlingVelocity = CometChatImagePreview.MIN_FLING_VELOCITY;
    private final Matrix transfrom = new Matrix();
    private final Rect originalViewBounds = new Rect();
    private final OverScroller scroller;
    private final WeakReference<ImageView> imageViewRef;
    private final WeakReference<ViewGroup> containerRef;
    // view rect - padding (recalculated on size changed)
    private RectF canvasBounds = new RectF();
    // bitmap drawing rect (move on scroll, recalculated on scale changed)
    private RectF bitmapBounds = new RectF();
    // displaying bitmap rect (does not move, recalculated on scale changed)
    private RectF viewport = new RectF();
    // minimum scale of bitmap
    private float minScale = 1f;
    // maximum scale of bitmap
    private float maxScale = 1f;
    private float dragToDismissThreshold = 0f;
    private boolean isVerticalScrollEnabled = true;
    private boolean isHorizontalScrollEnabled = true;
    private boolean isBitmapScaleAnimationRunning = false;
    private float initialY = 0f;
    // is ready for drawing bitmap
    private boolean isReadyToDraw = false;
    // bitmap (decoded) width
    private float imageWidth = 0f;
    // bitmap (decoded) height
    private float imageHeight = 0f;
    // bitmap scale
    private float scale = 1f;
    private Animator flingAnimator = new ValueAnimator();
    private boolean isBitmapTranslateAnimationRunning = false;
    private boolean isViewTranslateAnimationRunning = false;
    private float dragDismissDistanceInPx = 0f;
    // scaling helper
    private ScaleGestureDetector scaleGestureDetector = null;
    // translating helper
    private GestureDetector gestureDetector = null;
    // on view translate listener
    private OnViewTranslateListener onViewTranslateListener;
    // on scale changed listener
    private OnScaleChangedListener onScaleChangedListener;
    private final ScaleGestureDetector.OnScaleGestureListener onScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (isDragging() || isBitmapTranslateAnimationRunning || isBitmapScaleAnimationRunning) {
                return false;
            }

            float scaleFactor = detector != null ? detector.getScaleFactor() : 1.0f;
            float focalX = detector != null ? detector.getFocusX() : bitmapBounds.centerX();
            float focalY = detector != null ? detector.getFocusY() : bitmapBounds.centerY();

            if (scaleFactor == 1.0f) {
                // scale is not changing
                return true;
            }

            zoomToTargetScale(calcNewScale(scaleFactor), focalX, focalY);

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            // Do nothing
        }
    };
    private float lastDistY = Float.NaN;
    private final GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e2 != null && e2.getPointerCount() != 1) {
                return true;
            }

            if (scale > minScale) {
                processScroll(distanceX, distanceY);
            } else if (useDragToDismiss && scale == minScale) {
                processDrag(distanceY);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null) {
                return true;
            }

            if (scale > minScale) {
                processFlingBitmap(velocityX, velocityY);
            } else {
                processFlingToDismiss(velocityY);
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (e == null) {
                return false;
            }

            if (isBitmapScaleAnimationRunning) {
                return true;
            }

            if (scale > minScale) {
                zoomOutToMinimumScale(false);
            } else {
                zoomInToTargetScale(e);
            }
            return true;
        }
    };

    public CometChatImagePreview(ImageView imageView, ViewGroup container) {
        this.imageViewRef = new WeakReference<>(imageView);
        this.containerRef = new WeakReference<>(container);
        ((Activity) containerRef.get().getContext()).getWindow().getDecorView().setBackgroundColor(Color.BLACK);  // Replace with any color
        container.setOnTouchListener(this);
        container.addOnLayoutChangeListener(this);

        scaleGestureDetector = new ScaleGestureDetector(container.getContext(), onScaleGestureListener);
        gestureDetector = new GestureDetector(container.getContext(), onGestureListener);
        scroller = new OverScroller(container.getContext());

        dragDismissDistanceInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                            dragDismissDistanceInDp,
                                                            container.getContext().getResources().getDisplayMetrics());

        // Initialize imageView
        imageView.setImageMatrix(null);
        imageView.setY(0f);
        imageView.animate().cancel();
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
    }

    public static CometChatImagePreview create(ImageView imageView, ViewGroup container) {
        return new CometChatImagePreview(imageView, container);
    }

    public void setOnScaleChangedListener(OnScaleChangedListener onScaleChangedListener) {
        this.onScaleChangedListener = onScaleChangedListener;
    }

    public void setOnViewTranslateListener(OnViewTranslateListener onViewTranslateListener) {
        this.onViewTranslateListener = onViewTranslateListener;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        ImageView imageView = imageViewRef.get();
        ViewGroup container = containerRef.get();

        if (imageView == null || container == null) {
            return;
        }

        // Set up layout for the ImageView
        setupLayout(left, top, right, bottom);

        // Save initial Y position
        initialY = imageView.getY();

        if (useFlingToDismissGesture) {
            setDragToDismissDistance(DEFAULT_DRAG_DISMISS_DISTANCE_IN_VIEW_HEIGHT_RATIO);
        } else {
            setDragToDismissDistance(DEFAULT_DRAG_DISMISS_DISTANCE_IN_DP);
        }

        // Set the alpha value of the container background
//        container.getBackground().setAlpha(255);

        // Apply transform and invalidate the view
        setTransform();
        imageView.postInvalidate();
    }

    public void setupLayout(int left, int top, int right, int bottom) {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) {
            return;
        }

        originalViewBounds.set(left, top, right, bottom);
        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = (drawable instanceof BitmapDrawable) ? ((BitmapDrawable) drawable).getBitmap() : null;
        if (imageView.getWidth() == 0 || imageView.getHeight() == 0 || drawable == null) {
            return;
        }

        imageWidth = bitmap != null ? bitmap.getWidth() : drawable.getIntrinsicWidth();
        imageHeight = bitmap != null ? bitmap.getHeight() : drawable.getIntrinsicHeight();
        float canvasWidth = (imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight());
        float canvasHeight = (imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom());

        calcScaleRange(canvasWidth, canvasHeight, imageWidth, imageHeight);
        calcBounds();
        constrainBitmapBounds(false);
        isReadyToDraw = true;
        imageView.invalidate();
    }

    public void setDragToDismissDistance(float heightRatio) {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) return;

        dragToDismissThreshold = imageView.getHeight() * heightRatio;
    }

    public void setDragToDismissDistance(int distance) {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) return;

        dragToDismissThreshold = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            distance,
            imageView.getContext().getResources().getDisplayMetrics()
        );
    }

    public void setTransform() {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) {
            return;
        }

        Matrix transfrom = new Matrix();
        transfrom.reset();
        transfrom.postTranslate(-imageWidth / 2, -imageHeight / 2);
        transfrom.postScale(scale, scale);
        transfrom.postTranslate(bitmapBounds.centerX(), bitmapBounds.centerY());

        imageView.setImageMatrix(transfrom);
    }

    public void calcScaleRange(float canvasWidth, float canvasHeight, float bitmapWidth, float bitmapHeight) {
        float canvasRatio = canvasHeight / canvasWidth;
        float bitmapRatio = bitmapHeight / bitmapWidth;
        minScale = (canvasRatio > bitmapRatio) ? canvasWidth / bitmapWidth : canvasHeight / bitmapHeight;
        scale = minScale;
        maxScale = minScale * maxZoom;
    }

    public void calcBounds() {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) return;

        // Calculate canvas bounds
        canvasBounds = new RectF(
            imageView.getPaddingLeft(),
            imageView.getPaddingTop(),
            imageView.getWidth() - imageView.getPaddingRight(),
            imageView.getHeight() - imageView.getPaddingBottom()
        );

        // Calculate bitmap bounds
        bitmapBounds = new RectF(
            canvasBounds.centerX() - imageWidth * scale * 0.5f,
            canvasBounds.centerY() - imageHeight * scale * 0.5f,
            canvasBounds.centerX() + imageWidth * scale * 0.5f,
            canvasBounds.centerY() + imageHeight * scale * 0.5f
        );

        // Calculate viewport
        viewport = new RectF(
            Math.max(canvasBounds.left, bitmapBounds.left),
            Math.max(canvasBounds.top, bitmapBounds.top),
            Math.min(canvasBounds.right, bitmapBounds.right),
            Math.min(canvasBounds.bottom, bitmapBounds.bottom)
        );

        // Check scroll availability
        isHorizontalScrollEnabled = true;
        isVerticalScrollEnabled = true;

        if (bitmapBounds.width() < canvasBounds.width()) {
            isHorizontalScrollEnabled = false;
        }

        if (bitmapBounds.height() < canvasBounds.height()) {
            isVerticalScrollEnabled = false;
        }
    }

    public void constrainBitmapBounds(boolean animate) {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) {
            return;
        }

        if (isBitmapTranslateAnimationRunning || isBitmapScaleAnimationRunning) {
            return;
        }

        PointF offset = new PointF();

        // constrain viewport inside bitmap bounds
        if (viewport.left < bitmapBounds.left) {
            offset.x += viewport.left - bitmapBounds.left;
        }

        if (viewport.top < bitmapBounds.top) {
            offset.y += viewport.top - bitmapBounds.top;
        }

        if (viewport.right > bitmapBounds.right) {
            offset.x += viewport.right - bitmapBounds.right;
        }

        if (viewport.bottom > bitmapBounds.bottom) {
            offset.y += viewport.bottom - bitmapBounds.bottom;
        }

        if (offset.equals(0f, 0f)) {
            return;
        }

        if (animate) {
            if (!isVerticalScrollEnabled) {
                bitmapBounds.offset(0f, offset.y);
                offset.y = 0f;
            }

            if (!isHorizontalScrollEnabled) {
                bitmapBounds.offset(offset.x, 0f);
                offset.x = 0f;
            }

            RectF start = new RectF(bitmapBounds);
            RectF end = new RectF(bitmapBounds);
            end.offset(offset.x, offset.y);

            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(overScrollAnimationDuration);
            animator.setInterpolator(overScrollAnimationInterpolator);
            animator.addUpdateListener(valueAnimator -> {
                float amount = (Float) valueAnimator.getAnimatedValue();
                float newLeft = lerp(amount, start.left, end.left);
                float newTop = lerp(amount, start.top, end.top);
                bitmapBounds.offsetTo(newLeft, newTop);
                ViewCompat.postInvalidateOnAnimation(imageView);
                setTransform();
            });
            animator.start();
        } else {
            bitmapBounds.offset(offset.x, offset.y);
        }
    }

    public float lerp(float amt, float start, float stop) {
        return start + (stop - start) * amt;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event == null) {
            return false;
        }

        ImageView imageView = imageViewRef.get();
        ViewGroup container = containerRef.get();

        if (imageView == null || container == null) {
            return false;
        }

        // Request to disallow intercepting touch events based on scale
        container.getParent().requestDisallowInterceptTouchEvent(scale != minScale);

        if (!imageView.isEnabled()) {
            return false;
        }

        if (isViewTranslateAnimationRunning) {
            return false;
        }

        boolean scaleEvent = scaleGestureDetector != null && scaleGestureDetector.onTouchEvent(event);
        boolean isScaleAnimationRunning = scale < minScale;

        if (scaleEvent != scaleGestureDetector.isInProgress() && !isScaleAnimationRunning) {
            // Handle single touch gesture when scaling process is not running
            if (gestureDetector != null) {
                gestureDetector.onTouchEvent(event);
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                flingAnimator.cancel();
                break;

            case MotionEvent.ACTION_UP:
                if (scale == minScale) {
                    if (!isViewTranslateAnimationRunning) {
                        dismissOrRestoreIfNeeded();
                    }
                } else if (scale > minScale) {
                    constrainBitmapBounds(true);
                } else {
                    zoomOutToMinimumScale(true);
                }
                break;
        }

        setTransform();
        imageView.postInvalidate();
        return true;
    }

    public void dismissOrRestoreIfNeeded() {
        if (!isDragging() || isViewTranslateAnimationRunning) {
            return;
        }
        dismissOrRestore();
    }

    public void zoomOutToMinimumScale(boolean isOverScaling) {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) {
            return;
        }

        float startScale = scale;
        float endScale = minScale;
        float startLeft = bitmapBounds.left;
        float startTop = bitmapBounds.top;
        float endLeft = canvasBounds.centerX() - imageWidth * minScale * 0.5f;
        float endTop = canvasBounds.centerY() - imageHeight * minScale * 0.5f;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(isOverScaling ? overScaleAnimationDuration : scaleAnimationDuration);
        valueAnimator.setInterpolator(isOverScaling ? overScaleAnimationInterpolator : doubleTapScaleAnimationInterpolator);

        valueAnimator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            scale = lerp(value, startScale, endScale);
            float newLeft = lerp(value, startLeft, endLeft);
            float newTop = lerp(value, startTop, endTop);
            calcBounds();
            bitmapBounds.offsetTo(newLeft, newTop);
            constrainBitmapBounds(false);
            ViewCompat.postInvalidateOnAnimation(imageView);
            setTransform();
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isBitmapScaleAnimationRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isBitmapScaleAnimationRunning = false;
                if (endScale == minScale) {
                    scale = minScale;
                    calcBounds();
                    constrainBitmapBounds(false);
                    imageView.postInvalidate();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                isBitmapScaleAnimationRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                // No-op
            }
        });

        valueAnimator.start();
    }

    private void dismissOrRestore() {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) {
            return;
        }

        if (shouldTriggerDragToDismissAnimation()) {
            if (useFlingToDismissGesture) {
                startDragToDismissAnimation();
            } else {
                if (onViewTranslateListener != null) {
                    onViewTranslateListener.onDismiss(imageView);
                }
                cleanup();
            }
        } else {
            restoreViewTransform();
        }
    }

    public boolean shouldTriggerDragToDismissAnimation() {
        return dragDistance() > dragToDismissThreshold;
    }

    public void startDragToDismissAnimation() {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) {
            return;
        }

        // Calculate translationY based on the difference in Y positions
        float translationY;
        if (imageView.getY() - initialY > 0) {
            translationY = originalViewBounds.top + imageView.getHeight() - imageView.getTop();
        } else {
            translationY = originalViewBounds.top - imageView.getHeight() - imageView.getTop();
        }

        imageView.animate()
                 .setDuration(dismissAnimationDuration)
                 .setInterpolator(new AccelerateDecelerateInterpolator())
                 .translationY(translationY)
                 .setUpdateListener(animation -> {
                     float amount = calcTranslationAmount();
                     changeBackgroundAlpha(amount);
                     if (onViewTranslateListener != null) {
                         onViewTranslateListener.onViewTranslate(imageView, amount);
                     }
                 })
                 .setListener(new Animator.AnimatorListener() {
                     @Override
                     public void onAnimationStart(Animator animation) {
                         isViewTranslateAnimationRunning = true;
                     }

                     @Override
                     public void onAnimationEnd(Animator animation) {
                         isViewTranslateAnimationRunning = false;
                         if (onViewTranslateListener != null) {
                             onViewTranslateListener.onDismiss(imageView);
                         }
                         cleanup();
                     }

                     @Override
                     public void onAnimationCancel(Animator animation) {
                         isViewTranslateAnimationRunning = false;
                     }

                     @Override
                     public void onAnimationRepeat(Animator animation) {
                         // No-op
                     }
                 });
    }

    private boolean isDragging() {
        return dragDistance() > 0f;
    }

    // Process the fling to dismiss gesture based on velocityY
    public void processFlingToDismiss(float velocityY) {
        if (useFlingToDismissGesture && !isViewTranslateAnimationRunning) {
            if (Math.abs(velocityY) < minimumFlingVelocity) {
                return;
            }
            startVerticalTranslateAnimation(velocityY);
        }
    }

    public void startVerticalTranslateAnimation(float velY) {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) return;

        isViewTranslateAnimationRunning = true;

        // Calculate the translationY based on velY
        final float translationY = velY > 0 ? originalViewBounds.top + imageView.getHeight() - imageView.getTop() : originalViewBounds.top - imageView.getHeight() - imageView.getTop();

        imageView
            .animate()
            .setDuration(dismissAnimationDuration)
            .setInterpolator(dismissAnimationInterpolator)
            .translationY(translationY)
            .setUpdateListener(animation -> {
                // Call update listener and pass translation amount
                float amount = calcTranslationAmount();
                changeBackgroundAlpha(amount);
                if (onViewTranslateListener != null) {
                    onViewTranslateListener.onViewTranslate(imageView, amount);
                }
            })
            .setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // No operation needed here
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isViewTranslateAnimationRunning = false;
                    if (onViewTranslateListener != null) {
                        onViewTranslateListener.onDismiss(imageView);
                    }
                    cleanup();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isViewTranslateAnimationRunning = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    // No operation needed here
                }
            });
    }

    // Calculate the translation amount for dragging
    public float calcTranslationAmount() {
        return constrain(
            0f,
            norm(dragDistance(), 0f, originalViewBounds.height()),
            1f
        );
    }

    public void changeBackgroundAlpha(float amount) {
        ViewGroup container = containerRef.get();
        if (container == null) return;
        if (amount == 0.0) {
            ((Activity) containerRef.get().getContext()).getWindow().getDecorView().setBackgroundColor(Color.BLACK);  // Replace with any color
        } else {
            ((Activity) containerRef.get().getContext()).getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);  // Replace with any color
        }
    }

    public void cleanup() {
        ViewGroup container = containerRef.get();
        if (container != null) {
            container.setOnTouchListener(null);
            container.removeOnLayoutChangeListener(null);
        }

        imageViewRef.clear();
        containerRef.clear();
    }

    public float constrain(float min, float value, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public float norm(float value, float start, float stop) {
        return value / (stop - start);
    }

    // Calculate the drag distance
    public float dragDistance() {
        return Math.abs(viewOffsetY());
    }

    // Method to get the Y offset for the view
    public float viewOffsetY() {
        ImageView imageView = imageViewRef.get();
        return imageView != null ? imageView.getY() : 0 - initialY;
    }

    public void restoreViewTransform() {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) {
            return;
        }

        imageView.animate()
                 .setDuration(restoreAnimationDuration)
                 .setInterpolator(restoreAnimationInterpolator)
                 .translationY(originalViewBounds.top - imageView.getTop())
                 .setUpdateListener(animation -> {
                     float amount = calcTranslationAmount();
                     changeBackgroundAlpha(amount);
                     if (onViewTranslateListener != null) {
                         onViewTranslateListener.onViewTranslate(imageView, amount);
                     }
                 })
                 .setListener(new Animator.AnimatorListener() {
                     @Override
                     public void onAnimationStart(Animator animation) {
                         // No-op
                     }

                     @Override
                     public void onAnimationEnd(Animator animation) {
                         if (onViewTranslateListener != null) {
                             onViewTranslateListener.onRestore(imageView);
                         }
                     }

                     @Override
                     public void onAnimationCancel(Animator animation) {
                         // No-op
                     }

                     @Override
                     public void onAnimationRepeat(Animator animation) {
                         // No-op
                     }
                 });
    }

    public void processFlingBitmap(float velocityX, float velocityY) {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) {
            return;
        }

        float velX = velocityX / scale;
        float velY = velocityY / scale;

        if (velX == 0f && velY == 0f) {
            return;
        }

        if (velX > MAX_FLING_VELOCITY) {
            velX = MAX_FLING_VELOCITY;
        }

        if (velY > MAX_FLING_VELOCITY) {
            velY = MAX_FLING_VELOCITY;
        }

        float fromX = bitmapBounds.left;
        float fromY = bitmapBounds.top;

        scroller.forceFinished(true);
        scroller.fling(
            Math.round(fromX),
            Math.round(fromY),
            Math.round(velX),
            Math.round(velY),
            Math.round(viewport.right - bitmapBounds.width()),
            Math.round(viewport.left),
            Math.round(viewport.bottom - bitmapBounds.height()),
            Math.round(viewport.top)
        );

        ViewCompat.postInvalidateOnAnimation(imageView);

        float toX = scroller.getFinalX();
        float toY = scroller.getFinalY();

        flingAnimator = ValueAnimator.ofFloat(0f, 1f);
        flingAnimator.setDuration(flingAnimationDuration);
        flingAnimator.setInterpolator(flingAnimationInterpolator);

        // Adding an explicit AnimatorUpdateListener
        ((ValueAnimator) flingAnimator).addUpdateListener(animation -> {
            float amount = (Float) animation.getAnimatedValue();
            float newLeft = lerp(amount, fromX, toX);
            float newTop = lerp(amount, fromY, toY);
            bitmapBounds.offsetTo(newLeft, newTop);
            ViewCompat.postInvalidateOnAnimation(imageView);
            setTransform();
        });

        flingAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isBitmapTranslateAnimationRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isBitmapTranslateAnimationRunning = false;
                constrainBitmapBounds(false);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                isBitmapTranslateAnimationRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                // No-op
            }
        });
        flingAnimator.start();
    }

    public void processScroll(float distanceX, float distanceY) {
        float distX = isHorizontalScrollEnabled ? -distanceX : 0f;
        float distY = isVerticalScrollEnabled ? -distanceY : 0f;

        offsetBitmap(distX, distY);
        setTransform();
    }

    public void offsetBitmap(float offsetX, float offsetY) {
        bitmapBounds.offset(offsetX, offsetY);
    }

    public void zoomInToTargetScale(MotionEvent e) {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) {
            return;
        }

        float startScale = scale;
        float endScale = minScale * maxZoom * doubleTapZoomScale;
        float focalX = e.getX();
        float focalY = e.getY();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startScale, endScale);
        valueAnimator.setDuration(scaleAnimationDuration);
        valueAnimator.setInterpolator(doubleTapScaleAnimationInterpolator);
        valueAnimator.addUpdateListener(animation -> {
            float animatedValue = (Float) animation.getAnimatedValue();
            zoomToTargetScale(animatedValue, focalX, focalY);
            ViewCompat.postInvalidateOnAnimation(imageView);
            setTransform();
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isBitmapScaleAnimationRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isBitmapScaleAnimationRunning = false;
                if (endScale == minScale) {
                    zoomToTargetScale(minScale, focalX, focalY);
                    imageView.postInvalidate();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isBitmapScaleAnimationRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // No-op
            }
        });

        valueAnimator.start();
    }

    public void zoomToTargetScale(float targetScale, float focalX, float focalY) {
        scale = targetScale;
        RectF lastBounds = new RectF(bitmapBounds);
        // scale has changed, recalculate bitmap bounds
        calcBounds();
        // offset to focalPoint
        offsetToZoomFocalPoint(focalX, focalY, lastBounds, bitmapBounds);
        if (onScaleChangedListener != null) {
            onScaleChangedListener.onScaleChange(targetScale, focalX, focalY);
        }
    }

    public void offsetToZoomFocalPoint(float focalX, float focalY, RectF oldBounds, RectF newBounds) {
        float oldX = constrain(viewport.left, focalX, viewport.right);
        float oldY = constrain(viewport.top, focalY, viewport.bottom);
        float newX = map(oldX, oldBounds.left, oldBounds.right, newBounds.left, newBounds.right);
        float newY = map(oldY, oldBounds.top, oldBounds.bottom, newBounds.top, newBounds.bottom);
        offsetBitmap(oldX - newX, oldY - newY);
    }

    public float map(float value, float srcStart, float srcStop, float dstStart, float dstStop) {
        if (srcStop - srcStart == 0f) {
            return 0f;
        }
        return ((value - srcStart) * (dstStop - dstStart) / (srcStop - srcStart)) + dstStart;
    }

    public void processDrag(float distanceY) {
        ImageView imageView = imageViewRef.get();
        if (imageView == null) {
            return;
        }

        if (Float.isNaN(lastDistY)) {
            lastDistY = distanceY;
            return;
        }

        if (imageView.getY() == initialY) {
            if (onViewTranslateListener != null) {
                onViewTranslateListener.onStart(imageView);
            }
        }

        // Adjust the Y position of the image based on the drag friction
        imageView.setY(imageView.getY() - distanceY * viewDragFriction);

        // Calculate translation amount and update background alpha
        float amount = calcTranslationAmount();
        changeBackgroundAlpha(amount);

        // Notify the listener about the view translation
        if (onViewTranslateListener != null) {
            onViewTranslateListener.onViewTranslate(imageView, amount);
        }
    }

    public float calcNewScale(float newScale) {
        return Math.min(maxScale, newScale * scale);
    }

    public void dismiss() {
        // Animate down offscreen (the finish listener will call the cleanup method)
        startVerticalTranslateAnimation(MIN_FLING_VELOCITY);
    }

    public interface OnViewTranslateListener {
        void onStart(ImageView view);

        void onViewTranslate(ImageView view, float amount);

        void onDismiss(ImageView view);

        void onRestore(ImageView view);
    }

    public interface OnScaleChangedListener {
        void onScaleChange(float scaleFactor, float focusX, float focusY);
    }


}
