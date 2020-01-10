package com.inscripts.cometchatpulse.ViewModel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import android.view.View
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.models.Conversation
import com.cometchat.pro.models.User
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.inscripts.cometchatpulse.Repository.ConversationRepository
import com.inscripts.cometchatpulse.Repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class RecentViewModel(application: Application) : AndroidViewModel(application) {

    private val conversationRepository: ConversationRepository = ConversationRepository()

    var conversation = MutableLiveData<Conversation>()


    var conversationList:MutableLiveData<MutableList<Conversation>> = MutableLiveData()

    var filterList:MutableLiveData<MutableList<Conversation>> = MutableLiveData()

    init {
        conversation = conversationRepository.conversation
        conversationList = conversationRepository.conversationList
        filterList = conversationRepository.conversationFilterList
    }

    fun fetchConversation(LIMIT: Int,shimmer: ShimmerFrameLayout?,isRefresh:Boolean=false){
        conversationRepository.fetchConversations(LIMIT,shimmer,isRefresh)

    }

    fun addMessageListener(tag: String) {
        conversationRepository.addMessageListener(tag)
    }




}

