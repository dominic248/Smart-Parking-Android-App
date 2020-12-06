package com.dominicsilveira.parkingsystem.RegisterLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Intent prevIntent;
    String token;
    AppCompatEditText newPasswordField,confirmPasswordField;
    Button resetPasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initComponents();
        attachListeners();
    }

    private void initComponents() {
        prevIntent=getIntent();
        token=prevIntent.getStringExtra("TOKEN");

        auth=FirebaseAuth.getInstance();

        resetPasswordBtn=findViewById(R.id.resetPasswordBtn);
        newPasswordField=findViewById(R.id.newPasswordField);
        confirmPasswordField=findViewById(R.id.confirmPasswordField);
    }

    private void attachListeners() {
        auth.verifyPasswordResetCode(token).addOnCompleteListener(this, new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(ResetPasswordActivity.this, "Invalid Password Reset Token!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPassword=newPasswordField.getText().toString();
                String confirmPassword=confirmPasswordField.getText().toString();

                if(newPassword.isEmpty() || confirmPassword.isEmpty()){
                    Toast.makeText(ResetPasswordActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                }else{
                    if(newPassword.equals(confirmPassword)){
                        resetPassword(newPassword);
                    }else{
                        Toast.makeText(ResetPasswordActivity.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void resetPassword(String newPassword) {
        auth.confirmPasswordReset(token, newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // TODO: Display a link back to the app, or sign-in the user directly
                // if the page belongs to the same domain as the app:
                // auth.signInWithEmailAndPassword(accountEmail, newPassword);
                // TODO: If a continue URL is available, display a button which on
                // click redirects the user back to the app via continueUrl with
                // additional state determined from that URL's parameters.
                if(task.isSuccessful()){
                    Toast.makeText(ResetPasswordActivity.this, "Password Reset Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                    finish();
                }else{
                    Toast.makeText(ResetPasswordActivity.this, "Password Reset Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}