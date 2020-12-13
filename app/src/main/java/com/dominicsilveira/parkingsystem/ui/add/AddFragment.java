package com.dominicsilveira.parkingsystem.ui.add;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dominicsilveira.parkingsystem.NormalUser.BookParkingAreaActivity;
import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
import com.dominicsilveira.parkingsystem.utils.pdf.InvoiceGenerator;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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
    Spinner numberPlateSpinner;
    TextView slotNoText,amountText,wheelerText,endDateText,endTimeText,placeText;
    LinearLayout endDate,endTime,scanBtn;
    Button bookBtn;
    EditText emailText;

    FirebaseAuth auth;
    FirebaseDatabase db;
    Calendar calendar;
    BasicUtils utils=new BasicUtils();

    List<Integer> numberPlateWheeler = new ArrayList<Integer>();
    List<String> numberPlateNumber = new ArrayList<String>();
    User userObj;
    ParkingArea parkingArea;
    BookedSlots bookingSlot=new BookedSlots();


    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET,
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add, container, false);

        initComponents(root);
        attachListeners();

        if(!utils.isNetworkAvailable(getActivity().getApplication())){
            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
        }

        defaultSpinnerItems();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, numberPlateNumber);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberPlateSpinner.setAdapter(dataAdapter);
        addItemsOnSpinner();
        addListenerOnSpinnerItemSelection();

        askCameraFilePermission();
        return root;
    }

    private void initComponents(View root) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        scanBtn=root.findViewById(R.id.scanBtn);
        placeText = root.findViewById(R.id.placeText);
        slotNoText = root.findViewById(R.id.slotNoText);
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
        numberPlateSpinner = root.findViewById(R.id.vehicleSelect);

        calendar=new GregorianCalendar();
        bookingSlot.startTime=bookingSlot.endTime=bookingSlot.checkoutTime=calendar.getTime();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
        endTimeText.setText(simpleDateFormat.format(bookingSlot.startTime));
        simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
        endDateText.setText(simpleDateFormat.format(bookingSlot.endTime));
        bookingSlot.readNotification=0;
        bookingSlot.readBookedNotification=0;
        bookingSlot.hasPaid=1;
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

        bookBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(emailText.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), "Users Email can't be blank!", Toast.LENGTH_SHORT).show();
                }else if(numberPlateSpinner.getSelectedItemPosition()==0){
                    Toast.makeText(getActivity(), "Please select a vehicle!", Toast.LENGTH_SHORT).show();
                }else if(bookingSlot.endTime.equals(bookingSlot.startTime)){
                    Toast.makeText(getActivity(),
                            "Please set the end time!", Toast.LENGTH_SHORT).show();
                }else if(!bookingSlot.timeDiffValid()){
                    Toast.makeText(getActivity(),
                            "Less time difference (<15 minutes)!", Toast.LENGTH_SHORT).show();
                }else{
                    saveData();
                }
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
                            Log.e(String.valueOf(getActivity().getClass()),"Fetch parking area");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void defaultSpinnerItems() {
        numberPlateWheeler.clear();
        numberPlateWheeler.add(0);
        numberPlateNumber.clear();
        numberPlateNumber.add("Select a vehicle");
    }

    private void addItemsOnSpinner() {
        emailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override
            public void afterTextChanged(Editable et) {
                if(utils.isNetworkAvailable(getActivity().getApplication())){
                    String emailStr=et.toString();
                    db.getReference().child("Users").orderByChild("email").equalTo(emailStr).limitToFirst(1)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            userObj=dataSnapshot.getValue(User.class);
                                            if(userObj.isVerified==1){
                                                bookingSlot.userID=dataSnapshot.getKey();
                                                Log.i(String.valueOf(getActivity().getClass()),"UserID: "+bookingSlot.userID);
                                                db.getReference().child("NumberPlates").orderByChild("userID").equalTo(bookingSlot.userID)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                                    Log.i(String.valueOf(getActivity().getClass()),dataSnapshot.getKey());
                                                                    NumberPlate numberPlate = dataSnapshot.getValue(NumberPlate.class);
                                                                    if(numberPlate.isDeleted==0){
                                                                        numberPlateWheeler.add(numberPlate.wheelerType);
                                                                        numberPlateNumber.add(numberPlate.numberPlate);
                                                                    }
                                                                }
                                                            }
                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {}
                                                        });
                                            }
                                        }
                                    }else{
                                        defaultSpinnerItems();
//                                    Toast.makeText(getActivity(),"User Doesn't exist",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }else{
                    Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void addListenerOnSpinnerItemSelection() {
        numberPlateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position!=0){
                    bookingSlot.numberPlate=numberPlateNumber.get(position);
                    bookingSlot.wheelerType=numberPlateWheeler.get(position);
                    calcRefreshAmount();
                    String wheelerTypeStr=String.valueOf(bookingSlot.wheelerType);
                    wheelerText.setText(wheelerTypeStr);
                    Toast.makeText(getActivity(), String.valueOf(numberPlateSpinner.getSelectedItem())+String.valueOf(position), Toast.LENGTH_SHORT).show();
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

    private void setAddValues(ParkingArea parkingArea,String placeID) {
        this.bookingSlot.placeID=placeID;
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
                bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                calcRefreshAmount();
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
                bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                if(bookingSlot.endTime.after(bookingSlot.startTime)){
                    button.setText(simpleDateFormat.format(calendar.getTime()));
                    bookingSlot.endTime = bookingSlot.checkoutTime = calendar.getTime();
                    calcRefreshAmount();
                }else{
                    bookingSlot.endTime = bookingSlot.checkoutTime = bookingSlot.startTime;
                    Toast.makeText(getActivity(),
                            "Please select a time after Present time!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(),timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
        timePickerDialog.show();
    }

    private void saveData() {
        if(utils.isNetworkAvailable(getActivity().getApplication())){
            bookingSlot.notificationID=Math.abs((int)Calendar.getInstance().getTimeInMillis());
            final String key=db.getReference("BookedSlots").push().getKey();
//            bookingSlot.slotNo="None";
            if(parkingArea.availableSlots>0){
                parkingArea.allocateSpace();
                bookingSlot.slotNo=parkingArea.allocateSlot(bookingSlot.numberPlate);
                db.getReference("ParkingAreas").child(bookingSlot.placeID).setValue(parkingArea);
                db.getReference("BookedSlots").child(key).setValue(bookingSlot).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getActivity(),"Success",Toast.LENGTH_SHORT).show();
                            File file = new File(getActivity().getExternalCacheDir(), File.separator + "invoice.pdf");
                            InvoiceGenerator invoiceGenerator=new InvoiceGenerator(bookingSlot,parkingArea,key,userObj,file);
                            invoiceGenerator.create();
                            invoiceGenerator.uploadFile(getActivity(),getActivity().getApplication());
                        }else{
                            parkingArea.deallocateSpace();
                            parkingArea.deallocateSlot(bookingSlot.slotNo);
                            db.getReference("ParkingAreas").child(bookingSlot.placeID).setValue(parkingArea);
                            Toast.makeText(getActivity(),"Failed! Slots are full!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else {
                Toast.makeText(getActivity(),"Failed! Slots are full.",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
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
        if (!hasPermissions(getActivity(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, AppConstants.SCAN_PERMISSION_ALL);
        }else{
//            openCamera();
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
                            Log.i(String.valueOf(getActivity().getClass()),"Response: "+ resp);
                            JSONObject obj = new JSONObject(resp); //response.body().string() fetched only once
                            JSONArray geodata = obj.getJSONArray("results");
                            Bundle args = new Bundle();
                            args.putString("numberPlate", geodata.getJSONObject(0).getString("plate"));
                            NumberPlatePopUp numberPlateDialog = new NumberPlatePopUp();
                            numberPlateDialog.setTargetFragment(AddFragment.this, AppConstants.NUMBER_PLATE_POPUP_REQUEST_CODE);
                            numberPlateDialog.setArguments(args);
                            numberPlateDialog.show(getParentFragmentManager(), "exampledialog");
                            Log.e(String.valueOf(getActivity().getClass()), "plateNumber"+geodata.getJSONObject(0).getString("plate"));
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
                bookingSlot.numberPlate=data.getStringExtra("vehicleNumber");
                bookingSlot.wheelerType=data.getIntExtra("wheelerType",4);
                calcRefreshAmount();
                String wheelerTypeStr=String.valueOf(bookingSlot.wheelerType);
                wheelerText.setText(wheelerTypeStr);
            }else if (resultCode == Activity.RESULT_CANCELED) {
                //Do Something in case not recieved the data
            }
        }
    }
}