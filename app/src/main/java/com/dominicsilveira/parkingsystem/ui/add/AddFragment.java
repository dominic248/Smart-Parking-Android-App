package com.dominicsilveira.parkingsystem.ui.add;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dominicsilveira.parkingsystem.R;

public class AddFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        textView.setText("add");

        return root;
    }
}