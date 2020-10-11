package com.dominicsilveira.parkingsystem.NormalUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.utils.pdf.InvoiceGenerator;
import com.dominicsilveira.parkingsystem.utils.notifications.NotificationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookingDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    TextView placeText,wheelerText,amountText,endDateText,endTimeText,startDateText,startTimeText,numberPlateSpinner;
    FloatingActionButton checkoutBtn;
    LinearLayout openInvoicePdf;
    ImageView shareInvoicePdf;

    FirebaseAuth auth;
    FirebaseDatabase db;

    SupportMapFragment supportMapFragment;
    GoogleMap gMap;
    LatLng globalLatLng=null;
    String UUID;
    MarkerOptions options;

    BookedSlots bookingSlot;
    User userObj;
    NotificationHelper mNotificationHelper;
    AppConstants globalClass;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET,
    };

    ParkingArea parkingArea;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        Bundle bundle = getIntent().getExtras();
        UUID=bundle.getString("UUID");
        bookingSlot = (BookedSlots) getIntent().getSerializableExtra("BookedSlot");

        askCameraFilePermission();

        globalClass=(AppConstants)getApplicationContext();
        userObj=globalClass.getUserObj();

        placeText = findViewById(R.id.placeText);
        numberPlateSpinner = findViewById(R.id.vehicleSelect);
        endDateText = findViewById(R.id.endDateText);
        endTimeText = findViewById(R.id.endTimeText);
        startDateText = findViewById(R.id.startDateText);
        startTimeText = findViewById(R.id.startTimeText);
//        openInvoicePdf = findViewById(R.id.openInvoicePdf);
//        shareInvoicePdf = findViewById(R.id.shareInvoicePdf);

        checkoutBtn = findViewById(R.id.checkoutBtn);
        wheelerText = findViewById(R.id.wheelerText);
        amountText = findViewById(R.id.amountText);

        mNotificationHelper=new NotificationHelper(this);

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
        startTimeText.setText(simpleDateFormat.format(bookingSlot.startTime));
        endTimeText.setText(simpleDateFormat.format(bookingSlot.endTime));
        simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
        startDateText.setText(simpleDateFormat.format(bookingSlot.startTime));
        endDateText.setText(simpleDateFormat.format(bookingSlot.endTime));
        numberPlateSpinner.setText(bookingSlot.numberPlate);
        wheelerText.setText(String.valueOf(bookingSlot.wheelerType));
        amountText.setText(String.valueOf(bookingSlot.amount));

        findViewById(R.id.openInvoicePdf).setOnClickListener(BookingDetailsActivity.this);
        findViewById(R.id.shareInvoicePdf).setOnClickListener(BookingDetailsActivity.this);
//implement you java class with View.OnClickListener interface and override onClick method

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        db.getReference().child("ParkingAreas").child(bookingSlot.placeID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        parkingArea.setData(snapshot.getKey(),snapshot.getValue(int.class));
                        Log.e("CalledTwice", String.valueOf(snapshot.getKey())+snapshot.getValue(int.class));
                    }
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        db.getReference().child("ParkingAreas").child(bookingSlot.placeID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ParkingArea parkingArea = snapshot.getValue(ParkingArea.class);
                        setAddValues(parkingArea,snapshot.getKey());
                        Log.e("CalledTwice", String.valueOf(snapshot.getKey()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public void onClick(View view) {
        InvoiceGenerator invoiceGenerator=new InvoiceGenerator();
        switch(view.getId()){
            case R.id.openInvoicePdf:
                Toast.makeText(BookingDetailsActivity.this, "Open File", Toast.LENGTH_SHORT).show();
                invoiceGenerator.downloadFile(bookingSlot.userID,UUID);
                invoiceGenerator.openFile(BookingDetailsActivity.this);
                break;
            case R.id.shareInvoicePdf:
                Toast.makeText(BookingDetailsActivity.this, "Share File", Toast.LENGTH_SHORT).show();
                invoiceGenerator.downloadFile(bookingSlot.userID,UUID);
                invoiceGenerator.shareFile(BookingDetailsActivity.this);
                break;
        }
    }

    private void setAddValues(ParkingArea parkingArea,String placeID) {
        bookingSlot.placeID=placeID;
        this.parkingArea=parkingArea;
        placeText.setText(parkingArea.name);
        globalLatLng=new LatLng(parkingArea.latitude,parkingArea.longitude);
        options=new MarkerOptions().position(globalLatLng)
                .title(parkingArea.name);
        supportMapFragment=(SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap=googleMap;
                gMap.clear();
                gMap.addMarker(options);
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(globalLatLng,30));
            }
        });
    }

    private void saveData() {
        bookingSlot.notificationID=Math.abs((int)Calendar.getInstance().getTimeInMillis());
        final String key=db.getReference("BookedSlots").push().getKey();
        if(parkingArea.availableSlots>0){
            parkingArea.availableSlots-=1;
            parkingArea.occupiedSlots+=1;
            db.getReference("ParkingAreas").child(bookingSlot.placeID).setValue(parkingArea).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        db.getReference("BookedSlots").child(key).setValue(bookingSlot).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(BookingDetailsActivity.this,"Success",Toast.LENGTH_SHORT).show();
                                    File file = new File(Environment.getExternalStorageDirectory()
                                            + File.separator + "invoice.pdf");
                                    InvoiceGenerator invoiceGenerator=new InvoiceGenerator(bookingSlot,parkingArea,key,userObj,file);
                                    invoiceGenerator.create();
                                    Intent intent = new Intent(BookingDetailsActivity.this, MainNormalActivity.class);
                                    intent.putExtra("FRAGMENT_NO", 0);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(BookingDetailsActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                                    parkingArea.availableSlots+=1;
                                    parkingArea.occupiedSlots-=1;
                                    db.getReference("ParkingAreas").child(bookingSlot.placeID).setValue(parkingArea);
                                }
                            }
                        });
                    }
                }
            });
        }else {
            Toast.makeText(BookingDetailsActivity.this,"Failed! Slots are full.",Toast.LENGTH_SHORT).show();
        }
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

    private void askCameraFilePermission() {
        if (!hasPermissions(BookingDetailsActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(BookingDetailsActivity.this, PERMISSIONS, AppConstants.SCAN_PERMISSION_ALL);
        }else{
//            openCamera();
        }
    }
}