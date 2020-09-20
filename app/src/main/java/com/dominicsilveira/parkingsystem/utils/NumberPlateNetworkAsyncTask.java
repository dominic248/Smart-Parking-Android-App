package com.dominicsilveira.parkingsystem.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NumberPlateNetworkAsyncTask extends AsyncTask<Void, Void, String> {
    public Bitmap bitmap;
    public String response;

    public AsyncResponse delegate = null;

    public NumberPlateNetworkAsyncTask(AsyncResponse delegate,Bitmap image){
        super();
        this.bitmap=image;
        this.delegate = delegate;
    }

    public interface AsyncResponse {
        void NumberPlateNetworkAsyncTaskCallback(String output) throws JSONException;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL("https://api.platerecognizer.com/v1/plate-reader");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Token 0bd1219a5d0dfc9c5a4a633af1e3e9dd74fb882b");
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            String base64String = "data:image/png;base64," + Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            List<NameValuePair> param = new ArrayList<NameValuePair>();
            param.add(new BasicNameValuePair("upload", base64String));
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(param));
            writer.flush();
            writer.close();
            os.close();
            conn.connect();
            int status=conn.getResponseCode();
            Log.e("ImageUploader", String.valueOf(status));
            Scanner result = new Scanner(conn.getInputStream());
            response = result.nextLine();
            Log.e("ImageUploader", "Error uploading image: " + response);
            conn.disconnect();
        } catch (Exception e) {
            Log.e("ImageUploader", "Error", e);
        }
        return response;
    }

    protected void onPostExecute(String result) {//***HERE
        try {
            delegate.NumberPlateNetworkAsyncTaskCallback(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}