package com.dominicsilveira.parkingsystem.RegisterLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class VerifiedActivity extends AppCompatActivity {

    ImageView done;
    AnimatedVectorDrawable animatedVectorDrawable;
    AnimatedVectorDrawableCompat animatedVectorDrawableCompat;
    FirebaseAuth auth;
    Intent prevIntent;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verified);

        prevIntent=getIntent();
        token=prevIntent.getStringExtra("TOKEN");
        auth=FirebaseAuth.getInstance();
        done=findViewById(R.id.done);

        auth.applyActionCode(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Drawable drawable=done.getDrawable();
                    if(drawable instanceof AnimatedVectorDrawableCompat){
                        animatedVectorDrawableCompat=(AnimatedVectorDrawableCompat) drawable;
                        animatedVectorDrawableCompat.start();
                    }else if(drawable instanceof  AnimatedVectorDrawable) {
                        animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
                        animatedVectorDrawable.start();
                    }
                }else{
                    Toast.makeText(VerifiedActivity.this, "Unable to verify account!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Drawable drawable=done.getDrawable();
//                if(drawable instanceof AnimatedVectorDrawableCompat){
//                    animatedVectorDrawableCompat=(AnimatedVectorDrawableCompat) drawable;
//                    animatedVectorDrawableCompat.start();
//                }else if(drawable instanceof  AnimatedVectorDrawable){
//                    animatedVectorDrawable=(AnimatedVectorDrawable) drawable;
//                    animatedVectorDrawable.start();
//                }
//            }
//        }, 5000);

    }
}