package com.cometchat.sampleapp.kotlin.e2e.helpers

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
import com.cometchat.sampleapp.kotlin.utils.AppPreferences
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * UI Automator-based E2E test helper for sample-app-kotlin.
 *
 * Provides SDK initialization, login via UI, navigation, and
 * reusable wait/find utilities using UI Automator (no Espresso).
 *
 * Credentials can be passed via instrumentation arguments for CI/CD:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.appId=YOUR_APP_ID \
 *       -Pandroid.testInstrumentationRunnerArguments.region=in \
 *       -Pandroid.testInstrumentationRunnerArguments.authKey=YOUR_AUTH_KEY \
 *       -Pandroid.testInstrumentationRunnerArguments.testUid=cometchat-uid-4
 */
object E2ETestHelper {

    // ─── Constants ───────────────────────────────────────────────────────────────

    const val PACKAGE = "com.cometchat.sampleapp.kotlin"
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
     * Initializes the CometChat UIKit SDK (with calling enabled).
     * Saves credentials to SharedPreferences so the app recognizes them.
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
     * Launches the app via its launcher intent (SplashActivity → LoginActivity).
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
     * Assumes LoginActivity is currently displayed (call logoutIfNeeded + launchApp first).
     */
    fun loginViaUI(device: UiDevice, uid: String = testUid) {
        // Wait for the UID field to appear (LoginActivity loaded)
        val uidField = device.wait(
            Until.findObject(By.res(PACKAGE, "etUid")),
            TIMEOUT
        )
        assertNotNull("Login UID field (etUid) not found", uidField)

        // Clear any existing text and type the UID
        uidField!!.clear()
        uidField.text = uid

        // Tap Continue button
        val continueBtn = device.findObject(By.res(PACKAGE, "btnContinue"))
        assertNotNull("Continue button (btnContinue) not found", continueBtn)
        continueBtn!!.click()

        // Wait for Home screen (bottom nav appears). Login is a network round-trip, so be
        // generous and retry (dismissing any stray dialog) before giving up — this avoids
        // flaky "home did not load" failures on slow networks.
        var homeLoaded = device.wait(Until.hasObject(By.res(PACKAGE, "bottomNavigationView")), 40_000L)
        var retries = 0
        while (!homeLoaded && retries < 4) {
            retries++
            // If still on the login screen, the Continue tap may have been missed/dropped on a slow
            // network — re-tap it; otherwise dismiss a stray dialog. Then wait again.
            if (device.findObject(By.res(PACKAGE, "etUid")) != null) {
                device.findObject(By.res(PACKAGE, "btnContinue"))?.click()
            } else {
                device.pressBack()
            }
            Thread.sleep(2000)
            homeLoaded = device.wait(Until.hasObject(By.res(PACKAGE, "bottomNavigationView")), 20_000L)
        }
        assertTrue("Home screen (bottomNavigationView) did not load after login", homeLoaded)

        // Allow lists to load
        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Full setup: force-stop → init SDK → trigger Application callbacks → logout → launch app → login via UI.
     * Call in @Before of each test class.
     *
     * Uses `am force-stop` to guarantee a clean app state — no leftover activities,
     * dialogs, or stacked navigation from previous tests.
     */
    fun fullSetupAndLogin(device: UiDevice, uid: String = testUid) {
        // Send app to home screen to clear any leftover activity state.
        // Note: Cannot use "am force-stop" (crashes ViewModels) or "pm clear" (kills test process).
        // This approach clears the activity stack when launchApp() uses FLAG_ACTIVITY_CLEAR_TASK.
        device.pressHome()
        Thread.sleep(1000)

        initSdk()

        // Trigger the Application-level Calls SDK init + call listeners
        val app = ApplicationProvider.getApplicationContext<com.cometchat.sampleapp.kotlin.app.SampleApplication>()
        app.onSDKInitialized()

        logoutIfNeeded()
        launchApp(device)
        device.waitForIdle()
        loginViaUI(device, uid)
    }

    // ─── Navigation ──────────────────────────────────────────────────────────────

    /**
     * Navigates to a tab in the bottom navigation.
     * Valid tab names: "Chats", "Calls", "Users", "Groups"
     *
     * BottomNavigationView renders menu item titles as contentDescription on the
     * item views in the accessibility tree, so we use By.desc() first, then
     * fallback to By.text(), then fallback to resource-id based approach.
     */
    fun navigateToTab(device: UiDevice, tabName: String) {
        val menuItemId = when (tabName) {
            "Chats" -> "nav_chats"
            "Calls" -> "nav_calls"
            "Users" -> "nav_users"
            "Groups" -> "nav_groups"
            else -> null
        }

        // The bottom navigation may not be present yet if the screen is still transitioning
        // (right after login, returning from another activity, etc.). Wait for it to exist,
        // then POLL for the tab — retrying every lookup strategy — instead of failing on the
        // first miss. This removes the timing race without changing the click behavior.
        device.wait(Until.hasObject(By.res(PACKAGE, "bottomNavigationView")), TIMEOUT)

        var tab: UiObject2? = null
        val deadline = System.currentTimeMillis() + SHORT_TIMEOUT
        while (System.currentTimeMillis() < deadline) {
            tab = device.findObject(By.desc(tabName))                                 // Strategy 1: desc
                ?: device.findObject(By.text(tabName))                                // Strategy 2: text
                ?: device.findObject(                                                 // Strategy 3: desc within nav
                    By.descContains(tabName).hasAncestor(By.res(PACKAGE, "bottomNavigationView"))
                )
                ?: menuItemId?.let { device.findObject(By.res(PACKAGE, it)) }          // Strategy 4: res id
            if (tab != null) break
            device.waitForIdle()
            Thread.sleep(500)
        }

        assertNotNull("Tab '$tabName' not found in bottom navigation (tried desc, text, and res)", tab)
        tab!!.click()
        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Opens the first conversation item in the Chats tab.
     * Assumes we're on the Chats tab with conversations loaded.
     *
     * Targets the internal RecyclerView (recyclerview_conversations_list) inside
     * CometChatConversations to avoid clicking the avatar/overflow menu.
     */
    fun openFirstConversation(device: UiDevice) {
        // The CometChatConversations component has an internal RecyclerView
        // with ID "recyclerview_conversations_list" from chatuikit-kotlin.
        // We use the UIKit's internal resource ID namespace.
        val uikitPackage = "com.cometchat.uikit.kotlin"

        // Wait for a clickable item inside the internal RecyclerView
        var item = device.wait(
            Until.findObject(
                By.clickable(true).hasAncestor(By.res(uikitPackage, "recyclerview_conversations_list"))
            ),
            TIMEOUT
        )

        // Fallback: try with the app package (resource IDs can be merged)
        if (item == null) {
            item = device.wait(
                Until.findObject(
                    By.clickable(true).hasAncestor(By.res(PACKAGE, "recyclerview_conversations_list"))
                ),
                SHORT_TIMEOUT
            )
        }

        // Fallback: find RecyclerView by class name inside conversationList and get first child
        if (item == null) {
            val recyclerView = device.findObject(
                By.clazz("androidx.recyclerview.widget.RecyclerView")
                    .hasAncestor(By.res(PACKAGE, "conversationList"))
            )
            if (recyclerView != null) {
                val children = recyclerView.children
                if (children.isNotEmpty()) {
                    item = children[0]
                }
            }
        }

        assertNotNull("No clickable conversation item found in RecyclerView", item)
        item!!.click()

        // Wait for MessagesActivity (messageList component appears)
        val messagesLoaded = device.wait(
            Until.hasObject(By.res(PACKAGE, "messageList")),
            30_000L // generous: chat history load is a network round-trip
        )
        assertTrue("Messages screen did not load after tapping conversation", messagesLoaded)
        waitForComposer(device, 15_000) // composer renders just after the list — wait so tests don't race it
        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Opens the first user item in the Users tab.
     * Assumes we're on the Users tab with users loaded.
     *
     * Targets the internal RecyclerView inside CometChatUsers.
     * Uses multiple strategies because:
     * - Items load asynchronously from the CometChat API
     * - The RecyclerView items may not be marked clickable=true in the accessibility tree
     *   (the parentLayout INSIDE the item is clickable, not the item root itself)
     * - Resource ID package can be either the UIKit's or the app's namespace
     */
    fun openFirstUser(device: UiDevice) {
        val uikitPackage = "com.cometchat.uikit.kotlin"

        // Wait for the RecyclerView to exist and have children (API fetch can take time).
        var recyclerView: UiObject2? = null
        val rvDeadline = System.currentTimeMillis() + 30_000L
        while (System.currentTimeMillis() < rvDeadline) {
            recyclerView = device.findObject(By.res(uikitPackage, "recyclerview_users_list"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_users_list"))
            if (recyclerView != null && recyclerView.children.isNotEmpty()) {
                break
            }
            Thread.sleep(2000)
        }

        // Wait for idle to ensure list is fully rendered
        device.waitForIdle()

        var item: UiObject2? = null

        // Strategy 1: Find clickable items inside the RecyclerView (UIKit package)
        if (item == null) {
            item = device.findObject(
                By.clickable(true).hasAncestor(By.res(uikitPackage, "recyclerview_users_list"))
            )
        }

        // Strategy 2: Find clickable items inside the RecyclerView (app package)
        if (item == null) {
            item = device.findObject(
                By.clickable(true).hasAncestor(By.res(PACKAGE, "recyclerview_users_list"))
            )
        }

        // Strategy 3: Get the first child of the RecyclerView directly
        // (items may not be individually clickable in accessibility — the parentLayout inside is)
        if (item == null && recyclerView != null) {
            val children = recyclerView.children
            if (children.isNotEmpty()) {
                item = children[0]
            }
        }

        // Strategy 4: Find any RecyclerView on screen that has child items with user names
        if (item == null) {
            val allRecyclers = device.findObjects(
                By.clazz("androidx.recyclerview.widget.RecyclerView")
            )
            for (rv in allRecyclers) {
                val children = rv.children
                if (children.size > 1) { // Skip shimmer/single items
                    // Check if children contain TextViews (user names)
                    val firstChild = children[0]
                    val textViews = firstChild.findObjects(By.clazz("android.widget.TextView"))
                    if (textViews.isNotEmpty() && !textViews[0].text.isNullOrBlank()) {
                        item = firstChild
                        break
                    }
                }
            }
        }

        // Strategy 5: Look for any TextView that looks like a user name and click its parent
        if (item == null) {
            // Wait a bit more for items to load
            Thread.sleep(5000)
            recyclerView = device.findObject(By.res(uikitPackage, "recyclerview_users_list"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_users_list"))
            if (recyclerView != null) {
                val children = recyclerView.children
                if (children.isNotEmpty()) {
                    item = children[0]
                }
            }
        }

        assertNotNull("No user item found in Users RecyclerView after 20s+ wait", item)
        item!!.click()

        val messagesLoaded = device.wait(
            Until.hasObject(By.res(PACKAGE, "messageList")),
            30_000L // generous: chat history load is a network round-trip
        )
        assertTrue("Messages screen did not load after tapping user", messagesLoaded)
        waitForComposer(device, 15_000) // composer renders just after the list — wait so tests don't race it
        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Opens a public group item in the Groups tab.
     * Tries each group item in the list — skips private groups that bounce back
     * (ERR_PERMISSION_DENIED) and finds one that successfully opens messages.
     *
     * Assumes we're on the Groups tab with groups loaded.
     */
    fun openFirstGroup(device: UiDevice) {
        val uikitPackage = "com.cometchat.uikit.kotlin"

        // Wait for items to load
        Thread.sleep(SETTLE_TIME)

        // Strategy 1: Find clickable items inside the UIKit RecyclerView
        var items = device.findObjects(
            By.clickable(true).hasAncestor(By.res(uikitPackage, "recyclerview_groups_list"))
        )

        // Strategy 2: Find clickable items via app package
        if (items.isEmpty()) {
            items = device.findObjects(
                By.clickable(true).hasAncestor(By.res(PACKAGE, "recyclerview_groups_list"))
            )
        }

        // Strategy 3: Find RecyclerView by class inside groups_list, then get children
        if (items.isEmpty()) {
            val recyclerView = device.findObject(
                By.clazz("androidx.recyclerview.widget.RecyclerView")
                    .hasAncestor(By.res(PACKAGE, "groups_list"))
            )
            if (recyclerView != null) {
                // Get all children (they may or may not be marked clickable)
                items = recyclerView.children
            }
        }

        // Strategy 4: Fall back to any clickable in the groups_list container area
        if (items.isEmpty()) {
            val groupsList = device.findObject(By.res(PACKAGE, "groups_list"))
            if (groupsList != null) {
                items = groupsList.findObjects(By.clickable(true))
            }
        }

        assertTrue("No group items found in the groups list", items.isNotEmpty())

        // Try each group item — private groups will fail (MessagesActivity finishes),
        // public groups will succeed (messageList appears)
        for (item in items) {
            // A previous bounce-back re-renders the list, so an earlier-collected item ref may be
            // recycled — skip stale nodes instead of crashing the test.
            try {
                item.click()
            } catch (e: androidx.test.uiautomator.StaleObjectException) {
                continue
            }

            // Wait for either messages screen to load or activity to bounce back
            val messagesLoaded = device.wait(
                Until.hasObject(By.res(PACKAGE, "messageList")),
                10_000L
            )

            if (messagesLoaded) {
                // Successfully opened a public/joined group
                waitForComposer(device, 15_000) // wait for the composer so callers don't race it
                Thread.sleep(SETTLE_TIME)
                return
            }

            // If we're back on groups (activity finished), try the next one
            Thread.sleep(2000)
            val stillOnGroups = device.findObject(By.res(PACKAGE, "groups_list"))
            if (stillOnGroups != null) {
                continue // Try next group
            }
        }

        // If none worked, fail with a clear message
        assertTrue(
            "No public/accessible group found that opens messages. " +
                "All groups may be private with RBAC restrictions.",
            false
        )
    }

    /**
     * Opens a group by (partial) name from the Groups tab. Searches for it, then clicks the
     * clickable ROW (not the bare text node — tapping the text alone often doesn't open the chat),
     * waits for the messages screen, retrying the tap, and finally waits for the composer.
     */
    fun openGroupByName(device: UiDevice, name: String) {
        val uikitPackage = "com.cometchat.uikit.kotlin"
        navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        // Filter the list down to the target group via search.
        val search = device.findObject(By.clazz("android.widget.EditText"))
        search?.clear()
        search?.text = name
        Thread.sleep(5000)

        var loaded = false
        var attempt = 0
        while (!loaded && attempt < 3) {
            attempt++
            // Click the first clickable ROW INSIDE the groups RecyclerView (the filtered result).
            // Scoping to the RecyclerView is essential: the typed name also lives in the search
            // field, so an unscoped text/clickable match would click the search box, not the group.
            var items = device.findObjects(
                By.clickable(true).hasAncestor(By.res(uikitPackage, "recyclerview_groups_list"))
            )
            if (items.isEmpty()) {
                items = device.findObjects(
                    By.clickable(true).hasAncestor(By.res(PACKAGE, "recyclerview_groups_list"))
                )
            }
            val row = items.firstOrNull()
            if (row != null) {
                try { row.click() } catch (_: Exception) { }
            }
            loaded = device.wait(Until.hasObject(By.res(PACKAGE, "messageList")), 15_000L)
            if (!loaded) Thread.sleep(1500)
        }
        assertTrue("Messages screen did not load for group \"$name\"", loaded)
        waitForComposer(device, 15_000)
        Thread.sleep(SETTLE_TIME)
    }

    // ─── Message Composer Utilities ──────────────────────────────────────────────

    /**
     * Types a message in the composer and taps send.
     * Assumes MessagesActivity is currently displayed.
     *
     * @return The message text that was sent
     */
    /**
     * Polls for the message composer to render after a chat is opened. Opening a chat (e.g. via
     * [openFirstUser]) starts an async screen load, so an immediate findObject often misses the
     * composer — this waits for it, eliminating "Message composer not found" flakiness.
     */
    fun waitForComposer(device: UiDevice, timeoutMs: Long = TIMEOUT): UiObject2? =
        device.wait(Until.findObject(By.res(PACKAGE, "messageComposer")), timeoutMs)

    fun sendMessage(device: UiDevice, text: String): String {
        val composer = waitForComposer(device)
        assertNotNull("Message composer not found", composer)

        val editText = composer!!.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("EditText not found inside message composer", editText)

        editText!!.text = text

        // Wait for send button to become available, then click
        Thread.sleep(1000)
        val sendButton = device.wait(Until.findObject(By.desc("Send")), SHORT_TIMEOUT)
            ?: composer.findObject(By.clazz("android.widget.ImageView").clickable(true))
        assertNotNull("Send button not found", sendButton)
        sendButton!!.click()

        return text
    }

    /**
     * Generates a unique test message with timestamp.
     */
    fun uniqueMessage(prefix: String = "E2E"): String {
        return "$prefix ${System.currentTimeMillis()}"
    }

    // ─── Message Long-Press ──────────────────────────────────────────────────────

    /**
     * Long-presses on a message bubble to trigger the popup menu.
     *
     * The UIKit's MessageAdapter sets `setOnLongClickListener` on the `rowRoot`
     * LinearLayout — the direct child of the message RecyclerView
     * (id: recyclerview_message_list). This rowRoot has MATCH_PARENT width and
     * wraps the CometChatMessageBubble.
     *
     * Strategy (in order of reliability):
     * 1. Find the RecyclerView item (rowRoot) that contains the message text,
     *    then long-click on it directly since it's the longClickable target.
     * 2. If that doesn't work, use OS-level `input touchscreen swipe` with same
     *    start/end coordinates and 2-second duration — this produces a long-press
     *    event at the kernel input layer, bypassing any UI Automator gesture issues.
     *
     * After long-press, waits for the popup to appear (CometChatMessagePopupMenu
     * shows a full-screen PopupWindow with blurred background).
     */
    fun longPressMessage(device: UiDevice, messageText: String) {
        // Find the text element containing our message
        val textNode = device.findObject(By.textContains(messageText))
        assertNotNull("Message text '$messageText' not found on screen for long-press", textNode)

        // The UIKit's MessageAdapter sets OnLongClickListener on the rowRoot (LinearLayout,
        // MATCH_PARENT width, direct child of RecyclerView). The touch must land on the
        // rowRoot's padding area (not on a child MaterialCardView that might consume it).
        // For RECEIVED messages (left-aligned), long-pressing ON the text doesn't work because
        // the text view itself may consume the touch. Instead, press slightly ABOVE the text
        // (on the sender name / header area of the bubble) which is still inside the rowRoot.
        val bounds = textNode!!.visibleBounds
        val targetY = bounds.top - 30 // Above the text, on the bubble header (sender name area)
        val rowCenterX = device.displayWidth / 2

        // Ensure targetY is not negative (for messages very near the top of screen)
        val safeY = if (targetY > 50) targetY else bounds.centerY()

        // Long-press via OS-level shell command: same start/end = long press, 3s duration
        device.executeShellCommand("input touchscreen swipe $rowCenterX $safeY $rowCenterX $safeY 3000")
        Thread.sleep(2500)
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

    /**
     * Presses the device back button.
     */
    fun pressBack(device: UiDevice) {
        device.pressBack()
        Thread.sleep(1000)
    }

    /**
     * Long-clicks on a UI element (for context menus).
     */
    fun longClick(obj: UiObject2) {
        obj.longClick()
    }
}
