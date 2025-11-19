package com.cometchat.sampleapp.kotlin.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.data.repository.Repository
import com.cometchat.sampleapp.kotlin.databinding.ActivitySearchBinding
import com.cometchat.sampleapp.kotlin.utils.AppConstants
import com.google.gson.Gson
import org.json.JSONObject

class SearchActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SearchActivity"
    }
    private lateinit var binding: ActivitySearchBinding
    private var user: User? = null
    private var group: Group? = null
    private var isFromMessages: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isFromMessages = intent.getBooleanExtra("isFromMessageScreen", false)
        val userJson = intent.getStringExtra(getString(R.string.app_user))
        val groupJson = intent.getStringExtra(getString(R.string.app_group))
        try {
            if (userJson != null) {
                user = User.fromJson(JSONObject(userJson).toString())
            }
            if (groupJson != null) {
                group = Gson().fromJson(groupJson, Group::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: ", e)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.cometchatSearch.setOnBackPressListener { finish() }
        binding.cometchatSearch.setOnMessageClicked { view, position, baseMessage ->
            handleMessageClick(baseMessage)
        }

        binding.cometchatSearch.setOnConversationClicked { view, position, conversation ->
            if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP) {
                val group = conversation.conversationWith as Group
                navigateToActivity(null, group)
            } else {
                val user = conversation.conversationWith as User
                navigateToActivity(user)
            }
        }

        if (isFromMessages) {
            val searchFilters = listOf(
                UIKitConstants.SearchFilter.PHOTOS,
                UIKitConstants.SearchFilter.VIDEOS,
                UIKitConstants.SearchFilter.DOCUMENTS,
                UIKitConstants.SearchFilter.LINKS,
                UIKitConstants.SearchFilter.AUDIO
            )
            binding.cometchatSearch.searchFilters = searchFilters
            user?.let { user ->
                binding.cometchatSearch.uid = user.uid
                binding.cometchatSearch.setHintText(getString(R.string.cometchat_search_in) + " " + user.name)
            }

            group?.let { group ->
                binding.cometchatSearch.guid = group.guid
                binding.cometchatSearch.setHintText(getString(R.string.cometchat_search_in) + " " + group.name)
            }
        }
    }

    private fun handleMessageClick(goToMessage: BaseMessage) {
        if (UIKitConstants.ReceiverType.USER == goToMessage.receiverType) {
            val uid = if (goToMessage.sender.uid == CometChat.getLoggedInUser().uid) goToMessage.receiverUid else goToMessage.sender.uid
            Repository.getUser(uid, listener = object : CometChat.CallbackListener<User>() {
                override fun onSuccess(user: User) {
                    getParentMessage(goToMessage.parentMessageId, onSuccess = { parentMessage ->
                        navigateToActivity(user = user, group = null, baseMessage = goToMessage, parentMessage = parentMessage)
                    }, onError = {
                        navigateToActivity(user = user, group = null, baseMessage = goToMessage, parentMessage = null)
                    })
                }

                override fun onError(p0: CometChatException?) {
                    Log.e(TAG, "onError: ", p0)
                }
            })
        } else if (UIKitConstants.ReceiverType.GROUP == goToMessage.receiverType) {
            Repository.getGroup(
                goToMessage.receiverUid,
                listener = object : CometChat.CallbackListener<Group>() {
                    override fun onSuccess(group: Group) {
                        getParentMessage(goToMessage.parentMessageId, onSuccess = { parentMessage ->
                            navigateToActivity(user = null, group = group, baseMessage = goToMessage, parentMessage = parentMessage)
                        }, onError = {
                            navigateToActivity(user = null, group = group, baseMessage = goToMessage, parentMessage = null)
                        })
                    }

                    override fun onError(p0: CometChatException?) {
                        Log.e(TAG, "Test: onError: ", p0)
                    }
                })
        }
    }

    private fun getParentMessage(
        parentMessageId: Long,
        onSuccess: (BaseMessage) -> Unit,
        onError: () -> Unit
    ) {
        Repository.fetchMessageInformation(
            parentMessageId,
            callbackListener = object : CometChat.CallbackListener<BaseMessage>() {
                override fun onSuccess(parentMessage: BaseMessage) {
                    onSuccess(parentMessage)
                }

                override fun onError(p0: CometChatException?) {
                    onError()
                }
            })
    }

    private fun navigateToActivity(
        user: User? = null,
        group: Group? = null,
        baseMessage: BaseMessage? = null,
        parentMessage: BaseMessage? = null
    ) {
        val intent: Intent = if (parentMessage != null) {
            Intent(this@SearchActivity, ThreadMessageActivity::class.java)
        } else {
            Intent(this@SearchActivity, MessagesActivity::class.java)
        }
        if (parentMessage != null) intent.putExtra(AppConstants.JSONConstants.RAW_JSON, parentMessage.rawMessage.toString())
        if (user != null) intent.putExtra(getString(R.string.app_user), user.toJson().toString())
        if (group != null) intent.putExtra(getString(R.string.app_group), Gson().toJson(group))
        if (baseMessage != null) intent.putExtra(getString(R.string.app_go_to_message), baseMessage.rawMessage.toString())
        startActivity(intent)
        finish()
    }
}