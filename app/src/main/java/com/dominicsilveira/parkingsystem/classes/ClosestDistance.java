package com.dominicsilveira.parkingsystem.classes;

import java.util.Comparator;

public class ClosestDistance {
    public double distance;
    public ParkingArea parkingArea;
    public String key;

    public ClosestDistance(){}
    public ClosestDistance(double distance, ParkingArea parkingArea,String key){
        this.distance=distance;
        this.parkingArea=parkingArea;
        this.key=key;
    }

    /*Comparator for sorting the list by roll no*/
    public static Comparator<ClosestDistance> ClosestDistComparator = new Comparator<ClosestDistance>() {
        public int compare(ClosestDistance s1, ClosestDistance s2) {
            /*For ascending order*/
            return Double.compare(s1.distance,s2.distance);
            /*For descending order*/
            //rollno2-rollno1;
        }
    };
}
