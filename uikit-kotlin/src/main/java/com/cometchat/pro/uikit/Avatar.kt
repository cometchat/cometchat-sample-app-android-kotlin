package com.cometchat.pro.uikit

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bumptech.glide.Glide
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import utils.Utils


@BindingMethods(value = [BindingMethod(type = Avatar::class, attribute = "app:avatar", method = "setAvatar"), BindingMethod(type = Avatar::class, attribute = "app:avatar_name", method = "setInitials")])
class Avatar : AppCompatImageView {
    private val TAG = Avatar::class.java.simpleName

    private val avatar: Class<*> = Avatar::class.java

    private val SCALE_TYPE = ScaleType.CENTER_CROP

    /*
     * Path of them image to be clipped (to be shown)
     * */
    var clipPath: Path? = null

    /*
     * Place holder drawable (with background color and initials)
     * */
    var drawables : Drawable? = null

    /*
     * Contains initials of the member
     * */
    var text: String? = null

    /*
     * Used to set size and color of the member initials
     * */
    var textPaint: TextPaint? = null

    /*
     * Used as background of the initials with user specific color
     * */
    var paint: Paint? = null

    /*
     * To draw border
     */
    private var borderPaint: Paint? = null

    /*
     * Shape to be drawn
     * */
    var shape = 0

    /*
     * Constants to define shape
     * */
    protected val CIRCLE = 0
    protected val RECTANGLE = 1

    /*
     * User whose avatar should be displayed
     * */
    //User user;
    var avatarUrl: String? = null

    /*
     * Image width and height (both are same and constant, defined in dimens.xml
     * We cache them in this field
     * */
    private var imageSize = 0

    /*
     * We will set it as 2dp
     * */
    var cornerRadius = 0

    /*
     * Bounds of the canvas in float
     * Used to set bounds of member initial and background
     * */
    public var rectF: RectF? = null


    private var c: Context? = null

    private var color = 0

    private var borderColor = 0

    private var borderWidth = 0f

    private val borderRadius = 0f
    constructor(context: Context?) : super(context){
        this.c = context
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        this.c = context
        getAttributes(attrs!!)
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        this.c = context
        getAttributes(attrs!!)
        init()
    }

    private fun getAttributes(attrs: AttributeSet) {
        val a = getContext().theme.obtainStyledAttributes(
                attrs,
                R.styleable.Avatar,
                0, 0)
        try {

            /*
             * Get the shape and set shape field accordingly
             * */
            val avatarShape = a.getString(R.styleable.Avatar_avatar_shape)
            avatarUrl = a.getString(R.styleable.Avatar_avatar)
            borderColor = a.getColor(R.styleable.Avatar_border_color, Color.WHITE)
            borderWidth = a.getDimension(R.styleable.Avatar_border_width, 1f)


            /*
             * If the attribute is not specified, consider circle shape
             * */if (avatarShape == null) {
                shape = CIRCLE
            } else {
                if (getContext().getString(R.string.rectangle).equals(avatarShape, ignoreCase = true)) {
                    shape = RECTANGLE
                } else {
                    shape = CIRCLE
                }
            }
        } finally {
            a.recycle()
        }
    }

    override fun getScaleType(): ScaleType? {
        return SCALE_TYPE
    }

    override fun setScaleType(scaleType: ScaleType) {
        require(scaleType == SCALE_TYPE) { String.format(resources.getString(R.string.scale_type_not_supported), scaleType) }
    }

    fun setShape(shapestr: String) {
        if (shapestr.equals("circle", ignoreCase = true)) {
            shape = CIRCLE
        } else {
            shape = RECTANGLE
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        init()
    }

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        require(!adjustViewBounds) { resources.getString(R.string.adjust_viewbound_not_supported) }
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        init()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        init()
    }

    /*
     * Initialize fields
     * */
    protected fun init() {
        rectF = RectF()
        clipPath = Path()
        rectF!!.set(calculateBounds())

        //imageSize = getResources().getDimensionPixelSize(R.dimen.avatar_size);
        imageSize = height
        cornerRadius = Utils.dpToPixel(2f, resources).toInt()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.color = resources.getColor(R.color.colorPrimary)
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint!!.textSize = 16f * resources.displayMetrics.scaledDensity
        textPaint!!.color = Color.WHITE
        borderPaint = Paint()
        borderPaint!!.setStyle(Paint.Style.STROKE)
        borderPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
        // borderPaint.setColor(ContextCompat.getColor(context, R.color.border_color));
        // borderPaint.setStrokeWidth(context.getResources().getDimension(R.dimen.border_width));
        borderPaint!!.setColor(borderColor)
        borderPaint!!.setAntiAlias(true)
        borderPaint!!.setStrokeWidth(borderWidth)
        color = resources.getColor(R.color.colorPrimary)
        outlineProvider = OutlineProvider()
    }

    private fun calculateBounds(): RectF? {
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom
        val sideLength = Math.min(availableWidth, availableHeight)
        val left = paddingLeft + (availableWidth - sideLength) / 2f
        val top = paddingTop + (availableHeight - sideLength) / 2f
        return RectF(left, top, left + sideLength, top + sideLength)
    }

