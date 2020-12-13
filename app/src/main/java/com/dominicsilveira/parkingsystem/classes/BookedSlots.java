package com.dominicsilveira.parkingsystem.classes;


import android.util.Log;


import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BookedSlots implements Serializable {
    public String userID,placeID,numberPlate,slotNo;
    public int hasPaid,amount,wheelerType,notificationID,readNotification,readBookedNotification,checkout;
    public Date startTime, endTime, checkoutTime;

    public BookedSlots(){}

    public BookedSlots(String userID, String placeID, String slotNo, String numberPlate, int wheelerType, Date startTime, Date endTime, int hasPaid, int amount,int notificationID,int readNotification,int readBookedNotification){
        this.userID=userID;
        this.placeID=placeID;
        this.slotNo=slotNo;
        this.numberPlate=numberPlate;
        this.wheelerType=wheelerType;
        this.hasPaid=hasPaid;
        this.amount=amount;
        this.startTime=startTime;
        this.endTime=endTime;
        this.notificationID=notificationID;
        this.readNotification=readNotification;
        this.readBookedNotification=readBookedNotification;
    }

    public void calcAmount(ParkingArea parkingArea){
        if(this.wheelerType!=0 && this.numberPlate!=null) {
            long diffInMillies = Math.abs(this.endTime.getTime() - this.startTime.getTime());
            long diffHour = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            long diffMin = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS) - diffHour * 60;
            if (diffMin > 0) {
                diffHour += 1;
            }
            Log.i("diffHourMin", diffHour + " " + diffMin);
            int wheelerAmount;
            if (this.wheelerType == 2)
                wheelerAmount = parkingArea.amount2;
            else if (this.wheelerType == 3)
                wheelerAmount = parkingArea.amount3;
            else
                wheelerAmount = parkingArea.amount4;
            this.amount = (int) diffHour * wheelerAmount;
        }else{
            this.amount=0;
        }
    }

    public Boolean timeDiffValid(){
        long diffInMillies = Math.abs(this.endTime.getTime() - this.startTime.getTime());
        long diffHour = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        long diffMin = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS) - diffHour * 60;
        if(diffHour>0 || diffMin>=15)return true;
        return false;
    }

    public static Comparator<BookedSlots> DateComparator = new Comparator<BookedSlots>() {
        public int compare(BookedSlots s1, BookedSlots s2) {
            /*For ascending order*/
//            return s1.startTime.compareTo(s2.startTime);
            /*For descending order*/
            return s2.startTime.compareTo(s1.startTime);
        }
    };
}
