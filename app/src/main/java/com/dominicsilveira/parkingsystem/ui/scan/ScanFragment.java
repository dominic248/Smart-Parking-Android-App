package com.dominicsilveira.parkingsystem.ui.scan;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dominicsilveira.parkingsystem.utils.BasicUtils;
import com.dominicsilveira.parkingsystem.utils.animations.ViewAnimation;
import com.dominicsilveira.parkingsystem.R;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.utils.network.ApiService;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.dominicsilveira.parkingsystem.utils.dialog.NumberPlatePopUp;
import com.dominicsilveira.parkingsystem.utils.adapters.NumberPlateAdapter;
import com.dominicsilveira.parkingsystem.utils.swipe.SimpleToDeleteCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class ScanFragment extends Fragment implements NumberPlatePopUp.NumberPlatePopUpListener {

    FloatingActionButton fab_cameraBtn,fab_textBtn,fab_add;
    Bitmap upload;
    TextView empty_view;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    boolean rotate = false;

    BasicUtils utils=new BasicUtils();

    FirebaseAuth auth;
    FirebaseDatabase db;

    View lyt_cameraBtn, lyt_textBtn, back_drop;

    Map<String, NumberPlate> numberPlatesList = new HashMap<String, NumberPlate>();
    List<String> keys = new ArrayList<String>();
    Map<String, NumberPlate> treeMap;
    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET,
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scan, container, false);

        initComponents(root);
        attachListeners(root);
        if(!utils.isNetworkAvailable(getActivity().getApplication())){
            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
        }

        return root;
    }

    private void initComponents(View root) {
        fab_cameraBtn = (FloatingActionButton) root.findViewById(R.id.fab_cameraBtn);
        fab_textBtn = (FloatingActionButton) root.findViewById(R.id.fab_textBtn);
        fab_add = (FloatingActionButton) root.findViewById(R.id.fab_add);
        back_drop = root.findViewById(R.id.back_drop);
        empty_view = root.findViewById(R.id.empty_view);

        lyt_cameraBtn = root.findViewById(R.id.lyt_cameraBtn);
        lyt_textBtn = root.findViewById(R.id.lyt_textBtn);
        ViewAnimation.initShowOut(lyt_cameraBtn);
        ViewAnimation.initShowOut(lyt_textBtn);
        back_drop.setVisibility(View.GONE);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(v);
            }
        });

        back_drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
            }
        });

        fab_cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
                askCameraPermission();
            }
        });

        fab_textBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
                Bundle args = new Bundle();
                args.putString("numberPlate", "");
                NumberPlatePopUp numberPlateDialog = new NumberPlatePopUp();
                numberPlateDialog.setTargetFragment(ScanFragment.this, AppConstants.NUMBER_PLATE_POPUP_REQUEST_CODE);
                numberPlateDialog.setArguments(args);
                numberPlateDialog.show(getParentFragmentManager(), "exampledialog");
            }
        });

        recyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);


        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

    }

    private void attachListeners(View root) {
        db.getReference().child("NumberPlates").orderByChild("userID_isDeletedQuery").equalTo(auth.getCurrentUser().getUid()+"_0")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        SimpleToDeleteCallback itemTouchHelperCallback=new SimpleToDeleteCallback(getActivity()) {
                            @Override
                            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                return false;
                            }

                            @Override
                            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                                final int position=viewHolder.getAdapterPosition();
                                final String data = keys.get(position);
                                Snackbar snackbar = Snackbar
                                        .make(recyclerView, "Number Plate Removed", Snackbar.LENGTH_LONG)
                                        .setAction("UNDO", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                keys.add(position, data);
                                                mAdapter.notifyItemInserted(position);
                                                recyclerView.scrollToPosition(position);
                                                db.getReference().child("NumberPlates").child(data).child("userID_isDeletedQuery")
                                                        .setValue(auth.getCurrentUser().getUid()+"_0");
                                                db.getReference().child("NumberPlates").child(data).child("isDeleted")
                                                        .setValue(0);
                                            }
                                        });
                                snackbar.show();
                                keys.remove(position);
                                mAdapter.notifyItemRemoved(position);
                                db.getReference().child("NumberPlates").child(data).child("userID_isDeletedQuery")
                                        .setValue(auth.getCurrentUser().getUid()+"_1");
                                db.getReference().child("NumberPlates").child(data).child("isDeleted")
                                        .setValue(1);
                                checkRecyclerViewIsEmpty();
                            }
                        };
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NumberPlate numberPlate = dataSnapshot.getValue(NumberPlate.class);
                            numberPlatesList.put(dataSnapshot.getKey(),numberPlate);
                            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
                            itemTouchHelper.attachToRecyclerView(recyclerView);
                        }
                        treeMap = new TreeMap<String, NumberPlate>(numberPlatesList);
                        keys.addAll(treeMap.keySet());
                        mAdapter = new NumberPlateAdapter(treeMap,keys);
                        recyclerView.setAdapter(mAdapter);
                        checkRecyclerViewIsEmpty();
                        Log.d(String.valueOf(getActivity().getClass()),"Scan"+String.valueOf(numberPlatesList));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void checkRecyclerViewIsEmpty() {
        if(keys.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            empty_view.setVisibility(View.GONE);
        }
    }

    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            ViewAnimation.showIn(lyt_cameraBtn);
            ViewAnimation.showIn(lyt_textBtn);
            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.showOut(lyt_cameraBtn);
            ViewAnimation.showOut(lyt_textBtn);
            back_drop.setVisibility(View.GONE);
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

    private void askCameraPermission() {
        if (!hasPermissions(getActivity(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, AppConstants.SCAN_PERMISSION_ALL);
        }else{
            openCamera();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (!hasPermissions(getActivity(), PERMISSIONS)) {
//            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, AppConstants.SCAN_PERMISSION_ALL);
//        }else{
//            openCamera();
//        }
    }

    private void openCamera() {
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
//                        Toast.makeText(getActivity(), response.code() + " ", Toast.LENGTH_SHORT).show();
                        try {                            String resp=response.body().string();
                            Log.i(String.valueOf(getActivity().getClass()),"Response: "+ resp);
                            JSONObject obj = new JSONObject(resp); //response.body().string() fetched only once
                            JSONArray geodata = obj.getJSONArray("results");
                            if(geodata.length() > 0 ){
                                Bundle args = new Bundle();
                                args.putString("numberPlate", geodata.getJSONObject(0).getString("plate"));
                                NumberPlatePopUp numberPlateDialog = new NumberPlatePopUp();
                                numberPlateDialog.setTargetFragment(ScanFragment.this, AppConstants.NUMBER_PLATE_POPUP_REQUEST_CODE);
                                numberPlateDialog.setArguments(args);
                                numberPlateDialog.show(getParentFragmentManager(), "exampledialog");
                            }else{
                                Toast.makeText(getActivity(), "No Number-Plate found!", Toast.LENGTH_SHORT).show();
                            }
                            Log.e(String.valueOf(getActivity().getClass()),"ImageUploader"+ geodata.getJSONObject(0).getString("plate"));
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
                saveData(data.getStringExtra("vehicleNumber"),data.getIntExtra("wheelerType",4));
//                Toast.makeText(getActivity(), data.getStringExtra("selection"), Toast.LENGTH_SHORT).show();
            }else if (resultCode == Activity.RESULT_CANCELED) {
                    //Do Something in case not recieved the data
            }
        }
    }

    private void saveData(String vehicleNumber,int wheelerType) {
        final NumberPlate numberPlate = new NumberPlate(vehicleNumber,wheelerType,0,auth.getCurrentUser().getUid(),auth.getCurrentUser().getUid()+"_0");
        final String key=db.getReference("NumberPlates").push().getKey();
        db.getReference("NumberPlates")
                .child(key)
                .setValue(numberPlate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                    numberPlatesList.put(key,numberPlate);
                    treeMap = new TreeMap<String, NumberPlate>(numberPlatesList);
                    keys.add(key);
                    mAdapter = new NumberPlateAdapter(treeMap,keys);
                    recyclerView.setAdapter(mAdapter);
                    checkRecyclerViewIsEmpty();
                } else {
                    Toast.makeText(getActivity(), "Failed to add extra details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}