package com.dominicsilveira.parkingsystem.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.ui.dashboard.DashboardFragment;
import com.dominicsilveira.parkingsystem.ui.profile.ProfileFragment;
import com.dominicsilveira.parkingsystem.ui.scan.ScanFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

}