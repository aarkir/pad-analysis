package com.aarkir.padanalysis;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class MolarityFragment extends DialogFragment {
    EditText molarityText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final ViewGroup nullParent = null;
        View mainView = inflater.inflate(R.layout.molarity_layout, nullParent);
        builder.setView(mainView)
                .setPositiveButton("Got Molarity", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double molarity;
                        try {
                            molarity = Double.parseDouble(molarityText.getText().toString());
                        } catch (NullPointerException | NumberFormatException e) {
                            molarity = 0;
                        }
                        mListener.addMolarity(molarity);
                    }
                });

        AlertDialog alertDialog =  builder.create();

        molarityText = (EditText) mainView.findViewById(R.id.molarityText);

        return alertDialog;

    }

    /** LISTENER **/

    interface MolarityFragmentListener {
        void addMolarity(double molarity);
    }

    MolarityFragmentListener mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MolarityFragmentListener) activity;
        } catch (ClassCastException e) {
            //class doesnt implement the interface
            throw new ClassCastException(activity.toString() + " must implement PhotoDialogListener");
        }
    }
}
