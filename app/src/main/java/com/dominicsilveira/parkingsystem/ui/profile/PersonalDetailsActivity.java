package com.dominicsilveira.parkingsystem.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.NormalUser.MainNormalActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.UpiInfo;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PersonalDetailsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase db;

    AppCompatEditText nameText,phoneText,emailText;
    Button bt_submit;

    User userObj;
    String userID;

    AppConstants globalClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        globalClass=(AppConstants)getApplicationContext();
        userID=auth.getCurrentUser().getUid();

        userObj=globalClass.getUserObj();

        nameText=findViewById(R.id.nameText);
        phoneText=findViewById(R.id.phoneText);
        emailText=findViewById(R.id.emailText);
        bt_submit=findViewById(R.id.bt_submit);

        nameText.setText(userObj.name);
        nameText.setSelection(nameText.getText().length());
        phoneText.setText(userObj.contact_no);
        phoneText.setSelection(phoneText.getText().length());
        emailText.setText(userObj.email);
        emailText.setSelection(emailText.getText().length());

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userObj.name = nameText.getText().toString();
                userObj.contact_no = phoneText.getText().toString();
                userObj.email = emailText.getText().toString();

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
        });

    }
}