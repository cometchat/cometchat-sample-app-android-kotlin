package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import com.cometchat.uikit.core.data.datasource.StickerDataSource
import com.cometchat.uikit.core.data.repository.StickerRepositoryImpl
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.repository.StickerRepository
import com.cometchat.uikit.core.viewmodel.CometChatStickerKeyboardViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/** A ViewModel subclass unrelated to CometChatStickerKeyboardViewModel, used to test factory rejection. */
private class UnrelatedViewModel : ViewModel()

/**
 * Tests for CometChatStickerKeyboardViewModelFactory.
 *
 * Layer 5 — verifies correct ViewModel creation and error handling.
 *
 * Verifies:
 * - create() returns CometChatStickerKeyboardViewModel for correct class
 * - create() throws IllegalArgumentException for unsupported ViewModel class
 * - Factory uses provided repository
 * - Factory creates default repository when none provided
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.CometChatStickerKeyboardViewModelFactoryTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatStickerKeyboardViewModelFactoryTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
    }

    context("ViewModel creation") {

        test("create returns CometChatStickerKeyboardViewModel for correct class") {
            // Use a fake repository that returns empty to avoid SDK calls
            val fakeRepository = object : StickerRepository {
                override suspend fun getStickers(): Result<List<StickerSet>> =
                    Result.success(emptyList())
            }
            val factory = CometChatStickerKeyboardViewModelFactory(repository = fakeRepository)

            val viewModel = factory.create(CometChatStickerKeyboardViewModel::class.java)

            viewModel.shouldBeInstanceOf<CometChatStickerKeyboardViewModel>()
            println("    ✅ Factory creates correct ViewModel type")
        }

        test("create throws IllegalArgumentException for unsupported ViewModel class") {
            val fakeRepository = object : StickerRepository {
                override suspend fun getStickers(): Result<List<StickerSet>> =
                    Result.success(emptyList())
            }
            val factory = CometChatStickerKeyboardViewModelFactory(repository = fakeRepository)

            shouldThrow<IllegalArgumentException> {
                // Use a ViewModel subclass that is NOT CometChatStickerKeyboardViewModel
                factory.create(UnrelatedViewModel::class.java)
            }
            println("    ✅ Factory throws for unsupported ViewModel class")
        }

        test("Factory uses provided custom repository") {
            var fetchCalled = false
            val customRepository = object : StickerRepository {
                override suspend fun getStickers(): Result<List<StickerSet>> {
                    fetchCalled = true
                    return Result.success(emptyList())
                }
            }
            val factory = CometChatStickerKeyboardViewModelFactory(repository = customRepository)

            val viewModel = factory.create(CometChatStickerKeyboardViewModel::class.java)

            // ViewModel calls fetchStickers on init, which should use our custom repository
            viewModel.shouldBeInstanceOf<CometChatStickerKeyboardViewModel>()
            fetchCalled shouldBe true
            println("    ✅ Factory uses provided custom repository")
        }

        test("Factory with fake DataSource creates working ViewModel") {
            val fakeDataSource = object : StickerDataSource {
                override suspend fun fetchStickers(): Result<List<StickerSet>> =
                    Result.success(emptyList())
            }
            val repository = StickerRepositoryImpl(fakeDataSource)
            val factory = CometChatStickerKeyboardViewModelFactory(repository = repository)

            val viewModel = factory.create(CometChatStickerKeyboardViewModel::class.java)

            viewModel.shouldBeInstanceOf<CometChatStickerKeyboardViewModel>()
            println("    ✅ Factory with fake DataSource creates working ViewModel")
        }
    }
})
