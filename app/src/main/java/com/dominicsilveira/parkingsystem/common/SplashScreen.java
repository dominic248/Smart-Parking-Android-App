package com.dominicsilveira.parkingsystem.common;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            Intent intent=new Intent(SplashScreen.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent=new Intent(SplashScreen.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }
}