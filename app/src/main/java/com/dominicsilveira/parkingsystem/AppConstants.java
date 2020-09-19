package com.dominicsilveira.parkingsystem;

import android.app.Application;

public class AppConstants extends Application {
    public static final int LOCATION_REQUEST = 1000;
    public static final int GPS_REQUEST = 1001;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int NUMBER_PLATE_POPUP_REQUEST_CODE = 1002;

    private int userType;

    public int getUserType(){
        return userType;
    }

    public void setUserType(int userType){
        this.userType=userType;
    }
}
