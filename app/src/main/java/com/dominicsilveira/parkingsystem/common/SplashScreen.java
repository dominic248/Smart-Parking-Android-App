package com.dominicsilveira.parkingsystem.common;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.dominicsilveira.parkingsystem.AppConstants;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.utils.GpsUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {

    boolean isGPS;
    FirebaseAuth auth;

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
            if (requestCode == AppConstants.GPS_REQUEST) {
                Log.e("location","enabled");
                isGPS = true; // flag maintain before get location
                startApp();
            }
        }
    }

    private void startApp() {
        Log.e("location","enabled");
        isGPS = true; // flag maintain before get location
        if(auth.getCurrentUser()==null){
            Intent intent=new Intent(SplashScreen.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            final AppConstants globalClass=(AppConstants)getApplicationContext();
            FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid()).child("userType").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int val=snapshot.getValue(int.class);
                    Log.e("userTyp",String.valueOf(val));
                    globalClass.setUserType(val);
                    Intent intent;
                    if (val==2)
                        intent=new Intent(SplashScreen.this, MainOwnerActivity.class);
                    else
                        intent=new Intent(SplashScreen.this, MainNormalActivity.class);
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