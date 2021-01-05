package com.cometchat.pro.uikit.reaction

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.reaction.fragment.FragmentEmojiNature
import com.cometchat.pro.uikit.reaction.fragment.FragmentEmojiObject
import com.cometchat.pro.uikit.reaction.fragment.FragmentEmojiPeople
import com.cometchat.pro.uikit.reaction.fragment.FragmentEmojiPlaces
import com.cometchat.pro.uikit.reaction.model.Reaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class ReactionDialog : DialogFragment(), OnEmojiClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var frameLayout: FrameLayout

    private lateinit var emojiClickListener: OnEmojiClickListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.dialog!!.setCanceledOnTouchOutside(true)
        return inflater.inflate(R.layout.cometchat_reaction_window, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        frameLayout = view.findViewById(R.id.frame)
        bottomNavigationView = view.findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        val fragmentEmoji: FragmentEmoji = FragmentEmojiPeople()
        fragmentEmoji.addEmojiIconClickListener(emojiClickListener)
        loadFragment(fragmentEmoji)
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
        emojiClickListener.onEmojiClicked(emojicon)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var id = item.itemId
        var fragment = FragmentEmoji()
        when (id) {
            R.id.menu_people -> {
                fragment = FragmentEmojiPeople()
            }
            R.id.menu_nature -> {
                fragment = FragmentEmojiNature()
            }
            R.id.menu_places -> {
                fragment = FragmentEmojiPlaces()
            }
            R.id.menu_object -> {
                fragment = FragmentEmojiObject()
            }
        }
        fragment.addEmojiIconClickListener(emojiClickListener)
        return loadFragment(fragment)
    }
    fun setOnEmojiClick(emojiClickListener: OnEmojiClickListener?) {
        this.emojiClickListener = emojiClickListener!!
    }
}