package com.dominicsilveira.parkingsystem.utils.notifications;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class NotificationActionReceiver extends BroadcastReceiver {

    FirebaseAuth auth;
    FirebaseDatabase db;

    public void performMarkAsRead(Context context, Intent intent){
        NotificationHelper notificationHelper=new NotificationHelper(context);
        Log.e("Welcom", Objects.requireNonNull(intent.getStringExtra("readID")));
        db.getReference("BookedSlots").child(Objects.requireNonNull(intent.getStringExtra("readID"))).child("readNotification").setValue(1);
        notificationHelper.cancelNotification(intent.getIntExtra("notificationID",1));
    }

    private void showDateTime(Context context) {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();
        String action=intent.getStringExtra("action");
        if(action.equals("MarkAsRead")){
            performMarkAsRead(context, intent);
        }else if(action.equals("Calendar")){
            showDateTime(context);
            //This is used to close the notification tray
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }
    }
}