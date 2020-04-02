package utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;

public class AnimUtil {



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
}
