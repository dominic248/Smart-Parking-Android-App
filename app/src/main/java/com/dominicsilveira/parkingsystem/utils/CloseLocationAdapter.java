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
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
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

        TextView availableText = (TextView) holder.expandCard.findViewById(R.id.availableText);
        TextView outOfText = (TextView) holder.expandCard.findViewById(R.id.outOfText);
        TextView priceText = (TextView) holder.expandCard.findViewById(R.id.priceText);
        TextView locationText = (TextView) holder.expandCard.findViewById(R.id.locationText);
//        headerText.setText("this is header by program");

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return distParkingArea.size();
    }

}
