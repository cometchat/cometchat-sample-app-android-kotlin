package utils

import adapter.GroupListAdapter
import adapter.UserListAdapter
import android.content.Context
import android.view.View
import androidx.databinding.BindingAdapter
import com.cometchat.pro.core.UsersRequest
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.CometChatGroupList
import com.cometchat.pro.uikit.CometChatUserList
import com.cometchat.pro.uikit.R
import listeners.ClickListener
import listeners.RecyclerTouchListener
import listeners.UserListViewListener
import java.util.*

public class CometChatBind {
    private val usersRequest: UsersRequest? = null
    private val userListAdapter: UserListAdapter? = null
    private val groupListAdapter: GroupListAdapter? = null
    private val userHashMap = HashMap<String, User>()
    private val groupHashMap = HashMap<String, Group>()
    private var onClickListener: UserListViewListener? = null
    private var context: Context? = null
    @BindingAdapter(value = ["android:data_source"])
    fun setData(userListView: CometChatUserList?, users: List<User>?) {
        if (users != null) {
            for (user in users) {
                userHashMap[user.uid] = user
            }
        }
    }

    @BindingAdapter(value = ["android:data_source", "android:context"])
    fun setData(groupListView: CometChatGroupList?, c: Context?, groups: List<Group>?) {
        context = c
        if (groups != null) {
            for (group in groups) {
                groupHashMap[group.guid] = group
            }
        }
    }

    @BindingAdapter(value = ["android:itemClickListener"])
    fun setClickListener(userListView: CometChatUserList, context: Context?) {
        try {
            onClickListener = context as UserListViewListener?
            userListView.addOnItemTouchListener(RecyclerTouchListener(context, userListView, object : ClickListener() {
                override fun onClick(var1: View, var2: Int) {
                    val user = var1.getTag(R.string.user) as User
                    onClickListener!!.onClick(user, var2, var1)
                }

                override fun onLongClick(var1: View, var2: Int) {
                    val user = var1.getTag(R.string.user) as User
                    onClickListener!!.onLongClick(user, var2, var1)
                }
            }))
        } catch (e: ClassCastException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }
}