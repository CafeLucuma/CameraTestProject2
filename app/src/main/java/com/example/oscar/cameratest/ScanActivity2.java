package com.example.oscar.cameratest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScanActivity2 extends AppCompatActivity {

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private ImageView imagePreview;
    private ImageView imagePreview2;
    private int imgWidth;
    private int imgHeight;
    private MyBarcodeDetector mybarcode;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan2);

        imagePreview = (ImageView) findViewById(R.id.imageView);
        imagePreview2 = (ImageView) findViewById(R.id.imageView2);

        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        mybarcode = new MyBarcodeDetector(barcodeDetector);
        Log.i("ScanActivity2", "barcode puesto");

        if(!barcodeDetector.isOperational())
        {
            Log.i("CAMERATEST: DETECTOR", "DETECTOR NO ENCONTRADO");
            Toast.makeText(getApplicationContext(), "No se pudo iniciar el detector", Toast.LENGTH_LONG).show();
            this.finish();
        }

        cameraSource = new CameraSource.Builder(this, mybarcode)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(50)
                .build();

        mybarcode.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size() > 0){
                    Log.i("CAMERATEST: DETECTOR", "WEA DETECTADA");
                    Intent intent = new Intent();
                    intent.putExtra("barcode", barcodes.valueAt(0));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        try {
            if (ContextCompat.checkSelfPermission(ScanActivity2.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraSource.start();
                Log.i("ScanActivity2", "Empezo camara");
            }
        } catch (IOException e) {
            Log.i("ScanActivity", "Error: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraSource != null)
        {
            cameraSource.stop();
            cameraSource.release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraSource != null) {
            cameraSource.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (cameraSource != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            try {
                cameraSource.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyBarcodeDetector extends Detector<Barcode>
    {
        private BarcodeDetector mDelegate;

        public MyBarcodeDetector(BarcodeDetector detector) {
            mDelegate = detector;
        }

        public SparseArray<Barcode> detect(Frame frame) {
            Log.i("Scan", "frame");
            // *** add your code to access the preview frame here
            //obtener bitmap de frame
            int width = frame.getMetadata().getWidth();
            int height = frame.getMetadata().getHeight();
            imgWidth = imagePreview.getWidth();
            imgHeight = imagePreview.getHeight();
            YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21,
                    width  , height, null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);
            byte[] jpegArray = byteArrayOutputStream.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
            bitmap = bitmap.createScaledBitmap(bitmap, imgWidth, imgHeight, false);

            if(bitmap != null)
            {
                final Bitmap finalBitmap = bitmap;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //stuff that updates ui
                        imagePreview.setImageBitmap(finalBitmap);
                        imagePreview2.setImageBitmap(finalBitmap);
                    }
                });

            }
            return mDelegate.detect(frame);
        }

        public boolean isOperational() {
            return mDelegate.isOperational();
        }

        public boolean setFocus(int id) {
            return mDelegate.setFocus(id);
        }
    }
}
