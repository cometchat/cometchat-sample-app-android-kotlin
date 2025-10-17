package com.example.android_ui_kit_clone_test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.AppSettings;

public class CometChatBackgroundService extends Service {
    private static final String TAG = "CometChatBgService";
    private static final String APP_ID = "YOUR_COMET_CHAT_APP_ID"; // Replace with your actual App ID
    private static final String REGION = "YOUR_REGION"; // Replace with your region

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Background service started - initializing CometChat");
        
        initializeCometChat();
        
        return START_NOT_STICKY;
    }

    private void initializeCometChat() {
        AppSettings appSettings = new AppSettings.AppSettingsBuilder()
                .subscribePresenceForAllUsers()
                .setRegion(REGION)
                .build();

        CometChat.init(this, APP_ID, appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
                Log.d(TAG, "CometChat initialization completed successfully: " + successMessage);
                killApp();
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "CometChat initialization failed with exception: " + e.getMessage());
                killApp();
            }
        });
    }

    private void killApp() {
        Log.d(TAG, "Stopping app after CometChat initialization");
        
        // Stop the service
        stopSelf();
        
        // Kill the app process
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Background service destroyed");
    }
}