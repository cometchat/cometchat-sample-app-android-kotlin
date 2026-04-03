package com.cometchat.sampleapp.kotlin.ui.calls

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.CallDetailsHistoryItemsBinding
import java.util.Locale

/**
 * Adapter for displaying call history items in the call details screen.
 *
 * Validates: Requirements 2.7, 2.8
 */
class CallDetailsHistoryAdapter(
    private val context: Context
) : RecyclerView.Adapter<CallDetailsHistoryAdapter.ViewHolder>() {
    
    private var callLogs: List<CallLog> = emptyList()
    
    fun setCallLogs(list: List<CallLog>) {
        this.callLogs = list
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CallDetailsHistoryItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val callLog = callLogs[position]
        val isLoggedInUser = isLoggedInUser(callLog.initiator as CallUser)
        val isMissedOrUnanswered = callLog.status == CometChatCallsConstants.CALL_STATUS_UNANSWERED ||
                callLog.status == CometChatCallsConstants.CALL_STATUS_MISSED
        
        holder.binding.tvInfoDate.setDateText(Utils.callLogsTimeStamp(callLog.initiatedAt, null))
        
        if (callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO ||
            callLog.type == CometChatCallsConstants.CALL_TYPE_VIDEO ||
            callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO_VIDEO) {
            
            when {
                isLoggedInUser -> {
                    holder.binding.tvInfoTitle.setText(R.string.call_outgoing)
                    holder.binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(context))
                    setupCallIcon(
                        holder.binding.ivInfoIcon,
                        AppCompatResources.getDrawable(context, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_outgoing_call),
                        CometChatTheme.getSuccessColor(context)
                    )
                }
                isMissedOrUnanswered -> {
                    holder.binding.tvInfoTitle.setText(R.string.call_missed)
                    holder.binding.tvInfoTitle.setTextColor(CometChatTheme.getErrorColor(context))
                    setupCallIcon(
                        holder.binding.ivInfoIcon,
                        AppCompatResources.getDrawable(context, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_missed_call),
                        CometChatTheme.getErrorColor(context)
                    )
                }
                else -> {
                    holder.binding.tvInfoTitle.setText(R.string.call_incoming)
                    holder.binding.tvInfoTitle.setTextColor(CometChatTheme.getTextColorPrimary(context))
                    setupCallIcon(
                        holder.binding.ivInfoIcon,
                        AppCompatResources.getDrawable(context, com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_incoming_call),
                        CometChatTheme.getSuccessColor(context)
                    )
                }
            }
        }
        
        holder.binding.tvInfoCallDuration.text = formatCallDuration(callLog.totalDurationInMinutes)
    }
    
    private fun setupCallIcon(imageView: ImageView, icon: Drawable?, @ColorInt iconTint: Int) {
        imageView.background = icon
        imageView.backgroundTintList = ColorStateList.valueOf(iconTint)
    }
    
    private fun formatCallDuration(totalDurationInMinutes: Double): String {
        val minutes = totalDurationInMinutes.toInt()
        val seconds = ((totalDurationInMinutes - minutes) * 60).toInt()
        return String.format(Locale.US, "%dm %ds", minutes, seconds)
    }
    
    private fun isLoggedInUser(user: CallUser?): Boolean {
        return CometChatUIKit.getLoggedInUser()?.uid == (user?.uid ?: "")
    }
    
    override fun getItemCount(): Int = callLogs.size
    
    class ViewHolder(val binding: CallDetailsHistoryItemsBinding) : RecyclerView.ViewHolder(binding.root)
}
