package com.dominicsilveira.parkingsystem.RegisterLogin;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeResult;
import com.google.firebase.auth.FirebaseAuth;

public class RecoverAccountActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Intent prevIntent;
    String token,recoveredAccountMsg="Account Recovered!",failedAccountMsg="Failed to Recover Account!";
    String restoredEmail=null;

    ConstraintLayout verifyTick;
    Button loginBtn;
    ImageView done,circle;
    TextView forgotPasswordText,message;
    AnimatedVectorDrawable animatedVectorDrawable;
    AnimatedVectorDrawableCompat animatedVectorDrawableCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_account);

        initComponents();
        attachListeners();
    }

    private void initComponents() {
        prevIntent=getIntent();
        token=prevIntent.getStringExtra("TOKEN");
        
        auth=FirebaseAuth.getInstance();

        verifyTick=findViewById(R.id.verifyTick);
        done=verifyTick.findViewById(R.id.done);
        circle=verifyTick.findViewById(R.id.circle);
        loginBtn=findViewById(R.id.loginBtn);
        message=findViewById(R.id.message);
        forgotPasswordText=findViewById(R.id.forgotPasswordText);
    }

    private void attachListeners() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RecoverAccountActivity.this,LoginActivity.class));
                finish();
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RecoverAccountActivity.this,ForgotPasswordActivity.class));
            }
        });

        // Localize the UI to the selected language as determined by the lang parameter.
        // Confirm the action code is valid.
        auth.checkActionCode(token).addOnCompleteListener(new OnCompleteListener<ActionCodeResult>() {
            @Override
            public void onComplete(@NonNull Task<ActionCodeResult> task) {
                if(task.isSuccessful()) {
                    message.setText(recoveredAccountMsg);
                    forgotPasswordText.setVisibility(View.VISIBLE);
                    done.setImageDrawable(ContextCompat.getDrawable(RecoverAccountActivity.this, R.drawable.tick_anim));
                    done.setColorFilter(Color.WHITE);
                    circle.setColorFilter(Color.parseColor("#4CAF50"));
                    Drawable drawable=done.getDrawable();
                    if(drawable instanceof AnimatedVectorDrawableCompat){
                        animatedVectorDrawableCompat=(AnimatedVectorDrawableCompat) drawable;
                        animatedVectorDrawableCompat.start();
                    }else if(drawable instanceof AnimatedVectorDrawable) {
                        animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
                        animatedVectorDrawable.start();
                    }
                    restoredEmail = task.getResult().getInfo().getEmail();
                    Log.i("AccountRecovered",restoredEmail);
                    auth.applyActionCode(token);
                } else {
                    message.setText(failedAccountMsg);
                    forgotPasswordText.setVisibility(View.GONE);
                    done.setImageDrawable(ContextCompat.getDrawable(RecoverAccountActivity.this, R.drawable.ic_wrong_icon));
                    done.setColorFilter(Color.WHITE);
                    circle.setColorFilter(Color.RED);
                    try{ Log.i("AccountRecoveredNot",task.getResult().getInfo().getEmail()); }
                    catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }
}