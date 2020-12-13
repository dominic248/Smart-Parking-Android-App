package com.dominicsilveira.parkingsystem.NormalUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.classes.UpiInfo;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.common.BookingDetailsActivity;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
import com.dominicsilveira.parkingsystem.utils.network.UPIPayment;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BookParkingAreaActivity extends AppCompatActivity {
    Spinner numberPlateSpinner;
    TextView placeText,wheelerText,amountText,endDateText,endTimeText;
    FloatingActionButton bookBtn;
    LinearLayout endDate,endTime;

    FirebaseAuth auth;
    FirebaseDatabase db;

    SupportMapFragment supportMapFragment;
    GoogleMap gMap;
    LatLng globalLatLng=null;
    MarkerOptions options;

    List<Integer> numberPlateWheeler = new ArrayList<Integer>();
    List<String> numberPlateNumber = new ArrayList<String>();

    Calendar calendar;

    BookedSlots bookingSlot=new BookedSlots();
    ParkingArea parkingArea;
    User userObj;
    NotificationHelper mNotificationHelper;
    AppConstants globalClass;

    BasicUtils utils=new BasicUtils();

    UpiInfo upiInfo;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET,
    };

    UPIPayment upiPayment=new UPIPayment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_parking_area);

        initComponents();
        attachListeners();

        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(BookParkingAreaActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
        }
        addItemsOnSpinner();
        addListenerOnSpinnerItemSelection();
        askCameraFilePermission();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
    }

    private void initComponents() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        getSupportActionBar().setTitle("Book Area");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        globalClass=(AppConstants)getApplicationContext();
        userObj=globalClass.getUserObj();

        placeText = findViewById(R.id.placeText);
        numberPlateSpinner = findViewById(R.id.vehicleSelect);
        endDate = findViewById(R.id.endDate);
        endTime = findViewById(R.id.endTime);
        endDateText = findViewById(R.id.endDateText);
        endTimeText = findViewById(R.id.endTimeText);
