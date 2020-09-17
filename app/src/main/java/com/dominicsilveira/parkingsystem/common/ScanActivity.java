package com.dominicsilveira.parkingsystem.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.NetworkAsyncTask;
import com.dominicsilveira.parkingsystem.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class ScanActivity extends AppCompatActivity implements NetworkAsyncTask.AsyncResponse {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    ImageView selectedImage;
    FloatingActionButton cameraBtn;
    Bitmap upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        selectedImage=findViewById(R.id.displayImage);
        cameraBtn=findViewById(R.id.addVehicle);

        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.scan);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.dashboard:
                        startActivity(new Intent(getApplicationContext(),
                                DashboardActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.scan:
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(),
                                ProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });


        cameraBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                askCameraPermission();
                Toast.makeText(ScanActivity.this,"Camera Btn clicked",Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},CAMERA_PERM_CODE);
        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.INTERNET},103);
            }else{
                openCamera();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==CAMERA_PERM_CODE){
            if(grantResults.length<0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                openCamera();
            }else{
                Toast.makeText(this,"Camera Permission Required",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Toast.makeText(this,"Camera Open Request",Toast.LENGTH_SHORT).show();
        Intent camera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            try {
                upload = (Bitmap) data.getExtras().get("data");
//                selectedImage.setImageBitmap(upload);
                NetworkAsyncTask task=new NetworkAsyncTask(this,upload);
                task.execute();
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void myMethod(String result) throws JSONException {
        JSONObject obj = new JSONObject(result);
        JSONArray geodata = obj.getJSONArray("results");
        Intent intent=new Intent(ScanActivity.this,NumberPlateActivity.class);
        intent.putExtra("image", upload);
        intent.putExtra("numberPlate", geodata.getJSONObject(0).getString("plate"));
        startActivity(intent);
        Log.e("ImageUploader", geodata.getJSONObject(0).getString("plate"));
    }


//    public class NetworkAsyncTask extends AsyncTask<Void, Void, String> {
//        public Bitmap bitmap;
//        public String response;
//
//        public NetworkAsyncTask(Bitmap image){
//            super();
//            bitmap=image;
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            try {
//                URL url = new URL("https://api.platerecognizer.com/v1/plate-reader");
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestProperty("Authorization", "Token 0bd1219a5d0dfc9c5a4a633af1e3e9dd74fb882b");
//                conn.setDoInput(true);
//                conn.setRequestMethod("POST");
//                conn.setDoOutput(true);
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//                String base64String = "data:image/png;base64," + Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
//                List<NameValuePair> param = new ArrayList<NameValuePair>();
//                param.add(new BasicNameValuePair("upload", base64String));
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(
//                        new OutputStreamWriter(os, "UTF-8"));
//                writer.write(getQuery(param));
//                writer.flush();
//                writer.close();
//                os.close();
//                conn.connect();
//                int status=conn.getResponseCode();
//                Log.e("ImageUploader", String.valueOf(status));
//                Scanner result = new Scanner(conn.getInputStream());
//                response = result.nextLine();
//                Log.e("ImageUploader", "Error uploading image: " + response);
//                conn.disconnect();
//            } catch (Exception e) {
//                Log.e("ImageUploader", "Error", e);
//            }
//            return response;
//        }
//
//        protected void onPostExecute(String result) {//***HERE
//            try {
//                myMethod(result);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
//            StringBuilder result = new StringBuilder();
//            boolean first = true;
//            for (NameValuePair pair : params) {
//                if (first)
//                    first = false;
//                else
//                    result.append("&");
//                result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
//                result.append("=");
//                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
//            }
//            return result.toString();
//        }
//    }
}