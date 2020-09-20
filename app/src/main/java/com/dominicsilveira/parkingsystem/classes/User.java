package com.dominicsilveira.parkingsystem.classes;

import java.io.Serializable;

public class User implements Serializable {

    public int userType;
    public String name,email;
    public String contact_no;

    public User(){}

    public User(String name,String email, String contact_no, int userType){
        this.name=name;
        this.contact_no=contact_no;
        this.email=email;
        this.userType=userType;
    }
}
