package com.example.oscar.DocumentHelper;

import android.os.Environment;
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

//se encarga de leer los documentos generados por OCR y por el subrayado del documento digital
public class DocumentReader
{
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

    //funcion que lee si el archivo de hocr generado por tesseract existe
    //si existe lo carga
    public static HOCRModel readHOCR(String filename)
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

            //lee bounding box de bloque
            linea = br.readLine();
            String[] bloqueBBString = linea.split("\\s+");
            int[] bloqueBBInt = new int[4];
            //left top right bottom
            bloqueBBInt[0] = Integer.parseInt(bloqueBBString[0]);
            bloqueBBInt[1] = Integer.parseInt(bloqueBBString[1]);
            bloqueBBInt[2] = Integer.parseInt(bloqueBBString[2]);
            bloqueBBInt[3] = Integer.parseInt(bloqueBBString[3]);
            hocr.setBlockBoundingBox(bloqueBBInt);

            //lee número de línea
            while ((linea = br.readLine()) != null)
            {
                //lee numero de linea y top bottom pixels
                String[] numTopBot = linea.split("\\s+");
                int[] topBottom = new int[2];
                hocr.numLines++;
                lineaNum = Integer.parseInt(numTopBot[0]);
                topBottom[0] = Integer.parseInt(numTopBot[1]);
                topBottom[1] = Integer.parseInt(numTopBot[2]);
                hocr.lineTopBottomPixels.add(topBottom);
                //lee palabras en la linea
                palabras = br.readLine();
                hocr.wordsPerLine.add(palabras.split("\\s+"));

                ArrayList<int[]> bboxesPorLinea = new ArrayList<>();
                //hocr.bboxes = new ArrayList<>();

                for(int i = 0; i < hocr.wordsPerLine.get(lineaNum).length; i++)
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

            Log.i("DocumentReader: words", "bloque:" + bloqueBBInt[0] + bloqueBBInt[1] + bloqueBBInt[2] + bloqueBBInt[3]);
            for (String[] palabrasPorLinea: hocr.wordsPerLine)
            {
                int a = 0;
                Log.i("DocumentReader: words", "linea" + a);
                for (String word: palabrasPorLinea)
                {
                    Log.i("DocumentReader: words", word);
                }
                a++;
            }

            for (Object uno: hocr.bboxesLine)
            {
                ArrayList<int[]> arrayListInt = (ArrayList<int[]>) uno;
                for (int[] dos: arrayListInt)
                {
                    Log.i("DocumentReader: bboxesLine", " bbox: " + dos[0] + " " +dos[1] + " " +dos[2] + " " +dos[3]);
                }
            }

            for (int[] bb: hocr.bboxes)
            {
                Log.i("DocumentReader: hocr.bboxes", " bbox: " + bb[0] + " " + bb[1] + " " + bb[2] + " " + bb[3]);
            }

            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        return hocr;
    }

    public static HighlightModel readHighlights(int numLines)
    {
        Log.i("DocumentReader: highlightModel", "GETHIGHLIGHTS");

        file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "CameraTestDocuments/" + NOMBRE_ARCHIVO_HIGHLIGHTS);

        if(!file.exists())
            return null;

        HighlightModel hm = new HighlightModel(numLines);
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String linea;
            ArrayList<int[]> indexesArray = new ArrayList<>();

            //lee el index de las palabras subrayadas
            while ((linea = br.readLine()) != null)
            {
                //como puede haber más de un index, se separa por espacio
                String[] indexesString = linea.split("\\s+");
                int[] indexes = new int[indexesString.length];
                //pasar el offset de string a int
                int i = 0;
                for (String index: indexesString)
                {
                    int offsetPalabra = Integer.parseInt(index);
                    indexes[i] = offsetPalabra;
                    i++;
                }
                indexesArray.add(indexes);
            }
            hm.setWordsAbsoluteIndex(indexesArray);

            for (int[] indexArray: hm.getWordsAbsoluteIndex())
            {
                for (int index: indexArray)
                {
                    Log.i("DocumentReader: highlightModel", "offset palabra " + index);
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

    public static ArrayList<CommentModel> readComments()
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
                comment.setOffsetWordsCommented(new int[]{offset0, offset1});

                //leer index words comentadas
                line = br.readLine();
                Log.i("Comments", "Segunda linea leida" + line);
                String[] lineAux2 = line.split("\\s");
                ArrayList<Integer> indexPalabrasComentadas = new ArrayList<>();
                for (String index: lineAux2)
                {
                    indexPalabrasComentadas.add(Integer.parseInt(index));
                }
                comment.setIndexWordsCommented(indexPalabrasComentadas);

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
                comment.setComment(lineAux3.toString());

                comments.add(comment);
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }

        for (CommentModel com : comments)
        {
            Log.i("Comments", "Comentarios: " + com.getComment() + " " +
                    com.getOffsetWordsCommented()[0] + "-" + com.getOffsetWordsCommented()[1]);
            for (int ind: com.getIndexWordsCommented())
            {
                Log.i("Comments", "Index words seleccionadas: " + ind);
            }
        }

        return comments;
    }
}
