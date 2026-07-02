package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.repository.StickerRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for GetStickersUseCase.
 *
 * Layer 3 — thin delegation layer. Mocks Repository, verifies forwarding.
 *
 * Verifies:
 * - invoke() delegates to repository.getStickers()
 * - Success result is passed through unchanged
 * - Failure result is propagated unchanged
 * - Empty result is passed through
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.StickerUseCasesTest"
 */
class StickerUseCasesTest : FunSpec({

    lateinit var repository: StickerRepository
    lateinit var useCase: GetStickersUseCase

    beforeTest {
        repository = mock()
        useCase = GetStickersUseCase(repository)
        println("  🧪 ${it.name.testName}")
    }

    context("GetStickersUseCase") {

        test("invoke delegates to repository.getStickers and returns success") {
            runTest {
                val sets = listOf(
                    StickerSet(
                        name = "Emotions",
                        stickers = listOf(
                            Sticker("happy", "https://example.com/happy.png", "Emotions"),
                            Sticker("sad", "https://example.com/sad.png", "Emotions")
                        ),
                        iconUrl = "https://example.com/happy.png"
                    ),
                    StickerSet(
                        name = "Animals",
                        stickers = listOf(
                            Sticker("cat", "https://example.com/cat.gif", "Animals")
                        ),
                        iconUrl = "https://example.com/cat.gif"
                    )
                )
                whenever(repository.getStickers()).thenReturn(Result.success(sets))

                val result = useCase()

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe sets
                result.getOrNull()!!.size shouldBe 2
                verify(repository).getStickers()
                println("    ✅ Delegates to repository and returns success")
            }
        }

        test("invoke propagates Result.failure unchanged") {
            runTest {
                val exception = CometChatException("ERR_NETWORK", "Connection failed")
                whenever(repository.getStickers()).thenReturn(Result.failure(exception))

                val result = useCase()

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
                (result.exceptionOrNull() as CometChatException).code shouldBe "ERR_NETWORK"
                verify(repository).getStickers()
                println("    ✅ Propagates failure unchanged")
            }
        }

        test("invoke returns empty list when repository returns empty") {
            runTest {
                whenever(repository.getStickers()).thenReturn(Result.success(emptyList()))

                val result = useCase()

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe emptyList()
                verify(repository).getStickers()
                println("    ✅ Returns empty list from repository")
            }
        }

        test("invoke propagates extension not enabled error") {
            runTest {
                val exception = CometChatException(
                    "ERR_EXTENSION_NOT_ENABLED",
                    "Enable the stickers extension from CometChat Pro dashboard"
                )
                whenever(repository.getStickers()).thenReturn(Result.failure(exception))

                val result = useCase()

                result.isFailure shouldBe true
                (result.exceptionOrNull() as CometChatException).code shouldBe "ERR_EXTENSION_NOT_ENABLED"
                println("    ✅ Propagates extension not enabled error")
            }
        }

        test("invoke can be called multiple times") {
            runTest {
                val sets = listOf(
                    StickerSet("Set-1", listOf(Sticker("s1", "url1", "Set-1")), "url1")
                )
                whenever(repository.getStickers()).thenReturn(Result.success(sets))

                val result1 = useCase()
                val result2 = useCase()

                result1.isSuccess shouldBe true
                result2.isSuccess shouldBe true
                result1.getOrNull() shouldBe result2.getOrNull()
                println("    ✅ Multiple invocations work correctly")
            }
        }
    }
})
