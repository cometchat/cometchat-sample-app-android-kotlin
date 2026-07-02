package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.chat.models.AIAssistantBaseEvent
import com.cometchat.chat.models.AIAssistantContentReceivedEvent
import com.cometchat.chat.models.AIAssistantMessage
import com.cometchat.uikit.core.CometChatAIStreamService
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.QueueCompletionResult
import com.cometchat.uikit.core.domain.model.StreamMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.element
import io.kotest.property.forAll
import io.kotest.common.ExperimentalKotest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Feature: agentic-workflow-improvements
 *
 * Property 19: StreamMessage Lifecycle (example-based)
 * Property 20: Stream Interruption Marking (example-based)
 * Property 21: Parent Message ID Lifecycle (property-based)
 *
 * Validates: Requirements 14.1, 14.2, 14.3, 14.4, 11.1, 11.2, 11.3
 */
@OptIn(ExperimentalKotest::class, ExperimentalCoroutinesApi::class)
class StreamMessageLifecycleTest : StringSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Thread.sleep(50); Dispatchers.resetMain() }

    // ── Property 19: StreamMessage Lifecycle ─────────────────────────────
    // Validates: Requirements 14.1, 14.2, 14.3

    "Property 19: StreamMessage is created on RUN_STARTED, updated with text deltas, and replaced on completion" {
        val testScope = TestScope()
        val service = CometChatAIStreamService(testScope)
        service.setStreamDelay(0)

        val runId = 1001L
        val messageList = mutableListOf<Any>()

        // 1. Enqueue a RUN_STARTED event
        val runStarted = AIAssistantBaseEvent().apply {
            id = runId
            type = UIKitConstants.AIAssistantEventType.RUN_STARTED
        }
        service.handleIncomingEvent(runStarted)

        // 2. Create a StreamMessage and add to the test message list (simulating ViewModel behavior)
        val streamMessage = StreamMessage(
            receiverUid = "agent_bot_1",
            receiverType = UIKitConstants.ReceiverType.USER,
            text = ""
        )
        messageList.add(streamMessage)

        // 3. Register a listener that updates the StreamMessage text from accumulated content
        service.startStreamingForRunId(runId, CometChatAIStreamService.AIStreamListener { event ->
            val accumulated = service.accumulatedText(runId).value
            if (streamMessage.text != accumulated) {
                streamMessage.text = accumulated
            }
        })

        // 4. Enqueue content events with text deltas
        val deltas = listOf("Hello", " ", "World", "!")
        deltas.forEach { delta ->
            val contentEvent = AIAssistantContentReceivedEvent().apply {
                id = runId
                this.delta = delta
            }
            service.handleIncomingEvent(contentEvent)
        }

        // 5. Process the queue
        service.processQueueSequentially(runId)
        testScope.testScheduler.advanceUntilIdle()

        // 6. Verify the StreamMessage text matches accumulated content
        streamMessage.text shouldBe "Hello World!"

        // 7. Simulate completion: store a final AIAssistantMessage, set completion callback, trigger completion
        val finalMessage = AIAssistantMessage("agent_bot_1", UIKitConstants.ReceiverType.USER, "Hello World!")
        var completionInvoked = false
        var completionResult: QueueCompletionResult? = null

        service.setQueueCompletionCallback(runId, CometChatAIStreamService.QueueCompletionCallback { result ->
            completionInvoked = true
            completionResult = result
            // Replace StreamMessage with final message in the list (use index 0 since we know it's the only item)
            if (messageList.isNotEmpty()) {
                messageList[0] = finalMessage
            }
        })

        // Send RUN_FINISHED event so checkAndTriggerQueueCompletion knows the run is done
        val runFinished = AIAssistantBaseEvent().apply {
            id = runId
            type = UIKitConstants.AIAssistantEventType.RUN_FINISHED
        }
        service.handleIncomingEvent(runFinished)

        // Process the RUN_FINISHED event from the queue
        service.processQueueSequentially(runId)
        testScope.testScheduler.advanceUntilIdle()

        // 8. Verify the completion callback was invoked and the StreamMessage was replaced
        completionInvoked shouldBe true
        messageList.size shouldBe 1
        messageList[0] shouldBe finalMessage

        testScope.coroutineContext[Job]?.cancel()
    }

    "Property 19: StreamMessage text updates incrementally as deltas arrive" {
        val testScope = TestScope()
        val service = CometChatAIStreamService(testScope)
        service.setStreamDelay(0)

        val runId = 2002L
        val textSnapshots = mutableListOf<String>()

        val streamMessage = StreamMessage(
            receiverUid = "agent_bot_2",
            receiverType = UIKitConstants.ReceiverType.USER,
            text = ""
        )

        // Register listener that captures text snapshots on each event dispatch.
        // Note: accumulatedText is updated at enqueue time (handleIncomingEvent),
        // so the listener always sees the latest accumulated value.
        service.startStreamingForRunId(runId, CometChatAIStreamService.AIStreamListener { event ->
            val accumulated = service.accumulatedText(runId).value
            streamMessage.text = accumulated
            textSnapshots.add(accumulated)
        })

        // Enqueue deltas one at a time and process after each to observe incremental accumulation
        val deltas = listOf("A", "B", "C")
        deltas.forEach { delta ->
            val contentEvent = AIAssistantContentReceivedEvent().apply {
                id = runId
                this.delta = delta
            }
            service.handleIncomingEvent(contentEvent)
            // Process immediately after each enqueue so the listener sees incremental state
            service.processQueueSequentially(runId)
            testScope.testScheduler.advanceUntilIdle()
        }

        // Accumulated text builds up incrementally: "A", "AB", "ABC"
        textSnapshots.size shouldBe 3
        textSnapshots[0] shouldBe "A"
        textSnapshots[1] shouldBe "AB"
        textSnapshots[2] shouldBe "ABC"
        streamMessage.text shouldBe "ABC"

        testScope.coroutineContext[Job]?.cancel()
    }

    // ── Property 20: Stream Interruption Marking ────────────────────────
    // Validates: Requirements 14.4

    "Property 20: StreamMessage isStreamingInterrupted is false initially" {
        val streamMessage = StreamMessage(
            receiverUid = "agent_bot_3",
            receiverType = UIKitConstants.ReceiverType.USER,
            text = "streaming content"
        )
        streamMessage.isStreamingInterrupted shouldBe false
    }

    "Property 20: StreamMessage isStreamingInterrupted can be set to true on interruption" {
        val streamMessage = StreamMessage(
            receiverUid = "agent_bot_4",
            receiverType = UIKitConstants.ReceiverType.USER,
            text = "partial content"
        )

        // Initially false
        streamMessage.isStreamingInterrupted shouldBe false

        // Simulate interruption (as ViewModel would do on StreamingState.Interrupted)
        streamMessage.isStreamingInterrupted = true

        // Verify it's now true
        streamMessage.isStreamingInterrupted shouldBe true
    }

    "Property 20: StreamMessage retains text content after interruption marking" {
        val streamMessage = StreamMessage(
            receiverUid = "agent_bot_5",
            receiverType = UIKitConstants.ReceiverType.USER,
            text = "partial response from AI"
        )

        streamMessage.isStreamingInterrupted = true

        // Text should be preserved even after interruption
        streamMessage.text shouldBe "partial response from AI"
        streamMessage.isStreamingInterrupted shouldBe true
        streamMessage.type shouldBe UIKitConstants.MessageType.STREAM
        streamMessage.category shouldBe UIKitConstants.MessageCategory.STREAM
    }

    // ── Property 21: Parent Message ID Lifecycle ────────────────────────
    // Validates: Requirements 11.1, 11.2, 11.3
    //
    // Tests the logic pattern: parentMessageId starts at -1, agentChatParentMessageIdSet starts false.
    // On "success": if !agentChatParentMessageIdSet && parentMessageId == -1, set parentMessageId = messageId, set flag = true.
    // On "failure": nothing changes.

    /**
     * Simulates the parent message ID state machine from CometChatMessageListViewModel.
     * Returns the final (parentMessageId, agentChatParentMessageIdSet) after processing all events.
     */
    fun simulateParentMessageIdLifecycle(
        events: List<Pair<Long, Boolean>> // List of (messageId, isSuccess)
    ): Pair<Long, Boolean> {
        var parentMessageId = -1L
        var agentChatParentMessageIdSet = false

        for ((messageId, isSuccess) in events) {
            if (isSuccess) {
                if (!agentChatParentMessageIdSet && parentMessageId == -1L) {
                    parentMessageId = messageId
                    agentChatParentMessageIdSet = true
                }
            }
            // On failure: nothing changes (parentMessageId stays -1, flag stays false)
        }

        return parentMessageId to agentChatParentMessageIdSet
    }

    "Property 21: parentMessageId is set to first successful message ID" {
        forAll(
            PropTestConfig(iterations = 100),
            Arb.long(1L..Long.MAX_VALUE / 2)
        ) { messageId ->
            val (parentId, flagSet) = simulateParentMessageIdLifecycle(
                listOf(messageId to true)
            )
            parentId == messageId && flagSet
        }
    }

    "Property 21: parentMessageId stays -1 on failure, then set on subsequent success" {
        forAll(
            PropTestConfig(iterations = 100),
            Arb.long(1L..Long.MAX_VALUE / 2),
            Arb.long(1L..Long.MAX_VALUE / 2)
        ) { failedId, successId ->
            val (parentId, flagSet) = simulateParentMessageIdLifecycle(
                listOf(failedId to false, successId to true)
            )
            parentId == successId && flagSet
        }
    }

    "Property 21: after first success, subsequent sends do not change parentMessageId" {
        forAll(
            PropTestConfig(iterations = 100),
            Arb.long(1L..Long.MAX_VALUE / 2),
            Arb.list(Arb.long(1L..Long.MAX_VALUE / 2), 1..10)
        ) { firstId, subsequentIds ->
            val events = mutableListOf(firstId to true)
            subsequentIds.forEach { id -> events.add(id to true) }

            val (parentId, flagSet) = simulateParentMessageIdLifecycle(events)
            parentId == firstId && flagSet
        }
    }

    "Property 21: multiple failures followed by success sets parentMessageId to the successful ID" {
        forAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.long(1L..Long.MAX_VALUE / 2), 1..5),
            Arb.long(1L..Long.MAX_VALUE / 2)
        ) { failedIds, successId ->
            val events = failedIds.map { it to false } + listOf(successId to true)

            val (parentId, flagSet) = simulateParentMessageIdLifecycle(events)
            parentId == successId && flagSet
        }
    }

    "Property 21: all failures leave parentMessageId as -1" {
        forAll(
            PropTestConfig(iterations = 100),
            Arb.list(Arb.long(1L..Long.MAX_VALUE / 2), 1..10)
        ) { failedIds ->
            val events = failedIds.map { it to false }

            val (parentId, flagSet) = simulateParentMessageIdLifecycle(events)
            parentId == -1L && !flagSet
        }
    }

    "Property 21: mixed success/failure sequence - parentMessageId equals first success only" {
        forAll(
            PropTestConfig(iterations = 100),
            Arb.list(
                Arb.element(true, false),
                2..15
            ),
            Arb.list(Arb.long(1L..Long.MAX_VALUE / 2), 2..15)
        ) { outcomes, ids ->
            // Zip outcomes with ids, truncate to shorter list
            val minLen = minOf(outcomes.size, ids.size)
            val events = (0 until minLen).map { i -> ids[i] to outcomes[i] }

            val (parentId, flagSet) = simulateParentMessageIdLifecycle(events)

            // Find the first success index
            val firstSuccessIdx = events.indexOfFirst { it.second }

            if (firstSuccessIdx == -1) {
                // No success at all
                parentId == -1L && !flagSet
            } else {
                // parentMessageId should be the ID of the first successful send
                parentId == events[firstSuccessIdx].first && flagSet
            }
        }
    }
})
