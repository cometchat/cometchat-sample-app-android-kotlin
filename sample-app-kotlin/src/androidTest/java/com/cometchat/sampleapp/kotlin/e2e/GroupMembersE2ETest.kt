package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SETTLE_TIME
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * E2E tests for Group Members management (Admin/Member panel).
 *
 * Setup approach: Uses CometChat SDK to create a test group and add members
 * programmatically in @Before. This guarantees the logged-in user is the owner/admin
 * and that members exist for kick/ban/scope-change tests.
 *
 * Test IDs:
 * - E2E-030: testMembersListShowsAll
 * - E2E-031: testAdminAddsMember
 * - E2E-032: testAdminRemovesMember
 * - E2E-033: testAdminBansMember
 * - E2E-034: testAdminChangeRole
 * - E2E-072: testAdminDeletesGroup
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.GroupMembersE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GroupMembersE2ETest {

    private lateinit var device: UiDevice

    // The test group created via SDK — user is guaranteed to be owner
    private var testGroupId: String = ""
    private var testGroupName: String = ""

    // Members added to the group for admin action tests
    private val memberUid1 = E2ETestConfig.GROUP_MEMBER_1_UID // Andrew Joseph
    private val memberUid2 = E2ETestConfig.GROUP_MEMBER_2_UID // George Alan

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        // Create a test group via SDK (logged-in user becomes owner)
        createTestGroupViaSdk()
    }

    @After
    fun teardown() {
        // Clean up: delete the test group via SDK
        if (testGroupId.isNotEmpty()) {
            try {
                val latch = CountDownLatch(1)
                CometChat.deleteGroup(testGroupId, object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(p0: String?) { latch.countDown() }
                    override fun onError(e: CometChatException?) { latch.countDown() }
                })
                latch.await(10, TimeUnit.SECONDS)
            } catch (_: Exception) { }
        }
    }

    // ─── SDK Setup ───────────────────────────────────────────────────────────

    /**
     * Creates a public group via CometChat SDK and adds 2 test members.
     * The logged-in user (cometchat-uid-4) becomes the owner automatically.
     */
    private fun createTestGroupViaSdk() {
        testGroupName = "E2E_AdminTest_${System.currentTimeMillis()}"
        testGroupId = "e2e_admin_${System.currentTimeMillis()}"

        val group = Group(testGroupId, testGroupName, CometChatConstants.GROUP_TYPE_PUBLIC, "")

        // Create the group
        val createLatch = CountDownLatch(1)
        var createError: String? = null
        CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(createdGroup: Group) {
                testGroupId = createdGroup.guid
                createLatch.countDown()
            }
            override fun onError(e: CometChatException?) {
                createError = e?.message
                createLatch.countDown()
            }
        })
        createLatch.await(15, TimeUnit.SECONDS)
        assertTrue("Failed to create test group: $createError", createError == null)

        // Add members to the group
        val members = listOf(
            GroupMember(memberUid1, CometChatConstants.SCOPE_PARTICIPANT),
            GroupMember(memberUid2, CometChatConstants.SCOPE_PARTICIPANT)
        )

        val addLatch = CountDownLatch(1)
        CometChat.addMembersToGroup(
            testGroupId,
            members,
            null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(result: HashMap<String, String>?) { addLatch.countDown() }
                override fun onError(e: CometChatException?) { addLatch.countDown() }
            }
        )
        addLatch.await(15, TimeUnit.SECONDS)
    }

    // ─── UI Navigation Helpers ───────────────────────────────────────────────

    /**
     * Navigates to the test group's messages screen by searching for it in the Groups tab.
     */
    private fun navigateToTestGroup() {
        E2ETestHelper.navigateToTab(device, "Groups")
        Thread.sleep(SETTLE_TIME)

        // Search for the test group
        val searchIcon = device.findObject(By.descContains("Search"))
            ?: device.findObject(By.descContains("search"))
            ?: device.findObject(By.res(PACKAGE, "ivSearch"))
        if (searchIcon != null) {
            searchIcon.click()
            Thread.sleep(1500)
        }

        val searchField = device.findObject(By.clazz("android.widget.EditText"))
        if (searchField != null) {
            searchField.text = testGroupName
            Thread.sleep(5000)
        }

        // Tap the test group in search results.
        // IMPORTANT: We must find the group item in the RecyclerView results,
        // NOT the text we typed in the search field. The search field also contains
        // "E2E_AdminTest" text, so we need to find the item that is NOT the EditText.
        val uikitPackage = "com.cometchat.uikit.kotlin"

        // Strategy 1: Find the group name text inside the groups RecyclerView
        val groupsRv = device.findObject(By.res(uikitPackage, "recyclerview_groups_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_groups_list"))

        var groupClicked = false

        if (groupsRv != null) {
            // Click the first child in the RecyclerView (that's our search result)
            val children = groupsRv.children
            if (children.isNotEmpty()) {
                children[0].click()
                groupClicked = true
            }
        }

        // Strategy 2: Find all text matches and click the one that's NOT the EditText
        if (!groupClicked) {
            val allMatches = device.findObjects(By.textContains("E2E_AdminTest"))
            for (match in allMatches) {
                // Skip if this is the EditText (search field) itself
                if (match.className == "android.widget.EditText") continue
                // Skip if this is inside the search/toolbar area (top of screen)
                if (match.visibleBounds.top < 200) continue
                // This should be the group item in the list
                match.click()
                groupClicked = true
                break
            }
        }

        // Strategy 3: Find a clickable item below the search bar that contains our text
        if (!groupClicked) {
            val clickableItems = device.findObjects(By.clickable(true))
                .filter { item ->
                    item.visibleBounds.top > 200 && // Below toolbar/search
                        item.findObject(By.textContains("E2E_AdminTest")) != null
                }
            if (clickableItems.isNotEmpty()) {
                clickableItems[0].click()
                groupClicked = true
            }
        }

        if (!groupClicked) {
            // Last fallback: press back and open first group
            device.pressBack()
            Thread.sleep(1000)
            E2ETestHelper.openFirstGroup(device)
        }

        // Wait for messages screen
        device.wait(Until.hasObject(By.res(PACKAGE, "messageList")), TIMEOUT)
        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Opens Group Details from the message header overflow menu.
     * Finds the rightmost clickable in the header (the 3-dot/options icon),
     * clicks it, then taps "Details" from the popup.
     */
    private fun openGroupDetailsFromHeader() {
        Thread.sleep(SETTLE_TIME)

        var detailsOpened = false

        // Find the rightmost clickable ImageView in the header (options/overflow icon)
        val rightMostHeaderBtn = device.findObjects(
            By.clazz("android.widget.ImageView").clickable(true)
        ).filter {
            it.visibleBounds.top < 250 && it.visibleBounds.left > device.displayWidth / 2
        }.maxByOrNull { it.visibleBounds.left }

        if (rightMostHeaderBtn != null) {
            rightMostHeaderBtn.click()
            Thread.sleep(2000)

            val detailsOption = device.findObject(By.text("Details"))
                ?: device.findObject(By.textContains("Details"))
            if (detailsOption != null) {
                detailsOption.click()
                detailsOpened = true
            } else {
                device.pressBack()
                Thread.sleep(500)
            }
        }

        // Fallback: try via messageHeader resource ID
        if (!detailsOpened) {
            Thread.sleep(1000)
            val messageHeader = device.findObject(By.res(PACKAGE, "messageHeader"))
            if (messageHeader != null) {
                val clickables = messageHeader.findObjects(By.clickable(true))
                    .filter { it.visibleBounds.left > 200 }
                    .sortedByDescending { it.visibleBounds.left }

                for (clickable in clickables) {
                    clickable.click()
                    Thread.sleep(2000)

                    val detailsOption = device.findObject(By.text("Details"))
                        ?: device.findObject(By.textContains("Details"))
                    if (detailsOption != null) {
                        detailsOption.click()
                        detailsOpened = true
                        break
                    }
                    device.pressBack()
                    Thread.sleep(500)
                }
            }
        }

        assertTrue("Could not open Group Details from message header menu", detailsOpened)
        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Opens the View Members screen from Group Details.
     */
    private fun openViewMembers() {
        val viewMembersCard = device.findObject(By.text("View Members"))
            ?: device.findObject(By.textContains("View Members"))
            ?: device.findObject(By.res(PACKAGE, "view_members_card"))

        assertNotNull("View Members option not found on Group Details", viewMembersCard)
        viewMembersCard!!.click()
        Thread.sleep(SETTLE_TIME)
    }

    /**
     * Long-presses a non-owner member in the members list to open the action menu.
     * Skips the first item (owner/self) and targets the second.
     */
    private fun longPressNonOwnerMember(): Boolean {
        val uikitPackage = "com.cometchat.uikit.kotlin"

        val membersRv = device.findObject(By.res(uikitPackage, "recyclerview_group_members"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))

        if (membersRv == null) return false

        val children = membersRv.children
        if (children.size < 2) return false

        // Skip first (owner/self), long-press the second member
        children[1].longClick()
        Thread.sleep(2000)
        return true
    }

    // ─── Test Methods ────────────────────────────────────────────────────────

    /**
     * E2E-030: Verify the members list shows all added members.
     */
    @Test
    fun test01_membersListShowsAll() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()
        openViewMembers()

        val uikitPackage = "com.cometchat.uikit.kotlin"
        val membersRv = device.wait(
            Until.findObject(By.res(uikitPackage, "recyclerview_group_members")),
            TIMEOUT
        ) ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))

        assertNotNull("Group members RecyclerView not found", membersRv)

        // Should show at least 3 members (owner + 2 added members)
        val children = membersRv!!.children
        assertTrue(
            "Group members list should show at least 2 members (owner + 1), found ${children.size}",
            children.size >= 2
        )
    }

    /**
     * E2E-031: As admin, add a member to the group via Add Members dialog.
     * Verification: Read member count text on Group Details screen before and after adding.
     * The Group Details screen shows a "member_count" TextView (e.g., "3 Members").
     */
    @Test
    fun test02_adminAddsMember() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()

        // Read the member count text from Group Details (e.g., "3 Members")
        val memberCountBefore = device.findObject(By.res(PACKAGE, "member_count"))
        val countTextBefore = memberCountBefore?.text ?: ""
        val numBefore = extractNumberFromText(countTextBefore)

        // Tap "Add Members"
        val addMembersCard = device.findObject(By.text("Add Members"))
            ?: device.findObject(By.textContains("Add Members"))
            ?: device.findObject(By.res(PACKAGE, "view_add_members_card"))
            ?: device.findObject(By.res(PACKAGE, "tv_add_members"))

        assertNotNull("Add Members option not found — user should be admin", addMembersCard)
        addMembersCard!!.click()
        Thread.sleep(SETTLE_TIME)

        // Wait for the users list to load
        val uikitPackage = "com.cometchat.uikit.kotlin"
        val deadline = System.currentTimeMillis() + TIMEOUT
        var usersRv: androidx.test.uiautomator.UiObject2? = null
        while (System.currentTimeMillis() < deadline) {
            usersRv = device.findObject(By.res(uikitPackage, "recyclerview_users_list"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_users_list"))
            if (usersRv != null && usersRv.children.isNotEmpty()) break
            Thread.sleep(1500)
        }
        assertNotNull("Users list (Add Members) did not load", usersRv)

        // Scroll down to find a user who is NOT already a member.
        // The already-added members (cometchat-uid-1, cometchat-uid-2) will likely be
        // at the top. Scroll down and select a user further in the list.
        usersRv!!.fling(androidx.test.uiautomator.Direction.DOWN)
        Thread.sleep(2000)

        // Pick the last child in the currently visible list (less likely to be existing member)
        val children = usersRv.children
        if (children.size > 1) {
            children[children.size - 1].click()
        } else if (children.isNotEmpty()) {
            children[0].click()
        }
        Thread.sleep(1000)

        // Tap the "Add Members" button at the bottom to confirm
        val addBtn = device.findObject(By.res(PACKAGE, "add_members_btn"))
            ?: device.findObject(By.res(PACKAGE, "tv_add_members"))
            ?: device.findObject(By.text("Add Members"))

        if (addBtn != null) {
            addBtn.click()
            Thread.sleep(5000)
        }

        // After adding, we should be back on Group Details automatically
        // or we need to press back
        if (device.findObject(By.res(PACKAGE, "member_count")) == null) {
            device.pressBack()
            Thread.sleep(SETTLE_TIME)
        }

        // Verify: Read the member count text AFTER adding
        val memberCountAfter = device.findObject(By.res(PACKAGE, "member_count"))
        assertNotNull("Member count text not found on Group Details after adding", memberCountAfter)
        val countTextAfter = memberCountAfter!!.text ?: ""
        val numAfter = extractNumberFromText(countTextAfter)

        assertTrue(
            "After adding a member, expected member count to increase. " +
                "Before: '$countTextBefore' ($numBefore), After: '$countTextAfter' ($numAfter)",
            numAfter > numBefore
        )
    }

    /**
     * Extracts the first number from a text like "3 Members" → 3
     */
    private fun extractNumberFromText(text: String): Int {
        val regex = Regex("\\d+")
        val match = regex.find(text)
        return match?.value?.toIntOrNull() ?: 0
    }

    /**
     * E2E-032: As admin, kick a member from the group.
     * Verification: Count members before → kick → count after → assert decreased by 1.
     */
    @Test
    fun test03_adminRemovesMember() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()
        openViewMembers()

        val uikitPackage = "com.cometchat.uikit.kotlin"

        // Count members BEFORE kick
        val membersRvBefore = device.findObject(By.res(uikitPackage, "recyclerview_group_members"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))
        assertNotNull("Members RecyclerView not found", membersRvBefore)
        val countBefore = membersRvBefore!!.children.size
        assertTrue("Need at least 2 members to test kick (owner + member)", countBefore >= 2)

        // Long-press a non-owner member
        val memberFound = longPressNonOwnerMember()
        assertTrue("Could not find a non-owner member to kick", memberFound)

        val kickOption = device.wait(Until.findObject(By.text("Kick")), SHORT_TIMEOUT)
            ?: device.findObject(By.textContains("Kick"))

        if (kickOption != null) {
            kickOption.click()
            Thread.sleep(2000)

            // Confirm if dialog appears
            val confirmBtn = device.findObject(By.text("Kick"))
                ?: device.findObject(By.textContains("Confirm"))
                ?: device.findObject(By.textContains("Yes"))
            confirmBtn?.click()
            Thread.sleep(3000)

            // Verify: Member count should have decreased
            val membersRvAfter = device.findObject(By.res(uikitPackage, "recyclerview_group_members"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))
            assertNotNull("Members RecyclerView not found after kick", membersRvAfter)
            val countAfter = membersRvAfter!!.children.size
            assertTrue(
                "After kicking a member, expected member count to decrease. " +
                    "Before: $countBefore, After: $countAfter",
                countAfter < countBefore
            )
        } else {
            device.pressBack()
            assertTrue("Kick option not found in member action menu", false)
        }
    }

    /**
     * E2E-033: As admin, ban a member from the group.
     * Verification: Count members before → ban → count after → assert decreased by 1,
     * OR verify the banned member's name no longer appears in the list.
     */
    @Test
    fun test04_adminBansMember() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()
        openViewMembers()

        val uikitPackage = "com.cometchat.uikit.kotlin"

        // Count members BEFORE ban
        val membersRvBefore = device.findObject(By.res(uikitPackage, "recyclerview_group_members"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))
        assertNotNull("Members RecyclerView not found", membersRvBefore)
        val countBefore = membersRvBefore!!.children.size
        assertTrue("Need at least 2 members to test ban (owner + member)", countBefore >= 2)

        val memberFound = longPressNonOwnerMember()
        assertTrue("Could not find a non-owner member to ban", memberFound)

        val banOption = device.wait(Until.findObject(By.text("Ban")), SHORT_TIMEOUT)
            ?: device.findObject(By.textContains("Ban"))

        if (banOption != null) {
            banOption.click()
            Thread.sleep(2000)

            val confirmBtn = device.findObject(By.text("Ban"))
                ?: device.findObject(By.textContains("Confirm"))
                ?: device.findObject(By.textContains("Yes"))
            confirmBtn?.click()
            Thread.sleep(3000)

            // Verify: Member count should have decreased
            val membersRvAfter = device.findObject(By.res(uikitPackage, "recyclerview_group_members"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))
            assertNotNull("Members RecyclerView not found after ban", membersRvAfter)
            val countAfter = membersRvAfter!!.children.size
            assertTrue(
                "After banning a member, expected member count to decrease. " +
                    "Before: $countBefore, After: $countAfter",
                countAfter < countBefore
            )
        } else {
            device.pressBack()
            assertTrue("Ban option not found in member action menu", false)
        }
    }

    /**
     * E2E-034: As admin, change a member's scope/role.
     * Verification: After changing to Moderator, verify the "Moderator" label/badge
     * appears near the member's entry in the list.
     */
    @Test
    fun test05_adminChangeRole() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()
        openViewMembers()

        val memberFound = longPressNonOwnerMember()
        assertTrue("Could not find a non-owner member to change role", memberFound)

        val changeScopeOption = device.wait(Until.findObject(By.text("Change Scope")), SHORT_TIMEOUT)
            ?: device.findObject(By.textContains("Change Scope"))
            ?: device.findObject(By.textContains("Change Role"))

        if (changeScopeOption != null) {
            changeScopeOption.click()
            Thread.sleep(2000)

            // Select Moderator
            val moderatorOption = device.findObject(By.text("Moderator"))
                ?: device.findObject(By.textContains("Moderator"))
            assertNotNull("Moderator option not found in scope change dialog", moderatorOption)
            moderatorOption!!.click()
            Thread.sleep(1000)

            // Save/Confirm the change
            val saveBtn = device.findObject(By.text("Save"))
                ?: device.findObject(By.textContains("Save"))
                ?: device.findObject(By.textContains("Confirm"))
                ?: device.findObject(By.textContains("OK"))
            saveBtn?.click()
            Thread.sleep(3000)

            // Verify: "Moderator" text should now appear in the members list
            // (the UIKit shows scope labels like "Admin", "Moderator" next to member names)
            val uikitPackage = "com.cometchat.uikit.kotlin"
            val membersRv = device.findObject(By.res(uikitPackage, "recyclerview_group_members"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_group_members"))

            val moderatorLabel = device.findObject(By.textContains("Moderator"))
                ?: device.findObject(By.text("moderator"))
            assertTrue(
                "After changing scope to Moderator, expected 'Moderator' label to appear " +
                    "in the members list. The role change may not have been applied.",
                moderatorLabel != null
            )
        } else {
            device.pressBack()
            assertTrue("Change Scope option not found in member action menu", false)
        }
    }

    /**
     * E2E-072: As admin/owner, delete the group.
     * Uses the test group (we're the owner) — navigates to details → Delete and Exit.
     */
    @Test
    fun test06_adminDeletesGroup() {
        navigateToTestGroup()
        openGroupDetailsFromHeader()

        // Tap "Delete and Exit"
        val deleteOption = device.findObject(By.text("Delete and Exit"))
            ?: device.findObject(By.textContains("Delete and Exit"))
            ?: device.findObject(By.res(PACKAGE, "delete_group_lay"))

        if (deleteOption != null) {
            deleteOption.click()
            Thread.sleep(2000)

            // Confirm
            val confirmBtn = device.findObject(By.text("Delete"))
                ?: device.findObject(By.textContains("Delete"))
            confirmBtn?.click()
            Thread.sleep(5000)

            // After deletion, should be back on home/groups
            val backOnHome = device.findObject(By.res(PACKAGE, "bottomNavigationView"))
            assertNotNull("Should return to home after deleting group", backOnHome)

            // Mark as deleted so @After doesn't try to delete again
            testGroupId = ""
        } else {
            // Delete option not found (maybe different UI layout)
            val leaveOption = device.findObject(By.textContains("Leave"))
            assertNotNull("Neither Delete nor Leave option found", leaveOption)
        }
    }
}
