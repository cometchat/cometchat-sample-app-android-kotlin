package com.cometchat.uikit.kotlin.shared.resources.utils

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import androidx.lifecycle.LifecycleOwner
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.localise.CometChatLocalize
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.cometchat.uikit.kotlin.presentation.shared.mediaviewer.CometChatImageViewerActivity
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utility class providing common helper methods for CometChat UIKit components.
 */
object Utils {
    
    private const val TAG = "Utils"
    
    /**
     * Hides the soft keyboard.
     *
     * @param context The context
     * @param view The view to get the window token from
     */
    fun hideKeyBoard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Checks if a user is blocked (either blocked by me or has blocked me).
     *
     * @param user The user to check
     * @return true if the user is blocked, false otherwise
     */
    fun isBlocked(user: User): Boolean {
        return user.isBlockedByMe || user.isHasBlockedMe
    }

    /**
     * Initializes a MaterialCardView with default transparent styling.
     * This removes background color, elevation, radius, and stroke.
     *
     * @param view The MaterialCardView to initialize
     */
    fun initMaterialCard(view: MaterialCardView) {
        view.setCardBackgroundColor(Color.TRANSPARENT)
        view.cardElevation = 0f
        view.radius = 0f
        view.strokeWidth = 0
    }
    
    /**
     * Converts dp (density-independent pixels) to pixels.
     *
     * @param context The context to get display metrics from
     * @param dp The value in dp to convert
     * @return The value in pixels
     */
    fun convertDpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
    
