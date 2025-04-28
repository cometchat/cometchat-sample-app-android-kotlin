package com.cometchat.chatuikit.shared.resources.utils;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.FileProvider;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.helpers.Logger;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.AppEntity;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chat.models.Interaction;
import com.cometchat.chat.models.InteractiveMessage;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.MessageReceipt;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatCustomToastLayoutBinding;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.models.interactiveelements.DateTimeElement;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.CustomInteractiveMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.InteractiveConstants;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;
import com.cometchat.chatuikit.shared.views.mediaviewer.CometChatImageViewerActivity;
import com.cometchat.chatuikit.shared.resources.localise.CometChatLocalize;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import kotlin.ranges.RangesKt;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static void runOnMainThread(Runnable runnable) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(runnable);
    }

    public static Bitmap captureScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return captureScreenUsingPixelCopy(activity);
        } else {
            return captureScreenLegacy(activity);
        }
    }

    // ✅ **Best method for Android O (API 26+)**
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Bitmap captureScreenUsingPixelCopy(Activity activity) {
        try {
            // Get the Window Decor View
            Window window = activity.getWindow();
            final View decorView = window.getDecorView();
            final Bitmap bitmap = Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.ARGB_8888);

            PixelCopy.request(window, bitmap, copyResult -> {
                if (copyResult != PixelCopy.SUCCESS) {
                    throw new RuntimeException("PixelCopy failed");
                }
            }, new android.os.Handler());

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ **Fallback for Older Versions**
    private static Bitmap captureScreenLegacy(Activity activity) {
        try {
            View rootView = activity.getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
            rootView.setDrawingCacheEnabled(false);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap applyRenderScriptBlur(Activity activity, Bitmap bitmap) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap);
        RenderScript rs = RenderScript.create(activity, RenderScript.ContextType.NORMAL);
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        Allocation output = Allocation.createFromBitmap(rs, outputBitmap);
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, input.getElement());
        script.setRadius(25f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(outputBitmap);
        rs.destroy();
        return outputBitmap;
    }

    public static HashMap<String, String> getIdMap(BaseMessage baseMessage) {
        HashMap<String, String> idMap = new HashMap<>();
        if (baseMessage.getParentMessageId() > 0)
            idMap.put(UIKitConstants.MapId.PARENT_MESSAGE_ID, String.valueOf(baseMessage.getParentMessageId()));
        idMap.put(UIKitConstants.MapId.RECEIVER_ID,
                  baseMessage.getReceiverUid().equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid()) ? baseMessage
                      .getSender()
                      .getUid() : baseMessage.getReceiverUid());
        idMap.put(UIKitConstants.MapId.RECEIVER_TYPE, baseMessage.getReceiverType());
        return idMap;
    }

    public static Typeface getTypefaceFromTextAppearance(Context context, int textAppearanceResId) {
        String fontFamily;
        int textStyle = Typeface.NORMAL;
        TypedArray typedArray = context.obtainStyledAttributes(textAppearanceResId, androidx.appcompat.R.styleable.TextAppearance);
        try {
            fontFamily = typedArray.getString(androidx.appcompat.R.styleable.TextAppearance_fontFamily);
            if (fontFamily == null) {
                fontFamily = typedArray.getString(androidx.appcompat.R.styleable.TextAppearance_android_fontFamily);
            }
            textStyle = typedArray.getInt(androidx.appcompat.R.styleable.TextAppearance_android_textStyle, textStyle);
        } finally {
            typedArray.recycle();
        }
        Typeface typeface;
        if (fontFamily != null) {
            typeface = Typeface.create(fontFamily, textStyle);
        } else {
            typeface = Typeface.defaultFromStyle(textStyle);
        }
        return typeface;
    }

    public static @Dimension int getTextSize(Context context, @StyleRes int style) {
        int textSize;
        TypedArray typedArray = context.obtainStyledAttributes(style, androidx.appcompat.R.styleable.TextAppearance);
        try {
            textSize = typedArray.getDimensionPixelSize(androidx.appcompat.R.styleable.TextAppearance_android_textSize, -1);
        } finally {
            typedArray.recycle();
        }
        return textSize;
    }

    // Method to get the MIME type from file path
    public static String getMimeTypeFromFile(Context context, File file) {
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        return context.getContentResolver().getType(fileUri);
    }

    public static boolean isGifFile(File file) {
        if (file == null || !file.exists()) return false;
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[6];
            if (inputStream.read(bytes) != 6) {
                return false;
            }
            String signature = new String(bytes, StandardCharsets.US_ASCII);
            return signature.startsWith("GIF");
        } catch (IOException ex) {
            return false;
        }
    }

    public static File getFileFromLocalPath(MediaMessage mediaMessage) {
        if (mediaMessage.getMetadata() != null) {
            if (mediaMessage.getMetadata().has("path")) {
                try {
                    String localFilePath = mediaMessage.getMetadata().getString("path");
                    File file = new File(localFilePath);
                    if (file.exists()) {
                        return file;
                    }
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    public static float pxToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    public static void performAdapterClick(View view) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            ViewParent parent = view.getParent();
            while (parent != null) {
                if (parent instanceof View) {
                    if (((View) parent).getId() == R.id.message_adapter_message_bubble_parent) {
                        ViewParent finalParent = parent;
                        mainThread.post(() -> ((View) finalParent).performLongClick());
                        break;
                    }
                    parent = ((View) parent).getParent();
                } else {
                    break;
                }
            }
        }).start();
    }

    public static void openImageViewer(View imageView, List<String> imageUrls, List<String> mimeTypes, List<String> names) {
        Context context = imageView.getContext();
//        Rect visibleRect = new Rect();
//        boolean isFullyVisible = view.getGlobalVisibleRect(visibleRect);
//        if (isFullyVisible && visibleRect.height() == view.getHeight()) {
//            context.startActivity(CometChatImageViewerActivity.createIntent(context, imageUrl, 0),
//                                  getActivityOption(view).toBundle());
//        } else {
        context.startActivity(CometChatImageViewerActivity.createIntent(
            context,
            imageUrls,
            mimeTypes,
            names
        ));
        ((Activity) context).overridePendingTransition(R.anim.cometchat_fade_in_fast, R.anim.cometchat_fade_out_fast);
//        }
    }

    private static ActivityOptionsCompat getActivityOption(View targetView) {
        return ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) targetView.getContext(), targetView, targetView.getTransitionName());
    }

    @ColorInt
    public static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static int multiplyColorAlpha(int color, int alpha) {
        if (alpha == 255) {
            return color;
        } else if (alpha == 0) {
            return color & 16777215;
        } else {
            alpha += alpha >> 7;
            int colorAlpha = color >>> 24;
            int multipliedAlpha = colorAlpha * alpha >> 8;
            return multipliedAlpha << 24 | color & 16777215;
        }
    }

    public static int applyColorWithAlphaValue(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int colorAlpha = Color.argb(alpha, red, green, blue);
        return colorAlpha;
    }

    public static int getOpacityFromColor(int color) {
        int colorAlpha = color >>> 24;
        if (colorAlpha == 255) {
            return -1;
        } else {
            return colorAlpha == 0 ? -2 : -3;
        }
    }

    public static List<String> getMessageTypesFromTemplate(List<CometChatMessageTemplate> messageTemplates) {
        List<String> messageTypes = new ArrayList<>();
        for (CometChatMessageTemplate messageTemplate : messageTemplates) {
            messageTypes.add(messageTemplate.getType());
        }
        return messageTypes;
    }

    public static List<String> getMessageCategoryFromTemplate(List<CometChatMessageTemplate> messageTemplates) {
        List<String> messageTypes = new ArrayList<>();
        for (CometChatMessageTemplate messageTemplate : messageTemplates) {
            messageTypes.add(messageTemplate.getType());
        }
        return messageTypes;
    }

    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.cometchat_app_name);
            String description = context.getString(R.string.cometchat_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("2", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static boolean isCallingAvailable() {
        return isClass("com.cometchat.calls.core.CometChatCalls");
    }

    public static boolean isClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String getMessagePrefix(BaseMessage lastMessage, Context context) {
        String message = "";
        if (lastMessage.getReceiverType().equalsIgnoreCase(UIKitConstants.ReceiverType.GROUP)) {
            if (!isLoggedInUser(lastMessage.getSender())) {
                message = lastMessage.getSender().getName() + ": ";
            } else {
                message = context.getString(R.string.cometchat_you) + ": ";
            }
        }
        return message;
    }

    public static boolean isLoggedInUser(User user) {
        return user.getUid().equals(CometChatUIKit.getLoggedInUser().getUid());
    }

    public static void handleView(ViewGroup layout, View view, boolean hideIfNull) {
        if (view != null) {
            layout.removeAllViews();
            removeParentFromView(view);
            layout.setVisibility(VISIBLE);
            layout.addView(view);
        } else {
            if (hideIfNull) layout.setVisibility(GONE);
        }
    }

    public static void removeParentFromView(View view) {
        if (view != null && view.getParent() != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
    }

    public static void showBottomSheet(Context context,
                                       @NonNull BottomSheetDialog bottomSheetDialog,
                                       boolean isCancelable,
                                       boolean openHalfScreen,
                                       View view) {
        try {
            Utils.removeParentFromView(view);
            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.setOnShowListener(dialogInterface -> {
                View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    bottomSheet.setBackgroundResource(R.color.cometchat_color_transparent);

                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                    if (openHalfScreen) {
                        behavior.setPeekHeight((int) (context.getResources().getDisplayMetrics().heightPixels * 0.5)); // 50%
                        // of
                        // screen
                        // height
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        bottomSheet.getLayoutParams().height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.5);
                    } else {
                        bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    }
                    bottomSheet.requestLayout();
                }
            });
            // Enable dialog to resize when the keyboard opens
            bottomSheetDialog.setCancelable(isCancelable);
            bottomSheetDialog.show();
        } catch (Exception ignored) {
            // Exception is ignored to prevent crashing, but consider logging for debugging
            // purposes.
        }
    }

    public static Action getGroupActionMessage(AppEntity actionOn, Group actionFor, Group receiver, String uid) {
        Action action = new Action();
        action.setActionBy(CometChatUIKit.getLoggedInUser());
        action.setActionOn(actionOn);
        action.setActionFor(actionFor);
        action.setReceiverType(CometChatConstants.RECEIVER_TYPE_GROUP);
        action.setReceiver(receiver);
        action.setCategory(CometChatConstants.CATEGORY_ACTION);
        action.setType(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER);
        action.setReceiverUid(uid);
        action.setSender(CometChatUIKit.getLoggedInUser());
        action.setSentAt(System.currentTimeMillis() / 1000);
        action.setConversationId("group_" + actionFor.getGuid());
        return action;
    }

    public static MessageReceipt createMessageReceipt(BaseMessage baseMessage) {
        MessageReceipt messageReceipt;
        if (baseMessage != null) {
            messageReceipt = new MessageReceipt();
            messageReceipt.setSender(baseMessage.getReceiver() instanceof User ? (User) baseMessage.getReceiver() : null);
            messageReceipt.setReadAt(baseMessage.getReadAt());
            messageReceipt.setTimestamp(baseMessage.getReadAt());
            messageReceipt.setDeliveredAt(baseMessage.getDeliveredAt() == 0 ? baseMessage.getReadAt() : baseMessage.getDeliveredAt());
            messageReceipt.setMessageId(baseMessage.getId());
            messageReceipt.setReceivertype(baseMessage.getReceiverType());
            messageReceipt.setReceiverId(CometChatUIKit.getLoggedInUser().getUid());
            messageReceipt.setMessageSender(baseMessage.getSender().toString());
            return messageReceipt;
        }
        return null;
    }

    public static float softTransition(float $this$softTransition, float compareWith, float allowedDiff, float scaleFactor) {
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
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public static float dpToPixel(float dp, Resources resources) {
        float density = resources.getDisplayMetrics().density;
        float pixel = dp * density;
        return pixel;
    }

    public static String convertTimeStampToDurationTime(long var0) {
        long var2 = var0 / 1000L;
        long var4 = var2 / 60L % 60L;
        long var6 = var2 / 60L / 60L % 24L;
        return var6 == 0L ? String.format(Locale.US, "%02d:%02d", var4, var2 % 60L) : String.format(Locale.US,
                                                                                                    "%02d:%02d:%02d",
                                                                                                    var6,
                                                                                                    var4,
                                                                                                    var2 % 60L);
    }

    @NonNull
    public static String getDateId(long var0) {
        Calendar var2 = Calendar.getInstance(Locale.ENGLISH);
        var2.setTimeInMillis(var0);
        return DateFormat.format("ddMMyyyy", var2).toString();
    }

    public static String getCallDate(long var0) {
        Calendar var2 = Calendar.getInstance(Locale.ENGLISH);
        var2.setTimeInMillis(var0);
        return DateFormat.format("dd MMM yy", var2).toString();
    }

    public static String getDate(Context context, long var0) {
        Calendar var2 = Calendar.getInstance(Locale.ENGLISH);
        var2.setTimeInMillis(var0 * 1000L);

        long currentTimeStamp = System.currentTimeMillis();

        long diffTimeStamp = currentTimeStamp - (var0 * 1000);
        if (diffTimeStamp < 24 * 60 * 60 * 1000) {
            return context.getString(R.string.cometchat_today);

        } else if (diffTimeStamp < 48 * 60 * 60 * 1000) {

            return context.getString(R.string.cometchat_yesterday);
        } else return DateFormat.format("dd MMMM yyyy", var2).toString();
    }

    public static List<User> userSort(List<User> userList) {
        Collections.sort(userList, (user, user1) -> user.getName().toLowerCase().compareTo(user1.getName().toLowerCase()));
        return userList;
    }

    public static void showToast(Context context, String message, @ColorInt int backgroundColor) {
        CometchatCustomToastLayoutBinding binding = CometchatCustomToastLayoutBinding.inflate(LayoutInflater.from(context));
        binding.tvMsg.setText(message);
        binding.parentCard.setCardBackgroundColor(backgroundColor);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(binding.getRoot());
        toast.show();
    }

    public static void openAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
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

    public static String getFileSize(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            long fileSizeInBytes = file.length();
            return getFileSize((int) fileSizeInBytes);
        }
        return "";
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

    /**
     * This method is used to convert user to group member. This method is used when
     * we tries to add user in a group or update group member scope.
     *
     * @param user          is object of User
     * @param isScopeUpdate is boolean which help us to check if scope is updated or not.
     * @param newScope      is a String which contains newScope. If it is empty then user is
     *                      added as participant.
     * @return GroupMember
     * @see User
     * @see GroupMember
     */
    public static GroupMember UserToGroupMember(User user, boolean isScopeUpdate, String newScope) {
        GroupMember groupMember;
        if (isScopeUpdate) groupMember = new GroupMember(user.getUid(), newScope);
        else groupMember = new GroupMember(user.getUid(), CometChatConstants.SCOPE_PARTICIPANT);

        groupMember.setAvatar(user.getAvatar());
        groupMember.setName(user.getName());
        groupMember.setStatus(user.getStatus());
        return groupMember;
    }

    public static String getHeaderDate(long timestamp) {
        Calendar messageTimestamp = Calendar.getInstance();
        messageTimestamp.setTimeInMillis(timestamp);
        Calendar now = Calendar.getInstance();
        // if (now.get(5) == messageTimestamp.get(5)) {
        return DateFormat.format("hh:mm a", messageTimestamp).toString();
        // } else {
        // return now.get(5) - messageTimestamp.get(5) == 1 ? "Yesterday " +
        // DateFormat.format("hh:mm a", messageTimestamp).toString() :
        // DateFormat.format("d MMMM",
        // messageTimestamp).toString() + " " + DateFormat.format("hh:mm a",
        // messageTimestamp).toString();
        // }
    }

    public static void initMaterialCard(MaterialCardView view) {
        view.setCardBackgroundColor(Color.TRANSPARENT);
        view.setCardElevation(0);
        view.setRadius(0);
        view.setStrokeWidth(0);
    }

    public static String getLastMessageDate(Context context, long timestamp) {
        String lastMessageTime = new SimpleDateFormat("h:mm a", Locale.US).format(new Date(timestamp * 1000));
        String lastMessageDate = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date(timestamp * 1000));
        String lastMessageWeek = new SimpleDateFormat("EEE", Locale.US).format(new Date(timestamp * 1000));
        long currentTimeStamp = System.currentTimeMillis();

        long diffTimeStamp = currentTimeStamp - timestamp * 1000;

        if (diffTimeStamp < 24 * 60 * 60 * 1000) {
            return lastMessageTime;

        } else if (diffTimeStamp < 48 * 60 * 60 * 1000) {

            return context.getString(R.string.cometchat_yesterday);
        } else if (diffTimeStamp < 7 * 24 * 60 * 60 * 1000) {
            return lastMessageWeek;
        } else {
            return lastMessageDate;
        }
    }

    /**
     * This method is used to create group when called from layout. It uses
     * <code>Random.nextInt()
     * </code> to generate random number to use with group id and group icon. Any
     * Random number between 10 to 1000 are choosen.
     */
    public static String generateRandomString(int length) {
        if (length < 1) throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 0-62 (exclusive), random returns 0-61
            SecureRandom random = new SecureRandom();
            String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
            String CHAR_UPPER = CHAR_LOWER.toUpperCase();
            String NUMBER = "0123456789";
            String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            // debug
            System.out.format("%d\t:\t%c%n", rndCharAt, rndChar);
            sb.append(rndChar);
        }
        return sb.toString();
    }

    public static String getReceiptDate(Context context, long timestamp) {
        String lastMessageTime = new SimpleDateFormat("h:mm a", Locale.US).format(new Date(timestamp * 1000));
        String lastMessageDate = new SimpleDateFormat("dd MMMM h:mm a", Locale.US).format(new Date(timestamp * 1000));
        String lastMessageWeek = new SimpleDateFormat("EEE h:mm a", Locale.US).format(new Date(timestamp * 1000));
        long currentTimeStamp = System.currentTimeMillis();

        long diffTimeStamp = currentTimeStamp - timestamp * 1000;

        if (diffTimeStamp < 24 * 60 * 60 * 1000) {
            return lastMessageTime;

        } else if (diffTimeStamp < 48 * 60 * 60 * 1000) {

            return context.getString(R.string.cometchat_yesterday);
        } else if (diffTimeStamp < 7 * 24 * 60 * 60 * 1000) {
            return lastMessageWeek;
        } else {
            return lastMessageDate;
        }
    }

    public static boolean isBlocked(User user) {
        if (user != null) return (user.isBlockedByMe() || user.isHasBlockedMe());
        else return false;
    }

    public static Boolean checkDirExistence(Context context, String type) {

        File audioDir = new File(Environment.getExternalStorageDirectory().toString() + "/" + context
            .getResources()
            .getString(R.string.cometchat_app_name) + "/" + type + "/");

        return audioDir.isDirectory();
    }

    public static void makeDirectory(Context context, String type) {

        String audioDir = Environment.getExternalStorageDirectory().toString() + "/" + context
            .getResources()
            .getString(R.string.cometchat_app_name) + "/" + type + "/";

        createDirectory(audioDir);
    }

    public static void createDirectory(@NonNull String mPath) {
        File directory = new File(mPath);
        // Check if the directory exists, if not, try creating it
        if (!directory.exists()) {
            boolean dirsCreated = directory.mkdirs();
            if (!dirsCreated) {
                // Log the failure if the directories couldn't be created
                CometChatLogger.e(TAG, "Failed to create directory: " + directory.getAbsolutePath());
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                Logger.error(TAG,
                             " hasPermissions() : Permission : " + permission + "checkSelfPermission : " + ActivityCompat.checkSelfPermission(context,
                                                                                                                                              permission));
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
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

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaStore.Files.FileColumns.DATA;
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public static String getImagePathFromUri(Context context, @Nullable Uri aUri) {
        String imagePath = null;
        if (aUri == null) {
            return null;
        }
        if (DocumentsContract.isDocumentUri(context, aUri)) {
            String documentId = DocumentsContract.getDocumentId(aUri);
            if ("com.android.providers.media.documents".equals(aUri.getAuthority())) {
                final String id = DocumentsContract.getDocumentId(aUri);

                if (id != null && id.startsWith("raw:")) {
                    return id.substring(4);
                }

                String[] contentUriPrefixesToTry = new String[]{"content://downloads/public_downloads", "content://downloads/my_downloads"};

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

                // path could not be retrieved using ContentResolver, therefore copy file to
                // accessible cache using streams
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
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
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
            // Open the input stream from the URI
            is = context.getContentResolver().openInputStream(uri);
            if (is == null) {
                CometChatLogger.e(TAG, "Failed to open InputStream for URI: " + uri);
                return; // Early exit if the InputStream is null
            }

            // Set up a buffered output stream to write the data to the destination file
            bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));
            byte[] buf = new byte[1024];
            int bytesRead; // Track how many bytes are read

            // Read and write data in a loop
            while ((bytesRead = is.read(buf)) != -1) {
                bos.write(buf, 0, bytesRead); // Only write the number of bytes read
            }
        } catch (IOException e) {
            CometChatLogger.e(TAG, e.toString());
        } finally {
            // Close streams safely
            try {
                if (is != null) is.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                CometChatLogger.e(TAG, e.toString());
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
        // Check if directories were created successfully
        if (!dir.exists()) {
            boolean dirsCreated = dir.mkdirs();
            if (!dirsCreated) {
                CometChatLogger.e(TAG, "Failed to create directories: " + dir.getAbsolutePath());
            }
        }
        return dir;
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
        String[] t1 = mediaFile.substring(mediaFile.lastIndexOf("/")).split("_");
        return t1[2];
    }

    public static String getFileName(@NonNull Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        String filename = null;

        if (mimeType == null) {
            String path = getPath(context, uri);
            File file = new File(path);
            filename = file.getName();
        } else {
            Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
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
            CometChatLogger.e(TAG, e.toString());
        }

        return null;
    }

    public static String getOutputMediaFile(Context context) {
        File var0 = new File(Environment.getExternalStorageDirectory(), context.getResources().getString(R.string.cometchat_app_name));
        String dir;
        if (Build.VERSION_CODES.R > Build.VERSION.SDK_INT) {
            dir = Environment.getExternalStorageDirectory() + "/" + context.getResources().getString(R.string.cometchat_app_name) + "/" + "audio/";
        } else {
            if (Environment.isExternalStorageManager()) {
                dir = Environment.getExternalStorageState() + "/" + context.getResources().getString(R.string.cometchat_app_name) + "/" + "audio/";
            } else {
                dir = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS).getPath() + "/" + context
                    .getResources()
                    .getString(R.string.cometchat_app_name) + "/" + "audio/";
            }
        }
        createDirectory(dir);
        return dir + (new SimpleDateFormat("yyyyMMddHHmmss", Locale.US)).format(new Date()) + ".m4a";
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

    public static float dpToPx(Context context, float valueInDp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = valueInDp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
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
            CometChatLogger.e(TAG, e.toString());
            return null;
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static String getAddress(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, CometChatLocalize.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return null;
    }

    public static void hideKeyBoard(Context context, View mainLayout) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
    }

    public static void showKeyBoard(Context context, View mainLayout) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    public static JSONObject placeErrorObjectInMetaData(CometChatException exception) {
        JSONObject metaData = new JSONObject();
        try {
            metaData.put("error", exception.toString());
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return metaData;
    }

    public static BaseMessage convertToUIKitMessage(BaseMessage baseMessage) {
        if (baseMessage instanceof InteractiveMessage && baseMessage.getCategory().equals(CometChatConstants.CATEGORY_INTERACTIVE)) {
            if (baseMessage.getType().equals(UIKitConstants.MessageType.FORM)) {
                return FormMessage.fromInteractive((InteractiveMessage) baseMessage);
            } else if (baseMessage.getType().equals(UIKitConstants.MessageType.CARD)) {
                return CardMessage.fromInteractive((InteractiveMessage) baseMessage);
            } else if (baseMessage.getType().equals(UIKitConstants.MessageType.SCHEDULER)) {
                return SchedulerMessage.fromInteractive((InteractiveMessage) baseMessage);
            } else if (baseMessage.getType().equals(UIKitConstants.MessageType.CUSTOM_INTERACTIVE)) {
                return CustomInteractiveMessage.fromInteractive((InteractiveMessage) baseMessage);
            }
        }
        return baseMessage;
    }

    public static Call getDirectCallData(BaseMessage baseMessage) {
        Call call = null;
        String callType = CometChatConstants.CALL_TYPE_VIDEO;
        try {
            if (((CustomMessage) baseMessage).getCustomData() != null) {
                JSONObject customObject = ((CustomMessage) baseMessage).getCustomData();
                String receiverID = baseMessage.getReceiverUid();
                String receiverType = baseMessage.getReceiverType();
                if (customObject.has("callType")) {
                    callType = customObject.getString("callType");
                }
                call = new Call(receiverID, receiverType, callType);
                if (customObject.has("sessionID")) {
                    String sessionID = customObject.getString("sessionID");
                    call.setSessionId(sessionID);
                } else {
                    call.setSessionId(receiverID);
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return call;
    }

    public static void downloadFile(Context context, String url, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        // Ensure this download is visible and can be managed by user
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Set the destination path of the downloaded file for API level 29 and above
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName);
        } else {
            // Set the destination path of the downloaded file for API level below 29
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        }

        // Get the DownloadManager service and enqueue the request
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        if (manager != null) {
            manager.enqueue(request);
        }
    }

    public static void requestPermissions(Context context, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions((Activity) context, permissions, requestCode);
    }

    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        // Change the status bar color
        if (color != 0) {
            activity.getWindow().setStatusBarColor(color);
        }

        // Change the status bar text color
        if (!isDarkMode(activity)) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static boolean isDarkMode(Context context) {
        int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void setDialogStatusBarColor(Dialog dialog, @ColorInt int color) {
        // Change the status bar color
        if (color != 0 && dialog.getWindow() != null) {
            dialog.getWindow().setStatusBarColor(color);
        }

        // Change the status bar text color
        if (!isDarkMode(dialog.getContext()) && dialog.getWindow() != null) {
            dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void setMarginToView(Context context, View view, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginLayoutParams.setMargins(leftMargin != -1 ? Utils.convertDpToPx(context, leftMargin) : Utils.convertDpToPx(context, 0),
                                          topMargin != -1 ? Utils.convertDpToPx(context, topMargin) : Utils.convertDpToPx(context, 0),
                                          rightMargin != -1 ? Utils.convertDpToPx(context, rightMargin) : Utils.convertDpToPx(context, 0),
                                          bottomMargin != -1 ? Utils.convertDpToPx(context, bottomMargin) : Utils.convertDpToPx(context, 0));
            view.setLayoutParams(marginLayoutParams);
        }
    }

    public static int convertDpToPx(Context context, int dp) {
        int px = Math.round(dp * context.getResources().getDisplayMetrics().density);
        return px;
    }

    public static boolean isMediaMessage(BaseMessage baseMessage) {
        return baseMessage.getType().equals(UIKitConstants.MessageType.IMAGE) || baseMessage.getType().equals(UIKitConstants.MessageType.VIDEO);
    }

    public static boolean isGoalCompleted(InteractiveMessage interactiveMessage) {
        if (interactiveMessage.getInteractionGoal() == null || interactiveMessage
            .getInteractionGoal()
            .getType() == null || interactiveMessage.getInteractions() == null) {
            return false;
        }

        switch (interactiveMessage.getInteractionGoal().getType()) {
            case CometChatConstants.INTERACTION_TYPE_ANY:
                return !interactiveMessage.getInteractions().isEmpty();

            case CometChatConstants.INTERACTION_TYPE_ANY_OF:
            case CometChatConstants.INTERACTION_TYPE_All_OF:
                if (interactiveMessage.getInteractionGoal().getElementIds() == null || interactiveMessage
                    .getInteractionGoal()
                    .getElementIds()
                    .isEmpty()) {
                    return false;
                }
                HashSet<String> interactionSet = new HashSet<>();
                for (Interaction interaction : interactiveMessage.getInteractions()) {
                    interactionSet.add(interaction.getElementId());
                }

                for (String elementId : interactiveMessage.getInteractionGoal().getElementIds()) {
                    boolean isElementExists = interactionSet.contains(elementId);

                    if (interactiveMessage.getInteractionGoal().getType().equals(CometChatConstants.INTERACTION_TYPE_ANY_OF) && isElementExists) {
                        return true;
                    }
                    if (interactiveMessage.getInteractionGoal().getType().equals(CometChatConstants.INTERACTION_TYPE_All_OF) && !isElementExists) {
                        return false;
                    }
                }
                return interactiveMessage.getInteractionGoal().getType().equals(CometChatConstants.INTERACTION_TYPE_All_OF);
            default:
                return false;
        }
    }

    public static JSONObject getInteractiveRequestPayload(JSONObject payload, String interactedElementId, BaseMessage baseMessage) {
        JSONObject newPayload = new JSONObject();
        try {
            newPayload.put(InteractiveConstants.InteractiveRequestPayload.APP_ID, CometChatUIKit.getAuthSettings().getAppId());
            newPayload.put(InteractiveConstants.InteractiveRequestPayload.REGION, CometChatUIKit.getAuthSettings().getRegion());
            newPayload.put(InteractiveConstants.InteractiveRequestPayload.TRIGGER, "ui_message_interacted");
            newPayload.put(InteractiveConstants.InteractiveRequestPayload.PAYLOAD, payload);
            JSONObject data = new JSONObject();
            data.put(InteractiveConstants.InteractiveRequestPayload.CONVERSATION_ID, baseMessage.getConversationId());
            data.put(InteractiveConstants.InteractiveRequestPayload.SENDER, baseMessage.getSender().getName());
            data.put(InteractiveConstants.InteractiveRequestPayload.RECEIVER, baseMessage.getReceiverUid());
            data.put(InteractiveConstants.InteractiveRequestPayload.RECEIVER_TYPE, baseMessage.getReceiverType());
            data.put(InteractiveConstants.InteractiveRequestPayload.MESSAGE_CATEGORY, baseMessage.getCategory());
            data.put(InteractiveConstants.InteractiveRequestPayload.MESSAGE_ID, baseMessage.getId());
            data.put(InteractiveConstants.InteractiveRequestPayload.MESSAGE_TYPE, baseMessage.getType());
            data.put(InteractiveConstants.InteractiveRequestPayload.INTERACTION_TIMEZONE_CODE, TimeZone.getDefault().getID());
            data.put(InteractiveConstants.InteractiveRequestPayload.INTERACTED_BY, CometChatUIKit.getLoggedInUser().getUid());
            data.put(InteractiveConstants.InteractiveRequestPayload.INTERACTED_ELEMENT_ID, interactedElementId);
            newPayload.put(InteractiveConstants.InteractiveRequestPayload.DATA, data);

        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
        return newPayload;
    }

    public static SimpleDateFormat getDateFormat(DateTimeElement timeElement) {
        SimpleDateFormat dateFormat;
        if (timeElement.getSimpleDateFormat() == null || !isdateFormatValid(timeElement.getSimpleDateFormat().toPattern())) {
            if (timeElement.getMode().equals(UIKitConstants.DateTimeMode.DATE_TIME))
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            else if (timeElement.getMode().equals(UIKitConstants.DateTimeMode.DATE))
                dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            else if (timeElement.getMode().equals(UIKitConstants.DateTimeMode.TIME))
                dateFormat = new SimpleDateFormat("HH:mm", Locale.US);
            else dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        } else {
            dateFormat = timeElement.getSimpleDateFormat();
        }
        return dateFormat;
    }

    public static boolean isdateFormatValid(String pattern) {
        try {
            new SimpleDateFormat(pattern, Locale.US);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static SimpleDateFormat getDateTimeReadFormat(DateTimeElement timeElement) {
        SimpleDateFormat dateFormat;
        if (timeElement.getMode().equals(UIKitConstants.DateTimeMode.DATE_TIME))
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
        else if (timeElement.getMode().equals(UIKitConstants.DateTimeMode.DATE))
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        else if (timeElement.getMode().equals(UIKitConstants.DateTimeMode.TIME))
            dateFormat = new SimpleDateFormat("HH:mm", Locale.US);
        else dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        return dateFormat;
    }

    public static String[] getDefaultReactionsList() {
        return new String[]{"😍", "👍🏻", "🔥", "😊", "❤️"};
    }

    public static int getTheScreenHeight(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getViewWidth(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return view.getMeasuredWidth();
    }

    public static String getLastSeenTime(Context context, long timestamp) {
        return getLastSeenTime(context, timestamp, null);
    }

    /**
     * Returns a formatted string indicating when the user was last seen.
     *
     * @param context   The context for localization.
     * @param timestamp The timestamp in milliseconds.
     * @return A string describing when the user was last seen (e.g., "last seen
     * today at 10:10 am").
     */
    public static String getLastSeenTime(Context context, long timestamp, DateTimeFormatterCallback dateTimeFormatterCallback) {
        if (String.valueOf(timestamp).length() == 10) {
            // Convert seconds to milliseconds
            timestamp *= 1000;
        }

        Calendar now = Calendar.getInstance();
        Calendar lastSeen = Calendar.getInstance();
        lastSeen.setTimeInMillis(timestamp);

        long diffInMillis = now.getTimeInMillis() - lastSeen.getTimeInMillis();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);

        // Check if the timestamp is within the last hour
        if (diffInMinutes == 0) {
            if (dateTimeFormatterCallback != null) {
                String minute = dateTimeFormatterCallback.minute(timestamp);
                if (minute != null) {
                    return minute;
                }
            }
            return context.getResources().getString(R.string.cometchat_last_seen) + " " + context
                .getResources()
                .getQuantityString(R.plurals.cometchat_last_seen_minutes_ago, 1, 1);
        } else if (diffInMinutes < 60) {
            if (dateTimeFormatterCallback != null) {
                String minutes = dateTimeFormatterCallback.minutes(diffInMinutes, timestamp);
                if (minutes != null) {
                    return minutes;
                }
            }
            return context.getResources().getString(R.string.cometchat_last_seen) + " " + context
                .getResources()
                .getQuantityString(R.plurals.cometchat_last_seen_minutes_ago, (int) diffInMinutes, (int) diffInMinutes);
        }

        // Check if the timestamp is within the last 24 hours
        if (diffInHours < 24) {
            if (diffInHours < 2) {
                if (dateTimeFormatterCallback != null) {
                    String hour = dateTimeFormatterCallback.hour(timestamp);
                    if (hour != null) {
                        return hour;
                    }
                }
            } else {
                if (dateTimeFormatterCallback != null) {
                    String hours = dateTimeFormatterCallback.hours(diffInHours, timestamp);
                    if (hours != null) {
                        return hours;
                    }
                }
            }
            return context.getResources().getString(R.string.cometchat_last_seen) + " " + context
                .getResources()
                .getQuantityString(R.plurals.cometchat_last_seen_hours_ago, (int) diffInHours, (int) diffInHours);
        }

        if (dateTimeFormatterCallback != null) {
            String otherDays = dateTimeFormatterCallback.otherDays(timestamp);
            if (otherDays != null) {
                return otherDays;
            }
        }

        // Determine if the timestamp is within the current year
        boolean isSameYear = lastSeen.get(Calendar.YEAR) == now.get(Calendar.YEAR);
        String datePattern;

        if (isSameYear) {
            // If it's within the same year, show only day and month
            datePattern = "dd MMM";
        } else {
            // If it's a previous year, include the year
            datePattern = "dd MMM yyyy";
        }

        // Append time to the date pattern
        datePattern += " 'at' hh:mm a";

        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, CometChatLocalize.getDefault());
        return context.getResources().getString(R.string.cometchat_last_seen) + " " + dateFormat.format(new Date(timestamp));
    }

    public static String getDateTimeMessageInformation(long milliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy, h:mm a", CometChatLocalize.getDefault());
        Date date = new Date(milliseconds);
        return sdf.format(date);
    }

    public static String callLogsTimeStamp(long timestamp, @Nullable SimpleDateFormat dateFormat) {
        return callLogsTimeStamp(timestamp, dateFormat, null);
    }

    /**
     * Formats the given timestamp into a human-readable date string based on the
     * provided pattern. If the pattern is null, the default formats are applied. -
     * If the year matches the current year, the format will be: "8 August, 8:14
     * PM". - If the year is not the current year, the format will be: "8 August
     * 2022, 8:14 PM".
     *
     * @param timestamp  The timestamp.
     * @param dateFormat The desired date format pattern. If null, the default format is
     *                   applied.
     * @return A formatted date string.
     */
    public static String callLogsTimeStamp(long timestamp, @Nullable SimpleDateFormat dateFormat, DateTimeFormatterCallback dateTimeFormatter) {
        if (String.valueOf(timestamp).length() == 10) {
            // Convert seconds to milliseconds
            timestamp *= 1000;
        }

        // Convert the timestamp to a Date object
        Date date = new Date(timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Get the current year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int inputYear = calendar.get(Calendar.YEAR);

        if (dateTimeFormatter != null) {
            String dateTime = dateTimeFormatter.otherDays(timestamp);
            if (dateTime != null) {
                return dateTime;
            }
        }

        // Apply default formats if dateFormat is null
        if (dateFormat == null) {
            if (inputYear == currentYear) {
                dateFormat = new SimpleDateFormat("d MMMM, h:mm a", CometChatLocalize.getDefault());
                // "8 August, 8:14 PM"
            } else {
                dateFormat = new SimpleDateFormat("d MMMM yyyy, h:mm a", CometChatLocalize.getDefault()); // "8 August 2022, 8:14 PM"
            }
        }

        return dateFormat.format(date);
    }
}
