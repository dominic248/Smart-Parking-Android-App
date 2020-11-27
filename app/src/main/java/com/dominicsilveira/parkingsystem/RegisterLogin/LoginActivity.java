package com.dominicsilveira.parkingsystem.RegisterLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.NormalUser.MainNormalActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
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
    private FirebaseDatabase db;
    ProgressDialog progressDialog;

    BasicUtils utils=new BasicUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponents();
        attachListeners();
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(LoginActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initComponents() {
        Intent in = getIntent();
        String prevEmail = in.getStringExtra("EMAIL");
        email=findViewById(R.id.emailField);
        password=findViewById(R.id.passwordField);
        loginBtn=findViewById(R.id.loginBtn);
        registerSwitchText=findViewById(R.id.registerSwitchText);
        forgotPasswordText=findViewById(R.id.forgotPasswordText);

        email.setText(prevEmail);
        email.setSelection(email.getText().length());

        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
    }

    private void attachListeners() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email=email.getText().toString();
                String txt_password=password.getText().toString();
                if(TextUtils.isEmpty(txt_email)){
                    Toast.makeText(LoginActivity.this,"Email can't be blank!",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(txt_password)){
                    Toast.makeText(LoginActivity.this,"Password can't be blank!",Toast.LENGTH_SHORT).show();
                }else if(utils.isNetworkAvailable(getApplication())){
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("Signing-in...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    loginUser(txt_email,txt_password);
                }else{
                    Toast.makeText(LoginActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerSwitchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passEmail=email.getText().toString();
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                if(!passEmail.isEmpty()){
                    intent.putExtra("EMAIL",passEmail);
                    startActivity(intent);
                }else{
                    startActivity(intent);
                }
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passEmail=email.getText().toString();
                Intent intent=new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                if(!passEmail.isEmpty()){
                    intent.putExtra("EMAIL",passEmail);
                    startActivity(intent);
                }else{
                    startActivity(intent);
                }
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }


    private void loginUser(String email, String password) {
        final AppConstants globalClass=(AppConstants)getApplicationContext();
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    if(!auth.getCurrentUser().isEmailVerified()){
//                        db.getReference("Users").child(auth.getCurrentUser().getUid()).child("isVerified").setValue(0);
                        Toast.makeText(LoginActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                        auth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Verification email sent to " + auth.getCurrentUser().getEmail()+"!", Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().signOut();
                                            try{ progressDialog.dismiss();
                                            }catch (Exception e){ e.printStackTrace();}
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Failed to send verification email!", Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().signOut();
                                            try{ progressDialog.dismiss();
                                            }catch (Exception e){ e.printStackTrace();}
                                        }
                                    }
                                });
                    }else{
                        db.getReference("Users").child(auth.getCurrentUser().getUid()).child("isVerified").setValue(1);
                        db.getReference("Users").child(auth.getCurrentUser().getUid()).child("email").setValue(auth.getCurrentUser().getEmail());
                        db.getReference().child("Users").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User userObj=snapshot.getValue(User.class);
                                globalClass.setUserObj(userObj);
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent;
                                if(userObj.userType==2)
                                    intent=new Intent(LoginActivity.this, MainOwnerActivity.class);
                                else
                                    intent=new Intent(LoginActivity.this, MainNormalActivity.class);
                                intent.putExtra("FRAGMENT_NO", 0);
                                try{ progressDialog.dismiss();
                                }catch (Exception e){ e.printStackTrace();}
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                try{ progressDialog.dismiss();
                                }catch (Exception e){ e.printStackTrace();}
                            }
                        });
                    }
                }else{
                    try{ progressDialog.dismiss();
                    }catch (Exception e){ e.printStackTrace();}
                    try {
                        throw task.getException(); // if user enters wrong email.
                    }catch (FirebaseAuthInvalidCredentialsException invalid) {
                        Toast.makeText(LoginActivity.this, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                        Log.d(String.valueOf(LoginActivity.this.getClass()), "onComplete: Invalid Credentials");
                    } catch (Exception e) {
                        Log.d(String.valueOf(LoginActivity.this.getClass()), "onComplete: " + e.getMessage());
                        e.printStackTrace();
                        // TODO: some work
                    }
                }
            }
        });
    }
}