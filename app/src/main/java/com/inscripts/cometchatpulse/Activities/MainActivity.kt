package com.inscripts.cometchatpulse.Activities

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.helpers.Logger
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Fragment.ContactListFragment
import com.inscripts.cometchatpulse.Fragment.GroupFragment
import com.inscripts.cometchatpulse.Fragment.GroupListFragment
import com.inscripts.cometchatpulse.Fragment.OneToOneFragment
import com.inscripts.cometchatpulse.Helpers.*
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.Repository.GroupRepository
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.GroupChatViewModel
import com.inscripts.cometchatpulse.ViewModel.GroupViewModel
import com.inscripts.cometchatpulse.ViewModel.OnetoOneViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.record_audio.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
        ChildClickListener, OnBackArrowClickListener, OnAlertDialogButtonClickListener,GroupRepository.onGroupJoin {

    private var fragment : Fragment =ContactListFragment()

    private var twoPane: Boolean = false

    private var position: Int = 0

    private lateinit var progressDialog: ProgressDialog

    private lateinit var groupViewModel: GroupViewModel

    private lateinit var group: Group

    var resId: Int = 0

    private lateinit var groupChatViewModel: GroupChatViewModel

    private lateinit var oneToOneChatViewModel: OnetoOneViewModel

    private val TAG="MainActivity"

    private var selectedPage=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(this)

        frame_container_detail
        loadFragment(ContactListFragment())

        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel::class.java)
        groupChatViewModel = ViewModelProviders.of(this).get(GroupChatViewModel::class.java)
        oneToOneChatViewModel = ViewModelProviders.of(this).get(OnetoOneViewModel::class.java)

        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        toolbar.title = ""

        toolbar.setBackgroundColor(StringContract.Color.primaryColor)
        navigation.itemTextColor = getColorStateList()

        navigation.itemIconTintList = getColorStateList()

        overrideFont(this,navigation)


        if (main_frame != null) {
            main_frame.setBackgroundColor(StringContract.Color.primaryColor)
        }

        toolbar.overflowIcon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)

        toolbar_title?.typeface = StringContract.Font.title

        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {

            toolbar_title.setTextColor(StringContract.Color.black)

        } else {

            toolbar_title.setTextColor(StringContract.Color.white)
        }
        toolbar_title?.text = getString(R.string.contacts)

        if (frame_container_detail != null) {
            twoPane = true
        }

        CommonUtil.setStatusBarColor(this)
        
    }


    private fun overrideFont(context: Context, v: View) {
        val typeface = StringContract.Font.status
        try {
            if (v is ViewGroup) {
                for (i in 0..v.childCount) {
                    overrideFont(context, v.getChildAt(i))
                }
            } else if (v is TextView) {
                v.typeface = typeface
            }
        } catch (e: Exception) {

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)

        val menuDrawable = ContextCompat.getDrawable(this, R.drawable.ic_more)
        menuDrawable?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
        menu?.getItem(0)?.setIcon(menuDrawable)

        val menuItem = menu?.findItem(R.id.create_group)?.icon

        menuItem?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)


        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {

            R.id.create_group -> {
                startActivity(Intent(this, CreateGroupActivity::class.java))
            }

            R.id.logout_menu -> {

                CometChat.logout(object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(p0: String?) {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                          finish()
                    }
                    override fun onError(p0: CometChatException?) {
                        Log.e(TAG,p0?.code);
                    }

                })

            }

            R.id.blockedList_menu->{
                startActivity(Intent(this,BlockUserListActivity::class.java))
            }

            R.id.view_menu -> {
                val user = CometChat.getLoggedInUser()
                val profilViewIntent = Intent(this, UserProfileViewActivity::class.java)
                profilViewIntent.putExtra(StringContract.IntentString.USER_NAME, user.name)
                profilViewIntent.putExtra(StringContract.IntentString.USER_ID, user.uid)
                profilViewIntent.putExtra(StringContract.IntentString.USER_STATUS, user.status)
                profilViewIntent.putExtra(StringContract.IntentString.USER_AVATAR, user.avatar)
                startActivity(profilViewIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getColorStateList(): ColorStateList {

        val states = arrayOf(
                intArrayOf(-android.R.attr.state_selected), intArrayOf(android.R.attr.state_selected)
        )
        var colors = intArrayOf()

        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
            colors = intArrayOf(StringContract.Color.inactiveColor, StringContract.Color.iconTint)
        } else {
            colors = intArrayOf(StringContract.Color.inactiveColor, StringContract.Color.primaryColor)
        }
        return ColorStateList(states, colors)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        hideKeyboard()

    }

    private fun  hideKeyboard() {
        val inputManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // check if no view has focus:

        val  currentFocusedView = currentFocus
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    override fun OnChildClick(t: Any) {

        if (twoPane) {
            resId = R.id.frame_container_detail
        } else {
            resId = R.id.main_frame
        }
        if (t is User) {
            val oneToOneFragment = OneToOneFragment().apply {
                arguments = Bundle().apply {
                    putString(StringContract.IntentString.USER_ID, t.uid)
                    putString(StringContract.IntentString.USER_NAME, t.name)
                    putString(StringContract.IntentString.USER_AVATAR, t.avatar)
                    putString(StringContract.IntentString.USER_STATUS, t.status)
                    putLong(StringContract.IntentString.LAST_ACTIVE, t.lastActiveAt)

                }
            }
            supportFragmentManager.beginTransaction()
                    .replace(resId, oneToOneFragment).addToBackStack(null).commit()
        } else if (t is Group) {
            group = t
            initJoinGroup(group, resId)
        }

    }

    fun startGroupChatFragment(t: Group, resId: Int) {
        val groupChat = GroupFragment().apply {
            arguments = Bundle().apply {
                putString(StringContract.IntentString.GROUP_ID, t.guid)
                putString(StringContract.IntentString.GROUP_NAME, t.name)
                putString(StringContract.IntentString.GROUP_ICON, t.icon)
                putString(StringContract.IntentString.GROUP_OWNER, t.owner)
                putString(StringContract.IntentString.GROUP_DESCRIPTION, t.description)
                putString(StringContract.IntentString.USER_SCOPE,t.scope)
            }
        }
        supportFragmentManager.beginTransaction()
                .replace(resId, groupChat).addToBackStack(null).commit()

    }

    private lateinit var groupPassword: String

    override fun onButtonClick(alertDialog: AlertDialog?, v: View?, which: Int, popupId: Int) {

        val groupPasswordInput = v?.findViewById(R.id.edittextDialogueInput) as EditText
        if (which == DialogInterface.BUTTON_NEGATIVE) { // Cancel

            alertDialog?.dismiss()
        } else if (which == DialogInterface.BUTTON_POSITIVE) { // Join
            try {
                progressDialog = ProgressDialog.show(this, "", getString(R.string.joining))
                progressDialog.setProgressStyle(StringContract.Color.primaryColor)
                progressDialog.setCancelable(false)
                groupPassword = groupPasswordInput.text.toString()
                if (groupPassword.length == 0) {
                    groupPasswordInput.setText("")
                    groupPasswordInput.error = getString(R.string.incorrect_password)

                } else {
                    try {
                        alertDialog?.dismiss()
                        group.password = groupPassword

                        groupViewModel.joinGroup(group, progressDialog, resId, this@MainActivity)

                    } catch (e: Exception) {
                        Logger.error("Error at SHA1:UnsupportedEncodingException FOR PASSWORD " + e.localizedMessage)
                        e.printStackTrace()
                    }

                }
            } catch (e: Exception) {
                Logger.error("chatroomFragment.java onButtonClick() : Exception=" + e.localizedMessage)
                e.printStackTrace()
            }

        }
    }


    private fun initJoinGroup(group: Group, resId: Int) {

        if (CommonUtil.isConnected(this@MainActivity)) {

            if (group.groupType.equals(CometChatConstants.GROUP_TYPE_PUBLIC, ignoreCase = true) ||
                    group.groupType.equals(CometChatConstants.GROUP_TYPE_PRIVATE, ignoreCase = true)) {

                if (group.isJoined) {
                    startGroupChatFragment(group, resId)
                } else {
                    progressDialog = ProgressDialog.show(this, "", getString(R.string.joining))
                    progressDialog.setCancelable(false)
                    groupViewModel.joinGroup(group, progressDialog, resId, this@MainActivity)
                }

            } else {
                if (group.isJoined) {
                    startGroupChatFragment(group, resId)
                } else {
                    val dialogview = this.getLayoutInflater().inflate(R.layout.cc_custom_dialog, null)
                    val tvTitle = dialogview.findViewById(R.id.textViewDialogueTitle) as TextView
                    tvTitle.text = ""
                    CustomAlertDialogHelper(this, getString(R.string.group_password), dialogview, getString(R.string.enter),
                            "", getString(R.string.cancel), this, 1, false)
                }
            }
        } else {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss()
            }
            Toast.makeText(this, getString(R.string.warning), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackClick() {

        if (twoPane) {

            if (list_container.visibility == View.VISIBLE) {
                list_container.visibility = View.GONE
            } else if (list_container.visibility == View.GONE) {
                list_container.visibility = View.VISIBLE
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putInt(StringContract.IntentString.POSITION, position)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        try {
            supportActionBar?.title = ""
            navigation.selectedItemId = savedInstanceState?.getInt(StringContract.IntentString.POSITION)!!
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun loadFragment(fragment: Fragment?): Boolean {

        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
            return true
        } else {
            return false
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"onResume: ")
        groupChatViewModel.addGroupEventListener(StringContract.ListenerName.GROUP_EVENT_LISTENER)
        oneToOneChatViewModel.addCallListener(this, TAG, null)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG,"onPause: ")
        groupChatViewModel.removeGroupEventListener(StringContract.ListenerName.GROUP_EVENT_LISTENER)
        oneToOneChatViewModel.removeCallListener(TAG)
    }

    override fun onJoined(group: Group, resId: Int) {
        startGroupChatFragment(group, resId)
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {

        when (p0.itemId) {

            R.id.menu_contacts -> {

                selectedPage=0
                fragment = ContactListFragment()
                toolbar_title?.text = getString(R.string.contacts)
                position = R.id.menu_contacts
            }

            R.id.menu_group -> {
                selectedPage=1
                fragment = GroupListFragment()
                toolbar_title?.text = getString(R.string.groups)
                position = R.id.menu_group

            }

        }
        return loadFragment(fragment)
    }


}
