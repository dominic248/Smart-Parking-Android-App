package com.dominicsilveira.parkingsystem.classes;

import java.io.Serializable;

public class User implements Serializable {

    public int userType,isVerified;
    public String name,email,contact_no;

    public User(){}

    public User(String name,String email, String contact_no, int userType,int isVerified){
        this.name=name;
        this.contact_no=contact_no;
        this.email=email;
        this.userType=userType;
        this.isVerified=isVerified;
    }
}
