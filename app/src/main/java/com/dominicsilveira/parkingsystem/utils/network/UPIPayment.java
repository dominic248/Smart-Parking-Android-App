package com.dominicsilveira.parkingsystem.utils.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.NormalUser.BookParkingAreaActivity;
import com.dominicsilveira.parkingsystem.utils.AppConstants;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class UPIPayment {
    public Boolean payUsingUpi(String amount, String upiId, String name, String note, Context context) {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();
        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);
        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
        // check if intent resolves
        if(null != chooser.resolveActivity(context.getPackageManager())){
            ((Activity) context).startActivityForResult(chooser, AppConstants.UPI_PAYMENT);
            return false;
        }else{
            Toast.makeText(context,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
            return false;
        }

    }


    public Boolean upiPaymentDataOperation(Map<String, String> myMap, Context context) {
//        https://stackoverflow.com/a/10514517
        if (isConnectionAvailable(context)) {
            String approvalRefNo = "";
            if (Objects.equals(myMap.get("status"), "success")) {
                //Code to handle successful transaction here.
                if(myMap.get("approvalrefno")!=null){
                    approvalRefNo=myMap.get("approvalrefno");
                }else if(myMap.get("txnref")!=null){
                    approvalRefNo=myMap.get("txnref");
                }else{
                    approvalRefNo="";
                }
                Toast.makeText(context, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPIPay", "responseStr: "+approvalRefNo);
                return true;
            } else {
                Toast.makeText(context, "Transaction failed. Please try again!", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(context, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }
}
