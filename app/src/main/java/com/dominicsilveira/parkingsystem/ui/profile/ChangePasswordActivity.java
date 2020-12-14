package com.dominicsilveira.parkingsystem.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    AppCompatEditText oldPasswordText,newPasswordText,confirmPasswordText;
    Button bt_submit;
    TextInputLayout oldPasswordLayout,newPasswordLayout,confirmPasswordLayout;
    FirebaseUser user;
    String email;
    BasicUtils utils=new BasicUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initComponents();
        attachListeners();
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(ChangePasswordActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
    }

    private void initComponents() {
        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        oldPasswordText=findViewById(R.id.oldPasswordText);
        newPasswordText=findViewById(R.id.newPasswordText);
        confirmPasswordText=findViewById(R.id.confirmPasswordText);
        oldPasswordLayout=findViewById(R.id.oldPasswordLayout);
        newPasswordLayout=findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout=findViewById(R.id.confirmPasswordLayout);
        bt_submit=findViewById(R.id.bt_submit);
        user = FirebaseAuth.getInstance().getCurrentUser();
        email=user.getEmail();
    }

    private void attachListeners() {
        confirmPasswordText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                String confirmPasswordString = s.toString();
                if(confirmPasswordString.equals(newPasswordText.getText().toString())){
                    newPasswordLayout.setError("");
                    confirmPasswordLayout.setError("");
                }else{
                    newPasswordLayout.setError("Passwords don't match");
                    confirmPasswordLayout.setError("Passwords don't match");
                }
            }
        });

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newPass=newPasswordText.getText().toString();
                String oldPass=oldPasswordText.getText().toString();
                String confirmPass=confirmPasswordText.getText().toString();
                if(newPass.isEmpty() || oldPass.isEmpty() || confirmPass.isEmpty()){
                    Toast.makeText(ChangePasswordActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                }else if(newPass.equals(confirmPass)){
                    newPasswordLayout.setError("");
                    confirmPasswordLayout.setError("");
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(email, oldPass);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ChangePasswordActivity.this, "Password changed", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
                                                } else {
                                                    Toast.makeText(ChangePasswordActivity.this, "Password not changed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, "Auth Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(ChangePasswordActivity.this, "New Passwords don't match", Toast.LENGTH_SHORT).show();
                    newPasswordLayout.setError("Passwords don't match");
                    confirmPasswordLayout.setError("Passwords don't match");
                }
            }
        });
    }
}