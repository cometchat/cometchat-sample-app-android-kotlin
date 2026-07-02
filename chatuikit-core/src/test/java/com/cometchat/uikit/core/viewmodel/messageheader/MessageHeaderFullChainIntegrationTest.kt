package com.cometchat.uikit.core.viewmodel.messageheader

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageHeaderDataSource
import com.cometchat.uikit.core.data.repository.MessageHeaderRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.state.MessageHeaderUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Full chain integration test for MessageHeader.
 * Uses fake DataSource → real Repository → real UseCases → real ViewModel.
 * Only the DataSource is faked — everything else is real.
 *
 * Reference: ConversationFullChainIntegrationTest.kt
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageHeaderFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageHeaderFullChainIntegrationTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    fun buildViewModel(dataSource: MessageHeaderDataSource): CometChatMessageHeaderViewModel {
        val repository = MessageHeaderRepositoryImpl(dataSource)
        val getUserUseCase = GetUserUseCase(repository)
        val getGroupUseCase = GetGroupUseCase(repository)
        return CometChatMessageHeaderViewModel(
            getUserUseCase = getUserUseCase,
            getGroupUseCase = getGroupUseCase,
            enableListeners = false
        )
    }

    // ==================== User Flow ====================

    test("full chain: setUser → UserContent state with correct data") {
        runTest {
            val fakeUser = MockFactory.createUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_ONLINE)
            val dataSource = object : MessageHeaderDataSource {
                override suspend fun getUser(uid: String): User = fakeUser
                override suspend fun getGroup(guid: String): Group = throw NotImplementedError()
            }
            println("    → Building full chain with fake DataSource returning user: Alice")

            val viewModel = buildViewModel(dataSource)
            viewModel.setUser(fakeUser)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
            state.user.uid shouldBe "user-1"
            state.user.name shouldBe "Alice"
            state.user.status shouldBe CometChatConstants.USER_STATUS_ONLINE
            println("    → State: UserContent(uid=${state.user.uid}, name=${state.user.name}, status=${state.user.status})")
        }
    }

    test("full chain: refreshUser success → updates UserContent state") {
        runTest {
            val initialUser = MockFactory.createUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_OFFLINE)
            val refreshedUser = MockFactory.createUser(uid = "user-1", name = "Alice Updated", status = CometChatConstants.USER_STATUS_ONLINE)
            val dataSource = object : MessageHeaderDataSource {
                var callCount = 0
                override suspend fun getUser(uid: String): User {
                    callCount++
                    return if (callCount == 1) refreshedUser else refreshedUser
                }
                override suspend fun getGroup(guid: String): Group = throw NotImplementedError()
            }
            println("    → Building full chain, will refresh user")

            val viewModel = buildViewModel(dataSource)
            viewModel.setUser(initialUser)
            advanceUntilIdle()

            // Refresh should fetch from DataSource via UseCase
            viewModel.refreshUser("user-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value as MessageHeaderUIState.UserContent
            state.user.name shouldBe "Alice Updated"
            state.user.status shouldBe CometChatConstants.USER_STATUS_ONLINE
            println("    → After refresh: name=${state.user.name}, status=${state.user.status}")
        }
    }

    test("full chain: refreshUser failure → emits errorEvent") {
        runTest {
            val initialUser = MockFactory.createUser(uid = "user-1", name = "Alice")
            val dataSource = object : MessageHeaderDataSource {
                override suspend fun getUser(uid: String): User {
                    throw CometChatException("ERR_NETWORK", "Network timeout")
                }
                override suspend fun getGroup(guid: String): Group = throw NotImplementedError()
            }
            println("    → Building full chain with failing DataSource")

            val viewModel = buildViewModel(dataSource)
            viewModel.setUser(initialUser)
            advanceUntilIdle()

            var emittedError: CometChatException? = null
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { emittedError = it }
            }

            viewModel.refreshUser("user-1")
            advanceUntilIdle()

            emittedError?.code shouldBe "ERR_NETWORK"
            emittedError?.message shouldBe "Network timeout"
            println("    → Error emitted: code=${emittedError?.code}, message=${emittedError?.message}")
            job.cancel()
        }
    }

    // ==================== Group Flow ====================

    test("full chain: setGroup → GroupContent state with correct data") {
        runTest {
            val fakeGroup = MockFactory.createGroup(
                guid = "group-1",
                name = "Developers",
                type = CometChatConstants.GROUP_TYPE_PRIVATE,
                membersCount = 15
            )
            val dataSource = object : MessageHeaderDataSource {
                override suspend fun getUser(uid: String): User = throw NotImplementedError()
                override suspend fun getGroup(guid: String): Group = fakeGroup
            }
            println("    → Building full chain with fake DataSource returning group: Developers")

            val viewModel = buildViewModel(dataSource)
            viewModel.setGroup(fakeGroup)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()
            state.group.guid shouldBe "group-1"
            state.group.name shouldBe "Developers"
            state.group.groupType shouldBe CometChatConstants.GROUP_TYPE_PRIVATE
            viewModel.memberCount.value shouldBe 15
            println("    → State: GroupContent(guid=${state.group.guid}, name=${state.group.name}, type=${state.group.groupType}, members=${viewModel.memberCount.value})")
        }
    }

    test("full chain: refreshGroup success → updates GroupContent state") {
        runTest {
            val initialGroup = MockFactory.createGroup(guid = "group-1", name = "Devs", membersCount = 5)
            val refreshedGroup = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 20)
            val dataSource = object : MessageHeaderDataSource {
                override suspend fun getUser(uid: String): User = throw NotImplementedError()
                override suspend fun getGroup(guid: String): Group = refreshedGroup
            }
            println("    → Building full chain, will refresh group")

            val viewModel = buildViewModel(dataSource)
            viewModel.setGroup(initialGroup)
            advanceUntilIdle()

            viewModel.refreshGroup("group-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value as MessageHeaderUIState.GroupContent
            state.group.name shouldBe "Developers"
            viewModel.memberCount.value shouldBe 20
            println("    → After refresh: name=${state.group.name}, members=${viewModel.memberCount.value}")
        }
    }

    test("full chain: refreshGroup failure → emits errorEvent") {
        runTest {
            val initialGroup = MockFactory.createGroup(guid = "group-1", name = "Devs")
            val dataSource = object : MessageHeaderDataSource {
                override suspend fun getUser(uid: String): User = throw NotImplementedError()
                override suspend fun getGroup(guid: String): Group {
                    throw CometChatException("ERR_GROUP", "Group fetch failed")
                }
            }
            println("    → Building full chain with failing group DataSource")

            val viewModel = buildViewModel(dataSource)
            viewModel.setGroup(initialGroup)
            advanceUntilIdle()

            var emittedError: CometChatException? = null
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { emittedError = it }
            }

            viewModel.refreshGroup("group-1")
            advanceUntilIdle()

            emittedError?.code shouldBe "ERR_GROUP"
            println("    → Error emitted: code=${emittedError?.code}")
            job.cancel()
        }
    }

    // ==================== Switching Context ====================

    test("full chain: switching from user to group clears user state") {
        runTest {
            val user = MockFactory.createUser(uid = "user-1", name = "Alice")
            val group = MockFactory.createGroup(guid = "group-1", name = "Devs", membersCount = 8)
            val dataSource = object : MessageHeaderDataSource {
                override suspend fun getUser(uid: String): User = user
                override suspend fun getGroup(guid: String): Group = group
            }
            println("    → Testing user → group switch")

            val viewModel = buildViewModel(dataSource)

            viewModel.setUser(user)
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
            viewModel.user.value?.uid shouldBe "user-1"

            viewModel.setGroup(group)
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()
            viewModel.user.value shouldBe null
            viewModel.group.value?.guid shouldBe "group-1"
            viewModel.memberCount.value shouldBe 8
            println("    → After switch: user=null, group=group-1, members=8")
        }
    }

    test("full chain: switching from group to user clears group state") {
        runTest {
            val user = MockFactory.createUser(uid = "user-1", name = "Alice")
            val group = MockFactory.createGroup(guid = "group-1", name = "Devs", membersCount = 8)
            val dataSource = object : MessageHeaderDataSource {
                override suspend fun getUser(uid: String): User = user
                override suspend fun getGroup(guid: String): Group = group
            }
            println("    → Testing group → user switch")

            val viewModel = buildViewModel(dataSource)

            viewModel.setGroup(group)
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()

            viewModel.setUser(user)
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
            viewModel.group.value shouldBe null
            viewModel.user.value?.uid shouldBe "user-1"
            println("    → After switch: group=null, user=user-1")
        }
    }
})
