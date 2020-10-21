package com.dominicsilveira.parkingsystem.ui.dashboard.normal;

import android.app.ActivityManager;
import android.app.AlertDialog;
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
import android.content.Intent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.dominicsilveira.parkingsystem.NormalUser.GPSMapActivity;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.utils.notifications.AlarmUtils;
import com.dominicsilveira.parkingsystem.utils.notifications.NotificationReceiver;
import com.dominicsilveira.parkingsystem.utils.services.MyParkingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class DashboardNormalFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseDatabase db;
    LinearLayout openMapsBtn,myBookingsBtn,nearByBtn;

    Button startService,stopService,checkService;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_normal, container, false);

        initComponents(root);
        attachListeners();

        return root;
    }

    private void initComponents(View root) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        openMapsBtn = root.findViewById(R.id.openMapsBtn);
        myBookingsBtn = root.findViewById(R.id.myBookingsBtn);
        checkService = root.findViewById(R.id.checkService);
        nearByBtn = root.findViewById(R.id.nearByBtn);
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