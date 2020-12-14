package com.dominicsilveira.parkingsystem.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
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

    BasicUtils utils=new BasicUtils();

    AppConstants globalClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upi_details);

        initComponents();
        attachListeners();
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(UpiDetailsActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
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
        getSupportActionBar().setTitle("UPI Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        globalClass=(AppConstants)getApplicationContext();
        userID=auth.getCurrentUser().getUid();

        userObj=globalClass.getUserObj();

        upiIdText=findViewById(R.id.upiIdText);
        upiNameText=findViewById(R.id.upiNameText);
        bt_submit=findViewById(R.id.bt_submit);
    }

    private void attachListeners() {
        db.getReference().child("UpiInfo").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upiInfo=snapshot.getValue(UpiInfo.class);
                if(upiInfo!=null){
                    upiIdText.setText(upiInfo.upiId);
                    upiIdText.setSelection(upiIdText.getText().length());
                    upiNameText.setText(upiInfo.upiName);
                    upiNameText.setSelection(upiNameText.getText().length());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String upiId = upiIdText.getText().toString();
                String upiName = upiNameText.getText().toString();

                if(upiId.isEmpty() || upiName.isEmpty()){
                    Toast.makeText(UpiDetailsActivity.this, "Please fill all details!", Toast.LENGTH_SHORT).show();
                }else{
                    upiInfo=new UpiInfo(upiId,upiName);
                    db.getReference("UpiInfo")
                            .child(userID)
                            .setValue(upiInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(UpiDetailsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                finish();
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
                            } else {
                                Toast.makeText(UpiDetailsActivity.this, "Failed to add UPI details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

}