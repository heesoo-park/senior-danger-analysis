package com.example.SDA.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.SDA.Class.PreferenceManager;
import com.example.SDA.Service.FirebaseAuthService;
import com.example.SDA.Service.ForegroundService;

public class BootReceiver extends BroadcastReceiver {
    public static final String ACTION_RESTART_PERSISTENTSERVICE = "ACTION.RESTART.PersistentService";
    private FirebaseAuthService authService;

    @Override
    public void onReceive(Context context, Intent intent) {
        authService = new FirebaseAuthService();
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            if (!authService.isLogin() || PreferenceManager.getString(context, PreferenceManager.CARE_ID) == null) {
                return;
            }
            Intent serviceIntent = new Intent(context, ForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}