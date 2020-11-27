package com.dominicsilveira.parkingsystem.OwnerUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.classes.SlotNoInfo;
import com.dominicsilveira.parkingsystem.classes.UpiInfo;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class AddPositionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    GoogleMap gMap;
    FloatingActionButton addLocationBtn;
    AppCompatEditText areaNameText,upiIdText,upiNameText,amount2Text,amount3Text,amount4Text,totalSlotsText;
    Button loadFromFile;
    NachoTextView nachoTextView;

    LatLng gpsLatLng=null;
    LatLng globalLatLng=null;

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    List<SlotNoInfo> slotNos = new ArrayList<>();
    List<String> slotNoString = new ArrayList<>();
    BasicUtils utils=new BasicUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_position);

        initComponents();
        attachListeners();
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(AddPositionActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
        }

        getPreCurrentLocation();
    }

    private void initComponents() {
        supportMapFragment=(SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);
        client= LocationServices.getFusedLocationProviderClient(AddPositionActivity.this);

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        areaNameText=findViewById(R.id.areaNameText);
        upiIdText=findViewById(R.id.upiIdText);
        upiNameText=findViewById(R.id.upiNameText);
        totalSlotsText=findViewById(R.id.totalSlotsText);
        amount2Text=findViewById(R.id.amount2Text);
        amount3Text=findViewById(R.id.amount3Text);
        amount4Text=findViewById(R.id.amount4Text);
        addLocationBtn=findViewById(R.id.addLocationBtn);
        nachoTextView = findViewById(R.id.et_tag);
        loadFromFile = findViewById(R.id.loadFromFile);

        slotNoString.add("Slot-01");
        nachoTextView.setText(slotNoString);
        nachoTextView.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
    }

    private void attachListeners() {
        loadFromFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myFileIntent=new Intent(Intent.ACTION_GET_CONTENT);
                myFileIntent.setType("text/plain");
                startActivityForResult(myFileIntent,3000);
            }
        });
        addLocationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                slotNos.clear();
                for (Chip chip : nachoTextView.getAllChips()) {
                    CharSequence text = chip.getText();
                    slotNos.add(new SlotNoInfo((String) text,false));
                }
                String areaName = areaNameText.getText().toString();
                String upiId = upiIdText.getText().toString();
                String upiName = upiNameText.getText().toString();
                String amount2 = amount2Text.getText().toString();
                String amount3 = amount3Text.getText().toString();
                String amount4 = amount4Text.getText().toString();
                String totalSlots = totalSlotsText.getText().toString();
                if(globalLatLng==null){
                    Toast.makeText(AddPositionActivity.this, "Please select a location!", Toast.LENGTH_SHORT).show();
                }else if(areaName.equals("") || upiId.equals("") || upiName.equals("") || amount2.equals("") || amount3.equals("") || amount4.equals("") || totalSlots.equals("")){
                    Toast.makeText(AddPositionActivity.this, "Please enter all fields!", Toast.LENGTH_SHORT).show();
                }else if(!utils.isUpiIdValid(upiId)){
                    Toast.makeText(AddPositionActivity.this, "Invalid UPI ID!", Toast.LENGTH_SHORT).show();
                }else if(slotNos.size()>Integer.parseInt(totalSlots)){
                    Toast.makeText(AddPositionActivity.this, "No. of Slot Names are more than No. of Slots", Toast.LENGTH_SHORT).show();
                }else if(slotNos.size()<Integer.parseInt(totalSlots)){
                    Toast.makeText(AddPositionActivity.this, "No. of Slots are more than No. of Slot Names", Toast.LENGTH_SHORT).show();
                }else{
                    final ParkingArea parkingArea = new ParkingArea(areaName,globalLatLng.latitude,globalLatLng.longitude,
                            auth.getCurrentUser().getUid(),Integer.parseInt(totalSlots),0,
                            Integer.parseInt(amount2),Integer.parseInt(amount3),Integer.parseInt(amount4),slotNos);
                    final UpiInfo upiInfo=new UpiInfo(upiId,upiName);
                    final String key=db.getReference("ParkingAreas").push().getKey();
                    db.getReference("ParkingAreas")
                            .child(key)
                            .setValue(parkingArea).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                db.getReference("UpiInfo")
                                        .child(auth.getCurrentUser().getUid())
                                        .setValue(upiInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(AddPositionActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(AddPositionActivity.this, MainOwnerActivity.class);
                                            intent.putExtra("FRAGMENT_NO", 0);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
                                            finish();
                                        } else {
                                            Toast.makeText(AddPositionActivity.this, "Failed to add UPI details", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(AddPositionActivity.this, "Failed to add extra details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3000) {
            if (resultCode == RESULT_OK) {
                slotNoString.clear();
                Uri uri = data.getData();
                BufferedReader br;
                FileOutputStream os;
                try {
                    br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
                    os = openFileOutput("newFileName", Context.MODE_PRIVATE);
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        os.write(line.getBytes());
                        slotNoString.add(line);
                        Log.d(String.valueOf(AddPositionActivity.this.getClass()),"Read from file: "+String.valueOf(line));
                    }
                    nachoTextView.setText(slotNoString);
                    br.close();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap=googleMap;
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                globalLatLng=latLng;
                MarkerOptions markerOptions=new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(latLng.latitude+" : "+latLng.longitude);
                gMap.clear();
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,5));
                gMap.addMarker(markerOptions);
            }
        });
    }

    private void getPreCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(AddPositionActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }else{
            ActivityCompat.requestPermissions(AddPositionActivity.this,new String[]
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
                            gpsLatLng=new LatLng(location.getLatitude(),
                                        location.getLongitude());
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gpsLatLng,30));
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
                getCurrentLocation();
            }
        }
    }
}

