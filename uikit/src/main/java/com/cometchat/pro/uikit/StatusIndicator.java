package com.cometchat.pro.uikit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;

import com.cometchat.pro.uikit.R;

/**
 * Purpose - StatusIndicator is a subclass of View and it is used as component to display status
 * indicator of user. If helps to know whether user is online or offline.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 *
 */

@BindingMethods(value ={@BindingMethod(type = StatusIndicator.class, attribute = "app:user_status", method = "setUserStatus")})
public class StatusIndicator extends View {

    private Paint paint;
    RectF rectF;
    int status;

    /*
     * Constants to define shape
     * */
    protected static final int OFFLINE = 0;
    protected static final int ONLINE = 1;

    public StatusIndicator(Context context) {
        super(context);
        init();
    }

    public StatusIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttributes(attrs);
        init();
    }

    public StatusIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttributes(attrs);
        init();
    }

    public void setUserStatus(String userStatus) {

        if ("online".equalsIgnoreCase(userStatus)) {
            status = ONLINE ;
        } else {
            status = OFFLINE;
        }
        setValues();
    }

    private void setValues() {
        if(status == ONLINE)
            paint.setColor(Color.parseColor("#4CAF50"));
        else {
            paint.setColor(Color.parseColor("#6b000000"));
        }

        invalidate();
    }

  private void setColor(@ColorInt int color){
        paint.setColor(color);
        invalidate();
  }


    private void getAttributes(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.StatusIndicator, 0, 0);

        String userStatus = a.getString(R.styleable.StatusIndicator_user_status);
        if (userStatus == null) {
            status = OFFLINE;
        } else {
            if (getContext().getString(R.string.online).equalsIgnoreCase(userStatus)) {
                status = ONLINE ;
            } else {
                status = OFFLINE;
            }
        }
    }

    protected void init(){
        paint = new Paint();
        rectF = new RectF();

        if(status == ONLINE)
            paint.setColor(Color.parseColor("#4CAF50"));
        else
            paint.setColor(Color.parseColor("#6b000000"));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        int screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        rectF.set(0, 0, screenWidth, screenHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(rectF.centerX(), rectF.centerY(),(rectF.height() / 2), paint);
    }
}
