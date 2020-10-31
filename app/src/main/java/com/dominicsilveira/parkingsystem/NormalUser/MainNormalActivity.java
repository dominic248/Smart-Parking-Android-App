package com.dominicsilveira.parkingsystem.NormalUser;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.utils.notifications.AlarmUtils;
import com.dominicsilveira.parkingsystem.utils.notifications.NotificationReceiver;
import com.dominicsilveira.parkingsystem.utils.services.MyParkingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainNormalActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;

    Boolean dialogshown=false;

    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
        Log.d(String.valueOf(MainNormalActivity.this.getClass()),"Resume verify email");
        auth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                FirebaseUser user = auth.getCurrentUser();
                if(!user.isEmailVerified()){
                    if(!dialogshown) alertVerifyEmail();
                    db.getReference("Users").child(auth.getCurrentUser().getUid()).child("isVerified").setValue(0);
                }else{
                    db.getReference("Users").child(auth.getCurrentUser().getUid()).child("isVerified").setValue(1);
                    db.getReference("Users").child(auth.getCurrentUser().getUid()).child("email").setValue(auth.getCurrentUser().getEmail());
                }
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_normal);

        auth = FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        Log.d(String.valueOf(MainNormalActivity.this.getClass()),"isAuthenticatedCheck: "+String.valueOf(auth.getCurrentUser()));
        auth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                FirebaseUser user = auth.getCurrentUser();
                if(!user.isEmailVerified()){
                    if(!dialogshown) alertVerifyEmail();
                    db.getReference("Users").child(auth.getCurrentUser().getUid()).child("isVerified").setValue(0);
                }else{
                    db.getReference("Users").child(auth.getCurrentUser().getUid()).child("isVerified").setValue(1);
                    db.getReference("Users").child(auth.getCurrentUser().getUid()).child("email").setValue(auth.getCurrentUser().getEmail());
                }
            }
        });

        if(!isMyServiceRunning(MyParkingService.class))
            MainNormalActivity.this.startService(new Intent(MainNormalActivity.this, MyParkingService.class));
        startService(new Intent(MainNormalActivity.this, MyParkingService.class));

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_scan, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Intent mIntent = getIntent();
        int bottomInt= mIntent.getIntExtra("FRAGMENT_NO",0);
        if(bottomInt==0){
            navView.setSelectedItemId(R.id.navigation_dashboard);
        }else if(bottomInt==1){
            navView.setSelectedItemId(R.id.navigation_scan);
        }else if(bottomInt==2){
            navView.setSelectedItemId(R.id.navigation_profile);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) MainNormalActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void alertVerifyEmail() {
        dialogshown=true;
        final AlertDialog dialog = new AlertDialog.Builder(MainNormalActivity.this)
                .setTitle("Verify your E-mail ID!")
                .setMessage("Please Verify your E-mail ID and click on the OK button!")
                .setPositiveButton("YES", null)
                .setNegativeButton("Logout", null)
                .setNeutralButton("Resend E-mail", null)
                .show();


        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(String.valueOf(MainNormalActivity.this.getClass()),"isAuthenticatedCheck: "+String.valueOf(auth.getCurrentUser()));
                auth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            FirebaseUser user = auth.getCurrentUser();
                            if(user.isEmailVerified()){
                                dialog.cancel();
                                dialog.dismiss();
                                dialogshown=false;
                                db.getReference("Users").child(auth.getCurrentUser().getUid()).child("email").setValue(auth.getCurrentUser().getEmail());
                            }else{
                                Toast.makeText(MainNormalActivity.this, "Verify email " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(String.valueOf(MainNormalActivity.this.getClass()),"onFailure: "+e);
                            FirebaseAuth.getInstance().signOut();
                            stopService(new Intent(MainNormalActivity.this, MyParkingService.class));
                            AlarmUtils.cancelAllAlarms(MainNormalActivity.this,new Intent(MainNormalActivity.this, NotificationReceiver.class));
                            Toast.makeText(MainNormalActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainNormalActivity.this, LoginActivity.class));
                            finish();
                            e.printStackTrace();
                        }
                    });
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                dialog.cancel();
                Toast.makeText(MainNormalActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainNormalActivity.this, LoginActivity.class));
                finish();
            }
        });
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser user = auth.getCurrentUser();
                user.sendEmailVerification()
                        .addOnCompleteListener(MainNormalActivity.this, new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainNormalActivity.this, "Verification email sent to " + user.getEmail()+"!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainNormalActivity.this, "Failed to send verification email!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }
}