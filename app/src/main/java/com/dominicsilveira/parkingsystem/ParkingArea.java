package com.dominicsilveira.parkingsystem;

import java.io.Serializable;

public class ParkingArea implements Serializable{
    public String name;
    public double latitude,longitude;

    public ParkingArea(){}

    public ParkingArea(String name, double latitude,double longitude){
        this.name=name;
        this.latitude=latitude;
        this.longitude=longitude;
    }
}



