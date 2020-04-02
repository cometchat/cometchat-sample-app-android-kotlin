package utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewTreeObserver;

public class KeyBoardUtils {

    static int mAppHeight;

    static int currentOrientation = -1;

    public static void setKeyboardVisibilityListener(final Activity activity, final View contentView, final KeyboardVisibilityListener keyboardVisibilityListener) {

        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private int mPreviousHeight;

            @Override

            public void onGlobalLayout() {

                int newHeight = contentView.getHeight();

                if (newHeight == mPreviousHeight)

                    return;

                mPreviousHeight = newHeight;

                if (activity.getResources().getConfiguration().orientation != currentOrientation) {

                    currentOrientation = activity.getResources().getConfiguration().orientation;

                    mAppHeight = 0;

                }

                if (newHeight >= mAppHeight) {

                    mAppHeight = newHeight;

                }

                if (newHeight != 0) {

                    if (mAppHeight > newHeight) {


                        keyboardVisibilityListener.onKeyboardVisibilityChanged(true);

                    } else {


                        keyboardVisibilityListener.onKeyboardVisibilityChanged(false);

                    }

                }

            }

        });

    }
}

