package com.cometchat.uikit.kotlin.presentation.messagelist

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Shader
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubble
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView

/**
 * Type alias reusing the existing MenuItem from CometChatPopupMenu.
 */
typealias MenuItem = CometChatPopupMenu.MenuItem

/**
 * Callback interface for menu item clicks.
 */
fun interface OnMenuItemClickListener {
    fun onMenuItemClick(id: String, item: String)
}

/**
 * Callback interface for reaction clicks.
 */
fun interface ReactionClickListener {
    fun onReactionClick(baseMessage: BaseMessage, reaction: String)
}

/**
 * Callback interface for emoji picker clicks.
 */
fun interface EmojiPickerClickListener {
    fun onEmojiPickerClick()
}

/**
 * CometChatMessagePopupMenu manages a PopupWindow and a blur overlay ImageView
 * added to the WindowManager. It displays a full-screen popup with a blurred
 * background, a read-only message preview, quick reactions bar, and scrollable
 * option list when a user long-presses a message bubble.
 *
 * This is NOT a View subclass — it is a plain Kotlin class.
 */
class CometChatMessagePopupMenu(
    private val context: android.content.Context,
    @StyleRes style: Int = 0
) {
    companion object {
        private val TAG = CometChatMessagePopupMenu::class.java.simpleName
        val DEFAULT_REACTIONS = listOf("😍", "👍🏻", "🔥", "😊", "❤️")
    }

    // PopupWindow and blur overlay
    private var popupWindow: PopupWindow? = null
    private var blurIv: ImageView? = null
    private var currentMessage: BaseMessage? = null

    // Style attributes
    @Dimension private var elevation: Int = 0
    @Dimension private var cornerRadius: Int = 0
    @ColorInt private var backgroundColor: Int = 0
    @ColorInt private var textColor: Int = 0
    @StyleRes private var textAppearance: Int = 0
    @ColorInt private var strokeColor: Int = 0
    @Dimension private var strokeWidth: Int = 0
    @ColorInt private var startIconTint: Int = 0
    @ColorInt private var endIconTint: Int = 0

    // Configuration
    @DrawableRes private var addReactionIcon: Int = 0
    private var onMenuItemClickListener: OnMenuItemClickListener? = null
    private var messageAlignment: UIKitConstants.MessageListAlignment = UIKitConstants.MessageListAlignment.STANDARD
    private var menuItems: List<MenuItem> = emptyList()
    private var textFormatters: List<CometChatTextFormatter> = emptyList()
    private var quickReactions: List<String> = DEFAULT_REACTIONS
    private var reactionClickListener: ReactionClickListener? = null
    private var emojiPickerClickListener: EmojiPickerClickListener? = null
    private var quickReactionsVisibility: Int = View.VISIBLE
    private var receiptsVisibility: Int = View.VISIBLE

    init {
        applyDefaultStyle()
        if (style != 0) {
            setStyle(style)
        } else {
            // Resolve style from theme attribute
            val themeArray = context.theme.obtainStyledAttributes(intArrayOf(R.attr.cometchatPopupMenuStyle))
            val themeStyle = themeArray.getResourceId(0, 0)
            themeArray.recycle()
            if (themeStyle != 0) {
                setStyle(themeStyle)
            }
        }
    }

    private fun applyDefaultStyle() {
        backgroundColor = CometChatTheme.getBackgroundColor1(context)
        textColor = CometChatTheme.getTextColorPrimary(context)
        startIconTint = CometChatTheme.getIconTintSecondary(context)
        endIconTint = CometChatTheme.getIconTintSecondary(context)
        strokeColor = CometChatTheme.getStrokeColorLight(context)
        // Default corner radius = 16dp (cometchat_radius_4)
        cornerRadius = Utils.convertDpToPx(context, 16)
    }

    /**
     * Applies style attributes from a style resource.
     */
    fun setStyle(@StyleRes style: Int) {
        if (style == 0) return
        val typedArray = context.theme.obtainStyledAttributes(style, R.styleable.CometChatPopupMenu)
        try {
            elevation = typedArray.getDimensionPixelSize(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuElevation, elevation
            )
            cornerRadius = typedArray.getDimensionPixelSize(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuCornerRadius, cornerRadius
            )
            backgroundColor = typedArray.getColor(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuBackgroundColor, backgroundColor
            )
            textColor = typedArray.getColor(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuItemTextColor, textColor
            )
            textAppearance = typedArray.getResourceId(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuItemTextAppearance, textAppearance
            )
            strokeColor = typedArray.getColor(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuStrokeColor, strokeColor
            )
            strokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuStrokeWidth, strokeWidth
            )
            startIconTint = typedArray.getColor(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuItemStartIconTint, startIconTint
            )
            endIconTint = typedArray.getColor(
                R.styleable.CometChatPopupMenu_cometchatPopupMenuItemEndIconTint, endIconTint
            )
        } finally {
            typedArray.recycle()
        }
    }

    fun setAddReactionIcon(@DrawableRes icon: Int) {
        this.addReactionIcon = icon
    }

    fun setOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
        this.onMenuItemClickListener = listener
    }

    fun setMessageAlignment(alignment: UIKitConstants.MessageListAlignment) {
        this.messageAlignment = alignment
    }

    fun setMenuItems(items: List<MenuItem>) {
        this.menuItems = items
    }

    /**
     * Stores message templates for future use. The message bubble in the popup
     * preview gets its templates from the adapter during normal rendering.
     */
    fun setMessageTemplates(templates: List<Any>) {
        // Templates are stored for potential future use but not directly
        // applied to the bubble preview since CometChatMessageBubble
        // doesn't expose a setMessageTemplates method.
    }

    fun setTextFormatters(formatters: List<CometChatTextFormatter>) {
        this.textFormatters = formatters
    }

    fun setQuickReactions(reactions: List<String>) {
        this.quickReactions = reactions.ifEmpty { DEFAULT_REACTIONS }
    }

    fun setReactionClickListener(listener: ReactionClickListener?) {
        this.reactionClickListener = listener
    }

    fun setEmojiPickerClickListener(listener: EmojiPickerClickListener?) {
        this.emojiPickerClickListener = listener
    }

    fun setQuickReactionsVisibility(visibility: Int) {
        this.quickReactionsVisibility = visibility
    }

    fun setReceiptsVisibility(visibility: Int) {
        this.receiptsVisibility = visibility
    }

    fun getReceiptsVisibility(): Int = receiptsVisibility

    /**
     * Shows the popup menu anchored to the given view.
     *
     * @param anchorView The message bubble content view used as positional reference
     * @param parentView The parent view (typically CometChatMessageList)
     * @param baseMessage The message to show options for
     */
    fun show(anchorView: View, parentView: View, baseMessage: BaseMessage) {
        val activity = Utils.getActivity(context) ?: return
        if (!isActivityUsable(activity)) return

        currentMessage = baseMessage

        // 1. Inflate layout
        val popupView = LayoutInflater.from(context)
            .inflate(R.layout.cometchat_message_list_popup, null)

        val reactionCard = popupView.findViewById<MaterialCardView>(R.id.reaction_card)
        val viewReactions = popupView.findViewById<LinearLayout>(R.id.view_reactions)
        val messagePreview = popupView.findViewById<CometChatMessageBubble>(R.id.message_preview)
        val menuParent = popupView.findViewById<MaterialCardView>(R.id.menu_parent)
        val recyclerView = popupView.findViewById<RecyclerView>(R.id.recycler_view)

        // 2. Determine alignment
        val isLeft = determineIsLeftAligned(baseMessage)

        // Apply alignment to reaction_card and menu_parent
        applyAlignment(reactionCard, isLeft)
        applyAlignment(menuParent, isLeft)

        // 3. Configure message preview (read-only mode)
        configureMessagePreview(messagePreview, baseMessage)

        // 4. Configure quick reactions
        if (quickReactionsVisibility == View.VISIBLE) {
            configureQuickReactions(reactionCard, viewReactions, baseMessage, isLeft)
        } else {
            // 5. Hide reaction card when visibility is GONE
            reactionCard.visibility = View.GONE
        }

        // 6. Style menu card
        menuParent.cardElevation = elevation.toFloat()
        menuParent.radius = cornerRadius.toFloat()
        menuParent.setCardBackgroundColor(backgroundColor)
        menuParent.strokeColor = strokeColor
        menuParent.strokeWidth = strokeWidth

        // 7. Set up RecyclerView with adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = MessagePopupMenuAdapter(context, menuItems) { id, name ->
            menuItems.find { it.id == id }?.onClick?.invoke()
            onMenuItemClickListener?.onMenuItemClick(id, name)
        }
        adapter.setTextColor(textColor)
        adapter.setTextAppearance(textAppearance)
        adapter.setStartIconTint(startIconTint)
        adapter.setEndIconTint(endIconTint)
        recyclerView.adapter = adapter

        // 8. Create PopupWindow
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            this.elevation = this@CometChatMessagePopupMenu.elevation.toFloat()
            animationStyle = R.style.CometChatPopupMenuAnimation
        }

        // 9. Position popup
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        val yOffset = if ((location[1] - 1000) > 550) {
            location[1] - 1000
        } else {
            maxOf(maxOf(-(location[1] - 500), location[1] - 500), 300)
        }

        // 10. Blur background
        dimBackground(activity)

        popupWindow?.showAtLocation(
            anchorView,
            Gravity.TOP or Gravity.CENTER,
            0,
            yOffset
        )

        // 11. Dismiss handler
        popupWindow?.setOnDismissListener {
            removeDimBackground(activity)
        }

        // 12. Touch outside to dismiss
        popupView.setOnTouchListener { _, _ ->
            dismiss()
            true
        }
    }

    /**
     * Determines whether the popup content should be left-aligned.
     * Extracted as a companion-accessible function for testability.
     */
    internal fun determineIsLeftAligned(baseMessage: BaseMessage): Boolean {
        val loggedInUser = CometChatUIKit.getLoggedInUser()
        return baseMessage.sender?.uid != loggedInUser?.uid
            || messageAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED
    }

    private fun applyAlignment(view: View, isLeft: Boolean) {
        val params = view.layoutParams
        if (params is RelativeLayout.LayoutParams) {
            if (isLeft) {
                params.removeRule(RelativeLayout.ALIGN_PARENT_END)
                params.addRule(RelativeLayout.ALIGN_PARENT_START)
            } else {
                params.removeRule(RelativeLayout.ALIGN_PARENT_START)
                params.addRule(RelativeLayout.ALIGN_PARENT_END)
            }
            view.layoutParams = params
        }
    }

    private fun configureMessagePreview(
        messagePreview: CometChatMessageBubble,
        baseMessage: BaseMessage
    ) {
        messagePreview.setTextFormatters(textFormatters)
        messagePreview.setAvatarVisibility(View.GONE)

        // Determine alignment for the bubble
        val loggedInUser = CometChatUIKit.getLoggedInUser()
        val bubbleAlignment = if (baseMessage.sender?.uid == loggedInUser?.uid) {
            UIKitConstants.MessageBubbleAlignment.RIGHT
        } else {
            UIKitConstants.MessageBubbleAlignment.LEFT
        }
        messagePreview.setMessage(baseMessage, bubbleAlignment)

        // Set margins to 0
        messagePreview.setMessagePadding(0, 0, 0, 0)

        // Transparent background
        messagePreview.setCardBackgroundColor(Color.TRANSPARENT)

        // Hide reply count bar
        messagePreview.setThreadViewVisibility(View.GONE)

        // Max height 350dp — constrain via layout params
        val maxHeightPx = Utils.convertDpToPx(context, 350)
        messagePreview.post {
            if (messagePreview.height > maxHeightPx) {
                val lp = messagePreview.layoutParams
                lp.height = maxHeightPx
                messagePreview.layoutParams = lp
            }
        }

        // Receipts visibility
        messagePreview.setReceiptsVisibility(receiptsVisibility)
    }

    private fun configureQuickReactions(
        reactionCard: MaterialCardView,
        viewReactions: LinearLayout,
        baseMessage: BaseMessage,
        isLeft: Boolean
    ) {
        val effectiveReactions = quickReactions.ifEmpty { DEFAULT_REACTIONS }
        val radiusMax = context.resources.getDimension(R.dimen.cometchat_radius_max)
        val margin = context.resources.getDimensionPixelSize(R.dimen.cometchat_margin)
        val emojiTextColor = CometChatTheme.getTextColorPrimary(context)

        // Add emoji chips with initial hidden state for staggered animation
        effectiveReactions.forEachIndexed { index, emoji ->
            val chipView = LayoutInflater.from(context)
                .inflate(R.layout.cometchat_quick_reaction_view, viewReactions, false)
            val chipCard = chipView.findViewById<MaterialCardView>(R.id.card_reaction_chip)
            val tvReaction = chipView.findViewById<TextView>(R.id.tv_reaction)

            // Reset card to remove default elevation/shadow
            Utils.initMaterialCard(chipCard)
            chipCard.radius = radiusMax
            chipCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(context))

            tvReaction.text = emoji
            tvReaction.setTextColor(emojiTextColor)

            val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            layoutParams.rightMargin = margin
            layoutParams.leftMargin = if (index == 0) 0 else margin
            chipView.layoutParams = layoutParams

            // Initial state: hidden for staggered animation
            chipView.alpha = 0f
            chipView.scaleX = 0.6f
            chipView.scaleY = 0.6f
            chipView.translationY = 20f

            chipView.setOnClickListener {
                reactionClickListener?.onReactionClick(baseMessage, emoji)
            }

            viewReactions.addView(chipView)

            // Staggered per-chip animation
            chipView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(200)
                .setStartDelay(index * 40L)
                .start()
        }

        // Add "add reaction" icon chip
        val addReactionView = LayoutInflater.from(context)
            .inflate(R.layout.cometchat_quick_reaction_view, viewReactions, false)
        val addReactionCard = addReactionView.findViewById<MaterialCardView>(R.id.card_reaction_chip)
        val tvAddReaction = addReactionView.findViewById<TextView>(R.id.tv_reaction)

        // Reset card to remove default elevation/shadow
        Utils.initMaterialCard(addReactionCard)
        addReactionCard.radius = radiusMax
        addReactionCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(context))

        val iconRes = if (addReactionIcon != 0) addReactionIcon else R.drawable.cometchat_add_reaction
        tvAddReaction.background = ResourcesCompat.getDrawable(context.resources, iconRes, context.theme)
        tvAddReaction.text = ""

        val addLayoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        addLayoutParams.leftMargin = margin
        addLayoutParams.rightMargin = 0
        addReactionView.layoutParams = addLayoutParams

        // Initial state for add reaction chip
        addReactionView.alpha = 0f
        addReactionView.scaleX = 0.6f
        addReactionView.scaleY = 0.6f
        addReactionView.translationY = 20f

        addReactionView.setOnClickListener {
            emojiPickerClickListener?.onEmojiPickerClick()
        }

        viewReactions.addView(addReactionView)

        // Animate add reaction chip last in the stagger sequence
        addReactionView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(200)
            .setStartDelay(effectiveReactions.size * 40L)
            .start()
    }

    // ========================================
    // Blur Overlay (Task 3.3)
    // ========================================

    private fun dimBackground(activity: Activity) {
        if (blurIv != null) return
        if (!isActivityUsable(activity)) return

        try {
            blurIv = ImageView(activity)
            val bitmap = captureScreen(activity)

            if (bitmap != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // API 31+: Use RenderEffect
                    blurIv?.setImageBitmap(bitmap)
                    blurIv?.setRenderEffect(
                        android.graphics.RenderEffect.createBlurEffect(
                            70f, 70f, Shader.TileMode.CLAMP
                        )
                    )
                } else {
                    // Pre-API 31: Use RenderScript blur fallback
                    val blurredBitmap = applyRenderScriptBlur(activity, bitmap)
                    blurIv?.setImageBitmap(blurredBitmap ?: bitmap)
                    blurIv?.alpha = 1f
                }
            } else {
                // Fallback: semi-transparent overlay
                blurIv?.setBackgroundColor(Color.argb(180, 0, 0, 0))
            }

            blurIv?.scaleType = ImageView.ScaleType.FIT_XY

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER

            activity.windowManager.addView(blurIv, params)
        } catch (e: Exception) {
            // If anything goes wrong, clean up
            blurIv = null
        }
    }

    private fun removeDimBackground(activity: Activity?) {
        if (blurIv == null || activity == null) return
        if (!isActivityUsable(activity)) {
            blurIv = null
            return
        }
        try {
            activity.windowManager.removeView(blurIv)
        } catch (_: Exception) {
            // View may already be removed
        }
        blurIv = null
    }

    /**
     * Captures the current screen content as a Bitmap.
     */
    private fun captureScreen(activity: Activity): Bitmap? {
        return try {
            val decorView = activity.window.decorView
            val width = decorView.width
            val height = decorView.height
            if (width <= 0 || height <= 0) return null

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            decorView.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Applies a blur effect to a bitmap using RenderScript (for API < 31).
     */
    @Suppress("DEPRECATION")
    private fun applyRenderScriptBlur(context: android.content.Context, bitmap: Bitmap): Bitmap? {
        return try {
            val rs = android.renderscript.RenderScript.create(context)
            val input = android.renderscript.Allocation.createFromBitmap(rs, bitmap)
            val output = android.renderscript.Allocation.createTyped(rs, input.type)
            val script = android.renderscript.ScriptIntrinsicBlur.create(
                rs, android.renderscript.Element.U8_4(rs)
            )
            // RenderScript max blur radius is 25f
            script.setRadius(25f)
            script.setInput(input)
            script.forEach(output)
            output.copyTo(bitmap)
            rs.destroy()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    // ========================================
    // Dismiss (Task 3.4)
    // ========================================

    /**
     * Dismisses the popup and removes the blur overlay.
     */
    fun dismiss() {
        try {
            popupWindow?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        } catch (_: Exception) {
            // Guard against window already dismissed
        }

        val activity = Utils.getActivity(context)
        removeDimBackground(activity)
        popupWindow = null
        currentMessage = null
    }

    /**
     * Returns the message currently displayed in the popup, if any.
     */
    fun getCurrentMessage(): BaseMessage? = currentMessage

    /**
     * Checks if an activity is usable (not null, not finishing, not destroyed).
     */
    private fun isActivityUsable(activity: Activity?): Boolean {
        if (activity == null) return false
        if (activity.isFinishing) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed) return false
        return true
    }
}
