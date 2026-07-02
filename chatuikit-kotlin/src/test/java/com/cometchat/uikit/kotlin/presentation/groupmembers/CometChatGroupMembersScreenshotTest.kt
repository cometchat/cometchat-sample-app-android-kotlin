package com.cometchat.uikit.kotlin.presentation.groupmembers

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.GroupMembersRepository
import com.cometchat.uikit.core.domain.usecase.BanGroupMemberUseCase
import com.cometchat.uikit.core.domain.usecase.ChangeMemberScopeUseCase
import com.cometchat.uikit.core.domain.usecase.FetchGroupMembersUseCase
import com.cometchat.uikit.core.domain.usecase.KickGroupMemberUseCase
import com.cometchat.uikit.core.viewmodel.CometChatGroupMembersViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.groupmembers.style.CometChatGroupMembersStyle
import com.cometchat.uikit.kotlin.presentation.groupmembers.ui.CometChatGroupMembers
import com.cometchat.uikit.kotlin.presentation.groupmembers.utils.GroupMembersViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.presentation.utils.captureWithPopups
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatGroupMembers.
 *
 * Captures golden images for ALL visual states of the CometChatGroupMembers component
 * using Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatGroupMembersScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatGroupMembersScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatGroupMembersScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/groupmembers"
        )
    )

    // ==================== Constants ====================

    private companion object {
        private const val AVATAR_BASE_URL = "https://data-us.cometchat.io/assets/images/avatars"
        private const val FIXED_TIMESTAMP = 1735689600L // Jan 1, 2025 UTC
    }

    // ==================== Section 1: UI States ====================

    @Test
    fun stateLoading() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    @Test
    fun stateEmpty() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createErrorViewModel("LOAD_ERR", "Failed to load group members")
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    @Test
    fun stateContent() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(5))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    // ==================== Section 2: Popup Menu (Espresso longClick + captureWithPopups) ====================

    @Test
    fun longPressShowsDefaultPopupMenu() {
        launchAndCapturePopup(itemPosition = 0) { activity ->
            CometChatGroupMembers(activity).apply {
                setViewModel(createViewModel(createGroupMembers(5)))
                setGroup(createGroup())
            }
        }
    }

    @Test
    fun longPressShowsCustomPopupMenu() {
        launchAndCapturePopup(itemPosition = 1) { activity ->
            CometChatGroupMembers(activity).apply {
                setViewModel(createViewModel(createGroupMembers(5)))
                setGroup(createGroup())
                setOptions { _, _, _ ->
                    listOf(
                        CometChatPopupMenu.MenuItem("kick", "Kick Member", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("ban", "Ban Member", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("scope", "Change Scope", null, null, 0, 0, 0, 0, null)
                    )
                }
            }
        }
    }

    // ==================== Section 3: Selection Mode Interactions (Espresso click) ====================

    @Test
    fun singleSelectionAfterTap() {
        launchInteractWithConfig(
            members = createGroupMembers(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.SINGLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_group_members))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
            }
        )
    }

    @Test
    fun multipleSelectionAfterTaps() {
        launchInteractWithConfig(
            members = createGroupMembers(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_group_members))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_group_members))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            1, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_group_members))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            2, click()
                        )
                    )
            }
        )
    }

    // ==================== Section 4: Scroll States (Espresso scroll) ====================

    @Test
    fun scrollToBottom() {
        launchInteractWithConfig(
            members = createGroupMembers(20),
            interaction = {
                onView(withId(R.id.recyclerview_group_members))
                    .perform(
                        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(19)
                    )
            }
        )
    }

    @Test
    fun scrollToMiddle() {
        launchInteractWithConfig(
            members = createGroupMembers(20),
            interaction = {
                onView(withId(R.id.recyclerview_group_members))
                    .perform(
                        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(10)
                    )
            }
        )
    }

    // ==================== Section 5: Toolbar Interactions ====================

    @Test
    fun toolbarCustomTitle() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(5))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setTitle("Group Members")
            view
        }
    }

    @Test
    fun selectionClearedAfterDiscardButtonClick() {
        launchInteractWithConfig(
            members = createGroupMembers(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_group_members))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_group_members))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            2, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withContentDescription("Discard selection"))
                    .perform(click())
            }
        )
    }

    // ==================== Section 6: Visibility Toggles ====================

    @Test
    fun visibilityNoToolbar() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(5))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setToolbarVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoSearchBox() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(5))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setSearchBoxVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoSeparators() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(5))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setSeparatorVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoUserStatus() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(5))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setUserStatusVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityWithBackButton() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(5))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setBackIconVisibility(View.VISIBLE)
            view
        }
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customLoadingView() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val customLoading = TextView(activity).apply {
                text = "Custom Loading..."
                textSize = 18f
                setTextColor(Color.DKGRAY)
                setPadding(32, 64, 32, 64)
            }
            view.setLoadingView(customLoading)
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    @Test
    fun customEmptyView() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val customEmpty = TextView(activity).apply {
                text = "No members found!\nInvite members to this group."
                textSize = 16f
                setTextColor(Color.GRAY)
                setPadding(32, 64, 32, 64)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            view.setEmptyView(customEmpty)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    @Test
    fun customErrorView() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val customError = TextView(activity).apply {
                text = "Oops! Something went wrong.\nPlease try again later."
                textSize = 16f
                setTextColor(Color.RED)
                setPadding(32, 64, 32, 64)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            view.setErrorView(customError)
            val vm = createErrorViewModel("CUSTOM_ERR", "Custom error")
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    @Test
    fun customItemView() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(3))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setItemView(object : GroupMembersViewHolderListener() {
                override fun createView(
                    context: Context,
                    binding: com.cometchat.uikit.kotlin.databinding.CometchatGroupMemberListItemBinding
                ): View {
                    return TextView(context).apply {
                        textSize = 16f
                        setTextColor(Color.DKGRAY)
                        setPadding(48, 32, 48, 32)
                    }
                }
                override fun bindView(
                    context: Context,
                    createdView: View,
                    groupMember: GroupMember,
                    group: Group?,
                    holder: RecyclerView.ViewHolder,
                    memberList: List<GroupMember>,
                    position: Int
                ) {
                    (createdView as TextView).text = "⭐ ${groupMember.name} [${groupMember.scope}] — Custom Item View"
                }
            })
            view
        }
    }

    @Test
    fun customLeadingView() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(3))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setLeadingView(object : GroupMembersViewHolderListener() {
                override fun createView(
                    context: Context,
                    binding: com.cometchat.uikit.kotlin.databinding.CometchatGroupMemberListItemBinding
                ): View {
                    return TextView(context).apply {
                        textSize = 20f
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#6851D6"))
                        setPadding(24, 24, 24, 24)
                        gravity = android.view.Gravity.CENTER
                    }
                }
                override fun bindView(
                    context: Context,
                    createdView: View,
                    groupMember: GroupMember,
                    group: Group?,
                    holder: RecyclerView.ViewHolder,
                    memberList: List<GroupMember>,
                    position: Int
                ) {
                    (createdView as TextView).text = groupMember.name.first().uppercase()
                }
            })
            view
        }
    }

    @Test
    fun customTitleView() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(3))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setTitleView(object : GroupMembersViewHolderListener() {
                override fun createView(
                    context: Context,
                    binding: com.cometchat.uikit.kotlin.databinding.CometchatGroupMemberListItemBinding
                ): View {
                    return TextView(context).apply {
                        textSize = 16f
                        setTextColor(Color.parseColor("#6851D6"))
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                }
                override fun bindView(
                    context: Context,
                    createdView: View,
                    groupMember: GroupMember,
                    group: Group?,
                    holder: RecyclerView.ViewHolder,
                    memberList: List<GroupMember>,
                    position: Int
                ) {
                    (createdView as TextView).text = "★ ${groupMember.name}"
                }
            })
            view
        }
    }

    @Test
    fun customSubtitleView() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(3))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setSubtitleView(object : GroupMembersViewHolderListener() {
                override fun createView(
                    context: Context,
                    binding: com.cometchat.uikit.kotlin.databinding.CometchatGroupMemberListItemBinding
                ): View {
                    return TextView(context).apply {
                        textSize = 13f
                        setTextColor(Color.parseColor("#FF6600"))
                    }
                }
                override fun bindView(
                    context: Context,
                    createdView: View,
                    groupMember: GroupMember,
                    group: Group?,
                    holder: RecyclerView.ViewHolder,
                    memberList: List<GroupMember>,
                    position: Int
                ) {
                    (createdView as TextView).text = "🔔 Scope: ${groupMember.scope}"
                }
            })
            view
        }
    }

    @Test
    fun customTrailingView() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(3))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view.setTrailingView(object : GroupMembersViewHolderListener() {
                override fun createView(
                    context: Context,
                    binding: com.cometchat.uikit.kotlin.databinding.CometchatGroupMemberListItemBinding
                ): View {
                    return TextView(context).apply {
                        textSize = 12f
                        setTextColor(Color.parseColor("#6851D6"))
                        setPadding(8, 4, 8, 4)
                    }
                }
                override fun bindView(
                    context: Context,
                    createdView: View,
                    groupMember: GroupMember,
                    group: Group?,
                    holder: RecyclerView.ViewHolder,
                    memberList: List<GroupMember>,
                    position: Int
                ) {
                    (createdView as TextView).text = "📌 Remove"
                }
            })
            view
        }
    }

    // ==================== Section 8: Style & Theming ====================

    @Test
    fun styleCustomBackground() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(5))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            val customStyle = CometChatGroupMembersStyle(
                backgroundColor = Color.parseColor("#F5F5DC")
            )
            view.setStyle(customStyle)
            view
        }
    }

    @Test
    fun stylingCustomColors() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(5))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            val customStyle = CometChatGroupMembersStyle(
                backgroundColor = Color.parseColor("#1A1A2E"),
                titleTextColor = Color.WHITE,
                separatorColor = Color.parseColor("#333333")
            )
            view.setStyle(customStyle)
            view
        }
    }

    // ==================== Section 9: Content Variants ====================

    @Test
    fun contentLargeList() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(25))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    @Test
    fun contentAllScopes() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val members = listOf(
                createMember("member_0", "Iron Man", CometChatConstants.SCOPE_ADMIN, CometChatConstants.USER_STATUS_ONLINE),
                createMember("member_1", "Captain America", CometChatConstants.SCOPE_MODERATOR, CometChatConstants.USER_STATUS_OFFLINE),
                createMember("member_2", "Spiderman", CometChatConstants.SCOPE_PARTICIPANT, CometChatConstants.USER_STATUS_ONLINE),
                createMember("member_3", "Black Widow", CometChatConstants.SCOPE_ADMIN, CometChatConstants.USER_STATUS_OFFLINE),
                createMember("member_4", "Thor", CometChatConstants.SCOPE_PARTICIPANT, CometChatConstants.USER_STATUS_ONLINE)
            )
            val vm = createViewModel(members)
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    @Test
    fun contentModeratorBadgeTruncation() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val members = listOf(
                createMember("member_0", "Iron Man", CometChatConstants.SCOPE_MODERATOR, CometChatConstants.USER_STATUS_ONLINE),
                createMember("member_1", "Captain America", CometChatConstants.SCOPE_MODERATOR, CometChatConstants.USER_STATUS_OFFLINE),
                createMember("member_2", "Spiderman", CometChatConstants.SCOPE_MODERATOR, CometChatConstants.USER_STATUS_ONLINE)
            )
            val vm = createViewModel(members)
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    // ==================== Section 10: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(createGroupMembers(5))
            view.setViewModel(vm)
            view.setGroup(createGroup())
            view
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun longPressShowsDefaultPopupMenuDark() {
        launchAndCapturePopup(itemPosition = 0) { activity ->
            CometChatGroupMembers(activity).apply {
                setViewModel(createViewModel(createGroupMembers(5)))
                setGroup(createGroup())
            }
        }
    }

    // ==================== Helper: Static Screenshot Capture ====================

    private fun launchAndCapture(
        configure: (ComponentActivity) -> CometChatGroupMembers
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)

            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            container.addView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            activity.setContentView(container)

            ShadowLooper.idleMainLooper()

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            ShadowLooper.idleMainLooper()

            fixAvatarCircularRendering(container)

            view.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Popup Capture (captureWithPopups) ====================

    private fun launchAndCapturePopup(
        itemPosition: Int,
        configure: (ComponentActivity) -> CometChatGroupMembers
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = configure(activity)
            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addView(
                    view,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
            activity.setContentView(container)

            ShadowLooper.idleMainLooper()

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)
            ShadowLooper.idleMainLooper()

            onView(withId(R.id.recyclerview_group_members))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        itemPosition, longClick()
                    )
                )

            ShadowLooper.idleMainLooper()

            activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Interaction Capture (Espresso + captureRoboImage) ====================

    private fun launchInteractWithConfig(
        members: List<GroupMember>,
        configure: ((CometChatGroupMembers) -> Unit)? = null,
        interaction: () -> Unit
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        var captureView: View? = null

        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = CometChatGroupMembers(activity)
            val vm = createViewModel(members)
            view.setViewModel(vm)
            view.setGroup(createGroup())

            configure?.invoke(view)

            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            container.addView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            activity.setContentView(container)

            ShadowLooper.idleMainLooper()

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)
            ShadowLooper.idleMainLooper()

            fixAvatarCircularRendering(container)

            captureView = activity.window.decorView
        }

        interaction()
        ShadowLooper.idleMainLooper()

        scenario.onActivity {
            captureView!!.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }

        scenario.close()
    }

    // ==================== Avatar Fix ====================

    private fun fixAvatarCircularRendering(root: View) {
        if (root is CometChatAvatar) {
            root.radius = Float.MAX_VALUE
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                fixAvatarCircularRendering(root.getChildAt(i))
            }
        }
    }

    // ==================== Data Factories ====================

    private fun createGroup(): Group {
        return Group().apply {
            guid = "group_test"
            name = "The Avengers"
            icon = "$AVATAR_BASE_URL/theavengers.png"
            groupType = CometChatConstants.GROUP_TYPE_PUBLIC
            membersCount = 12
            owner = "member_0"
        }
    }

    private fun createMember(uid: String, name: String, scope: String, status: String): GroupMember {
        return GroupMember(uid, scope).apply {
            this.name = name
            this.avatar = "$AVATAR_BASE_URL/${name.lowercase().replace(" ", "")}.png"
            this.status = status
        }
    }

    private fun createGroupMembers(count: Int): List<GroupMember> {
        val names = listOf(
            "Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor",
            "Hulk", "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man",
            "Scarlet Witch", "Vision", "Falcon", "War Machine", "Loki",
            "Gamora", "Star-Lord", "Groot", "Rocket", "Drax",
            "Nebula", "Mantis", "Shuri", "Okoye", "Valkyrie"
        )
        val scopes = listOf(
            CometChatConstants.SCOPE_ADMIN,
            CometChatConstants.SCOPE_ADMIN,
            CometChatConstants.SCOPE_MODERATOR,
            CometChatConstants.SCOPE_PARTICIPANT
        )
        return (0 until count.coerceAtMost(names.size)).map { i ->
            GroupMember("member_$i", scopes[i % scopes.size]).apply {
                name = names[i]
                avatar = "$AVATAR_BASE_URL/${names[i].lowercase().replace(" ", "")}.png"
                status = if (i % 2 == 0) CometChatConstants.USER_STATUS_ONLINE else CometChatConstants.USER_STATUS_OFFLINE
            }
        }
    }

    // ==================== ViewModel Factories ====================

    private fun createViewModel(members: List<GroupMember>): CometChatGroupMembersViewModel {
        var fetched = false
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> {
                return if (!fetched) {
                    fetched = true
                    Result.success(members)
                } else {
                    Result.success(emptyList())
                }
            }
            override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                return Result.success(Unit)
            }
            override suspend fun banMember(guid: String, uid: String): Result<Unit> {
                return Result.success(Unit)
            }
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> {
                return Result.success(Unit)
            }
            override fun hasMore() = !fetched
            override fun resetRequest() { fetched = false }
        }
        return CometChatGroupMembersViewModel(
            fetchGroupMembersUseCase = FetchGroupMembersUseCase(repository),
            kickGroupMemberUseCase = KickGroupMemberUseCase(repository),
            banGroupMemberUseCase = BanGroupMemberUseCase(repository),
            changeMemberScopeUseCase = ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
    }

    private fun createErrorViewModel(code: String, message: String): CometChatGroupMembersViewModel {
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> {
                return Result.failure(CometChatException(code, message))
            }
            override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                return Result.failure(CometChatException(code, message))
            }
            override suspend fun banMember(guid: String, uid: String): Result<Unit> {
                return Result.failure(CometChatException(code, message))
            }
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> {
                return Result.failure(CometChatException(code, message))
            }
            override fun hasMore() = false
            override fun resetRequest() {}
        }
        return CometChatGroupMembersViewModel(
            fetchGroupMembersUseCase = FetchGroupMembersUseCase(repository),
            kickGroupMemberUseCase = KickGroupMemberUseCase(repository),
            banGroupMemberUseCase = BanGroupMemberUseCase(repository),
            changeMemberScopeUseCase = ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
    }

    private fun createLoadingViewModel(): CometChatGroupMembersViewModel {
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> {
                kotlinx.coroutines.awaitCancellation()
            }
            override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                kotlinx.coroutines.awaitCancellation()
            }
            override suspend fun banMember(guid: String, uid: String): Result<Unit> {
                kotlinx.coroutines.awaitCancellation()
            }
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> {
                kotlinx.coroutines.awaitCancellation()
            }
            override fun hasMore() = false
            override fun resetRequest() {}
        }
        return CometChatGroupMembersViewModel(
            fetchGroupMembersUseCase = FetchGroupMembersUseCase(repository),
            kickGroupMemberUseCase = KickGroupMemberUseCase(repository),
            banGroupMemberUseCase = BanGroupMemberUseCase(repository),
            changeMemberScopeUseCase = ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
    }
}
