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

    fun setParentMessage(parentMessage: BaseMessage?) {
        if (parentMessage != null) {
            this.id = parentMessage.id
            this.parentMessage.value = parentMessage
        }
    }
}
