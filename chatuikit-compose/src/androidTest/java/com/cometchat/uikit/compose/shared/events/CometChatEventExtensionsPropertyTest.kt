package com.cometchat.uikit.compose.shared.events

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.events.CometChatUIEvent
import com.cometchat.uikit.core.events.onMessageEvents
import com.cometchat.uikit.core.events.onUIEvents
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Property-based tests for CometChat Event Extensions (LifecycleOwner and ViewModel).
 *
 * Feature: compose-events-system
 * Properties tested:
 * - Property 5: LifecycleOwner Subscription Management
 * - Property 6: Custom Lifecycle Event Binding
 * - Property 10: ViewModel Scope Persistence
 *
 * Validates: Requirements 9.1-9.8
 */
@RunWith(AndroidJUnit4::class)
class CometChatEventExtensionsPropertyTest {

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun runOnMainThread(block: () -> Unit) {
        val latch = CountDownLatch(1)
        mainHandler.post {
            block()
            latch.countDown()
        }
        latch.await(5, TimeUnit.SECONDS)
    }

    /**
     * Property 5: LifecycleOwner Subscription Management
     *
     * For any LifecycleOwner using event extensions, reaching the start lifecycle event
     * SHALL enable event reception, and reaching the stop lifecycle event SHALL disable event reception.
     *
     * Feature: compose-events-system, Property 5: LifecycleOwner Subscription Management
     * Validates: Requirements 9.2, 9.3
     */
    @Test
    fun property5_lifecycleOwner_receivesEventsWhenStarted() {
        val eventReceived = AtomicBoolean(false)
        val latch = CountDownLatch(1)

        runOnMainThread {
            val testLifecycleOwner = TestLifecycleOwner()
            
            // Subscribe to events
            testLifecycleOwner.onMessageEvents { event ->
                if (event is CometChatMessageEvent.LiveReaction) {
                    eventReceived.set(true)
                    latch.countDown()
                }
            }

            // Move to STARTED state
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }

        // Wait for subscription to be active
        runBlocking { delay(100) }

        // Emit an event
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(12345))

        // Wait for event to be received
        val received = latch.await(2, TimeUnit.SECONDS)

