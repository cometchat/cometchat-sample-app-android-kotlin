package com.cometchat.uikit.core.utils

import android.content.Context
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * ENG-38903 — the thread-subscription toggle is offered in BOTH 1-1 and group conversations, on
 * messages you sent as well as ones you received.
 *
 * It used to be group-only (matching the React kit's bell), which is why QA saw no option at all in
 * a 1-1 and reported it as "missing for sent messages". The gate is now the same as threading
 * itself, and the action-sheet option and the header bell
 * ([CometChatThreadSubscription.isAvailableForThread]) must agree — gating one but not the other
 * would let a user subscribe from the sheet with no bell to unsubscribe from.
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*ThreadSubscriptionScopeTest"
 */
class ThreadSubscriptionScopeTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    lateinit var mockContext: Context
    lateinit var cometChatMock: MockedStatic<CometChat>

    val myUid = "me"

    fun optionIds(group: Group?, senderUid: String): List<String> {
        val message = MockFactory.createTextMessage(id = 10L, senderUid = senderUid)
        val user = if (group == null) MockFactory.createUser(uid = "peer-1") else null
        return MessageOptionsUtils.getDefaultMessageOptions(mockContext, message, user = user, group = group)
            .map { it.id }
    }

    /**
     * The flag lives on CometChatUIKit's private settings object; `mockStatic` cannot intercept a
     * Kotlin object's instance methods, so inject the settings directly.
     */
    fun setThreadSubscription(enabled: Boolean) {
        val field = CometChatUIKit::class.java.getDeclaredField("authenticationSettings")
        field.isAccessible = true
        field.set(
            CometChatUIKit,
            UIKitSettings.UIKitSettingsBuilder()
                .setAppId("app").setRegion("us").setAuthKey("key")
                .setEnableThreadSubscription(enabled)
                .build()
        )
    }

    beforeTest {
        mockContext = mock()
        whenever(mockContext.getString(any())).thenReturn("label")
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        val loggedIn = mock<com.cometchat.chat.models.User>()
        whenever(loggedIn.uid).thenReturn(myUid)
        cometChatMock.`when`<com.cometchat.chat.models.User?> { CometChat.getLoggedInUser() }.thenReturn(loggedIn)
        cometChatMock.`when`<Boolean> { CometChat.isPinMessageEnabled() }.thenReturn(true)
        cometChatMock.`when`<Boolean> { CometChat.isSaveMessageEnabled() }.thenReturn(true)
        setThreadSubscription(true)
    }

    afterTest {
        setThreadSubscription(false)
        cometChatMock.close()
    }

    test("offered in a 1-1 conversation — the ENG-38903 regression") {
        optionIds(group = null, senderUid = "peer-1")
            .shouldContain(UIKitConstants.MessageOption.THREAD_SUBSCRIPTION)
    }

    test("offered on a message I SENT, in a 1-1 and in a group") {
        optionIds(group = null, senderUid = myUid)
            .shouldContain(UIKitConstants.MessageOption.THREAD_SUBSCRIPTION)
        optionIds(group = MockFactory.createGroup(guid = "g1"), senderUid = myUid)
            .shouldContain(UIKitConstants.MessageOption.THREAD_SUBSCRIPTION)
    }

    test("still offered in a group — the old behaviour is not lost") {
        optionIds(group = MockFactory.createGroup(guid = "g1"), senderUid = "peer-1")
            .shouldContain(UIKitConstants.MessageOption.THREAD_SUBSCRIPTION)
    }

    test("the feature flag still gates it everywhere") {
        setThreadSubscription(false)

        optionIds(group = null, senderUid = "peer-1")
            .shouldNotContain(UIKitConstants.MessageOption.THREAD_SUBSCRIPTION)
        optionIds(group = MockFactory.createGroup(guid = "g1"), senderUid = "peer-1")
            .shouldNotContain(UIKitConstants.MessageOption.THREAD_SUBSCRIPTION)
    }

    test("the header bell follows the same scope — available for a 1-1 root, not just a group") {
        val oneOnOne = MockFactory.createTextMessage(id = 10L, senderUid = "peer-1")
        whenever(oneOnOne.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
        val inGroup = MockFactory.createTextMessage(id = 11L, senderUid = "peer-1")
        whenever(inGroup.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_GROUP)

        CometChatThreadSubscription.isAvailableForThread(oneOnOne) shouldBe true
        CometChatThreadSubscription.isAvailableForThread(inGroup) shouldBe true
    }

    test("the bell still hides on an un-sent root and when the feature is off") {
        val unsent = MockFactory.createTextMessage(id = 0L, senderUid = myUid)
        CometChatThreadSubscription.isAvailableForThread(unsent) shouldBe false
        CometChatThreadSubscription.isAvailableForThread(null) shouldBe false

        setThreadSubscription(false)
        val sent = MockFactory.createTextMessage(id = 10L, senderUid = myUid)
        CometChatThreadSubscription.isAvailableForThread(sent) shouldBe false
    }
})
