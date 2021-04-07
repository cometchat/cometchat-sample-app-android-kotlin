package com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.fragment

import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.adapter.ReactionAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.model.Reaction
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.ReactionUtils

class FragmentReactionNature : FragmentReaction() {

    val TAG = "FragmentEmojiNature"

    private lateinit var mRootView: View
    private lateinit var mData: Array<Reaction?>
    private var mUseSystemDefault = false

    private val USE_SYSTEM_DEFAULT_KEY = "useSystemDefaults"
    private val EMOJI_KEY = "emojic"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.fragment_emoji_objects, container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val gridView = view.findViewById<View>(R.id.Emoji_GridView) as GridView
        val bundle = arguments
        if (bundle == null) {
            mData = ReactionUtils.getNatureList()!!
            mUseSystemDefault = false
        } else {
            val parcels = bundle.getParcelableArray(EMOJI_KEY)
            mData = arrayOfNulls<Reaction>(parcels!!.size)
            for (i in parcels.indices) {
                mData[i] = parcels[i] as Reaction
            }
            mUseSystemDefault = bundle.getBoolean(USE_SYSTEM_DEFAULT_KEY)
        }
        gridView.adapter = ReactionAdapter(view.context, mData)
        gridView.onItemClickListener = this
    }
}