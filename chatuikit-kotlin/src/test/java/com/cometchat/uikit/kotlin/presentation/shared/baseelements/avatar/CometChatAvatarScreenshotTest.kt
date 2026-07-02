package com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
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
 * Roborazzi screenshot test for CometChatAvatar.
 *
 * Renders the avatar component exactly as real-world usage:
 *   val avatar = CometChatAvatar(context)
 *   avatar.setAvatar(user)
 *
 * Since Glide cannot load network images in Robolectric, the avatar displays
 * initials with the themed background — this is the expected fallback behavior.
 *
 * Note: MaterialCardView corner radius clipping is a GPU operation that
 * Robolectric's software renderer doesn't fully support. The avatar renders
 * with correct content (initials, colors, sizing) but without circular clipping.
 * This is a known Robolectric limitation, not a component bug.
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatAvatarScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatAvatarScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatAvatarScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/avatar"
        )
    )

    /**
     * Renders CometChatAvatar with a User object — the standard real-world usage.
     * The avatar displays initials with the themed background color.
     */
    @Test
    fun avatarWithUserInitials() {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val user = User().apply {
                uid = "user_1"
                name = "Iron Man"
                avatar = "https://data-us.cometchat.io/assets/images/avatars/ironman.png"
                status = CometChatConstants.USER_STATUS_ONLINE
            }

            // Create and configure avatar exactly like real usage
            val avatar = CometChatAvatar(activity).apply {
                setAvatar(user)
            }

            // Place in a container with explicit size (as it would appear in a list item)
            val container = FrameLayout(activity)
            val avatarSize = (48 * activity.resources.displayMetrics.density).toInt()
            val params = FrameLayout.LayoutParams(avatarSize, avatarSize).apply {
                gravity = Gravity.CENTER
            }
            container.addView(avatar, params)

            activity.setContentView(
                container,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        ShadowLooper.idleMainLooper()

        scenario.onActivity { activity ->
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }
}
