package listeners

import com.cometchat.pro.exceptions.CometChatException

public abstract class ExtensionResponseListener<T> {

    abstract fun onResponseSuccess(vararg: T?)

    abstract fun onResponseFailed(e: CometChatException?)

}