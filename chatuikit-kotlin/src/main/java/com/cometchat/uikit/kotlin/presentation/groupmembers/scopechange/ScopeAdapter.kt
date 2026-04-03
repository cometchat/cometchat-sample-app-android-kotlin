package com.cometchat.uikit.kotlin.presentation.groupmembers.scopechange

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.kotlin.R

/**
 * Adapter for displaying scope/role options with radio buttons.
 * Mirrors the original Java ScopeAdapter exactly.
 *
 * Key behaviors:
 * - Moderators cannot assign Admin scope (radio button disabled, text dimmed)
 * - Current member scope is pre-selected
 * - Single selection via radio buttons
 */
class ScopeAdapter(
    private val context: Context,
    private var roles: List<String>?
) : RecyclerView.Adapter<ScopeAdapter.RoleViewHolder>() {

    private var selectedPosition = -1
    private var group: Group? = null

    @ColorInt private var itemTextColor: Int = 0
    @ColorInt private var disableItemTextColor: Int = 0
    @StyleRes private var itemTextAppearance: Int = 0
    @ColorInt private var disableRadioButtonTint: Int = 0
    @ColorInt private var radioButtonTint: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.cometchat_list_scope_change_item_radio, parent, false)
        return RoleViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RoleViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val role = roles?.get(position) ?: return
        holder.textViewRole.text = role
        holder.radioButton.isChecked = holder.adapterPosition == selectedPosition
        if (itemTextAppearance != 0) {
            holder.textViewRole.setTextAppearance(itemTextAppearance)
        }

        // Moderator cannot assign Admin scope — matches original Java ScopeAdapter
        if (CometChatConstants.SCOPE_MODERATOR.equals(group?.scope, ignoreCase = true) &&
            role.equals(CometChatConstants.SCOPE_ADMIN, ignoreCase = true)
        ) {
            holder.radioButton.isEnabled = false
            holder.radioButton.buttonTintList = ColorStateList.valueOf(disableRadioButtonTint)
            holder.textViewRole.setTextColor(disableItemTextColor)
        } else {
            holder.textViewRole.setTextColor(itemTextColor)
            holder.radioButton.buttonTintList = ColorStateList.valueOf(radioButtonTint)
            holder.radioButton.isEnabled = true
            holder.itemView.setOnClickListener {
                selectedPosition = holder.adapterPosition
                notifyDataSetChanged()
            }
            holder.radioButton.setOnClickListener {
                selectedPosition = holder.adapterPosition
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = roles?.size ?: 0

    fun getSelectedRole(): String? {
        return if (selectedPosition != -1) roles?.get(selectedPosition) else null
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setGroupData(roles: List<String>, member: GroupMember?, group: Group?) {
        if (roles != null && member != null && group != null) {
            this.group = group
            this.roles = roles
            selectedPosition = when {
                member.scope.equals(CometChatConstants.SCOPE_ADMIN, ignoreCase = true) -> 0
                member.scope.equals(CometChatConstants.SCOPE_MODERATOR, ignoreCase = true) -> 1
                else -> 2
            }
            notifyDataSetChanged()
        }
    }

    fun setItemTextColor(@ColorInt color: Int) { itemTextColor = color }
    fun setDisableItemTextColor(@ColorInt color: Int) { disableItemTextColor = color }
    fun setItemTextAppearance(@StyleRes appearance: Int) { itemTextAppearance = appearance }
    fun setRadioButtonTint(@ColorInt color: Int) { radioButtonTint = color }
    fun setDisableRadioButtonTint(@ColorInt color: Int) { disableRadioButtonTint = color }

    class RoleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewRole: TextView = itemView.findViewById(R.id.textViewRole)
        val radioButton: AppCompatRadioButton = itemView.findViewById(R.id.radioButton)
    }
}
