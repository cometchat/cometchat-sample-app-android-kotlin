package com.cometchat.uikit.core.viewmodel.groupmembers

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.data.datasource.GroupMembersDataSource
import com.cometchat.uikit.core.data.repository.GroupMembersRepositoryImpl
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Full Chain Integration test for GroupMembers.
 * Fake DataSource → real Repository → real UseCases → real ViewModel.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.groupmembers.GroupMembersFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GroupMembersFullChainIntegrationTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    fun createMembers(count: Int): List<GroupMember> {
        return (1..count).map { i -> GroupMember("user-$i", CometChatConstants.SCOPE_PARTICIPANT).apply { name = "User $i" } }
    }

    fun createGroup(): Group {
        return Group().apply { guid = "test-group"; name = "Test Group"; membersCount = 10 }
    }

    fun buildViewModel(dataSource: GroupMembersDataSource): CometChatGroupMembersViewModel {
        println("    [WIRING] FakeDataSource → GroupMembersRepositoryImpl → UseCases → ViewModel")
        val repository = GroupMembersRepositoryImpl(dataSource)
        val fetchUseCase = FetchGroupMembersUseCase(repository)
        val kickUseCase = KickGroupMemberUseCase(repository)
        val banUseCase = BanGroupMemberUseCase(repository)
        val changeScopeUseCase = ChangeMemberScopeUseCase(repository)
        return CometChatGroupMembersViewModel(
            fetchGroupMembersUseCase = fetchUseCase,
            kickGroupMemberUseCase = kickUseCase,
            banGroupMemberUseCase = banUseCase,
            changeMemberScopeUseCase = changeScopeUseCase,
            enableListeners = false
        )
    }

    // ==================== Content path ====================

    test("full chain: DataSource returning 5 members → Content state") {
        println("=== TEST: Content path ===")
        runTest {
            val members = createMembers(5)
            val dataSource = object : GroupMembersDataSource {
                override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = members
                override suspend fun kickGroupMember(guid: String, uid: String) = "ok"
                override suspend fun banGroupMember(guid: String, uid: String) = "ok"
                override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = "ok"
                override fun hasMoreMembers() = true
                override fun resetRequest() {}
            }

            val viewModel = buildViewModel(dataSource)
            viewModel.setGroup(createGroup())
            viewModel.fetchGroupMembers()
            advanceUntilIdle()

            println("    [RESULT] uiState=${viewModel.uiState.value::class.simpleName}, members=${viewModel.members.value.size}")
            viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Content>()
            viewModel.members.value shouldHaveSize 5
            println("    ✅ Content state with 5 members")
        }
    }

    // ==================== Empty path ====================

    test("full chain: DataSource returning empty → Empty state") {
        println("=== TEST: Empty path ===")
        runTest {
            val dataSource = object : GroupMembersDataSource {
                override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = emptyList<GroupMember>()
                override suspend fun kickGroupMember(guid: String, uid: String) = "ok"
                override suspend fun banGroupMember(guid: String, uid: String) = "ok"
                override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = "ok"
                override fun hasMoreMembers() = false
                override fun resetRequest() {}
            }

            val viewModel = buildViewModel(dataSource)
            viewModel.setGroup(createGroup())
            viewModel.fetchGroupMembers()
            advanceUntilIdle()

            println("    [RESULT] uiState=${viewModel.uiState.value::class.simpleName}")
            viewModel.uiState.value shouldBe GroupMembersUIState.Empty
            viewModel.members.value shouldHaveSize 0
            println("    ✅ Empty state")
        }
    }

    // ==================== Error path ====================

    test("full chain: DataSource throwing → Error state") {
        println("=== TEST: Error path ===")
        runTest {
            val dataSource = object : GroupMembersDataSource {
                override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?): List<GroupMember> {
                    throw CometChatException("NET_ERR", "Network error")
                }
                override suspend fun kickGroupMember(guid: String, uid: String) = "ok"
                override suspend fun banGroupMember(guid: String, uid: String) = "ok"
                override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = "ok"
                override fun hasMoreMembers() = true
                override fun resetRequest() {}
            }

            val viewModel = buildViewModel(dataSource)
            viewModel.setGroup(createGroup())
            viewModel.fetchGroupMembers()
            advanceUntilIdle()

            println("    [RESULT] uiState=${viewModel.uiState.value::class.simpleName}")
            viewModel.uiState.value.shouldBeInstanceOf<GroupMembersUIState.Error>()
            println("    ✅ Error state")
        }
    }

    // ==================== Kick removes member ====================

    test("full chain: kickMember removes member from list") {
        println("=== TEST: Kick removes member ===")
        runTest {
            val members = createMembers(3)
            val dataSource = object : GroupMembersDataSource {
                override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = members
                override suspend fun kickGroupMember(guid: String, uid: String) = "ok"
                override suspend fun banGroupMember(guid: String, uid: String) = "ok"
                override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = "ok"
                override fun hasMoreMembers() = false
                override fun resetRequest() {}
            }

            val viewModel = buildViewModel(dataSource)
            viewModel.setGroup(createGroup())
            viewModel.fetchGroupMembers()
            advanceUntilIdle()

            viewModel.members.value shouldHaveSize 3
            println("    [BEFORE] members.size=3")

            viewModel.kickMember(members[1])
            advanceUntilIdle()

            viewModel.members.value shouldHaveSize 2
            println("    [AFTER] members.size=2 (user-2 kicked)")
            println("    ✅ Kick removed member from list")
        }
    }
})
