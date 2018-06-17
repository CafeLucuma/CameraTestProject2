package com.example.oscar.CameraHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.example.oscar.Models.HOCRModel;
import com.example.oscar.OpenCVHelper.PhysicalHighlightProcessorAsync;
import com.example.oscar.cameratest.MainActivity;
import com.example.oscar.cameratest.MainActivityEstereo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static android.R.attr.width;
import static android.R.attr.x;

/**
 * Created by oscar on 08-08-17.
 */

public class CameraPreview2_0 extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    private SurfaceHolder mHolder;
    private SurfaceTexture surfaceTexture;
    private Camera mCamera;
    private Camera.Parameters params;
    private boolean sincronizar = false;
    private PhysicalHighlightProcessorAsync doc;
    private ImageView imageView;
    private ImageView imageView2;
    private int bufferSize;
    private BitmapFactory.Options options;
    private Context context;
    private int sampleSize;

    public boolean isSincronizar() {
        return sincronizar;
    }

    public void setSincronizar(boolean sincronizar)
    {
        this.sincronizar = sincronizar;
    }

    public HOCRModel getHocr() {
        return hocr;
    }

    public void setHocr(HOCRModel hocr) {
        this.hocr = hocr;
    }

    private HOCRModel hocr;

    public CameraPreview2_0(Context context, Camera camera, ImageView imageView, ImageView imageView2, int reqWidth, int reqHeight)
    {
        super(context);

        this.context = context;
        this.imageView = imageView;
        this.imageView2 = imageView2;
        this.mCamera = camera;
        surfaceTexture = new SurfaceTexture(0);
        // get Camera parameters
        params = mCamera.getParameters();
        //calcular sample size para bitmap
        // set the focus mode
        //params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        //set picture size
        List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
        int maxSizeFirst = pictureSizes.get(0).width * pictureSizes.get(0).height;
        int maxSizeLast = pictureSizes.get(pictureSizes.size() - 1).width
                * pictureSizes.get(pictureSizes.size() - 1).height;
        Camera.Size pictureSizeSet;
        if(maxSizeFirst > maxSizeLast)
            pictureSizeSet = pictureSizes.get(0);
        else
            pictureSizeSet = pictureSizes.get(pictureSizes.size() - 1);
        Log.i("CAMERATEST: postExecute", "pictureSize SELECTED " + pictureSizeSet.width + " x " + pictureSizeSet.height);

        params.setPictureSize(pictureSizeSet.width, pictureSizeSet.height);
            //TODO calcular el preview size mas cercano a reqwidth reqheigth

        //calcular parametro mas parecido a req width y height
        Log.i("CAMERATEST: postExecute", "reqWIdth reqHeight" + reqWidth + " x " + reqHeight);
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        int[] widthHeight = calcularPreviewSize(reqWidth, reqHeight, previewSizes);
        params.setPreviewSize(widthHeight[0], widthHeight[1]);

        // set Camera parameters
        mCamera.setParameters(params);

        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Camera.Size preferedVideoSize = params.getPreferredPreviewSizeForVideo();
        Camera.Size previewSize = params.getPreviewSize();
        Camera.Size pictureSize = params.getPictureSize();
        int previewFormat = params.getPreviewFormat();
        List<int[]> previewFPSRange = params.getSupportedPreviewFpsRange();
        int[] fpsRange = new int[2];
        params.getPreviewFpsRange(fpsRange);

        //Log.i("CAMERATEST: postExecute", "pictureSize Now: " + pictureSize.width + " x " + pictureSize.height);
        Log.i("CAMERATEST: postExecute", "preferedVideoSize Now: " + preferedVideoSize.width + " x " + preferedVideoSize.height);
        Log.i("CAMERATEST: postExecute", "previewSize Now: " + previewSize.width + " x " + previewSize.height);
        Log.i("CAMERATEST: postExecute", "pictureSize Now: " + pictureSize.width + " x " + pictureSize.height);
        Log.i("CAMERATEST: postExecute", "previewFormat Now: " + previewFormat);
        Log.i("CAMERATEST: postExecute", "previewFpsRate Now: " + fpsRange [0] + " " + fpsRange[1]);
        for (int[] i: previewFPSRange)
        {
            Log.i("CAMERATEST: postExecute", "previewFPSRAnge: " );
            for (int fps: i)
            {
                Log.i("CAMERATEST: postExecute", "fps: " + fps);
            }
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // The Surface has been created, now tell the camera where to prepareToDraw the preview.
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
            Camera.Size pictureSize = params.getPictureSize();
            int previewFormat = params.getPreviewFormat();
            List<int[]> previewFPSRange = params.getSupportedPreviewFpsRange();
            int[] fpsRange = new int[2];
            params.getPreviewFpsRange(fpsRange);

            //Log.i("CAMERATEST: postExecute", "pictureSize Now: " + pictureSize.width + " x " + pictureSize.height);
            Log.i("CAMERATEST: postExecute", "preferedVideoSize Now: " + preferedVideoSize.width + " x " + preferedVideoSize.height);
            Log.i("CAMERATEST: postExecute", "previewSize Now: " + previewSize.width + " x " + previewSize.height);
            Log.i("CAMERATEST: postExecute", "pictureSize Now: " + pictureSize.width + " x " + pictureSize.height);
            Log.i("CAMERATEST: postExecute", "previewFormat Now: " + previewFormat);
            Log.i("CAMERATEST: postExecute", "previewFpsRate Now: " + fpsRange [0] + " " + fpsRange[1]);
            for (int[] i: previewFPSRange)
            {
                Log.i("CAMERATEST: postExecute", "previewFPSRAnge: " );
                for (int fps: i)
                {
                    Log.i("CAMERATEST: postExecute", "fps: " + fps);
                }
            }

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
        Camera.Size previewSize = params.getPreviewSize();
        return previewSize;
    }

    public Camera.Size getPictureSize()
    {
        Camera.Size pictureSize = params.getPictureSize();
        return pictureSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        if(data == null)
        {
            return;
        }
        //onpreviewframe retorna la imagen (data) con el tamaña de preview size
        //Log.i("CameraPreview", "frame!");
        int width = params.getPreviewSize().width;
        int height = params.getPreviewSize().height;

        YuvImage yuv = new YuvImage(data, params.getPreviewFormat(), width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);
        byte[] bytes = out.toByteArray();
        //convertir a bitmap
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        //scalebitmap para llenar imageView
        bmp = Bitmap.createScaledBitmap(bmp, imageView.getWidth(), imageView.getHeight(), true);
        imageView.setImageBitmap(bmp);
        imageView2.setImageBitmap(bmp);

        if(sincronizar)
        {
            if(doc == null)
            {
                doc = new PhysicalHighlightProcessorAsync(hocr);
                doc.execute(bytes, MainActivityEstereo.filename);
                Log.i("CameraPreview", "frame: doc null");
            }
            if(doc.getStatus() == AsyncTask.Status.RUNNING)
            {
                Log.i("CameraPreview", "frame: task running");
                return;
            }
            else
            {
                if(doc.getStatus() == AsyncTask.Status.FINISHED)
                {
                    Log.i("CameraPreview", "frame: task finished");
                    doc = new PhysicalHighlightProcessorAsync(hocr);
                    doc.execute(bytes, MainActivityEstereo.filename);
                }
            }
        }
    }

    public int[] calcularPreviewSize(int reqWidth, int reqHeight, List<Camera.Size> sizes)
    {

        int[] widthHeight = new int[2];
        int[] widthHeightAnterior = new int[2];

        for (Camera.Size size: sizes)
        {
            if (size.width > reqWidth || size.height > reqHeight)
            {
                widthHeightAnterior[0] = size.width;
                widthHeightAnterior[1] = size.height;
            }
            else
            {
                widthHeight[0] = widthHeightAnterior[0];
                widthHeight[1] = widthHeightAnterior[1];
                break;
            }
        }

        return widthHeight;
    }
}
