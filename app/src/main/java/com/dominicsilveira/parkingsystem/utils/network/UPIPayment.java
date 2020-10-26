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



    public Boolean upiPaymentDataOperation(ArrayList<String> data, Context context) {
        if (isConnectionAvailable(context)) {
            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase()))
                        status = equalStr[1].toLowerCase();
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase()))
                        approvalRefNo = equalStr[1];
                }
                else {
                    paymentCancel = "Payment cancelled by user-1.";
                }
            }
            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(context, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPIPay", "responseStr: "+approvalRefNo);
                return true;
            } else if("Payment cancelled by user.".equals(paymentCancel)){
                Toast.makeText(context, "Payment cancelled by user-2.", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Toast.makeText(context, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
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
