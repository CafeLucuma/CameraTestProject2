package com.example.oscar.DocumentHelper;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import com.example.oscar.Models.CommentModel;
import com.example.oscar.Models.HOCRModel;
import com.example.oscar.Models.HighlightModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Oscar on 18-08-2017.
 */

public class DocumentHandler {

    private static File mediaStorageDir;
    private static File file;
    private static final String NOMBRE_ARCHIVO_HIGHLIGHTS = "prueba-highlights-text-editor.txt";
    public static final String NOMBRE_ARCHIVO_COMENTARIOS = "comentarios.txt";

    public static boolean docExists(String filename) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "CameraTestDocuments");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            Log.i("CAMERATEST: DOC", "directorio no existe");
            if (!mediaStorageDir.mkdirs()) {
                Log.i("CAMERATEST: DOC", "failed to create directory");
                return false;
            } else {
                Log.i("CAMERATEST: DOC", "file doesnt exists, directorio creado");
                return false;
            }
        }

        File file = new File(mediaStorageDir + "/" + filename);
        Log.i("CAMERATEST: DOC", "filepath: " + file);

        if(file.exists())
        {
            Log.i("CAMERATEST: DOC", "file exists");
            return true;
        }
        else
        {
            Log.i("CAMERATEST: DOC", "file doesnt exist: " + file);
            return false;
        }
    }

    public static HOCRModel getHOCR(String filename)
    {
        file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "CameraTestDocuments/" + "prueba-highlights.txt");

        HOCRModel hocr = new HOCRModel();

        //Read text from file
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String linea;
            String palabras;
            String bboxesStringLine;
            String[] bboxesStringArr;
            int lineaNum;
            hocr.bboxes = new ArrayList<>();

            //lee número de línea
            while ((linea = br.readLine()) != null) {
                String[] numTopBot = linea.split("\\s+");
                int[] topBottom = new int[2];
                hocr.numLines++;
                lineaNum = Integer.parseInt(numTopBot[0]);
                topBottom[0] = Integer.parseInt(numTopBot[1]);
                topBottom[1] = Integer.parseInt(numTopBot[2]);
                hocr.lineTopBottomPixels.add(topBottom);
                //lee palabras en la linea
                palabras = br.readLine();
                hocr.wordsLine.add(palabras.split("\\s+"));

                ArrayList<int[]> bboxesPorLinea = new ArrayList<>();
                //hocr.bboxes = new ArrayList<>();

                for(int i = 0; i < hocr.wordsLine.get(lineaNum).length; i++)
                {
                    int[] bboxes = new int[4];
                    //lee los 4 bbox en un string array
                    bboxesStringLine = br.readLine();
                    bboxesStringArr = bboxesStringLine.split("\\s+");
                    //separa los bbox y parsea a int en un array de int
                    for (int j = 0; j < bboxesStringArr.length; j++)
                    {
                        bboxes[j] = Integer.parseInt(bboxesStringArr[j]);
                    }
                    bboxesPorLinea.add(bboxes);
                    hocr.bboxes.add(bboxes);
                }
                //añadir array de bboxes a linkedlist
                hocr.bboxesLine.add(bboxesPorLinea.clone());
            }
            //log
            for (String[] palabrasPorLinea: hocr.wordsLine)
            {
                int a = 0;
                Log.i("DocumentHandler: palabras", "linea" + a);
                for (String word: palabrasPorLinea)
                {
                    Log.i("DocumentHandler: palabras", word);
                }
                a++;
            }

            for (Object uno: hocr.bboxesLine)
            {
                ArrayList<int[]> arrayListInt = (ArrayList<int[]>) uno;
                for (int[] dos: arrayListInt)
                {
                    Log.i("DocumentHandler: bboxesLine", " bbox: " + dos[0] + " " +dos[1] + " " +dos[2] + " " +dos[3]);
                }
            }

            for (int[] bb: hocr.bboxes)
            {
                Log.i("DocumentHandler: hocr.bboxes", " bbox: " + bb[0] + " " + bb[1] + " " + bb[2] + " " + bb[3]);
            }

            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        return hocr;
    }

    public static HighlightModel getHighlights(int numLines)
    {
        Log.i("DocumentHandler: highlightModel", "GETHIGHLIGHTS");

        file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "CameraTestDocuments/" + NOMBRE_ARCHIVO_HIGHLIGHTS);

        if(!file.exists())
            return null;

        HighlightModel hm = new HighlightModel(numLines);
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String linea;
            String offsetPalabraString;
            int lineaNum;

            //lee número de línea
            while ((linea = br.readLine()) != null) {
                lineaNum = Integer.parseInt(linea);
                offsetPalabraString = br.readLine();
                //palabras highlight en array de string
                String[] palabrasArray = offsetPalabraString.split("\\s+");
                //pasar el offset de string a int
                for (String palabra: palabrasArray)
                {
                    //añadir arrayList de palabras highlightmodel
                    //hm.palabras.get(lineaNum).add(palabra);
                    int offsetPalabra = Integer.parseInt(palabra);
                    hm.wordOffset.get(lineaNum).add(offsetPalabra);
                }

            }
            int a = 0;
            for (ArrayList<Integer> uno: hm.wordOffset)
            {
                Log.i("DocumentHandler: highlightModel", "linea " + a);
                a++;
                for (int palabra: uno)
                {
                    Log.i("DocumentHandler: highlightModel", "offset palabra " + palabra);
                }

            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hm;
    }

    public static ArrayList<CommentModel> cargarComentarios()
    {
        file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "CameraTestDocuments/" + NOMBRE_ARCHIVO_COMENTARIOS);

        if(!file.exists())
            return null;

        ArrayList<CommentModel> comments = new ArrayList<>();

        //Read text from comments
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                Log.i("Comments", "Primera linea leida" + line);

                CommentModel comment = new CommentModel();
                //lee offset comentario
                String[] lineAux = line.split("\\s");
                int offset0 = Integer.parseInt(lineAux[0]);
                int offset1 = Integer.parseInt(lineAux[1]);
                comment.setOffsetComentarios(new int[]{offset0, offset1});

                //leer index palabras comentadas
                line = br.readLine();
                Log.i("Comments", "Segunda linea leida" + line);
                String[] lineAux2 = line.split("\\s");
                ArrayList<Integer> indexPalabrasComentadas = new ArrayList<>();
                for (String index: lineAux2)
                {
                    indexPalabrasComentadas.add(Integer.parseInt(index));
                }
                comment.setIndexPalabrasSeleccionadas(indexPalabrasComentadas);

                //leer comentarios
                line = br.readLine();
                Log.i("Comments", "Tercera linea leida" + line);
                StringBuilder lineAux3 = new StringBuilder();
                while(!line.matches("[\\n\\r]+") && !line.isEmpty())
                {
                    lineAux3.append(line);
                    lineAux3.append('\n');
                    line = br.readLine();
                    Log.i("Comments", "Tercera linea leida" + line);
                }
                comment.setComentario(lineAux3.toString());

                comments.add(comment);
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }

        for (CommentModel com : comments)
        {
            Log.i("Comments", "Comentarios: " + com.getComentario() + " " +
                    com.getOffsetComentarios()[0] + "-" + com.getOffsetComentarios()[1]);
            for (int ind: com.getIndexPalabrasSeleccionadas())
            {
                Log.i("Comments", "Index palabras seleccionadas: " + ind);
            }
        }

        return comments;
    }

}
