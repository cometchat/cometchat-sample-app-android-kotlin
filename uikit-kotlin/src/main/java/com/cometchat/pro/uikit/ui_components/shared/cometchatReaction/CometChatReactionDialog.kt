package com.cometchat.pro.uikit.ui_components.shared.cometchatReaction

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.model.Reaction
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.fragment.*
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.listener.OnReactionClickListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class CometChatReactionDialog : DialogFragment(), OnReactionClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var frameLayout: FrameLayout

    private lateinit var reactionClickListener: OnReactionClickListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.dialog!!.setCanceledOnTouchOutside(true)
        return inflater.inflate(R.layout.cometchat_reaction_window, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        frameLayout = view.findViewById(R.id.frame)
        bottomNavigationView = view.findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        val fragmentReaction: FragmentReaction = FragmentReactionPeople()
        fragmentReaction.addEmojiIconClickListener(reactionClickListener)
        loadFragment(fragmentReaction)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setCanceledOnTouchOutside(false)
        return d
    }

    private fun loadFragment(fragment: Fragment) : Boolean{
        if (fragment != null) {
            childFragmentManager.beginTransaction().replace(frameLayout.id, fragment).commit()
            return true
        }
        return false
    }

    override fun onEmojiClicked(emojicon: Reaction) {
        reactionClickListener.onEmojiClicked(emojicon)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var id = item.itemId
        var fragment = FragmentReaction()
        when (id) {
            R.id.menu_people -> {
                fragment = FragmentReactionPeople()
            }
            R.id.menu_nature -> {
                fragment = FragmentReactionNature()
            }
            R.id.menu_places -> {
                fragment = FragmentReactionPlaces()
            }
            R.id.menu_object -> {
                fragment = FragmentReactionObject()
            }
        }
        fragment.addEmojiIconClickListener(reactionClickListener)
        return loadFragment(fragment)
    }
    fun setOnEmojiClick(reactionClickListener: OnReactionClickListener?) {
        this.reactionClickListener = reactionClickListener!!
    }
}