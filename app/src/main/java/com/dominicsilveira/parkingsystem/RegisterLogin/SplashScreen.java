package com.dominicsilveira.parkingsystem.RegisterLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {

    boolean isGPS;
    FirebaseAuth auth;
    int activityInt;
    Intent parentIntent,intent;
    Boolean showIntro=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth=FirebaseAuth.getInstance();
        SharedPreferences sh = getSharedPreferences("ShowIntro", MODE_PRIVATE);// The value will be default as empty string because for the very first time when the app is opened, there is nothing to show
        if (sh.getBoolean("show",true)){
            showIntro = true;
        }
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
        Uri uri = parentIntent.getData();
        if (uri != null && auth.getCurrentUser()==null) {
            // Get the action to complete.
            String mode = uri.getQueryParameter("mode");
            // Get the one-time code from the query parameter.
            String actionCode = uri.getQueryParameter("oobCode");

            if(mode.equals("resetPassword")){
                resetPasswordActivity(actionCode);
            }else if(mode.equals("recoverEmail")){
                recoverAccount(actionCode);
            }else if(mode.equals("verifyEmail")){
                verifyAccount(actionCode);
            }
        }else{
            activityInt= parentIntent.getIntExtra("ACTIVITY_NO",0);
            Log.e("location","enabled");
            isGPS = true; // flag maintain before get location
            if(showIntro && auth.getCurrentUser()==null){
                intent=new Intent(SplashScreen.this, StepperWizardActivity.class);
                startActivity(intent);
                finish();
            }else if(auth.getCurrentUser()==null){
                intent=new Intent(SplashScreen.this, LoginActivity.class);
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
                            }else if(activityInt==34){
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

    private void recoverAccount(String actionCode) {
        intent=new Intent(SplashScreen.this, RecoverAccountActivity.class);
        intent.putExtra("TOKEN",actionCode);
        startActivity(intent);
        finish();
    }

    private void verifyAccount(String actionCode) {
        intent=new Intent(SplashScreen.this, VerifiedActivity.class);
        intent.putExtra("TOKEN",actionCode);
        startActivity(intent);
        finish();
    }

    private void resetPasswordActivity(String actionCode) {
        intent=new Intent(SplashScreen.this, ResetPasswordActivity.class);
        intent.putExtra("TOKEN",actionCode);
        startActivity(intent);
        finish();
    }
}