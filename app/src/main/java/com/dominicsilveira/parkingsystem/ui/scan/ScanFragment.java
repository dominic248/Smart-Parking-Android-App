package com.dominicsilveira.parkingsystem.ui.scan;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dominicsilveira.parkingsystem.R;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.dominicsilveira.parkingsystem.AppConstants;
import com.dominicsilveira.parkingsystem.classes.NumberPlate;
import com.dominicsilveira.parkingsystem.common.NumberPlatePopUp;
import com.dominicsilveira.parkingsystem.utils.NumberPlateAdapter;
import com.dominicsilveira.parkingsystem.utils.NumberPlateNetworkAsyncTask;
import com.dominicsilveira.parkingsystem.utils.SimpleToDeleteCallback;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



public class ScanFragment extends Fragment implements NumberPlatePopUp.NumberPlatePopUpListener,NumberPlateNetworkAsyncTask.AsyncResponse {

    FloatingActionButton cameraBtn;
    Bitmap upload;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    FirebaseAuth auth;
    FirebaseDatabase db;

    Map<String, NumberPlate> numberPlatesList = new HashMap<String, NumberPlate>();
    List<String> keys = new ArrayList<String>();
    Map<String, NumberPlate> treeMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_scan, container, false);
            cameraBtn=root.findViewById(R.id.addVehicle);

            recyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
            recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);

            auth = FirebaseAuth.getInstance();
            db = FirebaseDatabase.getInstance();


            cameraBtn.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    askCameraPermission();
                    Toast.makeText(getActivity(),"Camera Btn clicked",Toast.LENGTH_SHORT).show();
                }
            });

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
                                    String id;
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
                                                    db.getReference().child("NumberPlates").child(data).child("isDeletedQuery")
                                                            .setValue(0);
                                                }
                                            });
                                    snackbar.show();
                                    keys.remove(position);
                                    mAdapter.notifyItemRemoved(position);
                                    db.getReference().child("NumberPlates").child(data).child("userID_isDeletedQuery")
                                            .setValue(auth.getCurrentUser().getUid()+"_1");
                                    db.getReference().child("NumberPlates").child(data).child("isDeletedQuery")
                                            .setValue(1);


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
                            Log.d("GPS Map", String.valueOf(numberPlatesList));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
            return root;
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
                saveData(data.getStringExtra("vehicleNumber"),data.getIntExtra("wheelerType",4));
                Toast.makeText(getActivity(), data.getStringExtra("selection"), Toast.LENGTH_SHORT).show();
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
                } else {
                    Toast.makeText(getActivity(), "Failed to add extra details", Toast.LENGTH_SHORT).show();
                }
                numberPlatesList.put(key,numberPlate);
                treeMap = new TreeMap<String, NumberPlate>(numberPlatesList);
                keys.add(key);
                mAdapter = new NumberPlateAdapter(treeMap,keys);
                recyclerView.setAdapter(mAdapter);

            }
        });
    }

    public void NumberPlateNetworkAsyncTaskCallback(String result) throws JSONException {
        JSONObject obj = new JSONObject(result);
        JSONArray geodata = obj.getJSONArray("results");
        Bundle args = new Bundle();
        args.putString("numberPlate", geodata.getJSONObject(0).getString("plate"));
        NumberPlatePopUp numberPlateDialog = new NumberPlatePopUp();
        numberPlateDialog.setTargetFragment(ScanFragment.this, AppConstants.NUMBER_PLATE_POPUP_REQUEST_CODE);
        numberPlateDialog.setArguments(args);
        numberPlateDialog.show(getParentFragmentManager(), "exampledialog");
        Log.e("ImageUploader", geodata.getJSONObject(0).getString("plate"));
    }

}