package com.cometchat.pro.uikit.ui_components.messages.message_actions.listener

import com.cometchat.pro.models.BaseMessage

public interface OnMessageLongClick {
    fun setLongMessageClick(baseMessagesList: List<BaseMessage>?)
}