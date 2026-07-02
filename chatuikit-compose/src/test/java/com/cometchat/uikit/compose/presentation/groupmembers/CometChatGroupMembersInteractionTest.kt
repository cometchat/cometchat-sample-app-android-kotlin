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
 * Interaction tests for CometChatGroupMembers Compose component.
 *
 * Verifies user interactions → ViewModel state changes that drive
 * composable recomposition:
 * - Selection interactions (select/deselect/clear)
 * - Search interactions (searchGroupMembers with keyword, null reset)
 * - Kick/ban member removes from list
 * - PBT: selection count matches distinct selections
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.groupmembers.CometChatGroupMembersInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatGroupMembersInteractionTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== Helpers ====================

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

    // ==================== Selection Interaction Tests ====================

    context("Selection Interactions") {

        test("selectMember adds to selection map") {
            runTest {
                println("=== TEST: selectMember adds ===")
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

        test("deselectMember removes from selection map") {
            runTest {
                println("=== TEST: deselectMember removes ===")
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

        test("clearSelection empties all selections") {
            runTest {
                println("=== TEST: clearSelection empties all ===")
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

        test("selecting same member twice does not duplicate") {
            runTest {
                println("=== TEST: Double select no duplicate ===")
                val viewModel = createViewModel(createMembers(5))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()

                val member = viewModel.members.value[0]
                viewModel.selectMember(member)
                viewModel.selectMember(member)
                viewModel.selectedMembers.value.size shouldBe 1
                println("RESULT: No duplicate on double select ✅")
            }
        }
    }

    // ==================== Search Interaction Tests ====================

    context("Search Interactions") {

        test("searchGroupMembers with keyword — updates list") {
            runTest {
                println("=== TEST: Search updates list ===")
                val allMembers = createMembers(10)
                val searchResults = createMembers(2)

                val repository = object : GroupMembersRepository {
                    private var firstCall = true
                    override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?): Result<List<GroupMember>> {
                        return if (firstCall) { firstCall = false; Result.success(allMembers) }
                        else Result.success(searchResults)
                    }
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
                vm.fetchGroupMembers()
                advanceUntilIdle()
                vm.members.value shouldHaveSize 10

                vm.searchGroupMembers("test")
                advanceUntilIdle()

                vm.members.value shouldHaveSize 2
                println("RESULT: Search updated list ✅")
            }
        }

        test("searchGroupMembers with null — resets to full list") {
            runTest {
                println("=== TEST: Null search resets ===")
                val allMembers = createMembers(5)

                val repository = object : GroupMembersRepository {
                    override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = Result.success(allMembers)
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
                vm.fetchGroupMembers()
                advanceUntilIdle()

                vm.searchGroupMembers(null)
                advanceUntilIdle()

                vm.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Content>()
                vm.members.value shouldHaveSize 5
                println("RESULT: Null search resets ✅")
            }
        }
    }

    // ==================== Kick/Ban Interaction Tests ====================

    context("Kick/Ban Interactions") {

        test("kickMember removes member from list") {
            runTest {
                println("=== TEST: kickMember removes ===")
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
            runTest {
                println("=== TEST: banMember removes ===")
                val viewModel = createViewModel(createMembers(3))
                viewModel.fetchGroupMembers()
                advanceUntilIdle()

                viewModel.banMember(viewModel.members.value[0])
                advanceUntilIdle()
                viewModel.members.value shouldHaveSize 2
                println("RESULT: Ban removed member ✅")
            }
        }
    }

    // ==================== PBT ====================

    context("PBT: Selection Interactions") {

        test("PBT: selection count matches distinct selections") {
            println("=== PBT: Selection count matches distinct ===")
            checkAll(20, Arb.int(1..10)) { selectCount ->
                runTest {
                    val viewModel = createViewModel(createMembers(15))
                    viewModel.fetchGroupMembers()
                    advanceUntilIdle()

                    for (i in 0 until selectCount) {
                        viewModel.selectMember(viewModel.members.value[i])
                    }
                    viewModel.selectedMembers.value.size shouldBe selectCount
                    println("  [Iteration] selectCount=$selectCount ✅")
                }
            }
        }
    }
})
