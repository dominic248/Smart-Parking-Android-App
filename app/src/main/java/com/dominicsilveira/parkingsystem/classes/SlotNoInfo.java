package com.dominicsilveira.parkingsystem.classes;


import java.io.Serializable;

public class SlotNoInfo implements Serializable {
    public String name;
    public boolean isFull;

    public SlotNoInfo(){}

    public SlotNoInfo(String name, boolean isFull){
        this.name=name;
        this.isFull=isFull;
    }
}
