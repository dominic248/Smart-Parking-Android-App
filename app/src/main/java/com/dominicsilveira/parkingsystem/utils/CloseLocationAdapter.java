package com.dominicsilveira.parkingsystem.utils;

import android.content.Intent;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.dominicsilveira.parkingsystem.NormalUser.BookParkingAreaActivity;
import com.dominicsilveira.parkingsystem.NormalUser.GPSMapActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloseLocationAdapter extends RecyclerView.Adapter<CloseLocationAdapter.MyViewHolder>{

    Map<Double, HashMap<String, ParkingArea>> distParkingArea;
    List<Double> keys = new ArrayList<Double>();

    public CloseLocationAdapter(Map<Double, HashMap<String, ParkingArea>> distParkingArea){
        this.distParkingArea = distParkingArea;
        keys.addAll(distParkingArea.keySet());
        Log.d("distParkingArea", String.valueOf(keys));
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView closeLocationCard;
        TextView mainName;
        Button dropdownBtn;
        ConstraintLayout expandCard;
        ImageButton mapBtn,bookBtn;

        MyViewHolder(View itemView) {
            super(itemView);
            expandCard = (ConstraintLayout)itemView.findViewById(R.id.expandCard);
            closeLocationCard = (MaterialCardView)itemView.findViewById(R.id.closeLocationCard);
            mainName = (TextView)itemView.findViewById(R.id.mainName);
            dropdownBtn = (Button)itemView.findViewById(R.id.dropdownBtn);
            mapBtn = (ImageButton)itemView.findViewById(R.id.mapBtn);
            bookBtn = (ImageButton)itemView.findViewById(R.id.bookBtn);
        }
    }
        @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CloseLocationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.close_location_row_layout, parent, false);
        MyViewHolder pvh = new MyViewHolder(v);
        return pvh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        HashMap<String, ParkingArea> idParkingArea=distParkingArea.get(keys.get(position));
        String id = (String) idParkingArea.keySet().toArray()[0];
        ParkingArea parkingArea = (ParkingArea) idParkingArea.values().toArray()[0];
        Log.d("id", String.valueOf(id)+String.valueOf(parkingArea));
        holder.mainName.setText(parkingArea.name);

        TextView availableText = (TextView) holder.expandCard.findViewById(R.id.availableText);
        TextView occupiedText = (TextView) holder.expandCard.findViewById(R.id.occupiedText);
        TextView price2Text = (TextView) holder.expandCard.findViewById(R.id.price2Text);
        TextView price3Text = (TextView) holder.expandCard.findViewById(R.id.price3Text);
        TextView price4Text = (TextView) holder.expandCard.findViewById(R.id.price4Text);
        String prepend=": ";
        availableText.setText(prepend.concat(String.valueOf(parkingArea.availableSlots)));
        occupiedText.setText(prepend.concat(String.valueOf(parkingArea.occupiedSlots)));
        price2Text.setText(prepend.concat("Rs.").concat(String.valueOf(parkingArea.amount2).concat("/Hr")));
        price3Text.setText(prepend.concat("Rs.").concat(String.valueOf(parkingArea.amount3).concat("/Hr")));
        price4Text.setText(prepend.concat("Rs.").concat(String.valueOf(parkingArea.amount4).concat("/Hr")));

        holder.bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v ) {
                Log.e("Adap",String.valueOf(holder.getAdapterPosition()));
                HashMap<String, ParkingArea> idParkingArea=distParkingArea.get(keys.get(holder.getAdapterPosition()));
                String UUID = (String) idParkingArea.keySet().toArray()[0];
                ParkingArea val = (ParkingArea) idParkingArea.values().toArray()[0];
                Intent intent=new Intent(v.getContext(), BookParkingAreaActivity.class);
                intent.putExtra("UUID", UUID);
                intent.putExtra("ParkingArea", val);
                v.getContext().startActivity(intent);
            }
        });

        holder.dropdownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.expandCard.getVisibility()==View.GONE){
                    TransitionManager.beginDelayedTransition(holder.closeLocationCard, new AutoTransition());
                    holder.expandCard.setVisibility(View.VISIBLE);
                    holder.dropdownBtn.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                } else {
                    TransitionManager.beginDelayedTransition(holder.closeLocationCard, new AutoTransition());
                    holder.expandCard.setVisibility(View.GONE);
                    holder.dropdownBtn.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
                }
            }
        });

        holder.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v ) {
                Log.e("Adap",String.valueOf(holder.getAdapterPosition()));
                HashMap<String, ParkingArea> idParkingArea=distParkingArea.get(keys.get(holder.getAdapterPosition()));
                String UUID = (String) idParkingArea.keySet().toArray()[0];
                ParkingArea val = (ParkingArea) idParkingArea.values().toArray()[0];
                Intent intent=new Intent(v.getContext(), GPSMapActivity.class);
                Log.e("Close loc to GPS map",val.name);
                intent.putExtra("LOCATION_NAME", val.name);
                intent.putExtra("LOCATION_LATITUDE", val.latitude);
                intent.putExtra("LOCATION_LONGITUDE", val.longitude);
                v.getContext().startActivity(intent);
            }
        });


        PieChart platforms_chart = (PieChart) holder.expandCard.findViewById(R.id.platforms_chart);
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
        platforms_chart.animateXY(1400,1400);
        platforms_chart.notifyDataSetChanged();
        platforms_chart.invalidate();

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return distParkingArea.size();
    }

}
