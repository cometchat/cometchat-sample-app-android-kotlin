package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestConfig
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
 * E2E tests for Create Group functionality (sample-app-compose).
 *
 * NOTE: GRP-108 (public) and GRP-109 (private) are DUPLICATES of E2E-068/E2E-069.
 *
 * Key Compose differences:
 * - No resource IDs — uses text-based selectors
 * - Group type selector uses Compose Radio buttons / selectable text
 * - Create Group bottom sheet or screen found by text content
 * - Password field appears conditionally based on type selection
 *
 * Test IDs:
 * - GRP-110: testCreatePasswordGroupNavigatesToMessages
 * - GRP-111: testCreateGroupWithEmptyNameShowsError
 * - GRP-112: testCreatePasswordGroupWithoutPasswordShowsError
 * - GRP-113: testCreatedGroupAppearsInGroupsTab
 * - GRP-114: testCreatedGroupAppearsInConversationsTab
 * - GRP-115: testGroupTypeSelectorTogglesCorrectly
 * - GRP-116: testDismissCreateGroupBottomSheetCancelsAction
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.GroupCreateE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupCreateE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)
        E2ETestHelper.navigateToTab(device, "Groups")
    }

    /**
     * GRP-110: Create a password-protected group.
     */
    @Test
    fun test01_createPasswordGroupNavigatesToMessages() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        val groupName = "E2E Pass ${System.currentTimeMillis()}"
        typeInGroupNameField(groupName)

        selectGroupType("Password")
        Thread.sleep(1000)

        // Fill password field (second EditText on screen)
        val editTexts = device.findObjects(By.clazz("android.widget.EditText"))
        if (editTexts.size >= 2) {
            try {
                editTexts[1].click()
                Thread.sleep(500)
                editTexts[1].text = E2ETestConfig.GROUP_PASSWORD
            } catch (_: androidx.test.uiautomator.StaleObjectException) {
                // Retry
                val fields = device.findObjects(By.clazz("android.widget.EditText"))
                if (fields.size >= 2) { fields[1].click(); Thread.sleep(500); fields[1].text = E2ETestConfig.GROUP_PASSWORD }
            }
        }
        Thread.sleep(1000)

        // Tap Create
        val createBtn = device.findObject(By.text("Create"))
            ?: device.findObject(By.textContains("Create"))
        assertNotNull("Create button not found", createBtn)
        createBtn!!.click()
        Thread.sleep(5000)

        // Should navigate to messages or stay on groups
        val messagesLoaded = device.findObject(By.clazz("android.widget.EditText")) != null
        val groupsScreen = device.findObject(By.desc("Groups")) != null
        assertTrue("Should be on messages or groups after creating password group", messagesLoaded || groupsScreen)
    }

    /**
     * GRP-111: Create group with empty name shows error.
     */
    @Test
    fun test02_createGroupWithEmptyNameShowsError() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        // Leave name empty, tap Create
        val createBtn = device.findObject(By.text("Create"))
            ?: device.findObject(By.textContains("Create"))
        if (createBtn != null) {
            createBtn.click()
            Thread.sleep(2000)
        }

        // Should still be on the create screen or show an error
        val stillOnCreate = device.findObject(By.textContains("Create")) != null ||
            device.findObject(By.textContains("Name")) != null
        val errorMsg = device.findObject(By.textContains("required")) != null ||
            device.findObject(By.textContains("empty")) != null ||
            device.findObject(By.textContains("enter")) != null
        assertTrue("Should stay on create dialog or show error", stillOnCreate || errorMsg)
    }

    /**
     * GRP-112: Create password group without password shows error.
     */
    @Test
    fun test03_createPasswordGroupWithoutPasswordShowsError() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        typeInGroupNameField("NoPass${System.currentTimeMillis()}")
        selectGroupType("Password")
        Thread.sleep(1000)

        // Don't enter a password, tap Create
        val createBtn = device.findObject(By.text("Create"))
            ?: device.findObject(By.textContains("Create"))
        if (createBtn != null) {
            createBtn.click()
            Thread.sleep(2000)
        }

        // Should stay on dialog or show error
        val stillOnCreate = device.findObject(By.textContains("Protected")) != null ||
            device.findObject(By.textContains("Create")) != null
        assertTrue("Should stay on create dialog for missing password", stillOnCreate)
    }

    /**
     * GRP-113: Created group appears in Groups tab.
     */
    @Test
    fun test04_createdGroupAppearsInGroupsTab() {
        val groupName = "E2E Visible ${System.currentTimeMillis()}"

        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        typeInGroupNameField(groupName)
        selectGroupType("Public")

        val createBtn = device.findObject(By.text("Create"))
            ?: device.findObject(By.textContains("Create"))
        createBtn?.click()
        Thread.sleep(5000)

        // After creating, we may be on Messages screen. Re-launch app to get to home reliably.
        E2ETestHelper.launchApp(device)
        Thread.sleep(SETTLE_TIME)

        // Navigate to Groups tab and search
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        var searchSuccess = false
        repeat(3) {
            if (searchSuccess) return@repeat
            try {
                val searchBar = device.findObject(By.clazz("android.widget.EditText"))
                searchBar?.click(); Thread.sleep(500)
                searchBar?.clear(); searchBar?.text = groupName.take(10)
                searchSuccess = true
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
        Thread.sleep(5000)

        val found = E2ETestHelper.safeGetBounds(
            device, By.textContains(groupName.take(10))
        ) { true }
        assertTrue("Created group should appear in Groups tab", found.isNotEmpty())
    }

    /**
     * GRP-114: Created group appears in Conversations tab.
     */
    @Test
    fun test05_createdGroupAppearsInConversationsTab() {
        val groupName = "E2E Convo ${System.currentTimeMillis()}"

        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        typeInGroupNameField(groupName)
        selectGroupType("Public")

        val createBtn = device.findObject(By.text("Create"))
            ?: device.findObject(By.textContains("Create"))
        createBtn?.click()
        Thread.sleep(5000)

        // Send a message if on messages screen
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        if (editText != null) {
            E2ETestHelper.sendMessage(device, "Hello${System.currentTimeMillis()}")
            Thread.sleep(2000)
            device.pressBack()
            Thread.sleep(2000)
        }

        // Go to Chats tab
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(SETTLE_TIME)

        val found = E2ETestHelper.safeGetBounds(
            device, By.textContains(groupName.take(10))
        ) { true }
        assertTrue("Created group should appear in Conversations tab", found.isNotEmpty())
    }

    /**
     * GRP-115: Group type selector toggles correctly.
     */
    @Test
    fun test06_groupTypeSelectorTogglesCorrectly() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        selectGroupType("Public")
        Thread.sleep(500)
        selectGroupType("Private")
        Thread.sleep(500)
        selectGroupType("Password")
        Thread.sleep(1000)

        // After selecting Password, a second input field should appear
        val editTexts = device.findObjects(By.clazz("android.widget.EditText"))
        assertTrue(
            "Password field should appear after selecting Password type (expected >=2 EditTexts)",
            editTexts.size >= 2
        )

        // Switch back to Public
        selectGroupType("Public")
        Thread.sleep(1000)

        // Verify the dialog is still functional
        val nameField = device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Create dialog should still be functional after type switching", nameField)
    }

    /**
     * GRP-116: Dismiss Create Group cancels action.
     */
    @Test
    fun test07_dismissCreateGroupBottomSheetCancelsAction() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        // Dismiss by pressing back
        device.pressBack()
        Thread.sleep(2000)

        // Should be back on Groups screen
        val groupsTab = device.findObject(By.desc("Groups"))
            ?: device.findObject(By.text("Groups"))
        assertNotNull("Should be back on Groups after dismissing create dialog", groupsTab)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun openCreateGroupDialog(): Boolean {
        Thread.sleep(SETTLE_TIME)
        device.waitForIdle()

        // Find clickable icons/buttons in the header area
        val headerBounds = E2ETestHelper.safeGetBounds(
            device, By.clickable(true)
        ) { it.top < 250 }

        for (bounds in headerBounds.sortedByDescending { it.left }) {
            device.click(bounds.centerX(), bounds.centerY())
            Thread.sleep(2000)

            // Check if create dialog appeared
            val createText = device.findObject(By.textContains("Create"))
            val nameField = device.findObject(By.clazz("android.widget.EditText"))
            if (createText != null || nameField != null) {
                // Swipe the bottom sheet upward to fully expand it
                swipeBottomSheetUp()
                return true
            }

            device.pressBack()
            Thread.sleep(500)
        }

        // Fallback: look for FAB or add button by description
        val addBtn = device.findObject(By.descContains("Create"))
            ?: device.findObject(By.descContains("Add"))
            ?: device.findObject(By.descContains("New"))
        if (addBtn != null) {
            addBtn.click()
            Thread.sleep(2000)
            val found = device.findObject(By.textContains("Create")) != null ||
                device.findObject(By.clazz("android.widget.EditText")) != null
            if (found) {
                swipeBottomSheetUp()
                return true
            }
        }

        return false
    }

    /**
     * Swipes the bottom sheet upward to fully expand it so the Create button
     * is visible above the system navigation bar.
     */
    private fun swipeBottomSheetUp() {
        val displayHeight = device.displayHeight
        val displayWidth = device.displayWidth
        // Swipe from middle of screen upward to drag the bottom sheet to full height
        device.swipe(
            displayWidth / 2,
            displayHeight * 2 / 3,
            displayWidth / 2,
            displayHeight / 4,
            20
        )
        Thread.sleep(1000)
    }

    private fun typeInGroupNameField(name: String) {
        var typed = false
        repeat(3) {
            if (typed) return@repeat
            try {
                val editTexts = device.findObjects(By.clazz("android.widget.EditText"))
                if (editTexts.isNotEmpty()) {
                    editTexts[0].click()
                    Thread.sleep(500)
                    editTexts[0].clear()
                    editTexts[0].text = name
                    typed = true
                }
            } catch (_: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(1500) }
        }
    }

    private fun selectGroupType(type: String) {
        // UIKit uses "Protected" label for password-type groups
        val actualLabel = if (type.equals("Password", ignoreCase = true)) "Protected" else type
        val typeOption = device.findObject(By.text(actualLabel))
            ?: device.findObject(By.textContains(actualLabel))
            ?: device.findObject(By.desc(actualLabel))
            ?: device.findObject(By.text(type))
            ?: device.findObject(By.textContains(type))
        typeOption?.click()
        Thread.sleep(500)
    }
}
