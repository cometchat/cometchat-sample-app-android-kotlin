package screen.call

import adapter.TabAdapter
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.uikit.CometChatCallList
import com.cometchat.pro.uikit.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.tabs.TabLayout
import listeners.OnItemClickListener
import screen.CometChatUserCallListScreenActivity
import utils.Utils
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
class CometChatCallListScreen constructor() : Fragment() {
    private val rvCallList: CometChatCallList? = null
    private val messageRequest //Uses to fetch Conversations.
            : MessagesRequest? = null
    private var tvTitle: TextView? = null
    private val conversationShimmer: ShimmerFrameLayout? = null
//    private var v: View? = null
    private val callList: List<Call> = ArrayList()
    private var tabAdapter: TabAdapter? = null
    private var viewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null
    private var phoneAddIv: ImageView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.call_screen, container, false)
        tvTitle = v!!.findViewById(R.id.tv_title)
        phoneAddIv = v!!.findViewById(R.id.add_phone_iv)
        phoneAddIv!!.setOnClickListener(View.OnClickListener { openUserListScreen() })
        viewPager = v!!.findViewById(R.id.viewPager)
        tabLayout = v!!.findViewById(R.id.tabLayout)
        if (activity != null) {
            tabAdapter = TabAdapter(activity!!.supportFragmentManager)
            tabAdapter!!.addFragment(AllCall(), context!!.resources.getString(R.string.all))
            tabAdapter!!.addFragment(MissedCall(), context!!.resources.getString(R.string.missed))
            viewPager!!.setAdapter(tabAdapter)
        }
        tabLayout!!.setupWithViewPager(viewPager)
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
        val intent = Intent(context, CometChatUserCallListScreenActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private val events: OnItemClickListener<*>? = null
        private const val TAG = "CallList"
    }
}