package com.dominicsilveira.parkingsystem.classes;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.Date;

public class BookedSlots {
    public String userID,placeID,numberPlate;
    public int hasPaid,amount,wheelerType,notificationID,readNotification;
    public Date startTime, endTime;

    FirebaseAuth auth;
    FirebaseDatabase db;

    public BookedSlots(){}

    public BookedSlots(String userID, String placeID, String numberPlate, int wheelerType, Date startTime, Date endTime, int hasPaid, int amount,int notificationID,int readNotification){
        this.userID=userID;
        this.placeID=placeID;
        this.numberPlate=numberPlate;
        this.wheelerType=wheelerType;
        this.hasPaid=hasPaid;
        this.amount=amount;
        this.startTime=startTime;
        this.endTime=endTime;
        this.notificationID=notificationID;
        this.readNotification=readNotification;

    }


    public static Comparator<BookedSlots> DateComparator = new Comparator<BookedSlots>() {
        public int compare(BookedSlots s1, BookedSlots s2) {
            /*For ascending order*/
//            return s1.startTime.compareTo(s2.startTime);
            /*For descending order*/
            return s2.startTime.compareTo(s1.startTime);
        }
    };

    public void saveToFirebase(final Context context, final ParkingArea parkingArea) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        final String key=db.getReference("BookedSlots").push().getKey();
        if(parkingArea.availableSlots>0){
            parkingArea.availableSlots-=1;
            parkingArea.occupiedSlots+=1;
            db.getReference("ParkingAreas").child(placeID).setValue(parkingArea).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        db.getReference("BookedSlots").child(key).setValue(BookedSlots.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show();
                                    parkingArea.availableSlots+=1;
                                    parkingArea.occupiedSlots-=1;
                                    db.getReference("ParkingAreas").child(placeID).setValue(parkingArea);
                                }
                            }
                        });
                    }
                }
            });
        }else {
            Toast.makeText(context,"Failed! Slots are full.",Toast.LENGTH_SHORT).show();
        }
    }
}
