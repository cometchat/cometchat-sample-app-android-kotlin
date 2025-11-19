package com.cometchat.chatuikit.shared.resources.utils;

import static com.cometchat.chatuikit.shared.resources.utils.Utils.generateFileName;
import static com.cometchat.chatuikit.shared.resources.utils.Utils.getDocumentCacheDir;
import static com.cometchat.chatuikit.shared.resources.utils.Utils.getFileName;
import static com.cometchat.chatuikit.shared.resources.utils.Utils.getMimeTypeFromFile;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chatuikit.BuildConfig;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MediaUtils {
    private static final String TAG = MediaUtils.class.getSimpleName();

    public static String pictureImagePath;

    private static ProgressDialog mProgressDialog;

    private static BaseMessage baseMessage;

    public static Uri uri;

    public static Intent getFileIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
    }

    /**
     * This method is used to open file from url.
     *
     * @param url is Url of file.
     */
    public static void openFile(String url, Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    // Method to open media with the available player app on the device
    public static void openMediaInPlayer(Context context, String url, String type) {
        if (url != null && type != null) {
            // Create an intent with ACTION_VIEW to open the media with a compatible app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), type);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Verify if there's an app available to handle this intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }
        }
    }

    public static void downloadFile(Context context, String fileUrl, String fileName, String mimeType) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setTitle(fileName + mimeType);
        // request.setDescription(fileName + mimeType);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + mimeType);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    public static void openFile(Context context, File file) {
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, getMimeTypeFromFile(context, file));
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static Intent openCamera(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(pictureImagePath);
        Uri outputFileUri;
        ApplicationInfo app;
        String provider = null;
        try {
            app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            provider = bundle.getString(BuildConfig.LIBRARY_PACKAGE_NAME);
            Log.d("openCamera", "openCamera:  " + provider);
        } catch (PackageManager.NameNotFoundException e) {
            CometChatLogger.e(TAG, e.toString());
        }
        outputFileUri = FileProvider.getUriForFile(context, provider + ".provider", file);
        if (Build.VERSION.SDK_INT >= 29) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM");
            outputFileUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            uri = outputFileUri;
        } else if (Build.VERSION.SDK_INT <= 23) {
            outputFileUri = Uri.fromFile(file);
            uri = outputFileUri;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        return intent;
    }

    public static Intent openAudio(Context context) {
        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        Intent audioIntent = new Intent(Intent.ACTION_GET_CONTENT);
        audioIntent.setType("audio/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(audioIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(audioIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent() != null) {
                if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                    mainIntent = intent;
                    break;
                }
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[0]));

        return chooserIntent;
    }

    private static Uri getCaptureImageOutputUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(storageDir.getAbsolutePath() + "/" + imageFileName);
        return Uri.fromFile(file);
    }

    public static String handleCameraImage() {
        return pictureImagePath;
    }

    private static Uri getPickImageResultUri(@Nullable Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }

        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    public static File makeEmptyFileWithTitle(String title) {
        String root;
        if (Build.VERSION.SDK_INT < 29) {
            root = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            root = Environment.DIRECTORY_DOWNLOADS;
        }
        return new File(root, title);
    }

    public static File getRealPath(@NonNull Context context, Uri fileUri, boolean isThirdParty) {
        String realPath;
        if (isGoogleDrive(fileUri) || isThirdParty) {
            return downloadFile(context, fileUri);
        } else if (Build.VERSION.SDK_INT < 28) {
            realPath = getRealPathFromURI(context, fileUri);
        } else {
            realPath = getFilePathForN(fileUri, context);
        }
        return new File(realPath);
    }

    public static File downloadFile(Context context, Uri imageUri) {
        File file = null;
        try {
            if (imageUri != null) {
                file = new File(context.getCacheDir(), getFileName(context, imageUri));
                try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
                    try (OutputStream output = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                        int read;
                        if (inputStream != null) {
                            while ((read = inputStream.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                        }
                        output.flush();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "File Uri is null", Toast.LENGTH_LONG).show();
        }
        return file;
    }

    private static String getFilePathForN(Uri uri, Context context) {
        try (Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (returnCursor == null || !returnCursor.moveToFirst()) {
                throw new IllegalArgumentException("Failed to retrieve data from URI: " + uri);
            }
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            if (nameIndex == -1 || sizeIndex == -1) {
                throw new IllegalArgumentException("Invalid URI columns: " + uri);
            }
            String name = returnCursor.getString(nameIndex);
            File file = new File(context.getFilesDir(), name);

            // Using try-with-resources for InputStream and FileOutputStream
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(file)) {

                if (inputStream == null) {
                    throw new IllegalArgumentException("Failed to open InputStream for URI: " + uri);
                }

                int maxBufferSize = 1024 * 1024;
                byte[] buffer = new byte[maxBufferSize];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return file.getPath();
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
                return null;
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
            return null;
        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and other
     * file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    private static String getRealPathFromURI(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                String id = DocumentsContract.getDocumentId(uri);

                if (id != null) {
                    if (id.startsWith("raw:")) {
                        return id.substring(4);
                    }
                    if (id.startsWith("msf:")) {
                        id = id.substring(4);
                    }
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
                String fileName = getFileName(context, uri);
                File cacheDir = getDocumentCacheDir(context);
                File file = generateFileName(fileName, cacheDir);
                String destinationPath = null;
                if (file != null) {
                    destinationPath = file.getAbsolutePath();
                    saveFileFromUri(context, uri, destinationPath);
                }

                return destinationPath;
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = MediaStore.Images.Media._ID + "=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static void saveFileFromUri(Context context, Uri uri, String destinationPath) {
        InputStream is = null;
        BufferedOutputStream bos = null;
        try {
            // Open the InputStream
            is = context.getContentResolver().openInputStream(uri);
            if (is == null) {
                throw new IOException("Failed to open InputStream for URI: " + uri);
            }

            // Create the OutputStream
            bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));

            // Define buffer and read/write loop
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            CometChatLogger.e(TAG, e.toString());
        } finally {
            try {
                if (is != null) is.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }


    /**
     * Get the value of the data column for this Uri. This is useful for MediaStore
     * Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
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
     * @return Whether the Uri authority is Google Drive
     */
    public static boolean isGoogleDrive(Uri uri) {
        String authority = uri.getAuthority();
        return authority != null && authority.contains("com.google.android.apps.docs.storage");
    }

    public static boolean isWhatsAppMedia(@NonNull Uri uri) {
        String authority = uri.getAuthority();
        return authority != null && authority.contains("com.whatsapp.provider.media");
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(@NonNull Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(@NonNull Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

    public static void downloadFileInNewThread(Context context, String fileUrl, String fileName, String mimeType, String action) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        if (!file.exists()) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(context.getString(R.string.cometchat_downloading) + "...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
        Thread downloadThread = new Thread(() -> {
            try {
                if (!file.exists()) {
                    URL url = new URL(fileUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        handleDownloadFailure(context);
                        return;
                    }

                    int fileLength = connection.getContentLength();
                    InputStream input = connection.getInputStream();

                    FileOutputStream output = new FileOutputStream(file);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    int totalBytesRead = 0;

                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        int progress = (totalBytesRead * 100) / fileLength;
                        updateProgress(context, progress);
                    }

                    output.flush();
                    output.close();
                    input.close();

                    handleDownloadSuccess(context, fileUrl, mimeType, action, file);
                } else {
                    handleDownloadSuccess(context, fileUrl, mimeType, action, file);
                }
            } catch (Exception e) {
                Log.e("DownloadThread", "Error downloading file: " + e.getMessage());
                handleDownloadFailure(context);
            }
        });
        downloadThread.start();
    }

    private static void updateProgress(Context context, final int progress) {
        Activity activity = Utils.getActivity(context);
        if (Utils.isActivityUsable(activity)) {
            activity.runOnUiThread(() -> {
                if (mProgressDialog != null) mProgressDialog.setProgress(progress);
            });
        }
    }

    private static void handleDownloadSuccess(@NonNull Context context, String url, String mimeType, String Action, final File file) {
        Activity activity = Utils.getActivity(context);
        if (Utils.isActivityUsable(activity)) {
            activity.runOnUiThread(() -> {
                if (mProgressDialog != null) mProgressDialog.dismiss();
                if (UIKitConstants.files.OPEN.equals(Action)) {
                    openFile(url, context);
                } else {
                    shareFile(mimeType, context, file);
                }
            });
        }
    }

    private static void handleDownloadFailure(Context context) {
        Activity activity = Utils.getActivity(context);
        if (Utils.isActivityUsable(activity)) {
            activity.runOnUiThread(() -> {
                if (mProgressDialog != null) mProgressDialog.dismiss();
                // File download failed
                // Handle the failure scenario
                Toast.makeText(context, R.string.cometchat_file_download_failed, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private static void shareFile(String mimeType, Context context, File file) {
        try {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file));
            shareIntent.setType(mimeType);
            Intent intent = Intent.createChooser(shareIntent, context.getResources().getString(R.string.cometchat_share));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.cometchat_error) + ":" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
