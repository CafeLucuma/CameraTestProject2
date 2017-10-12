package com.example.oscar.OpenCVHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Oscar on 10-09-2017.
 */

public class ImageProcessor {

    Mat mat;
    private Bitmap image;
    private Bitmap image32;
    private Mat imageMat;
    private Mat outputImage;

    public ImageProcessor(byte[] data)
    {
        image = BitmapFactory.decodeByteArray(data, 0, data.length);
        //image32 = image.copy(Bitmap.Config.ARGB_8888, true);
        //transformar bitmap a mat
        imageMat = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC3);;
        image32 = image.copy(Bitmap.Config.ARGB_8888, true);
        Log.i("guardar bitmap", "image32 Width Height" + image32.getWidth() + " " + image32.getHeight());
        image.recycle();
        image = null;
        Utils.bitmapToMat(image32, imageMat);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2RGB,4);
        outputImage = new Mat();
        //image32.recycle();
        //image32 = null;
    }

    public Bitmap cleanImage()
    {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/req_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "inrange" + n + ".jpg";
        String fname2 = "bitwise" + n + ".jpg";
        String fname3 = "imageoriginal" + n + ".jpg";
        File file = new File(myDir, fname);
        File file2 = new File(myDir, fname2);
        File file3 = new File(myDir, fname3);
        Log.i("guardar bitmap", "" + file);

        FileOutputStream out3 = null;
        try {
            out3 = new FileOutputStream(file3);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        image32.compress(Bitmap.CompressFormat.JPEG, 90, out3);
        image32.recycle();
        image32 = null;


        //BGR BLUE GREEN RED
        //detectar texto en negro
        Core.inRange(imageMat, new Scalar(0, 0, 0), new Scalar(100, 100, 100), outputImage);
        //mat es BGR, bitmap es RGB, hay que cambiarlo
        //Imgproc.cvtColor(outputImage, outputImage, Imgproc.COLOR_BGR2RGB);
        Bitmap bm2 = Bitmap.createBitmap(outputImage.cols(), outputImage.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputImage, bm2);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bm2.compress(Bitmap.CompressFormat.JPEG, 90, out);
        bm2.recycle();

        //cambiar blanco a negro
        Core.bitwise_not(outputImage, outputImage);

        // convertir imagen limpia a bitmap:
        Bitmap bm = Bitmap.createBitmap(outputImage.cols(), outputImage.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputImage, bm);

        try {
            FileOutputStream out2 = new FileOutputStream(file2);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out2);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bm;
    }

}
