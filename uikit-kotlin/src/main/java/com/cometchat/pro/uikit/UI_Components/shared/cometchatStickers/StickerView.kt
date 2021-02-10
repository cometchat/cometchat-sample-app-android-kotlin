package com.cometchat.pro.uikit.ui_components.shared.cometchatStickers

import com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.adapter.StickerTabAdapter
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.listener.StickerClickListener
import com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.model.Sticker
import com.google.android.material.tabs.TabLayout
import java.util.*

class StickerView : RelativeLayout, StickerClickListener {

    private var c: Context? = null

    private var viewPager: ViewPager? = null

    private var tabLayout: TabLayout? = null

    private var Id: String? = null

    private var type: String? = null

    private var attrs: AttributeSet? = null

    private var adapter: StickerTabAdapter? = null

    private var stickerMap = java.util.HashMap<String?, MutableList<Sticker>?>()

    private var stickerClickListener: StickerClickListener? = null

    constructor(context: Context?) : super(context){
        c = context
        initViewComponent(context, null, -1, -1)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        this.attrs = attrs
        c = context
        initViewComponent(context, attrs, -1, -1)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        c = context
        this.attrs = attrs
        initViewComponent(context, attrs, defStyleAttr, -1)
    }

    private fun initViewComponent(context: Context?, attributeSet: AttributeSet?, defStyleAttr: Int, i: Int) {
        var view = inflate(context, R.layout.cometchat_sticker_view, null)
        val a = getContext().theme.obtainStyledAttributes(attributeSet, R.styleable.SharedMediaView, 0, 0)
        addView(view)

        if (type != null) {
            viewPager = findViewById(R.id.viewPager)
            tabLayout = view.findViewById(R.id.tabLayout)
            adapter = StickerTabAdapter(context!!, (context as FragmentActivity).supportFragmentManager)
        for (str in stickerMap.keys) {
            val bundle = Bundle()
            bundle.putString("Id", Id)
            bundle.putString("type", type)
            val stickersFragment = StickerFragment()
            bundle.putParcelableArrayList("stickerList", stickerMap[str] as ArrayList<out Parcelable?>?)
            stickersFragment.setArguments(bundle)
            stickersFragment.setStickerClickListener(stickerClickListener)
            adapter?.addFragment(stickersFragment, str, stickerMap[str]!![0].url)
        }
        viewPager?.adapter = adapter
        tabLayout?.setupWithViewPager(viewPager)

        for (i in 0 until tabLayout!!.tabCount) {
            tabLayout!!.getTabAt(i)?.setCustomView(createTabItemView(adapter?.getPageIcon(i)!!))
        }

        tabLayout!!.getTabAt(tabLayout!!.selectedTabPosition)!!.view.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        tabLayout!!.setSelectedTabIndicatorColor(resources.getColor(R.color.colorPrimary))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.view.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.view.setBackgroundColor(resources.getColor(android.R.color.transparent))
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    }

    private fun createTabItemView(imgUri: String): View? {
        val imageView = ImageView(context)
        val params = FrameLayout.LayoutParams(72, 72)
        imageView.layoutParams = params
        Glide.with(context).load(imgUri).into(imageView)
        return imageView
    }

    fun setStickerClickListener(stickerClickListener: StickerClickListener?) {
        this.stickerClickListener = stickerClickListener
    }

    public fun setData(uid: String, receiverType: String, stickers: java.util.HashMap<String?, MutableList<Sticker>?>){
        Id = uid
        type = receiverType
        stickerMap = stickers
        reload()
    }

    fun reload() {
        initViewComponent(context, null, -1, -1)
    }

    override fun onClickListener(sticker: Sticker?) {
        stickerClickListener!!.onClickListener(sticker)
    }
}