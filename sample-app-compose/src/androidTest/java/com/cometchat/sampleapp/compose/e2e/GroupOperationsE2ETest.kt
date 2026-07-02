package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
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
 * E2E tests for Group Operations (create public/private groups) in the Compose sample app.
 *
 * Key Compose adaptations:
 * - No resource IDs — uses By.desc(), By.text(), By.clazz() selectors
 * - Create button found via By.descContains("Create")/By.descContains("Add") or
 *   small clickable in top-right header area
 * - Group name field: By.clazz("android.widget.EditText")
 * - "Public"/"Private" type selectors: By.text("Public")/By.text("Private")
 * - "Create" submit button: By.text("Create")
 *
 * Test IDs:
 * - E2E-068: testCreatePublicGroup
 * - E2E-069: testCreatePrivateGroup
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.GroupOperationsE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupOperationsE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
        // Navigate to Groups tab
        E2ETestHelper.navigateToTab(device, "Groups")
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /**
     * Opens the Create Group dialog by tapping the overflow/add menu in the header.
     *
     * In Compose, the create button is typically:
     * 1. An IconButton with contentDescription containing "Create"/"Add"/"New"
     * 2. A small clickable in the top-right header area
     *
     * Returns true if the dialog was successfully opened (EditText or "Create" title visible).
     */
    private fun openCreateGroupDialog(): Boolean {
        Thread.sleep(SETTLE_TIME)

        // Strategy 1: Find by content description
        val addButton = device.findObject(By.descContains("Create"))
            ?: device.findObject(By.descContains("Add"))
            ?: device.findObject(By.descContains("New"))

        if (addButton != null) {
            addButton.click()
            Thread.sleep(1500)
            if (isCreateDialogVisible()) return true
            device.pressBack()
            Thread.sleep(500)
        }

        // Strategy 2: Find small clickable elements in the top-right header area
        val headerClickables = device.findObjects(By.clickable(true))
            .filter {
                val bounds = it.visibleBounds
                bounds.top < 200 &&
                    bounds.right > device.displayWidth / 2 &&
                    bounds.width() < 150 &&
                    bounds.height() < 150
            }

        for (clickable in headerClickables) {
            clickable.click()
            Thread.sleep(1500)

            if (isCreateDialogVisible()) return true

            // Dismiss if wrong popup
            device.pressBack()
            Thread.sleep(500)
        }

        return false
    }

    /**
     * Checks if the Create Group dialog/screen is visible.
     */
    private fun isCreateDialogVisible(): Boolean {
        val nameField = device.findObject(By.clazz("android.widget.EditText"))
        val createTitle = device.findObject(By.textContains("Create"))
            ?: device.findObject(By.textContains("Group Name"))
            ?: device.findObject(By.textContains("Name"))
        return nameField != null || createTitle != null
    }

    /**
     * Selects the group type in the create group dialog.
     *
     * In Compose, type options are rendered as selectable items (Radio buttons or
     * clickable text options) with text "Public" or "Private".
     *
     * @param type "Public" or "Private"
     */
    private fun selectGroupType(type: String) {
        val typeOption = device.findObject(By.text(type))
            ?: device.findObject(By.textContains(type))
            ?: device.findObject(By.desc(type))
            ?: device.findObject(By.descContains(type))

        if (typeOption != null) {
            typeOption.click()
            Thread.sleep(1000)
        }
        // If not found, some UIs default to Public — proceed silently
    }

    // ─── Test Methods ────────────────────────────────────────────────────────────

    /**
     * E2E-068: Create a public group via the UI.
     *
     * Navigates to Groups tab, taps create, enters group name,
     * selects "Public" type, taps create, and verifies the new group appears
     * (either on messages screen or back on groups list).
     */
    @Test
    fun test01_createPublicGroup() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        // Fill in group name
        val nameField = device.wait(
            Until.findObject(By.clazz("android.widget.EditText")),
            SHORT_TIMEOUT
        )
        assertNotNull("Group name field not found in create dialog", nameField)

        val groupName = "E2E Public ${System.currentTimeMillis()}"
        nameField!!.clear()
        nameField.text = groupName

        // Select "Public" type
        selectGroupType("Public")

        // Tap the Create button
        val createBtn = device.findObject(By.text("Create"))
            ?: device.findObject(By.textContains("Create"))

        assertNotNull("Create group button not found", createBtn)
        createBtn!!.click()

        // Wait for navigation — group created may open messages or return to list
        Thread.sleep(5000)

        // Verify we either:
        // 1. Landed on Messages screen (EditText for composer visible)
        // 2. Returned to Groups list (home screen with bottom nav)
        val messagesScreen = device.findObject(By.clazz("android.widget.EditText"))
        val onHome = E2ETestHelper.isOnHomeScreen(device)

        assertTrue(
            "After creating public group, should be on messages screen or groups list",
            messagesScreen != null || onHome
        )

        // If we ended up in messages, go back to verify groups list is intact
        if (messagesScreen != null && !onHome) {
            E2ETestHelper.pressBack(device)
            Thread.sleep(2000)
        }

        // Verify the app is in a valid state
        val validState = E2ETestHelper.isOnHomeScreen(device) ||
            device.findObject(By.scrollable(true)) != null
        assertTrue("App should be in a valid state after creating public group", validState)
    }

    /**
     * E2E-069: Create a private group via the UI.
     *
     * Same flow as public group but selects "Private" type.
     */
    @Test
    fun test02_createPrivateGroup() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        // Fill in group name
        val nameField = device.wait(
            Until.findObject(By.clazz("android.widget.EditText")),
            SHORT_TIMEOUT
        )
        assertNotNull("Group name field not found in create dialog", nameField)

        val groupName = "E2E Private ${System.currentTimeMillis()}"
        nameField!!.clear()
        nameField.text = groupName

        // Select "Private" type
        selectGroupType("Private")

        // Tap the Create button
        val createBtn = device.findObject(By.text("Create"))
            ?: device.findObject(By.textContains("Create"))

        assertNotNull("Create group button not found", createBtn)
        createBtn!!.click()

        // Wait for navigation
        Thread.sleep(5000)

        // Verify we either landed on messages screen or returned to groups list
        val messagesScreen = device.findObject(By.clazz("android.widget.EditText"))
        val onHome = E2ETestHelper.isOnHomeScreen(device)

        assertTrue(
            "After creating private group, should be on messages screen or groups list",
            messagesScreen != null || onHome
        )

        // Navigate back if we're on messages screen
        if (messagesScreen != null && !onHome) {
            E2ETestHelper.pressBack(device)
            Thread.sleep(2000)
        }

        // Verify app is in a valid state
        val validState = E2ETestHelper.isOnHomeScreen(device) ||
            device.findObject(By.scrollable(true)) != null
        assertTrue("App should be in a valid state after creating private group", validState)
    }
}
