package com.aarkir.padanalysis;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

class PairAdapter extends ArrayAdapter<Pair> {
    PairAdapter(Context context, ArrayList<Pair> pairs) {
        super(context, 0, pairs);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        Pair pair = getItem(position);
        // if convertView is not being used currently
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pair_list_layout, parent, false);
        }
        TextView molarityText = (TextView) convertView.findViewById(R.id.molarity);
        View rectangle = convertView.findViewById(R.id.rectangle);
        TextView color = (TextView) convertView.findViewById(R.id.color);
        if (pair != null) {
            molarityText.setText(String.format(Locale.getDefault(), "%f", pair.getMolarity()));
            int intColor = pair.getColor();
            float[] hsv = new float[3];
            Color.RGBToHSV(Color.red(intColor), Color.green(intColor), Color.blue(intColor), hsv);
            rectangle.setBackgroundColor(pair.getColor());
            color.setText(""+hsv[0]+"\n"+hsv[1]+"\n"+hsv[2]);
        }
        return convertView;
    }
}
