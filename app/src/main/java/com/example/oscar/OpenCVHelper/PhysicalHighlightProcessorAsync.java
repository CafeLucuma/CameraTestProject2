package com.example.oscar.OpenCVHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.oscar.Models.HOCRModel;
import com.example.oscar.Models.HighlightModel;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Oscar on 06-09-2017.
 */

//se encarga de generar las coordenadas de las words que fueron subrayadas en el documento físico


    //TODO hacer que detecte comentarios en este mismo task
    //puede hacerse después de detectar subrayado
public class PhysicalHighlightProcessorAsync extends AsyncTask<Object, Object, Void>
{
    private HOCRModel hocrModel;
    private HighlightModel highlightModel;
    private Bitmap image;
    private Bitmap image32;
    private Mat imageMat;
    private Bitmap imageOutputBitmap;
    private List<MatOfPoint> contours;
    private int minX = 3000, maxX = 0, minY = 3000, maxY = 0;
    private ArrayList<int[]> minX_MaxX;
    private ArrayList<int[]> minY_MaxY;
    private final String NOMBRE_ARCHIVO_SUBRAYADOS = "prueba-subrayados.txt";
    private final String NOMBRE_ARCHIVO_COMENTARIOS = "prueba-comentarios.jpg";

    public PhysicalHighlightProcessorAsync(HOCRModel hocrModel)
    {
        this.hocrModel = hocrModel;
        this.highlightModel = new HighlightModel(hocrModel.numLines);

        //inicializar arreglos
        minX_MaxX = new ArrayList<>();
        minY_MaxY = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(Object... params) {

        //Procesar comentarios primero
        Log.i("PhysicalDocumentHandler: words", "doingBackground");
        byte[] data = (byte[]) params[0];
        image = BitmapFactory.decodeByteArray(data, 0, data.length);
        //transformar bitmap a mat
        imageMat = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC3);
        image32 = image.copy(Bitmap.Config.ARGB_8888, true);
        image.recycle();
        image = null;
        Utils.bitmapToMat(image32, imageMat);
        //////////////RECORTAR IMAGEN
        Log.i("PhysicalDocumentHandler", "boundingBoxBloque[1] - image.width: " + hocrModel.getBlockBoundingBox()[1] + "-" + image32.getWidth());
        Rect rectCrop = new Rect(0, 0, image32.getWidth(), hocrModel.getBlockBoundingBox()[1]);
        //cortar rectangulo
        Mat imageOutputMat = imageMat.submat(rectCrop);
        //pasar a bitmap
        imageOutputBitmap = Bitmap.createBitmap(imageOutputMat.cols(), imageOutputMat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageOutputMat, imageOutputBitmap);
        /////////////////////////////////

        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2RGB,4);
        image32.recycle();
        image32 = null;

        Mat outputImage = new Mat();
        //BGR BLUE GREEN RED
        //detectar color subrayado
        Core.inRange(imageMat, new Scalar(70, 30, 90), new Scalar(130, 70, 170), outputImage);
        Log.i("OpenCV", "channel outputImage" + outputImage.channels());

