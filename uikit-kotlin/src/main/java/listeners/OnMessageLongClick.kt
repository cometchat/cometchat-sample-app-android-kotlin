package listeners

import com.cometchat.pro.models.BaseMessage

public interface OnMessageLongClick {
    fun setLongMessageClick(baseMessagesList: List<BaseMessage>?)
}