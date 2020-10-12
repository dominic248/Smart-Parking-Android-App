package com.dominicsilveira.parkingsystem.NormalUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dominicsilveira.parkingsystem.OwnerUser.MainOwnerActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.LoginActivity;
import com.dominicsilveira.parkingsystem.RegisterLogin.SplashScreen;
import com.dominicsilveira.parkingsystem.classes.BookedSlotKey;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.classes.ClosestDistance;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.utils.adapters.CloseLocationAdapter;
import com.dominicsilveira.parkingsystem.utils.adapters.UserHistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserHistoryActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    List<BookedSlotKey> bookedSlotKeyList=new ArrayList<BookedSlotKey>();

    AppConstants globalClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);

        initComponents();
        attachListener();
    }

    private void initComponents() {
        globalClass=(AppConstants)getApplicationContext();

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        recyclerView = (RecyclerView) findViewById(R.id.user_history_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(UserHistoryActivity.this);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void attachListener() {
        db.getReference().child("BookedSlots").orderByChild("userID").equalTo(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            BookedSlots bookedSlot = dataSnapshot.getValue(BookedSlots.class);
                            BookedSlotKey bookedSlotKey=new BookedSlotKey(bookedSlot,dataSnapshot.getKey());
                            bookedSlotKeyList.add(bookedSlotKey);
                        }
                        mAdapter = new UserHistoryAdapter(bookedSlotKeyList);
                        recyclerView.setAdapter(mAdapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

}