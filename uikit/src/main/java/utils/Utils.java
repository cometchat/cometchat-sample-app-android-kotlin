package utils;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.BindingAdapter;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.helpers.Logger;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import constant.StringContract;
import kotlin.ranges.RangesKt;
import screen.CometChatCallActivity;

public class Utils {

    private static final String TAG = "Utils";

    public static String removeEmojiAndSymbol(String content) {
        String utf8tweet = "";
        try {
            byte[] utf8Bytes = content.getBytes("UTF-8");
            utf8tweet = new String(utf8Bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Pattern unicodeOutliers = Pattern.compile(
                "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                        Pattern.UNICODE_CASE |
                                Pattern.CASE_INSENSITIVE);
        Matcher unicodeOutlierMatcher = unicodeOutliers.matcher(utf8tweet);
        utf8tweet = unicodeOutlierMatcher.replaceAll(" ");
        return utf8tweet;
    }
    public static boolean isDarkMode(Context context)
    {
        int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightMode== Configuration.UI_MODE_NIGHT_YES)
            return true;
        else
            return false;
    }

    public static final float softTransition(float $this$softTransition, float compareWith, float allowedDiff, float scaleFactor) {
        if (scaleFactor == 0.0F) {
            return $this$softTransition;
        } else {
            float result = $this$softTransition;
            float diff;
            if (compareWith > $this$softTransition) {
                if (compareWith / $this$softTransition > allowedDiff) {
                    diff = RangesKt.coerceAtLeast($this$softTransition, compareWith) - RangesKt.coerceAtMost($this$softTransition, compareWith);
                    result = $this$softTransition + diff / scaleFactor;
                }
            } else if ($this$softTransition > compareWith && $this$softTransition / compareWith > allowedDiff) {
                diff = RangesKt.coerceAtLeast($this$softTransition, compareWith) - RangesKt.coerceAtMost($this$softTransition, compareWith);
                result = $this$softTransition - diff / scaleFactor;
            }

            return result;
        }
    }

    public static AudioManager getAudioManager(Context context) {
        return (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }

    public static float dpToPixel(float dp, Resources resources) {
        float density = resources.getDisplayMetrics().density;
        float pixel = dp * density;
        return pixel;
    }


    public static void initiatecall(Context context,String recieverID,String receiverType,String callType)
    {
        Call call = new Call(recieverID,receiverType,callType);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("bookingId", 6);
        } catch(Exception e) {
            e.printStackTrace();
        }
        call.setMetadata(jsonObject);
        CometChat.initiateCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                Utils.startCallIntent(context,((User)call.getCallReceiver()),call.getType(),true,call.getSessionId());
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: "+e.getMessage());
                Snackbar.make(((Activity)context).getWindow().getDecorView().getRootView(),context.getResources().getString(R.string.call_initiate_error)+":"+e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }
    public static String convertTimeStampToDurationTime(long var0) {
        long var2 = var0 / 1000L;
        long var4 = var2 / 60L % 60L;
        long var6 = var2 / 60L / 60L % 24L;
        return var6 == 0L ? String.format(Locale.getDefault(), "%02d:%02d", var4, var2 % 60L) : String.format(Locale.getDefault(), "%02d:%02d:%02d", var6, var4, var2 % 60L);
    }

    public static String getDateId(long var0) {
        Calendar var2 = Calendar.getInstance(Locale.ENGLISH);
        var2.setTimeInMillis(var0);
        return DateFormat.format("ddMMyyyy", var2).toString();
    }

    public static String getDate(long var0) {
        Calendar var2 = Calendar.getInstance(Locale.ENGLISH);
        var2.setTimeInMillis(var0);
        return DateFormat.format("dd/MM/yyyy", var2).toString();
    }

    public static List<User> userSort(List<User> userList) {
        Collections.sort(userList, (user, user1) -> user.getName().toLowerCase().compareTo(user1.getName().toLowerCase()));
        return userList;
    }

    public static TextView changeToolbarFont(MaterialToolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                return (TextView) view;
            }
        }
        return null;
    }

    public static String getFileSize(int fileSize) {
        if (fileSize > 1024) {
            if (fileSize > (1024 * 1024)) {
                return fileSize / (1024 * 1024) + " MB";
            } else {
                return fileSize / 1024 + " KB";
            }
        } else {
            return fileSize + " B";
        }
    }


