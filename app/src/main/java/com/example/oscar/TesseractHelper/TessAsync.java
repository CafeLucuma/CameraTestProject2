package com.example.oscar.TesseractHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import com.example.oscar.OpenCVHelper.ImageProcessor;
import com.example.oscar.cameratest.MainActivity;
import com.example.oscar.cameratest.MainActivityNormal;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;

/**
 * Created by Oscar on 17-10-2017.
 */


//TODO cuando task termine, avisar a main thread para que envíe a rectDrawer a dibujar
public class TessAsync extends AsyncTask<Object, String, ArrayList<int[]>> {

    private byte[] imageByte;
    private Bitmap image;
    private String searchedWord;
    private TessBaseAPI mTess = null;
    private String datapath;
    private Camera.Size previewSize;
    private ArrayList<String> words = new ArrayList<>();
    private ArrayList<int[]> bboxes = new ArrayList<>();
    private ArrayList<int[]> bboxesToDraw = new ArrayList<>();
    private static final String LANG = "eng";

    //params[0] = imagen en bytes[]
    //params[1] = string con searchedWord a buscar
    //params[2] = datapath
    //params[3] = camera previewSize
    @Override
    protected ArrayList<int[]> doInBackground(Object... params) {

        imageByte = (byte[]) params[0];
        searchedWord = (String) params[1];
        datapath = (String) params[2];
        previewSize = (Camera.Size) params[3];
        int cam = (int) params[4];
        ImageProcessor imageProcessor = new ImageProcessor(imageByte);
        image = imageProcessor.cleanImage();
        //image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
        if(cam == 1)
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

        Log.i("CAMERATEST: HOCR", HOCRresult);


        words.clear();
        bboxes.clear();
        bboxesToDraw.clear();

        //recorrer la lista de palabras del texto reconocido
        iterator.begin();
        words.add(iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD));
        Log.i("CAMERATEST: iterator", "word: " + iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD));
        bboxes.add(iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD));

        if(searchedWord.equalsIgnoreCase(words.get(words.size() - 1)))
        {
            bboxesToDraw.add(iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD));
        }

        while(iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD))
        {
            words.add(iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD));
            bboxes.add(iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD));
            Log.i("CAMERATEST: iterator", "word: " + iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD));

            if(searchedWord.equalsIgnoreCase(words.get(words.size() - 1).replaceAll("[^a-zA-Z]", "")))
            {
                bboxesToDraw.add(iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD));
            }
        }

        for (int[] i: bboxes
                ) {
            Log.i("CAMERATEST: iterator", "bbox: " + i[0] + " " + i[1] + " "
                    + i[2] + " "+ i[3]);
        }

        Log.i("CAMERATEST: iterator", "words-bbox: " + words.size() + " " + bboxes.size());

        return bboxesToDraw;
    }
}
