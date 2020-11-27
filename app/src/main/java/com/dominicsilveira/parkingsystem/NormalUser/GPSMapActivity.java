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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
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
import com.google.firebase.database.snapshot.BooleanNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class GPSMapActivity extends AppCompatActivity implements OnMapReadyCallback{
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    Button getLocationBtn;

    GoogleMap gMap;
    LatLng globalLatLng;
    BasicUtils utils=new BasicUtils();

    private FirebaseAuth auth;
    private FirebaseDatabase db;

    LatLng globalLatLngIntent=null;
    MarkerOptions optionsIntent;

    HashMap<String, ParkingArea> parkingAreasList = new HashMap<String,ParkingArea>();

    AppConstants globalClass;

    String nameIntent;
    double latitudeIntent,longitudeIntent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_g_p_s_map);

        initComponents();
        attachListeners();

        getPreCurrentLocation();
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(GPSMapActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
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
        globalClass=(AppConstants)getApplicationContext();

        getSupportActionBar().setTitle("GPS Maps");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();

        Intent intent=getIntent();
        nameIntent=intent.getStringExtra("LOCATION_NAME");
        latitudeIntent=intent.getDoubleExtra("LOCATION_LATITUDE",-1);
        longitudeIntent= intent.getDoubleExtra("LOCATION_LONGITUDE",-1);

        if(latitudeIntent != -1){
            globalLatLngIntent=new LatLng(latitudeIntent,longitudeIntent);
            optionsIntent=new MarkerOptions().position(globalLatLngIntent)
                    .title(nameIntent);
        }

        supportMapFragment=(SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);
        client=LocationServices.getFusedLocationProviderClient(GPSMapActivity.this);

        getLocationBtn=findViewById(R.id.getLocationBtn);
    }

    private void attachListeners() {
        db.getReference().child("ParkingAreas")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
                            parkingAreasList.put(dataSnapshot.getKey(),parkingArea);
                            Log.d(GPSMapActivity.this.getComponentName().getClassName(), "Fetch Parking Area: "+parkingArea.name);
                        }
                        attachMarkerOnMap();
                        Log.d(GPSMapActivity.this.getComponentName().getClassName(), "Fetch Parking Area list: "+String.valueOf(parkingAreasList));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        gMap = googleMap;
                        gMap.clear();
                        getPreCurrentLocation();
                        attachMarkerOnMap();
                    }
                });
            }
        });
    }

    private void attachMarkerOnMap() {
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap=googleMap;
                if(globalLatLngIntent != null){
                    MarkerOptions options=new MarkerOptions().position(globalLatLngIntent)
                                .title(nameIntent);
                    gMap.addMarker(options);
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(globalLatLngIntent,30));
                }else{
                    for (Map.Entry<String, ParkingArea> stringParkingAreaEntry : parkingAreasList.entrySet()) {
                        Map.Entry mapElement = (Map.Entry) stringParkingAreaEntry;
                        ParkingArea parking = (ParkingArea) mapElement.getValue();
                        Log.e(GPSMapActivity.this.getComponentName().getClassName(), "Add Marker: "+parking.name);
                        LatLng latLngParking = new LatLng(parking.latitude,
                                parking.longitude);
                        MarkerOptions option = new MarkerOptions().position(latLngParking)
                                .title(mapElement.getKey().toString())
                                .snippet(parking.name);
                        gMap.addMarker(option);
                    }
                }
            }
        });
    }

    private void getPreCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(GPSMapActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            getCurrentLocation(true);
        }else{
            ActivityCompat.requestPermissions(GPSMapActivity.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},AppConstants.LOCATION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation(final Boolean zoom) {
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
                            globalLatLng=new LatLng(location.getLatitude(),
                                        location.getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(globalLatLng).title("I am here"));
                            if(zoom){
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(globalLatLng,30));
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
                getCurrentLocation(true);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            public void onInfoWindowClick(final Marker marker) {
                LatLng position = marker.getPosition();
                Log.d(String.valueOf(GPSMapActivity.this.getClass()),"Compare Location: "+globalLatLng.latitude+","+globalLatLng.longitude+" | "+position.latitude+","+position.longitude);
                if (position.equals(globalLatLng)) {
                    return;
                }
                String[] items={"Book Place","More Info"};
                AlertDialog.Builder itemDilog = new AlertDialog.Builder(GPSMapActivity.this);
                itemDilog.setTitle("");
                itemDilog.setCancelable(true);
                itemDilog.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case 0:{
                                Log.d(String.valueOf(GPSMapActivity.this.getClass()),"Book Place");
                                String UUID = marker.getTitle();
                                ParkingArea val = (ParkingArea)parkingAreasList.get(UUID);
                                Intent intent = new Intent(GPSMapActivity.this, BookParkingAreaActivity.class);
                                intent.putExtra("UUID", UUID);
                                intent.putExtra("ParkingArea", val);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                Log.d(String.valueOf(GPSMapActivity.this.getClass()), "Value of UUID: "+UUID);
                            }break;
                            case 1:{
                                Log.d(String.valueOf(GPSMapActivity.this.getClass()),"More Info");
                            }break;
                        }

                    }
                });
                itemDilog.show();

            }
        });

    }
}