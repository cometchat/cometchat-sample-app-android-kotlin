package com.cometchat.chatuikit.shared.ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.AIAssistantBaseEvent;
import com.cometchat.chat.models.AIAssistantMessage;
import com.cometchat.chat.models.AIAssistantToolEndedEvent;
import com.cometchat.chat.models.AIToolArgumentMessage;
import com.cometchat.chat.models.AIToolResultMessage;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.ToolCallListener;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class CometChatAIStreamService {

    private static final Object lock = new Object();
    private static final int DEFAULT_MAX_CONCURRENT_QUEUES = 10;
    private static final String TAG = "CometChatAIStreamService";

    // Static fields for shared state
    private static final ConcurrentHashMap<Long, ConcurrentLinkedQueue<AIAssistantBaseEvent>> eventQueues = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<CometChatAIStreamService.AIStreamListener>> runIdListeners = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, AIAssistantMessage> aiAssistantMessages = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, AIToolResultMessage> aiToolResultMessages = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, AIToolArgumentMessage> aiToolArgumentMessages = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, QueueCompletionCallback> queueCompletionCallbacks = new ConcurrentHashMap<>();
    private static final Set<Long> disconnectedRunIds = new CopyOnWriteArraySet<>();
    private static OnStreamCallBack streamCallBack = null;

    // Static connection state
    private static boolean isConnected = true;
    private static int maxConcurrentQueues = DEFAULT_MAX_CONCURRENT_QUEUES;
    private static long streamDelayMillis = 30; // Default Stream Speed
    private static HashMap<String, ToolCallListener> toolCallListenerHashMap = new HashMap<>();

    // Interface for queue completion callback
    public interface QueueCompletionCallback {
        void onQueueCompleted(AIAssistantMessage aiAssistantMessage,
                              AIToolResultMessage aiToolResultMessage,
                              AIToolArgumentMessage aiToolArgumentMessage);
    }

    public interface OnStreamCallBack {
        void onStreamCompleted();
        void onStreamInterrupted();
    }

    // Enhanced AI Assistant Listener interface
    public interface AIStreamListener  {
        void onAIAssistantEventReceived(AIAssistantBaseEvent aiAssistantBaseEvent);
        void onError(CometChatException exception);
    }

    public CometChatAIStreamService() {
        // Constructor can be used for instance-specific initialization if needed
    }

    public static void setMaxConcurrentQueues(int maxQueues) {
        maxConcurrentQueues = maxQueues > 0 ? maxQueues : DEFAULT_MAX_CONCURRENT_QUEUES;
    }

    public int getMaxConcurrentQueues() {
        return maxConcurrentQueues;
    }

    public static void attachListener(String listenerId) {
        CometChat.addAIAssistantListener(listenerId+TAG, new CometChat.AIAssistantListener() {
            @Override
            public void onAIAssistantEventReceived(AIAssistantBaseEvent aiAssistantBaseEvent) {
                handleIncomingEvent(aiAssistantBaseEvent);
                if (UIKitConstants.AIAssistantEventType.RUN_FINISHED.equalsIgnoreCase(aiAssistantBaseEvent.getType())) {
                    // Only trigger completion if queue is actually empty
                    ConcurrentLinkedQueue<AIAssistantBaseEvent> queue = eventQueues.get(aiAssistantBaseEvent.getId());
                    if (queue == null || queue.isEmpty()) {
                        checkAndTriggerQueueCompletion(aiAssistantBaseEvent.getId());
                        cleanupRunId(aiAssistantBaseEvent.getId());
                    }
                }
            }
        });
        CometChat.addMessageListener(listenerId+TAG, new CometChat.MessageListener() {
            @Override
            public void onAIAssistantMessageReceived(AIAssistantMessage aiAssistantMessage) {
                long runId = aiAssistantMessage.getRunId();

                // If disconnected and this runId was active during disconnection, discard
                if (disconnectedRunIds.contains(runId)) {
                    Log.d(TAG, "Discarding AIAssistantMessage for disconnected runId: " + runId);
                    return;
                }

                // Store the message for later callback
                aiAssistantMessages.put(runId, aiAssistantMessage);
                checkAndTriggerQueueCompletion(runId);
            }

            @Override
            public void onAIToolResultReceived(AIToolResultMessage aiToolResultMessage) {
                long runId = aiToolResultMessage.getRunId();

                // If disconnected and this runId was active during disconnection, discard
                if (disconnectedRunIds.contains(runId)) {
                    Log.d(TAG, "Discarding AIToolResultMessage for disconnected runId: " + runId);
                    return;
                }

                // Store the message for later callback
                aiToolResultMessages.put(runId, aiToolResultMessage);

                // Only trigger completion if event queue is empty
                ConcurrentLinkedQueue<AIAssistantBaseEvent> queue = eventQueues.get(runId);
                if (queue == null || queue.isEmpty()) {
                    checkAndTriggerQueueCompletion(runId);
                }
            }

            @Override
            public void onAIToolArgumentsReceived(AIToolArgumentMessage aiToolArgumentMessage) {
                long runId = aiToolArgumentMessage.getRunId();

                // If disconnected and this runId was active during disconnection, discard
                if (disconnectedRunIds.contains(runId)) {
                    Log.d(TAG, "Discarding AIToolArgumentMessage for disconnected runId: " + runId);
                    return;
                }

                // Store the message for later callback
                aiToolArgumentMessages.put(runId, aiToolArgumentMessage);

                // Only trigger completion if event queue is empty
                ConcurrentLinkedQueue<AIAssistantBaseEvent> queue = eventQueues.get(runId);
                if (queue == null || queue.isEmpty()) {
                    checkAndTriggerQueueCompletion(runId);
                }
            }
        });

        CometChat.addConnectionListener(listenerId+TAG, new CometChat.ConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "WebSocket connected");
                isConnected = true;
                // Clear disconnected run IDs as we're reconnected
                // disconnectedRunIds.clear();
            }

            @Override
            public void onConnecting() {
                Log.d(TAG, "WebSocket connecting");
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "WebSocket disconnected");
                isConnected = false;
                handleDisconnection();
            }

            @Override
            public void onFeatureThrottled() {
                Log.d(TAG, "Feature throttled");
            }

            @Override
            public void onConnectionError(CometChatException e) {
                Log.e(TAG, "Connection error: " + e.getMessage());
                isConnected = false;
            }
        });
    }

    public static void setOnStreamCallBack(OnStreamCallBack callBack) {
        streamCallBack = callBack;
    }

    private static void handleDisconnection() {
        // Add all current run IDs to disconnected set
        disconnectedRunIds.addAll(eventQueues.keySet());

        if (streamCallBack != null)
            streamCallBack.onStreamInterrupted();

        // Notify all listeners about connection error
        notifyConnectionError(new CometChatException("CometChatStreamService", "WebSocket disconnected"));

        // Stop streaming for all active run IDs
        for (Long runId : eventQueues.keySet()) {
            stopStreamingForRunIdInternal(runId);
        }
    }

    private static void handleConnectionError(CometChatException exception) {
        // Add all current run IDs to disconnected set
        disconnectedRunIds.addAll(eventQueues.keySet());

        // Stop streaming for all active run IDs
        for (Long runId : eventQueues.keySet()) {
            stopStreamingForRunIdInternal(runId);
        }
    }

    private static void notifyConnectionError(CometChatException exception) {
        for (CopyOnWriteArrayList<CometChatAIStreamService.AIStreamListener> listeners : runIdListeners.values()) {
            for (CometChatAIStreamService.AIStreamListener listener : listeners) {
                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(() -> listener.onError(exception));
                }
            }
        }
    }

    private static void handleIncomingEvent(AIAssistantBaseEvent event) {
        long runId = event.getId();

        // If not connected or this runId is marked as disconnected, ignore new events
        if (!isConnected || disconnectedRunIds.contains(runId)) {
            Log.d(TAG, "Ignoring event for runId: " + runId + " (disconnected or web socket not connected)");
            return;
        }

        // Check if we've reached the maximum number of concurrent queues
        if (eventQueues.size() >= maxConcurrentQueues && !eventQueues.containsKey(runId)) {
            // Queue limit exceeded, ignore this event
            return;
        }

        // Add event to the appropriate queue
        ConcurrentLinkedQueue<AIAssistantBaseEvent> queue = eventQueues.computeIfAbsent(runId, k -> new ConcurrentLinkedQueue<>());
        boolean wasEmpty = queue.isEmpty();
        queue.offer(event);

        // Check if queue becomes empty after adding this event (for completion check)
        checkQueueEmptyStatus(runId);

        // Only process events through the delayed mechanism if there are active listeners
        CopyOnWriteArrayList<CometChatAIStreamService.AIStreamListener> listeners = runIdListeners.get(runId);
        if (listeners != null && !listeners.isEmpty()) {

            // If the queue was empty and we just added an event, and there are active listeners,
            // we need to restart the sequential processing
            if (wasEmpty) {
                for (AIStreamListener listener : listeners) {
                    processQueueSequentially(runId, listener);
                    break;
                }
            }
        }
    }

    private static void checkQueueEmptyStatus(long runId) {
        ConcurrentLinkedQueue<AIAssistantBaseEvent> queue = eventQueues.get(runId);
        if (queue != null && queue.isEmpty()) {
            // Queue is empty, trigger callback if available
            checkAndTriggerQueueCompletion(runId);
        }
    }

    private static void checkAndTriggerQueueCompletion(long runId) {
        ConcurrentLinkedQueue<AIAssistantBaseEvent> queue = eventQueues.get(runId);
        if (queue == null || queue.isEmpty()) {
            QueueCompletionCallback callback = queueCompletionCallbacks.get(runId);
            if (callback != null) {
                AIAssistantMessage aiMsg = aiAssistantMessages.get(runId);
                AIToolResultMessage toolResult = aiToolResultMessages.get(runId);
                AIToolArgumentMessage toolArg = aiToolArgumentMessages.get(runId);
                if(aiMsg!=null) {
                    aiAssistantMessages.remove(runId);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onQueueCompleted(aiMsg, null, null));
                    if (streamCallBack != null)
                        streamCallBack.onStreamCompleted();
                }

                if(toolResult!=null){
                   aiToolResultMessages.remove(runId);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onQueueCompleted(null, toolResult, null));
                }

                if(toolArg!=null){
                    aiToolArgumentMessages.remove(runId);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onQueueCompleted(null, null, toolArg));
                }
            }
        }
    }

    public static void startStreamingForRunId(long runId, AIStreamListener listener) {
        // If this runId is marked as disconnected, don't start streaming
        if (disconnectedRunIds.contains(runId)) {
            if (listener != null) {
                listener.onError(new CometChatException(TAG, "Cannot start streaming for disconnected session"));
            }
            return;
        }

        // Check if we've reached the maximum number of concurrent queues
        if (eventQueues.size() >= maxConcurrentQueues && !eventQueues.containsKey(runId)) {
            // Queue limit exceeded, cannot start streaming for this run ID
            return;
        }

        // Add listener for this run ID
        runIdListeners.computeIfAbsent(runId, k -> new CopyOnWriteArrayList<>()).add(listener);

        // Process existing events sequentially with proper delay
        processQueueSequentially(runId, listener);
    }

    private static void processQueueSequentially(long runId, AIStreamListener listener) {
        ConcurrentLinkedQueue<AIAssistantBaseEvent> queue = eventQueues.get(runId);
        if (queue == null || queue.isEmpty()) {
            checkQueueEmptyStatus(runId);
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());

        Runnable processNext = new Runnable() {
            @Override
            public void run() {
                // Check connection and disconnection status
                if (!isConnected || disconnectedRunIds.contains(runId)) {
                    return;
                }

                // Poll next event (removes it permanently from queue)
                AIAssistantBaseEvent event = queue.poll();
                if (event != null) {
                    // Process event on main thread (already on main thread due to Handler)
                    listener.onAIAssistantEventReceived(event);

                    if (event instanceof AIAssistantToolEndedEvent) {
                        AIAssistantToolEndedEvent toolEndedEvent = (AIAssistantToolEndedEvent) event;
                        ToolCallListener toolCallListener = toolCallListenerHashMap.get(toolEndedEvent.getToolCallName());
                        if (toolCallListener != null) {
                            toolCallListener.call(toolEndedEvent.getArguments());
                        }
                    }

                    // Schedule next event processing after delay (maintains proper streaming effect)
                    if (!queue.isEmpty() && isConnected && !disconnectedRunIds.contains(runId)) {
                        handler.postDelayed(this, streamDelayMillis);
                    } else {
                        // Queue is empty or connection lost - trigger completion immediately
                        checkAndTriggerQueueCompletion(runId);
                    }
                } else {
                    // No more events to process - trigger completion immediately
                    checkAndTriggerQueueCompletion(runId);
                }
            }
        };

        // Start processing immediately
        handler.post(processNext);
    }

    public static void setStreamDelay(long delayMillis) {
        streamDelayMillis = delayMillis;
    }

    public static void stopStreamingForRunId(long runId, CometChatAIStreamService.AIStreamListener listener) {
        CopyOnWriteArrayList<CometChatAIStreamService.AIStreamListener> listeners = runIdListeners.get(runId);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                cleanupRunId(runId);
            }
        }
    }

    private static void stopStreamingForRunIdInternal(long runId) {
        cleanupRunId(runId);
    }

    public static void stopStreamingForRunId(long runId) {
        cleanupRunId(runId);
    }

    public static void setQueueCompletionCallback(long runId, QueueCompletionCallback callback) {
        queueCompletionCallbacks.put(runId, callback);
        checkAndTriggerQueueCompletion(runId);
    }

    /** Sets the tools available for the AI assistant.
     *
     * @param aiAssistantTools A HashMap containing tool names as keys and their corresponding ToolCallListener as values.
     */
    public static void setAiAssistantTools(HashMap<String, ToolCallListener> aiAssistantTools) {
        if (aiAssistantTools != null) toolCallListenerHashMap = aiAssistantTools;
    }

    public static void removeQueueCompletionCallback(long runId) {
        queueCompletionCallbacks.remove(runId);
    }

    private static void cleanupRunId(long runId) {
        runIdListeners.remove(runId);
        eventQueues.remove(runId);
        // aiAssistantMessages.remove(runId);
        // aiToolResultMessages.remove(runId);
        // aiToolArgumentMessages.remove(runId);
        // queueCompletionCallbacks.remove(runId);
    }

    public void clearQueueForRunId(long runId) {
        cleanupRunId(runId);
    }

    public int getCurrentQueueCount() {
        return eventQueues.size();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isQueueEmpty(long runId) {
        ConcurrentLinkedQueue<AIAssistantBaseEvent> queue = eventQueues.get(runId);
        return queue == null || queue.isEmpty();
    }

    public static void detachListener(String listenerId) {
        CometChat.removeAIAssistantListener(listenerId+TAG);
        CometChat.removeMessageListener(listenerId+TAG);
        CometChat.removeConnectionListener(listenerId+TAG);

        // Clear all data
        eventQueues.clear();
        runIdListeners.clear();
        aiAssistantMessages.clear();
        aiToolResultMessages.clear();
        aiToolArgumentMessages.clear();
        queueCompletionCallbacks.clear();
        disconnectedRunIds.clear();
    }
}
