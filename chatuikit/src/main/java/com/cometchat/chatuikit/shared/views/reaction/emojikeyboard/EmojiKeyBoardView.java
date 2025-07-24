package com.cometchat.chatuikit.shared.views.reaction.emojikeyboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.reaction.emojikeyboard.emojikeyboardadapters.EmojiAdapter;
import com.cometchat.chatuikit.shared.views.reaction.emojikeyboard.model.EmojiCategory;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class EmojiKeyBoardView extends MaterialCardView {
    private static final String TAG = EmojiKeyBoardView.class.getSimpleName();

    private RecyclerView emojiListRecyclerView;
    private EmojiAdapter emojiAdapter;
    private List<EmojiCategory> emojiCategories;
    private TabLayout tabLayout;
    private LinearLayoutManager linearLayoutManager;
    private boolean isScrolling;
    private TextView separator;
    private OnClick onClick;

    private @ColorInt int categoryIconTint;
    private @ColorInt int selectedCategoryIconTint;
    private @ColorInt int selectedCategoryBackgroundColor;
    private @StyleRes int categoryTextAppearance;
    private @ColorInt int categoryTextColor;
    private @ColorInt int separatorColor;
    private @ColorInt int backgroundColor;
    private @StyleRes int style;

    private @Dimension int strokeWidth;
    private @ColorInt int strokeColor;
    private @Dimension int cornerRadius;

    /**
     * Constructor that is called when creating the EmojiKeyBoardView
     * programmatically.
     *
     * @param context The Context in which the view is running.
     */
    public EmojiKeyBoardView(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating the EmojiKeyBoardView from XML.
     *
     * @param context The Context in which the view is running.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public EmojiKeyBoardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatEmojiKeyboardStyle);
    }

    /**
     * Constructor that allows for defining a default style attribute when inflating
     * the EmojiKeyBoardView from XML.
     *
     * @param context      The Context in which the view is running.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a
     *                     style resource that supplies default values for the view.
     */
    public EmojiKeyBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    /**
     * Initializes the EmojiKeyBoardView by inflating the layout, setting up the
     * emoji categories, and configuring the RecyclerView and TabLayout components.
     *
     * @param attrs        The attributes from the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a
     *                     style resource that supplies default values for the view.
     */
    private void init(AttributeSet attrs, int defStyleAttr) {
        View view = View.inflate(getContext(), R.layout.cometchat_emoji_keyboard_layout, null);
        Utils.initMaterialCard(this);
        emojiCategories = EmojiKeyboardUtils.getEmojiCategories();
        tabLayout = view.findViewById(R.id.category_tab);
        separator = view.findViewById(R.id.separator);
        emojiListRecyclerView = view.findViewById(R.id.emoji_list_view);
        linearLayoutManager = new LinearLayoutManager(getContext());
        emojiListRecyclerView.setLayoutManager(linearLayoutManager);
        emojiListRecyclerView.setHasFixedSize(true);
        emojiAdapter = new EmojiAdapter(getContext(), emojiCategories);
        emojiListRecyclerView.setAdapter(emojiAdapter);
        tabLayout.setTabRippleColor(null);
        emojiListRecyclerView.setItemViewCacheSize(10);
        emojiListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                TabLayout.Tab tab = tabLayout.getTabAt(lastVisibleItemPosition);
                if (tab != null) {
                    tab.select();
                }
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (!isScrolling) {
                    emojiListRecyclerView.scrollToPosition(tab.getPosition());
                }
                updateTabState(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabState(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        applyStyleAttributes(attrs, defStyleAttr, 0);
        addView(view);
    }

    /**
     * Updates the visual state of the specified tab based on whether it is selected
     * or not.
     *
     * @param tab        The tab to update.
     * @param isSelected True if the tab is selected; otherwise, false.
     */
    private void updateTabState(TabLayout.Tab tab, boolean isSelected) {
        View customView = tab.getCustomView();
        if (customView != null) {
            View background = customView.findViewById(R.id.tabBackground);
            ImageView icon = customView.findViewById(R.id.tabIcon);

            if (isSelected) {
                background.setBackgroundTintList(ColorStateList.valueOf(selectedCategoryBackgroundColor));
                icon.setColorFilter(selectedCategoryIconTint, PorterDuff.Mode.SRC_IN);
            } else {
                background.setBackgroundTintList(ColorStateList.valueOf(CometChatTheme.getErrorColor(getContext())));
                icon.setColorFilter(categoryIconTint, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.CometChatEmojiKeyBoardView, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatEmojiKeyBoardView, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Extracts attributes from the given {@link TypedArray} and applies default
     * values.
     *
     * @param typedArray The TypedArray containing the view's attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setCategoryIconTint(typedArray.getColor(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCategoryIconTint,
                                                    CometChatTheme.getIconTintSecondary(getContext())));
            setSelectedCategoryIconTint(typedArray.getColor(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardSelectedCategoryIconTint,
                                                            CometChatTheme.getIconTintHighlight(getContext())));
            setSelectedCategoryBackgroundColor(typedArray.getColor(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardSelectedCategoryBackgroundColor,
                                                                   CometChatTheme.getExtendedPrimaryColor100(getContext())));
            setCategoryTextAppearance(typedArray.getResourceId(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCategoryTextAppearance,
                                                               0));
            setCategoryTextColor(typedArray.getColor(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCategoryTextColor,
                                                     CometChatTheme.getTextColorTertiary(getContext())));
            setSeparatorColor(typedArray.getColor(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardSeparatorColor,
                                                  CometChatTheme.getStrokeColorDefault(getContext())));
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardBackgroundColor,
                                                   CometChatTheme.getBackgroundColor1(getContext())));
            setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardCornerRadius, 0));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardStrokeWidth, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatEmojiKeyBoardView_cometchatEmojiKeyboardStrokeColor,
                                               CometChatTheme.getStrokeColorLight(getContext())));
        } finally {
            typedArray.recycle();
        }
    }

    public @Dimension int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0)
            .build();
        super.setShapeAppearanceModel(shapeAppearanceModel);
    }    /**
     * Sets up the tabs for the emoji categories in the TabLayout. This method
     * iterates through the list of emoji categories and adds each category as a
     * tab.
     */
    private void setTabs() {
        for (int i = 0; i < emojiCategories.size(); i++) {
            addTabIcons(emojiCategories.get(i));
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                updateTabState(tab, i == 0);
            }
        }
    }

    // getters to get direct access to the attributes and components
    public int getStyle() {
        return style;
    }

    /**
     * Sets the style of the text bubble from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatEmojiKeyBoardView);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }    /**
     * Adds a tab icon for the specified emoji category to the TabLayout.
     *
     * @param emojiCategory The emoji category for which the tab icon is to be added.
     */
    private void addTabIcons(EmojiCategory emojiCategory) {
        String id = emojiCategory.getId();
        int drawableId;

        // Determine the drawable resource based on the category ID
        switch (id.toLowerCase()) {
            case "people":
                drawableId = R.drawable.cometchat_smileys;
                break;
            case "animals_and_nature":
                drawableId = R.drawable.cometchat_animals;
                break;
            case "food_and_drink":
                drawableId = R.drawable.cometchat_food;
                break;
            case "activity":
                drawableId = R.drawable.cometchat_activity;
                break;
            case "travel_and_places":
                drawableId = R.drawable.cometchat_travel;
                break;
            case "objects":
                drawableId = R.drawable.cometchat_objects;
                break;
            case "symbols":
                drawableId = R.drawable.cometchat_symbols;
                break;
            case "flags":
                drawableId = R.drawable.cometchat_flags;
                break;
            default:
                // Fallback to using the category symbol as the drawable resource
                try {
                    drawableId = emojiCategory.getSymbol();
                } catch (Exception e) {
                    drawableId = 0; // or a default drawable resource
                    CometChatLogger.e(TAG, e.toString());
                }
                break;
        }

        tabLayout.addTab(tabLayout.newTab().setCustomView(createTabView(drawableId)));
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for the EmojiKeyBoardView.
     *
     * @param backgroundColor The color to be used as the background for the view.
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
    }

    public int getSeparatorColor() {
        return separatorColor;
    }    /**
     * Creates a custom tab view with an icon for the specified drawable resource.
     *
     * @param drawableId The resource ID of the drawable to use as the icon.
     * @return A View representing the custom tab.
     */
    private View createTabView(int drawableId) {
        // Inflate a custom view (you need to create tab_item.xml layout in res/layout)
        View view = View.inflate(getContext(), R.layout.cometchat_emoji_tab_item, null);
        ImageView icon = view.findViewById(R.id.tabIcon);
        icon.setImageResource(drawableId);
        return view;
    }

    /**
     * Sets the color for the separator view.
     *
     * @param separatorColor The color to be used for the separator.
     */
    public void setSeparatorColor(@ColorInt int separatorColor) {
        this.separatorColor = separatorColor;
        separator.setBackgroundColor(separatorColor);
    }

    public int getCategoryTextColor() {
        return categoryTextColor;
    }

    /**
     * Sets the text color for category names in the emoji adapter.
     *
     * @param categoryTextColor The color to be used for category text.
     */
    public void setCategoryTextColor(@ColorInt int categoryTextColor) {
        this.categoryTextColor = categoryTextColor;
        emojiAdapter.setCategoryTextColor(categoryTextColor);
    }    /**
     * Called when the view is attached to a window. If the TabLayout does not have
     * any tabs, it sets up the tabs.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (tabLayout.getTabCount() == 0) {
            setTabs();
        }
    }

    public int getCategoryTextAppearance() {
        return categoryTextAppearance;
    }

    /**
     * Sets the text appearance for category names in the emoji adapter.
     *
     * @param categoryTextAppearance The style resource ID for the text appearance.
     */
    public void setCategoryTextAppearance(@StyleRes int categoryTextAppearance) {
        this.categoryTextAppearance = categoryTextAppearance;
        emojiAdapter.setCategoryTextAppearance(categoryTextAppearance);
    }

    public int getSelectedCategoryBackgroundColor() {
        return selectedCategoryBackgroundColor;
    }

    /**
     * Sets the background color for the selected category tab.
     *
     * @param backgroundColor The color to be used as the background for the selected category.
     */
    public void setSelectedCategoryBackgroundColor(@ColorInt int backgroundColor) {
        this.selectedCategoryBackgroundColor = backgroundColor;
    }

    public int getSelectedCategoryIconTint() {
        return selectedCategoryIconTint;
    }

    /**
     * Sets the tint color for the selected category icon.
     *
     * @param selectedCategoryIconTint The color to be used as the tint for the selected category icon.
     */
    public void setSelectedCategoryIconTint(@ColorInt int selectedCategoryIconTint) {
        this.selectedCategoryIconTint = selectedCategoryIconTint;
    }

    public int getCategoryIconTint() {
        return categoryIconTint;
    }

    /**
     * Sets the tint color for category icons.
     *
     * @param categoryIconTint The color to be used as the tint for category icons.
     */
    public void setCategoryIconTint(@ColorInt int categoryIconTint) {
        this.categoryIconTint = categoryIconTint;
    }

    public OnClick getOnClick() {
        return onClick;
    }    public @Dimension int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets a click listener for emoji categories.
     *
     * @param onClick The click listener to be set.
     */
    public void setOnClick(OnClick onClick) {
        if (onClick != null) {
            this.onClick = onClick;
            emojiAdapter.setOnClick(onClick);
        }
    }

    public TextView getSeparator() {
        return separator;
    }    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    public LinearLayoutManager getLinearLayoutManager() {
        return linearLayoutManager;
    }    @Override
    public int getStrokeColor() {
        return strokeColor;
    }

    public TabLayout getTabLayout() {
        return tabLayout;
    }

    public List<EmojiCategory> getEmojiCategories() {
        return emojiCategories;
    }    @Override
    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }

    public EmojiAdapter getEmojiAdapter() {
        return emojiAdapter;
    }

    public RecyclerView getEmojiListRecyclerView() {
        return emojiListRecyclerView;
    }

    /**
     * Interface for handling emoji click events.
     *
     * <p>
     * Implement this interface to respond to emoji click and long click events in
     * the EmojiKeyboardView. This allows customization of the behavior when an
     * emoji is selected or long-pressed by the user.
     */
    public interface OnClick {

        /**
         * Called when an emoji is clicked.
         *
         * @param emoji The emoji that was clicked.
         */
        void onClick(String emoji);

        /**
         * Called when an emoji is long-clicked.
         *
         * @param emoji The emoji that was long-clicked.
         */
        void onLongClick(String emoji);
    }


















}
