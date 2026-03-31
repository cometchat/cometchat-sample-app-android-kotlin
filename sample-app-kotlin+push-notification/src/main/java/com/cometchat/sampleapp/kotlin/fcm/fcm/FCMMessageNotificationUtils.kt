package com.cometchat.sampleapp.kotlin.fcm.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spanned
import android.text.SpannedString
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chatuikit.logger.CometChatLogger
import com.cometchat.chatuikit.shared.spans.MarkdownConverter
import com.cometchat.sampleapp.kotlin.fcm.R
import com.cometchat.sampleapp.kotlin.fcm.utils.AppConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random
import java.util.concurrent.Executor
import java.util.concurrent.Executors


object FCMMessageNotificationUtils {
    private val TAG: String = FCMMessageNotificationUtils::class.java.simpleName
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private var existingNotificationMessages: MutableMap<String, String> = mutableMapOf()

    fun showNotification(
        context: Context, fcmMessageDTO: FCMMessageDTO, onNotificationClickIntent: Intent, actionButtonText: String, notificationCategory: String
    ) { // Set Notification Type
        onNotificationClickIntent.putExtra(
            AppConstants.FCMConstants.NOTIFICATION_TYPE, AppConstants.FCMConstants.NOTIFICATION_TYPE_MESSAGE
        )
        onNotificationClickIntent.putExtra(
            AppConstants.FCMConstants.NOTIFICATION_PAYLOAD, Gson().toJson(fcmMessageDTO)
        )
        val isUser = fcmMessageDTO.receiverType == CometChatConstants.RECEIVER_TYPE_USER // Group or User Account Avatar
        val avatarURL = if (isUser) fcmMessageDTO.senderAvatar else fcmMessageDTO.receiverAvatar

        getBitmapFromURL(avatarURL) { bitmap: Bitmap? -> // Group or User ID
            val userId = if (isUser) fcmMessageDTO.sender else fcmMessageDTO.receiver // Title for notification Group and user
            val userName = if (isUser) fcmMessageDTO.senderName else fcmMessageDTO.receiverName
            var notificationID = System.currentTimeMillis().toInt()
            val mNotificationManager = context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
            val notifications = mNotificationManager.activeNotifications
            var currentText: String? = null
            val currentMessageId = fcmMessageDTO.tag
            val isMessageDeleted = fcmMessageDTO.text.equals("Message deleted")

            if (!isMessageDeleted) {
                if (currentMessageId != null && fcmMessageDTO.text != null) {
                    existingNotificationMessages[currentMessageId] = fcmMessageDTO.text!!
                }
            }

            if (notifications.isEmpty()) {
                currentText =
                    if (isUser) fcmMessageDTO.text else fcmMessageDTO.senderName + " @ " + fcmMessageDTO.receiverName + ": " + fcmMessageDTO.text
            } else {
                var isFound = false
                for (notification in notifications) {
                    val extras = notification.notification.extras
                    val uid = extras.getString(AppConstants.FCMConstants.KEY_UID)
                    if (uid != null && uid == userId) {
                        // Use toString() to handle both String and SpannableString (from formatted notifications)
                        val mText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

                        if (isMessageDeleted) {
                            val originalMessage = existingNotificationMessages[currentMessageId]
                            if (originalMessage != null && mText != null) {
                                currentText = if (isUser) {
                                    mText.replace(originalMessage, "Message deleted")
                                } else {
                                    "${mText.replace(originalMessage, "Message deleted")}\n${fcmMessageDTO.senderName} @ ${fcmMessageDTO.receiverName}: ${fcmMessageDTO.text}"
                                }
                            }
                        } else {
                            currentText = if (isUser) {
                                "$mText\n${fcmMessageDTO.text}"
                            } else {
                                "$mText\n${fcmMessageDTO.senderName} @ ${fcmMessageDTO.receiverName}: ${fcmMessageDTO.text}"
                            }
                        }

                        notificationID = extras.getInt(AppConstants.FCMConstants.KEY_NOTIFICATION_ID)
                        isFound = true
                        break
                    }
                }
                if (!isFound) {
                    currentText =
                        if (isUser) fcmMessageDTO.text else fcmMessageDTO.senderName + " @ " + fcmMessageDTO.receiverName + ": " + fcmMessageDTO.text
                }
            }
            val mNotificationBuilder = getNotificationBuilder(
                context,
                userId,
                userName,
                bitmap,
                currentText,
                onNotificationClickIntent,
                actionButtonText,
                notificationID,
                notificationCategory,
                fcmMessageDTO
            )
            val notificationChannelRef = NotificationChannel(
                AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_ID,
                AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            mNotificationBuilder.setChannelId(AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(notificationChannelRef)
            val summaryNotification = NotificationCompat.Builder(
                context, AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_ID
            ).setSmallIcon(R.drawable.ic_cometchat_notification).setGroup(
                AppConstants.FCMConstants.GROUP_KEY
            ).setGroupSummary(true).build()

            mNotificationManager.notify(notificationID, mNotificationBuilder.build())
            mNotificationManager.notify(
                AppConstants.FCMConstants.NOTIFICATION_GROUP_SUMMARY_ID, summaryNotification
            )
        }
    }

    private fun getNotificationBuilder(
        context: Context,
        userId: String?,
        userName: String?,
        bitmap: Bitmap?,
        currentText: String?,
        onNotificationClickIntent: Intent,
        actionButtonText: String,
        notificationID: Int,
        notificationCategory: String,
        fcmMessageDTO: FCMMessageDTO
    ): NotificationCompat.Builder {
        val isUser = fcmMessageDTO.receiverType == CometChatConstants.RECEIVER_TYPE_USER
        val bundle = Bundle()
        bundle.putString(AppConstants.FCMConstants.KEY_UID, userId)
        bundle.putInt(AppConstants.FCMConstants.KEY_NOTIFICATION_ID, notificationID)

        onNotificationClickIntent.putExtra(
            AppConstants.FCMConstants.KEY_CLICKED_NOTIFICATION_ID, notificationID
        )
        onNotificationClickIntent.putExtra(
            AppConstants.FCMConstants.KEY_NOTIFICATION_SUMMARY_ID, AppConstants.FCMConstants.NOTIFICATION_GROUP_SUMMARY_ID
        )
        val pendingIntent = PendingIntent.getActivity(
            context, Random().nextInt(), onNotificationClickIntent, PendingIntent.FLAG_MUTABLE
        )
        val mNotificationBuilder = NotificationCompat.Builder(
            context, AppConstants.FCMConstants.DEFAULT_NOTIFICATION_CHANNEL_ID
        )

        // Format the message text for notification display
        // Convert markdown to HTML for rich text formatting in notifications
        val formattedContentText = formatNotificationText(fcmMessageDTO.text)
        val formattedBigText = formatNotificationText(currentText)

        val contentSpanned = fromHtml(formattedContentText)
        val bigTextSpanned = fromHtml(formattedBigText)

        mNotificationBuilder.setSmallIcon(R.drawable.ic_cometchat_notification).setContentTitle(userName).setContentText(contentSpanned).setStyle(
            NotificationCompat.BigTextStyle().bigText(bigTextSpanned)
        ).setGroup(AppConstants.FCMConstants.GROUP_KEY).setExtras(bundle).setPriority(
            NotificationCompat.PRIORITY_HIGH
        ).setCategory(notificationCategory).setContentIntent(pendingIntent).addAction(
            handleReplyFromNotification(
                context, notificationID, actionButtonText, fcmMessageDTO
            )
        ).setAutoCancel(true).setOnlyAlertOnce(true)

        fcmMessageDTO.unreadMessageCount?.toIntOrNull()?.takeIf { it >= 0 }?.let { count ->
            mNotificationBuilder.setNumber(count)
            mNotificationBuilder.setSubText("$count unread messages")
        }

        if (bitmap != null) {
            mNotificationBuilder.setLargeIcon(bitmap)
        } else {
            mNotificationBuilder.setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources, if (isUser) R.drawable.ic_notification_user_placeholder else R.drawable.ic_notification_group_placeholder
                )
            )
        }
        return mNotificationBuilder
    }

