package com.cometchat.uikit.kotlin.presentation.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.WindowManager
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage

/**
 * Captures ALL windows (Activity DecorView + PopupWindows) into a single composite bitmap.
 *
 * Why this is needed:
 *   [Activity.window.decorView.captureRoboImage()] only captures the activity's root window.
 *   PopupWindow attaches its content to a SEPARATE window via WindowManager, so it's invisible
 *   to decorView-based capture.
 *
 * How it works:
 *   WindowManagerGlobal.mViews contains ALL registered window root views in z-order:
 *     [0] Activity DecorView
 *     [1] PopupWindow root view (if showing)
 *     [2] Another popup, dialog, etc.
 *
 *   We draw each view onto a single canvas at its (x, y) offset from LayoutParams,
 *   producing the same composite the user sees on screen.
 *
 * Usage:
 *   activity.captureWithPopups("src/test/snapshots/popup/my_test.png")
 */

@Suppress("UNCHECKED_CAST")
fun allWindowManagerViews(): List<Pair<View, WindowManager.LayoutParams>> {
    val global = Class.forName("android.view.WindowManagerGlobal")
        .getMethod("getInstance").invoke(null)

    val views = global.javaClass
        .getDeclaredField("mViews")
        .apply { isAccessible = true }
        .get(global) as ArrayList<View>

    val params = global.javaClass
        .getDeclaredField("mParams")
        .apply { isAccessible = true }
        .get(global) as ArrayList<WindowManager.LayoutParams>

    return views.zip(params)
}

/**
 * Captures the activity's full visual state including any PopupWindows,
 * and writes the result as a Roborazzi golden image.
 *
 * @param filePath relative path for the output PNG (e.g., "src/test/snapshots/popup/test.png")
 */
fun Activity.captureWithPopups(filePath: String, roborazziOptions: RoborazziOptions = RoborazziOptions()) {
    val decor = window.decorView
    val w = decor.width.coerceAtLeast(1)
    val h = decor.height.coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    allWindowManagerViews().forEach { (view, params) ->
        // Force measure/layout if popup hasn't been laid out yet
        if (view.width == 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.AT_MOST)
            )
            view.layout(params.x, params.y, params.x + view.measuredWidth, params.y + view.measuredHeight)
        }
        canvas.save()
        canvas.translate(params.x.toFloat(), params.y.toFloat())
        view.draw(canvas)
        canvas.restore()
    }

    bitmap.captureRoboImage(filePath, roborazziOptions = roborazziOptions)
}

/**
 * Overload that uses Roborazzi's automatic file naming via RoborazziRule.
 * The output path is determined by the test class + method name.
 */
fun Activity.captureWithPopups(roborazziOptions: RoborazziOptions = RoborazziOptions()) {
    val decor = window.decorView
    val w = decor.width.coerceAtLeast(1)
    val h = decor.height.coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    allWindowManagerViews().forEach { (view, params) ->
        // Force measure/layout if popup hasn't been laid out yet
        if (view.width == 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.AT_MOST)
            )
            view.layout(params.x, params.y, params.x + view.measuredWidth, params.y + view.measuredHeight)
        }
        canvas.save()
        canvas.translate(params.x.toFloat(), params.y.toFloat())
        view.draw(canvas)
        canvas.restore()
    }

    // Use Roborazzi's auto-naming (RoborazziRule determines the file path)
    bitmap.captureRoboImage(roborazziOptions = roborazziOptions)
}
