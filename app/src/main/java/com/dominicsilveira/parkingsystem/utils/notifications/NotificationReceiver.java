package com.dominicsilveira.parkingsystem.utils.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper=new NotificationHelper(context);
        NotificationCompat.Builder nb= notificationHelper.getChannelNotification(intent.getStringExtra("title"),intent.getStringExtra("message"),intent.getStringExtra("readID"));
        notificationHelper.getManager().notify(intent.getIntExtra("notificationID",1),nb.build());
    }
}
