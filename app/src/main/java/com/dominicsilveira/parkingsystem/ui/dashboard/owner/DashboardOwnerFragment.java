package com.dominicsilveira.parkingsystem.ui.dashboard.owner;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.utils.NumberPlateAdapter;
import com.dominicsilveira.parkingsystem.utils.SimpleToDeleteCallback;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class DashboardOwnerFragment extends Fragment {

    Button logout;
    ConstraintLayout expandCard;
    TextView availableText,occupiedText,price2Text,price3Text,price4Text;
    PieChart platforms_chart;

    boolean dataSet=false;

    FirebaseAuth auth;
    FirebaseDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_owner, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        logout = root.findViewById(R.id.logoutBtn);
        expandCard = root.findViewById(R.id.expandCard);
        availableText = expandCard.findViewById(R.id.availableText);
        occupiedText = expandCard.findViewById(R.id.occupiedText);
        price2Text = expandCard.findViewById(R.id.price2Text);
        price3Text = expandCard.findViewById(R.id.price3Text);
        price4Text = expandCard.findViewById(R.id.price4Text);
        platforms_chart = expandCard.findViewById(R.id.platforms_chart);


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "Logout Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        db.getReference().child("ParkingAreas").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        ParkingArea parkingArea = snapshot.getValue(ParkingArea.class);
                        setDashboardValues(parkingArea);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        db.getReference().child("ParkingAreas").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
                            setDashboardValues(parkingArea);
                            Log.e("CalledTwice","12");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        return root;
    }

    private void setDashboardValues(ParkingArea parkingArea) {
        String prepend=": ";
        availableText.setText(prepend.concat(String.valueOf(parkingArea.availableSlots)));
        occupiedText.setText(prepend.concat(String.valueOf(parkingArea.occupiedSlots)));
        price2Text.setText(prepend.concat("Rs.").concat(String.valueOf(parkingArea.amount2).concat("/Hr")));
        price3Text.setText(prepend.concat("Rs.").concat(String.valueOf(parkingArea.amount3).concat("/Hr")));
        price4Text.setText(prepend.concat("Rs.").concat(String.valueOf(parkingArea.amount4).concat("/Hr")));
//        platforms_chart.setUsePercentValues(true);
        Description desc=new Description();
        desc.setText("Details");
        platforms_chart.setDescription(desc);
        List<PieEntry> value=new ArrayList<>();
        value.add(new PieEntry(parkingArea.availableSlots,"Available"));
        value.add(new PieEntry(parkingArea.occupiedSlots,"Occupied"));
        PieDataSet pieDataSet=new PieDataSet(value,"Slots");
        PieData pieData=new PieData(pieDataSet);
        platforms_chart.setData(pieData);
        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        if(!dataSet){
            platforms_chart.animateXY(1400,1400);
            dataSet=true;
        }
        platforms_chart.notifyDataSetChanged();
        platforms_chart.invalidate();
    }
}