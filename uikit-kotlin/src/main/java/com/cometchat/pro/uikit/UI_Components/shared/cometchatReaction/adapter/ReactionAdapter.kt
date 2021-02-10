package com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.emoji.widget.EmojiTextView
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.model.Reaction
import java.lang.Boolean

class ReactionAdapter : ArrayAdapter<Reaction?> {
    private val mUseSystemDefault = Boolean.FALSE

    // CONSTRUCTOR
    constructor(context: Context?, data: Array<Reaction?>) : super(context!!, R.layout.rsc_emoji_item, data) {}
    constructor(context: Context?, data: List<Reaction?>) : super(context!!, R.layout.rsc_emoji_item, data) {}

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = View.inflate(context, R.layout.rsc_emoji_item, null)
            val viewHolder = ViewHolder(view)
            view.tag = viewHolder
        }
        if (null != getItem(position)) {
            val emoji: Reaction = getItem(position)!!
            val holder = view!!.tag as ViewHolder
            holder.icon.text = String(Character.toChars(emoji.code))
        }
        return view!!
    }

    internal class ViewHolder(view: View) {
        var icon: EmojiTextView = view.findViewById(R.id.emoji_icon)

    }
}