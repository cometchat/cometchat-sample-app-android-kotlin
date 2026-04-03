package com.cometchat.uikit.kotlin.presentation.messagelist.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.kotlin.presentation.reactionlist.style.CometChatReactionListStyle
import com.cometchat.uikit.kotlin.presentation.shared.aiconversationstarter.CometChatAIConversationStarterStyle
import com.cometchat.uikit.kotlin.presentation.shared.aiconversationsummary.CometChatAIConversationSummaryStyle
import com.cometchat.uikit.kotlin.presentation.shared.aismartreplies.CometChatAISmartRepliesStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialogStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble.CometChatActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.callactionbubble.CometChatCallActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenuStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for the [CometChatMessageList] component (XML Views).
 *
 * This data class defines all styling properties for the message list component,
 * allowing customization of:
 * - Container appearance (background, stroke, corner radius)
 * - Error state text styling
 * - Empty chat greeting text styling
 * - AI Assistant suggested message styling (for future AI features)
 * - Nested component style objects
 *
 * ## Usage
 *
 * ### Programmatic
 * ```kotlin
 * val style = CometChatMessageListStyle.default(context)
 * messageList.setStyle(style)
 * ```
 *
 * ### XML Attributes
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList
 *     app:cometchatMessageListBackgroundColor="@color/white"
 *     app:cometchatMessageListCornerRadius="8dp"
 *     app:cometchatMessageListIncomingMessageBubbleStyle="@style/IncomingBubble" />
 * ```
 *
 * ## Factory Functions
 *
 * - [default]: Creates a style with theme defaults
 * - [fromTypedArray]: Creates a style from XML TypedArray (handles recycling)
 *
 * ## Nested Style Objects
 *
 * The style includes typed style objects for nested components. When null, components
 * use their default factory methods:
 * - [incomingMessageBubbleStyle]: Style for incoming message bubbles
 * - [outgoingMessageBubbleStyle]: Style for outgoing message bubbles
 * - [actionBubbleStyle]: Style for action/system message bubbles
 * - [callActionBubbleStyle]: Style for call action bubbles
 * - [dateSeparatorStyle]: Style for date separator views
 * - [deleteDialogStyle]: Style for delete confirmation dialog
 * - [messageInformationStyle]: Style for message information view
 * - [messageOptionSheetStyle]: Style for message options bottom sheet
 * - [reactionListStyle]: Style for reaction list view
 * - [aiSmartRepliesStyle]: Style for AI smart replies
 * - [aiConversationStarterStyle]: Style for AI conversation starter
 * - [aiConversationSummaryStyle]: Style for AI conversation summary
 *
 * @property backgroundColor The background color of the message list container.
 * @property strokeColor The color of the container border stroke.
 * @property strokeWidth The width of the container border stroke in pixels.
 * @property cornerRadius The corner radius of the container in pixels.
 * @property backgroundDrawable Optional drawable for the container background.
 * @property errorStateTitleTextColor The text color for error state title.
 * @property errorStateTitleTextAppearance The text appearance resource for error state title.
 * @property errorStateSubtitleTextColor The text color for error state subtitle.
 * @property errorStateSubtitleTextAppearance The text appearance resource for error state subtitle.
 * @property emptyChatGreetingTitleTextColor The text color for empty chat greeting title.
 * @property emptyChatGreetingTitleTextAppearance The text appearance resource for empty chat greeting title.
 * @property emptyChatGreetingSubtitleTextColor The text color for empty chat greeting subtitle.
 * @property emptyChatGreetingSubtitleTextAppearance The text appearance resource for empty chat greeting subtitle.
 * @property aiAssistantSuggestedMessageTextColor The text color for AI suggested messages.
 * @property aiAssistantSuggestedMessageTextAppearance The text appearance for AI suggested messages.
 * @property aiAssistantSuggestedMessageCornerRadius The corner radius for AI suggested message container.
 * @property aiAssistantSuggestedMessageStrokeWidth The stroke width for AI suggested message container.
 * @property aiAssistantSuggestedMessageStrokeColor The stroke color for AI suggested message container.
 * @property aiAssistantSuggestedMessageBackgroundColor The background color for AI suggested message container.
 * @property aiAssistantSuggestedMessageEndIcon The end icon drawable for AI suggested messages.
 * @property aiAssistantSuggestedMessageEndIconTint The tint color for AI suggested message end icon.
 * @property incomingMessageBubbleStyle Style object for incoming message bubbles, or null to use default.
 * @property outgoingMessageBubbleStyle Style object for outgoing message bubbles, or null to use default.
 * @property actionBubbleStyle Style object for action/system message bubbles, or null to use default.
 * @property callActionBubbleStyle Style object for call action bubbles, or null to use default.
 * @property dateSeparatorStyle Style object for date separator views, or null to use default.
 * @property deleteDialogStyle Style object for delete confirmation dialog, or null to use default.
 * @property messageInformationStyle Style object for message information view, or null to use default.
 * @property messageOptionSheetStyle Style object for message options bottom sheet, or null to use default.
 * @property reactionListStyle Style object for reaction list view, or null to use default.
 * @property aiSmartRepliesStyle Style object for AI smart replies, or null to use default.
 * @property aiConversationStarterStyle Style object for AI conversation starter, or null to use default.
 * @property aiConversationSummaryStyle Style object for AI conversation summary, or null to use default.
 *
 * @see CometChatMessageList
 * @see CometChatTheme
 */
data class CometChatMessageListStyle(
    // Container
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    val backgroundDrawable: Drawable? = null,

    // Error state
    @ColorInt val errorStateTitleTextColor: Int = 0,
    @StyleRes val errorStateTitleTextAppearance: Int = 0,
    @ColorInt val errorStateSubtitleTextColor: Int = 0,
    @StyleRes val errorStateSubtitleTextAppearance: Int = 0,

    // Empty chat greeting
    @ColorInt val emptyChatGreetingTitleTextColor: Int = 0,
    @StyleRes val emptyChatGreetingTitleTextAppearance: Int = 0,
    @ColorInt val emptyChatGreetingSubtitleTextColor: Int = 0,
    @StyleRes val emptyChatGreetingSubtitleTextAppearance: Int = 0,

    // AI Assistant suggested message (for future AI features)
    @ColorInt val aiAssistantSuggestedMessageTextColor: Int = 0,
    @StyleRes val aiAssistantSuggestedMessageTextAppearance: Int = 0,
    @Dimension val aiAssistantSuggestedMessageCornerRadius: Int = 0,
    @Dimension val aiAssistantSuggestedMessageStrokeWidth: Int = 0,
    @ColorInt val aiAssistantSuggestedMessageStrokeColor: Int = 0,
    @ColorInt val aiAssistantSuggestedMessageBackgroundColor: Int = 0,
    val aiAssistantSuggestedMessageEndIcon: Drawable? = null,
    @ColorInt val aiAssistantSuggestedMessageEndIconTint: Int = 0,

    // Nested component style objects
    val incomingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
    val outgoingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
    val actionBubbleStyle: CometChatActionBubbleStyle? = null,
    val callActionBubbleStyle: CometChatCallActionBubbleStyle? = null,
    val dateSeparatorStyle: CometChatDateStyle? = null,
    val deleteDialogStyle: CometChatConfirmDialogStyle? = null,
    val messageInformationStyle: CometChatMessageInformationStyle? = null,
    val messageOptionSheetStyle: CometChatPopupMenuStyle? = null,
    val reactionListStyle: CometChatReactionListStyle? = null,
    val aiSmartRepliesStyle: CometChatAISmartRepliesStyle? = null,
    val aiConversationStarterStyle: CometChatAIConversationStarterStyle? = null,
    val aiConversationSummaryStyle: CometChatAIConversationSummaryStyle? = null
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatMessageListStyle.
         *
         * This method looks up the `cometchatMessageListStyle` attribute in the current theme
         * and extracts all style properties from it. If the attribute is not defined,
         * fallback defaults from [CometChatTheme] are used.
         *
         * @param context The Android context to access theme resources.
         * @return A [CometChatMessageListStyle] with values from theme or fallback defaults.
         */
        fun default(context: Context): CometChatMessageListStyle {
            return extractFromThemeStyle(context, R.attr.cometchatMessageListStyle)
        }

        /**
         * Extracts the message list style from a theme style attribute.
         *
         * @param context The Android context to access theme resources.
         * @param themeStyleAttr The attribute reference to the message list style in theme.
         * @return A [CometChatMessageListStyle] with values from the theme style.
         */
        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatMessageListStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()

            return extractFromStyleResource(context, styleResId)
        }

        /**
         * Extracts style values from a specific style resource ID.
         *
         * If [styleResId] is 0, returns a style with [CometChatTheme] defaults.
         *
         * @param context The Android context to access theme resources.
         * @param styleResId The style resource ID to extract from (0 for defaults only).
         * @return A [CometChatMessageListStyle] with values from the style resource or defaults.
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatMessageListStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatMessageList)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a style by extracting values from an XML [TypedArray].
         *
         * This method handles [TypedArray] recycling internally using try-finally.
         * All attributes are extracted from the corresponding `attr_cometchat_message_list.xml` file.
         *
         * Use this method when creating a style from XML attributes in a custom view constructor.
         *
         * @param context The Android context for accessing theme resources.
         * @param typedArray The [TypedArray] containing XML attribute values (will be recycled).
         * @return A [CometChatMessageListStyle] with values from XML or theme defaults.
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatMessageListStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Core extraction method that reads style values from a [TypedArray].
         *
         * **Note:** This method does NOT handle recycling - the caller is responsible
         * for recycling the [TypedArray].
         *
         * @param context The Android context for accessing theme resources.
         * @param typedArray The [TypedArray] to extract from, or `null` for defaults only.
         * @return A [CometChatMessageListStyle] with extracted values or theme defaults.
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatMessageListStyle {
            // Extract nested style resource IDs
            val incomingMessageBubbleStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListIncomingMessageBubbleStyle, 0
            ) ?: 0
            val outgoingMessageBubbleStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListOutgoingMessageBubbleStyle, 0
            ) ?: 0
            val actionBubbleStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListActionBubbleStyle, 0
            ) ?: 0
            val callActionBubbleStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListCallActionBubbleStyle, 0
            ) ?: 0
            val dateSeparatorStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListDateSeparatorStyle, 0
            ) ?: 0
            val deleteDialogStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListDeleteDialogStyle, 0
            ) ?: 0
            val messageInformationStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListMessageInformationStyle, 0
            ) ?: 0
            val messageOptionSheetStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListMessageOptionSheetStyle, 0
            ) ?: 0
            val reactionListStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListReactionListStyle, 0
            ) ?: 0
            val aiSmartRepliesStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListAISmartRepliesStyle, 0
            ) ?: 0
            val aiConversationStarterStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListAIConversationStarterStyle, 0
            ) ?: 0
            val aiConversationSummaryStyleResId = typedArray?.getResourceId(
                R.styleable.CometChatMessageList_cometchatMessageListAIConversationSummaryStyle, 0
            ) ?: 0

            return CometChatMessageListStyle(
                // Container
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageList_cometchatMessageListBackgroundColor,
                    CometChatTheme.getBackgroundColor3(context)
                ) ?: CometChatTheme.getBackgroundColor3(context),
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessageList_cometchatMessageListStrokeColor, 0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageList_cometchatMessageListStrokeWidth, 0
                ) ?: 0,
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageList_cometchatMessageListCornerRadius, 0
                ) ?: 0,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatMessageList_cometchatMessageListBackgroundDrawable
                ),

                // Error state
                errorStateTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageList_cometchatMessageListErrorStateTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                errorStateTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageList_cometchatMessageListErrorStateTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                errorStateSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageList_cometchatMessageListErrorStateSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                errorStateSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageList_cometchatMessageListErrorStateSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // Empty chat greeting
                emptyChatGreetingTitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageList_cometchatMessageListEmptyChatGreetingTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                emptyChatGreetingTitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageList_cometchatMessageListEmptyChatGreetingTitleTextAppearance,
                    CometChatTheme.getTextAppearanceHeading3Bold(context)
                ) ?: CometChatTheme.getTextAppearanceHeading3Bold(context),
                emptyChatGreetingSubtitleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageList_cometchatMessageListEmptyChatGreetingSubtitleTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                emptyChatGreetingSubtitleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageList_cometchatMessageListEmptyChatGreetingSubtitleTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),

                // AI Assistant suggested message
                aiAssistantSuggestedMessageTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),
                aiAssistantSuggestedMessageTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(context)
                ) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                aiAssistantSuggestedMessageCornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageCornerRadius, 0
                ) ?: 0,
                aiAssistantSuggestedMessageStrokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageStrokeWidth, 0
                ) ?: 0,
                aiAssistantSuggestedMessageStrokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageStrokeColor,
                    CometChatTheme.getStrokeColorDefault(context)
                ) ?: CometChatTheme.getStrokeColorDefault(context),
                aiAssistantSuggestedMessageBackgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageBackgroundColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),
                aiAssistantSuggestedMessageEndIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageEndIcon
                ),
                aiAssistantSuggestedMessageEndIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageList_cometchatMessageListAIAssistantSuggestedMessageEndIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Nested component style objects - parse XML resource IDs into style objects
                // Use fromTypedArrayIncoming/Outgoing to ensure correct default background colors
                incomingMessageBubbleStyle = if (incomingMessageBubbleStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(incomingMessageBubbleStyleResId, R.styleable.CometChatMessageBubble)
                    CometChatMessageBubbleStyle.fromTypedArrayIncoming(context, ta)
                } else null,

                outgoingMessageBubbleStyle = if (outgoingMessageBubbleStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(outgoingMessageBubbleStyleResId, R.styleable.CometChatMessageBubble)
                    CometChatMessageBubbleStyle.fromTypedArrayOutgoing(context, ta)
                } else null,

                actionBubbleStyle = if (actionBubbleStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(actionBubbleStyleResId, R.styleable.CometChatActionBubble)
                    CometChatActionBubbleStyle.fromTypedArray(context, ta)
                } else null,

                callActionBubbleStyle = if (callActionBubbleStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(callActionBubbleStyleResId, R.styleable.CometChatCallActionBubble)
                    CometChatCallActionBubbleStyle.fromTypedArray(context, ta)
                } else null,

                dateSeparatorStyle = if (dateSeparatorStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(dateSeparatorStyleResId, R.styleable.CometChatDate)
                    CometChatDateStyle.fromTypedArray(context, ta)
                } else null,

                deleteDialogStyle = if (deleteDialogStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(deleteDialogStyleResId, R.styleable.CometChatConfirmDialog)
                    CometChatConfirmDialogStyle.fromTypedArray(context, ta)
                } else null,

                messageInformationStyle = if (messageInformationStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(messageInformationStyleResId, R.styleable.CometChatMessageInformation)
                    CometChatMessageInformationStyle.fromTypedArray(context, ta)
                } else null,

                messageOptionSheetStyle = if (messageOptionSheetStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(messageOptionSheetStyleResId, R.styleable.CometChatPopupMenu)
                    CometChatPopupMenuStyle.fromTypedArray(context, ta)
                } else null,

                reactionListStyle = if (reactionListStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(reactionListStyleResId, R.styleable.CometChatReactionList)
                    CometChatReactionListStyle.fromTypedArray(context, ta)
                } else null,

                aiSmartRepliesStyle = if (aiSmartRepliesStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(aiSmartRepliesStyleResId, R.styleable.CometChatAISmartReplies)
                    CometChatAISmartRepliesStyle.fromTypedArray(context, ta)
                } else null,

                aiConversationStarterStyle = if (aiConversationStarterStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(aiConversationStarterStyleResId, R.styleable.CometChatAIConversationStarter)
                    CometChatAIConversationStarterStyle.fromTypedArray(context, ta)
                } else null,

                aiConversationSummaryStyle = if (aiConversationSummaryStyleResId != 0) {
                    val ta = context.obtainStyledAttributes(aiConversationSummaryStyleResId, R.styleable.CometChatAIConversationSummary)
                    CometChatAIConversationSummaryStyle.fromTypedArray(context, ta)
                } else null
            )
        }
    }
}