    public static String getLastMessage(BaseMessage lastMessage) {

        String message = null;

        switch (lastMessage.getCategory()) {

            case CometChatConstants.CATEGORY_MESSAGE:

                if (lastMessage instanceof TextMessage) {

                    if (isLoggedInUser(lastMessage.getSender()))
                        message = "You: " + (((TextMessage) lastMessage).getText()==null
                                ?"This message was deleted":((TextMessage) lastMessage).getText());
                    else
                        message = lastMessage.getSender().getName() + ": " + ((TextMessage) lastMessage).getText();

                } else if (lastMessage instanceof MediaMessage) {

                    if (isLoggedInUser(lastMessage.getSender()))
                        message = "You sent a " + lastMessage.getType();
                    else
                        message = "You received a " + lastMessage.getType();
                }

                break;

            case CometChatConstants.CATEGORY_CUSTOM:


                if (isLoggedInUser(lastMessage.getSender()))
                    message = "You sent a " + lastMessage.getType();
                else
                    message = "You received a " + lastMessage.getType();

                break;
            case CometChatConstants.CATEGORY_ACTION:
                message = ((Action) lastMessage).getMessage();
                break;

            case CometChatConstants.CATEGORY_CALL:
                message = "Call Message";
                break;
            default:
                message = "Tap to start conversation";
        }
        return message;
    }

    public static boolean isLoggedInUser(User user) {
        return user.getUid().equals(CometChat.getLoggedInUser().getUid());
    }

    /**
     * This method is used to convert user to group member. This method is used when we tries to add
     * user in a group or update group member scope.
     * @param user is object of User
     * @param isScopeUpdate is boolean which help us to check if scope is updated or not.
     * @param newScope is a String which contains newScope. If it is empty then user is added as participant.
     * @return GroupMember
     *
     * @see User
     * @see GroupMember
     */
    public static GroupMember UserToGroupMember(User user, boolean isScopeUpdate, String newScope) {
        GroupMember groupMember;
        if (isScopeUpdate)
            groupMember = new GroupMember(user.getUid(), newScope);
        else
            groupMember = new GroupMember(user.getUid(), CometChatConstants.SCOPE_PARTICIPANT);

        groupMember.setAvatar(user.getAvatar());
        groupMember.setName(user.getName());
        groupMember.setStatus(user.getStatus());
        return groupMember;
    }

