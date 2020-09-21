package com.dominicsilveira.parkingsystem.classes;

import org.joda.time.DateTime;

import java.util.Date;

public class BookedSlots {
    public String userID,placeID,numberPlate;
    public int hasPaid,amount,wheelerType;
    public Date startTime, endTime;

    public BookedSlots(){}

    public BookedSlots(String userID, String placeID, String numberPlate, int wheelerType, Date startTime, Date endTime, int hasPaid, int amount){
        this.userID=userID;
        this.placeID=placeID;
        this.numberPlate=numberPlate;
        this.wheelerType=wheelerType;
        this.hasPaid=hasPaid;
        this.amount=amount;
        this.startTime=startTime;
        this.endTime=endTime;
    }
}
