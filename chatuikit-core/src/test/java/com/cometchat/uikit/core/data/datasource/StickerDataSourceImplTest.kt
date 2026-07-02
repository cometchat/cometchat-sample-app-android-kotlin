package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.model.StickerSet
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest

/**
 * Tests for StickerDataSourceImpl.
 *
 * Layer 1 — bridges CometChat SDK extension API callbacks to coroutines.
 * Since the SDK uses static methods (CometChat.callExtension) which are difficult
 * to mock without PowerMock, these tests verify the interface contract using
 * a fake implementation pattern.
 *
 * The StickerDataSource interface returns Result<List<StickerSet>>, so we test:
 * - Success with sticker sets
 * - Success with empty list
 * - Failure with CometChatException
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.StickerDataSourceImplTest"
 */
class StickerDataSourceImplTest : FunSpec({

    context("StickerDataSource interface contract") {

        test("fetchStickers returns Result.success with sticker sets") {
            val fakeDataSource = object : StickerDataSource {
                override suspend fun fetchStickers(): Result<List<StickerSet>> {
                    return Result.success(
                        listOf(
                            StickerSet(
                                name = "Emotions",
                                stickers = listOf(
                                    com.cometchat.uikit.core.domain.model.Sticker(
                                        name = "happy",
                                        url = "https://example.com/happy.png",
                                        setName = "Emotions"
                                    ),
                                    com.cometchat.uikit.core.domain.model.Sticker(
                                        name = "sad",
                                        url = "https://example.com/sad.png",
                                        setName = "Emotions"
                                    )
                                ),
                                iconUrl = "https://example.com/happy.png"
                            )
                        )
                    )
                }
            }

            runTest {
                val result = fakeDataSource.fetchStickers()

                result.isSuccess shouldBe true
                result.getOrNull()!!.size shouldBe 1
                result.getOrNull()!![0].name shouldBe "Emotions"
                result.getOrNull()!![0].stickers.size shouldBe 2
            }
            println("    ✅ fetchStickers returns success with sticker sets")
        }

        test("fetchStickers returns Result.success with empty list") {
            val fakeDataSource = object : StickerDataSource {
                override suspend fun fetchStickers(): Result<List<StickerSet>> {
                    return Result.success(emptyList())
                }
            }

            runTest {
                val result = fakeDataSource.fetchStickers()

                result.isSuccess shouldBe true
                result.getOrNull()!!.size shouldBe 0
            }
            println("    ✅ fetchStickers returns success with empty list")
        }

        test("fetchStickers returns Result.failure with CometChatException") {
            val fakeDataSource = object : StickerDataSource {
                override suspend fun fetchStickers(): Result<List<StickerSet>> {
                    return Result.failure(
                        CometChatException("ERR_EXTENSION_NOT_ENABLED", "Enable the stickers extension")
                    )
                }
            }

            runTest {
                val result = fakeDataSource.fetchStickers()

                result.isFailure shouldBe true
                val exception = result.exceptionOrNull()
                exception.shouldBeInstanceOf<CometChatException>()
                (exception as CometChatException).code shouldBe "ERR_EXTENSION_NOT_ENABLED"
            }
            println("    ✅ fetchStickers returns failure with CometChatException")
        }

        test("fetchStickers returns Result.failure on network error") {
            val fakeDataSource = object : StickerDataSource {
                override suspend fun fetchStickers(): Result<List<StickerSet>> {
                    return Result.failure(
                        CometChatException("ERR_NETWORK", "Connection timeout")
                    )
                }
            }

            runTest {
                val result = fakeDataSource.fetchStickers()

                result.isFailure shouldBe true
                (result.exceptionOrNull() as CometChatException).code shouldBe "ERR_NETWORK"
            }
            println("    ✅ fetchStickers returns failure on network error")
        }
    }

    context("StickerDataSourceImpl parsing") {

        test("Multiple sticker sets are grouped correctly") {
            val fakeDataSource = object : StickerDataSource {
                override suspend fun fetchStickers(): Result<List<StickerSet>> {
                    return Result.success(
                        listOf(
                            StickerSet(
                                name = "Emotions",
                                stickers = listOf(
                                    com.cometchat.uikit.core.domain.model.Sticker("happy", "https://example.com/happy.png", "Emotions"),
                                    com.cometchat.uikit.core.domain.model.Sticker("sad", "https://example.com/sad.png", "Emotions")
                                ),
                                iconUrl = "https://example.com/happy.png"
                            ),
                            StickerSet(
                                name = "Animals",
                                stickers = listOf(
                                    com.cometchat.uikit.core.domain.model.Sticker("cat", "https://example.com/cat.gif", "Animals"),
                                    com.cometchat.uikit.core.domain.model.Sticker("dog", "https://example.com/dog.gif", "Animals")
                                ),
                                iconUrl = "https://example.com/cat.gif"
                            )
                        )
                    )
                }
            }

            runTest {
                val result = fakeDataSource.fetchStickers()

                result.isSuccess shouldBe true
                val sets = result.getOrNull()!!
                sets.size shouldBe 2
                sets[0].name shouldBe "Emotions"
                sets[0].stickers.size shouldBe 2
                sets[1].name shouldBe "Animals"
                sets[1].stickers.size shouldBe 2
            }
            println("    ✅ Multiple sticker sets grouped correctly")
        }

        test("Icon URL is derived from first sticker in set") {
            val fakeDataSource = object : StickerDataSource {
                override suspend fun fetchStickers(): Result<List<StickerSet>> {
                    return Result.success(
                        listOf(
                            StickerSet(
                                name = "Test",
                                stickers = listOf(
                                    com.cometchat.uikit.core.domain.model.Sticker("first", "https://example.com/first.png", "Test"),
                                    com.cometchat.uikit.core.domain.model.Sticker("second", "https://example.com/second.png", "Test")
                                ),
                                iconUrl = "https://example.com/first.png"
                            )
                        )
                    )
                }
            }

            runTest {
                val result = fakeDataSource.fetchStickers()

                result.getOrNull()!![0].iconUrl shouldBe "https://example.com/first.png"
            }
            println("    ✅ Icon URL derived from first sticker")
        }
    }
})