    public static String getMessageDate(long timestamp) {
        String messageDate = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new java.util.Date(timestamp * 1000));
        return messageDate;
    }

    public static String getHeaderDate(long timestamp) {
        Calendar messageTimestamp = Calendar.getInstance();
        messageTimestamp.setTimeInMillis(timestamp);
        Calendar now = Calendar.getInstance();
//        if (now.get(5) == messageTimestamp.get(5)) {
        return DateFormat.format("hh:mm a", messageTimestamp).toString();
//        } else {
//            return now.get(5) - messageTimestamp.get(5) == 1 ? "Yesterday " + DateFormat.format("hh:mm a", messageTimestamp).toString() : DateFormat.format("d MMMM", messageTimestamp).toString() + " " + DateFormat.format("hh:mm a", messageTimestamp).toString();
//        }
    }

    public static String getLastMessageDate(long timestamp) {
        String lastMessageTime = new SimpleDateFormat("h:mm a").format(new java.util.Date(timestamp * 1000));
        String lastMessageDate = new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(timestamp * 1000));
        String lastMessageWeek = new SimpleDateFormat("EEE").format(new java.util.Date(timestamp * 1000));
        long currentTimeStamp = System.currentTimeMillis();

        long diffTimeStamp = currentTimeStamp - timestamp * 1000;

        Log.e(TAG, "getLastMessageDate: " + 24 * 60 * 60 * 1000);
        if (diffTimeStamp < 24 * 60 * 60 * 1000) {
            return lastMessageTime;

        } else if (diffTimeStamp < 48 * 60 * 60 * 1000) {

            return "Yesterday";
        } else if (diffTimeStamp < 7 * 24 * 60 * 60 * 1000) {
            return lastMessageWeek;
        } else {
            return lastMessageDate;
        }

    }

    public static Boolean checkDirExistence(Context context,String type) {

        File  audioDir = new File(Environment.getExternalStorageDirectory().toString() + "/" +
                context.getResources().getString(R.string.app_name) + "/" + type + "/");

        return audioDir.isDirectory();

    }

    public static void  makeDirectory(Context context,String type) {

        String  audioDir = Environment.getExternalStorageDirectory().toString() + "/" +
                context.getResources().getString(R.string.app_name) + "/" + type + "/";

        createDirectory(audioDir);
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null
                && permissions != null) {
            for (String permission : permissions) {
                Logger.error(TAG, " hasPermissions() : Permission : " + permission
                        + "checkSelfPermission : " + ActivityCompat.checkSelfPermission(context, permission));
                if (ActivityCompat.checkSelfPermission(context, permission) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = MediaStore.Files.FileColumns.DATA;
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static String getImagePathFromUri(Context context, @Nullable Uri aUri) {
        String imagePath = null;
        if (aUri == null) {
            return imagePath;
        }
        if (DocumentsContract.isDocumentUri(context, aUri)) {
            String documentId = DocumentsContract.getDocumentId(aUri);
            if ("com.android.providers.media.documents".equals(aUri.getAuthority())) {
                final String id = DocumentsContract.getDocumentId(aUri);

                if (id != null && id.startsWith("raw:")) {
                    return id.substring(4);
                }

                String[] contentUriPrefixesToTry = new String[]{
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads"
                };

                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                    try {
                        String path = getDataColumn(context, contentUri, null, null);
                        if (path != null) {
                            return path;
                        }
                    } catch (Exception e) {
                    }
                }

                // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                String fileName = getFileName(context, aUri);
                File cacheDir = getDocumentCacheDir(context);
                File file = generateFileName(fileName, cacheDir);
                String destinationPath = null;
                if (file != null) {
                    destinationPath = file.getAbsolutePath();
                    saveFileFromUri(context, aUri, destinationPath);
                }
                imagePath = destinationPath;
            } else if ("com.android.providers.downloads.documents".equals(aUri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(documentId));
                imagePath = getImagePath(contentUri, null, context);
            }
        } else if ("content".equalsIgnoreCase(aUri.getScheme())) {
            imagePath = getImagePath(aUri, null, context);
        } else if ("file".equalsIgnoreCase(aUri.getScheme())) {
            imagePath = aUri.getPath();
        }
        return imagePath;
    }

    private static void saveFileFromUri(Context context, Uri uri, String destinationPath) {
        InputStream is = null;
        BufferedOutputStream bos = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));
            byte[] buf = new byte[1024];
            is.read(buf);
            do {
                bos.write(buf);
            } while (is.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File generateFileName(@Nullable String name, File directory) {
        if (name == null) {
            return null;
        }

        File file = new File(directory, name);

        if (file.exists()) {
            String fileName = name;
            String extension = "";
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex);
                extension = name.substring(dotIndex);
            }

            int index = 0;

            while (file.exists()) {
                index++;
                name = fileName + '(' + index + ')' + extension;
                file = new File(directory, name);
            }
        }

        try {
            if (!file.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            Log.w(TAG, e);
            return null;
        }

        return file;
    }


    public static File getDocumentCacheDir(@NonNull Context context) {
        File dir = new File(context.getCacheDir(), "documents");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static String  getPath(Context context, String folder) {

        return Environment.getExternalStorageDirectory().toString() + "/" +
                context.getResources().getString(R.string.app_name) + "/" + folder + "/";
    }

    public static String getPath(final Context context, final Uri uri) {
        String absolutePath = getImagePathFromUri(context, uri);
        return absolutePath != null ? absolutePath : uri.toString();
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf('/');
        return filename.substring(index + 1);
    }


    public static String getFileName(String mediaFile) {
        String t1[] = mediaFile.substring(mediaFile.lastIndexOf("/")).split("_");
        return t1[2];
    }

    public static String getFileName(@NonNull Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        String filename = null;

        if (mimeType == null && context != null) {
            String path = getPath(context, uri);
            if (path == null) {
                filename = getName(uri.toString());
            } else {
                File file = new File(path);
                filename = file.getName();
            }
        } else {
            Cursor returnCursor = context.getContentResolver().query(uri, null,
                    null, null, null);
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                filename = returnCursor.getString(nameIndex);
                returnCursor.close();
            }
        }

        return filename;
    }

    private static String getImagePath(Uri aUri, String aSelection, Context context) {
        try {
            String path = null;
            Cursor cursor = context.getContentResolver().query(aUri, null, aSelection, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                }
                cursor.close();
            }
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getOutputMediaFile(Context context) {
        File var0 = new File(Environment.getExternalStorageDirectory(), context.getResources().getString(R.string.app_name));
        if (!var0.exists() && !var0.mkdirs()) {
            return null;
        } else {
            String var1 = Environment.getExternalStorageDirectory() + "/" + context.getResources().getString(R.string.app_name) + "/"
                    + "audio/";
            createDirectory(var1);
            return var1 + (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date()) + ".mp3";
        }
    }

    public static void createDirectory(String var0) {
        if (!(new File(var0)).exists()) {
            (new File(var0)).mkdirs();
        }

    }

    public static Bitmap blur(Context context, Bitmap image) {
        int width = Math.round(image.getWidth() * 0.6f);
        int height = Math.round(image.getHeight() * 0.6f);
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        intrinsicBlur.setRadius(15f);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }
    public static void startCallIntent(Context context, User user, String type,
                                       boolean isOutgoing, @NonNull String sessionId) {
        Intent videoCallIntent = new Intent(context, CometChatCallActivity.class);
        videoCallIntent.putExtra(StringContract.IntentStrings.NAME, user.getName());
        videoCallIntent.putExtra(StringContract.IntentStrings.UID,user.getUid());
        videoCallIntent.putExtra(StringContract.IntentStrings.SESSION_ID,sessionId);
        videoCallIntent.putExtra(StringContract.IntentStrings.AVATAR, user.getAvatar());
        videoCallIntent.setAction(type);
        videoCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isOutgoing) {
            videoCallIntent.setType("outgoing");
        }
        else {
            videoCallIntent.setType("incoming");
        }
        context.startActivity(videoCallIntent);
    }
    public static void startGroupCallIntent(Context context, Group group, String type,
                                            boolean isOutgoing, @NonNull String sessionId) {
        Intent videoCallIntent = new Intent(context, CometChatCallActivity.class);
        videoCallIntent.putExtra(StringContract.IntentStrings.NAME, group.getName());
        videoCallIntent.putExtra(StringContract.IntentStrings.UID,group.getGuid());
        videoCallIntent.putExtra(StringContract.IntentStrings.SESSION_ID,sessionId);
        videoCallIntent.putExtra(StringContract.IntentStrings.AVATAR, group.getIcon());
        videoCallIntent.setAction(type);
        videoCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (isOutgoing) {
            videoCallIntent.setType("outgoing");
        }
        else {
            videoCallIntent.setType("incoming");
        }
        context.startActivity(videoCallIntent);
    }

    public static float dpToPx(Context context, float valueInDp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = valueInDp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void showCallNotifcation(Context context, Call call) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int REQUEST_CODE = 12;
                    int m = (int) ((new Date().getTime()));
                    String GROUP_ID = "group_id";
                    String receiverName="",callType,receiverAvatar="",receiverUid="";

                    if (call.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER) && call.getSender().getUid().equals(CometChat.getLoggedInUser().getUid()))
                    {
                        receiverUid = ((User)call.getCallReceiver()).getUid();
                        receiverName = ((User)call.getCallReceiver()).getName();
                        receiverAvatar = ((User)call.getCallReceiver()).getAvatar();
                    } else if(call.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        receiverUid = call.getSender().getUid();
                        receiverName = call.getSender().getName();
                        receiverAvatar = call.getSender().getAvatar();
                    } else {
                        receiverUid = ((Group)call.getReceiver()).getGuid();
                        receiverName = ((Group)call.getReceiver()).getName();
                        receiverAvatar = ((Group)call.getReceiver()).getIcon();
                    }
                    if (call.getType().equals(CometChatConstants.CALL_TYPE_AUDIO)) {
                        callType = context.getResources().getString(R.string.incoming_audio_call);
                    } else {
                        callType = context.getResources().getString(R.string.incoming_video_call);
                    }

                    Intent callIntent;
                    callIntent = new Intent(context, CometChatCallActivity.class);
                    callIntent.putExtra(StringContract.IntentStrings.NAME, receiverName);
                    callIntent.putExtra(StringContract.IntentStrings.UID, receiverUid);
                    callIntent.putExtra(StringContract.IntentStrings.SESSION_ID, call.getSessionId());
                    callIntent.putExtra(StringContract.IntentStrings.AVATAR, receiverAvatar);
                    callIntent.setAction(call.getType());
                    callIntent.setType("incoming");

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"2")
                            .setSmallIcon(R.drawable.cc)
                            .setContentTitle(receiverName)
                            .setContentText(callType)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setChannelId("2")
                            .setColor(context.getResources().getColor(R.color.colorPrimary))
                            .setLargeIcon(getBitmapFromURL(receiverAvatar))
                            .setGroup(GROUP_ID)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                    builder.setGroup(GROUP_ID+"Call");
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
                    builder.addAction(0, "Answers", PendingIntent.getBroadcast(context, REQUEST_CODE, callIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                    builder.addAction(0, "Decline", PendingIntent.getBroadcast(context, 1, callIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                    notificationManager.notify(05,builder.build());

                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startCall(Activity activity, Call call, RelativeLayout mainView) {
        CometChat.startCall(activity, call.getSessionId(), mainView, new CometChat.OngoingCallListener() {
            @Override
            public void onUserJoined(User user) {
                Log.e("onUserJoined: ",user.getUid() );
            }

            @Override
            public void onUserLeft(User user) {
                Snackbar.make(activity.getWindow().getDecorView().getRootView(),"User Left: "+user.getName(),Snackbar.LENGTH_LONG).show();
                Log.e( "onUserLeft: ",user.getUid() );
            }

            @Override
            public void onError(CometChatException e) {
                Log.e( "onError: ",e.getMessage() );
            }

            @Override
            public void onCallEnded(Call call) {
                Log.e(TAG, "onCallEnded: "+call.toString() );
                activity.finish();
            }
        });
    }

    public static void joinOnGoingCall(Context context) {
        Intent intent = new Intent(context,CometChatCallActivity.class);
        intent.putExtra(StringContract.IntentStrings.JOIN_ONGOING,true);
        context.startActivity(intent);
    }
}