    /**
     * Converts dp (density-independent pixels) to pixels.
     *
     * @param context The context to get display metrics from
     * @param dp The value in dp to convert (as float)
     * @return The value in pixels
     */
    fun convertDpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }
    
    /**
     * Gets the LifecycleOwner from a Context.
     * This traverses the context hierarchy to find an Activity that implements LifecycleOwner.
     *
     * @param context The context to get the LifecycleOwner from
     * @return The LifecycleOwner if found, null otherwise
     */
    fun getLifecycleOwner(context: Context): LifecycleOwner? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is LifecycleOwner) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }
    
    /**
     * Gets the Activity from a Context.
     * This traverses the context hierarchy to find an Activity.
     *
     * @param context The context to get the Activity from
     * @return The Activity if found, null otherwise
     */
    fun getActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Checks if an Activity is usable (not null, not finishing, and not destroyed).
     * This is useful for safely performing operations on an Activity.
     *
     * @param activity The Activity to check
     * @return true if the Activity is usable, false otherwise
     */
    fun isActivityUsable(activity: Activity?): Boolean {
        return activity != null && !activity.isFinishing && !activity.isDestroyed
    }
    
    /**
     * Handles adding a view to a ViewGroup container.
     * If the view is not null, it removes all existing views from the layout,
     * removes the view from its parent if it has one, and adds it to the layout.
     * If the view is null and hideIfNull is true, the layout is hidden.
     *
     * @param layout The ViewGroup container to add the view to
     * @param view The view to add (can be null)
     * @param hideIfNull If true, hides the layout when view is null
     */
    fun handleView(layout: ViewGroup, view: View?, hideIfNull: Boolean) {
        if (view != null) {
            layout.removeAllViews()
            removeParentFromView(view)
            layout.visibility = View.VISIBLE
            layout.addView(view)
        } else {
            if (hideIfNull) {
                layout.visibility = View.GONE
            }
        }
    }
    
    /**
     * Removes a view from its parent if it has one.
     *
     * @param view The view to remove from its parent
     */
    private fun removeParentFromView(view: View) {
        val parent = view.parent
        if (parent is ViewGroup) {
            parent.removeView(view)
        }
    }
    
    /**
     * Formats a timestamp for call logs display.
     *
     * Format:
     * - If the year matches the current year: "8 August, 8:14 PM"
     * - If the year is different: "8 August 2022, 8:14 PM"
     *
     * @param timestamp The timestamp in seconds or milliseconds (auto-detected)
     * @param dateFormat Optional custom date format. If null, default format is applied.
     * @return A formatted date string
     */
    fun callLogsTimeStamp(timestamp: Long, dateFormat: SimpleDateFormat? = null): String {
        var timestampMs = timestamp

        // Convert seconds to milliseconds if needed
        if (timestamp.toString().length == 10) {
            timestampMs = timestamp * 1000
        }

        val date = Date(timestampMs)
        val calendar = Calendar.getInstance()
        calendar.time = date

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val inputYear = calendar.get(Calendar.YEAR)

        // Apply default formats if dateFormat is null
        val format = dateFormat ?: if (inputYear == currentYear) {
            SimpleDateFormat("d MMMM, h:mm a", CometChatLocalize.getDefault())
        } else {
            SimpleDateFormat("d MMMM yyyy, h:mm a", CometChatLocalize.getDefault())
        }

        return format.format(date)
    }

    /**
     * Returns a formatted string indicating when the user was last seen.
     *
     * @param context The context for localization
     * @param timestamp The timestamp in milliseconds (or seconds, will be auto-detected)
     * @return A string describing when the user was last seen (e.g., "Last seen 5 mins ago")
     */
    fun getLastSeenTime(context: Context, timestamp: Long): String {
        var timestampMs = timestamp
        
        // Convert seconds to milliseconds if needed
        if (timestamp.toString().length == 10) {
            timestampMs = timestamp * 1000
        }
        
        val now = Calendar.getInstance()
        val lastSeen = Calendar.getInstance()
        lastSeen.timeInMillis = timestampMs
        
        val diffInMillis = now.timeInMillis - lastSeen.timeInMillis
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        
        val lastSeenPrefix = context.getString(R.string.cometchat_last_seen)
        
        // Check if the timestamp is within the last hour
        return when {
            diffInMinutes == 0L -> {
                "$lastSeenPrefix ${context.resources.getQuantityString(
                    R.plurals.cometchat_last_seen_minutes_ago, 1, 1
                )}"
            }
            diffInMinutes < 60 -> {
                "$lastSeenPrefix ${context.resources.getQuantityString(
                    R.plurals.cometchat_last_seen_minutes_ago, 
                    diffInMinutes.toInt(), 
                    diffInMinutes.toInt()
                )}"
            }
            diffInHours < 24 -> {
                "$lastSeenPrefix ${context.resources.getQuantityString(
                    R.plurals.cometchat_last_seen_hours_ago, 
                    diffInHours.toInt(), 
                    diffInHours.toInt()
                )}"
            }
            else -> {
                // Determine if the timestamp is within the current year
                val isSameYear = lastSeen.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                val datePattern = if (isSameYear) {
                    "dd MMM 'at' hh:mm a"
                } else {
                    "dd MMM yyyy 'at' hh:mm a"
                }
                
                val dateFormat = SimpleDateFormat(datePattern, Locale.getDefault())
                "$lastSeenPrefix ${dateFormat.format(Date(timestampMs))}"
            }
        }
    }
    
    /**
     * Performs a click on the adapter item by finding the parent RecyclerView
     * and triggering a long click on the item view.
     *
     * @param view The view that was clicked
     */
    fun performAdapterClick(view: View) {
        // Find the parent that is a direct child of RecyclerView
        var parent = view.parent
        while (parent != null && parent !is androidx.recyclerview.widget.RecyclerView) {
            if (parent is View) {
                parent = parent.parent
            } else {
                break
            }
        }
        // Trigger long click on the item view if found
        if (parent is androidx.recyclerview.widget.RecyclerView) {
            val itemView = findItemView(view, parent)
            itemView?.performLongClick()
        }
    }
    
    private fun findItemView(view: View, recyclerView: androidx.recyclerview.widget.RecyclerView): View? {
        var current: View? = view
        while (current != null && current.parent != recyclerView) {
            current = current.parent as? View
        }
        return current
    }
    
    /**
     * Checks if a file is a GIF based on its extension.
     *
     * @param file The file to check
     * @return true if the file is a GIF, false otherwise
     */
    fun isGifFile(file: File?): Boolean {
        if (file == null) return false
        val name = file.name.lowercase(Locale.getDefault())
        return name.endsWith(".gif")
    }
    
    /**
     * Shows a BottomSheetDialog with the given view content.
     * Matches the original Java Utils.showBottomSheet behavior.
     *
     * @param context The context
     * @param bottomSheetDialog The BottomSheetDialog to show
     * @param isCancelable Whether the dialog is cancelable
     * @param openHalfScreen Whether to open at half screen height
     * @param view The content view to display in the bottom sheet
     */
    fun showBottomSheet(
        context: Context,
        bottomSheetDialog: BottomSheetDialog,
        isCancelable: Boolean,
        openHalfScreen: Boolean,
        view: View
    ) {
        try {
            removeParentFromView(view)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.setOnShowListener {
                val bottomSheet = bottomSheetDialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
                if (bottomSheet != null) {
                    bottomSheet.setBackgroundResource(R.color.cometchat_color_transparent)
                    val behavior = BottomSheetBehavior.from(bottomSheet)
                    if (openHalfScreen) {
                        val halfHeight = (context.resources.displayMetrics.heightPixels * 0.5).toInt()
                        behavior.peekHeight = halfHeight
                        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        bottomSheet.layoutParams.height = halfHeight
                    } else {
                        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    bottomSheet.requestLayout()
                }
            }
            bottomSheetDialog.setCancelable(isCancelable)
            bottomSheetDialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing bottom sheet", e)
        }
    }

    /**
     * Opens an image viewer activity to display images.
     *
     * @param imageView The view that was clicked (used to obtain context)
     * @param urls List of image URLs to display
     * @param mimeTypes List of MIME types for each image
     * @param fileNames List of file names for each image
     */
    fun openImageViewer(
        imageView: View,
        urls: List<String>,
        mimeTypes: List<String>,
        fileNames: List<String>
    ) {
        val intent = CometChatImageViewerActivity.createIntent(
            imageView.context, urls, mimeTypes, fileNames
        )
        imageView.context.startActivity(intent)
    }

    /**
     * Calculates a unique date ID from a timestamp in milliseconds.
     *
     * Returns a Long in YYYYMMDD format for consistent date comparison.
     * This is used for sticky header grouping in message lists.
     *
     * @param timestampMillis The timestamp in milliseconds
     * @return A Long representing the date in YYYYMMDD format
     */
    fun getDateId(timestampMillis: Long): Long {
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeInMillis = timestampMillis
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return (year * 10000L) + (month * 100L) + day
    }

    /**
     * Downloads a file from a URL using the system DownloadManager.
     *
     * @param context The context to use
     * @param url The URL of the file to download
     * @param fileName The name to save the file as
     */
    fun downloadFile(context: Context, url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setTitle(fileName)
            request.setDescription("Downloading file...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file: ${e.message}")
        }
    }

    /**
     * Formats a file size in bytes to a human-readable string.
     *
     * @param size The file size in bytes
     * @return A formatted string (e.g., "1.5 MB", "256 KB")
     */
    fun getFileSize(size: Int): String {
        return getFileSize(size.toLong())
    }

    /**
     * Formats a file size in bytes to a human-readable string.
     *
     * @param size The file size in bytes
     * @return A formatted string (e.g., "1.5 MB", "256 KB")
     */
    fun getFileSize(size: Long): String {
        if (size <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)

        val sizeValue = size / Math.pow(1024.0, index.toDouble())
        return if (sizeValue == sizeValue.toLong().toDouble()) {
            "${sizeValue.toLong()} ${units[index]}"
        } else {
            String.format("%.1f %s", sizeValue, units[index])
        }
    }

    /**
     * Gets the file from the local path stored in the message metadata.
     *
     * @param message The media message
     * @return The File if it exists, null otherwise
     */
    fun getFileFromLocalPath(message: MediaMessage): File? {
        return try {
            val metadata = message.metadata ?: return null
            if (metadata.has(UIKitConstants.IntentStrings.PATH)) {
                val path = metadata.getString(UIKitConstants.IntentStrings.PATH)
                val file = File(path)
                if (file.exists()) file else null
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file from local path: ${e.message}")
            null
        }
    }

    /**
     * Gets the MIME type from a file.
     *
     * @param context The context to use
     * @param file The file to check
     * @return The MIME type string
     */
    fun getMimeTypeFromFile(context: Context, file: File): String {
        val extension = file.extension.lowercase(Locale.getDefault())
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return mimeType ?: "application/octet-stream"
    }
}
