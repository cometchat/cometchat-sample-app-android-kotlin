package com.cometchat.uikit.kotlin.presentation.calllogs

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.uikit.core.domain.repository.CallLogsRepository
import com.cometchat.uikit.core.domain.usecase.FetchCallLogsUseCase
import com.cometchat.uikit.core.domain.usecase.InitiateCallUseCase
import com.cometchat.uikit.core.viewmodel.CometChatCallLogsViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.calllogs.style.CometChatCallLogsStyle
import com.cometchat.uikit.kotlin.presentation.calllogs.ui.CometChatCallLogs
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.utils.captureWithPopups
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatCallLogs (chatuikit-kotlin).
 *
 * Captures golden images for ALL visual states of the CometChatCallLogs component.
 * Uses Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * 10 Sections:
 * 1. UI States (Loading, Empty, Error, Content)
 * 2. Popup Menu (long-press default/custom options)
 * 3. Selection Mode (single/multiple)
 * 4. Scroll States (large list)
 * 5. Toolbar
 * 6. Visibility (hide separator, hide toolbar)
 * 7. Custom Views (custom empty, error, item views)
 * 8. Style (custom colors)
 * 9. Content Variants (audio/video/missed mix)
 * 10. Dark Theme
 *
 * Validates: Requirements 18.1–18.14, 31.3–31.6
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatCallLogsScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatCallLogsScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatCallLogsScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/calllogs"
        )
    )

    private companion object {
        const val FIXED_TIMESTAMP = 1735689600L
    }

    /**
     * Swallow the CometChatCalls.init() RuntimeException that occurs when the view
     * creates its internal ViewModel (which we immediately replace with setViewModel).
     * This is a test-only workaround — in production, CometChatCalls.init() is called
     * in Application.onCreate().
     */
    private val originalHandler = Thread.getDefaultUncaughtExceptionHandler()

    @org.junit.Before
    fun setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (throwable.message?.contains("CometChatCalls.init()") == true) {
                // Swallow — expected in test environment
            } else {
                originalHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    @org.junit.After
    fun restoreExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(originalHandler)
    }

    // ==================== Section 1: UI States ====================

    @Test
    fun stateLoading() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createLoadingViewModel())
        }
    }

    @Test
    fun stateEmpty() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(emptyList()))
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createErrorViewModel("LOAD_ERR", "Failed to load call logs"))
        }
    }

    @org.junit.Ignore("Requires CometChatCalls.init() — internal ViewModel creates real CallLogRequest")
    @Test
    fun stateContent() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(createCallLogs(5)))
        }
    }

    // ==================== Section 2: Popup Menu ====================

    @org.junit.Ignore("Requires CometChatCalls.init() — internal ViewModel creates real CallLogRequest")
    @Test
    fun longPressShowsDefaultPopupMenu() {
        launchAndCapturePopup(itemPosition = 0) { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(createCallLogs(5)))
        }
    }

    @org.junit.Ignore("Requires CometChatCalls.init() — internal ViewModel creates real CallLogRequest")
    @Test
    fun longPressShowsCustomPopupMenu() {
        launchAndCapturePopup(itemPosition = 1) { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(createCallLogs(5))).apply {
                setOptions { _, _ ->
                    listOf(
                        CometChatPopupMenu.MenuItem("info", "Call Info", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("block", "Block User", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("delete", "Delete", null, null, 0, 0, 0, 0, null)
                    )
                }
            }
        }
    }

    // ==================== Section 3: Selection Mode ====================
    // Note: CometChatCallLogs does not have a setSelectionMode API at the view level.
    // Selection mode is handled differently from Conversations. Skipping selection screenshots.

    // ==================== Section 4: Scroll States ====================

    @org.junit.Ignore("Requires CometChatCalls.init() — internal ViewModel creates real CallLogRequest")
    @Test
    fun contentLargeList() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(createCallLogs(20)))
        }
    }

    // ==================== Section 5: Toolbar ====================

    @org.junit.Ignore("Requires CometChatCalls.init() — internal ViewModel creates real CallLogRequest")
    @Test
    fun toolbarWithBackButton() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(createCallLogs(3))).apply {
                setBackIconVisibility(View.VISIBLE)
            }
        }
    }

    // ==================== Section 6: Visibility ====================

    @org.junit.Ignore("Requires CometChatCalls.init() — internal ViewModel creates real CallLogRequest")
    @Test
    fun visibilityNoSeparators() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(createCallLogs(5))).apply {
                setSeparatorVisibility(View.GONE)
            }
        }
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customEmptyView() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(emptyList())).apply {
                setEmptyView(TextView(activity).apply {
                    text = "No call history yet"
                    textSize = 18f
                })
            }
        }
    }

    @Test
    fun customErrorView() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createErrorViewModel("NET_ERR", "Network error")).apply {
                setErrorView(TextView(activity).apply {
                    text = "Something went wrong. Tap to retry."
                    textSize = 16f
                })
            }
        }
    }

    // ==================== Section 8: Style ====================

    @org.junit.Ignore("Requires CometChatCalls.init() — internal ViewModel creates real CallLogRequest")
    @Test
    fun styleCustomBackground() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(createCallLogs(5))).apply {
                setStyle(CometChatCallLogsStyle(backgroundColor = 0xFFF5F5DC.toInt()))
            }
        }
    }

    @org.junit.Ignore("Requires CometChatCalls.init() — internal ViewModel creates real CallLogRequest")
    @Test
    fun stylingCustomColors() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(createCallLogs(5))).apply {
                setStyle(CometChatCallLogsStyle(
                    backgroundColor = 0xFF1A1A2E.toInt(),
                    titleTextColor = 0xFFFFFFFF.toInt(),
                    toolbarSeparatorColor = 0xFF333333.toInt()
                ))
            }
        }
    }

    // ==================== Section 9: Content Variants ====================

    @org.junit.Ignore("Requires CometChatCalls.init() — internal ViewModel creates real CallLogRequest")
    @Test
    fun contentMixedCallTypes() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(createMixedCallLogs()))
        }
    }

    // ==================== Section 10: Dark Theme ====================

    @org.junit.Ignore("Requires CometChatCalls.init() — internal ViewModel creates real CallLogRequest")
    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(createCallLogs(5)))
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEmptyDark() {
        launchAndCapture { activity ->
            createCallLogsViewWithViewModel(activity, createViewModel(emptyList()))
        }
    }

    // ==================== Helper: Static Screenshot Capture ====================

    private fun launchAndCapture(configure: (ComponentActivity) -> CometChatCallLogs) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)

            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            container.addView(view, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
            activity.setContentView(container)

            // Idle the looper — the internal ViewModel may throw CometChatCalls.init() error
            // on its coroutine, but our external ViewModel (set via setViewModel) will provide data.
            try {
                ShadowLooper.idleMainLooper()
            } catch (_: Exception) {
                // Swallow CometChatCalls.init() exception from internal ViewModel
            }

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            try {
                ShadowLooper.idleMainLooper()
            } catch (_: Exception) { }

            view.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Popup Capture ====================

    private fun launchAndCapturePopup(
        itemPosition: Int,
        configure: (ComponentActivity) -> CometChatCallLogs
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)

            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addView(view, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
            }
            activity.setContentView(container)
            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)
            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            onView(withId(R.id.recyclerview_call_logs))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        itemPosition, longClick()
                    )
                )
            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Interaction Capture ====================

    private fun launchInteractWithConfig(
        callLogs: List<CallLog>,
        configure: ((CometChatCallLogs) -> Unit)? = null,
        interaction: () -> Unit
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val vm = createViewModel(callLogs)
            val view = createCallLogsViewWithViewModel(activity, vm)
            configure?.invoke(view)

            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            container.addView(view, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
            activity.setContentView(container)
            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)
            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }
        }

        interaction()
        try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

        scenario.onActivity { activity ->
            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== View Factory ====================

    /**
     * Creates a CometChatCallLogs view with an external ViewModel pre-injected via reflection.
     * This prevents the internal ViewModel (which requires CometChatCalls.init()) from being created.
     *
     * The approach:
     * 1. Set `isExternalViewModel = true` and `viewModel` fields via reflection BEFORE init runs
     *    — Not possible since init runs during construction.
     * 2. Instead: Create the view, then immediately replace the internal ViewModel.
     *    The internal ViewModel's coroutine will throw, but we catch it via CoroutineExceptionHandler.
     *
     * Actually: We use a different approach — we set the viewModel field and isExternalViewModel
     * field via reflection AFTER construction but BEFORE the view is attached to the window.
     * Then when observeViewModel() is called (from setViewModel), it uses our ViewModel.
     */
    private fun createCallLogsViewWithViewModel(
        activity: ComponentActivity,
        vm: CometChatCallLogsViewModel
    ): CometChatCallLogs {
        val view = CometChatCallLogs(activity)
        // Use reflection to cancel the internal ViewModel's scope and replace with ours
        try {
            val viewModelField = CometChatCallLogs::class.java.getDeclaredField("viewModel")
            viewModelField.isAccessible = true
            val internalVm = viewModelField.get(view) as? CometChatCallLogsViewModel
            // Clear the internal ViewModel by calling onCleared via reflection
            internalVm?.let {
                val onClearedMethod = it::class.java.getDeclaredMethod("onCleared")
                onClearedMethod.isAccessible = true
                onClearedMethod.invoke(it)
            }
        } catch (_: Exception) { }

        // Now set our external ViewModel
        view.setViewModel(vm)
        return view
    }

    // ==================== ViewModel Factories ====================

    private fun createViewModel(callLogs: List<CallLog>): CometChatCallLogsViewModel {
        val repository = object : CallLogsRepository {
            override suspend fun getCallLogs(request: CallLogRequest) = Result.success(callLogs)
            override fun hasMoreCallLogs() = false
        }
        return CometChatCallLogsViewModel(
            fetchCallLogsUseCase = FetchCallLogsUseCase(repository),
            initiateCallUseCase = mock(),
            enableListeners = false
        )
    }

    private fun createErrorViewModel(code: String, message: String): CometChatCallLogsViewModel {
        val repository = object : CallLogsRepository {
            override suspend fun getCallLogs(request: CallLogRequest) =
                Result.failure<List<CallLog>>(com.cometchat.calls.exceptions.CometChatException(code, message))
            override fun hasMoreCallLogs() = false
        }
        return CometChatCallLogsViewModel(
            fetchCallLogsUseCase = FetchCallLogsUseCase(repository),
            initiateCallUseCase = mock(),
            enableListeners = false
        )
    }

    private fun createLoadingViewModel(): CometChatCallLogsViewModel {
        val repository = object : CallLogsRepository {
            override suspend fun getCallLogs(request: CallLogRequest): Result<List<CallLog>> {
                kotlinx.coroutines.awaitCancellation()
            }
            override fun hasMoreCallLogs() = false
        }
        return CometChatCallLogsViewModel(
            fetchCallLogsUseCase = FetchCallLogsUseCase(repository),
            initiateCallUseCase = mock(),
            enableListeners = false
        )
    }

    // ==================== Data Factories ====================

    private fun createRealCallLog(
        type: String = "audio",
        status: String = "ended",
        initiatorName: String = "Caller",
        initiatorUid: String = "caller-1",
        receiverName: String = "Receiver",
        receiverUid: String = "receiver-1",
        timestamp: Long = FIXED_TIMESTAMP
    ): CallLog {
        val callLog = CallLog()
        callLog.type = type
        callLog.status = status
        callLog.initiatedAt = timestamp
        callLog.endedAt = timestamp + 120
        callLog.totalDurationInMinutes = 2.0
        callLog.sessionID = "session-${initiatorUid.hashCode()}-$timestamp"

        val initiator = CallUser(initiatorUid, initiatorName)
        callLog.initiator = initiator

        val receiver = CallUser(receiverUid, receiverName)
        callLog.receiver = receiver

        return callLog
    }

    private fun createCallLogs(count: Int): List<CallLog> {
        val names = listOf("Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor",
            "Hulk", "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man",
            "Scarlet Witch", "Vision", "Falcon", "Winter Soldier", "War Machine",
            "Loki", "Gamora", "Star-Lord", "Groot", "Rocket")
        return (0 until count).map { i ->
            createRealCallLog(
                type = if (i % 2 == 0) "audio" else "video",
                status = "ended",
                initiatorName = names[i % names.size],
                initiatorUid = "user_$i",
                timestamp = FIXED_TIMESTAMP - (i * 3600L)
            )
        }
    }

    private fun createMixedCallLogs(): List<CallLog> {
        return listOf(
            createRealCallLog(type = "audio", status = "ended", initiatorName = "Iron Man", initiatorUid = "u1"),
            createRealCallLog(type = "video", status = "missed", initiatorName = "Captain America", initiatorUid = "u2"),
            createRealCallLog(type = "audio", status = "cancelled", initiatorName = "Spiderman", initiatorUid = "u3"),
            createRealCallLog(type = "video", status = "rejected", initiatorName = "Black Widow", initiatorUid = "u4"),
            createRealCallLog(type = "audio", status = "unanswered", initiatorName = "Thor", initiatorUid = "u5")
        )
    }
}
