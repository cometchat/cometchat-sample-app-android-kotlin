package com.cometchat.pro.uikit;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;

import com.bumptech.glide.Glide;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;

import utils.Utils;

/**
 * Purpose - This class is a subclass of AppCompatImageView, It is a component which is been used by developer
 * to display the avatar of their user or icon of the group. This class contains various methods which
 * are used to change shape of image as circle or rectangle, border with of image, border color of image.
 * and set default drawable if there is no image.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 20th January 2020
 *
*/
@BindingMethods(value = {@BindingMethod(type = Avatar.class, attribute = "app:avatar", method = "setAvatar"),
                   @BindingMethod(type = Avatar.class, attribute = "app:avatar_name", method = "setInitials")})
public class Avatar extends AppCompatImageView {

    private static final String TAG = Avatar.class.getSimpleName();

    private final Class avatar = Avatar.class;

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    /*
     * Path of them image to be clipped (to be shown)
     * */
    Path clipPath;

    /*
     * Place holder drawable (with background color and initials)
     * */
    Drawable drawable;

    /*
     * Contains initials of the member
     * */
    String text;

    /*
     * Used to set size and color of the member initials
     * */
    TextPaint textPaint;

    /*
     * Used as background of the initials with user specific color
     * */
    Paint paint;

    /*
     * To draw border
     */
    private Paint borderPaint;

    /*
     * Shape to be drawn
     * */
    int shape;

    /*
     * Constants to define shape
     * */
    protected static final int CIRCLE = 0;
    protected static final int RECTANGLE = 1;

    /*
     * User whose avatar should be displayed
     * */
    //User user;
    String avatarUrl;

    /*
     * Image width and height (both are same and constant, defined in dimens.xml
     * We cache them in this field
     * */
    private int imageSize;

    /*
     * We will set it as 2dp
     * */
    int cornerRadius;

    /*
     * Bounds of the canvas in float
     * Used to set bounds of member initial and background
     * */
    RectF rectF;


    private Context context;

    private int color;

    private int borderColor;

    private float borderWidth;

    private float borderRadius;

    public Avatar(Context context) {
        super(context);
        this.context = context;
    }

