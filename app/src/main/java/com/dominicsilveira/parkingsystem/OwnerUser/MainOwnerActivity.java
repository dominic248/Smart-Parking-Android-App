package com.dominicsilveira.parkingsystem.OwnerUser;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.NormalUser.MainNormalActivity;
import com.dominicsilveira.parkingsystem.NormalUser.NearByAreaActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainOwnerActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;

    AppConstants globalClass;
    Boolean dialogshown=false;

    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
        Log.i("RESUMETAGG","alertVerifyEmail();1");
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

        if(!isMyServiceRunning(MyParkingService.class))
            MainOwnerActivity.this.startService(new Intent(MainOwnerActivity.this, MyParkingService.class));
        startService(new Intent(MainOwnerActivity.this, MyParkingService.class));

        initComponents();
        attachListeners();
    }

    private void initComponents() {
        globalClass=(AppConstants)getApplicationContext();

        db=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

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

    private void attachListeners() {
        db.getReference().child("ParkingAreas")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int found=0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
                            if(parkingArea.userID.equals(auth.getCurrentUser().getUid())){
                                found=1;
                            }
                            Log.e("MainOwner", String.valueOf(parkingArea.longitude));
                        }
                        if(found==1){
                            setContentView(R.layout.activity_main_owner);
                            BottomNavigationView navView = findViewById(R.id.nav_view);
                            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                                    R.id.navigation_dashboard, R.id.navigation_add, R.id.navigation_profile)
                                    .build();
                            NavController navController = Navigation.findNavController(MainOwnerActivity.this, R.id.nav_host_fragment);
                            NavigationUI.setupActionBarWithNavController(MainOwnerActivity.this, navController, appBarConfiguration);
                            NavigationUI.setupWithNavController(navView, navController);
                            Intent mIntent = getIntent();
                            int bottomInt= mIntent.getIntExtra("FRAGMENT_NO",0);
                            if(bottomInt==0){
                                navView.setSelectedItemId(R.id.navigation_dashboard);
                            }else if(bottomInt==1){
                                navView.setSelectedItemId(R.id.navigation_add);
                            }else if(bottomInt==2){
                                navView.setSelectedItemId(R.id.navigation_profile);
                            }
                        }else{
                            Intent intent = new Intent(MainOwnerActivity.this, AddPositionActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        dialogshown=true;
        ActivityManager manager = (ActivityManager) MainOwnerActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void alertVerifyEmail() {
        final AlertDialog dialog = new AlertDialog.Builder(MainOwnerActivity.this)
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
                try{
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
                                Toast.makeText(MainOwnerActivity.this, "Verify email " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }catch(Exception e){
                    Log.i("isAuthenticatedCheck", String.valueOf(auth.getCurrentUser()));
                    FirebaseAuth.getInstance().signOut();
                    stopService(new Intent(MainOwnerActivity.this, MyParkingService.class));
                    AlarmUtils.cancelAllAlarms(MainOwnerActivity.this,new Intent(MainOwnerActivity.this, NotificationReceiver.class));
                    Toast.makeText(MainOwnerActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainOwnerActivity.this, LoginActivity.class));
                    finish();
                    e.printStackTrace();
                }

            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                dialog.cancel();
                Toast.makeText(MainOwnerActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainOwnerActivity.this, LoginActivity.class));
                finish();
            }
        });
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser user = auth.getCurrentUser();
                user.sendEmailVerification()
                        .addOnCompleteListener(MainOwnerActivity.this, new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainOwnerActivity.this, "Verification email sent to " + user.getEmail()+"!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainOwnerActivity.this, "Failed to send verification email!", Toast.LENGTH_SHORT).show();
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