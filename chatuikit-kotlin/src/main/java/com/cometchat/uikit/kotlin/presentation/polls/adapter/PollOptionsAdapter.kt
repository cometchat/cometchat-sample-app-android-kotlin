package com.cometchat.uikit.kotlin.presentation.polls.adapter

import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.databinding.CometchatPollsOptionBinding
import com.cometchat.uikit.kotlin.presentation.polls.style.CometChatCreatePollStyle
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils

/**
 * Adapter for displaying poll options in a RecyclerView.
 * Manages the options list internally (same pattern as reference implementation).
 * Supports text input for each option, drag handle visibility, and dynamic add/remove.
 *
 * @param isOptionsFilled LiveData to notify when at least 2 options are filled
 * @param recyclerView The RecyclerView this adapter is attached to (for scrolling)
 */
class PollOptionsAdapter(
    private val isOptionsFilled: MutableLiveData<Boolean>,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<PollOptionsAdapter.OptionViewHolder>() {

    companion object {
        private const val MAX_OPTIONS = 12
    }

    // Internal list - managed directly by adapter (same as reference)
    private val optionsArrayList = mutableListOf("", "")
    
    // Style properties
    private var style: CometChatCreatePollStyle? = null

    /**
     * Returns the current options list.
     */
    fun getOptionsArrayList(): List<String> = optionsArrayList.toList()

    /**
     * Sets the style for the adapter items.
     */
    fun setStyle(style: CometChatCreatePollStyle) {
        this.style = style
        notifyItemRangeChanged(0, itemCount)
    }

    /**
     * Resets the options to initial state (2 empty options).
     */
    fun reset() {
        optionsArrayList.clear()
        optionsArrayList.add("")
        optionsArrayList.add("")
        notifyDataSetChanged()
        updateSendButtonState()
    }

    /**
     * Moves an item from one position to another.
     * Used for drag-and-drop reordering.
     */
    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < 0 || fromPosition >= optionsArrayList.size) return
        if (toPosition < 0 || toPosition >= optionsArrayList.size) return
        
        val item = optionsArrayList.removeAt(fromPosition)
        optionsArrayList.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun add(position: Int) {
        optionsArrayList.add("")
        notifyItemInserted(position + 1)
        recyclerView.scrollToPosition(position + 1)
    }

    private fun remove(position: Int) {
        if (position >= 0 && position < optionsArrayList.size) {
            optionsArrayList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, optionsArrayList.size - position)
        }
    }

    private fun updateSendButtonState() {
        val firstFilled = optionsArrayList.getOrNull(0)?.trim()?.isNotEmpty() == true
        val secondFilled = optionsArrayList.getOrNull(1)?.trim()?.isNotEmpty() == true
        isOptionsFilled.value = firstFilled && secondFilled
    }

    private fun checkIfAnyPositionIsEmpty(currentPosition: Int) {
        if (currentPosition < 0 || currentPosition >= optionsArrayList.size) return
        
        if (optionsArrayList[currentPosition].isEmpty()) {
            // Don't remove if only 2 options remain
            // Don't remove the last option (placeholder for new input)
            // Don't remove if position is invalid
            if (optionsArrayList.size != 2 && 
                currentPosition != optionsArrayList.size - 1 && 
                currentPosition != -1) {
                recyclerView.post { remove(currentPosition) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = CometchatPollsOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        // Initialize MaterialCard with transparent background (same as reference)
        Utils.initMaterialCard(binding.optionCard)
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val option = optionsArrayList[position]
        holder.bindView(option)
    }

    override fun getItemCount(): Int = optionsArrayList.size

    inner class OptionViewHolder(
        val binding: CometchatPollsOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isAddOperationPending = false
        private var textWatcher: TextWatcher? = null

        init {
            // Setup TextWatcher ONCE in constructor (not in bindView)
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    @Suppress("DEPRECATION")
                    val position = adapterPosition
                    if (position == RecyclerView.NO_POSITION || position >= optionsArrayList.size) return

                    // Update the list directly (no ViewModel, no rebind)
                    optionsArrayList[position] = s?.toString() ?: ""
                    updateSendButtonState()

                    if ((s?.length ?: 0) > 0) {
                        binding.drag.visibility = View.VISIBLE
                        
                        if (isAddOperationPending) {
                            // Add 3rd option when both first two are filled
                            if (position == 0 || position == 1) {
                                val first = optionsArrayList.getOrNull(0)?.trim() ?: ""
                                val second = optionsArrayList.getOrNull(1)?.trim() ?: ""
                                if (first.isNotEmpty() && second.isNotEmpty()) {
                                    if (optionsArrayList.size == 2) {
                                        isAddOperationPending = false
                                        recyclerView.post { add(optionsArrayList.size) }
                                    }
                                }
                            } else if (position == optionsArrayList.size - 1 && optionsArrayList.size < MAX_OPTIONS) {
                                // Add new option when last option is filled
                                isAddOperationPending = false
                                recyclerView.post { add(position) }
                            }
                        }
                    } else {
                        binding.drag.visibility = View.GONE
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            }
            binding.etOption.addTextChangedListener(textWatcher)

            // Setup focus listener ONCE in constructor
            binding.etOption.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    @Suppress("DEPRECATION")
                    checkIfAnyPositionIsEmpty(adapterPosition)
                }
            }
        }

        fun bindView(option: String) {
            // Apply style
            style?.let { applyStyle(it) }

            // Set text without triggering TextWatcher issues
            // Remove and re-add to prevent callback during setText
            binding.etOption.removeTextChangedListener(textWatcher)
            binding.etOption.setText(option)
            binding.etOption.addTextChangedListener(textWatcher)

            // Show/hide drag handle based on content
            binding.drag.visibility = if (option.isEmpty()) View.GONE else View.VISIBLE

            // Reset flag for add operation
            isAddOperationPending = true
        }

        private fun applyStyle(style: CometChatCreatePollStyle) {
            // Option card styling
            binding.optionCard.apply {
                setCardBackgroundColor(style.optionBackgroundColor)
                radius = style.optionCornerRadius.toFloat()
                strokeWidth = style.optionStrokeWidth
                strokeColor = style.optionStrokeColor
            }

            // Option text styling
            binding.etOption.apply {
                setTextColor(style.optionTextColor)
                setHintTextColor(style.optionHintColor)
                if (style.optionTextAppearance != 0) {
                    setTextAppearance(style.optionTextAppearance)
                }
            }

            // Drag icon styling
            style.dragIcon?.let { drawable ->
                binding.drag.setImageDrawable(drawable)
                binding.drag.imageTintList = ColorStateList.valueOf(style.dragIconTint)
            }
        }

        /**
         * Returns the drag handle view for ItemTouchHelper.
         */
        fun getDragHandle(): View = binding.drag
    }
}
