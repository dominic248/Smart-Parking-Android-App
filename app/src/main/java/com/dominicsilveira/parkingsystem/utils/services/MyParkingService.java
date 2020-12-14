package com.dominicsilveira.parkingsystem.utils.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dominicsilveira.parkingsystem.OwnerUser.AddPositionActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.common.BookingDetailsActivity;
import com.dominicsilveira.parkingsystem.utils.notifications.AlarmUtils;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.utils.notifications.NotificationHelper;
import com.dominicsilveira.parkingsystem.utils.notifications.NotificationReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyParkingService extends Service {

    FirebaseAuth auth;
    FirebaseDatabase db;
    boolean foundArea=false;

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
        foundArea=false;

        if(auth.getCurrentUser()!=null){
            db.getReference().child("BookedSlots")
                    .addChildEventListener(new ChildEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            BookedSlots bookedSlots = snapshot.getValue(BookedSlots.class);
                            updateBookedSlots(snapshot,bookedSlots);
                            notificationUpdate();
                        }
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            BookedSlots bookedSlots = snapshot.getValue(BookedSlots.class);
                            updateBookedSlots(snapshot,bookedSlots);
                            notificationUpdate();
                        }
                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

            db.getReference().child("ParkingAreas")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
                                if(parkingArea.userID.equals(auth.getCurrentUser().getUid())){
                                    foundArea=true;
                                    setAdminNotification(dataSnapshot.getKey());
                                    break;
                                }
                                Log.d(String.valueOf(this.getClass()),"Load parking Area: "+String.valueOf(parkingArea));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
        return START_STICKY;
    }

    private void setAdminNotification(final String parkingArea) {
        db.getReference().child("BookedSlots")
                .addChildEventListener(new ChildEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        BookedSlots bookedSlots = snapshot.getValue(BookedSlots.class);
                        setExceedAlarm(snapshot,bookedSlots,parkingArea);
                        notificationUpdate();
                    }

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        BookedSlots bookedSlots = snapshot.getValue(BookedSlots.class);
                        setExceedAlarm(snapshot,bookedSlots,parkingArea);
                        notificationUpdate();
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setExceedAlarm(DataSnapshot snapshot, BookedSlots bookedSlots,String parkingArea) {
        if(auth.getCurrentUser()!=null){
            if(bookedSlots.placeID.equals(parkingArea) && bookedSlots.checkout==0 && bookedSlots.hasPaid==1) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(bookedSlots.endTime);
                Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                intent.putExtra("title", snapshot.getKey());
                intent.putExtra("message", "User exceeded time limit!");
                intent.putExtra("notificationID", Math.abs(bookedSlots.notificationID));
                intent.putExtra("readID", snapshot.getKey());
                intent.putExtra("admin", 1);
                intent.putExtra("when", "later");
                AlarmUtils.addAlarm(getApplicationContext(),
                        intent,
                        Math.abs(bookedSlots.notificationID)-1,
                        calendar);
                Log.i("NotificationTrigger","Add Admin alarm");
            }else if(bookedSlots.placeID.equals(parkingArea) && bookedSlots.checkout==1 && bookedSlots.hasPaid==1){
                Log.i("NotificationTrigger","Cancel Admin alarm");
                Intent notifyIntent = new Intent(getApplicationContext(), NotificationReceiver.class);
                AlarmUtils.cancelAlarm(getApplicationContext(),notifyIntent,bookedSlots.notificationID);
            }
        }
    }

    private void updateBookedSlots(DataSnapshot snapshot,BookedSlots bookedSlots) {
        if(auth.getCurrentUser()!=null){
            if((bookedSlots.readNotification==0 && bookedSlots.checkout==0 && bookedSlots.hasPaid==1) && bookedSlots.userID.equals(auth.getCurrentUser().getUid())) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(bookedSlots.endTime);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                Log.i(String.valueOf(this.getClass()), snapshot.getKey() + " onChildChanged ,id " + Math.abs(bookedSlots.notificationID) + ", Success: Alarm at " + simpleDateFormat.format(calendar.getTime()));

                if (calendar.before(Calendar.getInstance()))
                    Log.i(String.valueOf(this.getClass()),"Old Notification!");
                else {
                    Log.i(String.valueOf(this.getClass()),"New Notification!");
                }

                Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                intent.putExtra("title", snapshot.getKey());
                intent.putExtra("message", "Check-out your Booking");
                intent.putExtra("notificationID", Math.abs(bookedSlots.notificationID));
                intent.putExtra("readID", snapshot.getKey());
                intent.putExtra("when", "later");
                calendar.add(Calendar.MINUTE, -5); //subtract 5 minutes
                AlarmUtils.addAlarm(getApplicationContext(),
                        intent,
                        Math.abs(bookedSlots.notificationID)-1,
                        calendar);
                Log.i("NotificationTrigger","Add user checkout alarm");
            }
            if(bookedSlots.readBookedNotification==0 && bookedSlots.checkout==0 && bookedSlots.userID.equals(auth.getCurrentUser().getUid())){
                Calendar calendar = Calendar.getInstance();
                Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                intent.putExtra("title", snapshot.getKey());
                intent.putExtra("message", "Confirm your Booking");
                intent.putExtra("notificationID", Math.abs(bookedSlots.notificationID));
                intent.putExtra("readID", snapshot.getKey());
                intent.putExtra("when", "now");
                AlarmUtils.addAlarm(getApplicationContext(),
                        intent,
                        Math.abs(bookedSlots.notificationID)-1,
                        calendar);
//                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), Math.abs(bookedSlots.notificationID)-1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//                } else {
//                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//                }
                Log.i("NotificationTrigger","Add user confirm alarm");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void notificationUpdate() {
        NotificationHelper notificationHelper=new NotificationHelper(getApplicationContext());
        int notificationCount=notificationHelper.countNotificationGroup(getApplicationContext().getString(R.string.notification_group_id_1));
        if(notificationCount<=1){
            notificationHelper.cancelNotification(AppConstants.NOTIFICATION_GROUP_REQUEST_CODE);
        }
        Log.i(String.valueOf(this.getClass()),"Notifications Count: ".concat(String.valueOf(notificationCount)));
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