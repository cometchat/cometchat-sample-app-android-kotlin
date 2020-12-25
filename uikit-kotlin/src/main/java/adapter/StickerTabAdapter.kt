package adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import java.util.*

class StickerTabAdapter : FragmentStatePagerAdapter {

    private var myDrawable: Drawable? = null
    private var sb: SpannableStringBuilder? = null
    private var mFragmentList: MutableList<Fragment> = ArrayList()
    private var mFragmentTitleList: MutableList<String> = ArrayList()
    private var mFragmentIconList: MutableList<String> = ArrayList()
    private var context: Context? = null

    constructor(context: Context, fragmentManager: FragmentManager) : super(fragmentManager){
        this.context = context
    }


    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    fun addFragment(fragment: Fragment?, title: String?, icon: String?) {
        mFragmentList.add(fragment!!)
        mFragmentTitleList.add(title!!)
        mFragmentIconList.add(icon!!)
    }
    fun getPageIcon(position: Int): String? {
        return mFragmentIconList[position]
    }
    override fun getPageTitle(position: Int): CharSequence? {
        sb = SpannableStringBuilder("")
        return sb
    }
}