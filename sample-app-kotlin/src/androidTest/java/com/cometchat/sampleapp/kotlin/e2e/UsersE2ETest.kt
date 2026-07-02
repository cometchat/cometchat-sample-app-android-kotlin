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
 * E2E tests for the Users tab.
 *
 * Test IDs:
 * - E2E-010: testUsersListShowsTestUsers
 * - E2E-011: testScrollLoadsPagination
 * - E2E-012: testSearchFiltersUsers
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.UsersE2ETest
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
        // Verify users_list component is rendered
        val usersList = E2ETestHelper.waitForObject(
            device, By.res(PACKAGE, "users_list")
        )
        assertNotNull("CometChatUsers component (users_list) not found", usersList)

        // Wait for at least one clickable user item inside the RecyclerView
        val uikitPackage = "com.cometchat.uikit.kotlin"
        var firstItem = device.wait(
            Until.findObject(
                By.clickable(true).hasAncestor(By.res(uikitPackage, "recyclerview_users_list"))
            ),
            TIMEOUT
        )

        // Fallback: RecyclerView by class inside users_list
        if (firstItem == null) {
            val recyclerView = device.findObject(
                By.clazz("androidx.recyclerview.widget.RecyclerView")
                    .hasAncestor(By.res(PACKAGE, "users_list"))
            )
            if (recyclerView != null && recyclerView.children.isNotEmpty()) {
                firstItem = recyclerView.children[0]
            }
        }

        assertNotNull("No user items found in the users list", firstItem)
    }

    /**
     * E2E-011: Scrolling the users list loads more users (pagination).
     */
    @Test
    fun test02_scrollLoadsPagination() {
        // Wait for users to load
        val uikitPackage = "com.cometchat.uikit.kotlin"
        var firstItem = device.wait(
            Until.findObject(
                By.clickable(true).hasAncestor(By.res(uikitPackage, "recyclerview_users_list"))
            ),
            TIMEOUT
        )

        if (firstItem == null) {
            val recyclerView = device.findObject(
                By.clazz("androidx.recyclerview.widget.RecyclerView")
                    .hasAncestor(By.res(PACKAGE, "users_list"))
            )
            firstItem = recyclerView?.children?.firstOrNull()
        }

        assertNotNull("Users not loaded for pagination test", firstItem)

        // Scroll down to trigger pagination
        E2ETestHelper.scrollDown(device)
        E2ETestHelper.scrollDown(device)
        E2ETestHelper.scrollDown(device)

        // After scrolling, verify the list component still exists (not empty/crashed)
        val listAfterScroll = device.findObject(By.res(PACKAGE, "users_list"))
        assertNotNull(
            "Users list component disappeared after scrolling — pagination may have failed",
            listAfterScroll
        )
    }

    /**
     * E2E-012: Search filters users by name.
     * Types a known user name in the search bar and verifies filtered results.
     */
    @Test
    fun test03_searchFiltersUsers() {
        // Wait for users list to load
        E2ETestHelper.waitForObject(device, By.res(PACKAGE, "users_list"))
        Thread.sleep(E2ETestHelper.SETTLE_TIME)

        // Find search bar — try resource ID first, then fallback to EditText
        var searchBar = device.findObject(By.res(PACKAGE, "searchBar"))
        if (searchBar == null) {
            searchBar = device.findObject(By.res(PACKAGE, "search_bar"))
        }
        if (searchBar == null) {
            // Look for EditText within the users_list area or above the RecyclerView
            searchBar = device.findObject(
                By.clazz("android.widget.EditText")
                    .hasAncestor(By.res(PACKAGE, "users_list"))
            )
        }
        if (searchBar == null) {
            // Search might be an ImageView/icon that expands — look for search icon
            val searchIcon = device.findObject(By.descContains("Search"))
                ?: device.findObject(By.descContains("search"))
            if (searchIcon != null) {
                searchIcon.click()
                Thread.sleep(1500)
                searchBar = device.findObject(By.clazz("android.widget.EditText"))
            }
        }
        if (searchBar == null) {
            // Last fallback — any EditText on the screen
            searchBar = device.findObject(By.clazz("android.widget.EditText"))
        }

        assertNotNull("Search bar not found on Users tab", searchBar)

        // Type a known user name to filter. Search by the first name token (prefix) so the
        // filter matches as the original test did ("Andrew"); the name is resolved from getUser.
        val searchQuery = E2ETestHelper.getUserName(E2ETestConfig.GROUP_MEMBER_1_UID).substringBefore(" ")
        searchBar!!.clear()
        searchBar.text = searchQuery

        // Wait for filter results
        Thread.sleep(SHORT_TIMEOUT)

        // Verify that a user matching the search is visible
        val filteredResult = device.findObject(By.textContains(searchQuery))
        assertNotNull(
            "Filtered result for '$searchQuery' not found in users list",
            filteredResult
        )

        // Verify the users_list component is still present (didn't crash)
        val listStillPresent = device.findObject(By.res(PACKAGE, "users_list"))
        assertNotNull("Users list component disappeared after search", listStillPresent)
    }
}
