package com.dominicsilveira.parkingsystem.utils.adapters;

import android.app.Activity;
import android.content.Intent;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.dominicsilveira.parkingsystem.NormalUser.BookParkingAreaActivity;
import com.dominicsilveira.parkingsystem.NormalUser.GPSMapActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.ClosestDistance;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CloseLocationAdapter extends RecyclerView.Adapter<CloseLocationAdapter.MyViewHolder>{
    Activity context;
    List<ClosestDistance> closestDistances;
    List<ClosestDistance> arrayListFiltered;
    List<ClosestDistance> filteredList;
    FirebaseAuth auth;
    FirebaseDatabase db;

    public CloseLocationAdapter(List<ClosestDistance> closestDistances){
        this.closestDistances = closestDistances;
        this.arrayListFiltered = new ArrayList<>(closestDistances);
        this.filteredList = new ArrayList<>(closestDistances);
        Collections.sort(closestDistances, ClosestDistance.ClosestDistComparator);
        Log.d("distParkingArea", String.valueOf(closestDistances));
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView closeLocationCard;
        TextView mainName;
        Button dropdownBtn;
        LinearLayout expandCard;
        ImageButton mapBtn,bookBtn;

        MyViewHolder(View itemView) {
            super(itemView);
            expandCard = (LinearLayout)itemView.findViewById(R.id.expandCard);
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
        context = (Activity) recyclerView.getContext();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CloseLocationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.include_close_location_row_layout, parent, false);
        MyViewHolder pvh = new MyViewHolder(v);
        return pvh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ClosestDistance closestDistance=closestDistances.get(position);
        final String id = (String) closestDistance.key;
        final ParkingArea parkingArea = (ParkingArea) closestDistance.parkingArea;
        Log.d("id", String.valueOf(id)+String.valueOf(parkingArea));
        setDatas(holder,id,parkingArea);

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


        db.getReference().child("ParkingAreas").child(id)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if(snapshot.getKey().equals("availableSlots") || snapshot.getKey().equals("occupiedSlots") || snapshot.getKey().equals("totalSlots")){
                            parkingArea.setData(snapshot.getKey(),snapshot.getValue(int.class));
                            setDatas(holder,id,parkingArea);
                            Log.e("updateView", snapshot.getKey()+" "+snapshot.getValue(int.class)+" "+parkingArea.occupiedSlots);
                        }
                    }
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    public void setDatas(MyViewHolder holder, final String id, final ParkingArea parkingArea){
        holder.mainName.setText(parkingArea.name);
        TextView availableText = (TextView) holder.expandCard.findViewById(R.id.availableText);
        TextView occupiedText = (TextView) holder.expandCard.findViewById(R.id.occupiedText);
        TextView price2Text = (TextView) holder.expandCard.findViewById(R.id.price2Text);
        TextView price3Text = (TextView) holder.expandCard.findViewById(R.id.price3Text);
        TextView price4Text = (TextView) holder.expandCard.findViewById(R.id.price4Text);
        String prepend="Rs.";
        availableText.setText(String.valueOf(parkingArea.availableSlots));
        occupiedText.setText(String.valueOf(parkingArea.occupiedSlots));
        price2Text.setText(prepend.concat(String.valueOf(parkingArea.amount2).concat("/Hr")));
        price3Text.setText(prepend.concat(String.valueOf(parkingArea.amount3).concat("/Hr")));
        price4Text.setText(prepend.concat(String.valueOf(parkingArea.amount4).concat("/Hr")));
        PieChart platforms_chart = (PieChart) holder.expandCard.findViewById(R.id.platforms_chart);
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
        platforms_chart.notifyDataSetChanged();
        platforms_chart.invalidate();

        holder.bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v ) {
                Log.i("ErrSyn", String.valueOf(parkingArea));
                Intent intent=new Intent(v.getContext(), BookParkingAreaActivity.class);
                intent.putExtra("UUID", id);
                intent.putExtra("ParkingArea", parkingArea);
                context.startActivity(intent);
                context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        holder.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v ) {
                Intent intent=new Intent(v.getContext(), GPSMapActivity.class);
                Log.e("Close loc to GPS map",parkingArea.name);
                intent.putExtra("LOCATION_NAME", parkingArea.name);
                intent.putExtra("LOCATION_LATITUDE", parkingArea.latitude);
                intent.putExtra("LOCATION_LONGITUDE", parkingArea.longitude);
                context.startActivity(intent);
                context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return closestDistances.size();
    }

    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(arrayListFiltered);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (ClosestDistance item : arrayListFiltered) {
                    if (item.parkingArea.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            closestDistances.clear();
            try{closestDistances.addAll((Collection<? extends ClosestDistance>) results.values);}
            catch (Exception e){e.printStackTrace();}
            notifyDataSetChanged();
        }
    };

}
