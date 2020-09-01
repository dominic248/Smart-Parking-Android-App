package com.dominicsilveira.parkingsystem.common;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.Adapter;
import com.dominicsilveira.parkingsystem.NormalUser.BookingPaymentActivity;
import com.dominicsilveira.parkingsystem.NormalUser.GPSMapActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.AddPositionActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DashboardActivity extends AppCompatActivity {
    MaterialCardView cv;
    TextView stockName;

    FirebaseAuth auth;
    FirebaseDatabase db;

    FusedLocationProviderClient client;
    LatLng globalLatLng;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    Map<Double, HashMap<String, ParkingArea>> parkingAreasList = new HashMap<Double, HashMap<String, ParkingArea>>();
    Map<Double, HashMap<String, ParkingArea>> treeMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        Button logout = findViewById(R.id.logoutBtn);
        Button getLocationBtn = findViewById(R.id.getLocationBtn);
        Button addLocationBtn = findViewById(R.id.addLocationBtn);
        Button payBtn = findViewById(R.id.payBtn);

        client= LocationServices.getFusedLocationProviderClient(DashboardActivity.this);
        getPreCurrentLocation();


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(DashboardActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DashboardActivity.this, LoginActivity.class));

            }
        });

        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, GPSMapActivity.class));
            }
        });

        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, AddPositionActivity.class));
            }
        });

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, BookingPaymentActivity.class));
            }
        });

        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.dashboard);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.dashboard:
                        return true;
                    case R.id.scan:
                        startActivity(new Intent(getApplicationContext(),
                                ScanActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(),
                                ProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }




    private void getPreCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(DashboardActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }else{
            ActivityCompat.requestPermissions(DashboardActivity.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},44);
            return;
        }
        Task<Location> task= client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if(location!=null){
                    db.getReference().child("ParkingAreas")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        ParkingArea parkingData = dataSnapshot.getValue(ParkingArea.class);
                                        HashMap<String, ParkingArea> parkingArea = new HashMap<String, ParkingArea>();
                                        parkingArea.put(dataSnapshot.getKey(),parkingData);
                                        parkingAreasList.put(distance(location.getLatitude(), location.getLongitude(), parkingData.latitude, parkingData.longitude, "K"),
                                                parkingArea);
                                        treeMap = new TreeMap<Double, HashMap<String, ParkingArea>>(parkingAreasList);
                                        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
                                        recyclerView.setHasFixedSize(true);
                                        layoutManager = new LinearLayoutManager(DashboardActivity.this);
                                        recyclerView.setLayoutManager(layoutManager);
                                        mAdapter = new Adapter(treeMap);
                                        recyclerView.setAdapter(mAdapter);
                                    }
                                    Log.d("GPS Map", String.valueOf(parkingAreasList));
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });


                }
            }
        });
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==44){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            }
        }
    }
}