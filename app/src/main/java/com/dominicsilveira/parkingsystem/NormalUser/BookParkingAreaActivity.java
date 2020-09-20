package com.dominicsilveira.parkingsystem.NormalUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Hours;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookParkingAreaActivity extends AppCompatActivity {
    Spinner numberPlateSpinner;
    TextView placeText,coordText,slotNoText,wheelerText,amountText;
    TextView startBtn, endBtn;
    Button cancelBtn,bookBtn;

    DateTime startDateTime,endDateTime;
    String placeID;
    ParkingArea parkingArea;

    FirebaseAuth auth;
    FirebaseDatabase db;

    List<Integer> numberPlateWheeler = new ArrayList<Integer>();
    List<String> numberPlateNumber = new ArrayList<String>();
    String numberPlateText;
    int wheelerTypeText;
    final int UPI_PAYMENT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_parking_area);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        placeText = findViewById(R.id.placeText);
        coordText = findViewById(R.id.coordText);
        slotNoText = findViewById(R.id.slotNoText);
        numberPlateSpinner = findViewById(R.id.vehicleSelect);
        startBtn = findViewById(R.id.startBtn);
        endBtn = findViewById(R.id.endBtn);
        bookBtn = findViewById(R.id.bookBtn);
        wheelerText = findViewById(R.id.wheelerText);
        amountText = findViewById(R.id.amountText);


        startBtn.setInputType(InputType.TYPE_NULL);
        endBtn.setInputType(InputType.TYPE_NULL);

        Bundle bundle = getIntent().getExtras();
        String UUID=bundle.getString("UUID");
        final ParkingArea parkingArea = (ParkingArea) getIntent().getSerializableExtra("ParkingArea");
        Log.e("BookParkingAreaActivity",parkingArea.name+" "+UUID);

        placeText.setText(parkingArea.name);
        String coord=String.valueOf(parkingArea.latitude)+", "+String.valueOf(parkingArea.longitude);
        coordText.setText(coord);

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

        addItemsOnSpinner();
        addListenerOnSpinnerItemSelection();

        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

//        payBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
                //Getting the values from the EditTexts
//                String amount = "1";
//                String note ="Payment";
//                String name = "Michael Silveira";
//                String upiId = "micsilveira111@oksbi";
//                payUsingUpi(amount, upiId, name, note);
//            }
//        });
        db.getReference().child("ParkingAreas").child(UUID)
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

        db.getReference().child("ParkingAreas").child(UUID)
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

    private void setAddValues(ParkingArea parkingArea,String placeID) {
        this.placeID=placeID;
        this.parkingArea=parkingArea;
        placeText.setText(parkingArea.name);
        coordText.setText(String.valueOf(parkingArea.latitude).concat(", ").concat(String.valueOf(parkingArea.longitude)));
    }

    private void saveData() {
        final int amountInt=Integer.parseInt(amountText.getText().toString());
        String userID = auth.getCurrentUser().getUid();
        final BookedSlots bookingSlot=new BookedSlots(userID,placeID,numberPlateText,wheelerTypeText,startDateTime,endDateTime,0,amountInt);
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
                                    Toast.makeText(BookParkingAreaActivity.this,"Success",Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(BookParkingAreaActivity.this,"Failed",Toast.LENGTH_SHORT).show();
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
                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        button.setText(simpleDateFormat.format(calendar.getTime()));
                        if(end){
                            endDateTime = new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0,0);
                        }else{
                            startDateTime = new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0,0);
                        }
                        if(endDateTime!=null && startDateTime!=null){
                            if(endDateTime.getMillis()>startDateTime.getMillis()){
                                Toast.makeText(BookParkingAreaActivity.this,
                                        "after", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(BookParkingAreaActivity.this,
                                        "before", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                };
                TimePickerDialog timePickerDialog=new TimePickerDialog(BookParkingAreaActivity.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
                timePickerDialog.show();
            }
        };
        DatePickerDialog datePickerDialog=new DatePickerDialog(BookParkingAreaActivity.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
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
                            numberPlateWheeler.add(numberPlate.wheelerType);
                            numberPlateNumber.add(numberPlate.numberPlate);
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
                    numberPlateText= numberPlateNumber.get(position);
                    wheelerTypeText= numberPlateWheeler.get(position);
                    Hours hours = Hours.hoursBetween(startDateTime, endDateTime);
                    int wheelerAmount;
                    if(wheelerTypeText==2)
                        wheelerAmount=parkingArea.amount2;
                    else if(wheelerTypeText==3)
                        wheelerAmount=parkingArea.amount3;
                    else
                        wheelerAmount=parkingArea.amount4;
                    int amount=hours.getHours()*wheelerAmount;
                    String amountStr=String.valueOf(amount);
                    String wheelerTypeStr=String.valueOf(wheelerTypeText);
                    amountText.setText(amountStr);
                    wheelerText.setText(wheelerTypeStr);
                    Toast.makeText(BookParkingAreaActivity.this, String.valueOf(numberPlateSpinner.getSelectedItem())+String.valueOf(position), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    void payUsingUpi(String amount, String upiId, String name, String note) {
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
        if(null != chooser.resolveActivity(getPackageManager()))
            startActivityForResult(chooser, UPI_PAYMENT);
        else
            Toast.makeText(BookParkingAreaActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.d("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        Log.d("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(BookParkingAreaActivity.this)) {
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
                Toast.makeText(BookParkingAreaActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: "+approvalRefNo);
            } else if("Payment cancelled by user.".equals(paymentCancel))
                Toast.makeText(BookParkingAreaActivity.this, "Payment cancelled by user-2.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(BookParkingAreaActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(BookParkingAreaActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
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