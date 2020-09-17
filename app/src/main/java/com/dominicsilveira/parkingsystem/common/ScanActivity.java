package com.dominicsilveira.parkingsystem.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.AppConstants;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.utils.CloseLocationAdapter;
import com.dominicsilveira.parkingsystem.utils.NumberPlateAdapter;
import com.dominicsilveira.parkingsystem.utils.NumberPlateNetworkAsyncTask;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class ScanActivity extends AppCompatActivity implements NumberPlateNetworkAsyncTask.AsyncResponse {

    ImageView selectedImage;
    FloatingActionButton cameraBtn;
    Bitmap upload;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    FirebaseAuth auth;
    FirebaseDatabase db;

    Map<String, NumberPlate> numberPlatesList = new HashMap<String, NumberPlate>();
    Map<String, NumberPlate> treeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        cameraBtn=findViewById(R.id.addVehicle);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(ScanActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

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
//                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        return true;
                    case R.id.scan:
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(),
                                ProfileActivity.class));
                        overridePendingTransition(0,0);
//                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
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

        db.getReference().child("NumberPlates").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NumberPlate numberPlate = dataSnapshot.getValue(NumberPlate.class);
                            numberPlatesList.put(dataSnapshot.getKey(),numberPlate);
                            treeMap = new TreeMap<String, NumberPlate>(numberPlatesList);
                            mAdapter = new NumberPlateAdapter(treeMap);
                            recyclerView.setAdapter(mAdapter);
                        }
                        Log.d("GPS Map", String.valueOf(numberPlatesList));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }



    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, AppConstants.CAMERA_PERM_CODE);
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
        if(requestCode==AppConstants.CAMERA_PERM_CODE){
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
        startActivityForResult(camera, AppConstants.CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.CAMERA_REQUEST_CODE) {
            try {
                upload = (Bitmap) data.getExtras().get("data");
//                selectedImage.setImageBitmap(upload);
                NumberPlateNetworkAsyncTask task=new NumberPlateNetworkAsyncTask(this,upload);
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