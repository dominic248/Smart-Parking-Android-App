package com.dominicsilveira.parkingsystem.utils.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dominicsilveira.parkingsystem.AppConstants;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.utils.notifications.NotificationReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyParkingService extends Service {

    FirebaseAuth auth;
    FirebaseDatabase db;
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(MyParkingService.this,"bind",Toast.LENGTH_SHORT).show();
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(String.valueOf(this.getClass()),"Service onStartCommand");
        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();

        db.getReference().child("BookedSlots")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        updateBookedSlots(snapshot);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        updateBookedSlots(snapshot);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        return START_STICKY;
    }

    private void updateBookedSlots(DataSnapshot snapshot) {
        BookedSlots bookedSlots = snapshot.getValue(BookedSlots.class);
        if(bookedSlots.readNotification==0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(bookedSlots.endTime);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            Log.i(String.valueOf(this.getClass()), snapshot.getKey() + " onChildChanged ,id " + Math.abs(bookedSlots.notificationID) + ", Success: Alarm at " + simpleDateFormat.format(calendar.getTime()));

            if (calendar.before(Calendar.getInstance()))
                Toast.makeText(MyParkingService.this, "Invalid! End time can't be  less than Start time!", Toast.LENGTH_SHORT).show();
            else {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                intent.putExtra("title", snapshot.getKey());
                intent.putExtra("message", "Confirm your Booking");
                intent.putExtra("notificationID", Math.abs(bookedSlots.notificationID));
                intent.putExtra("readID", snapshot.getKey());
                PendingIntent pendingIntent=PendingIntent.getBroadcast(getApplicationContext(),
                        Integer.parseInt(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)).concat(String.valueOf(calendar.get(Calendar.MONTH)))
                                .concat(String.valueOf(calendar.get(Calendar.HOUR)))
                                .concat(String.valueOf(calendar.get(Calendar.MINUTE)))
                                .concat(String.valueOf(calendar.get(Calendar.AM_PM))))
                        ,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(String.valueOf(this.getClass()),"Service onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Log.i(String.valueOf(this.getClass()),"Service onTaskRemoved");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), AppConstants.RESTART_SERVICE_REQUEST_CODE, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(),
                restartServicePendingIntent);
        super.onTaskRemoved(rootIntent);
    }
}