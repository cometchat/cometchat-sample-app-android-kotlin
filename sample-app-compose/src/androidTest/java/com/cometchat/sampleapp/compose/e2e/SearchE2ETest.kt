package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SETTLE_TIME
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
 * E2E tests for Search functionality across the Compose sample app.
 *
 * Ported from sample-app-kotlin SearchE2ETest, adapted for Compose:
 * - No resource IDs (Compose renders generic Views in the a11y tree)
 * - Search bars are located via By.clazz("android.widget.EditText") or By.text/desc("Search")
 * - Lists are found via By.scrollable(true) with clickable children
 *
 * Test IDs:
 * - E2E-053: testSearchUserName
 * - E2E-054: testSearchGroupName
 * - E2E-055: testSearchMessageContent
 * - E2E-056: testSearchEmptyState
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.SearchE2ETest
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

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /**
     * Locates the search bar on the current screen.
     *
     * In Compose, search fields render as EditText in the accessibility tree.
     * If a search icon is present, it needs to be tapped first to reveal the field.
     *
     * @return UiObject2 for the search EditText, or null if not found
     */
    private fun getSearchBar(): UiObject2? {
        // Strategy 1: EditText already visible (Compose TextField → EditText in a11y tree)
        var searchBar = device.findObject(By.clazz("android.widget.EditText"))
        if (searchBar != null) return searchBar

        // Strategy 2: Tap search icon (content description "Search") to expand the field
        val searchIcon = device.findObject(By.descContains("Search"))
            ?: device.findObject(By.descContains("search"))
        if (searchIcon != null) {
            searchIcon.click()
            Thread.sleep(1500)
            searchBar = device.findObject(By.clazz("android.widget.EditText"))
            if (searchBar != null) return searchBar
        }

        // Strategy 3: Find by placeholder/hint text
        searchBar = device.findObject(By.textContains("Search"))
            ?: device.findObject(By.textContains("search"))

        return searchBar
    }

    // ─── Tests ───────────────────────────────────────────────────────────────────

    /**
     * E2E-053: Search for a user by name in the Users tab.
     *
     * Navigates to the Users tab, types a known user name in the search bar,
     * and verifies the filtered result contains that user.
     */
    @Test
    fun test01_searchUserName() {
        // Navigate to Users tab
        E2ETestHelper.navigateToTab(device, "Users")
        Thread.sleep(SETTLE_TIME)

        // Find the search bar
        val searchBar = getSearchBar()
        assertNotNull("Search bar not found on Users tab", searchBar)

        // Type a known user name. Search by the first name token (prefix) so the filter
        // matches the way the original test searched ("Andrew"); the full display name is
        // resolved from getUser but a prefix is what the search field expects.
        val searchQuery = E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_1_UID).substringBefore(" ")
        searchBar!!.click()
        Thread.sleep(500)
        searchBar.clear()
        searchBar.text = searchQuery

        // Wait for filter to apply (network call + recomposition)
        Thread.sleep(SHORT_TIMEOUT)

        // NOTE: no pressBack here. The soft keyboard is disabled on the test emulator, so
        // pressBack acts as a real back-navigation that collapses the search field and clears
        // the filtered results — making the assertion below fail. Matches kotlin, which omits it.

        // Verify filtered results show the searched user
        // In Compose, text might be in By.text, By.textContains, or By.descContains
        val filteredResult = device.findObject(By.textContains(searchQuery))
            ?: device.findObject(By.descContains(searchQuery))

        // Also check using safeGetBounds — the text might be below the search field
        val resultBounds = if (filteredResult == null) {
            var searchFieldBottom = 200
            try {
                val sf = device.findObject(By.clazz("android.widget.EditText"))
                if (sf != null) searchFieldBottom = sf.visibleBounds.bottom + 20
            } catch (_: androidx.test.uiautomator.StaleObjectException) { }

            E2ETestHelper.safeGetBounds(device, By.textContains(searchQuery)) { bounds ->
                bounds.top > searchFieldBottom
            }
        } else emptyList()

        assertNotNull(
            "User '$searchQuery' not found in filtered results",
            if (filteredResult != null) filteredResult else if (resultBounds.isNotEmpty()) resultBounds else null
        )

        // Verify the app is still functional (didn't crash)
        assertTrue(
            "App should still be on home screen after search",
            E2ETestHelper.isOnHomeScreen(device)
        )
    }

    /**
     * E2E-054: Search for a group by name in the Groups tab.
     *
     * Navigates to the Groups tab, types a known group name in the search bar,
     * and verifies the filtered result contains matching groups.
     */
    @Test
    fun test02_searchGroupName() {
        // Navigate to Groups tab
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        // Find the search bar
        val searchBar = getSearchBar()
        assertNotNull("Search bar not found on Groups tab", searchBar)

        // Type a partial group name (most test groups contain "Group")
        val searchQuery = "Group"
        searchBar!!.clear()
        searchBar.text = searchQuery

        // Wait for filter to apply
        Thread.sleep(SHORT_TIMEOUT)

        // Verify filtered results show a matching group
        val filteredResult = device.findObject(By.textContains(searchQuery))
        assertNotNull(
            "Group matching '$searchQuery' not found in filtered results",
            filteredResult
        )

        // Verify the app remains stable
        assertTrue(
            "App should still be on home screen after group search",
            E2ETestHelper.isOnHomeScreen(device)
        )
    }

    /**
     * E2E-055: Search for message content in conversations.
     *
     * Sends a unique message, goes back to the conversations list, and verifies
     * that searching for the message content works or at least doesn't crash.
     *
     * Note: Message-level search availability depends on the UIKit version.
     * This test verifies the search interaction is non-destructive.
     */
    @Test
    fun test03_searchMessageContent() {
        // Send a unique message so we have content to search for
        E2ETestHelper.openFirstConversation(device)
        val uniqueContent = E2ETestHelper.uniqueMessage("SearchTarget")
        E2ETestHelper.sendMessage(device, uniqueContent)

        // Wait for message to be sent
        device.wait(Until.hasObject(By.textContains(uniqueContent)), TIMEOUT)
        Thread.sleep(2000)

        // Go back to conversations list
        E2ETestHelper.pressBack(device)
        Thread.sleep(SETTLE_TIME)

        // Try to find and use a search bar on the Chats tab
        var searchBar = device.findObject(By.clazz("android.widget.EditText"))

        if (searchBar == null) {
            // Try clicking a search icon
            val searchIcon = device.findObject(By.descContains("Search"))
                ?: device.findObject(By.descContains("search"))
            if (searchIcon != null) {
                searchIcon.click()
                Thread.sleep(1500)
                searchBar = device.findObject(By.clazz("android.widget.EditText"))
            }
        }

        if (searchBar != null) {
            // Search is available — type partial message content
            searchBar.clear()
            searchBar.text = uniqueContent.take(10)
            Thread.sleep(SHORT_TIMEOUT)

            // Verify the app didn't crash (still has bottom nav or content)
            val stillFunctional = E2ETestHelper.isOnHomeScreen(device) ||
                device.findObject(By.clickable(true)) != null
            assertTrue(
                "App should remain functional after message content search",
                stillFunctional
            )
        } else {
            // Message search not available in current screen — verify app is still functional
            assertTrue(
                "App should remain on home screen when message search is unavailable",
                E2ETestHelper.isOnHomeScreen(device)
            )
        }
    }

    /**
     * E2E-056: Search for nonexistent text and verify empty state.
     *
     * Types a nonsensical query in the Users tab search and verifies that either
     * an empty state is shown or no matching results are visible.
     */
    @Test
    fun test04_searchEmptyState() {
        // Navigate to Users tab (has the most reliable search)
        E2ETestHelper.navigateToTab(device, "Users")
        Thread.sleep(SETTLE_TIME)

        // Find the search bar
        val searchBar = getSearchBar()
        assertNotNull("Search bar not found on Users tab", searchBar)

        // Type a nonsensical query that won't match any users
        val nonexistentQuery = "zzzxxx999nonexistent"
        searchBar!!.clear()
        searchBar.text = nonexistentQuery

        // Wait for filter to apply
        Thread.sleep(SHORT_TIMEOUT)

        // Verify no matching results are shown
        // Strategy 1: Look for an empty state text ("No Users Found", "No results", etc.)
        val emptyState = device.findObject(By.textContains("No"))
            ?: device.findObject(By.textContains("no"))
            ?: device.findObject(By.textContains("empty"))
            ?: device.findObject(By.textContains("found"))
            ?: device.findObject(By.descContains("empty"))
            ?: device.findObject(By.descContains("no results"))

        // Strategy 2: Verify that a known user (e.g., "Andrew") is NOT visible
        val matchingResult = device.findObject(By.textContains(E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_1_UID).substringBefore(" ")))

        // Either an explicit empty state is shown, or the known user is no longer visible
        val isEmptyState = emptyState != null || matchingResult == null
        assertTrue(
            "Empty state should be shown for nonsensical search query '$nonexistentQuery'",
            isEmptyState
        )

        // Verify the app is still stable (didn't crash)
        assertTrue(
            "App should remain on home screen after empty search",
            E2ETestHelper.isOnHomeScreen(device)
        )
    }
}
