package com.cometchat.uikit.core.utils

import android.content.Context
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for Pin/Unpin action-sheet visibility in [MessageOptionsUtils].
 *
 * Product rule: pin/unpin is visible to EVERYONE — participants included, with no client-side
 * role gate. Enforcement is the server's (RBAC): a user without permission gets
 * ERR_PERMISSION_DENIED on the pin call and the UI surfaces a "you don't have permission"
 * toast. Sent-vs-received makes no difference (no isMyMessage check on pin). The only
 * client-side gates are the feature flag and message eligibility (deleted / not-yet-sent).
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*PinOptionVisibilityTest"
 */
class PinOptionVisibilityTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    lateinit var mockContext: Context
    lateinit var cometChatMock: MockedStatic<CometChat>
    var pinFeatureEnabled = true

    fun groupWithScope(scope: String?, owner: String = "someone-else"): Group {
        val group = MockFactory.createGroup(guid = "g1")
        whenever(group.scope).thenReturn(scope)
        whenever(group.owner).thenReturn(owner)
        return group
    }

    fun optionIdsFor(
        group: Group?,
        pinned: Boolean = false,
        deleted: Boolean = false,
        senderUid: String = "other-user"
    ): List<String> {
        val message = MockFactory.createTextMessage(id = 10L, senderUid = senderUid)
        whenever(message.isPinned).thenReturn(pinned)
        if (deleted) whenever(message.deletedAt).thenReturn(123L)
        return MessageOptionsUtils.getDefaultMessageOptions(mockContext, message, user = null, group = group)
            .map { it.id }
    }

    beforeTest {
        pinFeatureEnabled = true
        mockContext = mock()
        whenever(mockContext.getString(any())).thenReturn("option")
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        val loggedInUser = mock<User>()
        whenever(loggedInUser.uid).thenReturn("test-user")
        cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)
        cometChatMock.`when`<Boolean> { CometChat.isPinMessageEnabled() }.thenAnswer { pinFeatureEnabled }
        cometChatMock.`when`<Boolean> { CometChat.isSaveMessageEnabled() }.thenReturn(true)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        cometChatMock.close()
        println()
    }

    test("participant sees PIN (no client-side role gate — server RBAC enforces)") {
        val ids = optionIdsFor(groupWithScope(CometChatConstants.SCOPE_PARTICIPANT))
        ids.contains(UIKitConstants.MessageOption.PIN) shouldBe true
    }

    test("participant sees UNPIN on a pinned message") {
        val ids = optionIdsFor(groupWithScope(CometChatConstants.SCOPE_PARTICIPANT), pinned = true)
        ids.contains(UIKitConstants.MessageOption.UNPIN) shouldBe true
    }

    test("participant sees PIN on their own message too (no isMyMessage check)") {
        val ids = optionIdsFor(groupWithScope(CometChatConstants.SCOPE_PARTICIPANT), senderUid = "test-user")
        ids.contains(UIKitConstants.MessageOption.PIN) shouldBe true
    }

    test("admin sees PIN on sent and received messages alike") {
        optionIdsFor(groupWithScope(CometChatConstants.SCOPE_ADMIN))
            .contains(UIKitConstants.MessageOption.PIN) shouldBe true
        optionIdsFor(groupWithScope(CometChatConstants.SCOPE_ADMIN), senderUid = "test-user")
            .contains(UIKitConstants.MessageOption.PIN) shouldBe true
    }

    test("moderator sees PIN, and UNPIN on pinned messages") {
        optionIdsFor(groupWithScope(CometChatConstants.SCOPE_MODERATOR))
            .contains(UIKitConstants.MessageOption.PIN) shouldBe true
        optionIdsFor(groupWithScope(CometChatConstants.SCOPE_MODERATOR), pinned = true)
            .contains(UIKitConstants.MessageOption.UNPIN) shouldBe true
    }

    test("null scope (unfetched group) still shows PIN — the server is the enforcer") {
        val ids = optionIdsFor(groupWithScope(null))
        ids.contains(UIKitConstants.MessageOption.PIN) shouldBe true
    }

    test("1-1 conversations show PIN") {
        val ids = optionIdsFor(group = null)
        ids.contains(UIKitConstants.MessageOption.PIN) shouldBe true
    }

    test("PIN and UNPIN are mutually exclusive on pin state") {
        val unpinnedIds = optionIdsFor(groupWithScope(CometChatConstants.SCOPE_PARTICIPANT))
        unpinnedIds.contains(UIKitConstants.MessageOption.PIN) shouldBe true
        unpinnedIds.contains(UIKitConstants.MessageOption.UNPIN) shouldBe false
        val pinnedIds = optionIdsFor(groupWithScope(CometChatConstants.SCOPE_PARTICIPANT), pinned = true)
        pinnedIds.contains(UIKitConstants.MessageOption.PIN) shouldBe false
        pinnedIds.contains(UIKitConstants.MessageOption.UNPIN) shouldBe true
    }

    test("feature flag off hides both PIN and UNPIN for everyone") {
        pinFeatureEnabled = false
        val unpinnedIds = optionIdsFor(groupWithScope(CometChatConstants.SCOPE_ADMIN))
        unpinnedIds.contains(UIKitConstants.MessageOption.PIN) shouldBe false
        val pinnedIds = optionIdsFor(groupWithScope(CometChatConstants.SCOPE_ADMIN), pinned = true)
        pinnedIds.contains(UIKitConstants.MessageOption.UNPIN) shouldBe false
    }

    test("deleted message is not pinnable") {
        val ids = optionIdsFor(groupWithScope(CometChatConstants.SCOPE_ADMIN), deleted = true)
        ids.contains(UIKitConstants.MessageOption.PIN) shouldBe false
        ids.contains(UIKitConstants.MessageOption.UNPIN) shouldBe false
    }
})
