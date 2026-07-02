package com.cometchat.uikit.core.viewmodel.stickerkeyboard

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.data.datasource.StickerDataSource
import com.cometchat.uikit.core.data.repository.StickerRepositoryImpl
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.usecase.GetStickersUseCase
import com.cometchat.uikit.core.state.StickerKeyboardUIState
import com.cometchat.uikit.core.viewmodel.CometChatStickerKeyboardViewModel
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
 * Full-chain integration test for the StickerKeyboard feature.
 *
 * Tests the complete chain: fake DataSource → real Repository → real UseCase → real ViewModel.
 * Only the DataSource is faked — everything else is real.
 *
 * This verifies that all layers integrate correctly without mocking intermediate layers.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.StickerKeyboardFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StickerKeyboardFullChainIntegrationTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
    }

    // ==================== Helper Methods ====================

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

    /**
     * Builds the full chain: DataSource → Repository → UseCase → ViewModel.
     * Only the DataSource is faked.
     */
    fun buildViewModel(dataSource: StickerDataSource): CometChatStickerKeyboardViewModel {
        val repository = StickerRepositoryImpl(dataSource)
        val getStickersUseCase = GetStickersUseCase(repository)
        return CometChatStickerKeyboardViewModel(getStickersUseCase)
    }

    // ==================== Integration Tests ====================

    context("Full chain: DataSource → Repository → UseCase → ViewModel") {

        test("DataSource returning sticker sets → ViewModel shows Content state") {
            runTest {
                val fakeSets = createStickerSets(3)
                val dataSource = object : StickerDataSource {
                    override suspend fun fetchStickers(): Result<List<StickerSet>> =
                        Result.success(fakeSets)
                }

                val viewModel = buildViewModel(dataSource)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                viewModel.stickerSets.value shouldHaveSize 3
                viewModel.stickerSets.value[0].name shouldBe "Set-1"
                viewModel.stickerSets.value[1].name shouldBe "Set-2"
                viewModel.stickerSets.value[2].name shouldBe "Set-3"
                viewModel.selectedSetIndex.value shouldBe 0
                viewModel.currentStickers.value shouldBe fakeSets[0].stickers
                println("    ✅ Full chain: 3 sets → Content state with correct data")
            }
        }

        test("DataSource returning empty → ViewModel shows Empty state") {
            runTest {
                val dataSource = object : StickerDataSource {
                    override suspend fun fetchStickers(): Result<List<StickerSet>> =
                        Result.success(emptyList())
                }

                val viewModel = buildViewModel(dataSource)
                advanceUntilIdle()

                viewModel.uiState.value shouldBe StickerKeyboardUIState.Empty
                viewModel.stickerSets.value shouldHaveSize 0
                viewModel.currentStickers.value shouldHaveSize 0
                println("    ✅ Full chain: empty → Empty state")
            }
        }

        test("DataSource returning failure → ViewModel shows Error state") {
            runTest {
                val dataSource = object : StickerDataSource {
                    override suspend fun fetchStickers(): Result<List<StickerSet>> =
                        Result.failure(CometChatException("ERR_NETWORK", "Connection failed"))
                }

                val viewModel = buildViewModel(dataSource)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                state.shouldBeInstanceOf<StickerKeyboardUIState.Error>()
                (state as StickerKeyboardUIState.Error).exception.code shouldBe "ERR_NETWORK"
                println("    ✅ Full chain: failure → Error state with correct exception")
            }
        }

        test("Full chain: Content → selectStickerSet → stickers update") {
            runTest {
                val fakeSets = createStickerSets(4)
                val dataSource = object : StickerDataSource {
                    override suspend fun fetchStickers(): Result<List<StickerSet>> =
                        Result.success(fakeSets)
                }

                val viewModel = buildViewModel(dataSource)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()

                // Select different sets
                viewModel.selectStickerSet(2)
                viewModel.selectedSetIndex.value shouldBe 2
                viewModel.currentStickers.value shouldBe fakeSets[2].stickers

                viewModel.selectStickerSet(3)
                viewModel.selectedSetIndex.value shouldBe 3
                viewModel.currentStickers.value shouldBe fakeSets[3].stickers
                println("    ✅ Full chain: Content → selectStickerSet works end-to-end")
            }
        }

        test("Full chain: Error → retry → Content") {
            runTest {
                var callCount = 0
                val fakeSets = createStickerSets(2)
                val dataSource = object : StickerDataSource {
                    override suspend fun fetchStickers(): Result<List<StickerSet>> {
                        callCount++
                        return if (callCount == 1) {
                            Result.failure(CometChatException("ERR", "First call fails"))
                        } else {
                            Result.success(fakeSets)
                        }
                    }
                }

                val viewModel = buildViewModel(dataSource)
                advanceUntilIdle()

                // First call fails
                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Error>()

                // Retry succeeds
                viewModel.retry()
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                viewModel.stickerSets.value shouldHaveSize 2
                println("    ✅ Full chain: Error → retry → Content")
            }
        }

        test("Full chain: sticker click callback works end-to-end") {
            runTest {
                val fakeSets = createStickerSets(2)
                val dataSource = object : StickerDataSource {
                    override suspend fun fetchStickers(): Result<List<StickerSet>> =
                        Result.success(fakeSets)
                }

                val viewModel = buildViewModel(dataSource)
                advanceUntilIdle()

                var clickedSticker: Sticker? = null
                viewModel.onStickerClick = { sticker -> clickedSticker = sticker }

                val targetSticker = fakeSets[1].stickers[2]
                viewModel.onStickerClicked(targetSticker)

                clickedSticker shouldBe targetSticker
                clickedSticker?.name shouldBe "Set-2-sticker-3"
                clickedSticker?.setName shouldBe "Set-2"
                println("    ✅ Full chain: sticker click callback works end-to-end")
            }
        }

        test("Full chain: large number of sticker sets") {
            runTest {
                val fakeSets = createStickerSets(20)
                val dataSource = object : StickerDataSource {
                    override suspend fun fetchStickers(): Result<List<StickerSet>> =
                        Result.success(fakeSets)
                }

                val viewModel = buildViewModel(dataSource)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()
                viewModel.stickerSets.value shouldHaveSize 20

                // Navigate to last set
                viewModel.selectStickerSet(19)
                viewModel.selectedSetIndex.value shouldBe 19
                viewModel.currentStickers.value shouldBe fakeSets[19].stickers
                println("    ✅ Full chain: 20 sticker sets handled correctly")
            }
        }

        test("Full chain: sticker sets with varying sticker counts") {
            runTest {
                val sets = listOf(
                    createStickerSet("Small", 2),
                    createStickerSet("Medium", 8),
                    createStickerSet("Large", 20)
                )
                val dataSource = object : StickerDataSource {
                    override suspend fun fetchStickers(): Result<List<StickerSet>> =
                        Result.success(sets)
                }

                val viewModel = buildViewModel(dataSource)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<StickerKeyboardUIState.Content>()

                viewModel.selectStickerSet(0)
                viewModel.currentStickers.value shouldHaveSize 2

                viewModel.selectStickerSet(1)
                viewModel.currentStickers.value shouldHaveSize 8

                viewModel.selectStickerSet(2)
                viewModel.currentStickers.value shouldHaveSize 20
                println("    ✅ Full chain: varying sticker counts per set")
            }
        }
    }
})
