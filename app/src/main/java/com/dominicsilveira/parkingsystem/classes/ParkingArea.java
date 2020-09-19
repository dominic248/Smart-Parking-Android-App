package com.dominicsilveira.parkingsystem.classes;

import java.io.Serializable;

public class ParkingArea implements Serializable{
    public String name,upiId,upiName,amount2,amount3,amount4,userID;
    public double latitude,longitude;
    public int totalSlots,occupiedSlots,availableSlots;

    public ParkingArea(){}

    public ParkingArea(String name, double latitude,double longitude,
                       String upiId,String upiName, String userID,
                       int totalSlots,int occupiedSlots,
                       String amount2,String amount3,String amount4){
        this.name=name;
        this.latitude=latitude;
        this.longitude=longitude;
        this.upiId=upiId;
        this.upiName=upiName;
        this.userID=userID;
        this.totalSlots=totalSlots;
        this.occupiedSlots=occupiedSlots;
        this.availableSlots=totalSlots-occupiedSlots;
        this.amount2=amount2;
        this.amount3=amount3;
        this.amount4=amount4;
    }
}



