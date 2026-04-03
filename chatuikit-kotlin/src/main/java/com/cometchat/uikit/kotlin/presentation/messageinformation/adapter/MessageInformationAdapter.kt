package com.cometchat.uikit.kotlin.presentation.messageinformation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.uikit.kotlin.databinding.CometchatMessageInformationListItemBinding
import com.cometchat.uikit.kotlin.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying message receipts in a RecyclerView.
 * Used for GROUP conversations to show delivery/read status for each member.
 *
 * Per design doc: Receipt List Item Layout section.
 */
class MessageInformationAdapter(
    private val context: Context
) : ListAdapter<MessageReceipt, MessageInformationAdapter.ReceiptViewHolder>(ReceiptDiffCallback()) {

    // Style properties
    @StyleRes private var itemNameTextAppearance: Int = 0
    @ColorInt private var itemNameTextColor: Int = 0
    @StyleRes private var itemReadTextAppearance: Int = 0
    @ColorInt private var itemReadTextColor: Int = 0
    @StyleRes private var itemReadDateTextAppearance: Int = 0
    @ColorInt private var itemReadDateTextColor: Int = 0
    @StyleRes private var itemDeliveredTextAppearance: Int = 0
    @ColorInt private var itemDeliveredTextColor: Int = 0
    @StyleRes private var itemDeliveredDateTextAppearance: Int = 0
    @ColorInt private var itemDeliveredDateTextColor: Int = 0
    private var avatarStyle: CometChatAvatarStyle? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val binding = CometchatMessageInformationListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReceiptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Applies style from a CometChatMessageInformationStyle object.
     */
    fun setStyle(style: CometChatMessageInformationStyle) {
        itemNameTextAppearance = style.itemNameTextAppearance
        itemNameTextColor = style.itemNameTextColor
        itemReadTextAppearance = style.itemReadTextAppearance
        itemReadTextColor = style.itemReadTextColor
        itemReadDateTextAppearance = style.itemReadDateTextAppearance
        itemReadDateTextColor = style.itemReadDateTextColor
        itemDeliveredTextAppearance = style.itemDeliveredTextAppearance
        itemDeliveredTextColor = style.itemDeliveredTextColor
        itemDeliveredDateTextAppearance = style.itemDeliveredDateTextAppearance
        itemDeliveredDateTextColor = style.itemDeliveredDateTextColor
        avatarStyle = style.itemAvatarStyle
        notifyDataSetChanged()
    }

    // Individual style setters
    fun setItemNameTextAppearance(@StyleRes appearance: Int) {
        itemNameTextAppearance = appearance
        notifyDataSetChanged()
    }

    fun setItemNameTextColor(@ColorInt color: Int) {
        itemNameTextColor = color
        notifyDataSetChanged()
    }

    fun setItemReadTextAppearance(@StyleRes appearance: Int) {
        itemReadTextAppearance = appearance
        notifyDataSetChanged()
    }

    fun setItemReadTextColor(@ColorInt color: Int) {
        itemReadTextColor = color
        notifyDataSetChanged()
    }

    fun setItemReadDateTextAppearance(@StyleRes appearance: Int) {
        itemReadDateTextAppearance = appearance
        notifyDataSetChanged()
    }

    fun setItemReadDateTextColor(@ColorInt color: Int) {
        itemReadDateTextColor = color
        notifyDataSetChanged()
    }

    fun setItemDeliveredTextAppearance(@StyleRes appearance: Int) {
        itemDeliveredTextAppearance = appearance
        notifyDataSetChanged()
    }

    fun setItemDeliveredTextColor(@ColorInt color: Int) {
        itemDeliveredTextColor = color
        notifyDataSetChanged()
    }

    fun setItemDeliveredDateTextAppearance(@StyleRes appearance: Int) {
        itemDeliveredDateTextAppearance = appearance
        notifyDataSetChanged()
    }

    fun setItemDeliveredDateTextColor(@ColorInt color: Int) {
        itemDeliveredDateTextColor = color
        notifyDataSetChanged()
    }

    fun setAvatarStyle(style: CometChatAvatarStyle?) {
        avatarStyle = style
        notifyDataSetChanged()
    }

    inner class ReceiptViewHolder(
        private val binding: CometchatMessageInformationListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(receipt: MessageReceipt) {
            // Set avatar
            val sender = receipt.sender
            if (sender != null) {
                binding.ivAvatar.setAvatar(sender.name ?: "", sender.avatar)
                binding.tvName.text = sender.name ?: ""
            } else {
                binding.ivAvatar.setAvatar("", null as String?)
                binding.tvName.text = ""
            }

            // Apply avatar style
            avatarStyle?.let { binding.ivAvatar.setStyle(it) }

            // Apply name styling
            if (itemNameTextAppearance != 0) {
                binding.tvName.setTextAppearance(itemNameTextAppearance)
            }
            if (itemNameTextColor != 0) {
                binding.tvName.setTextColor(itemNameTextColor)
            }

            // Read section - visible if readAt > 0
            if (receipt.readAt > 0) {
                binding.readLayout.visibility = View.VISIBLE
                binding.tvReadTimestamp.text = formatDateTime(receipt.readAt * 1000)

                // Apply read styling
                if (itemReadTextAppearance != 0) {
                    binding.tvReadLabel.setTextAppearance(itemReadTextAppearance)
                }
                if (itemReadTextColor != 0) {
                    binding.tvReadLabel.setTextColor(itemReadTextColor)
                }
                if (itemReadDateTextAppearance != 0) {
                    binding.tvReadTimestamp.setTextAppearance(itemReadDateTextAppearance)
                }
                if (itemReadDateTextColor != 0) {
                    binding.tvReadTimestamp.setTextColor(itemReadDateTextColor)
                }
            } else {
                binding.readLayout.visibility = View.GONE
            }

            // Delivered section - always visible if deliveredAt > 0
            if (receipt.deliveredAt > 0) {
                binding.deliveredLayout.visibility = View.VISIBLE
                binding.tvDeliveredTimestamp.text = formatDateTime(receipt.deliveredAt * 1000)

                // Apply delivered styling
                if (itemDeliveredTextAppearance != 0) {
                    binding.tvDeliveredLabel.setTextAppearance(itemDeliveredTextAppearance)
                }
                if (itemDeliveredTextColor != 0) {
                    binding.tvDeliveredLabel.setTextColor(itemDeliveredTextColor)
                }
                if (itemDeliveredDateTextAppearance != 0) {
                    binding.tvDeliveredTimestamp.setTextAppearance(itemDeliveredDateTextAppearance)
                }
                if (itemDeliveredDateTextColor != 0) {
                    binding.tvDeliveredTimestamp.setTextColor(itemDeliveredDateTextColor)
                }
            } else {
                binding.deliveredLayout.visibility = View.GONE
            }
        }

        /**
         * Formats timestamp to "dd/M/yyyy, h:mm a" format.
         * Per design doc: Date/Time Formatting section.
         */
        private fun formatDateTime(milliseconds: Long): String {
            val sdf = SimpleDateFormat("dd/M/yyyy, h:mm a", Locale.getDefault())
            return sdf.format(Date(milliseconds))
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    class ReceiptDiffCallback : DiffUtil.ItemCallback<MessageReceipt>() {
        override fun areItemsTheSame(oldItem: MessageReceipt, newItem: MessageReceipt): Boolean {
            return oldItem.sender?.uid == newItem.sender?.uid &&
                    oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: MessageReceipt, newItem: MessageReceipt): Boolean {
            return oldItem.readAt == newItem.readAt &&
                    oldItem.deliveredAt == newItem.deliveredAt
        }
    }
}
