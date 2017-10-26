package com.example.oscar.TesseractHelper;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Oscar on 17-10-2017.
 */

public class TesseractFileReader {

    private Context context;
    private String datapath;

    public TesseractFileReader(Context context, String datapath)
    {
        this.context = context;
        this.datapath = datapath;
    }

    private void copyFiles() {
        try {
            //location we want the file to be at
            String filepath = datapath + "/tessdata/eng.traineddata";
            Log.i("CAMERATEST: copyFiles", "Datapath: "+ datapath);

            //get access to AssetManager
            AssetManager assetManager = context.getAssets();

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();
            Log.i("CAMERATEST: copyFiles", "archivo creado");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkFile(File dir) {
        Log.i("CAMERATEST: checkFile", "adentro de checkfile");
        //directory does not exist, but we can successfully create it
        if (!dir.exists() && dir.mkdirs()){
            Log.i("CAMERATEST: checkFile", "Directorio no existe, pero se crea");
            copyFiles();
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            Log.i("CAMERATEST: checkFile", "Directorio existe");
            String datafilepath = datapath + "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                Log.i("CAMERATEST: checkFile", "Directorio existe, pero no esta el archivo");
                copyFiles();
            }
        }
    }
}
