package com.dominicsilveira.parkingsystem.ui.add;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dominicsilveira.parkingsystem.NormalUser.BookParkingAreaActivity;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.InvoiceGenerator;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.utils.dialog.NumberPlatePopUp;
import com.dominicsilveira.parkingsystem.utils.network.ApiService;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddFragment extends Fragment implements NumberPlatePopUp.NumberPlatePopUpListener {
    Bitmap upload;

    TextView slotNoText,numberPlate,amountText,wheelerText;
    TextView endDateText,endTimeText;
    LinearLayout endDate,endTime,scanBtn;
    Button bookBtn;
    EditText emailText;
    TextView placeText;

    FirebaseAuth auth;
    FirebaseDatabase db;

    Date startDateTime,endDateTime;
    String placeID;
    ParkingArea parkingArea;
    Calendar calendar;
    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET,
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        scanBtn=root.findViewById(R.id.scanBtn);
        placeText = root.findViewById(R.id.placeText);
        slotNoText = root.findViewById(R.id.slotNoText);
        numberPlate = root.findViewById(R.id.numberPlate);
        endDate = root.findViewById(R.id.endDate);
        endTime = root.findViewById(R.id.endTime);
        endDateText = root.findViewById(R.id.endDateText);
        endTimeText = root.findViewById(R.id.endTimeText);
//        endDateText.setInputType(InputType.TYPE_NULL);
//        endTimeText.setInputType(InputType.TYPE_NULL);
        bookBtn = root.findViewById(R.id.bookBtn);
        amountText=root.findViewById(R.id.amountText);
        wheelerText=root.findViewById(R.id.wheelerText);
        emailText=root.findViewById(R.id.emailText);

        calendar=new GregorianCalendar();
        startDateTime=endDateTime=calendar.getTime();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
        endTimeText.setText(simpleDateFormat.format(startDateTime));
        simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
        endDateText.setText(simpleDateFormat.format(startDateTime));

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(endDateText);
            }
        });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(endTimeText);
            }
        });


        scanBtn.setOnClickListener(new View.OnClickListener(){
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
    }

    private void showDatePicker(final TextView button) {
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, final int date) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,date);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
                button.setText(simpleDateFormat.format(calendar.getTime()));
                endDateTime = calendar.getTime();
            }
        };
        DatePickerDialog datePickerDialog=new DatePickerDialog(getActivity(),dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker(final TextView button) {
        TimePickerDialog.OnTimeSetListener timeSetListener= new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY,hour);
                calendar.set(Calendar.MINUTE,minute);
                calendar.set(Calendar.SECOND, 0);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
                endDateTime = calendar.getTime();
                if(endDateTime.after(startDateTime)){
                    button.setText(simpleDateFormat.format(calendar.getTime()));
                    endDateTime = calendar.getTime();
                }else{
                    endDateTime = startDateTime;
                    Toast.makeText(getActivity(),
                            "Please select a time after Present time!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(),timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
        timePickerDialog.show();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void askCameraPermission() {
        if (!hasPermissions(getActivity(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, AppConstants.SCAN_PERMISSION_ALL);
        }else{
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if(requestCode==AppConstants.SCAN_PERMISSION_ALL){
//            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
//                openCamera();
//            }
//        }
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
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                upload.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                String fileName="testimage.jpg";
                final File file = new File(Environment.getExternalStorageDirectory()
                        + File.separator + fileName);
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(outputStream.toByteArray());
                fo.close();
//                Uri yourUri = Uri.fromFile(file);
                OkHttpClient client = new OkHttpClient.Builder().build();
                ApiService apiService = new Retrofit.Builder().baseUrl("https://api.platerecognizer.com").client(client).build().create(ApiService.class);
                RequestBody reqFile = RequestBody.create(okhttp3.MediaType.parse("image/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("upload",
                        file.getName(), reqFile);
                RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload");
                Call<ResponseBody> req = apiService.postImage(body, name,"Token 0bd1219a5d0dfc9c5a4a633af1e3e9dd74fb882b");
                req.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Toast.makeText(getActivity(), response.code() + " ", Toast.LENGTH_SHORT).show();
                        try {
                            String resp=response.body().string();
                            Log.i("LogPlateRespon", resp);
                            JSONObject obj = new JSONObject(resp); //response.body().string() fetched only once
                            JSONArray geodata = obj.getJSONArray("results");
                            Bundle args = new Bundle();
                            args.putString("numberPlate", geodata.getJSONObject(0).getString("plate"));
                            NumberPlatePopUp numberPlateDialog = new NumberPlatePopUp();
                            numberPlateDialog.setTargetFragment(AddFragment.this, AppConstants.NUMBER_PLATE_POPUP_REQUEST_CODE);
                            numberPlateDialog.setArguments(args);
                            numberPlateDialog.show(getParentFragmentManager(), "exampledialog");
                            Log.e("ImageUploader", geodata.getJSONObject(0).getString("plate"));
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }

                        file.delete();
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getActivity(), "Request failed", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
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
//        final int wheelerInt=Integer.parseInt(wheelerText.getText().toString()),amountInt=Integer.parseInt(amountText.getText().toString());
        db.getReference().child("Users").orderByChild("email").equalTo(emailText.getText().toString()).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String userID = dataSnapshot.getKey();
                                final User userObj=dataSnapshot.getValue(User.class);
//                                final BookedSlots bookingSlot=new BookedSlots(userID,placeID,numberPlate.getText().toString(),wheelerInt,startDateTime,endDateTime,0,amountInt,Math.abs((int)Calendar.getInstance().getTimeInMillis()),0);
                                final BookedSlots bookingSlot=new BookedSlots(userID,placeID,"xfbhjk".toString(),4,
                                        startDateTime,endDateTime,0,1,Math.abs((int)Calendar.getInstance().getTimeInMillis()),0);
                                auth = FirebaseAuth.getInstance();
                                db = FirebaseDatabase.getInstance();
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
                                                            File file = new File(Environment.getExternalStorageDirectory()
                                                                    + File.separator + "invoice.pdf");
                                                            InvoiceGenerator invoiceGenerator=new InvoiceGenerator(bookingSlot,parkingArea,key,userObj,file);
                                                            invoiceGenerator.create();
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
                                }else {
                                    Toast.makeText(getActivity(),"Failed! Slots are full.",Toast.LENGTH_SHORT).show();
                                }

//                                Intent target = new Intent(Intent.ACTION_VIEW);
//                                target.setDataAndType(Uri.fromFile(file),"application/pdf");
//                                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//
//                                Intent intent = Intent.createChooser(target, "Open File");
//                                try {
//                                    startActivity(intent);
//                                } catch (ActivityNotFoundException e) {
//                                    // Instruct the user to install a PDF reader here, or something
//                                }

//                                Intent share = new Intent(Intent.ACTION_SEND);
//                                if(file.exists()) {
//                                    share.setType("application/pdf");
//                                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//                                    share.putExtra(Intent.EXTRA_SUBJECT,
//                                            "Sharing File...");
//                                    share.putExtra(Intent.EXTRA_TEXT, "Sharing File...");
//                                    startActivity(Intent.createChooser(share, "Share File"));
//                                }
                            }
                        }else{
                            Toast.makeText(getActivity(),"User Doesn't exist",Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

}