package com.cometchat.uikit.compose.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.preview.presentation.ui.*
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi screenshot tests for CometChatConversations (Compose).
 *
 * Captures golden images for ALL visual states of the CometChatConversations composable
 * using Robolectric for JVM-based rendering and Roborazzi for screenshot capture.
 *
 * Each test method produces one golden PNG in src/test/snapshots/.
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug   (generate reference images)
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug   (compare against reference)
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatConversationsScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "src/test/snapshots"
        )
    )

    /**
     * Launches a fresh Activity, sets Compose content, and captures a screenshot.
     * Each call gets a fresh Activity with a clean ViewModelStore.
     */
    private fun captureComposable(content: @Composable () -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setContent { content() }
        }
        scenario.onActivity { activity ->
            val composeView = activity.window.decorView
                .findViewById<android.view.ViewGroup>(android.R.id.content)
                .getChildAt(0)
            composeView.captureRoboImage()
        }
        scenario.close()
    }

    // ==================== Section 1: UI States ====================

    @Test
    fun stateLoading() {
        captureComposable { PreviewConversationListLoading() }
    }

    @Test
    fun stateEmpty() {
        captureComposable { PreviewConversationListEmpty() }
    }

    @Test
    fun stateError() {
        captureComposable { PreviewConversationListError() }
    }

    @Test
    fun stateContent() {
        captureComposable { PreviewConversationListContent() }
    }

    // ==================== Section 2: Custom ViewModels ====================

    @Test
    fun viewModelDefaultFactory() {
        captureComposable { PreviewWithDefaultFactoryViewModel() }
    }

    @Test
    fun viewModelHighUnread() {
        captureComposable { PreviewWithHighUnreadViewModel() }
    }

    @Test
    fun viewModelGroupsOnly() {
        captureComposable { PreviewWithGroupsOnlyViewModel() }
    }

    @Test
    fun viewModelUsersOnly() {
        captureComposable { PreviewWithUsersOnlyViewModel() }
    }

    @Test
    fun viewModelEmptyState() {
        captureComposable { PreviewWithEmptyStateViewModel() }
    }

    @Test
    fun viewModelErrorState() {
        captureComposable { PreviewWithErrorStateViewModel() }
    }

    // ==================== Section 3: Selection Modes ====================

    @Test
    fun selectionSingle() {
        captureComposable { PreviewSingleSelection() }
    }

    @Test
    fun selectionMultiple() {
        captureComposable { PreviewMultipleSelection() }
    }

    // ==================== Section 4: Visibility Props ====================

    @Test
    fun visibilityNoToolbar() {
        captureComposable { PreviewNoToolbar() }
    }

    @Test
    fun visibilityNoSearchBox() {
        captureComposable { PreviewNoSearchBox() }
    }

    @Test
    fun visibilityNoSeparators() {
        captureComposable { PreviewNoSeparators() }
    }

    @Test
    fun visibilityNoUserStatus() {
        captureComposable { PreviewHideUserStatus() }
    }

    @Test
    fun visibilityNoGroupType() {
        captureComposable { PreviewHideGroupType() }
    }

    @Test
    fun visibilityNoReceipts() {
        captureComposable { PreviewHideReceipts() }
    }

    @Test
    fun visibilityWithBackButton() {
        captureComposable { PreviewWithBackButton() }
    }

    // ==================== Section 5: Custom View Overrides ====================

    @Test
    fun customLoadingView() {
        captureComposable { PreviewCustomLoadingView() }
    }

    @Test
    fun customEmptyView() {
        captureComposable { PreviewCustomEmptyView() }
    }

    @Test
    fun customErrorView() {
        captureComposable { PreviewCustomErrorView() }
    }

    @Test
    fun customItemView() {
        captureComposable { PreviewCustomItemView() }
    }

    @Test
    fun customLeadingView() {
        captureComposable { PreviewCustomLeadingView() }
    }

    @Test
    fun customTitleView() {
        captureComposable { PreviewListCustomTitleView() }
    }

    @Test
    fun customSubtitleView() {
        captureComposable { PreviewListCustomSubtitleView() }
    }

    @Test
    fun customTrailingView() {
        captureComposable { PreviewListCustomTrailingView() }
    }

    // ==================== Section 6: Toolbar Customization ====================

    @Test
    fun toolbarCustomTitle() {
        captureComposable { PreviewCustomTitle() }
    }

    @Test
    fun toolbarCustomOverflowMenu() {
        captureComposable { PreviewCustomOverflowMenu() }
    }

    @Test
    fun toolbarCustomSearchPlaceholder() {
        captureComposable { PreviewCustomSearchPlaceholder() }
    }

    // ==================== Section 7: Style Customization ====================

    @Test
    fun styleCustomBackground() {
        captureComposable { PreviewCustomBackgroundStyle() }
    }

    @Test
    fun styleCustomTitleColor() {
        captureComposable { PreviewCustomTitleColorStyle() }
    }

    @Test
    fun styleDarkTheme() {
        captureComposable { PreviewDarkThemeStyle() }
    }

    // ==================== Section 8: Comprehensive ====================

    @Test
    fun comprehensiveAllFeatures() {
        captureComposable { PreviewComprehensive() }
    }

    @Test
    fun comprehensiveMinimal() {
        captureComposable { PreviewMinimal() }
    }

    @Test
    fun comprehensiveLargeList() {
        captureComposable { PreviewLargeList() }
    }

    @Test
    fun comprehensiveAllCustomViews() {
        captureComposable { PreviewListAllCustomViews() }
    }
}
