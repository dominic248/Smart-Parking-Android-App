package com.dominicsilveira.parkingsystem.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.NormalUser.MainNormalActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.RegisterActivity;
import com.dominicsilveira.parkingsystem.classes.UpiInfo;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PersonalDetailsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase db;

    AppCompatEditText nameText,phoneText,emailText,newEmailText,currentPasswordText;
    Button bt_submit,bt_submit_email;

    User userObj;
    String userID;

    Utils utils=new Utils();

    AppConstants globalClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);

        initComponents();
        attachListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initComponents() {
        getSupportActionBar().setTitle("Personal Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        globalClass=(AppConstants)getApplicationContext();
        userID=auth.getCurrentUser().getUid();

        userObj=globalClass.getUserObj();

        nameText=findViewById(R.id.nameText);
        phoneText=findViewById(R.id.phoneText);
        emailText=findViewById(R.id.emailText);
        bt_submit=findViewById(R.id.bt_submit);
        newEmailText=findViewById(R.id.newEmailText);
        currentPasswordText=findViewById(R.id.currentPasswordText);
        bt_submit_email=findViewById(R.id.bt_submit_email);

        nameText.setText(userObj.name);
        nameText.setSelection(nameText.getText().length());
        phoneText.setText(userObj.contact_no);
        phoneText.setSelection(phoneText.getText().length());
        emailText.setText(userObj.email);
        emailText.setSelection(emailText.getText().length());
    }

    private void attachListeners() {
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userObj.name = nameText.getText().toString();
                userObj.contact_no = phoneText.getText().toString();
                if(userObj.name.isEmpty() || userObj.contact_no.isEmpty()){
                    Toast.makeText(PersonalDetailsActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                }else if(!utils.isNameValid(userObj.name)){
                    Toast.makeText(PersonalDetailsActivity.this,"Name is Invalid!",Toast.LENGTH_SHORT).show();
                }else if(!utils.isPhoneNoValid(userObj.contact_no)){
                    Toast.makeText(PersonalDetailsActivity.this,"Phone Number is Invalid!",Toast.LENGTH_SHORT).show();
                }else{
                    db.getReference("Users")
                            .child(userID)
                            .setValue(userObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PersonalDetailsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                Intent intent;
                                if(userObj.userType==2)
                                    intent=new Intent(PersonalDetailsActivity.this, MainOwnerActivity.class);
                                else
                                    intent=new Intent(PersonalDetailsActivity.this, MainNormalActivity.class);
                                intent.putExtra("FRAGMENT_NO", 2);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(PersonalDetailsActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


        bt_submit_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newEmail=newEmailText.getText().toString();
                if(newEmail.isEmpty() || currentPasswordText.getText().toString().isEmpty() || emailText.getText().toString().isEmpty()){
                    Toast.makeText(PersonalDetailsActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                }else{
                    final FirebaseUser user = auth.getCurrentUser();
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(emailText.getText().toString(), currentPasswordText.getText().toString()); // Current Login Credentials \\
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(String.valueOf(PersonalDetailsActivity.this.getClass()), "User re-authenticated.");
                                    user.updateEmail(newEmail)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
//                                                    db.getReference("Users").child(userID).child("email").setValue(userObj.email);
                                                        Intent intent;
                                                        userObj.email = newEmail;
                                                        if(userObj.userType==2)
                                                            intent=new Intent(PersonalDetailsActivity.this, MainOwnerActivity.class);
                                                        else
                                                            intent=new Intent(PersonalDetailsActivity.this, MainNormalActivity.class);
                                                        intent.putExtra("FRAGMENT_NO", 2);
                                                        startActivity(intent);
                                                        finish();
                                                        Log.d(String.valueOf(PersonalDetailsActivity.this.getClass()), "User email address updated.");
                                                    }
                                                }
                                            });
                                }
                            });
                }
            }
        });
    }
}