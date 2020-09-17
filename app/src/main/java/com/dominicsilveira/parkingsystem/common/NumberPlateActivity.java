package com.dominicsilveira.parkingsystem.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.AppConstants;
import com.dominicsilveira.parkingsystem.OwnerUser.RegisterAreaActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.utils.NumberPlateNetworkAsyncTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NumberPlateActivity extends AppCompatActivity  implements NumberPlateNetworkAsyncTask.AsyncResponse{

    ImageView imageView;
    EditText numberPlateText;
    Button takeAgainBtn,saveBtn;
    Bitmap upload;
    private FirebaseAuth auth;
    private FirebaseDatabase db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_plate);
        imageView=findViewById(R.id.numberPlateImage);
        numberPlateText=findViewById(R.id.numberPlateText);
        takeAgainBtn=findViewById(R.id.takeAgainBtn);
        saveBtn=findViewById(R.id.saveBtn);

        auth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("image");
        imageView.setImageBitmap(bitmap);
        String numberPlate = intent.getStringExtra("numberPlate");
        numberPlateText.setText(numberPlate);

        takeAgainBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                askCameraPermission();
                Toast.makeText(NumberPlateActivity.this,"Camera Btn clicked",Toast.LENGTH_SHORT).show();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String number = numberPlateText.getText().toString();
                NumberPlate numberPlate = new NumberPlate(number,auth.getCurrentUser().getUid());
                String key=db.getReference("NumberPlates").push().getKey();
                db.getReference("NumberPlates")
                        .child(key)
                        .setValue(numberPlate).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(NumberPlateActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(NumberPlateActivity.this, MainActivity.class);
                            intent.putExtra("FRAGMENT_NO", 1);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(NumberPlateActivity.this, "Failed to add extra details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
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
//                imageView.setImageBitmap(upload);
                NumberPlateNetworkAsyncTask task=new NumberPlateNetworkAsyncTask(this,upload);
                task.execute();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void myMethod(String result) throws JSONException {
        JSONObject obj = new JSONObject(result);
        JSONArray geodata = obj.getJSONArray("results");
        imageView.setImageBitmap(upload);
        numberPlateText.setText(geodata.getJSONObject(0).getString("plate"));
        Log.e("ImageUploader", geodata.getJSONObject(0).getString("plate"));
    }
}