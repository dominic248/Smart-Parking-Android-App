package com.dominicsilveira.parkingsystem.utils.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NumberPlateAdapter extends RecyclerView.Adapter<NumberPlateAdapter.MyViewHolder>{
    Map<String, NumberPlate> numberPlateMap;
    List<String> keys = new ArrayList<String>();

    public NumberPlateAdapter(Map<String, NumberPlate> numberPlateMap,List<String> keys){
        this.numberPlateMap = numberPlateMap;
        this.keys=keys;
        Log.d("NumberPlate keys", String.valueOf(keys));
    }

    public void updateVal(){

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView numberPlateCard;
        TextView numberPlate;
        TextView wheelerType;

        MyViewHolder(View itemView) {
            super(itemView);
            numberPlateCard = (MaterialCardView)itemView.findViewById(R.id.numberPlateCard);
            numberPlate = (TextView)itemView.findViewById(R.id.mainText);
            wheelerType = (TextView)itemView.findViewById(R.id.wheelerType);
        }
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public NumberPlateAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.include_number_plate_row_layout, parent, false);
        MyViewHolder pvh = new MyViewHolder(v);
        return pvh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        String id = (String) keys.get(position);
        NumberPlate numberPlate=numberPlateMap.get(id);
        Log.d("Number plate id", String.valueOf(id)+String.valueOf(numberPlate));
        holder.numberPlate.setText(numberPlate.numberPlate);
        String wheelerTypeStr="";
        if(numberPlate.wheelerType==3)
            wheelerTypeStr="3 Wheeler";
        else if(numberPlate.wheelerType==2)
            wheelerTypeStr="2 Wheeler";
        else
            wheelerTypeStr="4 Wheeler";
        holder.wheelerType.setText(wheelerTypeStr);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return keys.size();
    }
}
