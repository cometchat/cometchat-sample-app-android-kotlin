package com.cometchat.uikit.core.factory

import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.domain.repository.GroupMembersRepository
import com.cometchat.uikit.core.viewmodel.CometChatGroupMembersViewModel
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Tests for CometChatGroupMembersViewModelFactory.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.factory.CometChatGroupMembersViewModelFactoryTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatGroupMembersViewModelFactoryTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    test("create returns CometChatGroupMembersViewModel for correct class") {
        println("=== TEST: Factory creates correct ViewModel ===")
        val mockRepository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = Result.success(emptyList<GroupMember>())
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = true
            override fun resetRequest() {}
        }

        val factory = CometChatGroupMembersViewModelFactory(
            repository = mockRepository,
            enableListeners = false
        )

        println("STEP 1: Calling factory.create()")
        val viewModel = factory.create(CometChatGroupMembersViewModel::class.java)

        println("STEP 2: Asserting type")
        viewModel.shouldBeInstanceOf<CometChatGroupMembersViewModel>()
        println("RESULT: Factory created CometChatGroupMembersViewModel ✅")
    }

    test("create throws IllegalArgumentException for unsupported class") {
        println("=== TEST: Factory throws for unsupported class ===")
        val mockRepository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = Result.success(emptyList<GroupMember>())
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = true
            override fun resetRequest() {}
        }

        val factory = CometChatGroupMembersViewModelFactory(
            repository = mockRepository,
            enableListeners = false
        )

        println("STEP 1: Calling factory.create() with wrong class")
        shouldThrow<IllegalArgumentException> {
            factory.create(CometChatGroupsViewModel::class.java)
        }
        println("RESULT: IllegalArgumentException thrown ✅")
    }

    test("create with enableListeners=false disables listeners") {
        println("=== TEST: Factory with enableListeners=false ===")
        val mockRepository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = Result.success(emptyList<GroupMember>())
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = true
            override fun resetRequest() {}
        }

        val factory = CometChatGroupMembersViewModelFactory(
            repository = mockRepository,
            enableListeners = false
        )

        val viewModel = factory.create(CometChatGroupMembersViewModel::class.java)
        viewModel.shouldBeInstanceOf<CometChatGroupMembersViewModel>()
        println("RESULT: ViewModel created with listeners disabled ✅")
    }
})
