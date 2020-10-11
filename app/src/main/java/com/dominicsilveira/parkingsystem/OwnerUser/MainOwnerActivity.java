package com.dominicsilveira.parkingsystem.OwnerUser;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dominicsilveira.parkingsystem.NormalUser.MainNormalActivity;
import com.dominicsilveira.parkingsystem.NormalUser.NearByAreaActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.utils.services.MyParkingService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isMyServiceRunning(MyParkingService.class))
            MainOwnerActivity.this.startService(new Intent(MainOwnerActivity.this, MyParkingService.class));
        startService(new Intent(MainOwnerActivity.this, MyParkingService.class));

        globalClass=(AppConstants)getApplicationContext();

        db=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        if(auth.getCurrentUser()==null){
            Intent intent=new Intent(MainOwnerActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

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
        ActivityManager manager = (ActivityManager) MainOwnerActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}