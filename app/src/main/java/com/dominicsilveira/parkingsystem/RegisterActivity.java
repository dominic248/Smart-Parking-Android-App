package com.dominicsilveira.parkingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private Button registerBtn;
    private Button loginSwitchBtn;

    private FirebaseAuth auth;
    private RadioGroup userTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email=findViewById(R.id.emailField);
        password=findViewById(R.id.passwordField);
        registerBtn=findViewById(R.id.registerBtn);
        loginSwitchBtn=findViewById(R.id.loginSwitchBtn);

        userTypes=findViewById(R.id.userTypes);

        auth=FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String txt_email=email.getText().toString();
                String txt_password=password.getText().toString();
                int checkedId=userTypes.getCheckedRadioButtonId();
                int userType=findRadioButton(checkedId);

                if(TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                    Toast.makeText(RegisterActivity.this,"Empty",Toast.LENGTH_SHORT).show();
                }else if(txt_password.length()<6){
                    Toast.makeText(RegisterActivity.this,"Password too short",Toast.LENGTH_SHORT).show();
                }else{
                    registerUser(txt_email,txt_password,userType);
                }
            }
        });
        loginSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });
    }

    private int findRadioButton(int checkedId) {
        int user;
        switch(checkedId){
            case R.id.normalType:
                user= 3;
                break;
            case R.id.ownerType:
                user=2;
                break;
            default:
                user=2;
        }
        return user;
    }

    private void registerUser(String email, String password, final int userType) {
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            
                            User user=new User(userType);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(auth.getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this,"Success",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                                        finish();
                                    }else{
                                        Toast.makeText(RegisterActivity.this,"Failed to add extra details",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(RegisterActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}