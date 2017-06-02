package com.aarkir.padanalysis;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class ResultFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        double molarity = getArguments().getDouble("molarity");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Double.toString(molarity)).setTitle("Predicted Molarity");
        return builder.create();
    }
}
