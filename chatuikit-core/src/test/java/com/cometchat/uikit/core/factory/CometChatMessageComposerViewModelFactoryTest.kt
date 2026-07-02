package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import com.cometchat.uikit.core.data.datasource.MessageComposerDataSource
import com.cometchat.uikit.core.data.repository.MessageComposerRepositoryImpl
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockito.kotlin.mock

/**
 * Tests for CometChatMessageComposerViewModelFactory.
 * Layer 5 — Verifies correct ViewModel creation and error handling for wrong class.
 *
 * Uses a mock DataSource to build a real Repository, then verifies the factory
 * creates the correct ViewModel type.
 */
class CometChatMessageComposerViewModelFactoryTest : FunSpec({

    beforeTest {
        println("\n  🧪 ${it.name.testName}")
        println("  ─────────────────────────────────────────────────")
    }

    afterTest {
        println()
    }

    test("create should return CometChatMessageComposerViewModel for correct class") {
        val mockDataSource = mock<MessageComposerDataSource>()
        val repository = MessageComposerRepositoryImpl(mockDataSource)
        val factory = CometChatMessageComposerViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        println("    [SETUP] Factory created with mock DataSource, enableListeners=false")
        val viewModel = factory.create(CometChatMessageComposerViewModel::class.java)

        println("    [Factory] created instance: ${viewModel::class.simpleName}")
        viewModel.shouldBeInstanceOf<CometChatMessageComposerViewModel>()
        println("    ✅ PASSED — Factory creates correct ViewModel type")
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        val mockDataSource = mock<MessageComposerDataSource>()
        val repository = MessageComposerRepositoryImpl(mockDataSource)
        val factory = CometChatMessageComposerViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        println("    [SETUP] Requesting unsupported ViewModel class")
        shouldThrow<IllegalArgumentException> {
            factory.create(UnsupportedViewModel::class.java)
        }
        println("    ✅ PASSED — IllegalArgumentException thrown for unsupported class")
    }

    test("create should pass enableListeners=false to ViewModel for testing") {
        val mockDataSource = mock<MessageComposerDataSource>()
        val repository = MessageComposerRepositoryImpl(mockDataSource)
        val factory = CometChatMessageComposerViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        println("    [SETUP] Verifying enableListeners=false propagation")
        val viewModel = factory.create(CometChatMessageComposerViewModel::class.java)

        // ViewModel should be created without listeners (no crash = success)
        viewModel.shouldBeInstanceOf<CometChatMessageComposerViewModel>()
        println("    ✅ PASSED — ViewModel created with enableListeners=false successfully")
    }
})

/**
 * Dummy ViewModel class used to test factory rejection of unsupported types.
 */
private class UnsupportedViewModel : ViewModel()
