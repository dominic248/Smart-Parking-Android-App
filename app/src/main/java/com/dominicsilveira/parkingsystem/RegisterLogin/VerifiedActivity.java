package com.dominicsilveira.parkingsystem.RegisterLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
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
    ConstraintLayout verifyTick;
    FirebaseAuth auth;
    Intent prevIntent;
    String token;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verified);

        initComponents();
        attachListeners();

    }

    private void initComponents() {
        prevIntent=getIntent();
        token=prevIntent.getStringExtra("TOKEN");
        auth=FirebaseAuth.getInstance();
        verifyTick=findViewById(R.id.verifyTick);
        done=verifyTick.findViewById(R.id.done);
        loginBtn=findViewById(R.id.loginBtn);
    }

    private void attachListeners() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VerifiedActivity.this,LoginActivity.class));
                finish();
            }
        });

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
    }
}