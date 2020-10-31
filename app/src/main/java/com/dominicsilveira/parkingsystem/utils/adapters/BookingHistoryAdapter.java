package com.dominicsilveira.parkingsystem.utils.adapters;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.dominicsilveira.parkingsystem.common.BookingDetailsActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.BookedSlotKey;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.MyViewHolder>{
    Context context;
    List<BookedSlotKey> bookedSlotKeyList = new ArrayList<BookedSlotKey>();
    FirebaseAuth auth;
    FirebaseDatabase db;

    public BookingHistoryAdapter(List<BookedSlotKey> bookedSlotKeyList){
        this.bookedSlotKeyList = bookedSlotKeyList;
        Log.d("distParkingArea", String.valueOf(bookedSlotKeyList));
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView userHistoryCard;
        TextView mainText,dateText;

        MyViewHolder(View itemView) {
            super(itemView);
            userHistoryCard = (MaterialCardView)itemView.findViewById(R.id.userHistoryCard);
            mainText = (TextView)itemView.findViewById(R.id.mainText);
            dateText = (TextView)itemView.findViewById(R.id.dateText);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BookingHistoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_history_row_layout, parent, false);
        BookingHistoryAdapter.MyViewHolder pvh = new BookingHistoryAdapter.MyViewHolder(v);
        return pvh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final BookingHistoryAdapter.MyViewHolder holder, int position) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        BookedSlotKey bookedSlotKey=bookedSlotKeyList.get(position);
//        final String id = (String) closestDistance.key;
//        final ParkingArea parkingArea = (ParkingArea) closestDistance.parkingArea;
        setDatas(holder,bookedSlotKey);
    }

    public void setDatas(BookingHistoryAdapter.MyViewHolder holder, final BookedSlotKey bookedSlotKey){
        final BookedSlots bookedSlot=bookedSlotKey.bookedSlots;
        holder.mainText.setText(bookedSlot.placeID);
        Date date=bookedSlot.startTime;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy HH:mm a");
        String dateStr= simpleDateFormat.format(date);
        holder.dateText.setText(dateStr);
        holder.userHistoryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, BookingDetailsActivity.class);
                intent.putExtra("UUID", bookedSlotKey.key);
                intent.putExtra("BookedSlot", bookedSlot);
                context.startActivity(intent);
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return bookedSlotKeyList.size();
    }
}
