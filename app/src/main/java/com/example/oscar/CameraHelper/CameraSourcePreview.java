package com.example.oscar.CameraHelper;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/**
 * Created by Oscar on 16-08-2017.
 */

public class CameraSourcePreview extends SurfaceView implements SurfaceHolder.Callback{
    SurfaceView surfaceView;
    SurfaceHolder mHolder;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;

    public CameraSourcePreview(final Context context)
    {
        super(context);

        barcodeDetector = new BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(context, barcodeDetector).setRequestedFps(15.0f)
                .setRequestedPreviewSize(640, 480).build();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if(qrCodes.size() != 0)
                {
                    Toast.makeText(context, qrCodes.valueAt(0).displayValue, Toast.LENGTH_SHORT);
                    cameraSource.release();
                }
            }
        });
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            cameraSource.start(holder);
        } catch (IOException e) {
            Log.i("CAMERATEST: sourcePreview", "error: " + e.getMessage().toString() );
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
