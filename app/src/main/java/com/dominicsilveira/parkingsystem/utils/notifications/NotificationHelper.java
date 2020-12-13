package com.dominicsilveira.parkingsystem.utils.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.NormalUser.MainNormalActivity;
import com.dominicsilveira.parkingsystem.RegisterLogin.SplashScreen;

public class NotificationHelper extends ContextWrapper {
    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannelGroup groupNotificationChannel=new NotificationChannelGroup(
                getApplicationContext().getString(R.string.notification_group_id_1),
                getApplicationContext().getString(R.string.notification_group_name_1)
        );
        NotificationChannel notificationChannel=new NotificationChannel(
                getApplicationContext().getString(R.string.notification_channel_id_1),
                getApplicationContext().getString(R.string.notification_channel_name_1),
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(R.color.colorPrimary);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationChannel.setGroup(getApplicationContext().getString(R.string.notification_group_id_1));

        getManager().createNotificationChannelGroup(groupNotificationChannel);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager(){
        if(mManager==null){
            mManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder setLaterChannelNotification(String title, String message,String readID,int notificationID){
        Log.i("NotificationSet",title+":"+message);

        Intent openIntent=new Intent(this, SplashScreen.class);
        openIntent.putExtra("ACTIVITY_NO",34);
        openIntent.putExtra("ORDER_ID",title);
        PendingIntent openPendingIntent=PendingIntent.getActivity(this,notificationID+1,openIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentAction = new Intent(this, NotificationActionReceiver.class);
        intentAction.putExtra("action","MarkAsReadCheckout");
        intentAction.putExtra("readID",readID);
        intentAction.putExtra("notificationID",notificationID);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pIntentMarkAsRead = PendingIntent.getBroadcast(this,notificationID+2,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(),getApplicationContext().getString(R.string.notification_channel_id_1))
                .setContentTitle("Order ID: " + title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                .setAutoCancel(true)
                .setGroup(getApplicationContext().getString(R.string.notification_group_id_1))
                .setContentIntent(openPendingIntent)
                .addAction(0, "Mark As Read", pIntentMarkAsRead);
    }

    public NotificationCompat.Builder setAdminLaterChannelNotification(String title, String message,String readID,int notificationID){
        Log.i("NotificationSet",title+":"+message);

        Intent openIntent=new Intent(this, SplashScreen.class);
        openIntent.putExtra("ACTIVITY_NO",34);
        openIntent.putExtra("ORDER_ID",title);
        PendingIntent openPendingIntent=PendingIntent.getActivity(this,notificationID+1,openIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(),getApplicationContext().getString(R.string.notification_channel_id_1))
                .setContentTitle("Order ID: " + title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                .setAutoCancel(true)
                .setGroup(getApplicationContext().getString(R.string.notification_group_id_1))
                .setContentIntent(openPendingIntent);
    }

    public NotificationCompat.Builder setNowChannelNotification(String title, String message,String readID,int notificationID){
        Log.i("NotificationSet",title+":"+message);

        Intent openIntent=new Intent(this, SplashScreen.class);
        openIntent.putExtra("ACTIVITY_NO",34);
        openIntent.putExtra("ORDER_ID",title);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent openPendingIntent=PendingIntent.getActivity(this,notificationID+1,openIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentAction = new Intent(this, NotificationActionReceiver.class);
        intentAction.putExtra("action","MarkAsReadBooking");
        intentAction.putExtra("readID",readID);
        intentAction.putExtra("notificationID",notificationID);
        intentAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pIntentMarkAsRead = PendingIntent.getBroadcast(this,notificationID+2,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(),getApplicationContext().getString(R.string.notification_channel_id_1))
                .setContentTitle("Order ID: " + title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                .setAutoCancel(true)
                .setGroup(getApplicationContext().getString(R.string.notification_group_id_1))
                .setContentIntent(openPendingIntent)
                .addAction(0, "Mark As Read", pIntentMarkAsRead);
    }

    public void cancelNotification(int id) {
        //you can get notificationManager like this:
        getManager().cancel(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public int countNotificationGroup(String notificationGroupID) {
        int count=0;
        StatusBarNotification[] activeNotifications = getManager().getActiveNotifications();
        for (StatusBarNotification activeNotification : activeNotifications) {
            Log.e("GroupID",activeNotification.getGroupKey().toString().split("g:")[1]+" "+activeNotification.getGroupKey().toString());
            if(activeNotification.getGroupKey().toString().split("g:")[1].equals(notificationGroupID)){
                count+=1;
            }
        }
        return count;
    }
}
