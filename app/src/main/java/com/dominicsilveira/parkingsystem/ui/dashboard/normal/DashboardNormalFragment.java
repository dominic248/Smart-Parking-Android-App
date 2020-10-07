package com.dominicsilveira.parkingsystem.ui.dashboard.normal;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dominicsilveira.parkingsystem.NormalUser.NearByAreaActivity;
import com.dominicsilveira.parkingsystem.NormalUser.UserHistoryActivity;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.NormalUser.BookingPaymentActivity;
import com.dominicsilveira.parkingsystem.NormalUser.GPSMapActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.AddPositionActivity;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.ClosestDistance;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.utils.adapters.CloseLocationAdapter;
import com.dominicsilveira.parkingsystem.utils.services.MyParkingService;
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

public class DashboardNormalFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseDatabase db;
    LinearLayout openMapsBtn,myBookingsBtn,nearByBtn;

    Button logout;
    Button startService,stopService,checkService;
    FusedLocationProviderClient client;



    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_normal, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        logout = root.findViewById(R.id.logoutBtn);
        openMapsBtn = root.findViewById(R.id.openMapsBtn);
        myBookingsBtn = root.findViewById(R.id.myBookingsBtn);
        checkService = root.findViewById(R.id.checkService);
        nearByBtn = root.findViewById(R.id.nearByBtn);

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
            }
        });

        myBookingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), UserHistoryActivity.class));
            }
        });

        openMapsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), GPSMapActivity.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "Logout Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
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

}