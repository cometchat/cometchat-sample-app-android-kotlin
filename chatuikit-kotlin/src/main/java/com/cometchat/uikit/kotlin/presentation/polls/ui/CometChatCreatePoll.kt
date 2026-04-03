package com.cometchat.uikit.kotlin.presentation.polls.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatCreatePollBinding
import com.cometchat.uikit.kotlin.presentation.polls.adapter.PollOptionsAdapter
import com.cometchat.uikit.kotlin.presentation.polls.style.CometChatCreatePollStyle
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray

/**
 * CometChatCreatePoll is a view for creating polls.
 * Follows the same pattern as the reference Java implementation.
 *
 * Features:
 * - Auto-add new option when last option is filled
 * - Drag-and-drop reordering of options
 * - Submit button enabled only when valid (question + 2+ options)
 * - Consumer handles poll creation via OnSubmitClickListener (same as reference)
 */
class CometChatCreatePoll @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatCreatePollStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: CometchatCreatePollBinding
    private var style: CometChatCreatePollStyle = CometChatCreatePollStyle.default(context)
    
    private lateinit var optionsAdapter: PollOptionsAdapter
    private var itemTouchHelper: ItemTouchHelper? = null
    
    // LiveData for options filled state (same pattern as reference)
    private val isOptionsFilled = MutableLiveData(false)
    
    // Visibility flags
    private var hideToolbar: Boolean = false

    // Callbacks - same pattern as reference (consumer handles poll creation)
    private var onSubmitClickListener: OnSubmitClickListener? = null
    private var backClickListener: View.OnClickListener? = null

    /**
     * Interface for handling submit button click events.
     * Same pattern as reference Java implementation.
     */
    fun interface OnSubmitClickListener {
        /**
         * Called when the submit button is clicked.
         *
         * @param question The question text entered by the user.
         * @param options A JSONArray containing the non-empty options provided by the user.
         */
        fun onSubmitClick(question: String, options: JSONArray)
    }

    /**
     * TextWatcher for question input - updates send button state
     */
    private val questionTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setSendButtonState()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    /**
     * ItemTouchHelper for drag-and-drop reordering
     */
    private val itemTouchCallback = object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return makeMovementFlags(dragFlags, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            @Suppress("DEPRECATION")
            val fromPosition = viewHolder.adapterPosition
            @Suppress("DEPRECATION")
            val toPosition = target.adapterPosition
            
            // Prevent moving if either position has empty option
            val options = optionsAdapter.getOptionsArrayList()
            if (options.getOrNull(fromPosition)?.isEmpty() == true ||
                options.getOrNull(toPosition)?.isEmpty() == true) {
                return false
            }
            
            optionsAdapter.moveItem(fromPosition, toPosition)
            return true
        }

        override fun isLongPressDragEnabled(): Boolean = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Not used
        }
    }


    init {
        binding = CometchatCreatePollBinding.inflate(LayoutInflater.from(context), this, true)
        
        // Initialize MaterialCard views (same as reference)
        Utils.initMaterialCard(binding.pollParentCard)
        Utils.initMaterialCard(binding.questionCard)
        Utils.initMaterialCard(binding.submitBtn)
        
        // Apply XML attributes if provided
        attrs?.let { applyAttributes(it, defStyleAttr) }
        
        setupRecyclerView()
        setupQuestionInput()
        applyStyle()
        
        // Initial button state
        setSendButtonState()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        
        // Observe isOptionsFilled to update send button state
        val lifecycleOwner = findViewTreeLifecycleOwner()
        lifecycleOwner?.let { owner ->
            isOptionsFilled.observe(owner) {
                setSendButtonState()
            }
        }
    }

    private fun applyAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CometChatCreatePoll,
            defStyleAttr,
            0
        )
        try {
            style = CometChatCreatePollStyle.fromTypedArray(context, typedArray)
        } finally {
            // typedArray is recycled in fromTypedArray
        }
    }

    private fun setupRecyclerView() {
        // Create adapter with LiveData and RecyclerView reference (same as reference)
        optionsAdapter = PollOptionsAdapter(isOptionsFilled, binding.rvOptions)
        optionsAdapter.setStyle(style)
        
        binding.rvOptions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = optionsAdapter
            // Disable nested scrolling for proper scrolling in parent
            isNestedScrollingEnabled = false
        }
        
        // Setup drag and drop
        itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper?.attachToRecyclerView(binding.rvOptions)
    }


    private fun setupQuestionInput() {
        binding.etQuestion.addTextChangedListener(questionTextWatcher)
        binding.etQuestion.requestFocus()
    }

    /**
     * Updates the state of the send button based on the input in the question
     * EditText and whether the options are filled.
     * Same logic as reference implementation.
     */
    private fun setSendButtonState() {
        if (binding.etQuestion.text?.toString()?.trim().isNullOrEmpty()) {
            binding.submitBtn.isEnabled = false
            binding.submitBtn.setCardBackgroundColor(style.disabledSubmitButtonBackgroundColor)
        } else {
            if (isOptionsFilled.value == true) {
                binding.submitBtn.isEnabled = true
                binding.submitBtn.setCardBackgroundColor(style.submitButtonBackgroundColor)
            } else {
                binding.submitBtn.isEnabled = false
                binding.submitBtn.setCardBackgroundColor(style.disabledSubmitButtonBackgroundColor)
            }
        }
    }

    /**
     * Sets the visibility of the send button text.
     */
    fun setSendButtonTextVisibility(visibility: Int) {
        binding.tvSend.visibility = visibility
    }

    /**
     * Sets the visibility of the progress indicator.
     * When visible, the send button text is hidden and button is disabled.
     */
    fun setProgressVisibility(visibility: Int) {
        binding.progress.visibility = visibility
        if (visibility == View.VISIBLE) {
            binding.tvSend.visibility = View.GONE
            binding.submitBtn.isEnabled = false
        } else {
            binding.tvSend.visibility = View.VISIBLE
            binding.submitBtn.isEnabled = true
        }
    }

    /**
     * Sets the visibility of the error state.
     */
    fun setErrorStateVisibility(visibility: Int) {
        binding.tvError.visibility = visibility
    }

    /**
     * Sets the error message text.
     */
    fun setErrorMessage(message: String?) {
        binding.tvError.text = message ?: ""
    }


    private fun applyStyle() {
        // Container styling - pollParentCard background
        binding.pollParentCard.setCardBackgroundColor(style.backgroundColor)
        style.backgroundDrawable?.let { binding.pollParentCard.background = it }
        binding.pollParentCard.radius = style.cornerRadius.toFloat()
        binding.pollParentCard.strokeWidth = style.strokeWidth
        binding.pollParentCard.strokeColor = style.strokeColor

        // Title styling
        binding.tvTitle.apply {
            setTextColor(style.titleTextColor)
            if (style.titleTextAppearance != 0) {
                setTextAppearance(style.titleTextAppearance)
            }
        }

        // Separator styling
        binding.separator.setBackgroundColor(style.separatorColor)

        // Question title styling
        binding.tvQuestion.apply {
            setTextColor(style.questionTitleTextColor)
            if (style.questionTitleTextAppearance != 0) {
                setTextAppearance(style.questionTitleTextAppearance)
            }
        }

        // Question input styling
        binding.questionCard.apply {
            setCardBackgroundColor(style.backgroundColor)
            radius = style.questionCornerRadius.toFloat()
            strokeWidth = style.questionStrokeWidth
            strokeColor = style.questionStrokeColor
        }
        binding.etQuestion.apply {
            setTextColor(style.questionTextColor)
            setHintTextColor(style.questionHintColor)
            if (style.questionTextAppearance != 0) {
                setTextAppearance(style.questionTextAppearance)
            }
        }

        // Options title styling
        binding.tvOptions.apply {
            setTextColor(style.optionTitleTextColor)
            if (style.optionTitleTextAppearance != 0) {
                setTextAppearance(style.optionTitleTextAppearance)
            }
        }


        // Back icon styling
        style.backIcon?.let { drawable ->
            val wrappedDrawable = DrawableCompat.wrap(drawable.mutate())
            DrawableCompat.setTint(wrappedDrawable, style.backIconTint)
            binding.imgBack.setImageDrawable(wrappedDrawable)
        }

        // Submit button styling
        binding.submitBtn.apply {
            setCardBackgroundColor(style.disabledSubmitButtonBackgroundColor)
            radius = style.submitButtonCornerRadius.toFloat()
            strokeWidth = style.submitButtonStrokeWidth
            strokeColor = style.submitButtonStrokeColor
        }
        binding.tvSend.apply {
            setTextColor(style.submitButtonTextColor)
            if (style.submitButtonTextAppearance != 0) {
                setTextAppearance(style.submitButtonTextAppearance)
            }
        }

        // Progress indicator styling
        binding.progress.indeterminateTintList = android.content.res.ColorStateList.valueOf(
            style.progressIndicatorColor
        )

        // Error text styling
        binding.tvError.apply {
            setTextColor(style.errorTextColor)
            if (style.errorTextAppearance != 0) {
                setTextAppearance(style.errorTextAppearance)
            }
        }

        // Update adapter style
        if (::optionsAdapter.isInitialized) {
            optionsAdapter.setStyle(style)
        }

        // Apply toolbar visibility
        updateToolbarVisibility()
    }

    private fun updateToolbarVisibility() {
        val toolbarVisibility = if (hideToolbar) View.GONE else View.VISIBLE
        binding.imgBack.visibility = toolbarVisibility
        binding.tvTitle.visibility = toolbarVisibility
        binding.separator.visibility = toolbarVisibility
    }


    // ==================== Public API ====================

    /**
     * Gets the current OnSubmitClickListener.
     */
    fun getOnSubmitClickListener(): OnSubmitClickListener? = onSubmitClickListener

    /**
     * Sets a listener for submit button click events.
     * Same pattern as reference Java implementation - consumer handles poll creation.
     *
     * @param listener The listener to be notified of submit button clicks.
     */
    fun setOnSubmitClickListener(listener: OnSubmitClickListener?) {
        if (listener != null) {
            this.onSubmitClickListener = listener
            binding.submitBtn.setOnClickListener {
                val question = binding.etQuestion.text?.toString() ?: ""
                val options = optionsAdapter.getOptionsArrayList()
                val processedOptions = JSONArray()
                
                options.forEachIndexed { index, option ->
                    if (option.isNotEmpty()) {
                        try {
                            processedOptions.put(index, option.trim())
                        } catch (e: Exception) {
                            // Log error if needed
                        }
                    }
                }
                
                listener.onSubmitClick(question.trim(), processedOptions)
            }
        }
    }

    /**
     * Gets the current back click listener.
     */
    fun getBackClickListener(): View.OnClickListener? = backClickListener

    /**
     * Sets a listener for back button click events.
     *
     * @param listener The listener to be notified of back button clicks.
     */
    fun setBackClickListener(listener: View.OnClickListener?) {
        if (listener != null) {
            this.backClickListener = listener
            binding.imgBack.setOnClickListener(listener)
        }
    }

    /**
     * Sets the style for the component.
     */
    fun setStyle(style: CometChatCreatePollStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     *
     * @param styleRes The style resource ID to apply
     */
    fun setStyle(@androidx.annotation.StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.obtainStyledAttributes(
                styleRes,
                R.styleable.CometChatCreatePoll
            )
            style = CometChatCreatePollStyle.fromTypedArray(context, typedArray)
            applyStyle()
        }
    }

    /**
     * Sets whether to hide the toolbar.
     */
    fun setHideToolbar(hide: Boolean) {
        this.hideToolbar = hide
        updateToolbarVisibility()
    }

    /**
     * Sets the title text.
     */
    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    /**
     * Resets the form to initial state.
     */
    fun resetForm() {
        binding.etQuestion.setText("")
        optionsAdapter.reset()
        setErrorStateVisibility(View.GONE)
        setSendButtonState()
    }

    /**
     * Gets the question text.
     */
    fun getQuestion(): String = binding.etQuestion.text?.toString() ?: ""

    /**
     * Gets the options list.
     */
    fun getOptions(): List<String> = optionsAdapter.getOptionsArrayList()
}
