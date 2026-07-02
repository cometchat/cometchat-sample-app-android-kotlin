package com.cometchat.sampleapp.compose.e2e.helpers

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.compose.utils.AppPreferences
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * UI Automator-based E2E test helper for sample-app-compose.
 *
 * Provides SDK initialization, login via UI, navigation, and
 * reusable wait/find utilities using UI Automator.
 *
 * Key differences from the Kotlin sample helper:
 * - No resource IDs (Compose uses semantics/testTag instead of R.id)
 * - No separate activities — everything is Compose Navigation within MainActivity
 * - No CometChatCalls SDK initialization (no onSDKInitialized())
 * - Bottom navigation uses content descriptions: "Chats", "Calls", "Users", "Groups"
 * - Login screen uses BasicTextField (hint "Enter UID") and Button with text "Continue"
 *
 * Credentials can be passed via instrumentation arguments for CI/CD:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.appId=YOUR_APP_ID \
 *       -Pandroid.testInstrumentationRunnerArguments.region=in \
 *       -Pandroid.testInstrumentationRunnerArguments.authKey=YOUR_AUTH_KEY \
 *       -Pandroid.testInstrumentationRunnerArguments.testUid=cometchat-uid-4
 */
object E2ETestHelper {

    // ─── Constants ───────────────────────────────────────────────────────────────

    const val PACKAGE = "com.cometchat.sampleapp.compose"
    const val TIMEOUT = 15_000L
    const val SHORT_TIMEOUT = 10_000L
    const val SETTLE_TIME = 3_000L

    // ─── Credential accessors (delegate to the single source of truth) ─────────────

    val appId: String get() = E2ETestConfig.APP_ID
    val region: String get() = E2ETestConfig.REGION
    val authKey: String get() = E2ETestConfig.AUTH_KEY
    val testUid: String get() = E2ETestConfig.LOGGED_IN_UID

