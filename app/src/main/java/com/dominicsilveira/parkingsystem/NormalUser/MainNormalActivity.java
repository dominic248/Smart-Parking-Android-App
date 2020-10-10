package com.dominicsilveira.parkingsystem.NormalUser;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.utils.services.MyParkingService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainNormalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_normal);

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
}