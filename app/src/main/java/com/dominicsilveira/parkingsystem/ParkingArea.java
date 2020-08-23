package com.dominicsilveira.parkingsystem;

import java.io.Serializable;

public class ParkingArea implements Serializable{
    public String name,upiId,upiName,amount,slots;
    public double latitude,longitude;

    public ParkingArea(){}

    public ParkingArea(String name, double latitude,double longitude,String upiId,String upiName,String amount,String slots){
        this.name=name;
        this.latitude=latitude;
        this.longitude=longitude;
        this.upiId=upiId;
        this.upiName=upiName;
        this.amount=amount;
        this.slots=slots;
    }
}



