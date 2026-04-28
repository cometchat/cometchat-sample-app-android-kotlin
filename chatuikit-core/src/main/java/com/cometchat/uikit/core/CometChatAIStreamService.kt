package com.cometchat.uikit.core

import android.util.Log
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.AIAssistantBaseEvent
import com.cometchat.chat.models.AIAssistantContentReceivedEvent
import com.cometchat.chat.models.AIAssistantMessage
import com.cometchat.chat.models.AIAssistantToolEndedEvent
import com.cometchat.chat.models.AIToolArgumentMessage
import com.cometchat.chat.models.AIToolResultMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.ConnectionState
import com.cometchat.uikit.core.domain.model.QueueCompletionResult
import com.cometchat.uikit.core.domain.model.StreamingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Instance-based AI stream service that manages event queuing, sequential
 * dispatch, listener management, and connection handling for AI assistant
 * streaming sessions.
 *
 * Each instance maintains fully independent state — multiple chat screens
 * can stream concurrently without interference.
 *
 * @param scope The [CoroutineScope] used for lifecycle management and
 *              structured concurrency. Typically `viewModelScope`.
 */
class CometChatAIStreamService(
    private val scope: CoroutineScope
) {

    // ── Callback Interfaces ─────────────────────────────────────────────

    /**
     * Listener for AI assistant streaming events dispatched per Run ID.
     */
    fun interface AIStreamListener {
        fun onAIAssistantEventReceived(event: AIAssistantBaseEvent)
        fun onError(exception: CometChatException) {}
    }

    /**
     * Callback invoked once when all events for a Run ID have been
     * dispatched and all final messages are available.
     */
    fun interface QueueCompletionCallback {
        fun onQueueCompleted(result: QueueCompletionResult)
    }

    /**
     * Callback for stream lifecycle events at the instance level.
     */
    interface OnStreamCallback {
        fun onStreamCompleted()
        fun onStreamInterrupted()
    }

    /**
     * Listener for AI assistant tool invocations by name.
     */
    fun interface ToolCallListener {
        fun call(args: String)
    }

    // ── Internal State (per-instance, no static/companion) ──────────────

    private val eventQueues = ConcurrentHashMap<Long, ArrayDeque<AIAssistantBaseEvent>>()
    private val runIdListeners = ConcurrentHashMap<Long, CopyOnWriteArrayList<AIStreamListener>>()
    private val storedAssistantMessages = ConcurrentHashMap<Long, AIAssistantMessage>()
    private val storedToolResultMessages = ConcurrentHashMap<Long, AIToolResultMessage>()
    private val storedToolArgumentMessages = ConcurrentHashMap<Long, AIToolArgumentMessage>()
    private val queueCompletionCallbacks = ConcurrentHashMap<Long, QueueCompletionCallback>()
    private val completionGuards = ConcurrentHashMap<Long, AtomicBoolean>()
    private val disconnectedRunIds = CopyOnWriteArraySet<Long>()
    private val processingJobs = ConcurrentHashMap<Long, Job>()
    private val accumulatedTexts = ConcurrentHashMap<Long, MutableStateFlow<String>>()
    /** Tracks Run IDs for which a RUN_FINISHED event has been received. */
    private val runFinishedReceived = CopyOnWriteArraySet<Long>()

    // ── Scope Cancellation Cleanup ──────────────────────────────────────

    init {
        scope.coroutineContext[Job]?.invokeOnCompletion {
            // Clean up all state when scope is cancelled
            processingJobs.values.forEach { it.cancel() }
            eventQueues.clear()
            runIdListeners.clear()
            storedAssistantMessages.clear()
            storedToolResultMessages.clear()
            storedToolArgumentMessages.clear()
            queueCompletionCallbacks.clear()
            completionGuards.clear()
            disconnectedRunIds.clear()
            processingJobs.clear()
            accumulatedTexts.clear()
            runFinishedReceived.clear()
            _streamingStates.value = emptyMap()
        }
    }

    // ── Observable State ────────────────────────────────────────────────

    private val _connectionState = MutableStateFlow(ConnectionState.CONNECTED)
    private val _streamingStates = MutableStateFlow<Map<Long, StreamingState>>(emptyMap())

    /** Current WebSocket connection state. */
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /** Per-Run ID streaming lifecycle states. */
    val streamingStates: StateFlow<Map<Long, StreamingState>> = _streamingStates.asStateFlow()

    /**
     * Returns a [StateFlow] of accumulated text content for the given [runId].
     * Creates a new flow with an empty string if one does not yet exist.
     */
    fun accumulatedText(runId: Long): StateFlow<String> {
        return accumulatedTexts.getOrPut(runId) { MutableStateFlow("") }.asStateFlow()
    }

    // ── Configuration ───────────────────────────────────────────────────

    private var streamDelayMillis: Long = 30L
    private var maxQueueSize: Int = 1000
    private var maxConcurrentQueues: Int = 10
    private var onStreamCallback: OnStreamCallback? = null
    private var toolCallListeners: Map<String, ToolCallListener> = emptyMap()

    // ── Configuration Methods ───────────────────────────────────────────

    /**
     * Sets the delay between sequential event dispatches.
     * @param delayMillis Delay in milliseconds. Default is 30.
     */
    fun setStreamDelay(delayMillis: Long) {
        streamDelayMillis = delayMillis
    }

    /**
     * Sets the maximum number of events a single Run ID queue can hold.
     * When exceeded, the oldest event is dropped.
     * @param maxSize Maximum queue size. Default is 1000.
     */
    fun setMaxQueueSize(maxSize: Int) {
        this.maxQueueSize = maxSize
    }

    /**
     * Sets the maximum number of concurrent Run ID queues.
     * New Run IDs are rejected when this limit is reached.
     * @param maxQueues Maximum concurrent queues. Default is 10.
     */
    fun setMaxConcurrentQueues(maxQueues: Int) {
        this.maxConcurrentQueues = if (maxQueues > 0) maxQueues else 10
    }

    /**
     * Registers an instance-level callback for stream lifecycle events.
     */
    fun setOnStreamCallback(callback: OnStreamCallback?) {
        this.onStreamCallback = callback
    }

    /**
     * Registers tool call handlers by name for AI assistant tool invocations.
     */
    fun setAiAssistantTools(tools: Map<String, ToolCallListener>) {
        this.toolCallListeners = tools
    }

    // ── Query Methods ───────────────────────────────────────────────────

    /** Returns `true` if the connection state is [ConnectionState.CONNECTED]. */
    fun isConnected(): Boolean = _connectionState.value == ConnectionState.CONNECTED

    /** Returns `true` if the event queue for [runId] is empty or absent. */
    fun isQueueEmpty(runId: Long): Boolean {
        val queue = eventQueues[runId]
        return queue == null || queue.isEmpty()
    }

    /** Returns the number of active event queues. */
    fun getCurrentQueueCount(): Int = eventQueues.size

    // ── Method Stubs (implemented in subsequent tasks) ──────────────────

    /** Enqueues an incoming SDK event. */
    internal fun handleIncomingEvent(event: AIAssistantBaseEvent) {
        val runId = event.id

        Log.d(TAG, "handleIncomingEvent: runId=$runId type=${event.type} class=${event.javaClass.simpleName} connected=${_connectionState.value} disconnected=${disconnectedRunIds.contains(runId)}")

        // 1. If not connected, discard
        if (_connectionState.value != ConnectionState.CONNECTED) return

        // 2. If runId is marked as disconnected, discard
        if (disconnectedRunIds.contains(runId)) return

        // 3. If runId is new and we've hit maxConcurrentQueues, reject
        if (!eventQueues.containsKey(runId) && eventQueues.size >= maxConcurrentQueues) {
            Log.w(TAG, "handleIncomingEvent: REJECTED runId=$runId — maxConcurrentQueues=$maxConcurrentQueues reached")
            return
        }

        // 4. Get or create queue for this runId
        val queue = eventQueues.getOrPut(runId) { ArrayDeque() }

        // 5. If queue is at capacity, drop the oldest event
        if (queue.size >= maxQueueSize) {
            queue.removeFirst()
        }

        // 6. Enqueue the new event
        queue.addLast(event)

        // 7. Update streaming state to Streaming
        _streamingStates.value = _streamingStates.value + (runId to StreamingState.Streaming)

        // Track RUN_FINISHED so checkAndTriggerQueueCompletion knows the run is truly done
        if (UIKitConstants.AIAssistantEventType.RUN_FINISHED.equals(event.type, ignoreCase = true)) {
            runFinishedReceived.add(runId)
        }

        // 8. If event contains text content, accumulate it
        if (event is AIAssistantContentReceivedEvent) {
            val delta = event.delta ?: ""
            val textFlow = accumulatedTexts.getOrPut(runId) { MutableStateFlow("") }
            textFlow.value += delta
        }

        // 9. Auto-start processing if listeners are already registered but no active job
        //    This handles the case where events arrive after startStreamingForRunId()
        //    was called but the previous processing job already finished.
        val hasListeners = runIdListeners[runId]?.isNotEmpty() == true
        val jobActive = processingJobs[runId]?.isActive == true
        if (hasListeners && !jobActive) {
            Log.d(TAG, "handleIncomingEvent: auto-starting processQueue for runId=$runId (listeners registered, no active job)")
            processQueueSequentially(runId)
        }
    }

    /** Launches a coroutine to process events sequentially for [runId]. */
    internal fun processQueueSequentially(runId: Long) {
        // Cancel any existing processing job for this runId to avoid duplicates
        processingJobs[runId]?.cancel()

        processingJobs[runId] = scope.launch(Dispatchers.Main) {
            val queue = eventQueues[runId] ?: return@launch
            Log.d(TAG, "processQueueSequentially: START runId=$runId queueSize=${queue.size}")

            // Keep draining until the queue is truly empty — new events may arrive
            // while we're processing, so we re-check after each drain pass.
            while (true) {
                val event = queue.removeFirstOrNull()
                if (event == null) {
                    // Queue is empty — check if we should finish
                    // Give a small window for late-arriving events
                    delay(streamDelayMillis)
                    if (queue.isEmpty()) {
                        Log.d(TAG, "processQueueSequentially: DRAINED runId=$runId — queue empty after recheck")
                        break
                    }
                    // New events arrived during the delay, continue processing
                    continue
                }

                Log.d(TAG, "processQueueSequentially: DISPATCH runId=$runId type=${event.type} listenersCount=${runIdListeners[runId]?.size ?: 0}")

                // Dispatch event to all registered listeners for this runId
                val listeners = runIdListeners[runId]
                listeners?.forEach { listener ->
                    listener.onAIAssistantEventReceived(event)
                }

                // If event is TOOL_CALL_END, invoke matching ToolCallListener
                if (UIKitConstants.AIAssistantEventType.TOOL_CALL_END.equals(event.type, ignoreCase = true)) {
                    if (event is AIAssistantToolEndedEvent) {
                        val toolListener = toolCallListeners[event.toolCallName]
                        if (toolListener != null) {
                            try {
                                toolListener.call(event.arguments)
                            } catch (e: Exception) {
                                val error = CometChatException(
                                    "TOOL_CALL_ERROR",
                                    "Tool call '${event.toolCallName}' failed: ${e.message}"
                                )
                                listeners?.forEach { listener ->
                                    listener.onError(error)
                                }
                            }
                        }
                    }
                }

                delay(streamDelayMillis)
            }

            Log.d(TAG, "processQueueSequentially: COMPLETE runId=$runId — calling checkAndTriggerQueueCompletion")
            checkAndTriggerQueueCompletion(runId)
        }
    }

    /** Registers a listener and begins streaming for [runId]. */
    fun startStreamingForRunId(runId: Long, listener: AIStreamListener) {
        Log.d(TAG, "startStreamingForRunId: runId=$runId disconnected=${disconnectedRunIds.contains(runId)} state=${_streamingStates.value[runId]?.javaClass?.simpleName}")

        // 1. If runId is marked as disconnected, notify error and return
        if (disconnectedRunIds.contains(runId)) {
            listener.onError(
                CometChatException(
                    "STREAM_ERROR",
                    "Cannot start streaming for disconnected session (runId: $runId)"
                )
            )
            return
        }

        // 2. If streaming state for runId is Completed, notify error and return
        if (_streamingStates.value[runId] is StreamingState.Completed) {
            listener.onError(
                CometChatException(
                    "STREAM_ERROR",
                    "Run already completed (runId: $runId)"
                )
            )
            return
        }

        // 3. Register listener
        runIdListeners.getOrPut(runId) { CopyOnWriteArrayList() }.add(listener)

        // 4. If there are queued events and no active processing job, start processing
        val queue = eventQueues[runId]
        val hasQueuedEvents = queue != null && queue.isNotEmpty()
        val jobActive = processingJobs[runId]?.isActive == true
        Log.d(TAG, "startStreamingForRunId: runId=$runId hasQueuedEvents=$hasQueuedEvents jobActive=$jobActive queueSize=${queue?.size ?: 0}")
        if (hasQueuedEvents && !jobActive) {
            processQueueSequentially(runId)
        }
    }

    /** Removes a single listener for [runId]. Remaining listeners continue receiving events. */
    fun stopStreamingForRunId(runId: Long, listener: AIStreamListener) {
        runIdListeners[runId]?.remove(listener)
    }

    /** Stops all listeners and processing for [runId]. */
    fun stopStreamingForRunId(runId: Long) {
        // Cancel the processing job
        processingJobs[runId]?.cancel()
        processingJobs.remove(runId)
        // Remove all listeners
        runIdListeners.remove(runId)
    }

    /** Registers a completion callback for [runId] and initializes the at-most-once guard. */
    fun setQueueCompletionCallback(runId: Long, callback: QueueCompletionCallback) {
        queueCompletionCallbacks[runId] = callback
        completionGuards[runId] = AtomicBoolean(false)
    }

    /** Removes the completion callback and guard for [runId]. */
    fun removeQueueCompletionCallback(runId: Long) {
        queueCompletionCallbacks.remove(runId)
        completionGuards.remove(runId)
    }

    /**
     * Checks if the event queue for [runId] is drained and triggers the
     * [QueueCompletionCallback] with a single [QueueCompletionResult]
     * containing all available final messages.
     *
     * Uses an [AtomicBoolean] guard to ensure at-most-once invocation
     * per Run ID, even if called from multiple coroutines or callbacks.
     */
    internal fun checkAndTriggerQueueCompletion(runId: Long) {
        // 1. Queue must be empty (or absent) to proceed
        val queue = eventQueues[runId]
        if (queue != null && queue.isNotEmpty()) {
            Log.d(TAG, "checkAndTriggerQueueCompletion: runId=$runId — queue NOT empty (${queue.size}), skipping")
            return
        }

        // 1b. RUN_FINISHED must have been received — without it, the queue may
        //     simply be temporarily empty while more events are in transit.
        if (!runFinishedReceived.contains(runId)) {
            Log.d(TAG, "checkAndTriggerQueueCompletion: runId=$runId — RUN_FINISHED not yet received, skipping premature completion")
            return
        }

        // 2. Acquire the at-most-once guard — if already triggered, bail out
        val guard = completionGuards.getOrPut(runId) { AtomicBoolean(false) }
        if (!guard.compareAndSet(false, true)) {
            Log.d(TAG, "checkAndTriggerQueueCompletion: runId=$runId — guard already triggered, skipping")
            return
        }

        Log.d(TAG, "checkAndTriggerQueueCompletion: runId=$runId — FIRING completion. storedAssistant=${storedAssistantMessages.containsKey(runId)}")

        // 3. Get the callback — if none registered, nothing to invoke
        val callback = queueCompletionCallbacks[runId]

        // 4. Build QueueCompletionResult from all stored final messages
        val result = QueueCompletionResult(
            aiAssistantMessage = storedAssistantMessages[runId],
            aiToolResultMessage = storedToolResultMessages[runId],
            aiToolArgumentMessage = storedToolArgumentMessages[runId]
        )

        // 5. Invoke the callback with the aggregated result (if registered)
        callback?.onQueueCompleted(result)

        // 6. Update streaming state to Completed
        //    NOTE: Do NOT call cleanupRunId() here. The ViewModel observes this
        //    Completed state to perform its own cleanup (removing StreamMessage
        //    from activeStreamMessages). If we clean up here, the runId is removed
        //    from _streamingStates before the ViewModel can observe it.
        _streamingStates.value = _streamingStates.value + (runId to StreamingState.Completed)

        // 7. Notify instance-level stream callback
        onStreamCallback?.onStreamCompleted()

        Log.d(TAG, "checkAndTriggerQueueCompletion: runId=$runId — DONE. Completed state emitted, awaiting ViewModel cleanup.")
    }

    /** Registers SDK listeners for AI events, messages, and connection state. */
    fun attachListener(listenerId: String) {
        val tag = listenerId + TAG
        Log.d(TAG, "attachListener: tag=$tag")

        // 1. AI Assistant listener — receives streaming events
        CometChat.addAIAssistantListener(tag, object : CometChat.AIAssistantListener() {
            override fun onAIAssistantEventReceived(event: AIAssistantBaseEvent) {
                Log.d(TAG, "SDK AIAssistantListener: runId=${event.id} type=${event.type}")
                handleIncomingEvent(event)
                if (UIKitConstants.AIAssistantEventType.RUN_FINISHED.equals(event.type, ignoreCase = true)) {
                    val queue = eventQueues[event.id]
                    Log.d(TAG, "SDK AIAssistantListener: RUN_FINISHED runId=${event.id} queueSize=${queue?.size ?: 0}")
                    if (queue == null || queue.isEmpty()) {
                        checkAndTriggerQueueCompletion(event.id)
                    }
                }
            }
        })

        // 2. Message listener — receives final messages for completion
        CometChat.addMessageListener(tag, object : CometChat.MessageListener() {
            override fun onAIAssistantMessageReceived(msg: AIAssistantMessage) {
                val runId = msg.runId
                Log.d(TAG, "SDK MessageListener: onAIAssistantMessageReceived runId=$runId msgId=${msg.id} disconnected=${disconnectedRunIds.contains(runId)}")
                if (disconnectedRunIds.contains(runId)) return
                storedAssistantMessages[runId] = msg
                checkAndTriggerQueueCompletion(runId)
            }

            override fun onAIToolResultReceived(msg: AIToolResultMessage) {
                val runId = msg.runId
                Log.d(TAG, "SDK MessageListener: onAIToolResultReceived runId=$runId")
                storedToolResultMessages[runId] = msg
                checkAndTriggerQueueCompletion(runId)
            }

            override fun onAIToolArgumentsReceived(msg: AIToolArgumentMessage) {
                val runId = msg.runId
                Log.d(TAG, "SDK MessageListener: onAIToolArgumentsReceived runId=$runId")
                storedToolArgumentMessages[runId] = msg
                checkAndTriggerQueueCompletion(runId)
            }
        })

        // 3. Connection listener — tracks WebSocket state
        CometChat.addConnectionListener(tag, object : CometChat.ConnectionListener {
            override fun onConnected() {
                _connectionState.value = ConnectionState.CONNECTED
            }

            override fun onConnecting() {}

            override fun onDisconnected() {
                handleDisconnection()
            }

            override fun onFeatureThrottled() {}

            override fun onConnectionError(error: CometChatException?) {}
        })
    }

    /** Removes all SDK listeners and clears all internal state. */
    fun detachListener(listenerId: String) {
        val tag = listenerId + TAG

        // 1. Remove all SDK listeners
        CometChat.removeAIAssistantListener(tag)
        CometChat.removeMessageListener(tag)
        CometChat.removeConnectionListener(tag)

        // 2. Cancel all processing jobs
        processingJobs.values.forEach { it.cancel() }

        // 3. Clear ALL internal state
        eventQueues.clear()
        runIdListeners.clear()
        storedAssistantMessages.clear()
        storedToolResultMessages.clear()
        storedToolArgumentMessages.clear()
        queueCompletionCallbacks.clear()
        completionGuards.clear()
        disconnectedRunIds.clear()
        processingJobs.clear()
        accumulatedTexts.clear()
        runFinishedReceived.clear()
        _streamingStates.value = emptyMap()
    }

    /**
     * Handles WebSocket disconnection by marking all active runs as
     * disconnected, notifying listeners, and stopping processing.
     */
    private fun handleDisconnection() {
        // 1. Update connection state
        _connectionState.value = ConnectionState.DISCONNECTED

        val error = CometChatException(
            "CONNECTION_ERROR",
            "WebSocket disconnected"
        )

        // 2. For each active runId: mark disconnected, cancel job, notify listeners
        for (runId in eventQueues.keys) {
            disconnectedRunIds.add(runId)

            // Cancel processing job
            processingJobs[runId]?.cancel()
            processingJobs.remove(runId)

            // Notify all listeners for this runId via onError
            runIdListeners[runId]?.forEach { listener ->
                listener.onError(error)
            }

            // Update streaming state to Interrupted
            _streamingStates.value = _streamingStates.value + (runId to StreamingState.Interrupted(error))
        }

        // 3. Invoke instance-level stream interrupted callback
        onStreamCallback?.onStreamInterrupted()
    }

    /** Removes all state associated with [runId]. */
    fun cleanupRunId(runId: Long) {
        Log.d(TAG, "cleanupRunId: runId=$runId")
        // 1. Cancel and remove processing job
        processingJobs.remove(runId)?.cancel()
        // 2. Remove event queue
        eventQueues.remove(runId)
        // 3. Remove all listeners
        runIdListeners.remove(runId)
        // 4. Remove completion callback
        queueCompletionCallbacks.remove(runId)
        // 5. Remove completion guard
        completionGuards.remove(runId)
        // 6. Remove stored messages
        storedAssistantMessages.remove(runId)
        storedToolResultMessages.remove(runId)
        storedToolArgumentMessages.remove(runId)
        // 7. Remove accumulated text
        accumulatedTexts.remove(runId)
        // 8. Remove from disconnected set
        disconnectedRunIds.remove(runId)
        // 9. Remove from streaming states
        _streamingStates.value = _streamingStates.value - runId
        // 10. Remove from run-finished tracking
        runFinishedReceived.remove(runId)
    }

    /** Delegates to [cleanupRunId]. */
    fun clearQueueForRunId(runId: Long) {
        cleanupRunId(runId)
    }

    companion object {
        private const val TAG = "CometChatAIStreamService"

        private var sharedInstance: CometChatAIStreamService? = null

        /**
         * Returns the current singleton instance, or `null` if no instance
         * has been set (e.g., before ViewModel initialization or after cleanup).
         */
        fun getInstance(): CometChatAIStreamService? = sharedInstance

        /**
         * Sets (or clears) the singleton instance. Called by the ViewModel
         * during `setUser()` and `onCleared()`.
         */
        internal fun setInstance(instance: CometChatAIStreamService?) {
            sharedInstance = instance
        }

        // ── Static Convenience Methods ──────────────────────────────────

        /**
         * Registers a [listener] and begins streaming for [runId] on the
         * current singleton instance. No-op if no instance is set.
         */
        fun startStreamingForRunId(runId: Long, listener: AIStreamListener) {
            getInstance()?.startStreamingForRunId(runId, listener)
        }

        /**
         * Stops all listeners and processing for [runId] on the current
         * singleton instance. No-op if no instance is set.
         */
        fun stopStreamingForRunId(runId: Long) {
            getInstance()?.stopStreamingForRunId(runId)
        }

        /**
         * Returns a [StateFlow] of accumulated text content for the given
         * [runId]. If no instance is set, returns an empty [StateFlow].
         */
        fun accumulatedText(runId: Long): StateFlow<String> {
            return getInstance()?.accumulatedText(runId)
                ?: MutableStateFlow("").asStateFlow()
        }
    }
}
