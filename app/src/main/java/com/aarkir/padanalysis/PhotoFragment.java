package com.aarkir.padanalysis;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class PhotoFragment extends DialogFragment {
    final static int cameraId = 0;
    @SuppressWarnings("deprecation")
    Camera camera;
    private TextureView mTextureView;
    View frameRectangle, savedRectangle;
    TextView frameRGB, saveRGB;
    int color = 0;
    Button button;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final ViewGroup nullParent = null;
        View mainView = inflater.inflate(R.layout.preview_layout, nullParent);
        builder.setView(mainView)
                .setPositiveButton("Got Color", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.addColor(color);
                    }
                });

        AlertDialog alertDialog =  builder.create();
        initiateUI(mainView);
        return alertDialog;
    }

    @SuppressWarnings("deprecation")
    private void initiateUI(final View view) {
        mTextureView = (TextureView) view.findViewById(R.id.surfaceView);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                camera = Camera.open(cameraId);
                Camera.Parameters params = camera.getParameters();
                if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }

                List<Camera.Size> sizes = params.getSupportedPreviewSizes();
                Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                params.setPreviewSize(optimalSize.width, optimalSize.height);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(optimalSize.width, optimalSize.height));

                camera.setParameters(params);

                try {
                    setCameraDisplayOrientation(getActivity(), cameraId, camera);
                    camera.setPreviewTexture(surface);
                    camera.startPreview();
                } catch (IOException ioe) {
                    //something
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                //camera does all work for us
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                camera.stopPreview();
                camera.release();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                takeFrame();
            }
        });

        frameRGB = (TextView) view.findViewById(R.id.frameRGB);
        frameRectangle = view.findViewById(R.id.frameRectangle);
        saveRGB = (TextView) view.findViewById(R.id.savedRGB);
        savedRectangle = view.findViewById(R.id.savedRectangle);

        button = (Button) view.findViewById(R.id.photoButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        Log.d("myTag", "This is my test");

    }

    /** LISTENER **/

    interface PhotoDialogListener {
        void addColor(int color);
    }

    PhotoDialogListener mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PhotoDialogListener) activity;
        } catch (ClassCastException e) {
            //class doesnt implement the interface
            throw new ClassCastException(activity.toString() + " must implement PhotoDialogListener");
        }
    }

    /**
     * CAMERA
     **/

    public void takeFrame() {
        Bitmap bmp = mTextureView.getBitmap();
        int color = averagergb(bmp, 20);
        frameRGB.setText(Integer.toHexString(color));
        frameRectangle.setBackgroundColor(color);
    }

    public void takePhoto() {
        Bitmap bmp = mTextureView.getBitmap();
        color = averagergb(bmp, 20);
        saveRGB.setText(Integer.toHexString(color));
        savedRectangle.setBackgroundColor(color);
    }

    public int averagergb(Bitmap bmp, int size) {
        int height = bmp.getHeight();
        int width = bmp.getWidth();
        int[] pixels = new int[size*size];
        final Bitmap subsetPixels = Bitmap.createBitmap(bmp, (width-size)/2, (height-size)/2, size, size);
        subsetPixels.getPixels(pixels, 0, size, 0, 0, size, size);
        int color, r = 0, g = 0, b = 0, n = 0;
        for (int i = 0; i < pixels.length; i += size) {
            color = pixels[i];
            r += Color.red(color);
            g += Color.green(color);
            b += Color.blue(color);
            n++;
        }
        return Color.rgb(r / n, g / n, b / n);
    }

    @SuppressWarnings("deprecation")
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @SuppressWarnings("deprecation")
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w/h;

        if (sizes==null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

}