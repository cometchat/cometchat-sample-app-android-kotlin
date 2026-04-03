package com.cometchat.uikit.kotlin.presentation.groupmembers.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatGroupMembers component (XML Views).
 *
 * This data class holds all styling properties parsed from XML styleable attributes
 * defined in `attr_cometchat_group_members.xml`. It covers container, toolbar, search,
 * list item, selection, separator, empty/error state, and nested component styles.
 *
 * Use [default] to create a style from the current theme, or [fromTypedArray] to
 * extract values from XML attributes.
 */
data class CometChatGroupMembersStyle(
    // Container
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    val backgroundDrawable: Drawable? = null,

    // Toolbar
    @ColorInt val titleTextColor: Int = 0,
    @StyleRes val titleTextAppearance: Int = 0,
    val backIcon: Drawable? = null,
    @ColorInt val backIconTint: Int = 0,

    // Search box
    @ColorInt val searchBackgroundColor: Int = 0,
    @ColorInt val searchTextColor: Int = 0,
    @StyleRes val searchTextAppearance: Int = 0,
    @ColorInt val searchPlaceholderColor: Int = 0,
    val searchStartIcon: Drawable? = null,
    @ColorInt val searchStartIconTint: Int = 0,
    val searchEndIcon: Drawable? = null,
    @ColorInt val searchEndIconTint: Int = 0,
    @Dimension val searchCornerRadius: Int = 0,
    @Dimension val searchStrokeWidth: Int = 0,
    @ColorInt val searchStrokeColor: Int = 0,

    // List item
    @ColorInt val itemTitleTextColor: Int = 0,
    @StyleRes val itemTitleTextAppearance: Int = 0,
    @ColorInt val selectedBackgroundColor: Int = 0,

    // Separator
    @ColorInt val separatorColor: Int = 0,

    // Empty state
    @ColorInt val emptyStateTitleTextColor: Int = 0,
    @ColorInt val emptyStateSubtitleTextColor: Int = 0,
    @StyleRes val emptyStateTitleTextAppearance: Int = 0,
    @StyleRes val emptyStateSubtitleTextAppearance: Int = 0,

    // Error state
    @ColorInt val errorStateTitleTextColor: Int = 0,
    @ColorInt val errorStateSubtitleTextColor: Int = 0,
    @StyleRes val errorStateTitleTextAppearance: Int = 0,
    @StyleRes val errorStateSubtitleTextAppearance: Int = 0,

    // Selection / Checkbox
    @Dimension val checkBoxStrokeWidth: Int = 0,
    @Dimension val checkBoxCornerRadius: Int = 0,
    @ColorInt val checkBoxStrokeColor: Int = 0,
    @ColorInt val checkBoxBackgroundColor: Int = 0,
    @ColorInt val checkBoxCheckedBackgroundColor: Int = 0,
    val checkBoxSelectIcon: Drawable? = null,
    @ColorInt val checkBoxSelectIconTint: Int = 0,
    val discardSelectionIcon: Drawable? = null,
    @ColorInt val discardSelectionIconTint: Int = 0,
    val submitSelectionIcon: Drawable? = null,
    @ColorInt val submitSelectionIconTint: Int = 0,

    // Nested component style resource IDs
    @StyleRes val avatarStyleResId: Int = 0,
    @StyleRes val statusIndicatorStyleResId: Int = 0,
    @StyleRes val scopeChangeStyleResId: Int = 0,

    // Resolved nested component styles
    val avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle(),
    val statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle()
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatGroupMembersStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatGroupMembersStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatGroupMembersStyle {
            return extractFromThemeStyle(context, R.attr.cometchatGroupMembersStyle)
        }

        /**
         * Extracts the group members style from a theme style attribute.
         */
        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatGroupMembersStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        /**
         * Extracts style values from a specific style resource ID.
         * If styleResId is 0, returns a style with CometChatTheme defaults.
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatGroupMembersStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatGroupMembers)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         * Handles TypedArray recycling internally.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatGroupMembersStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatGroupMembersStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Core extraction method that reads style values from a TypedArray.
         * Does NOT handle recycling — caller is responsible for recycling the TypedArray.
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatGroupMembersStyle {
            // Extract nested style resource IDs
            val avatarStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatGroupMembers_cometchatGroupMembersAvatarStyle, 0
            ) ?: 0
            val statusIndicatorStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatGroupMembers_cometchatGroupMembersStatusIndicatorStyle, 0
            ) ?: 0
            val scopeChangeStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatGroupMembers_cometchatGroupMemberScopeChangeStyle, 0
            ) ?: 0

            // Resolve nested component styles
            val avatarStyle = if (avatarStyleResId != 0) {
                val avatarTypedArray = context.theme.obtainStyledAttributes(
                    avatarStyleResId, R.styleable.CometChatAvatar
                )
                CometChatAvatarStyle.fromTypedArray(context, avatarTypedArray)
            } else {
                CometChatAvatarStyle.default(context)
            }

            val statusIndicatorStyle = if (statusIndicatorStyleResId != 0) {
                val statusIndicatorTypedArray = context.theme.obtainStyledAttributes(
                    statusIndicatorStyleResId, R.styleable.CometChatStatusIndicator
                )
                CometChatStatusIndicatorStyle.fromTypedArray(context, statusIndicatorTypedArray)
            } else {
                CometChatStatusIndicatorStyle.default(context)
            }

            return CometChatGroupMembersStyle(
                // Container
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersStrokeColor, 0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersStrokeWidth, 0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersCornerRadius, 0
                ) ?: 0,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersBackgroundDrawable
                ),

                // Toolbar
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading2Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading2Bold(context),
                backIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersBackIcon
                ),
                backIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersBackIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),

                // Search box
                searchBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                searchTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                searchTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                searchPlaceholderColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputPlaceHolderTextColor,
                    CometChatTheme.getTextColorTertiary(context)
                ) ?: CometChatTheme.getTextColorTertiary(context),
                searchStartIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputIcon
                ),
                searchStartIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputStartTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                searchEndIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputEndIcon
                ),
                searchEndIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputEndIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),
                searchCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputCornerRadius,
                    context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_max)
                ) ?: context.resources.getDimensionPixelSize(R.dimen.cometchat_radius_max),
                searchStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputStrokeWidth, 0
                ) ?: 0,
                searchStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputStrokeColor, 0
                ) ?: 0,

                // List item
                itemTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersItemTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                itemTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersItemTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading4Medium(context)
                ) ?: CometChatTheme.getTextAppearanceHeading4Medium(context),
                selectedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersItemSelectedBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),

                // Separator
                separatorColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSeparatorColor,
                    CometChatTheme.getStrokeColorLight(context)
                ) ?: CometChatTheme.getStrokeColorLight(context),

                // Empty state
                emptyStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersEmptyStateTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                emptyStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersEmptyStateSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                emptyStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersEmptyStateTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                emptyStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersEmptyStateSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Error state
                errorStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersErrorStateTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                errorStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersErrorStateSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                errorStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersErrorStateTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                errorStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersErrorStateSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Selection / Checkbox
                checkBoxStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxStrokeWidth, 0
                ) ?: 0,
                checkBoxCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxCornerRadius, 0
                ) ?: 0,
                checkBoxStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxStrokeColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context),
                checkBoxBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                checkBoxCheckedBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxCheckedBackgroundColor,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),
                checkBoxSelectIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxSelectIcon
                ),
                checkBoxSelectIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxSelectIconTint,
                    CometChatTheme.getColorWhite(context)
                ) ?: CometChatTheme.getColorWhite(context),
                discardSelectionIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersDiscardSelectionIcon
                ),
                discardSelectionIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersDiscardSelectionIconTint,
                    CometChatTheme.getIconTintPrimary(context)
                ) ?: CometChatTheme.getIconTintPrimary(context),
                submitSelectionIcon = typedArray?.getDrawable(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSubmitSelectionIcon
                ),
                submitSelectionIconTint = typedArray?.getColor(
                    R.styleable.CometChatGroupMembers_cometchatGroupMembersSubmitSelectionIconTint,
                    CometChatTheme.getPrimaryColor(context)
                ) ?: CometChatTheme.getPrimaryColor(context),

                // Nested component style resource IDs
                avatarStyleResId = avatarStyleResId,
                statusIndicatorStyleResId = statusIndicatorStyleResId,
                scopeChangeStyleResId = scopeChangeStyleResId,

                // Resolved nested component styles
                avatarStyle = avatarStyle,
                statusIndicatorStyle = statusIndicatorStyle
            )
        }
    }

    /**
     * Applies container-level styles to a MaterialCardView.
     *
     * Sets background color, stroke color/width, corner radius, and optional
     * background drawable on the given card view.
     *
     * @param cardView The MaterialCardView to style
     */
    fun applyToContainer(cardView: com.google.android.material.card.MaterialCardView) {
        if (backgroundColor != 0) cardView.setCardBackgroundColor(backgroundColor)
        if (strokeColor != 0) cardView.strokeColor = strokeColor
        if (strokeWidth > 0) cardView.strokeWidth = strokeWidth
        if (cornerRadius > 0) cardView.radius = cornerRadius.toFloat()
        backgroundDrawable?.let { cardView.background = it }
    }

    /**
     * Applies toolbar styles to a toolbar title TextView and back icon ImageView.
     *
     * @param titleView The TextView displaying the toolbar title
     * @param backIconView The ImageView displaying the back icon (nullable)
     */
    fun applyToToolbar(
        titleView: android.widget.TextView,
        backIconView: android.widget.ImageView? = null
    ) {
        if (titleTextColor != 0) titleView.setTextColor(titleTextColor)
        if (titleTextAppearance != 0) titleView.setTextAppearance(titleTextAppearance)

        backIconView?.let { iv ->
            backIcon?.let { iv.setImageDrawable(it) }
            if (backIconTint != 0) iv.setColorFilter(backIconTint)
        }
    }

    /**
     * Applies search box styles to the search-related views.
     *
     * @param searchContainer The container view for the search box
     * @param searchInput The EditText for search input
     * @param searchIconView The ImageView for the search start icon (nullable)
     * @param clearIconView The ImageView for the search end/clear icon (nullable)
     */
    fun applyToSearchBox(
        searchContainer: android.view.View,
        searchInput: android.widget.EditText,
        searchIconView: android.widget.ImageView? = null,
        clearIconView: android.widget.ImageView? = null
    ) {
        if (searchBackgroundColor != 0) searchContainer.setBackgroundColor(searchBackgroundColor)
        if (searchTextColor != 0) searchInput.setTextColor(searchTextColor)
        if (searchTextAppearance != 0) searchInput.setTextAppearance(searchTextAppearance)
        if (searchPlaceholderColor != 0) searchInput.setHintTextColor(searchPlaceholderColor)

        searchIconView?.let { iv ->
            searchStartIcon?.let { iv.setImageDrawable(it) }
            if (searchStartIconTint != 0) iv.setColorFilter(searchStartIconTint)
        }
        clearIconView?.let { iv ->
            searchEndIcon?.let { iv.setImageDrawable(it) }
            if (searchEndIconTint != 0) iv.setColorFilter(searchEndIconTint)
        }
    }

    /**
     * Applies list item styles to a member list item's title TextView.
     *
     * @param titleView The TextView displaying the member name
     */
    fun applyToListItem(titleView: android.widget.TextView) {
        if (itemTitleTextColor != 0) titleView.setTextColor(itemTitleTextColor)
        if (itemTitleTextAppearance != 0) titleView.setTextAppearance(itemTitleTextAppearance)
    }

    /**
     * Applies separator styles to a separator view.
     *
     * @param separatorView The View used as a list item separator
     */
    fun applyToSeparator(separatorView: android.view.View) {
        if (separatorColor != 0) separatorView.setBackgroundColor(separatorColor)
    }

    /**
     * Applies empty state styles to the empty state title and subtitle TextViews.
     *
     * @param titleView The TextView for the empty state title
     * @param subtitleView The TextView for the empty state subtitle (nullable)
     */
    fun applyToEmptyState(
        titleView: android.widget.TextView,
        subtitleView: android.widget.TextView? = null
    ) {
        if (emptyStateTitleTextColor != 0) titleView.setTextColor(emptyStateTitleTextColor)
        if (emptyStateTitleTextAppearance != 0) titleView.setTextAppearance(emptyStateTitleTextAppearance)
        subtitleView?.let { tv ->
            if (emptyStateSubtitleTextColor != 0) tv.setTextColor(emptyStateSubtitleTextColor)
            if (emptyStateSubtitleTextAppearance != 0) tv.setTextAppearance(emptyStateSubtitleTextAppearance)
        }
    }

    /**
     * Applies error state styles to the error state title and subtitle TextViews.
     *
     * @param titleView The TextView for the error state title
     * @param subtitleView The TextView for the error state subtitle (nullable)
     */
    fun applyToErrorState(
        titleView: android.widget.TextView,
        subtitleView: android.widget.TextView? = null
    ) {
        if (errorStateTitleTextColor != 0) titleView.setTextColor(errorStateTitleTextColor)
        if (errorStateTitleTextAppearance != 0) titleView.setTextAppearance(errorStateTitleTextAppearance)
        subtitleView?.let { tv ->
            if (errorStateSubtitleTextColor != 0) tv.setTextColor(errorStateSubtitleTextColor)
            if (errorStateSubtitleTextAppearance != 0) tv.setTextAppearance(errorStateSubtitleTextAppearance)
        }
    }
}
