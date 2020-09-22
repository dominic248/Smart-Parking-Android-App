package com.dominicsilveira.parkingsystem.utils.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

public class NotificationActionReceiver extends BroadcastReceiver {

    public void performMarkAsRead(Intent intent){
        Log.e("Welcom", Objects.requireNonNull(intent.getStringExtra("readID")));
    }

    public void performAction2(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();
        String action=intent.getStringExtra("action");
        if(action.equals("MarkAsRead")){
            performMarkAsRead(intent);
        }
        else if(action.equals("action2")){
            performAction2();

        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }
}