package com.cometchat.uikit.kotlin.presentation.messageinformation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A BottomSheetDialogFragment that displays the CometChatMessageInformation component.
 *
 * This dialog provides a modal bottom sheet presentation for viewing message receipt
 * information (delivered/read timestamps for user chats, receipt list for group chats).
 *
 * The message bubble is rendered internally using the provided bubble factories.
 * If no factories are provided, the component uses the default InternalContentRenderer.
 *
 * Usage:
 * ```kotlin
 * val bottomSheet = CometChatMessageInformationBottomSheet.newInstance(message)
 * bottomSheet.setBubbleFactories(bubbleFactories) // Optional: for custom bubble rendering
 * bottomSheet.setOnErrorListener { exception ->
 *     // Handle error
 * }
 * bottomSheet.show(supportFragmentManager, "message_info")
 * ```
 */
class CometChatMessageInformationBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "MessageInfoBottomSheet"

        /**
         * Creates a new instance of the bottom sheet with the specified message.
         *
         * @param message The message to display information for
         * @return A new CometChatMessageInformationBottomSheet instance
         */
        fun newInstance(message: BaseMessage): CometChatMessageInformationBottomSheet {
            return CometChatMessageInformationBottomSheet().apply {
                this.message = message
            }
        }
    }

    private var messageInformationView: CometChatMessageInformation? = null
    private var message: BaseMessage? = null
    private var onError: ((CometChatException) -> Unit)? = null
    private var bubbleFactories: Map<String, BubbleFactory> = emptyMap()
    private var textFormatters: List<com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        messageInformationView = CometChatMessageInformation(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return messageInformationView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure the bottom sheet behavior and styling
        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.isFitToContents = false
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                
                // Apply rounded corners to the bottom sheet container
                val cornerRadius = requireContext().resources.getDimensionPixelSize(
                    com.cometchat.uikit.kotlin.R.dimen.cometchat_radius_5
                ).toFloat()
                
                val shapeModel = com.google.android.material.shape.ShapeAppearanceModel.builder()
                    .setTopLeftCorner(com.google.android.material.shape.CornerFamily.ROUNDED, cornerRadius)
                    .setTopRightCorner(com.google.android.material.shape.CornerFamily.ROUNDED, cornerRadius)
                    .setBottomLeftCorner(com.google.android.material.shape.CornerFamily.ROUNDED, 0f)
                    .setBottomRightCorner(com.google.android.material.shape.CornerFamily.ROUNDED, 0f)
                    .build()
                
                val shapeDrawable = com.google.android.material.shape.MaterialShapeDrawable(shapeModel).apply {
                    fillColor = android.content.res.ColorStateList.valueOf(
                        com.cometchat.uikit.kotlin.theme.CometChatTheme.getBackgroundColor1(requireContext())
                    )
                }
                it.background = shapeDrawable
            }
        }

        // Set up the message information view
        messageInformationView?.apply {
            // Set bubble factories first so they're available when setMessage is called
            setBubbleFactories(bubbleFactories)
            // Set text formatters for mentions/markdown rendering
            setTextFormatters(textFormatters)
            message?.let { setMessage(it) }
            onError?.let { setOnError(it) }
        }
    }

    /**
     * Sets the error callback listener.
     *
     * @param listener Callback invoked when an error occurs
     */
    fun setOnErrorListener(listener: (CometChatException) -> Unit) {
        this.onError = listener
        messageInformationView?.setOnError(listener)
    }

    /**
     * Sets the bubble factories for rendering the message bubble internally.
     * 
     * The message bubble is rendered using CometChatMessageBubble with these factories.
     * If no factories are provided, the default InternalContentRenderer handles rendering.
     *
     * @param factories Map of factory key to BubbleFactory
     */
    fun setBubbleFactories(factories: Map<String, BubbleFactory>) {
        this.bubbleFactories = factories
        messageInformationView?.setBubbleFactories(factories)
    }

    /**
     * Sets the text formatters for rendering mentions and markdown in the message bubble.
     *
     * Text formatters are used to customize how message text is rendered,
     * including mentions, links, markdown, and other text transformations.
     *
     * @param formatters The list of text formatters to use for text rendering
     */
    fun setTextFormatters(formatters: List<com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter>) {
        this.textFormatters = formatters
        messageInformationView?.setTextFormatters(formatters)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messageInformationView = null
    }
}
