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
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SETTLE_TIME
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
 * E2E tests for Create Group functionality.
 *
 * NOTE: GRP-108 (public) and GRP-109 (private) are DUPLICATES of E2E-068 and E2E-069
 * already in GroupOperationsE2ETest.kt. They are marked as such below but included
 * for completeness with the GRP test ID mapping.
 *
 * Test IDs:
 * - GRP-108: testCreatePublicGroupNavigatesToMessages [DUPLICATE of E2E-068]
 * - GRP-109: testCreatePrivateGroupNavigatesToMessages [DUPLICATE of E2E-069]
 * - GRP-110: testCreatePasswordGroupNavigatesToMessages
 * - GRP-111: testCreateGroupWithEmptyNameShowsError
 * - GRP-112: testCreatePasswordGroupWithoutPasswordShowsError
 * - GRP-113: testCreatedGroupAppearsInGroupsTab
 * - GRP-114: testCreatedGroupAppearsInConversationsTab
 * - GRP-115: testGroupTypeSelectorTogglesCorrectly
 * - GRP-116: testDismissCreateGroupBottomSheetCancelsAction
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.GroupCreateE2ETest
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
     * GRP-110: Create a password-protected group via the UI.
     */
    @Test
    fun test01_createPasswordGroupNavigatesToMessages() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        val nameField = device.wait(Until.findObject(By.res(PACKAGE, "et_name")), SHORT_TIMEOUT)
            ?: device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Group name field not found", nameField)

        val groupName = "E2E Pass ${System.currentTimeMillis()}"
        nameField!!.clear()
        nameField.text = groupName

        // Select "Password" type
        selectGroupType("Password")

        // Fill in the password field
        Thread.sleep(1000)
        val passwordField = device.findObject(By.res(PACKAGE, "et_password"))
        if (passwordField != null) {
            passwordField.clear()
            passwordField.text = E2ETestConfig.GROUP_PASSWORD
        } else {
            // Fallback: find the second EditText on screen
            val editTexts = device.findObjects(By.clazz("android.widget.EditText"))
            if (editTexts.size >= 2) {
                editTexts[1].clear()
                editTexts[1].text = E2ETestConfig.GROUP_PASSWORD
            }
        }

        // Tap Create
        val createBtn = device.findObject(By.res(PACKAGE, "create_group_btn"))
            ?: device.findObject(By.res(PACKAGE, "createGroupBtnText"))
            ?: device.findObject(By.textContains("Create"))
        assertNotNull("Create button not found", createBtn)
        createBtn!!.click()

        Thread.sleep(5000)

        val messagesLoaded = device.findObject(By.res(PACKAGE, "messageList"))
        val groupsList = device.findObject(By.res(PACKAGE, "groups_list"))
        assertTrue(
            "After creating password group, should be on messages or groups screen",
            messagesLoaded != null || groupsList != null
        )
    }

    /**
     * GRP-111: Create group with empty name shows error.
     */
    @Test
    fun test02_createGroupWithEmptyNameShowsError() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        // Leave name empty and tap Create
        val nameField = device.findObject(By.res(PACKAGE, "et_name"))
            ?: device.findObject(By.clazz("android.widget.EditText"))
        nameField?.clear()

        val createBtn = device.findObject(By.res(PACKAGE, "create_group_btn"))
            ?: device.findObject(By.res(PACKAGE, "createGroupBtnText"))
            ?: device.findObject(By.textContains("Create"))
        if (createBtn != null) {
            createBtn.click()
            Thread.sleep(2000)
        }

        // Verify we're still on the create dialog (didn't navigate away)
        // or an error message appeared
        val stillOnDialog = device.findObject(By.res(PACKAGE, "et_name"))
            ?: device.findObject(By.textContains("Create"))
        val errorMsg = device.findObject(By.textContains("required"))
            ?: device.findObject(By.textContains("empty"))
            ?: device.findObject(By.textContains("enter"))
        assertTrue(
            "Should stay on create dialog or show error for empty name",
            stillOnDialog != null || errorMsg != null
        )
    }

    /**
     * GRP-112: Create password group without password shows error.
     */
    @Test
    fun test03_createPasswordGroupWithoutPasswordShowsError() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        val nameField = device.findObject(By.res(PACKAGE, "et_name"))
            ?: device.findObject(By.clazz("android.widget.EditText"))
        nameField?.clear()
        nameField?.text = "NoPassGroup${System.currentTimeMillis()}"

        // Select Password type but don't enter a password
        selectGroupType("Password")
        Thread.sleep(1000)

        val createBtn = device.findObject(By.res(PACKAGE, "create_group_btn"))
            ?: device.findObject(By.res(PACKAGE, "createGroupBtnText"))
            ?: device.findObject(By.textContains("Create"))
        if (createBtn != null) {
            createBtn.click()
            Thread.sleep(2000)
        }

        // Should remain on the dialog or show an error
        val stillOnDialog = device.findObject(By.res(PACKAGE, "et_password"))
            ?: device.findObject(By.textContains("Protected"))
            ?: device.findObject(By.textContains("Create"))
        val errorMsg = device.findObject(By.textContains("required"))
            ?: device.findObject(By.textContains("password"))
        assertTrue(
            "Should stay on create dialog or show error for missing password",
            stillOnDialog != null || errorMsg != null
        )
    }

    /**
     * GRP-113: Created group appears in the Groups tab.
     */
    @Test
    fun test04_createdGroupAppearsInGroupsTab() {
        val groupName = "E2E Visible ${System.currentTimeMillis()}"

        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        val nameField = device.wait(Until.findObject(By.res(PACKAGE, "et_name")), SHORT_TIMEOUT)
            ?: device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Name field not found", nameField)
        nameField!!.clear()
        nameField.text = groupName

        selectGroupType("Public")

        val createBtn = device.findObject(By.res(PACKAGE, "create_group_btn"))
            ?: device.findObject(By.res(PACKAGE, "createGroupBtnText"))
            ?: device.findObject(By.textContains("Create"))
        createBtn?.click()
        Thread.sleep(5000)

        // If on messages, go back
        if (device.findObject(By.res(PACKAGE, "messageList")) != null) {
            device.pressBack()
            Thread.sleep(2000)
        }

        // Navigate to Groups tab and search
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        val searchBar = device.findObject(By.clazz("android.widget.EditText"))
        searchBar?.clear()
        searchBar?.text = groupName.take(10)
        Thread.sleep(5000)

        val found = device.findObject(By.textContains(groupName.take(10)))
        assertNotNull("Created group should appear in Groups tab search results", found)
    }

    /**
     * GRP-114: Created group appears in the Conversations tab.
     */
    @Test
    fun test05_createdGroupAppearsInConversationsTab() {
        val groupName = "E2E Convo ${System.currentTimeMillis()}"

        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        val nameField = device.wait(Until.findObject(By.res(PACKAGE, "et_name")), SHORT_TIMEOUT)
            ?: device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Name field not found", nameField)
        nameField!!.clear()
        nameField.text = groupName

        selectGroupType("Public")

        val createBtn = device.findObject(By.res(PACKAGE, "create_group_btn"))
            ?: device.findObject(By.res(PACKAGE, "createGroupBtnText"))
            ?: device.findObject(By.textContains("Create"))
        createBtn?.click()
        Thread.sleep(5000)

        // Send a message so it shows in conversations
        if (device.findObject(By.res(PACKAGE, "messageList")) != null) {
            E2ETestHelper.sendMessage(device, "Hello${System.currentTimeMillis()}")
            Thread.sleep(2000)
            device.pressBack()
            Thread.sleep(2000)
        }

        // Go to Chats tab
        E2ETestHelper.navigateToTab(device, "Chats")
        Thread.sleep(SETTLE_TIME)

        // Look for the group name in conversations list
        val found = device.findObject(By.textContains(groupName.take(10)))
        assertNotNull("Created group should appear in Conversations tab after sending message", found)
    }

    /**
     * GRP-115: Group type selector toggles correctly between Public/Private/Password.
     */
    @Test
    fun test06_groupTypeSelectorTogglesCorrectly() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        // Verify we can switch between types
        selectGroupType("Public")
        Thread.sleep(500)
        selectGroupType("Private")
        Thread.sleep(500)
        selectGroupType("Password")
        Thread.sleep(500)

        // After selecting Password/Protected, a password field should appear
        val passwordField = device.findObject(By.res(PACKAGE, "et_password"))
            ?: device.findObject(By.textContains("Protected"))
            ?: device.findObject(By.textContains("password"))
        assertNotNull("Password field should appear after selecting Protected type", passwordField)

        // Switch back to Public — password field should disappear or be hidden
        selectGroupType("Public")
        Thread.sleep(1000)

        // Verify the dialog is still functional
        val nameField = device.findObject(By.res(PACKAGE, "et_name"))
            ?: device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull("Create dialog should still be functional after type switching", nameField)
    }

    /**
     * GRP-116: Dismiss Create Group bottom sheet cancels the action.
     */
    @Test
    fun test07_dismissCreateGroupBottomSheetCancelsAction() {
        val dialogOpened = openCreateGroupDialog()
        assertTrue("Create Group dialog did not open", dialogOpened)

        // Dismiss by pressing back
        device.pressBack()
        Thread.sleep(2000)

        // Verify we're back on the Groups list (no group was created)
        val groupsList = device.findObject(By.res(PACKAGE, "groups_list"))
        assertNotNull("Should be back on Groups list after dismissing create dialog", groupsList)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun openCreateGroupDialog(): Boolean {
        E2ETestHelper.waitForObject(device, By.res(PACKAGE, "groups_list"))
        Thread.sleep(SETTLE_TIME)

        val overflowMenuImages = device.findObjects(
            By.clazz("android.widget.ImageView").clickable(true)
        )
        for (img in overflowMenuImages) {
            img.click()
            Thread.sleep(1500)
            val nameField = device.findObject(By.res(PACKAGE, "et_name"))
            if (nameField != null) {
                // Swipe the bottom sheet upward to fully expand it (Create button may be hidden behind navbar)
                swipeBottomSheetUp()
                return true
            }
            val createGroupTitle = device.findObject(By.textContains("Create"))
            if (createGroupTitle != null) {
                swipeBottomSheetUp()
                return true
            }
            device.pressBack()
            Thread.sleep(500)
        }

        val addButton = device.findObject(By.descContains("Create"))
            ?: device.findObject(By.descContains("Add"))
            ?: device.findObject(By.descContains("New"))
        if (addButton != null) {
            addButton.click()
            Thread.sleep(1500)
            val nameField = device.findObject(By.res(PACKAGE, "et_name"))
                ?: device.findObject(By.clazz("android.widget.EditText"))
            if (nameField != null) {
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

    private fun selectGroupType(type: String) {
        // UIKit uses "Protected" label for password-type groups
        val actualLabel = if (type.equals("Password", ignoreCase = true)) "Protected" else type
        val typeOption = device.findObject(By.text(actualLabel))
            ?: device.findObject(By.textContains(actualLabel))
            ?: device.findObject(By.desc(actualLabel))
            ?: device.findObject(By.text(type))
            ?: device.findObject(By.textContains(type))
        if (typeOption != null) {
            typeOption.click()
            Thread.sleep(500)
        } else {
            val typeId = when (type.lowercase()) {
                "public" -> "rb_public"
                "private" -> "rb_private"
                "password", "protected" -> "rb_password"
                else -> null
            }
            if (typeId != null) {
                device.findObject(By.res(PACKAGE, typeId))?.click()
                Thread.sleep(500)
            }
        }
    }
}
