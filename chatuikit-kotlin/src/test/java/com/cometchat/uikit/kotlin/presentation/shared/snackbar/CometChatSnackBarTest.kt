package com.cometchat.uikit.kotlin.presentation.shared.snackbar

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import com.cometchat.uikit.kotlin.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * ENG-37011 — the attachment-rejection snackbar ([CometChatSnackBar]).
 *
 * The bug: picking a file over the server size limit produced no visible error at all. The fix
 * surfaces the SDK-provided rejection reason verbatim through this snackbar when the rejected
 * tray tile is tapped (the reason already carries the actual limit, e.g. "…exceeds 100 MB", so
 * the UIKit never recomputes it). These tests pin the component contract: the message text is
 * shown unmodified, a repeat show replaces the previous bar, and an unlaid-out anchor is a no-op.
 *
 * Run: ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatSnackBarTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CometChatSnackBarTest {

    private fun withThemedActivity(block: (ComponentActivity) -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            block(activity)
        }
        scenario.close()
    }

    /** An anchor with real dimensions — the bar sizes and positions itself from the anchor. */
    private fun laidOutAnchor(activity: ComponentActivity): View {
        val anchor = FrameLayout(activity)
        activity.setContentView(anchor, ViewGroup.LayoutParams(720, 120))
        anchor.measure(
            View.MeasureSpec.makeMeasureSpec(720, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(120, View.MeasureSpec.EXACTLY)
        )
        anchor.layout(0, 0, 720, 120)
        return anchor
    }

    private fun findTextView(root: View, predicate: (String) -> Boolean): TextView? {
        if (root is TextView && predicate(root.text.toString())) return root
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                findTextView(root.getChildAt(i), predicate)?.let { return it }
            }
        }
        return null
    }

    @Test
    fun `show surfaces the SDK rejection reason verbatim`() {
        withThemedActivity { activity ->
            val sdkMessage = "File size exceeds the maximum allowed limit of 100 MB"
            CometChatSnackBar.show(laidOutAnchor(activity), sdkMessage)

            val popup = shadowOf(activity.application).latestPopupWindow
            assertNotNull("snackbar popup expected on screen", popup)
            assertTrue(popup!!.isShowing)
            assertNotNull(
                "the SDK message must appear unmodified",
                findTextView(popup.contentView) { it == sdkMessage }
            )
        }
    }

    @Test
    fun `a second show replaces the bar instead of stacking`() {
        withThemedActivity { activity ->
            val anchor = laidOutAnchor(activity)
            CometChatSnackBar.show(anchor, "first reason")
            val first = shadowOf(activity.application).latestPopupWindow

            CometChatSnackBar.show(anchor, "second reason")
            val second = shadowOf(activity.application).latestPopupWindow

            assertFalse("first bar must be dismissed", first!!.isShowing)
            assertTrue(second!!.isShowing)
            assertNotNull(findTextView(second.contentView) { it == "second reason" })
        }
    }

    @Test
    fun `dismissCurrent hides the visible bar`() {
        withThemedActivity { activity ->
            CometChatSnackBar.show(laidOutAnchor(activity), "some reason")
            val popup = shadowOf(activity.application).latestPopupWindow
            assertTrue(popup!!.isShowing)

            CometChatSnackBar.dismissCurrent()
            assertFalse(popup.isShowing)
        }
    }

    @Test
    fun `an unlaid-out anchor is a safe no-op`() {
        withThemedActivity { activity ->
            // width 0 — the composer hasn't been laid out yet; showing must not crash or anchor
            CometChatSnackBar.show(View(activity), "too early")
            assertNull(shadowOf(activity.application).latestPopupWindow)
        }
    }
}
