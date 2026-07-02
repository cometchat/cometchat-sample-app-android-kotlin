package com.cometchat.uikit.compose.presentation.groupmembers

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.domain.repository.GroupMembersRepository
import com.cometchat.uikit.core.domain.usecase.BanGroupMemberUseCase
import com.cometchat.uikit.core.domain.usecase.ChangeMemberScopeUseCase
import com.cometchat.uikit.core.domain.usecase.FetchGroupMembersUseCase
import com.cometchat.uikit.core.domain.usecase.KickGroupMemberUseCase
import com.cometchat.uikit.core.state.GroupMembersUIState
import com.cometchat.uikit.core.viewmodel.CometChatGroupMembersViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Compose JVM tests for CometChatGroupMembers — rendering states, interactions, PBT.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.groupmembers.CometChatGroupMembersRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatGroupMembersRenderingTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    fun createMembers(count: Int, scope: String = CometChatConstants.SCOPE_PARTICIPANT): List<GroupMember> {
        return (1..count).map { i -> GroupMember("user-$i", scope).apply { name = "Member $i" } }
    }

    fun createGroup(): Group = Group().apply { guid = "test-group"; name = "Test Group"; membersCount = 10 }

    fun createViewModel(members: List<GroupMember>): CometChatGroupMembersViewModel {
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = Result.success(members)
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = false
            override fun resetRequest() {}
        }
        val vm = CometChatGroupMembersViewModel(
            FetchGroupMembersUseCase(repository),
            KickGroupMemberUseCase(repository),
            BanGroupMemberUseCase(repository),
            ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
        vm.setGroup(createGroup())
        return vm
    }

    fun createViewModelWithError(): CometChatGroupMembersViewModel {
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) =
                Result.failure<List<GroupMember>>(CometChatException("ERR", "Failed"))
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = true
            override fun resetRequest() {}
        }
        val vm = CometChatGroupMembersViewModel(
            FetchGroupMembersUseCase(repository),
            KickGroupMemberUseCase(repository),
            BanGroupMemberUseCase(repository),
            ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
        vm.setGroup(createGroup())
        return vm
    }

    // ==================== Rendering States ====================

    test("ViewModel with members → Content state") {
        println("=== TEST: Members → Content ===")
        runTest {
            val viewModel = createViewModel(createMembers(5))
            viewModel.fetchGroupMembers()
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Content>()
            viewModel.members.value shouldHaveSize 5
            println("RESULT: Content with 5 members ✅")
        }
    }

    test("ViewModel with empty → Empty state") {
        println("=== TEST: Empty → Empty state ===")
        runTest {
            val viewModel = createViewModel(emptyList())
            viewModel.fetchGroupMembers()
            advanceUntilIdle()
            viewModel.uiState.value shouldBe GroupMembersUIState.Empty
            println("RESULT: Empty state ✅")
        }
    }

    test("ViewModel with error → Error state") {
        println("=== TEST: Error → Error state ===")
        runTest {
            val viewModel = createViewModelWithError()
            viewModel.fetchGroupMembers()
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
            println("RESULT: Error state ✅")
        }
    }

    test("Initial state is Loading") {
        println("=== TEST: Initial Loading ===")
        runTest {
            val repository = object : GroupMembersRepository {
                override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = Result.success(createMembers(3))
                override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
                override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
                override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
                override fun hasMore() = true
                override fun resetRequest() {}
            }
            val vm = CometChatGroupMembersViewModel(
                FetchGroupMembersUseCase(repository), KickGroupMemberUseCase(repository),
                BanGroupMemberUseCase(repository), ChangeMemberScopeUseCase(repository),
                enableListeners = false
            )
            vm.uiState.value shouldBe GroupMembersUIState.Loading
            println("RESULT: Initial state is Loading ✅")
        }
    }

    // ==================== Selection ====================

    test("selectMember adds to selection") {
        println("=== TEST: selectMember ===")
        runTest {
            val viewModel = createViewModel(createMembers(5))
            viewModel.fetchGroupMembers()
            advanceUntilIdle()

            val member = viewModel.members.value[0]
            viewModel.selectMember(member)
            viewModel.selectedMembers.value.size shouldBe 1
            viewModel.isSelected(member) shouldBe true
            println("RESULT: Member selected ✅")
        }
    }

    test("deselectMember removes from selection") {
        println("=== TEST: deselectMember ===")
        runTest {
            val viewModel = createViewModel(createMembers(5))
            viewModel.fetchGroupMembers()
            advanceUntilIdle()

            val member = viewModel.members.value[0]
            viewModel.selectMember(member)
            viewModel.deselectMember(member)
            viewModel.selectedMembers.value.size shouldBe 0
            viewModel.isSelected(member) shouldBe false
            println("RESULT: Member deselected ✅")
        }
    }

    test("clearSelection empties all") {
        println("=== TEST: clearSelection ===")
        runTest {
            val viewModel = createViewModel(createMembers(5))
            viewModel.fetchGroupMembers()
            advanceUntilIdle()

            viewModel.selectMember(viewModel.members.value[0])
            viewModel.selectMember(viewModel.members.value[1])
            viewModel.selectMember(viewModel.members.value[2])
            viewModel.selectedMembers.value.size shouldBe 3

            viewModel.clearSelection()
            viewModel.selectedMembers.value.size shouldBe 0
            println("RESULT: Selection cleared ✅")
        }
    }

    // ==================== PBT ====================

    test("PBT: Any member count → correct state") {
        println("=== PBT: Member count → state ===")
        checkAll(20, Arb.int(0..15)) { count ->
            runTest {
                val viewModel = createViewModel(createMembers(count))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                if (count == 0) {
                    viewModel.uiState.value shouldBe GroupMembersUIState.Empty
                } else {
                    viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Content>()
                    viewModel.members.value shouldHaveSize count
                }
                println("  [Iteration] count=$count → ${if (count == 0) "Empty" else "Content"} ✅")
            }
        }
    }

    test("PBT: Members with different scopes are stored correctly") {
        println("=== PBT: Scope stored correctly ===")
        checkAll(20, Arb.element(CometChatConstants.SCOPE_ADMIN, CometChatConstants.SCOPE_MODERATOR, CometChatConstants.SCOPE_PARTICIPANT)) { scope ->
            runTest {
                val viewModel = createViewModel(createMembers(3, scope))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()
                viewModel.members.value.forEach { it.scope shouldBe scope }
                println("  [Iteration] scope=$scope → all 3 members correct ✅")
            }
        }
    }

    // ==================== Kick/Ban ====================

    test("kickMember removes member from list") {
        println("=== TEST: kickMember removes ===")
        runTest {
            val viewModel = createViewModel(createMembers(3))
            viewModel.fetchGroupMembers()
            advanceUntilIdle()
            viewModel.members.value shouldHaveSize 3

            viewModel.kickMember(viewModel.members.value[1])
            advanceUntilIdle()
            viewModel.members.value shouldHaveSize 2
            println("RESULT: Kick removed member ✅")
        }
    }

    test("banMember removes member from list") {
        println("=== TEST: banMember removes ===")
        runTest {
            val viewModel = createViewModel(createMembers(3))
            viewModel.fetchGroupMembers()
            advanceUntilIdle()

            viewModel.banMember(viewModel.members.value[0])
            advanceUntilIdle()
            viewModel.members.value shouldHaveSize 2
            println("RESULT: Ban removed member ✅")
        }
    }
})
