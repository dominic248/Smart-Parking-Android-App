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
import com.dominicsilveira.parkingsystem.OwnerUser.AddPositionActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
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

public class UpiDetailsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase db;

    AppCompatEditText upiIdText,upiNameText;
    Button bt_submit;

    UpiInfo upiInfo;
    User userObj;
    String userID;

    AppConstants globalClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upi_details);

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        globalClass=(AppConstants)getApplicationContext();
        userID=auth.getCurrentUser().getUid();

        userObj=globalClass.getUserObj();

        upiIdText=findViewById(R.id.upiIdText);
        upiNameText=findViewById(R.id.upiNameText);
        bt_submit=findViewById(R.id.bt_submit);

        db.getReference().child("UpiInfo").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upiInfo=snapshot.getValue(UpiInfo.class);
                upiIdText.setText(upiInfo.upiId);
                upiIdText.setSelection(upiIdText.getText().length());
                upiNameText.setText(upiInfo.upiName);
                upiNameText.setSelection(upiNameText.getText().length());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String upiId = upiIdText.getText().toString();
                String upiName = upiNameText.getText().toString();

                upiInfo=new UpiInfo(upiId,upiName);
                db.getReference("UpiInfo")
                        .child(userID)
                        .setValue(upiInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpiDetailsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            Intent intent;
                            if(userObj.userType==2)
                                intent=new Intent(UpiDetailsActivity.this, MainOwnerActivity.class);
                            else
                                intent=new Intent(UpiDetailsActivity.this, MainNormalActivity.class);
                            intent.putExtra("FRAGMENT_NO", 2);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(UpiDetailsActivity.this, "Failed to add UPI details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}