package com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.listener.OnReactionClickListener
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.model.Reaction

open class FragmentReaction : Fragment(), AdapterView.OnItemClickListener {
    private lateinit var reactionClickListener : OnReactionClickListener
    private lateinit var mRootView: View

    fun addEmojiIconClickListener(reactionClickListener: OnReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.fragment_emoji_objects, container, false)
        return mRootView
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val clickedReaction : Reaction = parent?.getItemAtPosition(position) as Reaction
        this.reactionClickListener.onEmojiClicked(clickedReaction)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
}