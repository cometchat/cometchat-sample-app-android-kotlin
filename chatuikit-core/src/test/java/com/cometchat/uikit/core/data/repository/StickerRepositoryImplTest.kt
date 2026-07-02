package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.data.datasource.StickerDataSource
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for StickerRepositoryImpl.
 *
 * Layer 2 — wraps DataSource calls and delegates to the data source.
 * Mocks the DataSource interface — no SDK involved.
 *
 * Verifies:
 * - Delegates to dataSource.fetchStickers()
 * - Returns Result.success when dataSource succeeds
 * - Returns Result.failure when dataSource fails
 * - Passes through empty results correctly
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.StickerRepositoryImplTest"
 */
class StickerRepositoryImplTest : FunSpec({

    lateinit var dataSource: StickerDataSource
    lateinit var repository: StickerRepositoryImpl

    beforeTest {
        dataSource = mock()
        repository = StickerRepositoryImpl(dataSource)
        println("  🧪 ${it.name.testName}")
    }

    context("getStickers delegation") {

        test("getStickers delegates to dataSource.fetchStickers and returns success") {
            runTest {
                val sets = listOf(
                    StickerSet(
                        name = "Emotions",
                        stickers = listOf(
                            Sticker("happy", "https://example.com/happy.png", "Emotions")
                        ),
                        iconUrl = "https://example.com/happy.png"
                    )
                )
                whenever(dataSource.fetchStickers()).thenReturn(Result.success(sets))

                val result = repository.getStickers()

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe sets
                verify(dataSource).fetchStickers()
                println("    ✅ Delegates to dataSource and returns success")
            }
        }

        test("getStickers returns Result.failure when dataSource fails") {
            runTest {
                val exception = CometChatException("ERR_FETCH", "Network error")
                whenever(dataSource.fetchStickers()).thenReturn(Result.failure(exception))

                val result = repository.getStickers()

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
                (result.exceptionOrNull() as CometChatException).code shouldBe "ERR_FETCH"
                verify(dataSource).fetchStickers()
                println("    ✅ Returns failure when dataSource fails")
            }
        }

        test("getStickers returns empty list when dataSource returns empty") {
            runTest {
                whenever(dataSource.fetchStickers()).thenReturn(Result.success(emptyList()))

                val result = repository.getStickers()

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe emptyList()
                println("    ✅ Returns empty list when dataSource returns empty")
            }
        }

        test("getStickers passes through multiple sticker sets") {
            runTest {
                val sets = listOf(
                    StickerSet("Set-1", listOf(Sticker("s1", "url1", "Set-1")), "url1"),
                    StickerSet("Set-2", listOf(Sticker("s2", "url2", "Set-2")), "url2"),
                    StickerSet("Set-3", listOf(Sticker("s3", "url3", "Set-3")), "url3")
                )
                whenever(dataSource.fetchStickers()).thenReturn(Result.success(sets))

                val result = repository.getStickers()

                result.isSuccess shouldBe true
                result.getOrNull()!!.size shouldBe 3
                result.getOrNull()!![0].name shouldBe "Set-1"
                result.getOrNull()!![1].name shouldBe "Set-2"
                result.getOrNull()!![2].name shouldBe "Set-3"
                println("    ✅ Passes through multiple sticker sets")
            }
        }

        test("getStickers propagates extension not enabled error") {
            runTest {
                val exception = CometChatException(
                    "ERR_EXTENSION_NOT_ENABLED",
                    "Enable the stickers extension from CometChat Pro dashboard"
                )
                whenever(dataSource.fetchStickers()).thenReturn(Result.failure(exception))

                val result = repository.getStickers()

                result.isFailure shouldBe true
                (result.exceptionOrNull() as CometChatException).code shouldBe "ERR_EXTENSION_NOT_ENABLED"
                println("    ✅ Propagates extension not enabled error")
            }
        }
    }
})
