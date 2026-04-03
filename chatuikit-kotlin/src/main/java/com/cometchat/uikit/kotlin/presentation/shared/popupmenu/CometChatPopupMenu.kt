package com.cometchat.uikit.kotlin.presentation.shared.popupmenu

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView

/**
 * Enum defining the position of the popup menu relative to the anchor view.
 */
enum class PopupPosition {
    /** Show popup above the anchor view */
    ABOVE,
    /** Show popup below the anchor view */
    BELOW,
    /** Automatically determine position based on available screen space (default behavior) */
    AUTO
}

/**
 * CometChatPopupMenu displays a popup menu with customizable menu items.
 * Used for long-press context menus in conversation lists and other components.
 */
class CometChatPopupMenu(
    private val context: Context,
    @StyleRes style: Int = 0
) {
    companion object {
        private val TAG = CometChatPopupMenu::class.java.simpleName
    }

    private val menuItems: MutableList<MenuItem> = mutableListOf()
    private var popupWindow: PopupWindow? = null
    private var onMenuItemClickListener: OnMenuItemClickListener? = null
    private var onDismissListener: (() -> Unit)? = null

    @Dimension private var elevation: Int = 0
    @Dimension private var cornerRadius: Int = 0
    @ColorInt private var backgroundColor: Int = 0
    @ColorInt private var textColor: Int = 0
    @StyleRes private var textAppearance: Int = 0
    @ColorInt private var strokeColor: Int = 0
    @Dimension private var strokeWidth: Int = 0
    @ColorInt private var startIconTint: Int = 0
    @ColorInt private var endIconTint: Int = 0
    @Dimension private var itemPaddingHorizontal: Int = 0
    @Dimension private var itemPaddingVertical: Int = 0
    @Dimension private var minWidth: Int = 0

    init {
        applyStyleAttributes(context, null, R.attr.cometchatPopupMenuStyle, style)
        // Set default padding/minWidth values if not set from style
        val density = context.resources.displayMetrics.density
        if (itemPaddingHorizontal == 0) {
            itemPaddingHorizontal = (16 * density + 0.5f).toInt()
        }
        if (itemPaddingVertical == 0) {
            itemPaddingVertical = (8 * density + 0.5f).toInt()
        }
        if (minWidth == 0) {
            minWidth = (128 * density + 0.5f).toInt()
        }
    }

    /**
     * Applies the style attributes from XML, allowing direct attribute overrides.
     */
    private fun applyStyleAttributes(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes style: Int
    ) {
        val directAttributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatPopupMenu, defStyleAttr, style
        )
        extractAttributesAndApplyDefaults(directAttributes)
    }


    /**
     * Extracts the attributes and applies the default values if they are not set.
     */
    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray?) {
        typedArray ?: return
        try {
            elevation = typedArray.getDimensionPixelSize(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuElevation, 0
            )
            cornerRadius = typedArray.getDimensionPixelSize(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuCornerRadius, 0
            )
            backgroundColor = typedArray.getColor(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuBackgroundColor,
                CometChatTheme.getBackgroundColor1(context)
            )
            textColor = typedArray.getColor(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuItemTextColor,
                CometChatTheme.getTextColorPrimary(context)
            )
            textAppearance = typedArray.getResourceId(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuItemTextAppearance, 0
            )
            strokeColor = typedArray.getColor(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuStrokeColor,
                CometChatTheme.getStrokeColorLight(context)
            )
            strokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuStrokeWidth, 0
            )
            startIconTint = typedArray.getColor(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuItemStartIconTint,
                CometChatTheme.getIconTintPrimary(context)
            )
            endIconTint = typedArray.getColor(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuItemEndIconTint,
                CometChatTheme.getIconTintPrimary(context)
            )
        } finally {
            typedArray.recycle()
        }
    }

    fun setOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
        this.onMenuItemClickListener = listener
    }
    
    fun setOnDismissListener(listener: (() -> Unit)?) {
        this.onDismissListener = listener
    }

    fun setStyle(@StyleRes style: Int) {
        if (style != 0) {
            val attributes = context.theme.obtainStyledAttributes(style, R.styleable.CometChatPopupMenu)
            extractAttributesAndApplyDefaults(attributes)
        }
    }

    /**
     * Applies a typed style object to the popup menu.
     *
     * @param style The CometChatPopupMenuStyle to apply
     */
    fun setStyle(style: CometChatPopupMenuStyle) {
        elevation = style.elevation
        cornerRadius = style.cornerRadius
        backgroundColor = style.backgroundColor
        textColor = style.itemTextColor
        textAppearance = style.itemTextAppearance
        strokeColor = style.strokeColor
        strokeWidth = style.strokeWidth
        startIconTint = style.itemStartIconTint
        endIconTint = style.itemEndIconTint
        itemPaddingHorizontal = style.itemPaddingHorizontal
        itemPaddingVertical = style.itemPaddingVertical
        minWidth = style.minWidth
    }

    fun dismiss() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    fun setMenuItems(items: List<MenuItem>) {
        menuItems.clear()
        menuItems.addAll(items)
    }
    
    fun setElevation(@Dimension elevationPx: Int) {
        this.elevation = elevationPx
    }
    
    fun setCornerRadius(@Dimension radiusPx: Int) {
        this.cornerRadius = radiusPx
    }
    
    fun setBackgroundColor(@ColorInt color: Int) {
        this.backgroundColor = color
    }
    
    fun setTextColor(@ColorInt color: Int) {
        this.textColor = color
    }
    
    fun setStrokeColor(@ColorInt color: Int) {
        this.strokeColor = color
    }
    
    fun setStrokeWidth(@Dimension widthPx: Int) {
        this.strokeWidth = widthPx
    }
    
    fun setStartIconTint(@ColorInt color: Int) {
        this.startIconTint = color
    }
    
    fun setEndIconTint(@ColorInt color: Int) {
        this.endIconTint = color
    }
    
    fun setItemPaddingHorizontal(@Dimension paddingPx: Int) {
        this.itemPaddingHorizontal = paddingPx
    }
    
    fun setItemPaddingVertical(@Dimension paddingPx: Int) {
        this.itemPaddingVertical = paddingPx
    }
    
    fun setMinWidth(@Dimension widthPx: Int) {
        this.minWidth = widthPx
    }

    /**
     * Shows the popup menu anchored to the specified view.
     * This is an alias for show() to match the Android PopupWindow API.
     * 
     * @param anchorView The view to anchor the popup to
     * @param position The position of the popup relative to the anchor (ABOVE, BELOW, or AUTO)
     */
    fun showAsDropDown(anchorView: View, position: PopupPosition = PopupPosition.AUTO) {
        show(anchorView, position)
    }

    /**
     * Shows the popup menu anchored to the specified view.
     * 
     * @param anchorView The view to anchor the popup to
     * @param position The position of the popup relative to the anchor (ABOVE, BELOW, or AUTO)
     */
    fun show(anchorView: View, position: PopupPosition = PopupPosition.AUTO) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.cometchat_popup_recycler_view, null)

        val recyclerView = popupView.findViewById<RecyclerView>(R.id.recycler_view)
        val cardView = popupView.findViewById<MaterialCardView>(R.id.menu_parent)

        cardView.cardElevation = elevation.toFloat()
        cardView.radius = cornerRadius.toFloat()
        cardView.setCardBackgroundColor(backgroundColor)
        cardView.strokeColor = strokeColor
        cardView.strokeWidth = strokeWidth
        cardView.minimumWidth = minWidth

        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = PopupMenuAdapter(context, menuItems, itemPaddingHorizontal, itemPaddingVertical) { id, item ->
            // Dismiss popup first before invoking callbacks
            dismiss()
            menuItems.find { it.id == id }?.onClick?.invoke()
            onMenuItemClickListener?.onMenuItemClick(id, item)
        }

        adapter.setEndIconTint(endIconTint)
        adapter.setStartIconTint(startIconTint)
        adapter.setTextColor(textColor)
        adapter.setTextAppearance(textAppearance)
        recyclerView.adapter = adapter

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false // Non-focusable to prevent keyboard dismissal when popup is shown
        ).apply {
            isOutsideTouchable = true
            elevation = this@CometChatPopupMenu.elevation.toFloat()
            animationStyle = R.style.CometChatPopupMenuAnimation
            setOnDismissListener {
                this@CometChatPopupMenu.onDismissListener?.invoke()
            }
        }

        // Convert dp offsets to pixels (matching Java: 12dp margin, 10dp vertical offset)
        val marginInPixels = (12 * context.resources.displayMetrics.density + 0.5f).toInt()
        val offsetInPixels = (10 * context.resources.displayMetrics.density + 0.5f).toInt()

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth
        val popupHeight = popupView.measuredHeight

        // Align popup's right edge with anchor's right edge (matching Java implementation)
        // Java: xOffset = location[0] + anchorView.getWidth()
        val xOffset = location[0] + anchorView.width
        // Ensure popup doesn't go off screen on the right
        val adjustedXOffset = minOf(screenWidth - popupWidth - marginInPixels, xOffset)

        // Calculate Y offset based on position preference
        val yOffset = when (position) {
            PopupPosition.ABOVE -> {
                // Always show above the anchor
                location[1] - popupHeight
            }
            PopupPosition.BELOW -> {
                // Always show below the anchor (matching Java: location[1] + height - offsetInPixels)
                location[1] + anchorView.height - offsetInPixels
            }
            PopupPosition.AUTO -> {
                // Auto-determine based on available space (matching Java default behavior)
                val availableSpaceBelow = screenHeight - (location[1] + anchorView.height)
                val availableSpaceAbove = location[1]
                
                var calculatedYOffset = location[1] + anchorView.height - offsetInPixels
                
                if (popupHeight > availableSpaceBelow) {
                    calculatedYOffset = if (popupHeight <= availableSpaceAbove) {
                        location[1] - popupHeight
                    } else {
                        maxOf(0, screenHeight - popupHeight)
                    }
                }
                calculatedYOffset
            }
        }

        popupWindow?.showAtLocation(anchorView, Gravity.NO_GRAVITY, adjustedXOffset, yOffset)
    }

    /**
     * Listener interface for menu item clicks.
     */
    fun interface OnMenuItemClickListener {
        fun onMenuItemClick(id: String, item: String)
    }

    /**
     * Data class representing a menu item.
     */
    data class MenuItem(
        val id: String,
        val name: String,
        val startIcon: Drawable? = null,
        val endIcon: Drawable? = null,
        @ColorInt val startIconTint: Int = 0,
        @ColorInt val endIconTint: Int = 0,
        @ColorInt val textColor: Int = 0,
        @StyleRes val textAppearance: Int = 0,
        val onClick: (() -> Unit)? = null
    ) {
        companion object {
            /**
             * Creates a simple MenuItem with just id, name, and optional click handler.
             *
             * @param id Unique identifier for the menu item
             * @param name Display text for the menu item
             * @param onClick Optional callback invoked when the menu item is clicked
             * @return A new MenuItem instance with minimal configuration
             */
            fun simple(
                id: String,
                name: String,
                onClick: (() -> Unit)? = null
            ): MenuItem = MenuItem(
                id = id,
                name = name,
                onClick = onClick
            )

            /**
             * Creates a MenuItem with icons.
             *
             * @param id Unique identifier for the menu item
             * @param name Display text for the menu item
             * @param startIcon Optional icon displayed at the start of the menu item row
             * @param endIcon Optional icon displayed at the end of the menu item row
             * @param onClick Optional callback invoked when the menu item is clicked
             * @return A new MenuItem instance with icon configuration
             */
            fun withIcons(
                id: String,
                name: String,
                startIcon: Drawable? = null,
                endIcon: Drawable? = null,
                onClick: (() -> Unit)? = null
            ): MenuItem = MenuItem(
                id = id,
                name = name,
                startIcon = startIcon,
                endIcon = endIcon,
                onClick = onClick
            )
        }

        constructor(id: String, name: String, onClick: (() -> Unit)?) : this(
            id, name, null, null, 0, 0, 0, 0, onClick
        )

        constructor(
            id: String,
            name: String,
            startIcon: Drawable?,
            endIcon: Drawable?,
            onClick: (() -> Unit)?
        ) : this(id, name, startIcon, endIcon, 0, 0, 0, 0, onClick)
    }
}