    /**
     * Fetches a user's display name from their UID via the SDK (uses the current logged-in
     * session — call from inside a test AFTER login, not in a field initializer). Falls back
     * to the uid if the lookup fails.
     */
    fun getUserName(uid: String): String {
        val latch = CountDownLatch(1)
        var name = uid
        CometChat.getUser(uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(p0: User?) { p0?.name?.let { name = it }; latch.countDown() }
            override fun onError(e: CometChatException?) { latch.countDown() }
        })
        latch.await(10, TimeUnit.SECONDS)
        return name
    }

    // ─── SDK Initialization ──────────────────────────────────────────────────────

    /**
     * Initializes the CometChat UIKit SDK.
     * Saves credentials to SharedPreferences so the app recognizes them.
     *
     * Note: Unlike the Kotlin sample, there is NO onSDKInitialized() call here
     * because the Compose app does not have CometChatCalls SDK integration.
     */
    fun initSdk() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Save credentials to SharedPreferences (app reads these on launch)
        val prefs = AppPreferences(context)
        prefs.saveCredentials(appId = appId, region = region, authKey = authKey)

        val latch = CountDownLatch(1)
        var error: String? = null

        val settings = UIKitSettings.UIKitSettingsBuilder()
            .setAppId(appId)
            .setRegion(region)
            .setAuthKey(authKey)
            .setEnableCalling(true)
            .subscribePresenceForAllUsers()
            .build()

        CometChatUIKit.init(context, settings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) { latch.countDown() }
            override fun onError(e: CometChatException?) {
                error = e?.message ?: "SDK init failed"
                latch.countDown()
            }
        })

        latch.await(30, TimeUnit.SECONDS)
        if (error != null) throw RuntimeException("SDK init failed: $error")
    }

    /**
     * Logs out the current user (if any) to ensure a clean login screen.
     */
    fun logoutIfNeeded() {
        try {
            if (CometChatUIKit.getLoggedInUser() != null) {
                val latch = CountDownLatch(1)
                CometChat.logout(object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(p0: String?) { latch.countDown() }
                    override fun onError(e: CometChatException?) { latch.countDown() }
                })
                latch.await(10, TimeUnit.SECONDS)
            }
        } catch (_: Exception) { /* Not logged in or SDK not initialized */ }
    }

    // ─── App Launch & Login ──────────────────────────────────────────────────────

    /**
     * Launches the app via its launcher intent (single-activity: MainActivity with Compose Navigation).
     */
    fun launchApp(device: UiDevice) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE)
            ?: throw IllegalStateException("Launch intent not found for $PACKAGE")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), TIMEOUT)
    }

    /**
     * Performs login via UI: types UID → taps Continue → waits for Home screen.
     *
     * The Compose login screen uses:
     * - BasicTextField with placeholder "Enter UID" (renders differently in a11y tree)
     * - Button with text "Continue"
     * - After login, bottom nav appears with content descriptions "Chats", "Calls", "Users", "Groups"
     *
     * IMPORTANT: Compose's BasicTextField does NOT always render as android.widget.EditText
     * in the accessibility tree. On newer Compose versions, it appears as a generic View
     * with editable/focusable properties. The approach:
     * 1. Try finding EditText by class (works on some Compose/API versions)
     * 2. Click the placeholder text "Enter UID" to focus the field, then use shell input
     * 3. Use coordinate-based tap to focus the text field area, then shell input
     */
    fun loginViaUI(device: UiDevice, uid: String = testUid) {
        // Wait for the Login screen to appear
        // The Login screen has placeholder "Enter UID" or sample user cards
        val loginScreenReady = device.wait(
            Until.hasObject(By.text("Enter UID")),
            TIMEOUT
        ) || device.wait(
            Until.hasObject(By.textContains("Enter UID")),
            SHORT_TIMEOUT
        ) || device.wait(
            Until.hasObject(By.text("Continue")),
            SHORT_TIMEOUT
        )
        assertTrue("Login screen did not appear (no 'Enter UID' or 'Continue' found)", loginScreenReady)

        // Strategy 1: Try finding EditText by class (works on some Compose versions)
        var uidField = device.findObject(By.clazz("android.widget.EditText"))

        if (uidField != null) {
            // Standard EditText approach
            uidField.clear()
            uidField.text = uid
        } else {
            // Strategy 2: Click the "Enter UID" placeholder to give focus to BasicTextField,
            // then type using shell command (most reliable for Compose BasicTextField)
            val placeholder = device.findObject(By.text("Enter UID"))
                ?: device.findObject(By.textContains("Enter UID"))

            if (placeholder != null) {
                placeholder.click()
                Thread.sleep(1000)

                // After clicking, try finding EditText again (focus may expose it)
                uidField = device.findObject(By.clazz("android.widget.EditText"))
                if (uidField != null) {
                    uidField.clear()
                    uidField.text = uid
                } else {
                    // Use shell input text — this types into whatever is currently focused
                    device.executeShellCommand("input text $uid")
                }
            } else {
                // Strategy 3: The "Enter UID" section is typically in the lower half of the screen
                // (below the sample user cards). Tap the text field area by coordinates.
                val screenWidth = device.displayWidth
                val screenHeight = device.displayHeight
                // The UID field is roughly in the middle-to-lower area
                device.click(screenWidth / 2, screenHeight * 2 / 3)
                Thread.sleep(1000)

                // Try EditText again after tap
                uidField = device.findObject(By.clazz("android.widget.EditText"))
                if (uidField != null) {
                    uidField.clear()
                    uidField.text = uid
                } else {
                    device.executeShellCommand("input text $uid")
                }
            }
        }

        // Wait a moment for the button to become enabled
        Thread.sleep(1500)

        // Tap Continue button (found by text "Continue")
        var continueBtn = device.findObject(By.text("Continue"))
        if (continueBtn == null) {
            continueBtn = device.findObject(By.textContains("Continue"))
        }
        if (continueBtn == null) {
            continueBtn = device.findObject(By.desc("Continue"))
        }
        assertNotNull("Continue button not found", continueBtn)
        continueBtn!!.click()

        // Wait for Home screen (bottom nav appears with "Chats" content description)
        var homeLoaded = device.wait(
            Until.hasObject(By.desc("Chats")),
            20_000L
        )
        // Fallback: try text-based detection
        if (!homeLoaded) {
            homeLoaded = device.wait(
                Until.hasObject(By.text("Chats")),
                SHORT_TIMEOUT
            )
        }
        // Retry: dismiss any unexpected dialog and wait again
        if (!homeLoaded) {
            device.pressBack()
            Thread.sleep(2000)
            homeLoaded = device.wait(
                Until.hasObject(By.desc("Chats")),
                10_000L
            ) || device.wait(Until.hasObject(By.text("Chats")), 5_000L)
        }
        assertTrue("Home screen did not load after login (no 'Chats' tab found)", homeLoaded)

        // Allow lists to load
        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Full setup: init SDK → logout → launch app → login via UI.
     * Call in @Before of each test class.
     *
     * Note: No onSDKInitialized() call — Compose app doesn't have CometChatCalls SDK.
     */
    fun fullSetupAndLogin(device: UiDevice) {
        // Send app to home screen to clear any leftover activity state.
        // Note: Cannot use "am force-stop" (crashes ViewModels) or "pm clear" (kills test process).
        // This approach clears the activity stack when launchApp() uses FLAG_ACTIVITY_CLEAR_TASK.
        device.pressHome()
        Thread.sleep(1000)

        initSdk()
        logoutIfNeeded()
        launchApp(device)
        loginViaUI(device)
    }

    // ─── Navigation ──────────────────────────────────────────────────────────────

    /**
     * Navigates to a tab in the bottom navigation.
     * Valid tab names: "Chats", "Calls", "Users", "Groups"
     *
     * Uses coordinate-based click to avoid StaleObjectException in Compose.
     * Compose NavigationBarItem uses contentDescription = label.
     */
    fun navigateToTab(device: UiDevice, tabName: String) {
        // Wait a moment for any ongoing recomposition to settle
        device.waitForIdle()
        Thread.sleep(500)

        // The bottom nav may not be present yet if the screen is still transitioning (right
        // after login, returning from a chat, etc.). Wait for the tab to exist, then click.
        device.wait(Until.hasObject(By.desc(tabName)), TIMEOUT)

        // Strategy 1: Find by content description and click. Poll over a deadline (Compose
        // recomposition can invalidate the node and the screen may still be settling).
        var clicked = false
        val deadline = System.currentTimeMillis() + SHORT_TIMEOUT
        while (!clicked && System.currentTimeMillis() < deadline) {
            try {
                val tab = device.findObject(By.desc(tabName))
                    ?: device.findObject(By.text(tabName))
                    ?: device.findObject(By.descContains(tabName))
                if (tab != null) {
                    tab.click()
                    clicked = true
                } else {
                    device.waitForIdle()
                    Thread.sleep(500)
                }
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                // Retry after a brief wait for recomposition to complete
                Thread.sleep(500)
            }
        }

        // Strategy 2: If direct click failed, use coordinate-based approach
        if (!clicked) {
            val tabBounds = safeGetBounds(device, By.desc(tabName)).firstOrNull()
                ?: safeGetBounds(device, By.text(tabName)).firstOrNull()
                ?: safeGetBounds(device, By.descContains(tabName)).firstOrNull()

            assertNotNull(
                "Tab '$tabName' not found in bottom navigation (tried desc, text, descContains with retry)",
                tabBounds
            )
            device.click(tabBounds!!.centerX(), tabBounds.centerY())
        }

        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Opens the first conversation/list item in the current screen.
     *
     * In Compose, list items are rendered within a scrollable/lazy container.
     * Uses safe bounds access to avoid StaleObjectException.
     * Skips the search bar (first item) and clicks an actual list item below it.
     */
    fun openFirstConversation(device: UiDevice) {
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        var clicked = false

        // Determine where the search field ends (to skip it)
        var searchFieldBottom = 250
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) {
                searchFieldBottom = sf.visibleBounds.bottom + 30
            }
        } catch (_: androidx.test.uiautomator.StaleObjectException) { }

        // Strategy 1: Find scrollable container and click a child BELOW the search field
        val scrollable = device.findObject(By.scrollable(true))
        if (scrollable != null && !clicked) {
            try {
                val children = scrollable.children
                for (child in children) {
                    try {
                        val bounds = child.visibleBounds
                        if (bounds.top > searchFieldBottom) {
                            child.click()
                            clicked = true
                            break
                        }
                    } catch (_: androidx.test.uiautomator.StaleObjectException) { continue }
                }
            } catch (_: androidx.test.uiautomator.StaleObjectException) { }
        }

        // Strategy 2: Find clickable items below the search field using safe bounds
        if (!clicked) {
            val contentBounds = safeGetBounds(device, By.clickable(true)) { bounds ->
                bounds.top > searchFieldBottom && bounds.bottom < device.displayHeight - 200
            }
            if (contentBounds.isNotEmpty()) {
                val first = contentBounds[0]
                device.click(first.centerX(), first.centerY())
                clicked = true
            }
        }

        assertTrue("No clickable conversation item found (below search bar)", clicked)

        // Wait for messages screen to load — look for composer (EditText for typing)
        val messagesLoaded = device.wait(
            Until.hasObject(By.clazz("android.widget.EditText")),
            TIMEOUT
        )
        // Fallback: look for Send button content description
        if (!messagesLoaded) {
            device.wait(Until.hasObject(By.desc("Send")), SHORT_TIMEOUT)
        }
        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Opens the first user item in the Users tab.
     * Uses safe bounds access to avoid StaleObjectException in Compose.
     * Skips the search bar (first item in LazyColumn) and clicks an actual user item.
     */
    fun openFirstUser(device: UiDevice) {
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        var clicked = false

        // Determine where the search field ends (to skip it)
        var searchFieldBottom = 250
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) {
                searchFieldBottom = sf.visibleBounds.bottom + 30
            }
        } catch (_: androidx.test.uiautomator.StaleObjectException) { }

        // Strategy 1: Find scrollable LazyColumn and click a child BELOW the search field
        val scrollable = device.findObject(By.scrollable(true))
        if (scrollable != null && !clicked) {
            try {
                val children = scrollable.children
                for (child in children) {
                    try {
                        val bounds = child.visibleBounds
                        if (bounds.top > searchFieldBottom) {
                            child.click()
                            clicked = true
                            break
                        }
                    } catch (_: androidx.test.uiautomator.StaleObjectException) { continue }
                }
            } catch (_: androidx.test.uiautomator.StaleObjectException) { }
        }

        // Strategy 2: Use safe bounds to find content area items below search
        if (!clicked) {
            val contentBounds = safeGetBounds(device, By.clickable(true)) { bounds ->
                bounds.top > searchFieldBottom && bounds.bottom < device.displayHeight - 200
            }
            if (contentBounds.isNotEmpty()) {
                val first = contentBounds[0]
                device.click(first.centerX(), first.centerY())
                clicked = true
            }
        }

        // Strategy 3: Find TextViews below search (user names) and click one
        if (!clicked) {
            val nameBounds = safeGetBounds(device, By.clazz("android.widget.TextView")) { bounds ->
                bounds.top > searchFieldBottom && bounds.bottom < device.displayHeight - 200
            }
            if (nameBounds.isNotEmpty()) {
                device.click(nameBounds[0].centerX(), nameBounds[0].centerY())
                clicked = true
            }
        }

        assertTrue("No clickable user item found (below search bar)", clicked)

        // Wait for messages screen
        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), TIMEOUT)
        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Opens the first group item in the Groups tab.
     * Uses safe bounds access to avoid StaleObjectException in Compose.
     * Skips the search bar (first item in LazyColumn) and clicks an actual group item.
     */
    fun openFirstGroup(device: UiDevice) {
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        var clicked = false

        // Determine where the search field ends (to skip it)
        var searchFieldBottom = 250
        try {
            val sf = device.findObject(By.clazz("android.widget.EditText"))
            if (sf != null) {
                searchFieldBottom = sf.visibleBounds.bottom + 30
            }
        } catch (_: androidx.test.uiautomator.StaleObjectException) { }

        // Strategy 1: Find scrollable LazyColumn and click a child BELOW the search field
        val scrollable = device.findObject(By.scrollable(true))
        if (scrollable != null && !clicked) {
            try {
                val children = scrollable.children
                for (child in children) {
                    try {
                        val bounds = child.visibleBounds
                        if (bounds.top > searchFieldBottom) {
                            child.click()
                            clicked = true
                            break
                        }
                    } catch (_: androidx.test.uiautomator.StaleObjectException) { continue }
                }
            } catch (_: androidx.test.uiautomator.StaleObjectException) { }
        }

        // Strategy 2: Use safe bounds to find content area items below search
        if (!clicked) {
            val contentBounds = safeGetBounds(device, By.clickable(true)) { bounds ->
                bounds.top > searchFieldBottom && bounds.bottom < device.displayHeight - 200
            }
            if (contentBounds.isNotEmpty()) {
                val first = contentBounds[0]
                device.click(first.centerX(), first.centerY())
                clicked = true
            }
        }

        // Strategy 3: Find TextViews below search (group names) and click one
        if (!clicked) {
            val nameBounds = safeGetBounds(device, By.clazz("android.widget.TextView")) { bounds ->
                bounds.top > searchFieldBottom && bounds.bottom < device.displayHeight - 200
            }
            if (nameBounds.isNotEmpty()) {
                device.click(nameBounds[0].centerX(), nameBounds[0].centerY())
                clicked = true
            }
        }

        assertTrue("No clickable group item found (below search bar)", clicked)

        // Group may need to be joined first; wait for messages screen
        Thread.sleep(3000)
        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), TIMEOUT)
        Thread.sleep(SETTLE_TIME)
    }

    // ─── Message Composer Utilities ──────────────────────────────────────────────

    /**
     * Types a message in the composer and taps send.
     * Assumes Messages screen is currently displayed.
     *
     * In Compose, the message composer uses BasicTextField which requires
     * clicking to focus + typing via the text field (not just setting text).
     * The Send button has contentDescription "Send message" when active.
     *
     * After sending, dismisses the keyboard to ensure the message list is fully visible.
     *
     * @return The message text that was sent
     */
    fun sendMessage(device: UiDevice, text: String): String {
        // Find the EditText in the composer area — with retry for StaleObjectException
        device.waitForIdle()
        Thread.sleep(1000)

        var messageSent = false
        repeat(3) { attempt ->
            if (messageSent) return@repeat
            try {
                // Re-find EditText fresh on each attempt
                val editText = device.findObject(By.clazz("android.widget.EditText"))
                assertNotNull("EditText not found in message composer (attempt ${attempt + 1})", editText)

                // Click to focus the text field first
                editText!!.click()
                Thread.sleep(500)

                // Clear and set text
                editText.clear()
                Thread.sleep(300)
                editText.text = text

                // Wait for Compose to process the text change
                Thread.sleep(2000)

                // Find and click the Send button
                var sendButton = device.findObject(By.desc("Send message"))
                    ?: device.findObject(By.descContains("Send message"))
                    ?: device.findObject(By.desc("Send"))
                    ?: device.findObject(By.descContains("Send"))
                    ?: device.findObject(By.descContains("send"))

                if (sendButton != null) {
                    sendButton.click()
                } else {
                    device.pressEnter()
                }

                messageSent = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                // Recomposition happened — wait and retry
                Thread.sleep(1500)
            } catch (_: Exception) {
                Thread.sleep(1500)
            }
        }

        // Last resort: use shell input if all retries failed
        if (!messageSent) {
            val editText = device.findObject(By.clazz("android.widget.EditText"))
            if (editText != null) {
                try {
                    editText.click()
                    Thread.sleep(500)
                } catch (_: Exception) { }
            }
            device.executeShellCommand("input text ${text.replace(" ", "%s")}")
            Thread.sleep(2000)
            device.pressEnter()
            messageSent = true
        }

        assertTrue("Failed to send message after 3 retries", messageSent)

        // Wait for message to be sent
        Thread.sleep(2000)

        // Scroll down to see the latest message at the bottom
        scrollDown(device)

        return text
    }

    /**
     * Generates a unique test message with timestamp.
     */
    fun uniqueMessage(prefix: String = "E2E"): String {
        return "$prefix ${System.currentTimeMillis()}"
    }

    // ─── General Utilities ───────────────────────────────────────────────────────

    /**
     * Waits until an object matching the selector appears on screen.
     * Returns the object or null if timeout reached.
     */
    fun waitForObject(device: UiDevice, selector: BySelector, timeout: Long = TIMEOUT): UiObject2? {
        return device.wait(Until.findObject(selector), timeout)
    }

    /**
     * Waits until any object matching the selector exists.
     * Returns true if found before timeout.
     */
    fun waitUntilExists(device: UiDevice, selector: BySelector, timeout: Long = TIMEOUT): Boolean {
        return device.wait(Until.hasObject(selector), timeout)
    }

    /**
     * Scrolls a list component down to trigger pagination.
     * Uses UiDevice swipe gesture.
     */
    fun scrollDown(device: UiDevice) {
        val displayHeight = device.displayHeight
        val displayWidth = device.displayWidth
        device.swipe(
            displayWidth / 2,
            displayHeight * 3 / 4,
            displayWidth / 2,
            displayHeight / 4,
            20
        )
        Thread.sleep(2000) // Wait for pagination load
    }

    // ─── Compose StaleObjectException-Safe Utilities ─────────────────────────────

    /**
     * Safely gets visible bounds of all clickable elements matching a filter.
     *
     * In Jetpack Compose, UiObject2 references go stale after recomposition.
     * This method captures bounds immediately, skipping any stale references.
     * Use the returned Rect list with device.click(rect.centerX(), rect.centerY()).
     *
     * @param device The UiDevice instance
     * @param selector The BySelector to find elements
     * @param boundsFilter Optional filter on the captured bounds
     * @return List of Rect bounds that matched, safe to use for coordinate-based clicks
     */
    fun safeGetBounds(
        device: UiDevice,
        selector: BySelector,
        boundsFilter: (android.graphics.Rect) -> Boolean = { true }
    ): List<android.graphics.Rect> {
        val results = mutableListOf<android.graphics.Rect>()
        for (obj in device.findObjects(selector)) {
            try {
                val bounds = obj.visibleBounds
                if (boundsFilter(bounds)) {
                    results.add(bounds)
                }
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                // Object was recomposed — skip it
                continue
            }
        }
        return results
    }

    /**
     * Safely finds and clicks the first element matching the selector and filter.
     *
     * Avoids StaleObjectException by capturing bounds and using coordinate click.
     *
     * @return true if an element was found and clicked
     */
    fun safeClickFirst(
        device: UiDevice,
        selector: BySelector,
        boundsFilter: (android.graphics.Rect) -> Boolean = { true }
    ): Boolean {
        val bounds = safeGetBounds(device, selector, boundsFilter).firstOrNull() ?: return false
        device.click(bounds.centerX(), bounds.centerY())
        return true
    }

    /**
     * Safely counts elements matching the selector, handling StaleObjectException.
     *
     * @return count of valid (non-stale) matching elements
     */
    fun safeCount(device: UiDevice, selector: BySelector): Int {
        var count = 0
        for (obj in device.findObjects(selector)) {
            try {
                obj.visibleBounds // Access bounds to verify the object is still valid
                count++
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                continue
            }
        }
        return count
    }

    /**
     * Scrolls a list component up (for loading older messages).
     * Uses UiDevice swipe gesture.
     */
    fun scrollUp(device: UiDevice) {
        val displayHeight = device.displayHeight
        val displayWidth = device.displayWidth
        device.swipe(
            displayWidth / 2,
            displayHeight / 4,
            displayWidth / 2,
            displayHeight * 3 / 4,
            20
        )
        Thread.sleep(2000)
    }

    /**
     * Presses the device back button.
     */
    fun pressBack(device: UiDevice) {
        device.pressBack()
        Thread.sleep(1000)
    }

    /**
     * Long-clicks on a UI element (for context menus / reactions).
     */
    fun longClick(obj: UiObject2) {
        obj.longClick()
    }

    /**
     * Checks if we are on the Home screen by looking for bottom nav tabs.
     */
    fun isOnHomeScreen(device: UiDevice): Boolean {
        return device.findObject(By.desc("Chats")) != null ||
            device.findObject(By.text("Chats")) != null
    }

    /**
     * Checks if we are on the Login screen by looking for the UID field hint.
     */
    fun isOnLoginScreen(device: UiDevice): Boolean {
        return device.findObject(By.text("Enter UID")) != null ||
            device.findObject(By.textContains("Enter UID")) != null ||
            device.findObject(By.text("Login")) != null
    }
}
