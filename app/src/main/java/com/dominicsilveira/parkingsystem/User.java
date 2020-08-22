package com.dominicsilveira.parkingsystem;

import java.io.Serializable;

public class User implements Serializable {

    public int userType;
    public String name;
    public String contact_no;

    public User(){}

    public User(String name, String contact_no, int userType){
        this.name=name;
        this.contact_no=contact_no;
        this.userType=userType;
    }
}