    /**
     * Formats notification text by converting markdown to HTML.
     * This enables rich text formatting (bold, italic, etc.) in notifications.
     *
     * @param text The raw text that may contain markdown formatting.
     * @return HTML-formatted text suitable for notification display.
     */
    private fun formatNotificationText(text: String?): String {
        if (text.isNullOrEmpty()) {
            return ""
        }
        return try {
            MarkdownConverter.toHtml(text)
        } catch (e: Exception) {
            CometChatLogger.e(TAG, "Error converting markdown to HTML: ${e.message}")
            text // Return original text if conversion fails
        }
    }

    /**
     * Converts HTML string to Spanned for notification display.
     * Handles API level differences for Html.fromHtml().
     *
     * @param html The HTML string to convert.
     * @return Spanned text for notification display.
     */
    @Suppress("DEPRECATION")
    private fun fromHtml(html: String?): Spanned {
        if (html.isNullOrEmpty()) {
            return SpannedString("")
        }
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(html)
            }
        } catch (e: Exception) {
            CometChatLogger.e(TAG, "Error converting HTML to Spanned: ${e.message}")
            SpannedString(html) // Return as plain text if conversion fails
        }
    }

    private fun handleReplyFromNotification(
        context: Context, notificationID: Int, actionButtonText: String, fcmMessageDTO: FCMMessageDTO
    ): NotificationCompat.Action {
        val replyIntent = Intent(context, FCMMessageBroadcastReceiver::class.java)

        replyIntent.putExtra(AppConstants.FCMConstants.KEY_DATA, Gson().toJson(fcmMessageDTO))
        replyIntent.putExtra(AppConstants.FCMConstants.KEY_CLICKED_NOTIFICATION_ID, notificationID)

        replyIntent.setAction(AppConstants.FCMConstants.NOTIFICATION_REPLY_ACTION)
        val replyPendingIntent = PendingIntent.getBroadcast(
            context, Random().nextInt(), replyIntent, PendingIntent.FLAG_MUTABLE
        )
        val remoteInput = RemoteInput.Builder(AppConstants.FCMConstants.REPLY_FROM_NOTIFICATION).setLabel(actionButtonText).build()
        return NotificationCompat.Action.Builder(
            R.drawable.ic_cometchat_notification, actionButtonText, replyPendingIntent
        ).addRemoteInput(remoteInput).build()
    }

    private fun getBitmapFromURL(
        strURL: String?, callback: Callback
    ) {
        executor.execute {
            if (strURL == null) {
                Handler(Looper.getMainLooper()).post { callback.onResult(null) }
                return@execute
            }
            try {
                val url = URL(strURL)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val bitmap = BitmapFactory.decodeStream(connection.inputStream)
                val circleBitmap = getCircleBitmap(
                    bitmap
                )
                Handler(Looper.getMainLooper()).post { callback.onResult(circleBitmap) }
            } catch (e: Exception) {
                CometChatLogger.e(
                    TAG, e.toString()
                )
                Handler(Looper.getMainLooper()).post { callback.onResult(null) }
            }
        }
    }

    private fun getCircleBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.RED
        canvas.drawOval(rectF, paint)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)
        bitmap.recycle()
        return output
    }

    fun interface Callback {
        fun onResult(bitmap: Bitmap?)
    }
}
