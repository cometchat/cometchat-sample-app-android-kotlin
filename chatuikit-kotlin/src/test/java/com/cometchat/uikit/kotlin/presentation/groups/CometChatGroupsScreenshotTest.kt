package com.cometchat.uikit.kotlin.presentation.groups

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
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.GroupsRepository
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.groups.style.CometChatGroupsStyle
import com.cometchat.uikit.kotlin.presentation.groups.ui.CometChatGroups
import com.cometchat.uikit.kotlin.presentation.groups.utils.GroupsViewHolderListener
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
 * Roborazzi screenshot tests for CometChatGroups.
 *
 * Captures golden images for ALL visual states of the CometChatGroups component
 * using Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatGroupsScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatGroupsScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatGroupsScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/groups"
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
            val view = CometChatGroups(activity)
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateEmpty() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createErrorViewModel("LOAD_ERR", "Failed to load groups")
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateContent() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(5))
            view.setViewModel(vm)
            view
        }
    }

    // ==================== Section 2: Popup Menu (Espresso longClick + captureWithPopups) ====================

    @Test
    fun longPressShowsDefaultPopupMenu() {
        launchAndCapturePopup(itemPosition = 0) { activity ->
            CometChatGroups(activity).apply {
                setViewModel(createViewModel(createGroups(5)))
            }
        }
    }

    @Test
    fun longPressShowsCustomPopupMenu() {
        launchAndCapturePopup(itemPosition = 1) { activity ->
            CometChatGroups(activity).apply {
                setViewModel(createViewModel(createGroups(5)))
                setOptions { _, _ ->
                    listOf(
                        CometChatPopupMenu.MenuItem("leave", "Leave Group", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("delete", "Delete Group", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("report", "Report", null, null, 0, 0, 0, 0, null)
                    )
                }
            }
        }
    }

    // ==================== Section 3: Selection Mode Interactions (Espresso click) ====================

    @Test
    fun singleSelectionAfterTap() {
        launchInteractWithConfig(
            groups = createGroups(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.SINGLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_groups_list))
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
            groups = createGroups(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_groups_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_groups_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            1, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_groups_list))
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
            groups = createGroups(20),
            interaction = {
                onView(withId(R.id.recyclerview_groups_list))
                    .perform(
                        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(19)
                    )
            }
        )
    }

    @Test
    fun scrollToMiddle() {
        launchInteractWithConfig(
            groups = createGroups(20),
            interaction = {
                onView(withId(R.id.recyclerview_groups_list))
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
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(5))
            view.setViewModel(vm)
            view.setTitle("My Groups")
            view
        }
    }

    @Test
    fun selectionClearedAfterDiscardButtonClick() {
        launchInteractWithConfig(
            groups = createGroups(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_groups_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_groups_list))
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
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(5))
            view.setViewModel(vm)
            view.setToolbarVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoSearchBox() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(5))
            view.setViewModel(vm)
            view.setSearchBoxVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoSeparators() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(5))
            view.setViewModel(vm)
            view.setSeparatorVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoGroupType() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(5))
            view.setViewModel(vm)
            view.setGroupTypeVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityWithBackButton() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(5))
            view.setViewModel(vm)
            view.setBackIconVisibility(View.VISIBLE)
            view
        }
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customLoadingView() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val customLoading = TextView(activity).apply {
                text = "Custom Loading..."
                textSize = 18f
                setTextColor(Color.DKGRAY)
                setPadding(32, 64, 32, 64)
            }
            view.setLoadingView(customLoading)
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun customEmptyView() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val customEmpty = TextView(activity).apply {
                text = "No groups found!\nCreate or join a group."
                textSize = 16f
                setTextColor(Color.GRAY)
                setPadding(32, 64, 32, 64)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            view.setEmptyView(customEmpty)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun customErrorView() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
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
            view
        }
    }

    @Test
    fun customItemView() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(3))
            view.setViewModel(vm)
            view.setItemView(object : GroupsViewHolderListener() {
                override fun createView(
                    context: Context,
                    binding: com.cometchat.uikit.kotlin.databinding.CometchatGroupsListItemBinding
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
                    group: Group,
                    holder: RecyclerView.ViewHolder,
                    groupList: List<Group>,
                    position: Int
                ) {
                    (createdView as TextView).text = "⭐ ${group.name} — Custom Item View"
                }
            })
            view
        }
    }

    @Test
    fun customLeadingView() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(3))
            view.setViewModel(vm)
            view.setLeadingView(object : GroupsViewHolderListener() {
                override fun createView(
                    context: Context,
                    binding: com.cometchat.uikit.kotlin.databinding.CometchatGroupsListItemBinding
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
                    group: Group,
                    holder: RecyclerView.ViewHolder,
                    groupList: List<Group>,
                    position: Int
                ) {
                    (createdView as TextView).text = group.name.first().uppercase()
                }
            })
            view
        }
    }

    @Test
    fun customTitleView() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(3))
            view.setViewModel(vm)
            view.setTitleView(object : GroupsViewHolderListener() {
                override fun createView(
                    context: Context,
                    binding: com.cometchat.uikit.kotlin.databinding.CometchatGroupsListItemBinding
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
                    group: Group,
                    holder: RecyclerView.ViewHolder,
                    groupList: List<Group>,
                    position: Int
                ) {
                    (createdView as TextView).text = "★ ${group.name}"
                }
            })
            view
        }
    }

    @Test
    fun customSubtitleView() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(3))
            view.setViewModel(vm)
            view.setSubtitleView(object : GroupsViewHolderListener() {
                override fun createView(
                    context: Context,
                    binding: com.cometchat.uikit.kotlin.databinding.CometchatGroupsListItemBinding
                ): View {
                    return TextView(context).apply {
                        textSize = 13f
                        setTextColor(Color.parseColor("#FF6600"))
                    }
                }
                override fun bindView(
                    context: Context,
                    createdView: View,
                    group: Group,
                    holder: RecyclerView.ViewHolder,
                    groupList: List<Group>,
                    position: Int
                ) {
                    (createdView as TextView).text = "🔔 ${group.membersCount} members"
                }
            })
            view
        }
    }

    @Test
    fun customTrailingView() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(3))
            view.setViewModel(vm)
            view.setTrailingView(object : GroupsViewHolderListener() {
                override fun createView(
                    context: Context,
                    binding: com.cometchat.uikit.kotlin.databinding.CometchatGroupsListItemBinding
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
                    group: Group,
                    holder: RecyclerView.ViewHolder,
                    groupList: List<Group>,
                    position: Int
                ) {
                    (createdView as TextView).text = "📌 Join"
                }
            })
            view
        }
    }

    // ==================== Section 8: Style & Theming ====================

    @Test
    fun styleCustomBackground() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(5))
            view.setViewModel(vm)
            val customStyle = CometChatGroupsStyle(
                backgroundColor = Color.parseColor("#F5F5DC")
            )
            view.setStyle(customStyle)
            view
        }
    }

    @Test
    fun stylingCustomColors() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(5))
            view.setViewModel(vm)
            val customStyle = CometChatGroupsStyle(
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
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(25))
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun contentMixedGroupTypes() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val groups = listOf(
                createGroup("group_0", "The Avengers", CometChatConstants.GROUP_TYPE_PUBLIC, 12),
                createGroup("group_1", "Justice League", CometChatConstants.GROUP_TYPE_PRIVATE, 8),
                createGroup("group_2", "Design Team", CometChatConstants.GROUP_TYPE_PASSWORD, 5),
                createGroup("group_3", "Developers Hub", CometChatConstants.GROUP_TYPE_PUBLIC, 25),
                createGroup("group_4", "S.H.I.E.L.D.", CometChatConstants.GROUP_TYPE_PRIVATE, 15),
                createGroup("group_5", "Secret Ops", CometChatConstants.GROUP_TYPE_PASSWORD, 3)
            )
            val vm = createViewModel(groups)
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun contentHighMemberCount() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val groups = listOf(
                createGroup("group_0", "Global Community", CometChatConstants.GROUP_TYPE_PUBLIC, 999),
                createGroup("group_1", "Large Team", CometChatConstants.GROUP_TYPE_PUBLIC, 500),
                createGroup("group_2", "Mega Group", CometChatConstants.GROUP_TYPE_PRIVATE, 999)
            )
            val vm = createViewModel(groups)
            view.setViewModel(vm)
            view
        }
    }

    // ==================== Section 10: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            val view = CometChatGroups(activity)
            val vm = createViewModel(createGroups(5))
            view.setViewModel(vm)
            view
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun longPressShowsDefaultPopupMenuDark() {
        launchAndCapturePopup(itemPosition = 0) { activity ->
            CometChatGroups(activity).apply {
                setViewModel(createViewModel(createGroups(5)))
            }
        }
    }

    // ==================== Helper: Static Screenshot Capture ====================

    private fun launchAndCapture(
        configure: (ComponentActivity) -> CometChatGroups
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
        configure: (ComponentActivity) -> CometChatGroups
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

            onView(withId(R.id.recyclerview_groups_list))
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
        groups: List<Group>,
        configure: ((CometChatGroups) -> Unit)? = null,
        interaction: () -> Unit
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        var captureView: View? = null

        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = CometChatGroups(activity)
            val vm = createViewModel(groups)
            view.setViewModel(vm)

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

    private fun createGroup(guid: String, name: String, groupType: String, membersCount: Int): Group {
        return Group().apply {
            this.guid = guid
            this.name = name
            this.icon = "$AVATAR_BASE_URL/${name.lowercase().replace(" ", "")}.png"
            this.groupType = groupType
            this.membersCount = membersCount
        }
    }

    private fun createGroups(count: Int): List<Group> {
        val names = listOf(
            "The Avengers", "Justice League", "Design Team", "Developers Hub", "S.H.I.E.L.D.",
            "Marketing Squad", "Product Team", "Engineering", "Data Science", "DevOps",
            "Mobile Team", "Web Platform", "QA Engineers", "Security Team", "Cloud Ops",
            "AI Research", "Backend Guild", "Frontend Guild", "Platform Team", "Growth Team",
            "Support Team", "Sales Team", "Finance Team", "HR Team", "Legal Team"
        )
        val types = listOf(
            CometChatConstants.GROUP_TYPE_PUBLIC,
            CometChatConstants.GROUP_TYPE_PRIVATE,
            CometChatConstants.GROUP_TYPE_PASSWORD
        )
        val memberCounts = listOf(12, 8, 5, 25, 15, 30, 7, 4)
        return (0 until count.coerceAtMost(names.size)).map { i ->
            Group().apply {
                guid = "group_$i"
                name = names[i]
                icon = "$AVATAR_BASE_URL/${names[i].lowercase().replace(" ", "")}.png"
                groupType = types[i % types.size]
                membersCount = memberCounts[i % memberCounts.size]
            }
        }
    }

    // ==================== ViewModel Factories ====================

    private fun createViewModel(groups: List<Group>): CometChatGroupsViewModel {
        var fetched = false
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
                return if (!fetched) {
                    fetched = true
                    Result.success(groups)
                } else {
                    Result.success(emptyList())
                }
            }
            override suspend fun joinGroup(
                groupId: String,
                groupType: String,
                password: String?
            ): Result<Group> {
                return Result.success(groups.firstOrNull() ?: Group())
            }
            override fun hasMoreGroups() = !fetched
        }
        return CometChatGroupsViewModel(
            fetchGroupsUseCase = FetchGroupsUseCase(repository),
            joinGroupUseCase = JoinGroupUseCase(repository),
            enableListeners = false
        )
    }

    private fun createErrorViewModel(code: String, message: String): CometChatGroupsViewModel {
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
                return Result.failure(CometChatException(code, message))
            }
            override suspend fun joinGroup(
                groupId: String,
                groupType: String,
                password: String?
            ): Result<Group> {
                return Result.failure(CometChatException(code, message))
            }
            override fun hasMoreGroups() = false
        }
        return CometChatGroupsViewModel(
            fetchGroupsUseCase = FetchGroupsUseCase(repository),
            joinGroupUseCase = JoinGroupUseCase(repository),
            enableListeners = false
        )
    }

    private fun createLoadingViewModel(): CometChatGroupsViewModel {
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
                kotlinx.coroutines.awaitCancellation()
            }
            override suspend fun joinGroup(
                groupId: String,
                groupType: String,
                password: String?
            ): Result<Group> {
                kotlinx.coroutines.awaitCancellation()
            }
            override fun hasMoreGroups() = false
        }
        return CometChatGroupsViewModel(
            fetchGroupsUseCase = FetchGroupsUseCase(repository),
            joinGroupUseCase = JoinGroupUseCase(repository),
            enableListeners = false
        )
    }
}
