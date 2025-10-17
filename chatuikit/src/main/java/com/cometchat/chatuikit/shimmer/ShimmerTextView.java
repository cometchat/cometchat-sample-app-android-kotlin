package com.cometchat.chatuikit.shimmer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.cometchat.chatuikit.CometChatTheme;

public class ShimmerTextView extends AppCompatTextView {

    private LinearGradient linearGradient;
    private Matrix gradientMatrix;
    private float translateX;
    private Paint paintObj;
    private int viewWidth;
    private boolean isShimmerEnabled = true;
    private float shimmerSpeed = 6f; // Adjust for faster/slower shimmer

    public ShimmerTextView(Context context) {
        super(context);
    }

    public ShimmerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShimmerTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0) {
            viewWidth = w;
            paintObj = getPaint();
            linearGradient = new LinearGradient(
                    -viewWidth * 3, 0,
                    0, 0,
                    new int[]{getCurrentTextColor(), CometChatTheme.getTextColorWhite(getContext()), CometChatTheme.getTextColorWhite(getContext()), getCurrentTextColor()},
                    new float[]{0f, 0.4f, 0.5f, 1f},
                    Shader.TileMode.MIRROR
            );
            paintObj.setShader(linearGradient);
            gradientMatrix = new Matrix();
        }
    }

    public void startShimmer() {
        isShimmerEnabled = true;
        if (linearGradient != null && paintObj != null)
            paintObj.setShader(linearGradient);
        invalidate();
    }

    public boolean isShimmerEnabled() {
        return isShimmerEnabled;
    }

    public void stopShimmer() {
        isShimmerEnabled = false;
        if (paintObj != null) {
            paintObj.setShader(null);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (gradientMatrix != null) {
            translateX += shimmerSpeed;
            if (translateX > 2 * viewWidth) {
                translateX = -viewWidth;
            }
            gradientMatrix.setTranslate(translateX, 0);
            linearGradient.setLocalMatrix(gradientMatrix);
            invalidate(); // redraw for animation
        }
    }
}