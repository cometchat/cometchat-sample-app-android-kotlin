package com.cometchat.pro.uikit;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import utils.MediaUtils;
import utils.Utils;

public class CallScreenButton extends LinearLayout implements View.OnTouchListener {

    private static final int TOTAL_TIME = 1000;
    private static final int SHAKE_TIME = 200;

    private static final int UP_TIME = (TOTAL_TIME - SHAKE_TIME) / 2;
    private static final int DOWN_TIME = (TOTAL_TIME - SHAKE_TIME) / 2;

    private static final int SHIMMER_TOTAL = UP_TIME + SHAKE_TIME;

    private static final int ANSWER_THRESHOLD = 40;
    private static final int DECLINE_THRESHOLD = 56;

    private ImageView ivRedArrowOne, ivRedArrowTwo, ivRedArrowThree,
            ivGreenArrowOne, ivGreenArrowTwo, ivGreenArrowThree;

    private FloatingActionButton fab;

    private float lastY;

    private boolean animating = false;
    private boolean complete = false;

    private AnimatorSet animatorSet;

    private AnswerDeclineListener listener;

    private boolean isVideo;
    private TextView swipeUpText;
    private TextView swipeDownText;
    private boolean isIncoming;

    public CallScreenButton(Context context) {
        super(context);
        intiComponentView();
    }

    public CallScreenButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        intiComponentView();
    }

    public CallScreenButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intiComponentView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CallScreenButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        intiComponentView();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                resetAnimation();
                lastY = motionEvent.getRawY();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                fab.setRotation(0);

                if (Build.VERSION.SDK_INT >= 21) {
                    fab.getDrawable().setTint(getResources().getColor(R.color.green_600));
                    fab.getBackground().setTint(Color.WHITE);
                }
                animating = true;
                animateElement(0);
                break;
            case MotionEvent.ACTION_MOVE:
                float diff = motionEvent.getRawY() - lastY;

                float diffThreshold;
                float percentageToThreshold;
                int backgroundColor;
                int foregroundColor;

                if (diff < 0) {
                    diffThreshold = Utils.dpToPx(getContext(), ANSWER_THRESHOLD);
                    percentageToThreshold = Math.min(1, (diff * -1) / diffThreshold);
                    backgroundColor = (int) new ArgbEvaluator().evaluate(percentageToThreshold,
                            getResources().getColor(R.color.green_100), getResources().getColor(R.color.green_600));

                    if (percentageToThreshold > 0.5) {
                        foregroundColor = Color.WHITE;
                    } else {
                        foregroundColor = getResources().getColor(R.color.green_600);
                    }

                    fab.setTranslationY(diff);

                    if (percentageToThreshold == 1 && listener != null) {
                        fab.setVisibility(View.INVISIBLE);
                        lastY = motionEvent.getRawY();
                        if (!complete) {
                            complete = true;
                            MediaUtils.vibrate(getContext());
                            listener.onAnswered();

                        }
                    }
                } else {
                    diffThreshold = Utils.dpToPx(getContext(), DECLINE_THRESHOLD);
                    percentageToThreshold = Math.min(1, diff / diffThreshold);
                    backgroundColor = (int) new ArgbEvaluator().evaluate(percentageToThreshold,
                            getResources().getColor(R.color.red_100), getResources().getColor(R.color.red_600));

                    if (percentageToThreshold > 0.5) {
                        foregroundColor = Color.WHITE;
                    } else {
                        foregroundColor = getResources().getColor(R.color.green_600);
                    }
                    if (!isVideo) {
                        fab.setRotation(135 * percentageToThreshold);
                    }

                    if (percentageToThreshold == 1 && listener != null) {
                        fab.setVisibility(View.INVISIBLE);
                        lastY = motionEvent.getRawY();

                        if (!complete) {
                            complete = true;
                            MediaUtils.vibrate(getContext());
                            listener.onDeclined();

                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.getBackground().setTint(backgroundColor);
                    fab.getDrawable().setTint(foregroundColor);
                }

                break;
        }

        return true;
    }

    public void setAnswerDeclineListener(AnswerDeclineListener listener,boolean isVideo) {
        this.listener = listener;
        this.isVideo=isVideo;
        if (isVideo)
        {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_videocam_green_24dp));

        }
        else {
            fab.setBackground(getResources().getDrawable(R.drawable.ic_call_green24dp));
        }

    }

    private void intiComponentView() {
        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        View view = View.inflate(getContext(), R.layout.cometchat_call_screen_button, this);

        this.ivGreenArrowOne = view.findViewById(R.id.green_arrow_one);
        this.ivGreenArrowTwo = view.findViewById(R.id.green_arrow_two);
        this.ivGreenArrowThree = view.findViewById(R.id.green_arrow_three);
        this.ivRedArrowOne = view.findViewById(R.id.red_arrow_one);
        this.ivRedArrowTwo = view.findViewById(R.id.red_arrow_two);
        this.ivRedArrowThree = view.findViewById(R.id.red_arrow_three);
        this.fab = view.findViewById(R.id.fab);

        this.fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        this.fab.setOnTouchListener(this);

    }

    public void startAnimation() {
        if (!animating) {
            animating = true;
            animateElement(0);
        }
    }

    public void stopAnimation() {
        if (animating) {
            animating = false;
            resetAnimation();
        }
    }

    private void animateElement(int delay) {

        ObjectAnimator fabUp = getUpAnimation(fab);
        ObjectAnimator fabDown = getDownAnimation(fab);
        ObjectAnimator fabShake = getShakeAnimation(fab);

        animatorSet = new AnimatorSet();
        animatorSet.play(fabShake).after(fabUp);
        animatorSet.play(fabDown).after(fabShake);


        animatorSet.play(getShimmer(ivGreenArrowOne, ivGreenArrowTwo,ivGreenArrowThree,
                ivRedArrowOne, ivRedArrowTwo,ivRedArrowThree));

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animating) animateElement(1000);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animatorSet.setStartDelay(delay);
        animatorSet.start();
    }

    private Animator getShimmer(View... views) {

        AnimatorSet animatorSet = new AnimatorSet();
        int evenDuration = SHIMMER_TOTAL / views.length;
        int interval = 75;

        for (int i = 0; i < views.length; i++) {
            animatorSet.play(getShimmer(views[i], evenDuration + (evenDuration - interval)))
                    .after(interval * i);
        }
        return animatorSet;
    }

    private ObjectAnimator getShakeAnimation(View target) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", 0, 15, -15, 15, -15, 10, -10, 5, -5, 0);
        animator.setDuration(SHAKE_TIME);

        return animator;
    }

    private ObjectAnimator getUpAnimation(View target) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationY", 0, -1 * Utils.dpToPx(getContext(), 16));
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(UP_TIME);

        return animator;
    }

    private ObjectAnimator getShimmer(View target, int duration) {
        ObjectAnimator shimmer = ObjectAnimator.ofFloat(target, "alpha", 0, 0.5f, 1,0,1,0.5f);
        shimmer.setDuration(duration);

        return shimmer;
    }

    private ObjectAnimator getDownAnimation(View target) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationY", 0);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(DOWN_TIME);

        return animator;
    }

    private void resetAnimation() {

        animating = false;
        complete = false;

        if (animatorSet != null) animatorSet.cancel();
        fab.setTranslationY(0);
    }

    public interface AnswerDeclineListener {
        void onAnswered();

        void onDeclined();
    }
}