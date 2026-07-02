package com.cometchat.uikit.kotlin.presentation.polls

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.core.domain.usecase.CreatePollUseCase
import com.cometchat.uikit.core.viewmodel.CometChatCreatePollViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.polls.style.CometChatCreatePollStyle
import com.cometchat.uikit.kotlin.presentation.polls.ui.CometChatCreatePoll
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatCreatePoll (Kotlin XML View).
 *
 * Captures golden images for ALL visual states of the CometChatCreatePoll component
 * using Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * Each test method produces one golden PNG in src/test/snapshots/createpoll/.
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatCreatePollScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatCreatePollScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatCreatePollScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/createpoll"
        )
    )

    // ==================== Helper Methods ====================

    private fun launchAndCapture(configure: (ComponentActivity) -> View) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)
            activity.setContentView(view)
            ShadowLooper.idleMainLooper()
        }
        scenario.onActivity { activity ->
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    private fun createComponentView(activity: ComponentActivity): CometChatCreatePoll {
        return CometChatCreatePoll(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    // ==================== UI States ====================

    @Test
    fun stateIdle() {
        launchAndCapture { activity ->
            val view = createComponentView(activity)
            // Default state: empty form, submit button disabled
            view
        }
    }

    @Test
    fun stateWithQuestion() {
        launchAndCapture { activity ->
            val view = createComponentView(activity)
            ShadowLooper.idleMainLooper()
            // Set question text via EditText
            val etQuestion = view.findViewById<android.widget.EditText>(R.id.etQuestion)
            etQuestion.setText("Where should we go for the team outing this weekend?")
            ShadowLooper.idleMainLooper()
            view
        }
    }

    @Test
    fun stateWithOptions() {
        launchAndCapture { activity ->
            val view = createComponentView(activity)
            ShadowLooper.idleMainLooper()
            // Set question
            val etQuestion = view.findViewById<android.widget.EditText>(R.id.etQuestion)
            etQuestion.setText("What's your favorite programming language?")
            ShadowLooper.idleMainLooper()
            // Set options via RecyclerView adapter EditTexts
            val rvOptions = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvOptions)
            rvOptions.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.AT_MOST)
            )
            rvOptions.layout(0, 0, 1080, 2160)
            ShadowLooper.idleMainLooper()
            // Fill first option
            val holder0 = rvOptions.findViewHolderForAdapterPosition(0)
            holder0?.itemView?.findViewById<android.widget.EditText>(R.id.et_option)?.setText("Kotlin")
            ShadowLooper.idleMainLooper()
            // Fill second option
            val holder1 = rvOptions.findViewHolderForAdapterPosition(1)
            holder1?.itemView?.findViewById<android.widget.EditText>(R.id.et_option)?.setText("Swift")
            ShadowLooper.idleMainLooper()
            view
        }
    }

    @Test
    fun stateSubmitting() {
        launchAndCapture { activity ->
            val view = createComponentView(activity)
            // Show progress indicator
            view.setProgressVisibility(View.VISIBLE)
            ShadowLooper.idleMainLooper()
            view
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            val view = createComponentView(activity)
            // Show error message
            view.setErrorStateVisibility(View.VISIBLE)
            view.setErrorMessage("Failed to create poll. Please try again.")
            ShadowLooper.idleMainLooper()
            view
        }
    }

    // ==================== Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            val view = createComponentView(activity)
            ShadowLooper.idleMainLooper()
            view
        }
    }

    // ==================== Style ====================

    @Test
    fun styleCustomBackground() {
        launchAndCapture { activity ->
            val view = createComponentView(activity)
            val customStyle = CometChatCreatePollStyle(
                backgroundColor = android.graphics.Color.parseColor("#F5F5DC"),
                titleTextColor = android.graphics.Color.parseColor("#333333"),
                separatorColor = android.graphics.Color.parseColor("#CCCCCC"),
                submitButtonBackgroundColor = android.graphics.Color.parseColor("#6851D6"),
                disabledSubmitButtonBackgroundColor = android.graphics.Color.parseColor("#DDDDDD"),
                submitButtonTextColor = android.graphics.Color.WHITE
            )
            view.setStyle(customStyle)
            ShadowLooper.idleMainLooper()
            view
        }
    }

    // ==================== Visibility Toggles ====================

    @Test
    fun visibilityNoToolbar() {
        launchAndCapture { activity ->
            val view = createComponentView(activity)
            view.setHideToolbar(true)
            ShadowLooper.idleMainLooper()
            view
        }
    }

    // ==================== Content Variants ====================

    @Test
    fun contentManyOptions() {
        launchAndCapture { activity ->
            val view = createComponentView(activity)
            ShadowLooper.idleMainLooper()
            // Set question
            val etQuestion = view.findViewById<android.widget.EditText>(R.id.etQuestion)
            etQuestion.setText("Which day works best for the sprint retrospective?")
            ShadowLooper.idleMainLooper()
            // Set options via RecyclerView adapter EditTexts
            val rvOptions = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvOptions)
            rvOptions.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.AT_MOST)
            )
            rvOptions.layout(0, 0, 1080, 2160)
            ShadowLooper.idleMainLooper()
            // Fill first option
            val holder0 = rvOptions.findViewHolderForAdapterPosition(0)
            holder0?.itemView?.findViewById<android.widget.EditText>(R.id.et_option)?.setText("Monday")
            ShadowLooper.idleMainLooper()
            // Fill second option
            val holder1 = rvOptions.findViewHolderForAdapterPosition(1)
            holder1?.itemView?.findViewById<android.widget.EditText>(R.id.et_option)?.setText("Wednesday")
            ShadowLooper.idleMainLooper()
            // Third option (auto-added after first two are filled)
            rvOptions.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.AT_MOST)
            )
            rvOptions.layout(0, 0, 1080, 2160)
            ShadowLooper.idleMainLooper()
            val holder2 = rvOptions.findViewHolderForAdapterPosition(2)
            holder2?.itemView?.findViewById<android.widget.EditText>(R.id.et_option)?.setText("Friday")
            ShadowLooper.idleMainLooper()
            view
        }
    }
}
