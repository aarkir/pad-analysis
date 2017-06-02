package com.aarkir.padanalysis;

import android.Manifest;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements PhotoFragment.PhotoDialogListener, MolarityFragment.MolarityFragmentListener {

    private int tempColor;
    private ArrayList<Pair> list = new ArrayList<>(0);
    private PairAdapter pairAdapter;
    private boolean isResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissions();

        setContentView(R.layout.activity_main);

        pairAdapter = new PairAdapter(this, list);
        final ListView listview = (ListView) findViewById(R.id.layout);
        listview.setAdapter(pairAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                list.remove(position);
                pairAdapter.notifyDataSetChanged();
            }
        });
    }

    /** OPTIONS MENU **/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                clear();
                return true;
            case R.id.add:
                addSample();
                return true;
            case R.id.eval:
                evaluateSample();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** ACTIONS **/

    private void addSample() {
        DialogFragment photoFragment = new PhotoFragment();
        photoFragment.show(getFragmentManager(), "photo");
    }

    private void evaluateSample() {
        isResult = true;
        DialogFragment photoFragment = new PhotoFragment();
        photoFragment.show(getFragmentManager(), "photo");
    }

    private void clear() {
        list.clear();
        pairAdapter.notifyDataSetChanged();
    }


    /** HELPER **/
    private void showResult(double molarity) {
        ResultFragment f = new ResultFragment();
        Bundle args = new Bundle();
        args.putDouble("molarity", molarity);
        f.setArguments(args);
        f.show(getFragmentManager(), "result");
    }

    private double[] generateGrayscale(ArrayList<Pair> pairs) {
        double[] grayscale = new double[pairs.size()];
        for (int i = 0; i < pairs.size(); ++i) {
            grayscale[i] = rgbtogray(pairs.get(i).getColor());
        }
        return grayscale;
    }

    private double rgbtogray(int rgb) {
        return 0.2989* Color.red(rgb)+0.5870*Color.green(rgb)+0.1140*Color.blue(rgb);
    }

    private double[] extractMolarities(ArrayList<Pair> pairs) {
        double[] molarities = new double[pairs.size()];
        for (int i = 0; i < pairs.size(); ++i) {
            molarities[i] = pairs.get(i).getMolarity();
        }
        return molarities;
    }

    /** FRAGMENT LISTENER METHODS **/

    @Override
    public void addColor(int color) {
        tempColor = color;

        if (isResult) {
            double[] grayscales = generateGrayscale(list);
            double[] molarities = extractMolarities(list);
            LinearRegression curve = new LinearRegression(grayscales, molarities);
            showResult(curve.predict(rgbtogray(tempColor)));
            isResult = false;
        } else {
            MolarityFragment molarityFragment = new MolarityFragment();
            molarityFragment.show(getFragmentManager(), "molarity");
        }
    }

    @Override
    public void addMolarity(double molarity) {
        list.add(new Pair(tempColor, molarity));
        pairAdapter.notifyDataSetChanged();
    }

    /** PERMISSIONS **/

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    private void requestPermissions() {
        // Assume thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
//                // show asynchronous explanation
//            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
//            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_CAMERA: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }
}