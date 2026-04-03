package com.cometchat.sampleapp.kotlin.ui.calls

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.Participant
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.sampleapp.kotlin.databinding.CallDetailsParticipantsItemsBinding
import java.util.Locale

/**
 * Adapter for displaying call participants in the call details screen.
 *
 * Validates: Requirements 2.7, 2.8
 */
class CallDetailsParticipantsAdapter(
    private val callLog: CallLog
) : RecyclerView.Adapter<CallDetailsParticipantsAdapter.ViewHolder>() {
    
    private val participants: List<Participant> = callLog.participants ?: emptyList()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CallDetailsParticipantsItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participant = participants[position]
        holder.binding.avatar.setAvatar(participant.name, participant.avatar)
        holder.binding.tvTitle.text = participant.name
        holder.binding.dateTime.setDateText(Utils.callLogsTimeStamp(callLog.initiatedAt, null))
        holder.binding.tvCallDuration.text = formatCallDuration(participant.totalDurationInMinutes)
    }
    
    private fun formatCallDuration(totalMinutes: Double): String {
        val hours = totalMinutes.toInt() / 60
        val remainingMinutes = totalMinutes.toInt() % 60
        val seconds = ((totalMinutes - totalMinutes.toInt()) * 60).toInt()
        
        return when {
            hours > 0 -> when {
                remainingMinutes == 0 && seconds == 0 -> String.format(Locale.US, "%d hr", hours)
                seconds == 0 -> String.format(Locale.US, "%d hr %d min", hours, remainingMinutes)
                else -> String.format(Locale.US, "%d hr %d min %d sec", hours, remainingMinutes, seconds)
            }
            remainingMinutes == 0 && seconds == 0 -> "0 min"
            seconds == 0 -> String.format(Locale.US, "%d min", remainingMinutes)
            else -> String.format(Locale.US, "%d min %d sec", remainingMinutes, seconds)
        }
    }
    
    override fun getItemCount(): Int = participants.size
    
    class ViewHolder(val binding: CallDetailsParticipantsItemsBinding) : RecyclerView.ViewHolder(binding.root)
}
