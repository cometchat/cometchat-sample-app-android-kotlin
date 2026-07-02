package com.cometchat.uikit.kotlin.presentation.stickerkeyboard

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.usecase.GetStickersUseCase
import com.cometchat.uikit.core.state.StickerKeyboardUIState
import com.cometchat.uikit.core.viewmodel.CometChatStickerKeyboardViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
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
 * Interaction tests for CometChatStickerKeyboard (Kotlin View).
 *
 * Verifies user interactions map to correct ViewModel state changes:
 * - Sticker click invokes callback
 * - Tab selection changes page (selectStickerSet)
 * - Page swipe updates tab (selectStickerSet from ViewPager2 callback)
 * - Retry button triggers retry
 * - Style application works (style is a data class)
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.CometChatStickerKeyboardInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatStickerKeyboardInteractionTest : FunSpec({

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
        return (1..count).map { i -> createStickerSet("Set-$i", stickerCount = 4) }
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        getStickersUseCase = mock()
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
    }

    // ==================== Sticker Click ====================

    context("Sticker click interaction") {

        test("Clicking a sticker invokes the onStickerClick callback") {
            runTest {
                val sets = createStickerSets(2)
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                var clickedSticker: Sticker? = null
                viewModel.onStickerClick = { sticker -> clickedSticker = sticker }

                val targetSticker = sets[1].stickers[2]
                viewModel.onStickerClicked(targetSticker)

                clickedSticker shouldBe targetSticker
                clickedSticker?.name shouldBe "Set-2-sticker-3"
                println("    ✅ Sticker click invokes callback with correct sticker")
            }
        }

        test("Clicking sticker from different sets works correctly") {
            runTest {
                val sets = createStickerSets(3)
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                val clickedStickers = mutableListOf<Sticker>()
                viewModel.onStickerClick = { sticker -> clickedStickers.add(sticker) }

                viewModel.onStickerClicked(sets[0].stickers[0])
                viewModel.onStickerClicked(sets[1].stickers[1])
                viewModel.onStickerClicked(sets[2].stickers[2])

                clickedStickers.size shouldBe 3
                clickedStickers[0].setName shouldBe "Set-1"
                clickedStickers[1].setName shouldBe "Set-2"
                clickedStickers[2].setName shouldBe "Set-3"
                println("    ✅ Multiple sticker clicks from different sets work")
            }
        }
    }

    // ==================== Tab Selection ====================

    context("Tab selection interaction") {

        test("Selecting a tab updates the selected set index") {
            runTest {
                val sets = createStickerSets(4)
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                viewModel.selectedSetIndex.value shouldBe 0

                viewModel.selectStickerSet(2)
                viewModel.selectedSetIndex.value shouldBe 2
                viewModel.currentStickers.value shouldBe sets[2].stickers
                println("    ✅ Tab selection updates index and stickers")
            }
        }

        test("Tab selection syncs with page content") {
            runTest {
                val sets = createStickerSets(3)
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                // Simulate tab click → page should change
                viewModel.selectStickerSet(1)
                viewModel.currentStickers.value shouldBe sets[1].stickers

                viewModel.selectStickerSet(2)
                viewModel.currentStickers.value shouldBe sets[2].stickers

                viewModel.selectStickerSet(0)
                viewModel.currentStickers.value shouldBe sets[0].stickers
                println("    ✅ Tab selection syncs with page content")
            }
        }

        test("PBT: Tab-pager sync for any valid index") {
            checkAll(30, Arb.int(2..8)) { setCount ->
                runTest {
                    val sets = createStickerSets(setCount)
                    whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
                    val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                    advanceUntilIdle()

                    val targetIndex = (0 until setCount).random()
                    viewModel.selectStickerSet(targetIndex)

                    viewModel.selectedSetIndex.value shouldBe targetIndex
                    viewModel.currentStickers.value shouldBe sets[targetIndex].stickers
                }
            }
        }
    }

    // ==================== Page Swipe ====================

    context("Page swipe interaction") {

        test("Page swipe updates tab selection (simulated via selectStickerSet)") {
            runTest {
                val sets = createStickerSets(3)
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                // ViewPager2 onPageSelected calls selectStickerSet
                viewModel.selectStickerSet(1)
                viewModel.selectedSetIndex.value shouldBe 1

                viewModel.selectStickerSet(2)
                viewModel.selectedSetIndex.value shouldBe 2
                println("    ✅ Page swipe (via selectStickerSet) updates tab")
            }
        }
    }

    // ==================== Retry Button ====================

    context("Retry button interaction") {

        test("Retry button triggers fetchStickers and transitions to Content") {
            runTest {
                whenever(getStickersUseCase.invoke()).thenReturn(
                    Result.failure(CometChatException("ERR", "Network error"))
                )
                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Error>()

                // Configure success for retry
                val sets = createStickerSets(2)
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
                viewModel.retry()
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                println("    ✅ Retry button triggers fetch and transitions to Content")
            }
        }

        test("Retry after multiple failures eventually succeeds") {
            runTest {
                whenever(getStickersUseCase.invoke()).thenReturn(
                    Result.failure(CometChatException("ERR", "Fail"))
                )
                val viewModel = CometChatStickerKeyboardViewModel(getStickersUseCase)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Error>()

                // Second failure
                viewModel.retry()
                advanceUntilIdle()
                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Error>()

                // Third attempt succeeds
                val sets = createStickerSets(1)
                whenever(getStickersUseCase.invoke()).thenReturn(Result.success(sets))
                viewModel.retry()
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                println("    ✅ Multiple retries eventually succeed")
            }
        }
    }

    // ==================== Style Application ====================

    context("Style application") {

        test("Style is a data class with copy support") {
            val style = com.cometchat.uikit.kotlin.presentation.stickerkeyboard.style.CometChatStickerKeyboardStyle()
            val modified = style.copy(backgroundColor = android.graphics.Color.RED)

            modified.backgroundColor shouldBe android.graphics.Color.RED
            style.backgroundColor shouldBe 0
            println("    ✅ Style copy creates independent instance")
        }

        test("Style properties are independently modifiable") {
            val style = com.cometchat.uikit.kotlin.presentation.stickerkeyboard.style.CometChatStickerKeyboardStyle(
                backgroundColor = android.graphics.Color.WHITE,
                separatorColor = android.graphics.Color.GRAY,
                tabActiveIndicatorColor = android.graphics.Color.BLUE
            )

            style.backgroundColor shouldBe android.graphics.Color.WHITE
            style.separatorColor shouldBe android.graphics.Color.GRAY
            style.tabActiveIndicatorColor shouldBe android.graphics.Color.BLUE
            println("    ✅ Style properties are independently accessible")
        }
    }
})
