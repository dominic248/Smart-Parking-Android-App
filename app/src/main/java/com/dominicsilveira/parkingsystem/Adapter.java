package com.dominicsilveira.parkingsystem;

import android.content.Intent;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.dominicsilveira.parkingsystem.NormalUser.BookParkingAreaActivity;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.card.MaterialCardView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder>{

//    List<String> mDamascus;
    Map<Double, HashMap<String, ParkingArea>> distParkingArea;
    List<Double> keys = new ArrayList<Double>();

    public Adapter(Map<Double, HashMap<String, ParkingArea>> distParkingArea){
        this.distParkingArea = distParkingArea;
        Log.d("distParkingArea", String.valueOf(distParkingArea));
        Log.d("distParkingArea", String.valueOf(distParkingArea.keySet()));
        keys.addAll(distParkingArea.keySet());
        Log.d("distParkingArea", String.valueOf(keys));
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cv;
        TextView stockName;
        Button stockbtn;
        ConstraintLayout expandableView;

        MyViewHolder(View itemView) {
            super(itemView);
            expandableView = (ConstraintLayout)itemView.findViewById(R.id.expandableView);
            cv = (MaterialCardView)itemView.findViewById(R.id.card1);
            stockName = (TextView)itemView.findViewById(R.id.text);
            stockbtn = (Button)itemView.findViewById(R.id.textButton);
        }
    }
        @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    // Create new views (invoked by the layout manager)
    @Override
    public Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        MyViewHolder pvh = new MyViewHolder(v);
        return pvh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        Log.e("mDamascus", String.valueOf(position));
//        Log.e("mDamascus", mDamascus.get(position));
        HashMap<String, ParkingArea> idParkingArea=distParkingArea.get(keys.get(position));
        String id = (String) idParkingArea.keySet().toArray()[0];
        ParkingArea parkingArea = (ParkingArea) idParkingArea.values().toArray()[0];
        Log.d("id", String.valueOf(id)+String.valueOf(parkingArea));
        holder.stockName.setText(parkingArea.name);


//        holder.stockbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v ) {
//                Log.e("Adap",String.valueOf(holder.getAdapterPosition()));
//                HashMap<String, ParkingArea> idParkingArea=distParkingArea.get(keys.get(holder.getAdapterPosition()));
//                String UUID = (String) idParkingArea.keySet().toArray()[0];
//                ParkingArea val = (ParkingArea) idParkingArea.values().toArray()[0];
//                Intent intent=new Intent(v.getContext(), BookParkingAreaActivity.class);
//                intent.putExtra("UUID", UUID);
//                intent.putExtra("ParkingArea", val);
//                v.getContext().startActivity(intent);
//            }
//        });

        holder.stockbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.expandableView.getVisibility()==View.GONE){
                    TransitionManager.beginDelayedTransition(holder.cv, new AutoTransition());
                    holder.expandableView.setVisibility(View.VISIBLE);
                    holder.stockbtn.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                } else {
                    TransitionManager.beginDelayedTransition(holder.cv, new AutoTransition());
                    holder.expandableView.setVisibility(View.GONE);
                    holder.stockbtn.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
                }
            }
        });

        TextView headerText = (TextView) holder.expandableView.findViewById(R.id.phoneNumber);
        headerText.setText("this is header by program");



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return distParkingArea.size();
    }


}
