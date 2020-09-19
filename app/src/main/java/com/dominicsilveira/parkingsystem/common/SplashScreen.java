package com.dominicsilveira.parkingsystem.common;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.AppConstants;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        FirebaseAuth auth=FirebaseAuth.getInstance();

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
                    Intent intent=new Intent(SplashScreen.this, MainActivity.class);
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