/**
 * RecyclerView adapter for popup menu items.
 */
internal class PopupMenuAdapter(
    private val context: Context,
    private val items: List<CometChatPopupMenu.MenuItem>,
    @Dimension private val itemPaddingHorizontal: Int,
    @Dimension private val itemPaddingVertical: Int,
    private val onItemClick: (String, String) -> Unit
) : RecyclerView.Adapter<PopupMenuAdapter.ViewHolder>() {

    @ColorInt private var textColor: Int = 0
    @StyleRes private var textAppearance: Int = 0
    @ColorInt private var startIconTint: Int = 0
    @ColorInt private var endIconTint: Int = 0

    fun setTextColor(@ColorInt color: Int) {
        this.textColor = color
    }

    fun setTextAppearance(@StyleRes appearance: Int) {
        this.textAppearance = appearance
    }

    fun setStartIconTint(@ColorInt tint: Int) {
        this.startIconTint = tint
    }

    fun setEndIconTint(@ColorInt tint: Int) {
        this.endIconTint = tint
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_popup_menu_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.menu_item)
        private val ivStartIcon: ImageView = itemView.findViewById(R.id.start_icon)
        private val ivEndIcon: ImageView = itemView.findViewById(R.id.end_icon)

        fun bind(item: CometChatPopupMenu.MenuItem) {
            // Apply item padding
            itemView.setPadding(itemPaddingHorizontal, itemPaddingVertical, itemPaddingHorizontal, itemPaddingVertical)
            
            tvTitle.text = item.name

            // Apply text styling
            val itemTextColor = if (item.textColor != 0) item.textColor else textColor
            if (itemTextColor != 0) {
                tvTitle.setTextColor(itemTextColor)
            }

            val itemTextAppearance = if (item.textAppearance != 0) item.textAppearance else textAppearance
            if (itemTextAppearance != 0) {
                tvTitle.setTextAppearance(itemTextAppearance)
            }

            // Start icon
            if (item.startIcon != null) {
                ivStartIcon.visibility = View.VISIBLE
                ivStartIcon.setImageDrawable(item.startIcon)
                val tint = if (item.startIconTint != 0) item.startIconTint else startIconTint
                if (tint != 0) {
                    ivStartIcon.setColorFilter(tint)
                }
            } else {
                ivStartIcon.visibility = View.GONE
            }

            // End icon
            if (item.endIcon != null) {
                ivEndIcon.visibility = View.VISIBLE
                ivEndIcon.setImageDrawable(item.endIcon)
                val tint = if (item.endIconTint != 0) item.endIconTint else endIconTint
                if (tint != 0) {
                    ivEndIcon.setColorFilter(tint)
                }
            } else {
                ivEndIcon.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(item.id, item.name)
            }
        }
    }
}
