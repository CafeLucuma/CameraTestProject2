package com.example.oscar.TesseractHelper;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import com.example.oscar.OpenCVHelper.ImageProcessor;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;

/**
 * Created by Oscar on 02-11-2017.
 */

public class HOCRGenerator extends AsyncTask<Object, String, ResultIterator> {

    private byte[] imageByte;
    private Bitmap image;
    private TessBaseAPI mTess = null;
    private String datapath;
    private Camera.Size previewSize;
    private static final String LANG = "eng";

    //params[0] = imagen en bytes[]
    //params[1] = datapath
    //params[2] = camera previewSize
    @Override
    protected ResultIterator doInBackground(Object... params) {

        imageByte = (byte[]) params[0];
        datapath = (String) params[1];
        previewSize = (Camera.Size) params[2];
        ImageProcessor imageProcessor = new ImageProcessor(imageByte);
        image = imageProcessor.cleanImage();
        //image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
        image = Bitmap.createScaledBitmap(image, previewSize.width, previewSize.height, false);
        Log.i("CAMERATEST: HOCR: image width height: ",  image.getWidth() + " " + image.getHeight());

        if(mTess == null)
        {
            mTess = new TessBaseAPI();
            mTess.init(datapath, LANG);
        }

        String OCRresult = null;
        String HOCRresult = null;
        mTess.setImage(image);

        //OCRresult = mTess.getUTF8Text();
        HOCRresult = mTess.getHOCRText(0);
        ResultIterator iterator = mTess.getResultIterator();

        return iterator;
    }

    @Override
    protected void onPostExecute(ResultIterator resultIterator) {
        super.onPostExecute(resultIterator);


    }
}