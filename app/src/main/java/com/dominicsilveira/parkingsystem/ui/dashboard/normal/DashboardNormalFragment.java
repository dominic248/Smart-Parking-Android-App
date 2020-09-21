package com.dominicsilveira.parkingsystem.ui.dashboard.normal;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dominicsilveira.parkingsystem.R;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.NormalUser.BookingPaymentActivity;
import com.dominicsilveira.parkingsystem.NormalUser.GPSMapActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.AddPositionActivity;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.common.MainNormalActivity;
import com.dominicsilveira.parkingsystem.utils.CloseLocationAdapter;
import com.dominicsilveira.parkingsystem.utils.MyParkingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DashboardNormalFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseDatabase db;

    Button logout,openMapsBtn,addLocationBtn,payBtn;
    Button startService,stopService,checkService;

    FusedLocationProviderClient client;
    LatLng globalLatLng;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    Map<Double, HashMap<String, ParkingArea>> parkingAreasList = new HashMap<Double, HashMap<String, ParkingArea>>();
    Map<Double, HashMap<String, ParkingArea>> treeMap;


    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_normal, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        logout = root.findViewById(R.id.logoutBtn);
        openMapsBtn = root.findViewById(R.id.openMapsBtn);
        addLocationBtn = root.findViewById(R.id.addLocationBtn);
        payBtn = root.findViewById(R.id.payBtn);

        startService = root.findViewById(R.id.startService);
        stopService = root.findViewById(R.id.stopService);
        checkService = root.findViewById(R.id.checkService);

        recyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        client= LocationServices.getFusedLocationProviderClient(getActivity());
        getPreCurrentLocation();


        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startService(new Intent(getActivity(), MyParkingService.class));
                Toast.makeText(getActivity(), "Service started", Toast.LENGTH_SHORT).show();
            }
        });
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().stopService(new Intent(getActivity(), MyParkingService.class));
                Toast.makeText(getActivity(), "Service stopped", Toast.LENGTH_SHORT).show();
            }
        });
        checkService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isMyServiceRunning(MyParkingService.class))
                    Toast.makeText(getActivity(), "Service is not running", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Service is running", Toast.LENGTH_SHORT).show();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "Logout Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), LoginActivity.class));

            }
        });

        openMapsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), GPSMapActivity.class));
            }
        });

        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddPositionActivity.class));
            }
        });

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), BookingPaymentActivity.class));
            }
        });

        return root;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void getPreCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }else{
            ActivityCompat.requestPermissions(getActivity(),new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]
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
                                    }
                                    treeMap = new TreeMap<Double, HashMap<String, ParkingArea>>(parkingAreasList);
                                    mAdapter = new CloseLocationAdapter(treeMap);
                                    recyclerView.setAdapter(mAdapter);
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