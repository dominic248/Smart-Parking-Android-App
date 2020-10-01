package com.dominicsilveira.parkingsystem.utils.adapters;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UserHistoryAdapter extends RecyclerView.Adapter<UserHistoryAdapter.MyViewHolder>{

    List<BookedSlots> bookedSlotsList = new ArrayList<BookedSlots>();
    FirebaseAuth auth;
    FirebaseDatabase db;

    public UserHistoryAdapter(List<BookedSlots> bookedSlotsList){
        this.bookedSlotsList = bookedSlotsList;
        Collections.sort(bookedSlotsList, BookedSlots.DateComparator);
        Log.d("distParkingArea", String.valueOf(bookedSlotsList));
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView closeLocationCard;
        TextView mainText,dateText;

        MyViewHolder(View itemView) {
            super(itemView);
            closeLocationCard = (MaterialCardView)itemView.findViewById(R.id.closeLocationCard);
            mainText = (TextView)itemView.findViewById(R.id.mainText);
            dateText = (TextView)itemView.findViewById(R.id.dateText);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserHistoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_history_row_layout, parent, false);
        UserHistoryAdapter.MyViewHolder pvh = new UserHistoryAdapter.MyViewHolder(v);
        return pvh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final UserHistoryAdapter.MyViewHolder holder, int position) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        BookedSlots bookedSlot=bookedSlotsList.get(position);
//        final String id = (String) closestDistance.key;
//        final ParkingArea parkingArea = (ParkingArea) closestDistance.parkingArea;
        setDatas(holder,bookedSlot);
    }

    public void setDatas(UserHistoryAdapter.MyViewHolder holder, final BookedSlots bookedSlot){
        holder.mainText.setText(bookedSlot.placeID);
        Date date=bookedSlot.startTime;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy HH:mm a");
        String dateStr= simpleDateFormat.format(date);
        holder.dateText.setText(dateStr);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return bookedSlotsList.size();
    }
}
