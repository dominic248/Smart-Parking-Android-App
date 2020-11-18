package com.dominicsilveira.parkingsystem.utils.dialog;
//https://medium.com/better-programming/what-is-target-fragment-da0e7c7f345c


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.dominicsilveira.parkingsystem.utils.AppConstants;
import com.dominicsilveira.parkingsystem.R;

import java.util.ArrayList;
import java.util.List;

public class NumberPlatePopUp extends AppCompatDialogFragment {
    private EditText vehicleNumber;
    private Spinner vehicleWheeler;
    int wheelerType=4;
    private NumberPlatePopUpListener listener;

    List<String> wheeler = new ArrayList<String>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle mArgs = getArguments();
        String numberPlate = mArgs.getString("numberPlate").toUpperCase();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.include_add_plate_popup_layout, null);
        builder.setView(view)
                .setTitle("Add Vehicle")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        listener.onActivityResult(AppConstants.NUMBER_PLATE_POPUP_REQUEST_CODE, Activity.RESULT_CANCELED, intent);
                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String vehicleNumberStr = vehicleNumber.getText().toString();
                        Intent intent = new Intent();
                        intent.putExtra("vehicleNumber",vehicleNumberStr);
                        intent.putExtra("wheelerType",wheelerType);
                        listener.onActivityResult(AppConstants.NUMBER_PLATE_POPUP_REQUEST_CODE, Activity.RESULT_OK, intent);
                    }
                });
        vehicleNumber = view.findViewById(R.id.vehicleNumber);
        vehicleNumber.setText(numberPlate);
        vehicleWheeler = view.findViewById(R.id.vehicleWheeler);
        vehicleNumber.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        addItemsOnSpinner();
        addListenerOnSpinnerItemSelection();
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (NumberPlatePopUpListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +"must implement NumberPlatePopUpListener");
        }
    }

    public interface NumberPlatePopUpListener {
        void onActivityResult(int requestCode, int resultCode, Intent intent);
    }


    public void addItemsOnSpinner() {
        wheeler.add("4 Wheeler");
        wheeler.add("3 Wheeler");
        wheeler.add("2 Wheeler");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, wheeler);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleWheeler.setAdapter(dataAdapter);
    }

    public void addListenerOnSpinnerItemSelection() {
        vehicleWheeler.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch(position){
                    case 1:
                        wheelerType=3;
                        break;
                    case 2:
                        wheelerType=2;
                        break;
                    default:
                        wheelerType=4;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }
}
