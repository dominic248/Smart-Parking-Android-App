package com.dominicsilveira.parkingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class GPSMapActivity extends AppCompatActivity implements OnMapReadyCallback{
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;

    GoogleMap gMap;

    private FirebaseAuth auth;
    private FirebaseDatabase db;

    public boolean doubleBackToExitPressedOnce = false;

    HashMap<String,ParkingArea> parkingAreasList = new HashMap<String,ParkingArea>();
//    ArrayList<ParkingArea> parkingAreasList = new ArrayList<ParkingArea>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_g_p_s_map);

        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();

        supportMapFragment=(SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);

        Button getLocationBtn=findViewById(R.id.getLocationBtn);

        FirebaseDatabase.getInstance().getReference().child("ParkingAreas")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
//                            parkingAreasList.add(parkingArea);
                            parkingAreasList.put(dataSnapshot.getKey(),parkingArea);
                            Log.e("GPS Map",parkingArea.name);
                        }
                        Log.e("GPS Map", String.valueOf(parkingAreasList));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        getLocationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                client=LocationServices.getFusedLocationProviderClient(GPSMapActivity.this);
                if(ActivityCompat.checkSelfPermission(GPSMapActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    getCurrentLocation();
                }else{
                    ActivityCompat.requestPermissions(GPSMapActivity.this,new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},44);
                }
            }
        });
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
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            LatLng latLng=new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            MarkerOptions options=new MarkerOptions().position(latLng)
                                    .title("I am here");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,30));
                            googleMap.addMarker(options);
                            Iterator hmIterator = parkingAreasList.entrySet().iterator();
                            while (hmIterator.hasNext()) {
                                Map.Entry mapElement = (Map.Entry)hmIterator.next();
                                ParkingArea parking = (ParkingArea)mapElement.getValue();
                                LatLng latLngParking=new LatLng(parking.latitude,
                                        parking.longitude);
                                options.position(latLngParking);
                                options.title(mapElement.getKey().toString());
                                options.snippet(parking.name);
                                googleMap.addMarker(options);

                            }
//                            for (ParkingArea parking : parkingAreasList) {
//                                LatLng latLngParking=new LatLng(parking.latitude,
//                                        parking.longitude);
//                                options.position(latLngParking);
//                                options.title(parking.name);
//                                options.snippet(parking.name);
//                                googleMap.addMarker(options);
//                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==44){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (doubleBackToExitPressedOnce) {
                    String UUID = marker.getTitle();
                    ParkingArea val = (ParkingArea)parkingAreasList.get(UUID);
                    doubleBackToExitPressedOnce = false;
                    Intent intent = new Intent(GPSMapActivity.this, BookParkingAreaActivity.class);
                    intent.putExtra("UUID", UUID);
                    intent.putExtra("ParkingArea", val);
                    startActivity(intent);
                    Log.e("double", String.valueOf(2));
                    Log.e("title", UUID);
                } else {
                    doubleBackToExitPressedOnce = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }, 500);
                    Log.e("double", String.valueOf(1));
                }

                return false;
            }
        });

    }
}