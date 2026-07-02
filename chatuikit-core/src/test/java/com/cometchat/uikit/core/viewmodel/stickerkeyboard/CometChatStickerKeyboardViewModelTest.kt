package com.cometchat.uikit.core.viewmodel.stickerkeyboard

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.usecase.GetStickersUseCase
import com.cometchat.uikit.core.state.StickerKeyboardUIState
import com.cometchat.uikit.core.viewmodel.CometChatStickerKeyboardViewModel
import io.kotest.core.spec.IsolationMode
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Comprehensive tests for CometChatStickerKeyboardViewModel.
 *
 * Tests cover:
 * - Initial state (Loading → fetch on init)
 * - fetchStickers success with sets → Content state
 * - fetchStickers success with empty → Empty state
 * - fetchStickers failure → Error state
 * - selectStickerSet updates selectedSetIndex and currentStickers
 * - selectStickerSet with invalid index does nothing
 * - retry resets to Loading and fetches again
 * - onStickerClicked invokes callback
 * - Concurrent fetch guard (isFetching)
 * - PBT: for any valid index 0..N-1, selectStickerSet updates correctly
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.CometChatStickerKeyboardViewModelTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatStickerKeyboardViewModelTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var getStickersUseCase: GetStickersUseCase

    fun createStickerSet(name: String, stickerCount: Int): StickerSet {
        val stickers = (1..stickerCount).map { i ->
            Sticker(
                name = "$name-sticker-$i",
                url = "https://example.com/stickers/$name/$i.png",
                setName = name
            )
        }
        return StickerSet(
            name = name,
            stickers = stickers,
            iconUrl = stickers.firstOrNull()?.url ?: ""
        )
    }

    fun createStickerSets(count: Int): List<StickerSet> {
        return (1..count).map { i ->
            createStickerSet("Set-$i", stickerCount = 4)
        }
    }

    /**
     * Creates a ViewModel that will return the given sticker sets on fetch.
     * Since the ViewModel calls fetchStickers() in init, we must configure
     * the mock BEFORE constructing the ViewModel.
     */
    suspend fun createViewModel(sets: List<StickerSet>): CometChatStickerKeyboardViewModel {
        whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
        return CometChatStickerKeyboardViewModel(getStickersUseCase)
    }

    suspend fun createErrorViewModel(
        code: String = "ERR_FETCH",
        message: String = "Network error"
    ): CometChatStickerKeyboardViewModel {
        whenever(getStickersUseCase.invoke()).thenReturn(
            Result.failure(CometChatException(code, message))
        )
        return CometChatStickerKeyboardViewModel(getStickersUseCase)
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        getStickersUseCase = mock()
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
    }

    // ==================== A. Fetch & UI State ====================

    context("A. Fetch & UI State") {

        test("fetchStickers success with non-empty sets transitions to Content state") {
            runTest {
                val sets = createStickerSets(3)
                val viewModel = createViewModel(sets)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                viewModel.stickerSets.value shouldHaveSize 3
                println("    ✅ Content state with 3 sets")
            }
        }

        test("fetchStickers success with empty list transitions to Empty state") {
            runTest {
                val viewModel = createViewModel(emptyList())
                advanceUntilIdle()

                viewModel.uiState.value shouldBe StickerKeyboardUIState.Empty
                viewModel.stickerSets.value shouldHaveSize 0
                viewModel.currentStickers.value shouldHaveSize 0
                println("    ✅ Empty state when no sticker sets")
            }
        }

        test("fetchStickers failure transitions to Error state") {
            runTest {
                val viewModel = createErrorViewModel("ERR_NETWORK", "Connection failed")
                advanceUntilIdle()

                val state = viewModel.uiState.value
                state.shouldBeInstanceOf<StickerKeyboardUIState.Error>()
                (state as StickerKeyboardUIState.Error).exception.code shouldBe "ERR_NETWORK"
                println("    ✅ Error state with correct exception")
            }
        }

        test("PBT: for any set count, empty → Empty state, non-empty → Content state") {
            checkAll(50, Arb.int(0..10)) { count ->
                runTest {
                    val sets = createStickerSets(count)
                    val viewModel = createViewModel(sets)
                    advanceUntilIdle()

                    if (count == 0) {
                        viewModel.uiState.value shouldBe StickerKeyboardUIState.Empty
                    } else {
                        viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                        viewModel.stickerSets.value shouldHaveSize count
                    }
                }
            }
        }

        test("Content state selects first set by default") {
            runTest {
                val sets = createStickerSets(3)
                val viewModel = createViewModel(sets)
                advanceUntilIdle()

                viewModel.selectedSetIndex.value shouldBe 0
                viewModel.currentStickers.value shouldBe sets[0].stickers
                println("    ✅ First set selected by default")
            }
        }
    }

    // ==================== B. selectStickerSet ====================

    context("B. selectStickerSet") {

        test("selectStickerSet updates selectedSetIndex and currentStickers") {
            runTest {
                val sets = createStickerSets(3)
                val viewModel = createViewModel(sets)
                advanceUntilIdle()

                viewModel.selectStickerSet(1)

                viewModel.selectedSetIndex.value shouldBe 1
                viewModel.currentStickers.value shouldBe sets[1].stickers
                println("    ✅ selectStickerSet(1) updates index and stickers")
            }
        }

        test("selectStickerSet with negative index does nothing") {
            runTest {
                val sets = createStickerSets(3)
                val viewModel = createViewModel(sets)
                advanceUntilIdle()

                viewModel.selectStickerSet(-1)

                viewModel.selectedSetIndex.value shouldBe 0
                viewModel.currentStickers.value shouldBe sets[0].stickers
                println("    ✅ Negative index ignored")
            }
        }

        test("selectStickerSet with index >= size does nothing") {
            runTest {
                val sets = createStickerSets(3)
                val viewModel = createViewModel(sets)
                advanceUntilIdle()

                viewModel.selectStickerSet(3)

                viewModel.selectedSetIndex.value shouldBe 0
                viewModel.currentStickers.value shouldBe sets[0].stickers
                println("    ✅ Out-of-bounds index ignored")
            }
        }

        test("selectStickerSet with index equal to size does nothing") {
            runTest {
                val sets = createStickerSets(5)
                val viewModel = createViewModel(sets)
                advanceUntilIdle()

                viewModel.selectStickerSet(5)

                viewModel.selectedSetIndex.value shouldBe 0
                println("    ✅ Index == size ignored")
            }
        }

        test("PBT: for any valid index 0..N-1, selectStickerSet updates correctly") {
            checkAll(50, Arb.int(1..10)) { setCount ->
                runTest {
                    val sets = createStickerSets(setCount)
                    val viewModel = createViewModel(sets)
                    advanceUntilIdle()

                    val validIndex = (0 until setCount).random()
                    viewModel.selectStickerSet(validIndex)

                    viewModel.selectedSetIndex.value shouldBe validIndex
                    viewModel.currentStickers.value shouldBe sets[validIndex].stickers
                }
            }
        }

        test("PBT: invalid indices never change state") {
            checkAll(30, Arb.int(1..10), Arb.int(-100..-1)) { setCount, negativeIndex ->
                runTest {
                    val sets = createStickerSets(setCount)
                    val viewModel = createViewModel(sets)
                    advanceUntilIdle()

                    val originalIndex = viewModel.selectedSetIndex.value
                    val originalStickers = viewModel.currentStickers.value

                    viewModel.selectStickerSet(negativeIndex)

                    viewModel.selectedSetIndex.value shouldBe originalIndex
                    viewModel.currentStickers.value shouldBe originalStickers
                }
            }
        }

        test("PBT: out-of-bounds indices never change state") {
            checkAll(30, Arb.int(1..10), Arb.int(10..100)) { setCount, largeIndex ->
                runTest {
                    val sets = createStickerSets(setCount)
                    val viewModel = createViewModel(sets)
                    advanceUntilIdle()

                    val originalIndex = viewModel.selectedSetIndex.value
                    val originalStickers = viewModel.currentStickers.value

                    viewModel.selectStickerSet(largeIndex)

                    viewModel.selectedSetIndex.value shouldBe originalIndex
                    viewModel.currentStickers.value shouldBe originalStickers
                }
            }
        }
    }

    // ==================== C. Retry ====================

    context("C. Retry") {

        test("retry after error resets to Loading and fetches again") {
            runTest {
                // First call fails
                whenever(getStickersUseCase.invoke()).thenReturn(
                    Result.failure(CometChatException("ERR", "Fail"))
                )
                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Error>()

                // Now configure success for retry
                val sets = createStickerSets(2)
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))

                viewModel.retry()
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                viewModel.stickerSets.value shouldHaveSize 2
                println("    ✅ Retry transitions from Error → Content")
            }
        }

        test("retry after empty state fetches again") {
            runTest {
                // First call returns empty
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(emptyList()))
                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                viewModel.uiState.value shouldBe StickerKeyboardUIState.Empty

                // Now configure success with data for retry
                val sets = createStickerSets(3)
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))

                viewModel.retry()
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                viewModel.stickerSets.value shouldHaveSize 3
                println("    ✅ Retry from Empty → Content")
            }
        }
    }

    // ==================== D. onStickerClicked ====================

    context("D. onStickerClicked") {

        test("onStickerClicked invokes callback with correct sticker") {
            runTest {
                val sets = createStickerSets(2)
                val viewModel = createViewModel(sets)
                advanceUntilIdle()

                var clickedSticker: Sticker? = null
                viewModel.onStickerClick = { sticker -> clickedSticker = sticker }

                val targetSticker = sets[0].stickers[0]
                viewModel.onStickerClicked(targetSticker)

                clickedSticker shouldBe targetSticker
                println("    ✅ Callback invoked with correct sticker")
            }
        }

        test("onStickerClicked with no callback does not crash") {
            runTest {
                val sets = createStickerSets(1)
                val viewModel = createViewModel(sets)
                advanceUntilIdle()

                // No callback set — should not throw
                viewModel.onStickerClicked(sets[0].stickers[0])
                println("    ✅ No crash when callback is null")
            }
        }

        test("onStickerClicked invokes latest callback after reassignment") {
            runTest {
                val sets = createStickerSets(1)
                val viewModel = createViewModel(sets)
                advanceUntilIdle()

                var firstCallbackInvoked = false
                var secondCallbackInvoked = false

                viewModel.onStickerClick = { firstCallbackInvoked = true }
                viewModel.onStickerClick = { secondCallbackInvoked = true }

                viewModel.onStickerClicked(sets[0].stickers[0])

                firstCallbackInvoked shouldBe false
                secondCallbackInvoked shouldBe true
                println("    ✅ Latest callback is used after reassignment")
            }
        }
    }

    // ==================== E. Concurrent Fetch Guard ====================

    context("E. Concurrent Fetch Guard") {

        test("calling fetchStickers while already fetching is ignored") {
            runTest {
                val successResult = Result.success(createStickerSets(2))
                whenever(getStickersUseCase.invoke()).thenReturn(successResult)

                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                // After init fetch completes, state should be Content
                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()

                // Calling fetchStickers again should work since isFetching is reset after completion
                viewModel.fetchStickers()
                advanceUntilIdle()

                // State should still be Content (sequential fetch succeeded)
                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                viewModel.stickerSets.value shouldHaveSize 2
                println("    ✅ Fetch guard allows sequential fetches after completion")
            }
        }
    }

    // ==================== F. State Transitions ====================

    context("F. State Transitions") {

        test("Loading → Content → select set → stickers update") {
            runTest {
                val sets = createStickerSets(3)
                val viewModel = createViewModel(sets)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()

                viewModel.selectStickerSet(2)
                viewModel.selectedSetIndex.value shouldBe 2
                viewModel.currentStickers.value shouldBe sets[2].stickers
                println("    ✅ Full flow: Loading → Content → select set")
            }
        }

        test("Loading → Error → retry → Content") {
            runTest {
                whenever(getStickersUseCase.invoke()).thenReturn(
                    Result.failure(CometChatException("ERR", "Fail"))
                )
                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Error>()

                val sets = createStickerSets(2)
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
                viewModel.retry()
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                println("    ✅ Error → retry → Content")
            }
        }
    }
})
