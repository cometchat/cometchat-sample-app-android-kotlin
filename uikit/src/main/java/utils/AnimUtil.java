package utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;

public class AnimUtil {

    private static AlphaAnimation alphaAnimation;

    private  static boolean isStartRecorded = false;

    public static void stopBlinkAnimation(View view)
    {
        view.clearAnimation();
        view.setAlpha(1.0f);
    }

    public static void blinkAnimation(View view) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(700); //You can manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(anim);
    }

    public static void start(View view) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 2.0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 2.0f);
        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(scaleY, scaleX);
        set.start();
    }

    public static void stop(View view) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f);


        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f);
        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(scaleY, scaleX);
        set.start();
    }


    public static void setStartRecorded(boolean startRecorded) {
        isStartRecorded = startRecorded;
    }

    public static void clearAlphaAnimation(boolean hideView,ImageView recordMic) {
        alphaAnimation.cancel();
        alphaAnimation.reset();
        recordMic.clearAnimation();
        if (hideView) {
            recordMic.setVisibility(View.GONE);
        }
    }

    public static void animateMic(ImageView recordMic) {
        alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        recordMic.startAnimation(alphaAnimation);
    }

    public static void resetSmallMic(ImageView recordMic) {
        recordMic.setAlpha(1.0f);
        recordMic.setScaleX(1.0f);
        recordMic.setScaleY(1.0f);
    }

//    public  static void moveSlideToCancel(final RecordMicButton recordMicButton, FrameLayout layout, float initialX, float difX) {
//
//        final ValueAnimator positionAnimator =
//                ValueAnimator.ofFloat(recordMicButton.getX(), initialX);
//
//        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                float x = (Float) animation.getAnimatedValue();
//                recordMicButton.setX(x);
//
//            }
//        });
//
//        recordMicButton.stopScale();
//        positionAnimator.setDuration(0);
//        positionAnimator.start();
//
//
//        // if the move event was not called ,then the difX will still 0 and there is no need to move it back
//        if (difX != 0) {
//            float x = initialX - difX;
//            layout.animate()
//                    .x(x)
//                    .setDuration(0)
//                    .start();
//        }
//
//
//    }

}
