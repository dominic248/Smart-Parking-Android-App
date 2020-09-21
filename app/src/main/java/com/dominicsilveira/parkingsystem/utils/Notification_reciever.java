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

public class Notification_reciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent openIntent=new Intent(context, MainNormalActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,100,openIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context, "notifyLemubit")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_baseline_keyboard_arrow_down_24)
                .setContentTitle("Notification Title")
                .setContentText("Text")
                .setAutoCancel(true);
        notificationManager.notify(100,builder.build());
    }
}
