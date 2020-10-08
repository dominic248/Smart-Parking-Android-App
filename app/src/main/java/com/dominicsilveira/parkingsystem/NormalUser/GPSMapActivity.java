package com.dominicsilveira.parkingsystem.NormalUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;

import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class GPSMapActivity extends AppCompatActivity implements OnMapReadyCallback{
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;

    GoogleMap gMap;
    LatLng globalLatLng;

    private FirebaseAuth auth;
    private FirebaseDatabase db;


    LatLng globalLatLngIntent=null;
    MarkerOptions optionsIntent;


    HashMap<String, ParkingArea> parkingAreasList = new HashMap<String,ParkingArea>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable mToastRunnable = new Runnable() {
        @Override
        public void run() {
            getCurrentLocation();
            Log.i("GPS","Getting new Location after every 5 seconds");
            mHandler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mToastRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mToastRunnable, 5000);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_g_p_s_map);

        Intent intent=getIntent();
        String nameIntent=intent.getStringExtra("LOCATION_NAME");
        double latitudeIntent=intent.getDoubleExtra("LOCATION_LATITUDE",-1);
        double longitudeIntent= intent.getDoubleExtra("LOCATION_LONGITUDE",-1);

        if(latitudeIntent != -1){
             globalLatLngIntent=new LatLng(latitudeIntent,longitudeIntent);
             optionsIntent=new MarkerOptions().position(globalLatLngIntent)
                    .title(nameIntent);
        }

        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();

        supportMapFragment=(SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);
        client=LocationServices.getFusedLocationProviderClient(GPSMapActivity.this);

        Button getLocationBtn=findViewById(R.id.getLocationBtn);

        FirebaseDatabase.getInstance().getReference().child("ParkingAreas")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
//                            parkingAreasList.add(parkingArea);
                            parkingAreasList.put(dataSnapshot.getKey(),parkingArea);
                            Log.d("GPS Map",parkingArea.name);
                        }
                        Log.d("GPS Map", String.valueOf(parkingAreasList));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        getPreCurrentLocation();
    }

    private void getPreCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(GPSMapActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            mToastRunnable.run(); //To avoid double looping because of onRequestPermissionsResult
        }else{
            ActivityCompat.requestPermissions(GPSMapActivity.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},AppConstants.LOCATION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},AppConstants.LOCATION_REQUEST_CODE);
        }
        Task<Location> task= client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if(location!=null){
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            gMap=googleMap;
                            gMap.clear();
                            MarkerOptions options;
                            if(globalLatLng==null){
                                globalLatLng=new LatLng(location.getLatitude(),
                                        location.getLongitude());
                                options=new MarkerOptions().position(globalLatLng)
                                        .title("I am here");
                                if(globalLatLngIntent != null)
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(globalLatLngIntent,30));
                                else
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(globalLatLng,30));
                            }else{
                                globalLatLng=new LatLng(location.getLatitude(),
                                        location.getLongitude());
                                options=new MarkerOptions().position(globalLatLng)
                                        .title("I am here");
                            }
                            googleMap.addMarker(options);

                            Iterator hmIterator = parkingAreasList.entrySet().iterator();
                            while (hmIterator.hasNext()) {
                                Map.Entry mapElement = (Map.Entry)hmIterator.next();
                                ParkingArea parking = (ParkingArea)mapElement.getValue();
                                LatLng latLngParking=new LatLng(parking.latitude,
                                        parking.longitude);
                                MarkerOptions option=new MarkerOptions().position(latLngParking)
                                        .title(mapElement.getKey().toString())
                                        .snippet(parking.name);
                                gMap.addMarker(option);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode== AppConstants.LOCATION_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                mToastRunnable.run();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            public void onInfoWindowClick(final Marker marker) {
                String[] items={"Book Place","More Info"};
                AlertDialog.Builder itemDilog = new AlertDialog.Builder(GPSMapActivity.this);
                itemDilog.setTitle("");
                itemDilog.setCancelable(true);
                itemDilog.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case 0:{
                                Log.d("Funct1","Google maps");
                                String UUID = marker.getTitle();
                                ParkingArea val = (ParkingArea)parkingAreasList.get(UUID);
                                Intent intent = new Intent(GPSMapActivity.this, BookParkingAreaActivity.class);
                                intent.putExtra("UUID", UUID);
                                intent.putExtra("ParkingArea", val);
                                startActivity(intent);
                                Log.d("values", String.valueOf(2)+" "+UUID);
                            }break;
                            case 1:{
                                Log.d("Funct2","Google maps");
                            }break;
                        }

                    }
                });
                itemDilog.show();

            }
        });

//        //Method 2 - No Opts
//        public boolean doubleBackToExitPressedOnce = false;
//        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                if (doubleBackToExitPressedOnce) {
//                    String UUID = marker.getTitle();
//                    ParkingArea val = (ParkingArea)parkingAreasList.get(UUID);
//                    doubleBackToExitPressedOnce = false;
//                    Intent intent = new Intent(GPSMapActivity.this, BookParkingAreaActivity.class);
//                    intent.putExtra("UUID", UUID);
//                    intent.putExtra("ParkingArea", val);
//                    startActivity(intent);
//                } else {
//                    doubleBackToExitPressedOnce = true;
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            doubleBackToExitPressedOnce = false;
//                        }
//                    }, 500);
//                    Log.d("double", String.valueOf(1));
//                }
//                return false;
//            }
//        });

    }
}