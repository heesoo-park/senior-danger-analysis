package com.example.SDA.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.SDA.Activity.SplashActivity;

public class BootReceiver extends BroadcastReceiver {
    public static final String ACTION_RESTART_PERSISTENTSERVICE = "ACTION.RESTART.PersistentService";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent splashIntent = new Intent(context, SplashActivity.class);
            context.startActivity(splashIntent);
        }
    }
}