package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Tests for CreatePollUIState sealed class.
 * Pure data class tests — no mocking needed.
 *
 * Verifies:
 * - Singleton instances for Idle, Submitting, Success
 * - Error holds exception correctly
 * - Type checking works as expected
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.CreatePollUIStateTest"
 */
class CreatePollUIStateTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    context("Idle") {
        test("Idle is a singleton instance") {
            println("    → Verifying Idle singleton")
            val state1: CreatePollUIState = CreatePollUIState.Idle
            val state2: CreatePollUIState = CreatePollUIState.Idle
            state1.shouldBeInstanceOf<CreatePollUIState.Idle>()
            state1 shouldBeSameInstanceAs state2
            println("    ✓ Idle is singleton")
        }

        test("Idle is a CreatePollUIState subtype") {
            println("    → Verifying Idle type hierarchy")
            val state: CreatePollUIState = CreatePollUIState.Idle
            state.shouldBeInstanceOf<CreatePollUIState>()
            println("    ✓ Idle is CreatePollUIState")
        }
    }

    context("Submitting") {
        test("Submitting is a singleton instance") {
            println("    → Verifying Submitting singleton")
            val state1: CreatePollUIState = CreatePollUIState.Submitting
            val state2: CreatePollUIState = CreatePollUIState.Submitting
            state1.shouldBeInstanceOf<CreatePollUIState.Submitting>()
            state1 shouldBeSameInstanceAs state2
            println("    ✓ Submitting is singleton")
        }

        test("Submitting is distinct from Idle") {
            println("    → Verifying Submitting != Idle")
            val idle: CreatePollUIState = CreatePollUIState.Idle
            val submitting: CreatePollUIState = CreatePollUIState.Submitting
            (idle === submitting) shouldBe false
            println("    ✓ Submitting is distinct from Idle")
        }
    }

    context("Success") {
        test("Success is a singleton instance") {
            println("    → Verifying Success singleton")
            val state1: CreatePollUIState = CreatePollUIState.Success
            val state2: CreatePollUIState = CreatePollUIState.Success
            state1.shouldBeInstanceOf<CreatePollUIState.Success>()
            state1 shouldBeSameInstanceAs state2
            println("    ✓ Success is singleton")
        }

        test("Success is distinct from Idle and Submitting") {
            println("    → Verifying Success distinctness")
            val success: CreatePollUIState = CreatePollUIState.Success
            (success === CreatePollUIState.Idle) shouldBe false
            (success === CreatePollUIState.Submitting) shouldBe false
            println("    ✓ Success is distinct")
        }
    }

    context("Error") {
        test("Error holds the exception") {
            println("    → Creating Error with exception")
            val exception = CometChatException("ERR_POLL", "Poll creation failed")
            val state = CreatePollUIState.Error(exception)
            state.shouldBeInstanceOf<CreatePollUIState.Error>()
            state.exception shouldBe exception
            state.exception.code shouldBe "ERR_POLL"
            println("    ✓ Error holds exception with correct code")
        }

        test("Error data class equality works correctly") {
            println("    → Verifying Error equality")
            val exception1 = CometChatException("ERR_1", "Error 1")
            val exception2 = CometChatException("ERR_2", "Error 2")
            val state1 = CreatePollUIState.Error(exception1)
            val state2 = CreatePollUIState.Error(exception1)
            val state3 = CreatePollUIState.Error(exception2)

            state1 shouldBe state2
            (state1 == state3) shouldBe false
            println("    ✓ Error equality based on exception reference")
        }

        test("Error is distinct from other states") {
            println("    → Verifying Error type discrimination")
            val exception = CometChatException("ERR", "fail")
            val error: CreatePollUIState = CreatePollUIState.Error(exception)
            (error is CreatePollUIState.Idle) shouldBe false
            (error is CreatePollUIState.Submitting) shouldBe false
            (error is CreatePollUIState.Success) shouldBe false
            (error is CreatePollUIState.Error) shouldBe true
            println("    ✓ Error is distinct from Idle, Submitting, Success")
        }
    }

    context("when expression exhaustiveness") {
        test("all states can be matched in when expression") {
            println("    → Verifying when expression covers all states")
            val states = listOf(
                CreatePollUIState.Idle,
                CreatePollUIState.Submitting,
                CreatePollUIState.Success,
                CreatePollUIState.Error(CometChatException("ERR", "test"))
            )

            states.forEach { state ->
                val label = when (state) {
                    is CreatePollUIState.Idle -> "idle"
                    is CreatePollUIState.Submitting -> "submitting"
                    is CreatePollUIState.Success -> "success"
                    is CreatePollUIState.Error -> "error"
                }
                label.isNotEmpty() shouldBe true
            }
            println("    ✓ all 4 states matched exhaustively")
        }
    }
})
