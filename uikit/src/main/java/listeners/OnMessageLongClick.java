package listeners;

import com.cometchat.pro.models.BaseMessage;

import java.util.List;

public interface OnMessageLongClick
{
    void setLongMessageClick(List<BaseMessage> baseMessage);
}
