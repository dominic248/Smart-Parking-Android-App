package com.dominicsilveira.parkingsystem.RegisterLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.AppConstants;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.common.MainNormalActivity;
import com.dominicsilveira.parkingsystem.common.MainOwnerActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button loginBtn;
    private TextView forgotPasswordText,registerSwitchText;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        email=findViewById(R.id.emailField);
        password=findViewById(R.id.passwordField);
        loginBtn=findViewById(R.id.loginBtn);
        registerSwitchText=findViewById(R.id.registerSwitchText);
        forgotPasswordText=findViewById(R.id.forgotPasswordText);

        auth=FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email=email.getText().toString();
                String txt_password=password.getText().toString();
                loginUser(txt_email,txt_password);
            }
        });

        registerSwitchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                final AppConstants globalClass=(AppConstants)getApplicationContext();
                FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid()).child("userType").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int val=snapshot.getValue(int.class);
                        Log.e("userTyp",String.valueOf(val));
                        globalClass.setUserType(val);
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent;
                        if(val==2)
                            intent=new Intent(LoginActivity.this, MainOwnerActivity.class);
                        else
                            intent=new Intent(LoginActivity.this, MainNormalActivity.class);
                        intent.putExtra("FRAGMENT_NO", 0);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }
}