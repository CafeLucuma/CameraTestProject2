package com.example.oscar.OpenCVHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * Created by Oscar on 24-10-2017.
 */

//clase que se encarga de obtener el recuadro donde se realizan los comentarios
//en el documento físico


//necesita: imagen, boundingbox de bloque
public class PhysicalCommentsProcessorAsync extends AsyncTask<Object, Void, Bitmap>
{
    private final String TAG = "PHYSICALCOMMENTPROCESSOR";

    @Override
    protected Bitmap doInBackground(Object... params) {
        byte[] imageByte = (byte[]) params[0];
        Bitmap image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
        int[] boundingBoxBloque = (int[]) params[1];
        Log.i(TAG, "boundingBoxBloque[1] - image.height: " + boundingBoxBloque[1] + "-" + image.getHeight());
        //sacar relación entre boundingBoxBloque y imagen para cortar solo parte superior
        //double reasonHeight = (double) (boundingBoxBloque[1] / image.getHeight());
        //Log.i(TAG, "reasonHeight: " + reasonHeight);

        //cortar parte superior de la imagen, que quede arriba del bloque de texto
        //boundingBoxBloque[1] = (int) (boundingBoxBloque[1] / reasonHeight);
        //Log.i(TAG, "boundingBoxBloque[1] escala: " + boundingBoxBloque[1]);
        Mat imageMat = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC3);
        Bitmap image32 = image.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(image32, imageMat);

        //se obtiene el rectangulo a cortar
        //coordenadas: top, left, right, bottom
        Log.i(TAG, "boundingBoxBloque[1] - image.width: " + boundingBoxBloque[1] + "-" + image.getWidth());
        Rect rectCrop = new Rect(0, 0, image32.getWidth(), boundingBoxBloque[1]);
        //cortar rectangulo
        Mat imageOutputMat = imageMat.submat(rectCrop);
        //pasar a bitmap
        Bitmap imageOutputBitmap = Bitmap.createBitmap(imageOutputMat.cols(), imageOutputMat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageOutputMat, imageOutputBitmap);

        image.recycle();

        return imageOutputBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        //guardar la imagen recortada
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "CameraTestDocuments/commentImages");
        if(!mediaStorageDir.exists())
            mediaStorageDir.mkdir();

        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String filename = "comment" + n + ".jpg";
        File file = new File(mediaStorageDir, filename);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        bitmap.recycle();
    }
}
