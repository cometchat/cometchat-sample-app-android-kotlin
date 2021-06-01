package com.cometchat.pro.uikit.ui_components.shared.cometchatSharedMedia

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.calls.call_list.TabAdapter
import com.cometchat.pro.uikit.ui_components.shared.cometchatSharedMedia.fragments.CometChatSharedFiles
import com.cometchat.pro.uikit.ui_components.shared.cometchatSharedMedia.fragments.CometChatSharedImages
import com.cometchat.pro.uikit.ui_components.shared.cometchatSharedMedia.fragments.CometChatSharedVideos
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.google.android.material.tabs.TabLayout

class CometChatSharedMedia : RelativeLayout {
    private var c: Context? = null
    private var viewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null
    private var Id: String? = null
    private var type: String? = null
    private var attrs: AttributeSet? = null
    private var adapter: TabAdapter? = null

    constructor(context: Context) : super(context) {
        this.c = context
        initViewComponent(context, null, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.attrs = attrs
        this.c = context
        initViewComponent(context, attrs, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.c = context
        this.attrs = attrs
        initViewComponent(context, attrs, defStyleAttr, -1)
    }

    private fun initViewComponent(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val view = View.inflate(context, R.layout.cometchat_shared_media, null)
        val a = getContext().theme.obtainStyledAttributes(attributeSet, R.styleable.SharedMediaView, 0, 0)
        addView(view)
        val bundle = Bundle()
        bundle.putString("Id", Id)
        bundle.putString("type", type)
        if (type != null) {
            viewPager = findViewById(R.id.viewPager)
            tabLayout = view.findViewById(R.id.tabLayout)
            adapter = TabAdapter((context as FragmentActivity).supportFragmentManager)
            val images = CometChatSharedImages()
            images.arguments = bundle
            adapter?.addFragment(images, resources.getString(R.string.images))
            val videos = CometChatSharedVideos()
            videos.arguments = bundle
            adapter?.addFragment(videos, resources.getString(R.string.videos))
            val files = CometChatSharedFiles()
            files.arguments = bundle
            adapter?.addFragment(files, resources.getString(R.string.files))
            viewPager?.adapter = adapter
            viewPager?.offscreenPageLimit = 3
            tabLayout?.setupWithViewPager(viewPager)

            if (UIKitSettings.color != null) {
                val wrappedDrawable = DrawableCompat.wrap(resources.getDrawable(R.drawable.tab_layout_background_active))
                DrawableCompat.setTint(wrappedDrawable, Color.parseColor(UIKitSettings.color))
                tabLayout?.getTabAt(tabLayout!!.selectedTabPosition)?.view?.background = wrappedDrawable
                tabLayout?.setSelectedTabIndicatorColor(Color.parseColor(UIKitSettings.color))
            } else {
                tabLayout?.getTabAt(tabLayout!!.selectedTabPosition)?.view?.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                tabLayout?.setSelectedTabIndicatorColor(resources.getColor(R.color.colorPrimary))
            }

            tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    if (UIKitSettings.color!= null) {
                        val wrappedDrawable = DrawableCompat.wrap(resources.getDrawable(R.drawable.tab_layout_background_active))
                        DrawableCompat.setTint(wrappedDrawable, Color.parseColor(UIKitSettings.color))
                        tab.view.background = wrappedDrawable
                    } else tab.view.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    tab.view.setBackgroundColor(resources.getColor(android.R.color.transparent))
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            if (Utils.isDarkMode(context)) {
                tabLayout?.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.grey))
                tabLayout?.setTabTextColors(resources.getColor(R.color.light_grey), resources.getColor(R.color.textColorWhite))
            } else {
                tabLayout?.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
                tabLayout?.setTabTextColors(resources.getColor(R.color.primaryTextColor), resources.getColor(R.color.textColorWhite))
            }
        }
    }

    fun setRecieverId(uid: String?) {
        Id = uid
    }

    fun setRecieverType(receiverTypeUser: String?) {
        type = receiverTypeUser
    }

    fun reload() {
        initViewComponent(context, null, -1, -1)
    }
}