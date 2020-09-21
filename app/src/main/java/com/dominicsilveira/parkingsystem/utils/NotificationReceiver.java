package com.dominicsilveira.parkingsystem.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.common.MainNormalActivity;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper=new NotificationHelper(context);
        NotificationCompat.Builder nb= notificationHelper.getChannelNotification("Title","Alarm at time");
        notificationHelper.getManager().notify(1,nb.build());
    }
}