        assert(received && eventReceived.get()) {
            "Event should be received when LifecycleOwner is STARTED"
        }
    }

    @Test
    fun property5_lifecycleOwner_stopsReceivingEventsWhenStopped() {
        val eventsReceived = AtomicInteger(0)
        val firstEventLatch = CountDownLatch(1)
        lateinit var testLifecycleOwner: TestLifecycleOwner

        runOnMainThread {
            testLifecycleOwner = TestLifecycleOwner()
            
            // Subscribe to events
            testLifecycleOwner.onMessageEvents { event ->
                if (event is CometChatMessageEvent.LiveReaction) {
                    eventsReceived.incrementAndGet()
                    firstEventLatch.countDown()
                }
            }

            // Move to STARTED state
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }

        runBlocking { delay(100) }

        // Emit first event while STARTED
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(1))
        firstEventLatch.await(2, TimeUnit.SECONDS)

        val countAfterFirstEvent = eventsReceived.get()
        assert(countAfterFirstEvent >= 1) { "Should receive event while STARTED" }

        // Move to STOPPED state
        runOnMainThread {
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }

        runBlocking { delay(100) }

        // Emit second event after STOPPED
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(2))

        runBlocking { delay(500) }

        val countAfterSecondEvent = eventsReceived.get()
        assert(countAfterSecondEvent == countAfterFirstEvent) {
            "Should NOT receive events after STOPPED. Expected $countAfterFirstEvent but got $countAfterSecondEvent"
        }
    }

    @Test
    fun property5_lifecycleOwner_resumesReceivingEventsWhenRestarted() {
        val eventsReceived = CopyOnWriteArrayList<Int>()
        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)
        lateinit var testLifecycleOwner: TestLifecycleOwner

        runOnMainThread {
            testLifecycleOwner = TestLifecycleOwner()
            
            // Subscribe to events
            testLifecycleOwner.onMessageEvents { event ->
                if (event is CometChatMessageEvent.LiveReaction) {
                    eventsReceived.add(event.icon)
                    if (event.icon == 1) latch1.countDown()
                    if (event.icon == 3) latch2.countDown()
                }
            }

            // Move to STARTED state
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }

        runBlocking { delay(100) }

        // Emit first event
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(1))
        latch1.await(2, TimeUnit.SECONDS)

        // Move to STOPPED state
        runOnMainThread {
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        runBlocking { delay(100) }

        // Emit second event (should not be received)
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(2))
        runBlocking { delay(200) }

        // Move back to STARTED state
        runOnMainThread {
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
        runBlocking { delay(100) }

        // Emit third event (should be received)
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(3))
        latch2.await(2, TimeUnit.SECONDS)

        assert(eventsReceived.contains(1)) { "Should receive first event" }
        assert(!eventsReceived.contains(2)) { "Should NOT receive event while STOPPED" }
        assert(eventsReceived.contains(3)) { "Should receive event after restart" }
    }

    @Test
    fun property5_lifecycleOwner_mainThreadExecution() {
        val isMainThread = AtomicBoolean(false)
        val latch = CountDownLatch(1)

        runOnMainThread {
            val testLifecycleOwner = TestLifecycleOwner()
            
            // Subscribe to events
            testLifecycleOwner.onMessageEvents { event ->
                isMainThread.set(Looper.getMainLooper().isCurrentThread)
                latch.countDown()
            }

            // Move to STARTED state
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }

        runBlocking { delay(100) }

        // Emit an event
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(999))

        latch.await(2, TimeUnit.SECONDS)

        assert(isMainThread.get()) {
            "LifecycleOwner callback should execute on main thread"
        }
    }

    /**
     * Property 6: Custom Lifecycle Event Binding
     *
     * For any LifecycleOwner subscription with custom start/stop events,
     * the subscription SHALL only be active between those specific lifecycle states.
     *
     * Feature: compose-events-system, Property 6: Custom Lifecycle Event Binding
     * Validates: Requirements 9.4
     */
    @Test
    fun property6_customLifecycle_onResumeOnPause() {
        val eventsReceived = CopyOnWriteArrayList<Int>()
        val latch = CountDownLatch(1)
        lateinit var testLifecycleOwner: TestLifecycleOwner

        runOnMainThread {
            testLifecycleOwner = TestLifecycleOwner()
            
            // Subscribe with custom lifecycle events (ON_RESUME/ON_PAUSE)
            testLifecycleOwner.onMessageEvents(
                startEvent = Lifecycle.Event.ON_RESUME,
                stopEvent = Lifecycle.Event.ON_PAUSE
            ) { event ->
                if (event is CometChatMessageEvent.LiveReaction) {
                    eventsReceived.add(event.icon)
                    if (event.icon == 2) latch.countDown()
                }
            }

            // Move to STARTED state - should NOT receive events yet
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }

        runBlocking { delay(100) }

        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(1))
        runBlocking { delay(200) }

        assert(!eventsReceived.contains(1)) { "Should NOT receive events before RESUME" }

        // Move to RESUMED state - should start receiving events
        runOnMainThread {
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
        runBlocking { delay(100) }

        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(2))
        latch.await(2, TimeUnit.SECONDS)

        assert(eventsReceived.contains(2)) { "Should receive events after RESUME" }

        // Move to PAUSED state - should stop receiving events
        runOnMainThread {
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        runBlocking { delay(100) }

        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(3))
        runBlocking { delay(200) }

        assert(!eventsReceived.contains(3)) { "Should NOT receive events after PAUSE" }
    }

    @Test
    fun property6_customLifecycle_onCreateOnDestroy() {
        val eventsReceived = CopyOnWriteArrayList<Int>()
        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)
        lateinit var testLifecycleOwner: TestLifecycleOwner

        runOnMainThread {
            testLifecycleOwner = TestLifecycleOwner()
            
            // Subscribe with custom lifecycle events (ON_CREATE/ON_DESTROY)
            testLifecycleOwner.onMessageEvents(
                startEvent = Lifecycle.Event.ON_CREATE,
                stopEvent = Lifecycle.Event.ON_DESTROY
            ) { event ->
                if (event is CometChatMessageEvent.LiveReaction) {
                    eventsReceived.add(event.icon)
                    if (event.icon == 1) latch1.countDown()
                    if (event.icon == 2) latch2.countDown()
                }
            }

            // Move to CREATED state - should start receiving events
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }

        runBlocking { delay(100) }

        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(1))
        latch1.await(2, TimeUnit.SECONDS)

        assert(eventsReceived.contains(1)) { "Should receive events after CREATE" }

        // Move through lifecycle - should still receive events
        runOnMainThread {
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        runBlocking { delay(100) }

        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(2))
        latch2.await(2, TimeUnit.SECONDS)

        assert(eventsReceived.contains(2)) { "Should still receive events before DESTROY" }

        // Move to DESTROYED state - should stop receiving events
        runOnMainThread {
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
        runBlocking { delay(100) }

        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(3))
        runBlocking { delay(200) }

        assert(!eventsReceived.contains(3)) { "Should NOT receive events after DESTROY" }
    }

    /**
     * Property 10: ViewModel Scope Persistence
     *
     * For any ViewModel-scoped subscription, the subscription SHALL remain active
     * across configuration changes until the ViewModel is cleared.
     *
     * Feature: compose-events-system, Property 10: ViewModel Scope Persistence
     * Validates: Requirements 9.8
     */
    @Test
    fun property10_viewModelScope_receivesEventsWhileActive() {
        val testViewModel = TestViewModel()
        val eventReceived = AtomicBoolean(false)
        val latch = CountDownLatch(1)

        // Subscribe to events via ViewModel
        testViewModel.onMessageEvents { event ->
            if (event is CometChatMessageEvent.LiveReaction) {
                eventReceived.set(true)
                latch.countDown()
            }
        }

        runBlocking { delay(100) }

        // Emit an event
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(12345))

        val received = latch.await(2, TimeUnit.SECONDS)

        assert(received && eventReceived.get()) {
            "ViewModel should receive events while active"
        }

        // Cleanup
        testViewModel.clear()
    }

    @Test
    fun property10_viewModelScope_stopsReceivingEventsWhenCleared() {
        val testViewModel = TestViewModel()
        val eventsReceived = AtomicInteger(0)
        val firstEventLatch = CountDownLatch(1)

        // Subscribe to events via ViewModel
        val job = testViewModel.onMessageEvents { event ->
            if (event is CometChatMessageEvent.LiveReaction) {
                eventsReceived.incrementAndGet()
                firstEventLatch.countDown()
            }
        }

        runBlocking { delay(100) }

        // Emit first event
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(1))
        firstEventLatch.await(2, TimeUnit.SECONDS)

        val countAfterFirstEvent = eventsReceived.get()
        assert(countAfterFirstEvent >= 1) { "Should receive event while ViewModel is active" }

        // Cancel the job (simulating ViewModel clear)
        job.cancel()
        runBlocking { delay(100) }

        // Emit second event
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(2))
        runBlocking { delay(500) }

        val countAfterSecondEvent = eventsReceived.get()
        assert(countAfterSecondEvent == countAfterFirstEvent) {
            "Should NOT receive events after job is cancelled. Expected $countAfterFirstEvent but got $countAfterSecondEvent"
        }

        // Cleanup
        testViewModel.clear()
    }

    @Test
    fun property10_viewModelScope_mainThreadExecution() {
        val testViewModel = TestViewModel()
        val isMainThread = AtomicBoolean(false)
        val latch = CountDownLatch(1)

        // Subscribe to events via ViewModel
        testViewModel.onMessageEvents { event ->
            isMainThread.set(Looper.getMainLooper().isCurrentThread)
            latch.countDown()
        }

        runBlocking { delay(100) }

        // Emit an event
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(999))

        latch.await(2, TimeUnit.SECONDS)

        assert(isMainThread.get()) {
            "ViewModel callback should execute on main thread"
        }

        // Cleanup
        testViewModel.clear()
    }

    @Test
    fun property10_viewModelScope_multipleSubscriptions() {
        val testViewModel = TestViewModel()
        val messageReceived = AtomicBoolean(false)
        val uiReceived = AtomicBoolean(false)
        val latch = CountDownLatch(2)

        // Subscribe to multiple event types
        testViewModel.onMessageEvents { event ->
            if (event is CometChatMessageEvent.LiveReaction) {
                messageReceived.set(true)
                latch.countDown()
            }
        }

        testViewModel.onUIEvents { event ->
            if (event is CometChatUIEvent.ComposeMessage) {
                uiReceived.set(true)
                latch.countDown()
            }
        }

        runBlocking { delay(100) }

        // Emit events
        CometChatEvents.emitMessageEvent(CometChatMessageEvent.LiveReaction(1))
        CometChatEvents.emitUIEvent(CometChatUIEvent.ComposeMessage("test", "text"))

        latch.await(2, TimeUnit.SECONDS)

        assert(messageReceived.get()) { "Should receive message events" }
        assert(uiReceived.get()) { "Should receive UI events" }

        // Cleanup
        testViewModel.clear()
    }
}

/**
 * Test implementation of LifecycleOwner for instrumented testing.
 */
class TestLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }
}

/**
 * Test implementation of ViewModel for instrumented testing.
 */
class TestViewModel : ViewModel() {
    fun clear() {
        viewModelScope.cancel()
    }
}
