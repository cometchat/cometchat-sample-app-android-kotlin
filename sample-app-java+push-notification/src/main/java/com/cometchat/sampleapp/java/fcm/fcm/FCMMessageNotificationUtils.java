package com.cometchat.sampleapp.java.fcm.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.sampleapp.java.fcm.R;
import com.cometchat.sampleapp.java.fcm.utils.AppConstants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FCMMessageNotificationUtils {
    private static final String TAG = FCMMessageNotificationUtils.class.getSimpleName();
    private static Map<String, String> existingNotificationMessages = new HashMap<>();
    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static void showNotification(
        Context context,
        FCMCallDto fcmCallDto,
        Intent onNotificationClickIntent,
        String actionButtonText,
        String notificationCategory
    ) {

        // Set Notification Type
        onNotificationClickIntent.putExtra(AppConstants.FCMConstants.NOTIFICATION_TYPE, AppConstants.FCMConstants.NOTIFICATION_TYPE_CALL);
        onNotificationClickIntent.putExtra(AppConstants.FCMConstants.NOTIFICATION_PAYLOAD, new Gson().toJson(fcmCallDto));

        boolean isUser = fcmCallDto.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER);

        // Group or User Account Avatar
        String avatarURL = isUser ? fcmCallDto.getSenderAvatar() : fcmCallDto.getReceiverAvatar();

        getBitmapFromURL(avatarURL, bitmap -> {
            // Group or User ID
            String userId = isUser ? fcmCallDto.getSender() : fcmCallDto.getReceiver();

            // Title for notification Group and user
            String userName = isUser ? fcmCallDto.getSenderName() : fcmCallDto.getReceiverName();

            int notificationID = (int) System.currentTimeMillis();
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE);

            String currentText = fcmCallDto.getBody();

            NotificationCompat.Builder mNotificationBuilder = getNotificationBuilder(
                context,
                userId,
                userName,
                bitmap,
                currentText,
                onNotificationClickIntent,
                actionButtonText,
                notificationID,
                notificationCategory,
                fcmCallDto
            );

            NotificationChannel notificationChannelRef = new NotificationChannel(
                AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_ID,
                AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            mNotificationBuilder.setChannelId(AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannelRef);

            Notification summaryNotification = new NotificationCompat.Builder(context, AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_cometchat_notification)
                .setGroup(AppConstants.FCMConstants.GROUP_KEY)
                .setGroupSummary(true)
                .build();

            mNotificationManager.notify(notificationID, mNotificationBuilder.build());
            mNotificationManager.notify(AppConstants.FCMConstants.NOTIFICATION_GROUP_SUMMARY_ID, summaryNotification);
        });
    }

    public static void getBitmapFromURL(String strURL, Callback callback) {
        executor.execute(() -> {
            if (strURL == null) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onResult(null));
                return;
            }
            try {
                URL url = new URL(strURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                Bitmap circleBitmap = getCircleBitmap(bitmap);
                new Handler(Looper.getMainLooper()).post(() -> callback.onResult(circleBitmap));
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
                new Handler(Looper.getMainLooper()).post(() -> callback.onResult(null));
            }
        });
    }

    private static NotificationCompat.Builder getNotificationBuilder(
        Context context,
        String userId,
        String userName,
        Bitmap bitmap,
        String currentText,
        Intent onNotificationClickIntent, String actionButtonText,
        int notificationID,
        String notificationCategory,
        FCMCallDto fcmCallDto
    ) {
        boolean isUser = fcmCallDto.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER);

        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.FCMConstants.KEY_UID, userId);
        bundle.putInt(AppConstants.FCMConstants.KEY_NOTIFICATION_ID, notificationID);

        onNotificationClickIntent.putExtra(AppConstants.FCMConstants.KEY_CLICKED_NOTIFICATION_ID, notificationID);
        onNotificationClickIntent.putExtra(AppConstants.FCMConstants.KEY_NOTIFICATION_SUMMARY_ID,
                                           AppConstants.FCMConstants.NOTIFICATION_GROUP_SUMMARY_ID);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            new Random().nextInt(),
            onNotificationClickIntent,
            PendingIntent.FLAG_MUTABLE
        );
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(
            context,
            AppConstants.FCMConstants.DEFAULT_NOTIFICATION_CHANNEL_ID
        );

        mNotificationBuilder
            .setSmallIcon(R.drawable.ic_cometchat_notification)
            .setContentTitle(userName)
            .setContentText(fcmCallDto.getBody())
            .setStyle(new NotificationCompat.BigTextStyle().bigText(currentText))
            .setGroup(AppConstants.FCMConstants.GROUP_KEY)
            .setExtras(bundle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(notificationCategory)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true);

        if (bitmap != null) {
            mNotificationBuilder.setLargeIcon(bitmap);
        } else {
            mNotificationBuilder.setLargeIcon(BitmapFactory.decodeResource(
                context.getResources(),
                isUser ? R.drawable.ic_notification_user_placeholder : R.drawable.ic_notification_group_placeholder
            ));
        }
        return mNotificationBuilder;
    }

    private static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.RED);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

    public static void showNotification(
        Context context,
        FCMMessageDTO fcmMessageDTO,
        Intent onNotificationClickIntent,
        String actionButtonText,
        String notificationCategory
    ) {

        // Set Notification Type
        onNotificationClickIntent.putExtra(AppConstants.FCMConstants.NOTIFICATION_TYPE, AppConstants.FCMConstants.NOTIFICATION_TYPE_MESSAGE);
        onNotificationClickIntent.putExtra(AppConstants.FCMConstants.NOTIFICATION_PAYLOAD, new Gson().toJson(fcmMessageDTO));

        boolean isUser = fcmMessageDTO.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER);

        // Group or User Account Avatar
        String avatarURL = isUser ? fcmMessageDTO.getSenderAvatar() : fcmMessageDTO.getReceiverAvatar();

        getBitmapFromURL(avatarURL, bitmap -> {
            // Group or User ID
            String userId = isUser ? fcmMessageDTO.getSender() : fcmMessageDTO.getReceiver();

            // Title for notification Group and user
            String userName = isUser ? fcmMessageDTO.getSenderName() : fcmMessageDTO.getReceiverName();

            int notificationID = (int) System.currentTimeMillis();
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE);
            StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();

            String currentText = null;
            String currentMessageId = fcmMessageDTO.getTag();
            boolean isMessageDeleted = fcmMessageDTO.getText().equalsIgnoreCase("message deleted");

            if (!isMessageDeleted) {
                existingNotificationMessages.put(currentMessageId, fcmMessageDTO.getText());
            }

            if (notifications.length == 0) {
                currentText = isUser ? fcmMessageDTO.getText() : fcmMessageDTO.getSenderName() + " @ " + fcmMessageDTO.getReceiverName() + ": " + fcmMessageDTO.getText();
            } else {
                boolean isFound = false;
                for (StatusBarNotification notification : notifications) {
                    Bundle extras = notification.getNotification().extras;
                    String UID = extras.getString(AppConstants.FCMConstants.KEY_UID);
                    if (UID != null && UID.equals(userId)) {
                        String mText = (String) extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
                        if (isMessageDeleted) {
                            String originalMessage = existingNotificationMessages.get(currentMessageId);
                            if (originalMessage != null & mText != null)
                                currentText = isUser ? mText.replace(originalMessage, "Message Deleted") : mText.replace(originalMessage, "Message Deleted") + "\n" + fcmMessageDTO.getSenderName() + " @ " + fcmMessageDTO.getReceiverName() + ": " + fcmMessageDTO.getText();
                        } else {
                            currentText = isUser ? mText + "\n" + fcmMessageDTO.getText() : mText + "\n" + fcmMessageDTO.getSenderName() + " @ " + fcmMessageDTO.getReceiverName() + ": " + fcmMessageDTO.getText();
                        }
                        notificationID = extras.getInt(AppConstants.FCMConstants.KEY_NOTIFICATION_ID);
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    currentText = isUser ? fcmMessageDTO.getText() : fcmMessageDTO.getSenderName() + " @ " + fcmMessageDTO.getReceiverName() + ": " + fcmMessageDTO.getText();
                }
            }

            NotificationCompat.Builder mNotificationBuilder = getNotificationBuilder(
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
            );

            NotificationChannel notificationChannelRef = new NotificationChannel(
                AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_ID,
                AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            mNotificationBuilder.setChannelId(AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannelRef);

            Notification summaryNotification = new NotificationCompat.Builder(context, AppConstants.FCMConstants.MESSAGE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_cometchat_notification)
                .setGroup(AppConstants.FCMConstants.GROUP_KEY)
                .setGroupSummary(true)
                .build();

            mNotificationManager.notify(notificationID, mNotificationBuilder.build());
            mNotificationManager.notify(AppConstants.FCMConstants.NOTIFICATION_GROUP_SUMMARY_ID, summaryNotification);
        });
    }

    private static NotificationCompat.Builder getNotificationBuilder(
        Context context,
        String userId,
        String userName,
        Bitmap bitmap,
        String currentText,
        Intent onNotificationClickIntent, String actionButtonText,
        int notificationID,
        String notificationCategory,
        FCMMessageDTO fcmMessageDTO
    ) {
        boolean isUser = fcmMessageDTO.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER);

        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.FCMConstants.KEY_UID, userId);
        bundle.putInt(AppConstants.FCMConstants.KEY_NOTIFICATION_ID, notificationID);

        onNotificationClickIntent.putExtra(AppConstants.FCMConstants.KEY_CLICKED_NOTIFICATION_ID, notificationID);
        onNotificationClickIntent.putExtra(AppConstants.FCMConstants.KEY_NOTIFICATION_SUMMARY_ID,
                                           AppConstants.FCMConstants.NOTIFICATION_GROUP_SUMMARY_ID);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            new Random().nextInt(),
            onNotificationClickIntent,
            PendingIntent.FLAG_MUTABLE
        );
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(
            context,
            AppConstants.FCMConstants.DEFAULT_NOTIFICATION_CHANNEL_ID
        );

        mNotificationBuilder
            .setSmallIcon(R.drawable.ic_cometchat_notification)
            .setContentTitle(userName)
            .setContentText(fcmMessageDTO.getText())
            .setStyle(new NotificationCompat.BigTextStyle().bigText(currentText))
            .setGroup(AppConstants.FCMConstants.GROUP_KEY)
            .setExtras(bundle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(notificationCategory)
            .setContentIntent(pendingIntent)
            .addAction(handleReplyFromNotification(context, notificationID, actionButtonText, fcmMessageDTO))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true);

        if (bitmap != null) {
            mNotificationBuilder.setLargeIcon(bitmap);
        } else {
            mNotificationBuilder.setLargeIcon(BitmapFactory.decodeResource(
                context.getResources(),
                isUser ? R.drawable.ic_notification_user_placeholder : R.drawable.ic_notification_group_placeholder
            ));
        }
        return mNotificationBuilder;
    }

    private static NotificationCompat.Action handleReplyFromNotification(
        Context context,
        int notificationID,
        String actionButtonText,
        FCMMessageDTO fcmMessageDTO
    ) {
        Intent replyIntent = new Intent(context, FCMMessageBroadcastReceiver.class);

        replyIntent.putExtra(AppConstants.FCMConstants.KEY_DATA, new Gson().toJson(fcmMessageDTO));
        replyIntent.putExtra(AppConstants.FCMConstants.KEY_CLICKED_NOTIFICATION_ID, notificationID);

        replyIntent.setAction(AppConstants.FCMConstants.NOTIFICATION_REPLY_ACTION);

        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(), replyIntent, PendingIntent.FLAG_MUTABLE);

        RemoteInput remoteInput = new RemoteInput.Builder(AppConstants.FCMConstants.REPLY_FROM_NOTIFICATION).setLabel(actionButtonText).build();
        return new NotificationCompat.Action.Builder(R.drawable.ic_cometchat_notification, actionButtonText, replyPendingIntent).addRemoteInput(
                                                                                                                                    remoteInput)
                                                                                                                                .build();
    }

    public interface Callback {
        void onResult(Bitmap bitmap);
    }
}