//        endDateText.setInputType(InputType.TYPE_NULL);
//        endTimeText.setInputType(InputType.TYPE_NULL);
        bookBtn = findViewById(R.id.bookBtn);
        wheelerText = findViewById(R.id.wheelerText);
        amountText = findViewById(R.id.amountText);
        mNotificationHelper=new NotificationHelper(this);

        calendar=new GregorianCalendar();
        bookingSlot.startTime=bookingSlot.endTime=bookingSlot.checkoutTime=calendar.getTime();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
        endTimeText.setText(simpleDateFormat.format(bookingSlot.endTime));
        simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
        endDateText.setText(simpleDateFormat.format(bookingSlot.endTime));
        bookingSlot.readNotification=0;
        bookingSlot.readBookedNotification=1;
        bookingSlot.hasPaid=0;
        bookingSlot.userID=auth.getCurrentUser().getUid();

        Bundle bundle = getIntent().getExtras();
        bookingSlot.placeID=bundle.getString("UUID");
        final ParkingArea parkingArea = (ParkingArea) getIntent().getSerializableExtra("ParkingArea");
        Log.e(String.valueOf(BookParkingAreaActivity.this.getClass()),"Fetched parking Area:"+ parkingArea.name+" "+bookingSlot.placeID);

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

    private void attachListeners() {
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
        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(numberPlateSpinner.getSelectedItemPosition()==0){
                    Toast.makeText(BookParkingAreaActivity.this, "Please select a vehicle!", Toast.LENGTH_SHORT).show();
                }else if(bookingSlot.endTime.equals(bookingSlot.startTime)){
                    Toast.makeText(BookParkingAreaActivity.this,
                                "Please set the end time!", Toast.LENGTH_SHORT).show();
                }else if(!bookingSlot.timeDiffValid()){
                    Toast.makeText(BookParkingAreaActivity.this,
                            "Less time difference (<15 minutes)!", Toast.LENGTH_SHORT).show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(BookParkingAreaActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("Confirm Booking");
                    builder.setMessage("Confirm Booking for this area?");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(parkingArea.availableSlots>0) {
                                        parkingArea.allocateSpace();
                                        db.getReference("ParkingAreas").child(bookingSlot.placeID).setValue(parkingArea);
                                        String note ="Payment for ".concat(bookingSlot.placeID).concat(" and number ").concat(bookingSlot.numberPlate);
                                        Boolean upi=upiPayment.payUsingUpi(String.valueOf(bookingSlot.amount), "micsilveira111@oksbi", "Michael", note,BookParkingAreaActivity.this);
//                                        Boolean upi=upiPayment.payUsingUpi(String.valueOf(bookingSlot.amount), upiInfo.upiId, upiInfo.upiName, note,BookParkingAreaActivity.this);
//                                        saveData();
                                    }else{
                                        Toast.makeText(BookParkingAreaActivity.this,"Failed! Slots are full.",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });


        db.getReference().child("ParkingAreas").child(bookingSlot.placeID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if(snapshot.getKey().equals("availableSlots") || snapshot.getKey().equals("occupiedSlots") || snapshot.getKey().equals("totalSlots")){
                            parkingArea.setData(snapshot.getKey(),snapshot.getValue(int.class));
                            Log.e(String.valueOf(BookParkingAreaActivity.this.getClass()),"Fetched updated parking Area:"+ String.valueOf(snapshot.getKey())+snapshot.getValue(int.class));
                        }
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
                        setAddValues(parkingArea);
                        db.getReference().child("UpiInfo").child(parkingArea.userID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                upiInfo=snapshot.getValue(UpiInfo.class);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    private void setAddValues(ParkingArea parkingArea) {
        this.parkingArea=parkingArea;
        placeText.setText(parkingArea.name);
    }

    private void saveData() {
        bookingSlot.notificationID=Math.abs((int)Calendar.getInstance().getTimeInMillis());
        final String key=db.getReference("BookedSlots").push().getKey();
        bookingSlot.slotNo=parkingArea.allocateSlot(bookingSlot.numberPlate);
        db.getReference("BookedSlots").child(key).setValue(bookingSlot).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    db.getReference("ParkingAreas").child(bookingSlot.placeID).setValue(parkingArea);
                    Toast.makeText(BookParkingAreaActivity.this,"Success",Toast.LENGTH_SHORT).show();
                    File file = new File(BookParkingAreaActivity.this.getExternalCacheDir(), File.separator + "invoice.pdf");
                    InvoiceGenerator invoiceGenerator=new InvoiceGenerator(bookingSlot,parkingArea,key,userObj,file);
                    invoiceGenerator.create();
                    invoiceGenerator.uploadFile(BookParkingAreaActivity.this,getApplication());
                    Intent intent = new Intent(BookParkingAreaActivity.this, MainNormalActivity.class);
                    intent.putExtra("FRAGMENT_NO", 0);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);  //slide from left to right
                    finish();
                }else{
                    Toast.makeText(BookParkingAreaActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                    parkingArea.deallocateSpace();
                    parkingArea.deallocateSlot(bookingSlot.slotNo);
                    db.getReference("ParkingAreas").child(bookingSlot.placeID).setValue(parkingArea);
                }
            }
        });
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
                bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                calcRefreshAmount();
            }
        };
        DatePickerDialog datePickerDialog=new DatePickerDialog(BookParkingAreaActivity.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
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
                bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                if(bookingSlot.endTime.after(bookingSlot.startTime)){
                    button.setText(simpleDateFormat.format(calendar.getTime()));
                    bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                    calcRefreshAmount();
                }else{
                    bookingSlot.endTime = bookingSlot.checkoutTime = bookingSlot.startTime;
                    Toast.makeText(BookParkingAreaActivity.this,
                            "Please select a time after Present time!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        TimePickerDialog timePickerDialog=new TimePickerDialog(BookParkingAreaActivity.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
        timePickerDialog.show();
    }

    public void addItemsOnSpinner() {
        numberPlateWheeler.add(0);
        numberPlateNumber.add("Select a vehicle");
        db.getReference().child("NumberPlates").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NumberPlate numberPlate = dataSnapshot.getValue(NumberPlate.class);
                            if(numberPlate.isDeleted==0){
                                numberPlateWheeler.add(numberPlate.wheelerType);
                                numberPlateNumber.add(numberPlate.numberPlate);
                            }
                        }
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(BookParkingAreaActivity.this,
                                android.R.layout.simple_spinner_item, numberPlateNumber);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        numberPlateSpinner.setAdapter(dataAdapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    public void addListenerOnSpinnerItemSelection() {
        numberPlateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position!=0){
                    bookingSlot.numberPlate= numberPlateNumber.get(position);
                    bookingSlot.wheelerType= numberPlateWheeler.get(position);
                    calcRefreshAmount();
                    String wheelerTypeStr=String.valueOf(bookingSlot.wheelerType);
                    wheelerText.setText(wheelerTypeStr);
                    Toast.makeText(BookParkingAreaActivity.this, String.valueOf(numberPlateSpinner.getSelectedItem())+String.valueOf(position), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void calcRefreshAmount() {
        bookingSlot.calcAmount(parkingArea);
        String amountStr=String.valueOf(bookingSlot.amount);
        amountText.setText(amountStr);
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
        if (!hasPermissions(BookParkingAreaActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(BookParkingAreaActivity.this, PERMISSIONS, AppConstants.SCAN_PERMISSION_ALL);
        }else{
//            openCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppConstants.UPI_PAYMENT:
                Boolean paid=false;
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.d(String.valueOf(BookParkingAreaActivity.this.getClass()),"UPI:" +"onActivityResult: " + trxt);
                        Map<String, String> myMap = new HashMap<String, String>();
                        String[] pairs = trxt.split("&");
                        for (String pair : pairs) {
                            String[] keyValue = pair.split("=");
                            myMap.put(keyValue[0].toLowerCase(), keyValue[1].toLowerCase());
                        }
                        paid=upiPayment.upiPaymentDataOperation(myMap,BookParkingAreaActivity.this);

                    } else {
                        Log.d(String.valueOf(BookParkingAreaActivity.this.getClass()),"UPI:" + "onActivityResult: " + "Return data is null");
                        Map<String, String> myMap = new HashMap<String, String>();
                        myMap.put("status", "-1");
                        paid=upiPayment.upiPaymentDataOperation(myMap,BookParkingAreaActivity.this);
                    }
                } else {
                    Log.d(String.valueOf(BookParkingAreaActivity.this.getClass()),"UPI:" + "onActivityResult: " + "Return data is null"); //when user simply back without payment
                    Map<String, String> myMap = new HashMap<String, String>();
                    myMap.put("status", "-1");
                    paid=upiPayment.upiPaymentDataOperation(myMap,BookParkingAreaActivity.this);
                }
                if(paid){
                    bookingSlot.hasPaid=1;
                    saveData();
                }else{
                    parkingArea.deallocateSpace();
                    db.getReference("ParkingAreas").child(bookingSlot.placeID).setValue(parkingArea);
                }
                break;
        }
    }
}