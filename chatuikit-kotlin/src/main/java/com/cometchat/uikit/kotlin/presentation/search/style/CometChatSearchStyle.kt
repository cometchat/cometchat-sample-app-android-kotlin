package com.cometchat.uikit.kotlin.presentation.search.style

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount.CometChatBadgeCountStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle

/**
 * Style configuration for CometChatSearch component.
 *
 * This class encapsulates all visual styling properties for the search component,
 * including container, search bar, filter chips, section headers, date separators,
 * conversation items, message items, and state views.
 */
class CometChatSearchStyle private constructor(
    // Container styling
    @ColorInt val backgroundColor: Int?,

    // Search bar styling
    @ColorInt val searchBarBackgroundColor: Int?,
    @ColorInt val searchBarStrokeColor: Int?,
    val searchBarStrokeWidth: Float?,
    val searchBarCornerRadius: Float?,
    @ColorInt val searchBarTextColor: Int?,
    @StyleRes val searchBarTextAppearance: Int?,
    @ColorInt val searchBarHintTextColor: Int?,
    @StyleRes val searchBarHintTextAppearance: Int?,

    // Search bar icons
    val backIcon: Drawable?,
    @ColorInt val backIconTint: Int?,
    val clearIcon: Drawable?,
    @ColorInt val clearIconTint: Int?,
    val searchIcon: Drawable?,
    @ColorInt val searchIconTint: Int?,

    // Filter chip styling
    @ColorInt val filterChipBackgroundColor: Int?,
    @ColorInt val filterChipSelectedBackgroundColor: Int?,
    @ColorInt val filterChipTextColor: Int?,
    @ColorInt val filterChipSelectedTextColor: Int?,
    @StyleRes val filterChipTextAppearance: Int?,
    @ColorInt val filterChipStrokeColor: Int?,
    @ColorInt val filterChipSelectedStrokeColor: Int?,
    val filterChipStrokeWidth: Float?,
    val filterChipCornerRadius: Float?,

    // Section header styling
    @ColorInt val sectionHeaderTextColor: Int?,
    @StyleRes val sectionHeaderTextAppearance: Int?,
    @ColorInt val sectionHeaderBackgroundColor: Int?,

    // Conversation item styling
    @ColorInt val conversationItemBackgroundColor: Int?,
    @ColorInt val conversationTitleTextColor: Int?,
    @StyleRes val conversationTitleTextAppearance: Int?,
    @ColorInt val conversationSubtitleTextColor: Int?,
    @StyleRes val conversationSubtitleTextAppearance: Int?,
    @ColorInt val conversationTimestampTextColor: Int?,
    @StyleRes val conversationTimestampTextAppearance: Int?,
    @ColorInt val conversationSeparatorColor: Int?,

    // Message item styling
    @ColorInt val messageItemBackgroundColor: Int?,
    @ColorInt val messageTitleTextColor: Int?,
    @StyleRes val messageTitleTextAppearance: Int?,
    @ColorInt val messageSubtitleTextColor: Int?,
    @StyleRes val messageSubtitleTextAppearance: Int?,
    @ColorInt val messageTimestampTextColor: Int?,
    @StyleRes val messageTimestampTextAppearance: Int?,
    @ColorInt val messageSeparatorColor: Int?,
    @ColorInt val messageLinkTextColor: Int?,
    @StyleRes val messageLinkTextAppearance: Int?,
    val messageThreadIcon: Drawable?,
    @ColorInt val messageThreadIconTint: Int?,

    // Date separator styling
    @ColorInt val dateSeparatorBackgroundColor: Int?,
    @ColorInt val dateSeparatorTextColor: Int?,
    @StyleRes val dateSeparatorTextAppearance: Int?,
    val dateSeparatorStyle: CometChatDateStyle?,

    // Avatar styling
    val avatarStyle: CometChatAvatarStyle?,

    // Badge styling
    val badgeStyle: CometChatBadgeCountStyle?,

    // Status indicator styling
    val statusIndicatorStyle: CometChatStatusIndicatorStyle?,

    // Loading state styling
    @ColorInt val loadingStateBackgroundColor: Int?,

    // Empty state styling
    @ColorInt val emptyStateTextColor: Int?,
    @StyleRes val emptyStateTextAppearance: Int?,
    @ColorInt val emptyStateSubtitleTextColor: Int?,
    @StyleRes val emptyStateSubtitleTextAppearance: Int?,
    val emptyStateIcon: Drawable?,
    @ColorInt val emptyStateIconTint: Int?,

    // Initial state styling
    @ColorInt val initialStateTextColor: Int?,
    @StyleRes val initialStateTextAppearance: Int?,
    @ColorInt val initialStateSubtitleTextColor: Int?,
    @StyleRes val initialStateSubtitleTextAppearance: Int?,
    val initialStateIcon: Drawable?,
    @ColorInt val initialStateIconTint: Int?,

    // Error state styling
    @ColorInt val errorStateTextColor: Int?,
    @StyleRes val errorStateTextAppearance: Int?,
    @ColorInt val errorStateSubtitleTextColor: Int?,
    @StyleRes val errorStateSubtitleTextAppearance: Int?,
    val errorStateIcon: Drawable?,
    @ColorInt val errorStateIconTint: Int?,

    // See more button styling
    @ColorInt val seeMoreTextColor: Int?,
    @StyleRes val seeMoreTextAppearance: Int?
) {

    /**
     * Builder for creating CometChatSearchStyle instances.
     */
    class Builder(private val context: Context) {
        // Container styling
        @ColorInt private var backgroundColor: Int? = null

        // Search bar styling
        @ColorInt private var searchBarBackgroundColor: Int? = null
        @ColorInt private var searchBarStrokeColor: Int? = null
        private var searchBarStrokeWidth: Float? = null
        private var searchBarCornerRadius: Float? = null
        @ColorInt private var searchBarTextColor: Int? = null
        @StyleRes private var searchBarTextAppearance: Int? = null
        @ColorInt private var searchBarHintTextColor: Int? = null
        @StyleRes private var searchBarHintTextAppearance: Int? = null

        // Search bar icons
        private var backIcon: Drawable? = null
        @ColorInt private var backIconTint: Int? = null
        private var clearIcon: Drawable? = null
        @ColorInt private var clearIconTint: Int? = null
        private var searchIcon: Drawable? = null
        @ColorInt private var searchIconTint: Int? = null

        // Filter chip styling
        @ColorInt private var filterChipBackgroundColor: Int? = null
        @ColorInt private var filterChipSelectedBackgroundColor: Int? = null
        @ColorInt private var filterChipTextColor: Int? = null
        @ColorInt private var filterChipSelectedTextColor: Int? = null
        @StyleRes private var filterChipTextAppearance: Int? = null
        @ColorInt private var filterChipStrokeColor: Int? = null
        @ColorInt private var filterChipSelectedStrokeColor: Int? = null
        private var filterChipStrokeWidth: Float? = null
        private var filterChipCornerRadius: Float? = null

        // Section header styling
        @ColorInt private var sectionHeaderTextColor: Int? = null
        @StyleRes private var sectionHeaderTextAppearance: Int? = null
        @ColorInt private var sectionHeaderBackgroundColor: Int? = null

        // Conversation item styling
        @ColorInt private var conversationItemBackgroundColor: Int? = null
        @ColorInt private var conversationTitleTextColor: Int? = null
        @StyleRes private var conversationTitleTextAppearance: Int? = null
        @ColorInt private var conversationSubtitleTextColor: Int? = null
        @StyleRes private var conversationSubtitleTextAppearance: Int? = null
        @ColorInt private var conversationTimestampTextColor: Int? = null
        @StyleRes private var conversationTimestampTextAppearance: Int? = null
        @ColorInt private var conversationSeparatorColor: Int? = null

        // Message item styling
        @ColorInt private var messageItemBackgroundColor: Int? = null
        @ColorInt private var messageTitleTextColor: Int? = null
        @StyleRes private var messageTitleTextAppearance: Int? = null
        @ColorInt private var messageSubtitleTextColor: Int? = null
        @StyleRes private var messageSubtitleTextAppearance: Int? = null
        @ColorInt private var messageTimestampTextColor: Int? = null
        @StyleRes private var messageTimestampTextAppearance: Int? = null
        @ColorInt private var messageSeparatorColor: Int? = null
        @ColorInt private var messageLinkTextColor: Int? = null
        @StyleRes private var messageLinkTextAppearance: Int? = null
        private var messageThreadIcon: Drawable? = null
        @ColorInt private var messageThreadIconTint: Int? = null

        // Date separator styling
        @ColorInt private var dateSeparatorBackgroundColor: Int? = null
        @ColorInt private var dateSeparatorTextColor: Int? = null
        @StyleRes private var dateSeparatorTextAppearance: Int? = null
        private var dateSeparatorStyle: CometChatDateStyle? = null

        // Avatar styling
        private var avatarStyle: CometChatAvatarStyle? = null

        // Badge styling
        private var badgeStyle: CometChatBadgeCountStyle? = null

        // Status indicator styling
        private var statusIndicatorStyle: CometChatStatusIndicatorStyle? = null

        // Loading state styling
        @ColorInt private var loadingStateBackgroundColor: Int? = null

        // Empty state styling
        @ColorInt private var emptyStateTextColor: Int? = null
        @StyleRes private var emptyStateTextAppearance: Int? = null
        @ColorInt private var emptyStateSubtitleTextColor: Int? = null
        @StyleRes private var emptyStateSubtitleTextAppearance: Int? = null
        private var emptyStateIcon: Drawable? = null
        @ColorInt private var emptyStateIconTint: Int? = null

        // Initial state styling
        @ColorInt private var initialStateTextColor: Int? = null
        @StyleRes private var initialStateTextAppearance: Int? = null
        @ColorInt private var initialStateSubtitleTextColor: Int? = null
        @StyleRes private var initialStateSubtitleTextAppearance: Int? = null
        private var initialStateIcon: Drawable? = null
        @ColorInt private var initialStateIconTint: Int? = null

        // Error state styling
        @ColorInt private var errorStateTextColor: Int? = null
        @StyleRes private var errorStateTextAppearance: Int? = null
        @ColorInt private var errorStateSubtitleTextColor: Int? = null
        @StyleRes private var errorStateSubtitleTextAppearance: Int? = null
        private var errorStateIcon: Drawable? = null
        @ColorInt private var errorStateIconTint: Int? = null

        // See more button styling
        @ColorInt private var seeMoreTextColor: Int? = null
        @StyleRes private var seeMoreTextAppearance: Int? = null

        // Builder methods
        fun setBackgroundColor(@ColorInt color: Int) = apply { backgroundColor = color }
        fun setSearchBarBackgroundColor(@ColorInt color: Int) = apply { searchBarBackgroundColor = color }
        fun setSearchBarStrokeColor(@ColorInt color: Int) = apply { searchBarStrokeColor = color }
        fun setSearchBarStrokeWidth(width: Float) = apply { searchBarStrokeWidth = width }
        fun setSearchBarCornerRadius(radius: Float) = apply { searchBarCornerRadius = radius }
        fun setSearchBarTextColor(@ColorInt color: Int) = apply { searchBarTextColor = color }
        fun setSearchBarTextAppearance(@StyleRes appearance: Int) = apply { searchBarTextAppearance = appearance }
        fun setSearchBarHintTextColor(@ColorInt color: Int) = apply { searchBarHintTextColor = color }
        fun setSearchBarHintTextAppearance(@StyleRes appearance: Int) = apply { searchBarHintTextAppearance = appearance }
        fun setBackIcon(icon: Drawable?) = apply { backIcon = icon }
        fun setBackIconTint(@ColorInt color: Int) = apply { backIconTint = color }
        fun setClearIcon(icon: Drawable?) = apply { clearIcon = icon }
        fun setClearIconTint(@ColorInt color: Int) = apply { clearIconTint = color }
        fun setSearchIcon(icon: Drawable?) = apply { searchIcon = icon }
        fun setSearchIconTint(@ColorInt color: Int) = apply { searchIconTint = color }
        fun setFilterChipBackgroundColor(@ColorInt color: Int) = apply { filterChipBackgroundColor = color }
        fun setFilterChipSelectedBackgroundColor(@ColorInt color: Int) = apply { filterChipSelectedBackgroundColor = color }
        fun setFilterChipTextColor(@ColorInt color: Int) = apply { filterChipTextColor = color }
        fun setFilterChipSelectedTextColor(@ColorInt color: Int) = apply { filterChipSelectedTextColor = color }
        fun setFilterChipTextAppearance(@StyleRes appearance: Int) = apply { filterChipTextAppearance = appearance }
        fun setFilterChipStrokeColor(@ColorInt color: Int) = apply { filterChipStrokeColor = color }
        fun setFilterChipSelectedStrokeColor(@ColorInt color: Int) = apply { filterChipSelectedStrokeColor = color }
        fun setFilterChipStrokeWidth(width: Float) = apply { filterChipStrokeWidth = width }
        fun setFilterChipCornerRadius(radius: Float) = apply { filterChipCornerRadius = radius }
        fun setSectionHeaderTextColor(@ColorInt color: Int) = apply { sectionHeaderTextColor = color }
        fun setSectionHeaderTextAppearance(@StyleRes appearance: Int) = apply { sectionHeaderTextAppearance = appearance }
        fun setSectionHeaderBackgroundColor(@ColorInt color: Int) = apply { sectionHeaderBackgroundColor = color }
        fun setConversationItemBackgroundColor(@ColorInt color: Int) = apply { conversationItemBackgroundColor = color }
        fun setConversationTitleTextColor(@ColorInt color: Int) = apply { conversationTitleTextColor = color }
        fun setConversationTitleTextAppearance(@StyleRes appearance: Int) = apply { conversationTitleTextAppearance = appearance }
        fun setConversationSubtitleTextColor(@ColorInt color: Int) = apply { conversationSubtitleTextColor = color }
        fun setConversationSubtitleTextAppearance(@StyleRes appearance: Int) = apply { conversationSubtitleTextAppearance = appearance }
        fun setConversationTimestampTextColor(@ColorInt color: Int) = apply { conversationTimestampTextColor = color }
        fun setConversationTimestampTextAppearance(@StyleRes appearance: Int) = apply { conversationTimestampTextAppearance = appearance }
        fun setConversationSeparatorColor(@ColorInt color: Int) = apply { conversationSeparatorColor = color }
        fun setMessageItemBackgroundColor(@ColorInt color: Int) = apply { messageItemBackgroundColor = color }
        fun setMessageTitleTextColor(@ColorInt color: Int) = apply { messageTitleTextColor = color }
        fun setMessageTitleTextAppearance(@StyleRes appearance: Int) = apply { messageTitleTextAppearance = appearance }
        fun setMessageSubtitleTextColor(@ColorInt color: Int) = apply { messageSubtitleTextColor = color }
        fun setMessageSubtitleTextAppearance(@StyleRes appearance: Int) = apply { messageSubtitleTextAppearance = appearance }
        fun setMessageTimestampTextColor(@ColorInt color: Int) = apply { messageTimestampTextColor = color }
        fun setMessageTimestampTextAppearance(@StyleRes appearance: Int) = apply { messageTimestampTextAppearance = appearance }
        fun setMessageSeparatorColor(@ColorInt color: Int) = apply { messageSeparatorColor = color }
        fun setMessageLinkTextColor(@ColorInt color: Int) = apply { messageLinkTextColor = color }
        fun setMessageLinkTextAppearance(@StyleRes appearance: Int) = apply { messageLinkTextAppearance = appearance }
        fun setMessageThreadIcon(icon: Drawable?) = apply { messageThreadIcon = icon }
        fun setMessageThreadIconTint(@ColorInt color: Int) = apply { messageThreadIconTint = color }
        fun setDateSeparatorBackgroundColor(@ColorInt color: Int) = apply { dateSeparatorBackgroundColor = color }
        fun setDateSeparatorTextColor(@ColorInt color: Int) = apply { dateSeparatorTextColor = color }
        fun setDateSeparatorTextAppearance(@StyleRes appearance: Int) = apply { dateSeparatorTextAppearance = appearance }
        fun setDateSeparatorStyle(style: CometChatDateStyle?) = apply { dateSeparatorStyle = style }
        fun setAvatarStyle(style: CometChatAvatarStyle?) = apply { avatarStyle = style }
        fun setBadgeStyle(style: CometChatBadgeCountStyle?) = apply { badgeStyle = style }
        fun setStatusIndicatorStyle(style: CometChatStatusIndicatorStyle?) = apply { statusIndicatorStyle = style }
        fun setLoadingStateBackgroundColor(@ColorInt color: Int) = apply { loadingStateBackgroundColor = color }
        fun setEmptyStateTextColor(@ColorInt color: Int) = apply { emptyStateTextColor = color }
        fun setEmptyStateTextAppearance(@StyleRes appearance: Int) = apply { emptyStateTextAppearance = appearance }
        fun setEmptyStateSubtitleTextColor(@ColorInt color: Int) = apply { emptyStateSubtitleTextColor = color }
        fun setEmptyStateSubtitleTextAppearance(@StyleRes appearance: Int) = apply { emptyStateSubtitleTextAppearance = appearance }
        fun setEmptyStateIcon(icon: Drawable?) = apply { emptyStateIcon = icon }
        fun setEmptyStateIconTint(@ColorInt color: Int) = apply { emptyStateIconTint = color }
        fun setInitialStateTextColor(@ColorInt color: Int) = apply { initialStateTextColor = color }
        fun setInitialStateTextAppearance(@StyleRes appearance: Int) = apply { initialStateTextAppearance = appearance }
        fun setInitialStateSubtitleTextColor(@ColorInt color: Int) = apply { initialStateSubtitleTextColor = color }
        fun setInitialStateSubtitleTextAppearance(@StyleRes appearance: Int) = apply { initialStateSubtitleTextAppearance = appearance }
        fun setInitialStateIcon(icon: Drawable?) = apply { initialStateIcon = icon }
        fun setInitialStateIconTint(@ColorInt color: Int) = apply { initialStateIconTint = color }
        fun setErrorStateTextColor(@ColorInt color: Int) = apply { errorStateTextColor = color }
        fun setErrorStateTextAppearance(@StyleRes appearance: Int) = apply { errorStateTextAppearance = appearance }
        fun setErrorStateSubtitleTextColor(@ColorInt color: Int) = apply { errorStateSubtitleTextColor = color }
        fun setErrorStateSubtitleTextAppearance(@StyleRes appearance: Int) = apply { errorStateSubtitleTextAppearance = appearance }
        fun setErrorStateIcon(icon: Drawable?) = apply { errorStateIcon = icon }
        fun setErrorStateIconTint(@ColorInt color: Int) = apply { errorStateIconTint = color }
        fun setSeeMoreTextColor(@ColorInt color: Int) = apply { seeMoreTextColor = color }
        fun setSeeMoreTextAppearance(@StyleRes appearance: Int) = apply { seeMoreTextAppearance = appearance }

        /**
         * Builds the CometChatSearchStyle instance.
         * Sets default text colors from CometChatTheme when not explicitly provided,
         * matching the Java reference implementation behavior.
         */
        fun build(): CometChatSearchStyle {
            // Set default text colors from CometChatTheme if not explicitly set
            // This matches the Java reference implementation behavior
            val defaultConversationTitleTextColor = conversationTitleTextColor 
                ?: CometChatTheme.getTextColorPrimary(context)
            val defaultConversationSubtitleTextColor = conversationSubtitleTextColor 
                ?: CometChatTheme.getTextColorSecondary(context)
            val defaultConversationTimestampTextColor = conversationTimestampTextColor 
                ?: CometChatTheme.getTextColorSecondary(context)
            val defaultMessageTitleTextColor = messageTitleTextColor 
                ?: CometChatTheme.getTextColorPrimary(context)
            val defaultMessageSubtitleTextColor = messageSubtitleTextColor 
                ?: CometChatTheme.getTextColorSecondary(context)
            val defaultMessageTimestampTextColor = messageTimestampTextColor 
                ?: CometChatTheme.getTextColorSecondary(context)
            val defaultMessageLinkTextColor = messageLinkTextColor 
                ?: CometChatTheme.getInfoColor(context)
            val defaultSectionHeaderTextColor = sectionHeaderTextColor 
                ?: CometChatTheme.getTextColorSecondary(context)

            return CometChatSearchStyle(
                backgroundColor = backgroundColor,
                searchBarBackgroundColor = searchBarBackgroundColor,
                searchBarStrokeColor = searchBarStrokeColor,
                searchBarStrokeWidth = searchBarStrokeWidth,
                searchBarCornerRadius = searchBarCornerRadius,
                searchBarTextColor = searchBarTextColor,
                searchBarTextAppearance = searchBarTextAppearance,
                searchBarHintTextColor = searchBarHintTextColor,
                searchBarHintTextAppearance = searchBarHintTextAppearance,
                backIcon = backIcon,
                backIconTint = backIconTint,
                clearIcon = clearIcon,
                clearIconTint = clearIconTint,
                searchIcon = searchIcon,
                searchIconTint = searchIconTint,
                filterChipBackgroundColor = filterChipBackgroundColor,
                filterChipSelectedBackgroundColor = filterChipSelectedBackgroundColor,
                filterChipTextColor = filterChipTextColor,
                filterChipSelectedTextColor = filterChipSelectedTextColor,
                filterChipTextAppearance = filterChipTextAppearance,
                filterChipStrokeColor = filterChipStrokeColor,
                filterChipSelectedStrokeColor = filterChipSelectedStrokeColor,
                filterChipStrokeWidth = filterChipStrokeWidth,
                filterChipCornerRadius = filterChipCornerRadius,
                sectionHeaderTextColor = defaultSectionHeaderTextColor,
                sectionHeaderTextAppearance = sectionHeaderTextAppearance,
                sectionHeaderBackgroundColor = sectionHeaderBackgroundColor,
                conversationItemBackgroundColor = conversationItemBackgroundColor,
                conversationTitleTextColor = defaultConversationTitleTextColor,
                conversationTitleTextAppearance = conversationTitleTextAppearance,
                conversationSubtitleTextColor = defaultConversationSubtitleTextColor,
                conversationSubtitleTextAppearance = conversationSubtitleTextAppearance,
                conversationTimestampTextColor = defaultConversationTimestampTextColor,
                conversationTimestampTextAppearance = conversationTimestampTextAppearance,
                conversationSeparatorColor = conversationSeparatorColor,
                messageItemBackgroundColor = messageItemBackgroundColor,
                messageTitleTextColor = defaultMessageTitleTextColor,
                messageTitleTextAppearance = messageTitleTextAppearance,
                messageSubtitleTextColor = defaultMessageSubtitleTextColor,
                messageSubtitleTextAppearance = messageSubtitleTextAppearance,
                messageTimestampTextColor = defaultMessageTimestampTextColor,
                messageTimestampTextAppearance = messageTimestampTextAppearance,
                messageSeparatorColor = messageSeparatorColor,
                messageLinkTextColor = defaultMessageLinkTextColor,
                messageLinkTextAppearance = messageLinkTextAppearance,
                messageThreadIcon = messageThreadIcon,
                messageThreadIconTint = messageThreadIconTint,
                dateSeparatorBackgroundColor = dateSeparatorBackgroundColor,
                dateSeparatorTextColor = dateSeparatorTextColor,
                dateSeparatorTextAppearance = dateSeparatorTextAppearance,
                dateSeparatorStyle = dateSeparatorStyle,
                avatarStyle = avatarStyle,
                badgeStyle = badgeStyle,
                statusIndicatorStyle = statusIndicatorStyle,
                loadingStateBackgroundColor = loadingStateBackgroundColor,
                emptyStateTextColor = emptyStateTextColor,
                emptyStateTextAppearance = emptyStateTextAppearance,
                emptyStateSubtitleTextColor = emptyStateSubtitleTextColor,
                emptyStateSubtitleTextAppearance = emptyStateSubtitleTextAppearance,
                emptyStateIcon = emptyStateIcon,
                emptyStateIconTint = emptyStateIconTint,
                initialStateTextColor = initialStateTextColor,
                initialStateTextAppearance = initialStateTextAppearance,
                initialStateSubtitleTextColor = initialStateSubtitleTextColor,
                initialStateSubtitleTextAppearance = initialStateSubtitleTextAppearance,
                initialStateIcon = initialStateIcon,
                initialStateIconTint = initialStateIconTint,
                errorStateTextColor = errorStateTextColor,
                errorStateTextAppearance = errorStateTextAppearance,
                errorStateSubtitleTextColor = errorStateSubtitleTextColor,
                errorStateSubtitleTextAppearance = errorStateSubtitleTextAppearance,
                errorStateIcon = errorStateIcon,
                errorStateIconTint = errorStateIconTint,
                seeMoreTextColor = seeMoreTextColor,
                seeMoreTextAppearance = seeMoreTextAppearance
            )
        }
    }

    companion object {
        /**
         * Creates a default CometChatSearchStyle with theme-based values.
         *
         * @param context The Android context
         * @return A new CometChatSearchStyle instance with default values
         */
        fun default(context: Context): CometChatSearchStyle {
            return Builder(context).build()
        }
    }
}