    /**
     * This method is used to check if the user parameter passed is null or not. If it is not null then
     * it will show avatar of user, else it will show default drawable or first two letter of user name.
     *
     * @param user is an object of User.class.
     * @see User
     */
    fun setAvatar(user: User) {
        if (user != null) {
            if (user.avatar != null) {
                avatarUrl = user.avatar
                if (isValidContextForGlide(context)) {
                    init()
                    setValues()
                }
            } else {
                text = if (user.name != null && !user.name.isEmpty()) {
                    if (user.name.length > 2) {
                        user.name.substring(0, 2)
                    } else {
                        user.name
                    }
                } else {
                    "??"
                }
                init()
                setImageDrawable(drawable)
                setDrawable()
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
    fun setAvatar(group: Group) {
        if (group != null) {
            if (group.icon != null) {
                avatarUrl = group.icon
                if (isValidContextForGlide(context)) init()
                setValues()
            } else {
                text = if (group.name.length > 2) group.name.substring(0, 2) else {
                    group.name
                }
                init()
                setDrawable()
                setImageDrawable(drawable)
            }
        }
    }

    /**
     * This method is used to set image by using url passed in parameter..
     *
     * @param avatarUrl is an object of String.class which is used to set avatar.
     */
    fun setAvatar(avatarUrl: String?) {
        this.avatarUrl = avatarUrl
        if (isValidContextForGlide(context)) init()
        setValues()
    }

    /**
     * @param drawable  placeholder image
     * @param avatarUrl url of the image
     */
    fun setAvatar(drawable: Drawable?, avatarUrl: String) {
        this.drawables = drawable
        this.avatarUrl = avatarUrl
        if (isValidContextForGlide(context)) {
            init()
            setValues()
        }
    }

//    fun getAvatarUrl(): String? {
//        return avatarUrl
//    }

    /**
     * This method is used to set first two character as image. It is used when user, group or url
     * is null.
     *
     * @param name is a object of String.class. Its first 2 character are used in image with no avatar or icon.
     */
    fun setInitials(name: String) {
        text = if (name.length >= 2) {
            name.substring(0, 2)
        } else {
            name
        }
        setDrawable()
        setImageDrawable(drawable)
    }

    override fun getDrawable(): Drawable? {
        return drawables
    }

    /*
     * Set user specific fields in here
     * */
    private fun setValues() {
        if (avatarUrl != null && !avatarUrl!!.isEmpty()) {
            if (context != null) {
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(drawable)
                        .centerCrop()
                        .override(imageSize, imageSize)
                        .into(this)
            }
        } else {
            setImageDrawable(drawable)
        }
        invalidate()
    }

    fun isValidContextForGlide(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        if (context is Activity) {
            val activity = context
            if (activity.isDestroyed || activity.isFinishing) {
                return false
            }
        }
        return true
    }


    /*
     * Create placeholder drawable
     * */
    private fun setDrawable() {
        drawables = object : Drawable() {
            override fun draw(canvas: Canvas) {
                val centerX = Math.round(canvas.width * 0.5f)
                val centerY = Math.round(canvas.height * 0.5f)

                /*
                 * To draw text
                 * */if (text != null) {
                    val textWidth = textPaint!!.measureText(text) * 0.5f
                    val textBaseLineHeight = textPaint!!.fontMetrics.ascent * -0.4f

                    /*
                     * Draw the background color before drawing initials text
                     * */if (shape == RECTANGLE) {
                        canvas.drawRoundRect(rectF!!, cornerRadius.toFloat(), cornerRadius.toFloat(), paint!!)
                    } else {
                        canvas.drawCircle(centerX.toFloat(),
                                centerY.toFloat(),
                                Math.max(canvas.height / 2.toFloat(), textWidth / 2),
                                paint!!)
                    }

                    /*
                     * Draw the text above the background color
                     * */canvas.drawText(text!!, centerX - textWidth, centerY + textBaseLineHeight, textPaint!!)
                }
            }

            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(colorFilter: ColorFilter?) {}
            override fun getOpacity(): Int {
                return PixelFormat.UNKNOWN
            }
        }
    }

    /*
     * Set the canvas bounds here
     * */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val screenWidth = MeasureSpec.getSize(widthMeasureSpec)
        val screenHeight = MeasureSpec.getSize(heightMeasureSpec)
        rectF!![0f, 0f, screenWidth.toFloat()] = screenHeight.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        if (shape == RECTANGLE) {
            canvas.drawRoundRect(rectF!!, cornerRadius.toFloat(), cornerRadius.toFloat(), borderPaint!!)
            clipPath!!.addRoundRect(rectF, cornerRadius.toFloat(), cornerRadius.toFloat(), Path.Direction.CCW)
        } else {
            canvas.drawCircle(rectF!!.centerX(), rectF!!.centerY(), rectF!!.height() / 2 - borderWidth, borderPaint!!)
            clipPath!!.addCircle(rectF!!.centerX(), rectF!!.centerY(), rectF!!.height() / 2 - borderWidth, Path.Direction.CCW)
        }
        canvas.clipPath(clipPath!!)
        super.onDraw(canvas)
    }

    override fun setBackgroundColor(color: Int) {
        paint!!.color = color
    }

    /**
     * This method is used to set border color of avatar.
     * @param color
     */
    fun setBorderColor(@ColorInt color: Int) {
        borderColor = color
        borderPaint!!.color = color
    }

    /**
     * This method is used to set border width of avatar
     * @param borderWidth
     */
    fun setBorderWidth(borderWidth: Int) {
        this.borderWidth = borderWidth.toFloat()
        borderPaint!!.strokeWidth = borderWidth.toFloat()
        invalidate()
    }

    private inner class OutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val bounds = Rect()
            rectF!!.roundOut(bounds)
            outline.setRoundRect(bounds, bounds.width() / 2.0f)
        }
    }
}