        //ENCONTRAR CONTORNO DE LA IMAGEN
        contours = new ArrayList<>();
        Imgproc.findContours(outputImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(outputImage, contours, -1, new Scalar(255,255,255), 3);

        //erosión de imagen para rellenar el rectangulo
        int erosion_size = 5;
        int dilation_size = 5;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*erosion_size + 1, 2*erosion_size+1));

        //los pixeles subrayados se expanden para cerrar el rectangulo
        Imgproc.dilate(outputImage, outputImage, element);
        Imgproc.erode(outputImage, outputImage, element);
        Mat hierarchy2 = new Mat();
        List<MatOfPoint> contours2 = new ArrayList<>();
        Imgproc.findContours(outputImage, contours2, hierarchy2, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //contours a point
        int cantCont = 0;
        for (MatOfPoint points: contours2)
        {
            Log.i("OpenCV", "contour numero: " + cantCont);
            List<Point> lista = new ArrayList<>();
            Converters.Mat_to_vector_Point(points, lista);
            //encontrar los puntos más externos del subrayado
            for (Point point: lista)
            {
                Log.i("OpenCV", "contours: X " + point.x + " Y " + point.y);
                if(point.x > maxX)
                    maxX = (int) point.x;
                else
                if(point.x < minX)
                    minX = (int) point.x;

                if(point.y > maxY)
                    maxY = (int) point.y;
                else
                if(point.y < minY)
                    minY = (int) point.y;
            }
            Log.i("OpenCV", "contours: minX " + minX + " maxX " + maxX);
            Log.i("OpenCV", "contours: minY " + minY + " maxY " + maxY);
            //añadir extremos de points a arreglo de extremos correspondiente a X y Y
            int[] minmax = new int[2];
            int[] minmax2 = new int[2];
            minmax[0] = minX;
            minmax[1] = maxX;
            minX_MaxX.add(minmax);
            minmax2[0] = minY;
            minmax2[1] = maxY;
            minY_MaxY.add(minmax2);
            minX = 3000; maxX = 0; minY = 3000; maxY = 0;
            cantCont++;
        }

        for (int i = 0; i < minY_MaxY.size(); i++)
        {
            Log.i("OpenCV", i + ": Max X  y Min X" + minX_MaxX.get(i)[1] + " " + minX_MaxX.get(i)[0]
                    + "Max Y y Min Y " + minY_MaxY.get(i)[1] + " " + minY_MaxY.get(i)[0]);

        }

        //ver las words que estan en highlight
        //ver linea que esta subrayada
        int numLinea = 0;
        boolean siguienteContorno = false;
        //para cada linea
        for (int[] topBottomPixels: hocrModel.lineTopBottomPixels)
        {
            //para cada contorno de subrayado
            for(int cantidadContornos = 0; cantidadContornos < minX_MaxX.size(); cantidadContornos++)
            {
                Log.i("OpenCV", "Linea: " + numLinea + " num contorno: " + cantidadContornos);
                siguienteContorno = false;
                //si la parte de arriba del subrayado esta dentro de la parte de  abajo de la linea, esta linea se considera subrayada
                //hay que arreglarlo para mejorar calidad
                if((minY_MaxY.get(cantidadContornos)[0] < topBottomPixels[1]) && (minY_MaxY.get(cantidadContornos)[1] > topBottomPixels[0]))
                {
                    //bboxesLine contiene las bboxes de cada palabra en la linea i
                    ArrayList<int[]> bboxesLine = (ArrayList<int[]>) hocrModel.bboxesLine.get(numLinea);
                    int offsetWord = 0;
                    for (int[] bboxes: bboxesLine)
                    {
                        if(offsetWord >= hocrModel.wordsPerLine.get(numLinea).length)
                            break;
                        Log.i("OpenCV", "palabra: " + hocrModel.wordsPerLine.get(numLinea)[offsetWord]
                                + " bbox" + bboxes[0] + " " + bboxes[1] + " " + bboxes[2] + " " + bboxes[3]);
                        //si lado izquierdo de subrayado es menor que derecha de palabra
                        if(minX_MaxX.get(cantidadContornos)[0] < bboxes[2])
                        {
                            //si lado derecho de subrayado es mayor que derecha de palabra
                            if(minX_MaxX.get(cantidadContornos)[1] > bboxes[2])
                            {
                                //ver si 50% o mas esta subrayado, si es así, se considera subrayada
                                if(bboxes[2] - minX_MaxX.get(cantidadContornos)[0] >= (bboxes[2] - bboxes[0]) / 2)
                                {
                                    highlightModel.wordsIndex.get(numLinea).add(offsetWord);
                                    Log.i("OpenCV", "palabra añadida" + hocrModel.wordsPerLine.get(numLinea)[offsetWord] + " offset " + offsetWord);
                                    //save = true;
                                    //saveHighlights();
                                }

                                //resto de words en la linea
                                offsetWord++;
                                List<int[]> restWords = bboxesLine.subList(offsetWord, bboxesLine.size());
                                for (int[] bboxesRest: restWords)
                                {
                                    if(minX_MaxX.get(cantidadContornos)[1] > bboxesRest[0])
                                    {
                                        if(minX_MaxX.get(cantidadContornos)[1] <= bboxesRest[2])
                                        {
                                            //ver si abarca más de 50%
                                            if(minX_MaxX.get(cantidadContornos)[1] - bboxesRest[0] >= (bboxesRest[2] - bboxesRest[0]) / 2)
                                            {
                                                //no abarca más words -> siguiente contorno
                                                highlightModel.wordsIndex.get(numLinea).add(offsetWord);
                                                Log.i("OpenCV", "palabra añadida" + hocrModel.wordsPerLine.get(numLinea)[offsetWord] + " offset " + offsetWord);
                                                //saveHighlights();
                                                siguienteContorno = true;
                                                break;
                                            }
                                        }
                                        else
                                        {
                                            highlightModel.wordsIndex.get(numLinea).add(offsetWord);
                                            Log.i("OpenCV", "palabra añadida" + hocrModel.wordsPerLine.get(numLinea)[offsetWord] + " offset " + offsetWord);
                                        }
                                    }
                                    else
                                    {
                                        //no abarca más words -> siguiente contorno
                                        //TIENE QUE CONTINUAR CON EL SIGUIENTE CONTORNO
                                        siguienteContorno = true;
                                        break;
                                    }
                                    offsetWord++;
                                }
                            }
                            else
                            {
                                //ver si 50% o mas esta subrayado, si es así, se considera subrayada y termina
                                if(minX_MaxX.get(cantidadContornos)[1] - minX_MaxX.get(cantidadContornos)[0] > (bboxes[2] - bboxes[0]) / 2)
                                {
                                    highlightModel.wordsIndex.get(numLinea).add(offsetWord);
                                    Log.i("OpenCV", "palabra añadida" + hocrModel.wordsPerLine.get(numLinea)[offsetWord] + " offset " + offsetWord);
                                    //termina = true;
                                    //saveHighlights();
                                    break;
                                }
                            }
                        }
                        if(siguienteContorno)
                            break;
                        offsetWord++;
                    }
                }
            }
            numLinea++;
        }
        return null;
    }

    //se encarga de guardar las palaras que han sido subrayadas
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        File fileHighlights = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "CameraTestDocuments/" + NOMBRE_ARCHIVO_SUBRAYADOS);

        //guardar la imagen recortada
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "CameraTestDocuments/commentImages");

        if(!mediaStorageDir.exists())
            mediaStorageDir.mkdir();

        File fileComments = new File(mediaStorageDir, NOMBRE_ARCHIVO_COMENTARIOS);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileComments);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageOutputBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        imageOutputBitmap.recycle();

        if(fileHighlights.exists())
            fileHighlights.delete();


        //guardar subrayados
        FileOutputStream f = null;
        try {
            f = new FileOutputStream(fileHighlights);
            PrintWriter pw = new PrintWriter(f);
            //write en doc
            int numLinea = 0;
            for (ArrayList<Integer> offsetPorLinea: highlightModel.wordsIndex)
            {
                if(!offsetPorLinea.isEmpty())
                {
                    pw.println(numLinea);
                    Log.i("PhysicalDocumentHandler: words", "linea" + numLinea);
                    for(int i = 0; i < offsetPorLinea.size(); i++)
                    {
                        if(i + 1 == offsetPorLinea.size())
                            pw.println(offsetPorLinea.get(i));
                        else
                            pw.print(offsetPorLinea.get(i) + " ");
                        Log.i("PhysicalDocumentHandler: words", "offset " + offsetPorLinea.get(i));
                    }
                }
                numLinea++;
            }
            pw.flush();
            pw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
