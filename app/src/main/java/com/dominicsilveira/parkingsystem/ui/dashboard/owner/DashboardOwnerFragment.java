package com.dominicsilveira.parkingsystem.ui.dashboard.owner;



import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dominicsilveira.parkingsystem.OwnerUser.AreaHistoryActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.classes.SlotNoInfo;
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
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
import java.util.ArrayList;
import java.util.List;

public class DashboardOwnerFragment extends Fragment {

    LinearLayout expandCard,slotStatus,slot_individual_list_view,historyBtn;
    TextView availableText,occupiedText,price2Text,price3Text,price4Text,slotName,numberPlate;
    PieChart platforms_chart;
    SlotNoInfo slotNoInfo;

    BasicUtils utils=new BasicUtils();

    boolean dataSet=false;

    FirebaseAuth auth;
    FirebaseDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_owner, container, false);

        initComponents(root, inflater);
        attachListeners(inflater);

        if(!utils.isNetworkAvailable(getActivity().getApplication())){
            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
        }

        return root;
    }

    private void initComponents(View root,LayoutInflater inflater) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        expandCard = root.findViewById(R.id.expandCard);
        availableText = expandCard.findViewById(R.id.availableText);
        occupiedText = expandCard.findViewById(R.id.occupiedText);
        price2Text = expandCard.findViewById(R.id.price2Text);
        price3Text = expandCard.findViewById(R.id.price3Text);
        price4Text = expandCard.findViewById(R.id.price4Text);
        platforms_chart = expandCard.findViewById(R.id.platforms_chart);
        slotStatus=root.findViewById(R.id.slotStatus);
        historyBtn=root.findViewById(R.id.historyBtn);

    }

    private void attachListeners(final LayoutInflater inflater) {

        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AreaHistoryActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        db.getReference().child("ParkingAreas").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        ParkingArea parkingArea = snapshot.getValue(ParkingArea.class);
                        setDashboardValues(parkingArea, inflater);
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
                            setDashboardValues(parkingArea, inflater);
                            Log.e("DashboardOwnerFragment","Fetch parking area");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setDashboardValues(ParkingArea parkingArea,LayoutInflater inflater) {
        String prepend="Rs.";
        availableText.setText(String.valueOf(parkingArea.availableSlots));
        occupiedText.setText(String.valueOf(parkingArea.occupiedSlots));
        price2Text.setText(prepend.concat(String.valueOf(parkingArea.amount2).concat("/Hr")));
        price3Text.setText(prepend.concat(String.valueOf(parkingArea.amount3).concat("/Hr")));
        price4Text.setText(prepend.concat(String.valueOf(parkingArea.amount4).concat("/Hr")));
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
        if(((LinearLayout) slotStatus).getChildCount() > 0)
            ((LinearLayout) slotStatus).removeAllViews();
        for(int i=0;i<parkingArea.slotNos.size();i++){
            slot_individual_list_view = (LinearLayout)inflater.inflate(R.layout.include_slot_individual_list_view, null);
            slotName = (TextView)slot_individual_list_view.findViewById(R.id.slotName);
            numberPlate = (TextView)slot_individual_list_view.findViewById(R.id.numberPlate);
            slotNoInfo=parkingArea.slotNos.get(i);
            slotName.setText(slotNoInfo.name);
            numberPlate.setText(slotNoInfo.numberPlate);
            slotStatus.addView(slot_individual_list_view);
        }
    }
}