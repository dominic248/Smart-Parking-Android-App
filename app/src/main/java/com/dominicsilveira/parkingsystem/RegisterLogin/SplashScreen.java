package com.dominicsilveira.parkingsystem.RegisterLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dominicsilveira.parkingsystem.common.BookingDetailsActivity;
import com.dominicsilveira.parkingsystem.NormalUser.GPSMapActivity;
import com.dominicsilveira.parkingsystem.NormalUser.MainNormalActivity;
import com.dominicsilveira.parkingsystem.NormalUser.NearByAreaActivity;
import com.dominicsilveira.parkingsystem.NormalUser.UserHistoryActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.AreaHistoryActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.utils.gps.GpsUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {

    boolean isGPS;
    FirebaseAuth auth;
    int activityInt;
    Intent parentIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth=FirebaseAuth.getInstance();
        new GpsUtils(SplashScreen.this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
                startApp();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST_CODE) {
                Log.e("location","enabled");
                isGPS = true; // flag maintain before get location
                startApp();
            }
        }
    }

    private void startApp() {
        parentIntent=getIntent();
        activityInt= parentIntent.getIntExtra("ACTIVITY_NO",0);
        Log.e("location","enabled");
        isGPS = true; // flag maintain before get location
        if(auth.getCurrentUser()==null){
            Intent intent=new Intent(SplashScreen.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            final AppConstants globalClass=(AppConstants)getApplicationContext();
            FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User userObj=snapshot.getValue(User.class);
                    Log.e("userTyp",String.valueOf(userObj.userType));
                    globalClass.setUserObj(userObj);
                    Intent intent;
                    if(activityInt==0){
                        if (userObj.userType==2)
                            intent=new Intent(SplashScreen.this, MainOwnerActivity.class);
                        else
                            intent=new Intent(SplashScreen.this, MainNormalActivity.class);
                    }else{      // For Home Screen Widgets
                        if(activityInt==21 && userObj.userType==2){
                            intent=new Intent(SplashScreen.this, MainOwnerActivity.class);
                        }else if(activityInt==22 && userObj.userType==2){
                            intent=new Intent(SplashScreen.this, AreaHistoryActivity.class);
                        }else if(activityInt==31 && userObj.userType==3){
                            intent=new Intent(SplashScreen.this, GPSMapActivity.class);
                        }else if(activityInt==32 && userObj.userType==3){
                            intent=new Intent(SplashScreen.this, NearByAreaActivity.class);
                        }else if(activityInt==33 && userObj.userType==3){
                            intent=new Intent(SplashScreen.this, UserHistoryActivity.class);
                        }else if(activityInt==34 && userObj.userType==3){
                            intent=new Intent(SplashScreen.this, BookingDetailsActivity.class);
                            intent.putExtra("UUID",parentIntent.getStringExtra("ORDER_ID"));
                        }else if(userObj.userType==2){
                            intent=new Intent(SplashScreen.this, MainOwnerActivity.class);
                        }else if(userObj.userType==3){
                            intent=new Intent(SplashScreen.this, MainNormalActivity.class);
                        }else{
                            intent=new Intent(SplashScreen.this, LoginActivity.class);
                        }
                    }
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}