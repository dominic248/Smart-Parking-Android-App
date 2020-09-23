package com.dominicsilveira.parkingsystem.utils.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.dominicsilveira.parkingsystem.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class NotificationActionReceiver extends BroadcastReceiver {

    FirebaseAuth auth;
    FirebaseDatabase db;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void performMarkAsRead(Context context, Intent intent){
        NotificationHelper notificationHelper=new NotificationHelper(context);
        Log.e("Welcom", Objects.requireNonNull(intent.getStringExtra("readID")));
        db.getReference("BookedSlots").child(Objects.requireNonNull(intent.getStringExtra("readID"))).child("readNotification").setValue(1);
        notificationHelper.cancelNotification(intent.getIntExtra("notificationID",1));
        int notificationCount=notificationHelper.countNotificationGroup(context.getString(R.string.notification_group_id_1));
        if(notificationCount==1){
            notificationHelper.cancelNotification(2);
        }
        Log.e("CountNotify",String.valueOf(notificationCount));
    }

    public void performAction2(){

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();
        String action=intent.getStringExtra("action");
        if(action.equals("MarkAsRead")){
            performMarkAsRead(context, intent);
        }
        else if(action.equals("action2")){
            performAction2();

        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }
}