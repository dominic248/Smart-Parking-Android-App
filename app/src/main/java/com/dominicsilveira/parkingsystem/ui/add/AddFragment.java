package com.dominicsilveira.parkingsystem.ui.add;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dominicsilveira.parkingsystem.AppConstants;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.common.NumberPlatePopUp;
import com.dominicsilveira.parkingsystem.utils.network.NumberPlateNetworkAsyncTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddFragment extends Fragment implements NumberPlatePopUp.NumberPlatePopUpListener,NumberPlateNetworkAsyncTask.AsyncResponse {
    Bitmap upload;

    TextView placeText,coordText,slotNoText,numberPlate,amountText,wheelerText;
    TextView startBtn, endBtn;
    Button cameraBtn,bookBtn;
    EditText emailText;

    FirebaseAuth auth;
    FirebaseDatabase db;

    Date startDateTime,endDateTime;
    String placeID;
    ParkingArea parkingArea;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        cameraBtn=root.findViewById(R.id.scanBtn);
        placeText = root.findViewById(R.id.placeText);
        coordText = root.findViewById(R.id.coordText);
        slotNoText = root.findViewById(R.id.slotNoText);
        numberPlate = root.findViewById(R.id.numberPlate);
        startBtn = root.findViewById(R.id.startBtn);
        endBtn = root.findViewById(R.id.endBtn);
        bookBtn = root.findViewById(R.id.bookBtn);
        amountText=root.findViewById(R.id.amountText);
        wheelerText=root.findViewById(R.id.wheelerText);
        emailText=root.findViewById(R.id.emailText);

        startBtn.setInputType(InputType.TYPE_NULL);
        endBtn.setInputType(InputType.TYPE_NULL);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTIme(startBtn,false);
            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTIme(endBtn,true);
            }
        });


        cameraBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                askCameraPermission();
                Toast.makeText(getActivity(),"Camera Btn clicked",Toast.LENGTH_SHORT).show();
            }
        });

        bookBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                saveData();
            }
        });

        db.getReference().child("ParkingAreas").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        ParkingArea parkingArea = snapshot.getValue(ParkingArea.class);
                        setAddValues(parkingArea,snapshot.getKey());
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        db.getReference().child("ParkingAreas").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ParkingArea parkingArea = dataSnapshot.getValue(ParkingArea.class);
                            setAddValues(parkingArea,dataSnapshot.getKey());
                            Log.e("CalledTwice","12");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        return root;
    }

    private void setAddValues(ParkingArea parkingArea,String placeID) {
        this.placeID=placeID;
        this.parkingArea=parkingArea;
        placeText.setText(parkingArea.name);
        coordText.setText(String.valueOf(parkingArea.latitude).concat(", ").concat(String.valueOf(parkingArea.longitude)));
    }

    private void showDateTIme(final TextView button, final boolean end) {
        final Calendar calendar=Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, final int date) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,date);

                TimePickerDialog.OnTimeSetListener timeSetListener= new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY,hour);
                        calendar.set(Calendar.MINUTE,minute);
                        calendar.set(Calendar.SECOND, 0);
                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        button.setText(simpleDateFormat.format(calendar.getTime()));
                        if(end){
                            endDateTime = calendar.getTime();
                        }else{
                            startDateTime = calendar.getTime();
                        }
                        if(endDateTime!=null && startDateTime!=null){
                            if(endDateTime.after(startDateTime)){
                                Toast.makeText(getActivity(),
                                        "after"+calendar.get(Calendar.HOUR_OF_DAY), Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getActivity(),
                                        "before"+calendar.get(Calendar.HOUR_OF_DAY), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                };
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(),timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
                timePickerDialog.show();
            }
        };
        DatePickerDialog datePickerDialog=new DatePickerDialog(getActivity(),dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[] {Manifest.permission.CAMERA}, AppConstants.CAMERA_PERM_CODE);
        }else{
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(),new String[] {Manifest.permission.INTERNET},103);
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
                Toast.makeText(getActivity(),"Camera Permission Required",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Toast.makeText(getActivity(),"Camera Open Request",Toast.LENGTH_SHORT).show();
        Intent camera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, AppConstants.CAMERA_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.CAMERA_REQUEST_CODE) {
            try {
                upload = (Bitmap) data.getExtras().get("data");
                NumberPlateNetworkAsyncTask task=new NumberPlateNetworkAsyncTask(this,upload);
                task.execute();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        if(requestCode==AppConstants.NUMBER_PLATE_POPUP_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                int wheelerType=data.getIntExtra("wheelerType",4);
                String vehicleNumber=data.getStringExtra("vehicleNumber");
                String wheelerTypeStr=String.valueOf(wheelerType);
                long diffInMillies = Math.abs(endDateTime.getTime() - startDateTime.getTime());
                long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                int wheelerAmount;
                if(wheelerType==2)
                    wheelerAmount=parkingArea.amount2;
                else if(wheelerType==3)
                    wheelerAmount=parkingArea.amount3;
                else
                    wheelerAmount=parkingArea.amount4;
                int amount=(int)diff*wheelerAmount;
                String amountStr=String.valueOf(amount);
                numberPlate.setText(vehicleNumber);
                amountText.setText(amountStr);
                wheelerText.setText(wheelerTypeStr);
            }else if (resultCode == Activity.RESULT_CANCELED) {
                //Do Something in case not recieved the data
            }
        }

    }

    private void saveData() {
        final int wheelerInt=Integer.parseInt(wheelerText.getText().toString()),amountInt=Integer.parseInt(amountText.getText().toString());
        db.getReference().child("Users").orderByChild("email").equalTo(emailText.getText().toString()).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String userID = dataSnapshot.getKey();
                                final BookedSlots bookingSlot=new BookedSlots(userID,placeID,numberPlate.getText().toString(),wheelerInt,startDateTime,endDateTime,0,amountInt,Math.abs((int)Calendar.getInstance().getTimeInMillis()),0);
                                final String key=db.getReference("BookedSlots").push().getKey();
                                if(parkingArea.availableSlots>0){
                                    parkingArea.availableSlots-=1;
                                    parkingArea.occupiedSlots+=1;
                                    db.getReference("ParkingAreas").child(placeID).setValue(parkingArea).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    db.getReference("BookedSlots").child(key).setValue(bookingSlot).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(getActivity(),"Success",Toast.LENGTH_SHORT).show();
                                                            }else{
                                                                Toast.makeText(getActivity(),"Failed",Toast.LENGTH_SHORT).show();
                                                                parkingArea.availableSlots+=1;
                                                                parkingArea.occupiedSlots-=1;
                                                                db.getReference("ParkingAreas").child(placeID).setValue(parkingArea);
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                    });
                                }
                            }
                        }else{
                            Toast.makeText(getActivity(),"User Doesn't exist",Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    public void NumberPlateNetworkAsyncTaskCallback(String result) throws JSONException {
        JSONObject obj = new JSONObject(result);
        JSONArray geodata = obj.getJSONArray("results");
        Bundle args = new Bundle();
        args.putString("numberPlate", geodata.getJSONObject(0).getString("plate"));
        NumberPlatePopUp numberPlateDialog = new NumberPlatePopUp();
        numberPlateDialog.setTargetFragment(AddFragment.this, AppConstants.NUMBER_PLATE_POPUP_REQUEST_CODE);
        numberPlateDialog.setArguments(args);
        numberPlateDialog.show(getParentFragmentManager(), "exampledialog");
        Log.e("ImageUploader", geodata.getJSONObject(0).getString("plate"));
    }
}