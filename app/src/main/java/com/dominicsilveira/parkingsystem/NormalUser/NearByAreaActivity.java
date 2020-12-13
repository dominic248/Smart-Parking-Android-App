package com.dominicsilveira.parkingsystem.NormalUser;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.ClosestDistance;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
import com.dominicsilveira.parkingsystem.utils.adapters.CloseLocationAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NearByAreaActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;
    BasicUtils utils=new BasicUtils();

    FusedLocationProviderClient client;

    private RecyclerView recyclerView;
    private CloseLocationAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    TextView empty_view;

    List<ClosestDistance> closestDistanceList=new ArrayList<ClosestDistance>();

    AppConstants globalClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_by_area);

        initComponents();
        getPreCurrentLocation();
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(NearByAreaActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
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

        getSupportActionBar().setTitle("Near-by Areas");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        empty_view=findViewById(R.id.empty_view);
        recyclerView = (RecyclerView) findViewById(R.id.closest_location_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(NearByAreaActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        client=LocationServices.getFusedLocationProviderClient(NearByAreaActivity.this);
    }


    private void getPreCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(NearByAreaActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }else{
            ActivityCompat.requestPermissions(NearByAreaActivity.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, AppConstants.LOCATION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(NearByAreaActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NearByAreaActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NearByAreaActivity.this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},AppConstants.LOCATION_REQUEST_CODE);
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
                                        ClosestDistance closestDistance=new ClosestDistance(
                                                distance(location.getLatitude(), location.getLongitude(), parkingData.latitude, parkingData.longitude, "K"),
                                                parkingData,
                                                dataSnapshot.getKey());
                                        closestDistanceList.add(closestDistance);
                                    }
                                    mAdapter = new CloseLocationAdapter(closestDistanceList);
                                    mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                                        @Override public void onChanged() {
                                            super.onChanged();
                                            if(mAdapter.getItemCount()>0){
                                                recyclerView.setVisibility(View.VISIBLE);
                                                empty_view.setVisibility(View.GONE);
                                                Log.i("FilterSearch","not");
                                            }else{
                                                recyclerView.setVisibility(View.GONE);
                                                empty_view.setVisibility(View.VISIBLE);
                                                Log.i("FilterSearch","empty");
                                            }
                                            // access adapter's dataset size here or in that method
                                        }
                                    });
                                    recyclerView.setAdapter(mAdapter);
                                    if(closestDistanceList.isEmpty()){
                                        recyclerView.setVisibility(View.GONE);
                                        empty_view.setVisibility(View.VISIBLE);
                                    }else{
                                        recyclerView.setVisibility(View.VISIBLE);
                                        empty_view.setVisibility(View.GONE);
                                    }
                                    Log.d(String.valueOf(NearByAreaActivity.this.getClass()),"recyclerview pass: "+String.valueOf(closestDistanceList));
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
        if(requestCode==AppConstants.LOCATION_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nearby_menu,menu);
        MenuItem item=menu.findItem(R.id.action_search);
        SearchView searchView=(SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mAdapter!=null){
                    mAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}