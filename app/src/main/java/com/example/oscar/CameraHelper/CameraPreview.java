package com.example.oscar.CameraHelper;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.oscar.cameratest.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by oscar on 08-08-17.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int i = 0;

    public CameraPreview(Context context, Camera camera)
    {
        super(context);
        mCamera = camera;
        // get Camera parameters
        Camera.Parameters params = mCamera.getParameters();
        // set the focus mode
        params.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
        // set Camera parameters
        mCamera.setParameters(params);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();


            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
            List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
            List<Camera.Size> videoSizes = params.getSupportedVideoSizes();

            for (Camera.Size sizes: previewSizes) {
                Log.i("CAMERATEST: postExecute", "previewSize: " + sizes.width + " x " + sizes.height);
            }

            for (Camera.Size sizes: pictureSizes) {
                Log.i("CAMERATEST: postExecute", "pictureSizes: " + sizes.width + " x " + sizes.height);
            }

            for (Camera.Size sizes: videoSizes) {
                Log.i("CAMERATEST: postExecute", "videoSizes: " + sizes.width + " x " + sizes.height);
            }

            Camera.Size preferedVideoSize = params.getPreferredPreviewSizeForVideo();
            Camera.Size previewSize = params.getPreviewSize();
            int previewFormat = params.getPreviewFormat();
            List<int[]> previewFPSRange = params.getSupportedPreviewFpsRange();

            //Log.i("CAMERATEST: postExecute", "pictureSize Now: " + pictureSize.width + " x " + pictureSize.height);
            Log.i("CAMERATEST: postExecute", "preferedVideoSize Now: " + preferedVideoSize.width + " x " + preferedVideoSize.height);
            Log.i("CAMERATEST: postExecute", "previewSize Now: " + previewSize.width + " x " + previewSize.height);
            Log.i("CAMERATEST: postExecute", "previewFormat Now: " + previewFormat);

        } catch (IOException e) {
            Log.d("MyCameraApp", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate             Camera.Size pictureSize = params.getPictureSize();

        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("MyCameraApp", "Error starting camera preview: " + e.getMessage());
        }
    }

    public Camera.Size getPreviewSize()
    {
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size previewSize = params.getPreviewSize();

        return previewSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.

    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        if(i < 5)
        {
            Camera.Parameters parameters = camera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;

            YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

            byte[] bytes = out.toByteArray();

            Log.i("CameraPreview", "frame");
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.

            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraApp");
            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists()){
                if (! mediaStorageDir.mkdirs()){
                    Log.d("MyCameraApp", "failed to create directory");
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");

            try {
                FileOutputStream fos = new FileOutputStream(mediaFile);
                fos.write(bytes);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("MyCameraApp", "File not found: " + e.getMessage());

            } catch (IOException e) {
                Log.d("MyCameraApp", "Error accessing file: " + e.getMessage());
            }
        }


        i++;
    }
}
