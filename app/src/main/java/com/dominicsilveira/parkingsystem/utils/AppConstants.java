package com.dominicsilveira.parkingsystem.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dominicsilveira.parkingsystem.classes.User;

public class AppConstants extends Application {
    public static final int LOCATION_REQUEST_CODE = 100;
    public static final int GPS_REQUEST_CODE = 101;
    public static final int CAMERA_PERM_CODE = 102;
    public static final int CAMERA_REQUEST_CODE = 103;
    public static final int INTERNET_PERM_CODE = 104;
    public static final int WRITE_EXTERNAL_STORAGE_PERM_CODE = 105;
    public static final int NUMBER_PLATE_POPUP_REQUEST_CODE = 106;
    public static final int RESTART_SERVICE_REQUEST_CODE = 107;
    public static final int NOTIFICATION_GROUP_REQUEST_CODE = 108;
    public static final int SCAN_PERMISSION_ALL = 109;
    public static final int UPI_PAYMENT  = 110;



    private User userObj;

    public User getUserObj(){
        return userObj;
    }

    public void setUserObj(User userObj){
        this.userObj=userObj;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // register to be informed of activities starting up
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                Log.i(String.valueOf(activity.getComponentName().getClassName()),"Application onActivityDestroyed");
            }
        });

    } //End of onCreate
}
