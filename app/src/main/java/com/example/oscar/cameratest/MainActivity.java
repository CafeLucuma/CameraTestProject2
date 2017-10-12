package com.example.oscar.cameratest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.oscar.CameraHelper.CameraPreview;
import com.example.oscar.CameraHelper.CameraSaveFile;
import com.example.oscar.CameraHelper.CameraSourcePreview;
import com.example.oscar.DocumentHelper.DocumentHandler;
import com.example.oscar.DrawHelper.RectangleDrawer;
import com.example.oscar.Models.CommentModel;
import com.example.oscar.Models.HOCRModel;
import com.example.oscar.Models.HighlightModel;
import com.example.oscar.OpenCVHelper.ImageProcessor;
import com.example.oscar.OpenCVHelper.PhysicalDocumentFunctions;
import com.google.android.gms.vision.barcode.Barcode;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Policy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Manifest;

import static android.R.attr.left;
import static android.R.attr.right;

public class MainActivity extends Activity {

//TODO inicializar commentAdapter y listview 
    public static final int REQUEST_CODE = 100;
    public static final int PERMISSION_REQUEST = 200;
    private static final String TAG_APP = "MainActivity";
    private ArrayList<String> words = new ArrayList<>();
    private ArrayList<int[]> bboxes = new ArrayList<>();
    private ArrayList<int[]> bboxesToDraw = new ArrayList<>();
    private ArrayList<CommentModel> comments;
    private MenuItem sincronizar;
    private MenuItem activarComentarios;
    private Button searchButton;
    private Button captureButton;
    private EditText editView;
    private FrameLayout preview;
    private RelativeLayout relativeLayout;
    private ListView listViewComments;
    private RectangleDrawer rect;
    private Camera mCamera = null;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isTakingPicture;
    private String datapath;
    private HOCRModel hocr;
    private HighlightModel hm;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            new TesseractHelper().execute(data, editView.getText().toString());
            mCamera.startPreview();
            isTakingPicture = false;
        }
    };


    //cargar opencv
    static {
        if(!OpenCVLoader.initDebug())
            Log.i(TAG_APP, "OpenCV not loaded");
        else
        {
            Log.i(TAG_APP, "OpenCV loaded");
        }
    }



    //resultado de actividad escanear documento
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("CAMERATEST: Main", "PASA activity result");
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            if(data != null)
            {
                Barcode barcode =  data.getParcelableExtra("barcode");
                boolean existe = DocumentHandler.docExists(barcode.displayValue.toString());
                if(existe)
                {
                    Toast.makeText(getApplicationContext(), "QR: " + "'" + barcode.displayValue + "'" + " existe", Toast.LENGTH_LONG).show();
                    //hacer sincronizar clickable
                    sincronizar.setEnabled(true);
                    activarComentarios.setEnabled(true);
                    hocr = DocumentHandler.getHOCR(barcode.displayValue.toString());
                    //cargar comentarios del documento físico
                    comments = DocumentHandler.cargarComentarios();
                    if(comments == null)
                    {
                        activarComentarios.setEnabled(false);
                        Toast.makeText(this, "No se pudieron cargar los comentrios", Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        List<String> comentariosString = new ArrayList<>();
                        for (CommentModel com: comments)
                        {
                            comentariosString.add(com.getComentario());
                        }
                        CustomCommentAdapter adapter = new CustomCommentAdapter(getApplicationContext(), R.layout.comment_row, comentariosString);
                        listViewComments.setAdapter(adapter);
                        listViewComments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Log.i("CAMERATEST: LISTVIEW, Position: ", " " + position);
                                Log.i("CAMERATEST: LISTVIEW, Id: ", " " + id);

                                //highlisght a palabra comentada en pantalla
                                bboxesToDraw.clear();
                                ArrayList<Integer> indexPalabrasHighlight =  comments.get(position).getIndexPalabrasSeleccionadas();
                                for (int indexPalabra : indexPalabrasHighlight)
                                {
                                    int[] bboxes = new int[4];
                                    bboxes[0] = hocr.bboxes.get(indexPalabra)[0];
                                    bboxes[1] = hocr.bboxes.get(indexPalabra)[1];
                                    bboxes[2] = hocr.bboxes.get(indexPalabra)[2];
                                    bboxes[3] = hocr.bboxes.get(indexPalabra)[3];

                                    bboxesToDraw.add(bboxes);
                                }
                                draw(false);
                            }
                        });
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "QR: " + "'" + barcode.displayValue + "'" + " No existe", Toast.LENGTH_LONG).show();
            }
        }
        relativeLayout.bringToFront();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        sincronizar = menu.findItem(R.id.action_sincronize);
        activarComentarios = menu.findItem(R.id.action_activarComentarios);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_scan:
                //detener camara para fotos
                //mCamera.release();
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;

            //sincronizar palabras subrayadas de documento fisico con digital y viceversa
            case R.id.action_sincronize:
                //obtener palabras en highlight de documento digital
                hm = DocumentHandler.getHighlights(hocr.numLines);
                mPreview.setSincronizar(true);
                mPreview.setHocr(hocr);
                sincronizarHilight();

                break;

            case R.id.action_activarComentarios:
                //hacer comentarios visibles
                if(listViewComments != null)
                {
                    if(listViewComments.getVisibility() == View.INVISIBLE)
                    {
                        listViewComments.setVisibility(View.VISIBLE);
                        activarComentarios.setTitle("Desactivar Comentarios");
                    }
                    else
                    {
                        listViewComments.setVisibility(View.INVISIBLE);
                        activarComentarios.setTitle("Activar Comentarios");
                    }

                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }

        datapath = getFilesDir() + "/tesseract/";
        checkFile(new File(datapath + "tessdata/"));

        init();
    }


    //hacer setlayoutparams con parametros que soporta camara, y hacerlos igual a picture size


    //inicializa botones, views y camara
    private void init()
    {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST);
        }

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        Camera.Size previewSize = mPreview.getPreviewSize();
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        relativeLayout = (RelativeLayout) findViewById(R.id.rlLayout);
        preview.setLayoutParams(new RelativeLayout.LayoutParams(previewSize.width, previewSize.height));
        preview.addView(mPreview);
        listViewComments = (ListView) findViewById(R.id.lvComments);

        //para dibujar los rectangulos en las palabras
        rect = (RectangleDrawer) findViewById(R.id.rdRect);
        relativeLayout.bringToFront();

        searchButton = (Button) findViewById(R.id.btnSearch);
        searchButton.bringToFront();
        captureButton = (Button) findViewById(R.id.btnWrite);
        captureButton.bringToFront();
        editView = (EditText) findViewById(R.id.etWrite);
        editView.bringToFront();


        rect.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("CAMERATEST: rectangledrawrew", "se toco la pantalla");
                Log.i("CAMERATEST: postExecute", "framelayout width height: " + preview.getWidth() + " " + preview.getHeight());
                Log.i("CAMERATEST: postExecute", "razon de width: " + (double)(2560 / preview.getWidth() ));
                rect.clear();
                relativeLayout.bringToFront();
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureButton.setVisibility(View.VISIBLE);
                editView.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.INVISIBLE);
            }
        });

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //ver si el usuario escribió una palabra
                if(editView.getText().toString().matches(""))
                {
                    searchButton.setVisibility(View.VISIBLE);
                    captureButton.setVisibility(View.INVISIBLE);
                    editView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Debe introducir una palabra", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    searchButton.setVisibility(View.VISIBLE);
                    captureButton.setVisibility(View.INVISIBLE);
                    editView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Buscó la palabra: " +
                            editView.getText().toString(), Toast.LENGTH_SHORT).show();
                    if(!isTakingPicture)
                    {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                        isTakingPicture = true;
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
        Log.i("CAMERATEST: ONPAUSE", "PASA POR ONPAUSE");
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
            Log.i("CAMERATEST: ONPAUSE", "PASA releasemedia");
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();        // release the camera for other applications

            mCamera = null;
            Log.i("CAMERATEST: ONPAUSE", "PASA releasecamera");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("CAMERATEST: ONPAUSE", "PASA onResume");
        // Get the Camera instance as the activity achieves full user focus
        if (mCamera == null) {
            Log.i("CAMERATEST: ONPAUSE", "PASA nuevo camera.open");
            mCamera = Camera.open();
            mPreview = new CameraPreview(this, mCamera);
            preview.addView(mPreview);
            Log.i("CAMERATEST: ONPAUSE", "PASA nuevo preview.addView");
        }
    }

    private void copyFiles() {
        try {
            //location we want the file to be at
            String filepath = datapath + "/tessdata/eng.traineddata";
            Log.i("CAMERATEST: copyFiles", "Datapath: "+datapath);

            //get access to AssetManager
            AssetManager assetManager = getAssets();

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

    private void checkFile(File dir) {
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


    //la camara saca las fotos de width = 2560 y height = 1920 pixeles
    public void draw(boolean sinc)
    {
        //sacar razón entre 2560/camera.width
        //y 1920/camera.height
        //y dividir boundingbox por esa razón

        //layout in the activity that the cameraView will placed in
        int layoutWidth = preview.getWidth();
        int layoutHeight = preview.getHeight();
        Camera.Size previewSize = mPreview.getPreviewSize();
        int previewWidth = previewSize.width;
        int previewHeight = previewSize.height;

        //razón del ancho
        double reasonWidth = (double) previewWidth / layoutWidth;
        //razón del largo
        double reasonHeight = (double) previewHeight / layoutHeight;


        for (int[] bb: bboxesToDraw)
        {
            //left y right
            bb[0] = (int) (bb[0] / reasonWidth);
            bb[2] = (int) (bb[2] / reasonWidth);

            //top y bottom
            bb[1] = (int) (bb[1] / reasonHeight);
            bb[3] = (int) (bb[3] / reasonHeight);
            Log.i("CAMERATEST: draw", "left top right bottom: " + bb[0] + " " + bb[1] + " "+ bb[2] + " " + bb[3]);
        }

        rect.setParameters(bboxesToDraw, sinc);
        rect.bringToFront();
    }

    public void sincronizarHilight()
    {

        if(hm == null)
            return;

        //para cada linea de higlightModel, buscar esa palabra en hocrModel
        //una vez que se encuentra, obtener su índice, y buscar ese indice en bboxes
        //ese bboxes mandarlo a draw

        ///////para cada linea de higlight
        bboxesToDraw.clear();

        //linea
        int i = 0;

        LinkedList bboxesLineCopy = (LinkedList) hocr.bboxesLine.clone();
        //TODO top y bottom deben ser de la linea (hocr.lineTopBottomPixels)
        for (ArrayList<Integer> palabrasLinea: hm.wordOffset)
        {
            //para cada palabra
            for (int offsetPalabra: palabrasLinea)
            {
                //buscar bbox correspondiente
                int[] bboxex = new int[4];
                ArrayList<int[]> bboxLine = (ArrayList<int[]>) bboxesLineCopy.get(i);

                bboxex[0] = bboxLine.get(offsetPalabra)[0];
                bboxex[1] = hocr.lineTopBottomPixels.get(i)[0];
                bboxex[2] = bboxLine.get(offsetPalabra)[2];
                bboxex[3] = hocr.lineTopBottomPixels.get(i)[1];
                bboxesToDraw.add(bboxex);
            }
            i++;
        }
        draw(true);
    }

    public class TesseractHelper extends AsyncTask<Object, String, int[]> {

        private byte[] imageByte;
        private Bitmap image;
        private String palabra;
        private TessBaseAPI mTess = null;
        private static final String LANG = "eng";

        //params[0] = imagen en bytes[]
        //params[1] = string con palabra a buscar
        @Override
        protected int[] doInBackground(Object... params) {

            imageByte = (byte[]) params[0];
            ImageProcessor imageProcessor = new ImageProcessor(imageByte);
            image = imageProcessor.cleanImage();
            Camera.Size previewSize = mPreview.getPreviewSize();
            image = Bitmap.createScaledBitmap(image, previewSize.width, previewSize.height, false);
            Log.i("CAMERATEST: HOCR: image width height: ",  image.getWidth() + " " + image.getHeight());
            palabra = (String) params[1];

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

            //recorrer la lista de alabras del texto reconocido
            iterator.begin();
            words.add(iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD));
            Log.i("CAMERATEST: iterator", "word: " + iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD));
            bboxes.add(iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD));

            if(palabra.equalsIgnoreCase(words.get(words.size() - 1)))
            {
                bboxesToDraw.add(iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD));
            }

            while(iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD))
            {
                words.add(iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD));
                bboxes.add(iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD));
                Log.i("CAMERATEST: iterator", "word: " + iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD));

                if(palabra.equalsIgnoreCase(words.get(words.size() - 1)))
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

            return bboxes.get(0);
        }

        @Override
        protected void onPostExecute(int[] s)
        {
            super.onPostExecute(s);

            if(bboxesToDraw.isEmpty())
                Toast.makeText(getApplicationContext(), "No se encontró la palabra buscada", Toast.LENGTH_SHORT).show();
            else
                draw(false);
        }
    }
}
