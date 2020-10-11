package com.dominicsilveira.parkingsystem.RegisterLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.NormalUser.MainNormalActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText email,name,contactNo;
    private EditText password;
    private Button registerBtn;
    private TextView loginSwitchText;

    private FirebaseAuth auth;
    private FirebaseDatabase db;

    private RadioGroup userTypes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name=findViewById(R.id.nameField);
        contactNo=findViewById(R.id.contactNoField);
        email=findViewById(R.id.emailField);
        password=findViewById(R.id.passwordField);
        registerBtn=findViewById(R.id.registerBtn);
        loginSwitchText=findViewById(R.id.loginSwitchText);

        userTypes=findViewById(R.id.userTypes);

        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String txt_email=email.getText().toString();
                String txt_password=password.getText().toString();
                String txt_name=name.getText().toString();
                String txt_contact_no=contactNo.getText().toString();
                int checkedId=userTypes.getCheckedRadioButtonId();
                int userType=findRadioButton(checkedId);

                if(TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                    Toast.makeText(RegisterActivity.this,"Empty",Toast.LENGTH_SHORT).show();
                }else if(txt_password.length()<6){
                    Toast.makeText(RegisterActivity.this,"Password too short",Toast.LENGTH_SHORT).show();
                }else{
                    registerUser(txt_email,txt_password,txt_name,txt_contact_no,userType);
                }
            }
        });
        loginSwitchText.setOnClickListener(new View.OnClickListener() {
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

    private void registerUser(final String email, String password, final String name, final String contact_no, final int userType) {
        final AppConstants globalClass=(AppConstants)getApplicationContext();
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            final User userObj=new User(name,email,contact_no,userType);
                            globalClass.setUserObj(userObj);
                            db.getReference("Users")
                                    .child(auth.getCurrentUser().getUid())
                                    .setValue(userObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this,"Success",Toast.LENGTH_SHORT).show();
                                        final FirebaseUser user = auth.getCurrentUser();
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(RegisterActivity.this, "Verification email sent to " + user.getEmail()+"!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "Failed to send verification email!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                        Intent intent;
                                        if(userObj.userType==2){
                                            intent = new Intent(RegisterActivity.this, MainOwnerActivity.class);
                                        }else{
                                            intent = new Intent(RegisterActivity.this, MainNormalActivity.class);
                                        }
                                        intent.putExtra("FRAGMENT_NO", 0);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Toast.makeText(RegisterActivity.this,"Failed to add User Details!",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            try{
                                throw task.getException();
                            }// if user enters wrong email.
                            catch (FirebaseAuthWeakPasswordException weakPassword) {
                                Toast.makeText(RegisterActivity.this, "Too Weak Password!", Toast.LENGTH_SHORT).show();
                                Log.d(String.valueOf(RegisterActivity.this.getClass()), "onComplete: weak_password");
                            }// if user enters wrong password.
                            catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                                Toast.makeText(RegisterActivity.this, "Malformed_email!", Toast.LENGTH_SHORT).show();
                                Log.d(String.valueOf(RegisterActivity.this.getClass()), "onComplete: malformed_email");
                            } catch (FirebaseAuthUserCollisionException existEmail) {
                                Toast.makeText(RegisterActivity.this, "Email already exists!", Toast.LENGTH_SHORT).show();
                                Log.d(String.valueOf(RegisterActivity.this.getClass()), "onComplete: exist_email");
                            } catch (Exception e) {
                                Log.d(String.valueOf(RegisterActivity.this.getClass()), "onComplete: " + e.getMessage());
                                // TODO: some work
                            }
                        }
                    }
                });
    }
}