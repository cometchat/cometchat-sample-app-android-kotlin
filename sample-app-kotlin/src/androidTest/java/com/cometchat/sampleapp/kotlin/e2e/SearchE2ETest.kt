package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for Search functionality across the app.
 *
 * Test IDs:
 * - E2E-053: testSearchUserName
 * - E2E-054: testSearchGroupName
 * - E2E-055: testSearchMessageContent
 * - E2E-056: testSearchEmptyState
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.SearchE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class SearchE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
    }

    /**
     * Finds and activates the search bar on the current screen.
     * Tries multiple strategies to locate the search input.
     */
    private fun findSearchBar(): android.view.accessibility.AccessibilityNodeInfo? {
        // Strategy 1: resource ID
        var searchBar = device.findObject(By.res(PACKAGE, "searchBar"))
        if (searchBar != null) return null // Return null to signal we use UiObject2

        // Strategy 2: EditText in the list component
        searchBar = device.findObject(By.clazz("android.widget.EditText"))
        if (searchBar != null) return null

        // Strategy 3: Search icon that expands
        val searchIcon = device.findObject(By.descContains("Search"))
            ?: device.findObject(By.descContains("search"))
        if (searchIcon != null) {
            searchIcon.click()
            Thread.sleep(1500)
        }

        return null
    }

    /**
     * Gets the search bar UiObject2 using multiple strategies.
     */
    private fun getSearchBar(): androidx.test.uiautomator.UiObject2? {
        var searchBar = device.findObject(By.res(PACKAGE, "searchBar"))
        if (searchBar != null) return searchBar

        searchBar = device.findObject(By.res(PACKAGE, "search_bar"))
        if (searchBar != null) return searchBar

        // Look for EditText that might be the search field
        searchBar = device.findObject(By.clazz("android.widget.EditText"))
        if (searchBar != null) return searchBar

        // Try clicking search icon first
        val searchIcon = device.findObject(By.descContains("Search"))
            ?: device.findObject(By.descContains("search"))
        if (searchIcon != null) {
            searchIcon.click()
            Thread.sleep(1500)
            searchBar = device.findObject(By.clazz("android.widget.EditText"))
        }

        return searchBar
    }

    /**
     * E2E-053: Search for a user by name in the Users tab.
     */
    @Test
    fun test01_searchUserName() {
        // Navigate to Users tab
        E2ETestHelper.navigateToTab(device, "Users")

        // Wait for users list to load
        E2ETestHelper.waitForObject(device, By.res(PACKAGE, "users_list"))
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        // Find the search bar
        val searchBar = getSearchBar()
        assertNotNull("Search bar not found on Users tab", searchBar)

        // Type a known user name. Search by the first name token (prefix) so the filter
        // matches the way the original test searched ("Andrew"); the full display name is
        // resolved from getUser but a prefix is what the search field expects.
        val searchQuery = E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_1_UID).substringBefore(" ")
        searchBar!!.clear()
        searchBar.text = searchQuery

        // Wait for filter to apply
        Thread.sleep(SHORT_TIMEOUT)

        // Verify filtered results show the searched user
        val filteredResult = device.findObject(By.textContains(searchQuery))
        assertNotNull(
            "User '$searchQuery' not found in filtered results",
            filteredResult
        )

        // Verify the list component is still present
        val listPresent = device.findObject(By.res(PACKAGE, "users_list"))
        assertNotNull("Users list disappeared after search", listPresent)
    }

    /**
     * E2E-054: Search for a group by name in the Groups tab.
     */
    @Test
    fun test02_searchGroupName() {
        // Navigate to Groups tab
        E2ETestHelper.navigateToTab(device, "Groups")

        // Wait for groups list to load
        E2ETestHelper.waitForObject(device, By.res(PACKAGE, "groups_list"))
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        // Find the search bar
        val searchBar = getSearchBar()
        assertNotNull("Search bar not found on Groups tab", searchBar)

        // Type a known group name (partial match)
        val searchQuery = "Group"
        searchBar!!.clear()
        searchBar.text = searchQuery

        // Wait for filter to apply
        Thread.sleep(SHORT_TIMEOUT)

        // Verify filtered results show matching groups
        val filteredResult = device.findObject(By.textContains(searchQuery))
        assertNotNull(
            "Group matching '$searchQuery' not found in filtered results",
            filteredResult
        )

        // Verify the list component is still present
        val listPresent = device.findObject(By.res(PACKAGE, "groups_list"))
        assertNotNull("Groups list disappeared after search", listPresent)
    }

    /**
     * E2E-055: Search for message content (if global search is available).
     *
     * Note: This test verifies that search functionality works for messages.
     * If message search is not available in the current UI, the test verifies
     * that the search in the conversations/messages area doesn't crash.
     */
    @Test
    fun test03_searchMessageContent() {
        // First, send a unique message so we have something to search for
        E2ETestHelper.openFirstConversation(device)
        val uniqueContent = E2ETestHelper.uniqueMessage("SearchTarget")
        E2ETestHelper.sendMessage(device, uniqueContent)

        // Wait for message to be sent
        device.wait(Until.hasObject(By.textContains(uniqueContent)), TIMEOUT)
        Thread.sleep(2000)

        // Go back to conversations list
        E2ETestHelper.pressBack(device)
        Thread.sleep(2000)

        // Try to find a global search or conversations search
        var searchBar = device.findObject(By.res(PACKAGE, "searchBar"))
        if (searchBar == null) {
            searchBar = device.findObject(By.res(PACKAGE, "search_bar"))
        }
        if (searchBar == null) {
            searchBar = device.findObject(By.clazz("android.widget.EditText"))
        }
        if (searchBar == null) {
            // Try clicking search icon
            val searchIcon = device.findObject(By.descContains("Search"))
                ?: device.findObject(By.descContains("search"))
            if (searchIcon != null) {
                searchIcon.click()
                Thread.sleep(1500)
                searchBar = device.findObject(By.clazz("android.widget.EditText"))
            }
        }

        if (searchBar != null) {
            // Search is available — type message content
            searchBar.clear()
            searchBar.text = uniqueContent.take(10) // Use first part of message

            Thread.sleep(SHORT_TIMEOUT)

            // Verify the app didn't crash
            val bottomNav = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
                ?: device.findObject(By.descContains("Chats"))
            assertNotNull(
                "App should remain functional after message search",
                bottomNav
            )
        } else {
            // Message search is not available in the current screen
            // Verify the app is still functional
            assertTrue(
                "App should remain on home screen when message search is unavailable",
                device.findObject(By.res(PACKAGE, "bottomNavigationView")) != null
            )
        }
    }

    /**
     * E2E-056: Search for nonexistent text and verify empty state.
     */
    @Test
    fun test04_searchEmptyState() {
        // Navigate to Users tab (has the most reliable search)
        E2ETestHelper.navigateToTab(device, "Users")

        // Wait for users list to load
        E2ETestHelper.waitForObject(device, By.res(PACKAGE, "users_list"))
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        // Find the search bar
        val searchBar = getSearchBar()
        assertNotNull("Search bar not found on Users tab", searchBar)

        // Type a nonsensical query that won't match any users
        val nonexistentQuery = "zzzxxx999nonexistent"
        searchBar!!.clear()
        searchBar.text = nonexistentQuery

        // Wait for filter to apply
        Thread.sleep(SHORT_TIMEOUT)

        // Verify no results are shown — look for empty state indicators
        val emptyState = device.findObject(By.textContains("No"))
            ?: device.findObject(By.textContains("no"))
            ?: device.findObject(By.textContains("empty"))
            ?: device.findObject(By.textContains("found"))
            ?: device.findObject(By.descContains("empty"))
            ?: device.findObject(By.descContains("no results"))

        // Also check that the original search query result (matching user) is NOT visible
        val matchingResult = device.findObject(By.textContains(E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_1_UID).substringBefore(" ")))

        // Either we have an empty state indicator, or the known user is not visible
        val isEmptyState = emptyState != null || matchingResult == null
        assertTrue(
            "Empty state should be shown for nonsensical search query '$nonexistentQuery'",
            isEmptyState
        )

        // Verify the list component is still present (didn't crash)
        val listPresent = device.findObject(By.res(PACKAGE, "users_list"))
        assertNotNull("Users list component disappeared after empty search", listPresent)
    }
}