    public Avatar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getAttributes(attrs);
        init();
    }

    public Avatar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        getAttributes(attrs);
        init();
    }

    private void getAttributes(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Avatar,
                0, 0);

        try {

            /*
             * Get the shape and set shape field accordingly
             * */
            String avatarShape = a.getString(R.styleable.Avatar_avatar_shape);
            avatarUrl = a.getString(R.styleable.Avatar_avatar);
            borderColor =a.getColor(R.styleable.Avatar_border_color,Color.WHITE);
            borderWidth=a.getDimension(R.styleable.Avatar_border_width,1);



            /*
             * If the attribute is not specified, consider circle shape
             * */
            if (avatarShape == null) {
                shape = CIRCLE;
            } else {
                if (getContext().getString(R.string.rectangle).equalsIgnoreCase(avatarShape)) {
                    shape = RECTANGLE;
                } else {
                    shape = CIRCLE;
                }
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format(getResources().getString(R.string.scale_type_not_supported), scaleType));
        }
    }

    public void setShape(String shapestr)
    {
        if (shapestr.equalsIgnoreCase("circle")) {
            shape = CIRCLE;
        } else {
            shape = RECTANGLE;
        }
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException(getResources().getString(R.string.adjust_viewbound_not_supported));
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        init();
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        init();
    }
    /*
     * Initialize fields
     * */
    protected void init() {
        rectF = new RectF();
        clipPath = new Path();
        rectF.set(calculateBounds());

        //imageSize = getResources().getDimensionPixelSize(R.dimen.avatar_size);
        imageSize = getHeight();
        cornerRadius = (int) Utils.dpToPixel(2, getResources());

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(16f * getResources().getDisplayMetrics().scaledDensity);
        textPaint.setColor(Color.WHITE);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        // borderPaint.setColor(ContextCompat.getColor(context, R.color.border_color));
        // borderPaint.setStrokeWidth(context.getResources().getDimension(R.dimen.border_width));

        borderPaint.setColor(borderColor);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(borderWidth);
        color = getResources().getColor(R.color.colorPrimary);
        setOutlineProvider(new OutlineProvider());
    }

   private RectF calculateBounds() {
        int availableWidth  = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        int sideLength = Math.min(availableWidth, availableHeight);

        float left = getPaddingLeft() + (availableWidth - sideLength) / 2f;
        float top = getPaddingTop() + (availableHeight - sideLength) / 2f;

        return new RectF(left, top, left + sideLength, top + sideLength);
    }

    /**
     * This method is used to check if the user parameter passed is null or not. If it is not null then
     * it will show avatar of user, else it will show default drawable or first two letter of user name.
     *
     * @param user is an object of User.class.
     * @see User
     */
    public void setAvatar(@NonNull User user) {

         if (user!=null) {
             if (user.getAvatar() != null) {
                 avatarUrl = user.getAvatar();
                 if (isValidContextForGlide(context)) {
                     init();
                     setValues();
                 }
             } else {
                  if (user.getName()!=null&&!user.getName().isEmpty()) {
                      if (user.getName().length() > 2) {
                          text = user.getName().substring(0, 2);
                      } else {
                          text = user.getName();
                      }
                  }else {
                      text="??";
                  }
                 init();
                 setImageDrawable(drawable);
                 setDrawable();
             }
         }

    }

    /**
     * This method is used to check if the group parameter passed is null or not. If it is not null then
     * it will show icon of group, else it will show default drawable or first two letter of group name.
     *
     * @param group is an object of Group.class.
     * @see Group
     */
    public void setAvatar(@NonNull Group group) {

         if (group!=null) {

             if (group.getIcon() != null) {
                 avatarUrl = group.getIcon();
                 if (isValidContextForGlide(context))
                     init();
                 setValues();
             } else {
                 if (group.getName().length() > 2)
                     text = group.getName().substring(0, 2);
                 else {
                     text = group.getName();
                 }

                 init();
                 setDrawable();
                 setImageDrawable(drawable);
             }
         }
    }
    /**
     * This method is used to set image by using url passed in parameter..
     *
     * @param avatarUrl is an object of String.class which is used to set avatar.
     *
     */
    public void setAvatar(@NonNull String avatarUrl) {

        this.avatarUrl = avatarUrl;
        if (isValidContextForGlide(context))
            init();
            setValues();

    }

    /**
     * @param drawable  placeholder image
     * @param avatarUrl url of the image
     */
    public void setAvatar(Drawable drawable, @NonNull String avatarUrl) {
        this.drawable = drawable;
        this.avatarUrl = avatarUrl;
        if (isValidContextForGlide(context)) {
            init();
            setValues();
        }
    }

    public String getAvatarUrl() {
        return  this.avatarUrl;
    }
    /**
     * This method is used to set first two character as image. It is used when user, group or url
     * is null.
     *
     * @param name is a object of String.class. Its first 2 character are used in image with no avatar or icon.
     */
    public void setInitials(@NonNull String name) {

        if (name.length() >= 2) {
            text = name.substring(0, 2);
        }else {
            text=name;
        }
        setDrawable();
        setImageDrawable(drawable);
    }

    public Drawable getDrawable()
    {
        return drawable;
    }
    /*
     * Set user specific fields in here
     * */
    private void setValues() {


        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            if (context!=null) {
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(drawable)
                        .centerCrop()
                        .override(imageSize, imageSize)
                        .into(this);
            }
        } else {
            setImageDrawable(drawable);
        }
        invalidate();
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }


    /*
     * Create placeholder drawable
     * */
    private void setDrawable() {
        drawable = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {

                int centerX = Math.round(canvas.getWidth() * 0.5f);
                int centerY = Math.round(canvas.getHeight() * 0.5f);

                /*
                 * To draw text
                 * */
                if (text != null) {
                    float textWidth = textPaint.measureText(text) * 0.5f;
                    float textBaseLineHeight = textPaint.getFontMetrics().ascent * -0.4f;

                    /*
                     * Draw the background color before drawing initials text
                     * */
                    if (shape == RECTANGLE) {
                        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
                    } else {
                        canvas.drawCircle(centerX,
                                centerY,
                                Math.max(canvas.getHeight() / 2, textWidth / 2),
                                paint);
                    }

                    /*
                     * Draw the text above the background color
                     * */
                    canvas.drawText(text, centerX - textWidth, centerY + textBaseLineHeight, textPaint);
                }
            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.UNKNOWN;
            }
        };
    }

    /*
     * Set the canvas bounds here
     * */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        int screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        rectF.set(0, 0, screenWidth, screenHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shape == RECTANGLE) {
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint);
            clipPath.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CCW);
        } else {
            canvas.drawCircle(rectF.centerX(), rectF.centerY(), (rectF.height() / 2)-borderWidth, borderPaint);

            clipPath.addCircle(rectF.centerX(), rectF.centerY(), (rectF.height() / 2)-borderWidth, Path.Direction.CCW);
        }

        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }

    @Override
    public void setBackgroundColor(int color) {
        this.paint.setColor(color);
    }

    /**
     * This method is used to set border color of avatar.
     * @param color
     */
    public void setBorderColor(@ColorInt int color) {
        this.borderColor = color;
        this.borderPaint.setColor(color);
    }

    /**
     * This method is used to set border width of avatar
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth) {

        this.borderWidth = borderWidth;
        this.borderPaint.setStrokeWidth(borderWidth);
        invalidate();
    }

    private class OutlineProvider extends ViewOutlineProvider {

        @Override
        public void getOutline(View view, Outline outline) {
            Rect bounds = new Rect();
            rectF.roundOut(bounds);
            outline.setRoundRect(bounds, bounds.width() / 2.0f);
        }

    }

}
