package com.dominicsilveira.parkingsystem;

import java.io.Serializable;

public class User implements Serializable {

    public int userType;

    public User(){}

    public User(int userType){
        this.userType=userType;
    }
}
