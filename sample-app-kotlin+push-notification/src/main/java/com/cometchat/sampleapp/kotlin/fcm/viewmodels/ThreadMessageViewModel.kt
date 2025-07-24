package com.cometchat.sampleapp.kotlin.fcm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.sampleapp.kotlin.fcm.data.repository.Repository

class ThreadMessageViewModel : ViewModel() {
    val parentMessage: MutableLiveData<BaseMessage> = MutableLiveData()
    private var id: Long = 0

    fun fetchMessageDetails(id: Long) {
        this.id = id
        Repository.fetchMessageInformation(id, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(message: BaseMessage) {
                parentMessage.value = message
            }

            override fun onError(e: CometChatException) {
            }
        })
    }
}
