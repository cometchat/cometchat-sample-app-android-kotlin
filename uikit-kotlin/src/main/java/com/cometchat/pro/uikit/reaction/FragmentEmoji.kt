package com.cometchat.pro.uikit.reaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.reaction.model.Reaction

open class FragmentEmoji : Fragment(), AdapterView.OnItemClickListener {
    private lateinit var emojiClickListener : OnEmojiClickListener
    private lateinit var mRootView: View

    fun addEmojiIconClickListener(emojiClickListener: OnEmojiClickListener) {
        this.emojiClickListener = emojiClickListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.fragment_emoji_objects, container, false)
        return mRootView
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val clickedReaction : Reaction = parent?.getItemAtPosition(position) as Reaction
        this.emojiClickListener.onEmojiClicked(clickedReaction)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
}