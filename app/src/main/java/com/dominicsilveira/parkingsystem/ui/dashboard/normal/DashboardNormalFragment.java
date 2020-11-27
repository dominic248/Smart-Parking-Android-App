package com.dominicsilveira.parkingsystem.ui.dashboard.normal;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.dominicsilveira.parkingsystem.NormalUser.BookParkingAreaActivity;
import com.dominicsilveira.parkingsystem.NormalUser.NearByAreaActivity;
import com.dominicsilveira.parkingsystem.NormalUser.UserHistoryActivity;
import com.dominicsilveira.parkingsystem.R;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.dominicsilveira.parkingsystem.NormalUser.GPSMapActivity;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
import com.dominicsilveira.parkingsystem.utils.notifications.AlarmUtils;
import com.dominicsilveira.parkingsystem.utils.notifications.NotificationHelper;
import com.dominicsilveira.parkingsystem.utils.notifications.NotificationReceiver;
import com.dominicsilveira.parkingsystem.utils.services.MyParkingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class DashboardNormalFragment extends Fragment implements OnMapReadyCallback {

    FirebaseAuth auth;
    FirebaseDatabase db;
    LinearLayout openMapsBtn,myBookingsBtn,nearByBtn;

    Button startService,stopService,checkService;
    BasicUtils utils=new BasicUtils();

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    ImageButton getLocationBtn;

    GoogleMap gMap;
    LatLng globalLatLng;

    HashMap<String, ParkingArea> parkingAreasList = new HashMap<String,ParkingArea>();
    AppConstants globalClass;


    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_normal, container, false);

        initComponents(root);
        attachListeners();

        getPreCurrentLocation();
        if(!utils.isNetworkAvailable(getActivity().getApplication())){
            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
        }

        return root;
    }

    private void initComponents(View root) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        openMapsBtn = root.findViewById(R.id.openMapsBtn);
        myBookingsBtn = root.findViewById(R.id.myBookingsBtn);
        checkService = root.findViewById(R.id.checkService);
        nearByBtn = root.findViewById(R.id.nearByBtn);
        getLocationBtn=root.findViewById(R.id.getLocationBtn);

        globalClass=(AppConstants)getActivity().getApplicationContext();

        supportMapFragment=(SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);
        client= LocationServices.getFusedLocationProviderClient(getActivity());
    }

    private void attachListeners() {
//        startService.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                ContextCompat.startForegroundService(getActivity(),new Intent(getActivity(), MyParkingService.class));
//                getActivity().startService(new Intent(getActivity(), MyParkingService.class));
//                Toast.makeText(getActivity(), "Service started", Toast.LENGTH_SHORT).show();
//            }
//        });
//        stopService.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getActivity().stopService(new Intent(getActivity(), MyParkingService.class));
//                Toast.makeText(getActivity(), "Service stopped", Toast.LENGTH_SHORT).show();
//            }
//        });

        db.getReference().child("ParkingAreas")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
                            parkingAreasList.put(dataSnapshot.getKey(),parkingArea);
                            Log.d(String.valueOf(getActivity().getClass()),"GPS Map: "+parkingArea.name);
                        }
                        attachMarkerOnMap();
                        Log.d(String.valueOf(getActivity().getClass()),"GPS Map list"+ String.valueOf(parkingAreasList));
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

        checkService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isMyServiceRunning(MyParkingService.class))
                    Toast.makeText(getActivity(), "Service is not running", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Service is running", Toast.LENGTH_SHORT).show();
            }
        });

        nearByBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), NearByAreaActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        myBookingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), UserHistoryActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        openMapsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), GPSMapActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
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

    private void attachMarkerOnMap() {
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap=googleMap;
                for (Map.Entry<String, ParkingArea> stringParkingAreaEntry : parkingAreasList.entrySet()) {
                    Map.Entry mapElement = (Map.Entry) stringParkingAreaEntry;
                    ParkingArea parking = (ParkingArea) mapElement.getValue();
                    Log.e(String.valueOf(getActivity().getClass()),"Add marker on map: "+parking.name);
                    LatLng latLngParking = new LatLng(parking.latitude,
                                parking.longitude);
                    MarkerOptions option = new MarkerOptions().position(latLngParking)
                                .title(mapElement.getKey().toString())
                                .snippet(parking.name);
                    gMap.addMarker(option);
                }
            }
        });
    }

    private void getPreCurrentLocation() {
        if(ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            getCurrentLocation(true);
        }else{
            ActivityCompat.requestPermissions(getActivity(),new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},AppConstants.LOCATION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation(final Boolean zoom) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]
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
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(globalLatLng,18));
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
                Log.d(String.valueOf(getActivity().getClass()),"Compare Location: "+globalLatLng.latitude+","+globalLatLng.longitude+" | "+position.latitude+","+position.longitude);
                if (position.equals(globalLatLng)) {
                    return;
                }
                String[] items={"Book Place","More Info"};
                androidx.appcompat.app.AlertDialog.Builder itemDilog = new AlertDialog.Builder(getActivity());
                itemDilog.setTitle("");
                itemDilog.setCancelable(true);
                itemDilog.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case 0:{
                                Log.d(getActivity().getComponentName().getClassName(),"Book Place");
                                String UUID = marker.getTitle();
                                ParkingArea val = (ParkingArea)parkingAreasList.get(UUID);
                                Intent intent = new Intent(getActivity(), BookParkingAreaActivity.class);
                                intent.putExtra("UUID", UUID);
                                intent.putExtra("ParkingArea", val);
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                Log.d(String.valueOf(getActivity().getClass()), "Value of UUID: "+UUID);
                            }break;
                            case 1:{
                                Log.d(String.valueOf(getActivity().getClass()),"More Info");
                            }break;
                        }

                    }
                });
                itemDilog.show();

            }
        });

    }
}