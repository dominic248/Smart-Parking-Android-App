package com.dominicsilveira.parkingsystem.OwnerUser;

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

import com.dominicsilveira.parkingsystem.common.DashboardActivity;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterAreaActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase db;

    private EditText areaNameText,upiIdText,upiNameText,amountText,slotsText;
    private TextView latitudeText,longitudeText;
    Button saveBtn,cancelBtn;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_area);

        Bundle bundle = getIntent().getExtras();
        final double latitude = bundle.getDouble("latitude");
        final double longitude = bundle.getDouble("longitude");

        final TextView nameText=findViewById(R.id.nameText);
        areaNameText=findViewById(R.id.areaNameText);
        latitudeText=findViewById(R.id.latitudeText);
        longitudeText=findViewById(R.id.longitudeText);
        upiIdText=findViewById(R.id.upiIdText);
        upiNameText=findViewById(R.id.upiNameText);
        amountText=findViewById(R.id.amountText);
        slotsText=findViewById(R.id.slotsText);

        saveBtn=findViewById(R.id.saveBtn);

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        Log.e("Map",String.valueOf(latitude)+" : "+String.valueOf(longitude));
        latitudeText.setText(String.valueOf(longitude));
        longitudeText.setText(String.valueOf(longitude));

        db.getReference().child("Users")
                .child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            name= (String) snapshot.child("name").getValue();
                            Log.e("Name",name);
                            nameText.setText(name);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String areaName = areaNameText.getText().toString();
                    String upiId = upiIdText.getText().toString();
                    String upiName = upiNameText.getText().toString();
                    String amount = amountText.getText().toString();
                    String slots = slotsText.getText().toString();
                    ParkingArea parkingArea = new ParkingArea(areaName,latitude,longitude,upiId,upiName,amount,slots);
                    String key=db.getReference("ParkingAreas").push().getKey();
                    db.getReference("ParkingAreas")
                            .child(key)
                            .setValue(parkingArea).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterAreaActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterAreaActivity.this, DashboardActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterAreaActivity.this, "Failed to add extra details", Toast.LENGTH_SHORT).show();
                                    }
                                }
                    });
                }
            });


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}