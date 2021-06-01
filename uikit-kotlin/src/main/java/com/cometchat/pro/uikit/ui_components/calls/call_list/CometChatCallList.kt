package com.cometchat.pro.uikit.ui_components.calls.call_list

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatCalls.CometChatCalls
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.tabs.TabLayout
import java.util.*

/**
 * * Purpose - CometChatCallList class is a activity used to display list of calls recieved to user and perform certain action on click of item.
 * It also consist of two tabs **All** and **Missed Call**.
 *
 * Created on - 23rd March 2020
 *
 * Modified on  - 24th March 2020
 *
 */
class CometChatCallList constructor() : Fragment() {
    private val rvCallList: CometChatCalls? = null
    private val messageRequest //Uses to fetch Conversations.
            : MessagesRequest? = null
    private var tvTitle: TextView? = null
    private var conversationShimmer: ShimmerFrameLayout? = null
//    private var v: View? = null
    private val callList: List<Call> = ArrayList()
    private var tabAdapter: TabAdapter? = null
    private var viewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null
    private var phoneAddIv: ImageView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_cometchat_calls, container, false)
        conversationShimmer = v?.findViewById(R.id.shimmer_layout)
        tvTitle = v?.findViewById(R.id.tv_title)
        tvTitle?.typeface = FontUtils.getInstance(activity).getTypeFace(FontUtils.robotoMedium)
        phoneAddIv = v.findViewById(R.id.add_phone_iv)
        FeatureRestriction.isOneOnOneAudioCallEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (!p0) phoneAddIv?.visibility = View.GONE
            }
        })
        phoneAddIv!!.setOnClickListener(View.OnClickListener { openUserListScreen() })
        viewPager = v.findViewById(R.id.viewPager)
        tabLayout = v.findViewById(R.id.tabLayout)
        if (activity != null) {
            tabAdapter = TabAdapter(activity!!.supportFragmentManager)
            tabAdapter!!.addFragment(AllCall(), context!!.resources.getString(R.string.all))
            tabAdapter!!.addFragment(MissedCall(), context!!.resources.getString(R.string.missed))
            viewPager!!.adapter = tabAdapter
        }
        tabLayout!!.setupWithViewPager(viewPager)

        if (UIKitSettings.color != null) {
            phoneAddIv?.imageTintList = ColorStateList.valueOf(Color.parseColor(UIKitSettings.color))
            val wrappedDrawable = DrawableCompat.wrap(resources.getDrawable(R.drawable.tab_layout_background_active))
            DrawableCompat.setTint(wrappedDrawable, Color.parseColor(UIKitSettings.color))
            tabLayout?.getTabAt(tabLayout!!.selectedTabPosition)!!.view.background = wrappedDrawable
            tabLayout?.setSelectedTabIndicatorColor(Color.parseColor(UIKitSettings.color))
        } else {
            tabLayout?.getTabAt(tabLayout!!.selectedTabPosition)!!.view.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            tabLayout?.setSelectedTabIndicatorColor(resources.getColor(R.color.colorPrimary))
        }

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (UIKitSettings.color != null) {
                    val wrappedDrawable = DrawableCompat.wrap(resources.getDrawable(R.drawable.tab_layout_background_active))
                    DrawableCompat.setTint(wrappedDrawable, Color.parseColor(UIKitSettings.color))
                    tab?.view?.background = wrappedDrawable
                } else tab?.view?.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.view?.setBackgroundColor(resources.getColor(android.R.color.transparent))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        checkDarkMode()
        return v
    }

    private fun checkDarkMode() {
        if (Utils.isDarkMode(context!!)) {
            tvTitle!!.setTextColor(resources.getColor(R.color.textColorWhite))
            tabLayout!!.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.grey))
            tabLayout!!.setTabTextColors(resources.getColor(R.color.textColorWhite), resources.getColor(R.color.light_grey))
        } else {
            tvTitle!!.setTextColor(resources.getColor(R.color.primaryTextColor))
            tabLayout!!.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            tabLayout!!.setTabTextColors(resources.getColor(R.color.primaryTextColor), resources.getColor(R.color.textColorWhite))
        }
    }

    private fun openUserListScreen() {
        val intent = Intent(context, CometChatNewCallList::class.java)
        startActivity(intent)
    }

    companion object {
        private val events: OnItemClickListener<*>? = null
        private const val TAG = "CallList"
    }
}