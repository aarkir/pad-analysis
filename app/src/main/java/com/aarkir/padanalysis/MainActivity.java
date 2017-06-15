package com.aarkir.padanalysis;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements PhotoFragment.PhotoDialogListener, MolarityFragment.MolarityFragmentListener {

    /**
     * PERMISSIONS
     **/

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
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
        final ListView listview = (ListView) findViewById(android.R.id.list);
        listview.setEmptyView(findViewById(android.R.id.empty)); // necessary because activity is not list activity
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
            case R.id.save:
                save();
                return true;
            case R.id.clear:
                clear();
                return true;
            case R.id.add:
                addSample();
                return true;
            case R.id.eval:
                evaluateSample();
                return true;
            case R.id.sendEmail:
                File file = save();
                sendEmail(file);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** ACTIONS **/

    private File save() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "pad-analysis";
        File folder = new File(path);
        folder.mkdirs();
        String fileName = new SimpleDateFormat("yyyyMMddHHmm'.csv'").format(new Date());
        File file = new File(folder, fileName);
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(makeDataString());
            myOutWriter.close();
            fOut.flush();
            fOut.close();
        } catch (IOException e) {

        }
        Toast.makeText(this, "Saved!", Toast.LENGTH_LONG).show();
        return file;
    }

    private void sendEmail(File file) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"aaronk3.14@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PADAnalysis");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Here's a document you sent from PADAnalysis.");
        if (!file.exists() || !file.canRead()) {
            return;
        }
        Uri uri = Uri.fromFile(file);
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
    }

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

    private String makeDataString() {
        String str = "Concentration,R,G,B,H,S,V,\n";
        int color;
        float[] hsv = new float[3];
        for (Pair pair : list) {
            color = pair.getColor();
            str += pair.getMolarity() + "," + Color.red(color) + "," + Color.green(color) + "," + Color.blue(color);
            Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
            str += "," + hsv[0] + "," + hsv[1] + "," + hsv[2] + "\n";
        }
        return str;
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