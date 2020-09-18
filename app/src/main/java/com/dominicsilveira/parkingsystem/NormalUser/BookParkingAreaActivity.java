package com.dominicsilveira.parkingsystem.NormalUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.utils.NumberPlateAdapter;
import com.dominicsilveira.parkingsystem.utils.SimpleToDeleteCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BookParkingAreaActivity extends AppCompatActivity {
    Spinner numberPlateSpinner;
    FirebaseAuth auth;
    FirebaseDatabase db;

    List<String> numerPlateKeys = new ArrayList<String>();
    List<String> numerPlateList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_parking_area);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        numberPlateSpinner = findViewById(R.id.selectPlateNumber);
        addItemsOnSpinner();
        addListenerOnSpinnerItemSelection();

        Bundle bundle = getIntent().getExtras();
        String UUID=bundle.getString("UUID");
        final ParkingArea parkingArea = (ParkingArea) getIntent().getSerializableExtra("ParkingArea");
        Log.e("BookParkingAreaActivity",parkingArea.name+" "+UUID);

    }
    public void addItemsOnSpinner() {
        numerPlateKeys.add("0");
        numerPlateList.add("Select a vehicle");
//        list.add("list 1");
//        list.add("list 2");
//        list.add("list 3");
        db.getReference().child("NumberPlates").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NumberPlate numberPlate = dataSnapshot.getValue(NumberPlate.class);
                            numerPlateKeys.add(dataSnapshot.getKey());
                            numerPlateList.add(numberPlate.numberPlate);
                        }
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(BookParkingAreaActivity.this,
                                android.R.layout.simple_spinner_item, numerPlateList);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        numberPlateSpinner.setAdapter(dataAdapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

    }

    public void addListenerOnSpinnerItemSelection() {
        numberPlateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position!=0)
                Toast.makeText(BookParkingAreaActivity.this, String.valueOf(numberPlateSpinner.getSelectedItem())+String.valueOf(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }
}