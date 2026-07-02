package com.cometchat.uikit.compose.presentation.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.WindowManager
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage

/**
 * Captures ALL windows (Activity DecorView + Popup/DropdownMenu windows) into a single bitmap.
 *
 * In Compose, DropdownMenu creates a separate Popup window registered in WindowManagerGlobal.
 * Standard captureRoboImage() on a single root only captures that one root.
 * This utility composites all windows together — same approach as chatuikit-kotlin.
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
 * Captures the activity's full visual state including any Compose Popup/DropdownMenu windows.
 * Uses Roborazzi's auto-naming via RoborazziRule.
 */
fun Activity.captureWithPopups(roborazziOptions: RoborazziOptions = RoborazziOptions()) {
    val decor = window.decorView
    val w = decor.width.coerceAtLeast(1)
    val h = decor.height.coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    allWindowManagerViews().forEach { (view, params) ->
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

    bitmap.captureRoboImage(roborazziOptions = roborazziOptions)
}
