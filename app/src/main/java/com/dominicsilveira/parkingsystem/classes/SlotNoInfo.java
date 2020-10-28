package com.dominicsilveira.parkingsystem.classes;


import java.io.Serializable;

public class SlotNoInfo implements Serializable {
    public String name,numberPlate;
    public boolean isFull;

    public SlotNoInfo(){}

    public SlotNoInfo(String name, boolean isFull){
        this.name=name;
        this.isFull=isFull;
        this.numberPlate="NONE";
    }
}
