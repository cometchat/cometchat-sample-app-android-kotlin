package com.cometchat.uikit.kotlin.presentation.users

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
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.UsersRepository
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.presentation.users.style.CometChatUsersStyle
import com.cometchat.uikit.kotlin.presentation.users.ui.CometChatUsers
import com.cometchat.uikit.kotlin.presentation.users.utils.UsersViewHolderListener
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
 * Roborazzi screenshot tests for CometChatUsers.
 *
 * Captures golden images for ALL visual states of the CometChatUsers component
 * using Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * This test uses the same approach as real-world usage: inflate CometChatUsers,
 * set it as the activity's content view, and inject a ViewModel with mock data.
 * The activity handles layout naturally, ensuring proper rendering of all
 * decorations (sticky headers, separators, etc.).
 *
 * Each test method produces one golden PNG in src/test/snapshots/users/.
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatUsersScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatUsersScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatUsersScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/users"
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
            val view = CometChatUsers(activity)
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateEmpty() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createErrorViewModel("LOAD_ERR", "Failed to load users")
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateContent() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(5))
            view.setViewModel(vm)
            view
        }
    }

    // ==================== Section 2: Popup Menu (Espresso longClick + captureWithPopups) ====================

    @Test
    fun longPressShowsDefaultPopupMenu() {
        launchAndCapturePopup(itemPosition = 0) { activity ->
            CometChatUsers(activity).apply {
                setViewModel(createViewModel(createUsers(5)))
            }
        }
    }

    @Test
    fun longPressShowsCustomPopupMenu() {
        launchAndCapturePopup(itemPosition = 1) { activity ->
            CometChatUsers(activity).apply {
                setViewModel(createViewModel(createUsers(5)))
                setOptions { _, _ ->
                    listOf(
                        CometChatPopupMenu.MenuItem("block", "Block User", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("report", "Report User", null, null, 0, 0, 0, 0, null),
                        CometChatPopupMenu.MenuItem("delete", "Delete", null, null, 0, 0, 0, 0, null)
                    )
                }
            }
        }
    }

    // ==================== Section 3: Selection Mode Interactions (Espresso click) ====================

    @Test
    fun singleSelectionAfterTap() {
        launchInteractWithConfig(
            users = createUsers(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.SINGLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_users_list))
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
            users = createUsers(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_users_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_users_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            1, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_users_list))
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
            users = createUsers(20),
            interaction = {
                onView(withId(R.id.recyclerview_users_list))
                    .perform(
                        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(19)
                    )
            }
        )
    }

    @Test
    fun scrollToMiddle() {
        launchInteractWithConfig(
            users = createUsers(20),
            interaction = {
                onView(withId(R.id.recyclerview_users_list))
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
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(5))
            view.setViewModel(vm)
            view.setTitle("My Contacts")
            view
        }
    }

    @Test
    fun selectionClearedAfterDiscardButtonClick() {
        launchInteractWithConfig(
            users = createUsers(5),
            configure = { view ->
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
            },
            interaction = {
                onView(withId(R.id.recyclerview_users_list))
                    .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0, click()
                        )
                    )
                ShadowLooper.idleMainLooper()

                onView(withId(R.id.recyclerview_users_list))
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
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(5))
            view.setViewModel(vm)
            view.setToolbarVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoSearchBox() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(5))
            view.setViewModel(vm)
            view.setSearchBoxVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoSeparators() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(5))
            view.setViewModel(vm)
            view.setSeparatorVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoUserStatus() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(5))
            view.setViewModel(vm)
            view.setUserStatusVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityWithBackButton() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(5))
            view.setViewModel(vm)
            view.setBackIconVisibility(View.VISIBLE)
            view
        }
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customLoadingView() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
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
            val view = CometChatUsers(activity)
            val customEmpty = TextView(activity).apply {
                text = "No users found!\nInvite friends to chat."
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
            val view = CometChatUsers(activity)
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
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(3))
            view.setViewModel(vm)
            view.setItemView(object : UsersViewHolderListener {
                override fun createView(context: Context, user: User?): View {
                    return TextView(context).apply {
                        textSize = 16f
                        setTextColor(Color.DKGRAY)
                        setPadding(48, 32, 48, 32)
                    }
                }
                override fun bindView(context: Context, view: View, user: User, userList: List<User>, position: Int) {
                    (view as TextView).text = "⭐ ${user.name} — Custom Item View"
                }
            })
            view
        }
    }

    @Test
    fun customLeadingView() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(3))
            view.setViewModel(vm)
            view.setLeadingView(object : UsersViewHolderListener {
                override fun createView(context: Context, user: User?): View {
                    return TextView(context).apply {
                        textSize = 20f
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#6851D6"))
                        setPadding(24, 24, 24, 24)
                        gravity = android.view.Gravity.CENTER
                    }
                }
                override fun bindView(context: Context, view: View, user: User, userList: List<User>, position: Int) {
                    (view as TextView).text = user.name.first().uppercase()
                }
            })
            view
        }
    }

    @Test
    fun customTitleView() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(3))
            view.setViewModel(vm)
            view.setTitleView(object : UsersViewHolderListener {
                override fun createView(context: Context, user: User?): View {
                    return TextView(context).apply {
                        textSize = 16f
                        setTextColor(Color.parseColor("#6851D6"))
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                }
                override fun bindView(context: Context, view: View, user: User, userList: List<User>, position: Int) {
                    (view as TextView).text = "★ ${user.name}"
                }
            })
            view
        }
    }

    @Test
    fun customSubtitleView() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(3))
            view.setViewModel(vm)
            view.setSubtitleView(object : UsersViewHolderListener {
                override fun createView(context: Context, user: User?): View {
                    return TextView(context).apply {
                        textSize = 13f
                        setTextColor(Color.parseColor("#FF6600"))
                    }
                }
                override fun bindView(context: Context, view: View, user: User, userList: List<User>, position: Int) {
                    (view as TextView).text = "🔔 Custom subtitle for ${user.name}"
                }
            })
            view
        }
    }

    @Test
    fun customTrailingView() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(3))
            view.setViewModel(vm)
            view.setTrailingView(object : UsersViewHolderListener {
                override fun createView(context: Context, user: User?): View {
                    return TextView(context).apply {
                        textSize = 12f
                        setTextColor(Color.parseColor("#6851D6"))
                        setPadding(8, 4, 8, 4)
                    }
                }
                override fun bindView(context: Context, view: View, user: User, userList: List<User>, position: Int) {
                    (view as TextView).text = "📌 Follow"
                }
            })
            view
        }
    }

    // ==================== Section 8: Style & Theming ====================

    @Test
    fun styleCustomBackground() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(5))
            view.setViewModel(vm)
            val customStyle = CometChatUsersStyle(
                backgroundColor = Color.parseColor("#F5F5DC")
            )
            view.setStyle(customStyle)
            view
        }
    }

    @Test
    fun stylingCustomColors() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(5))
            view.setViewModel(vm)
            val customStyle = CometChatUsersStyle(
                backgroundColor = Color.parseColor("#1A1A2E"),
                titleTextColor = Color.WHITE,
                separatorColor = Color.parseColor("#333333")
            )
            view.setStyle(customStyle)
            view.setTitleTextColor(Color.WHITE)
            view
        }
    }

    // ==================== Section 9: Content Variants ====================

    @Test
    fun contentLargeList() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(25))
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun contentOnlineOfflineMix() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val users = listOf(
                createUser("user_0", "Iron Man", CometChatConstants.USER_STATUS_ONLINE),
                createUser("user_1", "Captain America", CometChatConstants.USER_STATUS_OFFLINE),
                createUser("user_2", "Spiderman", CometChatConstants.USER_STATUS_ONLINE),
                createUser("user_3", "Black Widow", CometChatConstants.USER_STATUS_OFFLINE),
                createUser("user_4", "Thor", CometChatConstants.USER_STATUS_ONLINE),
                createUser("user_5", "Hulk", CometChatConstants.USER_STATUS_OFFLINE),
                createUser("user_6", "Hawkeye", CometChatConstants.USER_STATUS_ONLINE),
                createUser("user_7", "Black Panther", CometChatConstants.USER_STATUS_OFFLINE)
            )
            val vm = createViewModel(users)
            view.setViewModel(vm)
            view
        }
    }

    // ==================== Section 10: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            val view = CometChatUsers(activity)
            val vm = createViewModel(createUsers(5))
            view.setViewModel(vm)
            view
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun longPressShowsDefaultPopupMenuDark() {
        launchAndCapturePopup(itemPosition = 0) { activity ->
            CometChatUsers(activity).apply {
                setViewModel(createViewModel(createUsers(5)))
            }
        }
    }

    // ==================== Helper: Static Screenshot Capture ====================

    /**
     * Launches an activity, sets the CometChatUsers view as content (just like real usage),
     * and captures a screenshot from the window's decor view.
     *
     * This mirrors real-world usage:
     *   activity.setContentView(CometChatUsers(this))
     *
     * The activity handles layout naturally, ensuring proper rendering of all
     * decorations including sticky headers, separators, and item decorations.
     */
    private fun launchAndCapture(
        configure: (ComponentActivity) -> CometChatUsers
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)

            // Set content view just like real usage: activity.setContentView(CometChatUsers(this))
            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        // Let the looper process all pending messages (layout, draw, ViewModel emissions)
        ShadowLooper.idleMainLooper()

        // Capture from the decor view to include all drawn decorations (sticky headers, etc.)
        scenario.onActivity { activity ->
            fixAvatarCircularRendering(activity.window.decorView)
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Popup Capture (captureWithPopups) ====================

    private fun launchAndCapturePopup(
        itemPosition: Int,
        configure: (ComponentActivity) -> CometChatUsers
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = configure(activity)
            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        ShadowLooper.idleMainLooper()

        onView(withId(R.id.recyclerview_users_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    itemPosition, longClick()
                )
            )

        ShadowLooper.idleMainLooper()

        scenario.onActivity { activity ->
            activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Interaction Capture (Espresso + captureRoboImage) ====================

    /**
     * Launches an activity with CometChatUsers as content, performs interactions,
     * and captures the result. Uses the same real-world pattern as launchAndCapture.
     */
    private fun launchInteractWithConfig(
        users: List<User>,
        configure: ((CometChatUsers) -> Unit)? = null,
        interaction: () -> Unit
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)

        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = CometChatUsers(activity)
            val vm = createViewModel(users)
            view.setViewModel(vm)

            configure?.invoke(view)

            // Set content view just like real usage
            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        ShadowLooper.idleMainLooper()

        interaction()
        ShadowLooper.idleMainLooper()

        scenario.onActivity { activity ->
            fixAvatarCircularRendering(activity.window.decorView)
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
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

    private fun createUser(uid: String, name: String, status: String): User {
        return User().apply {
            this.uid = uid
            this.name = name
            this.avatar = "$AVATAR_BASE_URL/${name.lowercase().replace(" ", "")}.png"
            this.status = status
        }
    }

    private fun createUsers(count: Int): List<User> {
        val names = listOf(
            "Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor",
            "Hulk", "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man",
            "Scarlet Witch", "Vision", "Falcon", "Winter Soldier", "War Machine",
            "Loki", "Gamora", "Star-Lord", "Groot", "Rocket",
            "Nebula", "Drax", "Mantis", "Shuri", "Okoye"
        )
        val statuses = listOf(
            CometChatConstants.USER_STATUS_ONLINE,
            CometChatConstants.USER_STATUS_OFFLINE
        )
        return (0 until count.coerceAtMost(names.size)).map { i ->
            User().apply {
                uid = "user_$i"
                name = names[i]
                avatar = "$AVATAR_BASE_URL/${names[i].lowercase().replace(" ", "")}.png"
                status = statuses[i % 2]
            }
        }
    }

    // ==================== ViewModel Factories ====================

    private fun createViewModel(users: List<User>): CometChatUsersViewModel {
        var fetched = false
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                return if (!fetched) {
                    fetched = true
                    Result.success(users)
                } else {
                    Result.success(emptyList())
                }
            }
            override fun hasMoreUsers() = !fetched
        }
        return CometChatUsersViewModel(
            fetchUsersUseCase = FetchUsersUseCase(repository),
            searchUsersUseCase = SearchUsersUseCase(repository),
            enableListeners = false
        )
    }

    private fun createErrorViewModel(code: String, message: String): CometChatUsersViewModel {
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                return Result.failure(CometChatException(code, message))
            }
            override fun hasMoreUsers() = false
        }
        return CometChatUsersViewModel(
            fetchUsersUseCase = FetchUsersUseCase(repository),
            searchUsersUseCase = SearchUsersUseCase(repository),
            enableListeners = false
        )
    }

    private fun createLoadingViewModel(): CometChatUsersViewModel {
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                kotlinx.coroutines.awaitCancellation()
            }
            override fun hasMoreUsers() = false
        }
        return CometChatUsersViewModel(
            fetchUsersUseCase = FetchUsersUseCase(repository),
            searchUsersUseCase = SearchUsersUseCase(repository),
            enableListeners = false
        )
    }
}
