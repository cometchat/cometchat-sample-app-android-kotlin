package com.cometchat.uikit.core.state

import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Tests for GroupMembersUIState, GroupMembersEvent, and DialogState sealed classes.
 * Pure data class tests — no mocking needed.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.state.GroupMembersUIStateTest"
 */
class GroupMembersUIStateTest : FunSpec({

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    // ==================== GroupMembersUIState ====================

    test("Loading is singleton") {
        println("=== TEST: Loading singleton ===")
        val s1 = GroupMembersUIState.Loading
        val s2 = GroupMembersUIState.Loading
        s1.shouldBeInstanceOf<GroupMembersUIState.Loading>()
        (s1 === s2) shouldBe true
        println("RESULT: Loading is singleton ✅")
    }

    test("Empty is singleton") {
        println("=== TEST: Empty singleton ===")
        val s1 = GroupMembersUIState.Empty
        val s2 = GroupMembersUIState.Empty
        s1.shouldBeInstanceOf<GroupMembersUIState.Empty>()
        (s1 === s2) shouldBe true
        println("RESULT: Empty is singleton ✅")
    }

    test("Error holds exception") {
        println("=== TEST: Error holds exception ===")
        val ex = MockFactory.createCometChatException("ERR", "Failed")
        val state = GroupMembersUIState.Error(ex)
        state.shouldBeInstanceOf<GroupMembersUIState.Error>()
        state.exception.code shouldBe "ERR"
        println("RESULT: Error holds exception ✅")
    }

    test("Content holds members list") {
        println("=== TEST: Content holds members ===")
        val members = listOf(
            GroupMember("user-1", "participant").apply { name = "User 1" },
            GroupMember("user-2", "admin").apply { name = "User 2" }
        )
        val state = GroupMembersUIState.Content(members)
        state.shouldBeInstanceOf<GroupMembersUIState.Content>()
        state.members.size shouldBe 2
        println("RESULT: Content holds 2 members ✅")
    }

    test("All 4 UIState types are distinct") {
        println("=== TEST: Distinct types ===")
        val loading: GroupMembersUIState = GroupMembersUIState.Loading
        val empty: GroupMembersUIState = GroupMembersUIState.Empty
        val error: GroupMembersUIState = GroupMembersUIState.Error(MockFactory.createCometChatException("E", "e"))
        val content: GroupMembersUIState = GroupMembersUIState.Content(emptyList())
        loading shouldNotBe empty
        loading shouldNotBe error
        loading shouldNotBe content
        empty shouldNotBe error
        println("RESULT: All distinct ✅")
    }

    test("PBT: Any error code preserved") {
        println("=== PBT: Error code preserved ===")
        checkAll(20, Arb.string(1..20), Arb.string(1..50)) { code, msg ->
            val ex = MockFactory.createCometChatException(code, msg)
            val state = GroupMembersUIState.Error(ex)
            state.exception.code shouldBe code
            state.exception.message shouldBe msg
        }
        println("RESULT: PBT passed ✅")
    }

    // ==================== GroupMembersEvent ====================

    test("MemberKicked holds member") {
        println("=== TEST: MemberKicked ===")
        val member = GroupMember("user-1", "participant").apply { name = "User 1" }
        val event = GroupMembersEvent.MemberKicked(member)
        event.shouldBeInstanceOf<GroupMembersEvent.MemberKicked>()
        event.member.uid shouldBe "user-1"
        println("RESULT: MemberKicked holds member ✅")
    }

    test("MemberBanned holds member") {
        println("=== TEST: MemberBanned ===")
        val member = GroupMember("user-2", "participant").apply { name = "User 2" }
        val event = GroupMembersEvent.MemberBanned(member)
        event.member.uid shouldBe "user-2"
        println("RESULT: MemberBanned holds member ✅")
    }

    test("MemberScopeChanged holds member and newScope") {
        println("=== TEST: MemberScopeChanged ===")
        val member = GroupMember("user-3", "participant").apply { name = "User 3" }
        val event = GroupMembersEvent.MemberScopeChanged(member, "admin")
        event.member.uid shouldBe "user-3"
        event.newScope shouldBe "admin"
        println("RESULT: MemberScopeChanged correct ✅")
    }

    // ==================== DialogState ====================

    test("DialogState.Hidden is singleton") {
        println("=== TEST: DialogState.Hidden ===")
        val s1 = DialogState.Hidden
        val s2 = DialogState.Hidden
        (s1 === s2) shouldBe true
        println("RESULT: Hidden is singleton ✅")
    }

    test("DialogState.ConfirmKick holds member") {
        println("=== TEST: ConfirmKick ===")
        val member = GroupMember("user-1", "participant")
        val state = DialogState.ConfirmKick(member)
        state.shouldBeInstanceOf<DialogState.ConfirmKick>()
        state.member.uid shouldBe "user-1"
        println("RESULT: ConfirmKick holds member ✅")
    }

    test("DialogState.ConfirmBan holds member") {
        println("=== TEST: ConfirmBan ===")
        val member = GroupMember("user-2", "participant")
        val state = DialogState.ConfirmBan(member)
        state.member.uid shouldBe "user-2"
        println("RESULT: ConfirmBan holds member ✅")
    }

    test("DialogState.SelectScope holds member and currentScope") {
        println("=== TEST: SelectScope ===")
        val member = GroupMember("user-3", "moderator")
        val state = DialogState.SelectScope(member, "moderator")
        state.member.uid shouldBe "user-3"
        state.currentScope shouldBe "moderator"
        println("RESULT: SelectScope correct ✅")
    }
})
