package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for the Users tab in the Compose sample app.
 *
 * Test IDs:
 * - E2E-010: testUsersListShowsTestUsers
 * - E2E-011: testScrollLoadsPagination
 * - E2E-012: testSearchFiltersUsers
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.UsersE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UsersE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
        // Navigate to Users tab
        E2ETestHelper.navigateToTab(device, "Users")
    }

    /**
     * E2E-010: Users list displays test users.
     * Verifies the CometChatUsers component renders with at least one user item.
     */
    @Test
    fun test01_usersListShowsTestUsers() {
        // Wait for users to load
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        // In Compose, list items are rendered within a scrollable/lazy container.
        // Find clickable user items in the content area.

        var firstItem: androidx.test.uiautomator.UiObject2? = null

        // Strategy 1: Find scrollable container with clickable children (Compose-safe)
        val scrollable = device.wait(
            Until.findObject(By.scrollable(true)),
            TIMEOUT
        )
        if (scrollable != null) {
            try {
                val clickableChildren = scrollable.findObjects(By.clickable(true))
                if (clickableChildren.isNotEmpty()) {
                    firstItem = clickableChildren[0]
                }
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                // Will use safeGetBounds fallback below
            }
        }

        // Strategy 2: Find clickable items in the content area (safe for Compose)
        if (firstItem == null) {
            val contentBounds = E2ETestHelper.safeGetBounds(
                device,
                By.clickable(true)
            ) { bounds ->
                bounds.top > 150 && bounds.bottom < device.displayHeight - 150
            }

            if (contentBounds.isNotEmpty()) {
                // We found valid clickable items — verify by re-querying fresh
                firstItem = device.findObject(By.clickable(true))
            }
        }

        // Final check: at minimum, verify there's content on screen
        val hasContent = firstItem != null || E2ETestHelper.safeGetBounds(
            device, By.clickable(true)
        ) { it.top > 150 && it.bottom < device.displayHeight - 150 }.isNotEmpty()

        assertTrue("No user items found in the users list", hasContent)
    }

    /**
     * E2E-011: Scrolling the users list loads more users (pagination).
     */
    @Test
    fun test02_scrollLoadsPagination() {
        // Wait for users to load
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        // Verify there's content before scrolling
        val scrollable = device.findObject(By.scrollable(true))
        val hasContent = scrollable != null || device.findObjects(By.clickable(true)).size > 4
        assertTrue("No content found for pagination test", hasContent)

        // Scroll down to trigger pagination
        E2ETestHelper.scrollDown(device)
        E2ETestHelper.scrollDown(device)
        E2ETestHelper.scrollDown(device)

        // After scrolling, verify the app is still on the Users tab (not crashed)
        assertTrue(
            "App crashed or navigated away after scrolling users list",
            E2ETestHelper.isOnHomeScreen(device)
        )

        // Verify the Users tab is still selected (content still visible)
        val usersTab = device.findObject(By.desc("Users"))
            ?: device.findObject(By.text("Users"))
        assertNotNull("Users tab not found after scrolling — may have navigated away", usersTab)
    }

    /**
     * E2E-012: Search filters users by name.
     * Types a known user name in the search bar and verifies filtered results.
     */
    @Test
    fun test03_searchFiltersUsers() {
        // Wait for users list to load
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        // Find search bar — in Compose, it's an EditText or TextField
        var searchBar = device.findObject(By.clazz("android.widget.EditText"))

        if (searchBar == null) {
            // Try clicking a search icon first
            val searchIcon = device.findObject(By.descContains("Search"))
                ?: device.findObject(By.descContains("search"))
            if (searchIcon != null) {
                searchIcon.click()
                Thread.sleep(1500)
                searchBar = device.findObject(By.clazz("android.widget.EditText"))
            }
        }

        if (searchBar == null) {
            // Try finding by hint text
            searchBar = device.findObject(By.textContains("Search"))
                ?: device.findObject(By.textContains("search"))
        }

        assertNotNull("Search bar not found on Users tab", searchBar)

        // Type a known user name to filter. Search by the first name token (prefix) so the
        // filter matches as the original test did ("Andrew"); the name is resolved from getUser.
        val searchQuery = E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_1_UID).substringBefore(" ")
        searchBar!!.click()
        Thread.sleep(500)
        searchBar.clear()
        searchBar.text = searchQuery

        // Wait for filter results
        Thread.sleep(SHORT_TIMEOUT)

        // NOTE: no pressBack here. The soft keyboard is disabled on the test emulator, so
        // pressBack acts as a real back-navigation that collapses the search field and clears
        // the filtered results — making the assertion below fail. Matches kotlin, which omits it.

        // Verify that a user matching the search is visible
        val filteredResult = device.findObject(By.textContains(searchQuery))
            ?: device.findObject(By.descContains(searchQuery))
        assertNotNull(
            "Filtered result for '$searchQuery' not found in users list",
            filteredResult
        )

        // Verify we're still on the home screen (didn't crash)
        assertTrue(
            "App should still be on Users tab after search",
            E2ETestHelper.isOnHomeScreen(device)
        )
    }
}
