package com.deepblue.cleaning.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.deepblue.cleaning.activity.WelcomeActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {

    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(action_boot)) {
            Intent mWelcomeActivity = new Intent(context, WelcomeActivity.class);
            // 这里必须为FLAG_ACTIVITY_NEW_TASK
            mWelcomeActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mWelcomeActivity);
        }
    }
}