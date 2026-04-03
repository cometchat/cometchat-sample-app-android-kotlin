package com.cometchat.uikit.kotlin.presentation.emojikeyboard.ui

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.adapter.EmojiAdapter
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.adapter.EmojiItemOnClick
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.model.EmojiCategory
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.model.EmojiRepository
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.tabs.TabLayout

/**
 * MaterialCardView-based emoji keyboard view that displays categorized emojis
 * with a vertically scrolling category list, a separator, and a bottom TabLayout.
 *
 * Direct 1:1 port of `EmojiKeyBoardView.java` from the Java chatuikit module.
 */
class EmojiKeyBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatEmojiKeyboardStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    /**
     * Interface for handling emoji click events.
     */
    interface OnClick {
        fun onClick(emoji: String)
        fun onLongClick(emoji: String)
    }

    companion object {
        private const val TAG = "EmojiKeyBoardView"
    }

    // Internal state
    private val emojiListRecyclerView: RecyclerView
    private val emojiAdapter: EmojiAdapter
    private var emojiCategories: List<EmojiCategory>?
    private val tabLayout: TabLayout
    private val linearLayoutManager: LinearLayoutManager
    private var isScrolling: Boolean = false
    private val separator: TextView
    private var onClick: OnClick? = null

    // Style properties
    @ColorInt private var categoryIconTint: Int = 0
    @ColorInt private var selectedCategoryIconTint: Int = 0
    @ColorInt private var selectedCategoryBackgroundColor: Int = 0
    @StyleRes private var categoryTextAppearance: Int = 0
    @ColorInt private var categoryTextColor: Int = 0
    @ColorInt private var separatorColor: Int = 0
    @ColorInt private var backgroundColor: Int = 0
    @StyleRes private var style: Int = 0
    @Dimension private var strokeWidthValue: Int = 0
    @ColorInt private var strokeColorValue: Int = 0
    @Dimension private var cornerRadius: Int = 0

    init {
        val view = View.inflate(context, R.layout.cometchat_emoji_keyboard_layout, null)
        Utils.initMaterialCard(this)
        tabLayout = view.findViewById(R.id.category_tab)
        separator = view.findViewById(R.id.separator)
        emojiListRecyclerView = view.findViewById(R.id.emoji_list_view)
        linearLayoutManager = LinearLayoutManager(context)
        emojiListRecyclerView.layoutManager = linearLayoutManager
        emojiListRecyclerView.setHasFixedSize(true)

        // Try to get cached emoji data; if null, register for async load callback
        val cachedCategories = EmojiRepository.getEmojiCategories()
        emojiCategories = cachedCategories
        emojiAdapter = EmojiAdapter(context, emojiCategories)
        emojiListRecyclerView.adapter = emojiAdapter

        if (cachedCategories == null || cachedCategories.isEmpty()) {
            EmojiRepository.setOnEmojisLoadedListener(object : EmojiRepository.OnEmojisLoadedListener {
                override fun onEmojisLoaded(categories: List<EmojiCategory>) {
                    emojiCategories = categories
                    emojiAdapter.updateCategories(categories)
                    setTabs()
                    EmojiRepository.setOnEmojisLoadedListener(null)
                }
            })
        }

        tabLayout.tabRippleColor = null
        emojiListRecyclerView.setItemViewCacheSize(10)

        emojiListRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()
                val tab = tabLayout.getTabAt(lastVisibleItemPosition)
                tab?.select()
            }
        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (!isScrolling) {
                    emojiListRecyclerView.scrollToPosition(tab.position)
                }
                updateTabState(tab, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateTabState(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // no-op
            }
        })

        applyStyleAttributes(attrs, defStyleAttr, 0)
        addView(view)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (tabLayout.tabCount == 0) {
            setTabs()
        }
    }

    // ==================== Tab Methods (stubs for tasks 5.2 and 5.5) ====================

    /**
     * Sets up the tabs for the emoji categories in the TabLayout.
     * Iterates through emoji categories and adds each as a tab.
     */
    private fun setTabs() {
        emojiCategories?.let { categories ->
            for (i in categories.indices) {
                addTabIcons(categories[i])
                val tab = tabLayout.getTabAt(i)
                if (tab != null) {
                    updateTabState(tab, i == 0)
                }
            }
        }
    }

    /**
     * Adds a tab icon for the specified emoji category to the TabLayout.
     * Full icon mapping will be implemented in task 5.2.
     */
    private fun addTabIcons(emojiCategory: EmojiCategory) {
        val id = emojiCategory.id
        val drawableId = when (id.lowercase()) {
            "people" -> R.drawable.cometchat_smileys
            "animals_and_nature" -> R.drawable.cometchat_animals
            "food_and_drink" -> R.drawable.cometchat_food
            "activity" -> R.drawable.cometchat_activity
            "travel_and_places" -> R.drawable.cometchat_travel
            "objects" -> R.drawable.cometchat_objects
            "symbols" -> R.drawable.cometchat_symbols
            "flags" -> R.drawable.cometchat_flags
            else -> {
                try {
                    emojiCategory.symbol
                } catch (e: Exception) {
                    0
                }
            }
        }
        tabLayout.addTab(tabLayout.newTab().setCustomView(createTabView(drawableId)))
    }

    /**
     * Creates a custom tab view with an icon for the specified drawable resource.
     */
    private fun createTabView(drawableId: Int): View {
        val view = View.inflate(context, R.layout.cometchat_emoji_tab_item, null)
        val icon: ImageView = view.findViewById(R.id.tabIcon)
        icon.setImageResource(drawableId)
        return view
    }

    /**
     * Updates the visual state of the specified tab based on whether it is selected.
     */
    private fun updateTabState(tab: TabLayout.Tab, isSelected: Boolean) {
        val customView = tab.customView ?: return
        val background: View = customView.findViewById(R.id.tabBackground)
        val icon: ImageView = customView.findViewById(R.id.tabIcon)

        if (isSelected) {
            background.backgroundTintList = ColorStateList.valueOf(selectedCategoryBackgroundColor)
            icon.setColorFilter(selectedCategoryIconTint, PorterDuff.Mode.SRC_IN)
        } else {
            background.backgroundTintList = ColorStateList.valueOf(CometChatTheme.getErrorColor(context))
            icon.setColorFilter(categoryIconTint, PorterDuff.Mode.SRC_IN)
        }
    }

    // ==================== Style Application ====================

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatEmojiKeyBoardView, defStyleAttr, defStyleRes
        )
        @StyleRes val styleRes = typedArray.getResourceId(
            R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardStyle, 0
        )
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatEmojiKeyBoardView, defStyleAttr, styleRes
        )
        extractAttributesAndApplyDefaults(typedArray)
    }

    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray) {
        try {
            setCategoryIconTint(
                typedArray.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCategoryIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                )
            )
            setSelectedCategoryIconTint(
                typedArray.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardSelectedCategoryIconTint,
                    CometChatTheme.getIconTintHighlight(context)
                )
            )
            setSelectedCategoryBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardSelectedCategoryBackgroundColor,
                    CometChatTheme.getExtendedPrimaryColor100(context)
                )
            )
            setCategoryTextAppearance(
                typedArray.getResourceId(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCategoryTextAppearance,
                    0
                )
            )
            setCategoryTextColor(
                typedArray.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCategoryTextColor,
                    CometChatTheme.getTextColorTertiary(context)
                )
            )
            setSeparatorColor(
                typedArray.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardSeparatorColor,
                    CometChatTheme.getStrokeColorDefault(context)
                )
            )
            setBackgroundColor(
                typedArray.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                )
            )
            setCornerRadius(
                typedArray.getDimensionPixelSize(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCornerRadius,
                    0
                )
            )
            setStrokeWidth(
                typedArray.getDimensionPixelSize(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardStrokeWidth,
                    0
                )
            )
            setStrokeColor(
                typedArray.getColor(
                    R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardStrokeColor,
                    CometChatTheme.getStrokeColorLight(context)
                )
            )
        } finally {
            typedArray.recycle()
        }
    }

    // ==================== Public API ====================

    fun setOnClick(onClick: OnClick?) {
        if (onClick != null) {
            this.onClick = onClick
            emojiAdapter.setOnClick(object : EmojiItemOnClick {
                override fun onClick(emoji: String) = onClick.onClick(emoji)
                override fun onLongClick(emoji: String) = onClick.onLongClick(emoji)
            })
        }
    }

    fun setStyle(@StyleRes style: Int) {
        if (style != 0) {
            this.style = style
            val typedArray = context.theme.obtainStyledAttributes(
                style, R.styleable.CometChatEmojiKeyBoardView
            )
            extractAttributesAndApplyDefaults(typedArray)
        }
    }

    fun setCornerRadius(@Dimension cornerRadius: Int) {
        this.cornerRadius = cornerRadius
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius.toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius.toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
            .build()
        super.setShapeAppearanceModel(shapeAppearanceModel)
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        this.backgroundColor = backgroundColor
        super.setCardBackgroundColor(backgroundColor)
    }

    fun setSeparatorColor(@ColorInt separatorColor: Int) {
        this.separatorColor = separatorColor
        separator.setBackgroundColor(separatorColor)
    }

    fun setCategoryTextColor(@ColorInt categoryTextColor: Int) {
        this.categoryTextColor = categoryTextColor
        emojiAdapter.setCategoryTextColor(categoryTextColor)
    }

    fun setCategoryTextAppearance(@StyleRes categoryTextAppearance: Int) {
        this.categoryTextAppearance = categoryTextAppearance
        emojiAdapter.setCategoryTextAppearance(categoryTextAppearance)
    }

    fun setSelectedCategoryBackgroundColor(@ColorInt backgroundColor: Int) {
        this.selectedCategoryBackgroundColor = backgroundColor
    }

    fun setSelectedCategoryIconTint(@ColorInt selectedCategoryIconTint: Int) {
        this.selectedCategoryIconTint = selectedCategoryIconTint
    }

    fun setCategoryIconTint(@ColorInt categoryIconTint: Int) {
        this.categoryIconTint = categoryIconTint
    }

    override fun setStrokeWidth(@Dimension strokeWidth: Int) {
        this.strokeWidthValue = strokeWidth
        super.setStrokeWidth(strokeWidth)
    }

    override fun setStrokeColor(strokeColor: Int) {
        this.strokeColorValue = strokeColor
        super.setStrokeColor(strokeColor)
    }

    // ==================== Getters ====================

    fun getCornerRadius(): Int = cornerRadius

    fun getStyle(): Int = style

    fun getBackgroundColor(): Int = backgroundColor

    fun getSeparatorColor(): Int = separatorColor

    fun getCategoryTextColor(): Int = categoryTextColor

    fun getCategoryTextAppearance(): Int = categoryTextAppearance

    fun getSelectedCategoryBackgroundColor(): Int = selectedCategoryBackgroundColor

    fun getSelectedCategoryIconTint(): Int = selectedCategoryIconTint

    fun getCategoryIconTint(): Int = categoryIconTint

    fun getOnClick(): OnClick? = onClick

    override fun getStrokeWidth(): Int = strokeWidthValue

    override fun getStrokeColor(): Int = strokeColorValue

    fun isScrolling(): Boolean = isScrolling

    fun getLinearLayoutManager(): LinearLayoutManager = linearLayoutManager

    fun getTabLayout(): TabLayout = tabLayout

    fun getEmojiCategories(): List<EmojiCategory>? = emojiCategories

    fun getEmojiAdapter(): EmojiAdapter = emojiAdapter

    fun getEmojiListRecyclerView(): RecyclerView = emojiListRecyclerView

    fun getSeparator(): TextView = separator
}
