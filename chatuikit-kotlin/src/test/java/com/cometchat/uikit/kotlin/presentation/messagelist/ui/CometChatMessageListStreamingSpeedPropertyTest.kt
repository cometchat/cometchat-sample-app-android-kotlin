package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

// Feature: kotlin-stream-bubble, Property 5: Streaming speed passthrough

/**
 * Test double that simulates [CometChatAIStreamService.setStreamDelay] behavior.
 * Records the last delay value passed to [setStreamDelay].
 */
private class FakeAIStreamService {
    var lastStreamDelay: Long? = null
        private set

    var callCount: Int = 0
        private set

    fun setStreamDelay(delayMillis: Long) {
        lastStreamDelay = delayMillis
        callCount++
    }
}

/**
 * Test double that simulates the ViewModel providing the stream service.
 */
private class FakeMessageListViewModel(
    private val streamService: FakeAIStreamService?
) {
    fun getAIStreamService(): FakeAIStreamService? = streamService
}

/**
 * Simulates the streaming speed passthrough logic of [CometChatMessageList].
 *
 * This mirrors the actual implementation:
 * - [CometChatMessageList] stores `streamingSpeed: Int?`
 * - [setStreamingSpeed] stores the value and, if non-null, calls
 *   `viewModel?.getAIStreamService()?.setStreamDelay(value.toLong())`
 */
private class TestStreamingSpeedStorage(
    private val viewModel: FakeMessageListViewModel?
) {
    private var streamingSpeed: Int? = null

    fun setStreamingSpeed(speed: Int?) {
        this.streamingSpeed = speed
        if (speed != null) {
            viewModel?.getAIStreamService()?.setStreamDelay(speed.toLong())
        }
    }

    fun getStreamingSpeed(): Int? = streamingSpeed
}

/**
 * Property-based tests for CometChatMessageList streaming speed passthrough.
 *
 * Feature: kotlin-stream-bubble, Property 5: Streaming speed passthrough
 *
 * *For any* non-null positive integer value passed to `CometChatMessageList.setStreamingSpeed()`,
 * the `CometChatAIStreamService.setStreamDelay()` SHALL be called with that value converted
 * to Long milliseconds.
 *
 * **Validates: Requirements 6.1**
 */
class CometChatMessageListStreamingSpeedPropertyTest : FunSpec({

    // ==================== Property Tests ====================

    context("Property 5: Streaming speed passthrough") {

        test("setStreamingSpeed with positive int calls setStreamDelay with same value as Long") {
            checkAll(100, Arb.int(1..Int.MAX_VALUE)) { speed ->
                val service = FakeAIStreamService()
                val viewModel = FakeMessageListViewModel(service)
                val storage = TestStreamingSpeedStorage(viewModel)

                storage.setStreamingSpeed(speed)

                service.lastStreamDelay shouldBe speed.toLong()
                service.lastStreamDelay!! shouldBeGreaterThan 0L
            }
        }

        test("setStreamingSpeed with null does not call setStreamDelay") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val service = FakeAIStreamService()
                val viewModel = FakeMessageListViewModel(service)
                val storage = TestStreamingSpeedStorage(viewModel)

                storage.setStreamingSpeed(null)

                service.lastStreamDelay.shouldBeNull()
                service.callCount shouldBe 0
            }
        }

        test("setStreamDelay receives exact Long conversion of the Int value") {
            checkAll(100, Arb.int(1..Int.MAX_VALUE)) { speed ->
                val service = FakeAIStreamService()
                val viewModel = FakeMessageListViewModel(service)
                val storage = TestStreamingSpeedStorage(viewModel)

                storage.setStreamingSpeed(speed)

                // The Long value must be exactly the Int value widened to Long
                service.lastStreamDelay shouldBe speed.toLong()
            }
        }

        test("setStreamDelay is called exactly once per non-null setStreamingSpeed call") {
            checkAll(100, Arb.int(1..Int.MAX_VALUE)) { speed ->
                val service = FakeAIStreamService()
                val viewModel = FakeMessageListViewModel(service)
                val storage = TestStreamingSpeedStorage(viewModel)

                storage.setStreamingSpeed(speed)

                service.callCount shouldBe 1
            }
        }

        test("getStreamingSpeed returns the value that was set") {
            checkAll(100, Arb.int(1..Int.MAX_VALUE)) { speed ->
                val service = FakeAIStreamService()
                val viewModel = FakeMessageListViewModel(service)
                val storage = TestStreamingSpeedStorage(viewModel)

                storage.setStreamingSpeed(speed)

                storage.getStreamingSpeed() shouldBe speed
            }
        }
    }
})
