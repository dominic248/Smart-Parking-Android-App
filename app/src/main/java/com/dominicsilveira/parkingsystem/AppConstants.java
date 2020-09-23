package com.dominicsilveira.parkingsystem;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppConstants extends Application {
    public static final int LOCATION_REQUEST = 1000;
    public static final int GPS_REQUEST = 1001;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int NUMBER_PLATE_POPUP_REQUEST_CODE = 1002;
    public static final int RESTART_SERVICE_REQUEST_CODE = 1003;
    public static final int NOTIFICATION_GROUP_REQUEST_CODE = 1004;

    private int userType;

    public int getUserType(){
        return userType;
    }

    public void setUserType(int userType){
        this.userType=userType;
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
