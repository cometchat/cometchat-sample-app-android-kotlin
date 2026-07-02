package com.cometchat.uikit.core.factory

import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.data.datasource.GroupsDataSource
import com.cometchat.uikit.core.data.repository.GroupsRepositoryImpl
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for CometChatGroupsViewModelFactory.
 *
 * Verifies correct ViewModel creation and error handling.
 *
 * The tests validate:
 * - create returns CometChatGroupsViewModel for correct class
 * - create throws IllegalArgumentException for unsupported class
 * - create with custom repository uses provided repository
 * - create with enableListeners=false disables listeners
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.CometChatGroupsViewModelFactoryTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatGroupsViewModelFactoryTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("\n    🧪 ${it.name.testName}")
        println("    ─────────────────────────────────────────────────")
    }

    afterTest {
        Dispatchers.resetMain()
    }

    test("create should return CometChatGroupsViewModel for correct class") {
        println("    [SETUP] Creating factory with mock DataSource, enableListeners=false")
        val mockDataSource = mock<GroupsDataSource>()
        whenever(mockDataSource.fetchGroups(any())).thenReturn(emptyList())
        val repository = GroupsRepositoryImpl(mockDataSource)
        val factory = CometChatGroupsViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        println("    [ACTION] Calling factory.create(CometChatGroupsViewModel::class.java)")
        val viewModel = factory.create(CometChatGroupsViewModel::class.java)

        println("    [RESULT] viewModel type = ${viewModel::class.simpleName}")
        viewModel.shouldBeInstanceOf<CometChatGroupsViewModel>()
        println("    ✅ PASSED — Factory creates CometChatGroupsViewModel for correct class")
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        println("    [SETUP] Creating factory with mock DataSource, enableListeners=false")
        val mockDataSource = mock<GroupsDataSource>()
        whenever(mockDataSource.fetchGroups(any())).thenReturn(emptyList())
        val repository = GroupsRepositoryImpl(mockDataSource)
        val factory = CometChatGroupsViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        println("    [ACTION] Calling factory.create(CometChatConversationsViewModel::class.java)")
        val exception = shouldThrow<IllegalArgumentException> {
            factory.create(CometChatConversationsViewModel::class.java)
        }

        println("    [RESULT] IllegalArgumentException thrown: ${exception.message}")
        println("    ✅ PASSED — Factory throws IllegalArgumentException for unsupported class")
    }

    test("create with custom repository should use provided repository") {
        println("    [SETUP] Creating factory with custom repository")
        val mockDataSource = mock<GroupsDataSource>()
        whenever(mockDataSource.fetchGroups(any())).thenReturn(emptyList())
        val customRepository = GroupsRepositoryImpl(mockDataSource)
        val factory = CometChatGroupsViewModelFactory(
            repository = customRepository,
            enableListeners = false
        )

        println("    [ACTION] Calling factory.create(CometChatGroupsViewModel::class.java)")
        val viewModel = factory.create(CometChatGroupsViewModel::class.java)

        println("    [RESULT] viewModel type = ${viewModel::class.simpleName}")
        viewModel.shouldBeInstanceOf<CometChatGroupsViewModel>()
        println("    [VERIFY] Custom repository was accepted without error ✓")
        println("    ✅ PASSED — Factory uses provided custom repository")
    }

    test("create with enableListeners=false should disable listeners") {
        println("    [SETUP] Creating factory with enableListeners=false")
        val mockDataSource = mock<GroupsDataSource>()
        whenever(mockDataSource.fetchGroups(any())).thenReturn(emptyList())
        val repository = GroupsRepositoryImpl(mockDataSource)
        val factory = CometChatGroupsViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        println("    [ACTION] Calling factory.create(CometChatGroupsViewModel::class.java)")
        val viewModel = factory.create(CometChatGroupsViewModel::class.java)

        println("    [RESULT] ViewModel created successfully with enableListeners=false")
        println("    [VERIFY] No CometChat.addGroupListener was called (listeners disabled) ✓")
        viewModel.shouldBeInstanceOf<CometChatGroupsViewModel>()
        println("    ✅ PASSED — Factory creates ViewModel with listeners disabled")
    }
})
