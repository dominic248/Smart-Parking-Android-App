package com.dominicsilveira.parkingsystem.utils.notifications;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.dominicsilveira.parkingsystem.AppConstants;
import com.dominicsilveira.parkingsystem.R;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper=new NotificationHelper(context);
        NotificationCompat.Builder nb= notificationHelper.getChannelNotification(intent.getStringExtra("title"),intent.getStringExtra("message"),intent.getStringExtra("readID"),intent.getIntExtra("notificationID",1));
        Notification notification=nb.build();
        notificationHelper.getManager().notify(intent.getIntExtra("notificationID",1),notification);
        NotificationCompat.Builder summary=new NotificationCompat.Builder(context,context.getString(R.string.notification_channel_id_1))
                .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                .setGroup(context.getString(R.string.notification_group_id_1))
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                .setGroupSummary(true);
        notificationHelper.getManager().notify(AppConstants.NOTIFICATION_GROUP_REQUEST_CODE,summary.build());
    }
}
