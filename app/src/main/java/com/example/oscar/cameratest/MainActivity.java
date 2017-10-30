package com.example.oscar.cameratest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.oscar.CameraHelper.CameraPreview;
import com.example.oscar.DocumentHelper.DocumentReader;
import com.example.oscar.DrawHelper.CommentDrawer;
import com.example.oscar.DrawHelper.RectangleDrawer;
import com.example.oscar.Models.CommentModel;
import com.example.oscar.Models.HOCRModel;
import com.example.oscar.Models.HighlightModel;
import com.example.oscar.TesseractHelper.TessAsync;
import com.example.oscar.TesseractHelper.TesseractFileReader;
import com.google.android.gms.vision.barcode.Barcode;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity {

    //TODO arreglar rectangleDrawer para dibujar lineas entre comentarios
    public static final int REQUEST_CODE = 100;
    public static final int PERMISSION_REQUEST = 200;
    private static final String TAG_APP = "MainActivity";
    private ArrayList<CommentModel> comments;

    public ArrayList<int[]> getBboxexComment() {
        return bboxexComment;
    }

    public void setBboxexComment(ArrayList<int[]> bboxexComment) {
        this.bboxexComment = bboxexComment;
    }

    private ArrayList<int[]> bboxexComment;
    private ArrayList<int[]> linesFinishStart;
    public static String datapath;
    private MenuItem sincronizar;
    private MenuItem activarComentarios;
    private Button searchButton;
    private Button captureButton;
    private EditText editView;
    private FrameLayout preview;
    private RelativeLayout relativeLayout;
    private ListView listViewComments;
    private RectangleDrawer rect;
    private CommentDrawer commentDrawer;
    private Camera mCamera = null;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isTakingPicture;
    private HOCRModel hocr;
    private HighlightModel hm;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            new TessAsync(){
                @Override
                protected void onPostExecute(ArrayList<int[]> s)
                {
                    super.onPostExecute(s);

                    if(s.isEmpty())
                        Toast.makeText(MainActivity.this, "No se encontró la palabra buscada...", Toast.LENGTH_SHORT).show();
                    else
                        prepareToDraw(s, false, null);
                }

            }.execute(data, editView.getText().toString(), datapath, mPreview.getPreviewSize());
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
                boolean existe = DocumentReader.docExists(barcode.displayValue.toString());
                if(existe)
                {
                    Toast.makeText(getApplicationContext(), "QR: " + "'" + barcode.displayValue + "'" + " existe", Toast.LENGTH_LONG).show();
                    //hacer sincronizar clickable
                    sincronizar.setEnabled(true);
                    activarComentarios.setEnabled(true);
                    hocr = DocumentReader.readHOCR(barcode.displayValue.toString());
                    //cargar comentarios del documento físico
                    comments = DocumentReader.readComments();
                    if(comments == null)
                    {
                        activarComentarios.setEnabled(false);
                        Toast.makeText(this, "No se pudieron cargar los comentrios", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //obtiene comentarios string de los comments
                        List<String> comentariosString = new ArrayList<>();
                        for (CommentModel com : comments) {
                            comentariosString.add(com.getComment());
                        }

                        //crear commentAdapter para la listView
                        CustomCommentAdapter adapter = new CustomCommentAdapter(getApplicationContext(), R.layout.comment_row, comentariosString);
                        listViewComments.setAdapter(adapter);

                        bboxexComment = new ArrayList<>();
                        //obtiene bboxes de todos los comentarios
                        for (int i = 0; i < comments.size(); i++) {
                            ArrayList<Integer> indexPalabrasHighlight = comments.get(i).getIndexWordsCommented();
                            for (int indexPalabra : indexPalabrasHighlight) {
                                int[] bboxes = new int[4];
                                bboxes[0] = hocr.bboxes.get(indexPalabra)[0];
                                bboxes[1] = hocr.bboxes.get(indexPalabra)[1];
                                bboxes[2] = hocr.bboxes.get(indexPalabra)[2];
                                bboxes[3] = hocr.bboxes.get(indexPalabra)[3];
                                bboxexComment.add(bboxes);
                            }
                        }

                        // TODO cambiarlo para que guarde todos los comentarios, pero solo dibuje los visibles
                        listViewComments.post(new Runnable() {
                            @Override
                            public void run() {
                                listViewComments.setOnScrollListener(new AbsListView.OnScrollListener()
                                {
                                    @Override
                                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                                    }

                                    @Override
                                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
                                    {
                                        linesFinishStart = new ArrayList<>();
                                        ArrayList<int[]> bboxex = new ArrayList<>();
                                        int first = 0;
                                        int last = visibleItemCount - 1;
                                        View firstChild = view.getChildAt(0);
                                        Log.i("ChildView", "First child view top bottom " + firstChild.getTop() + " " + firstChild.getBottom());

                                        if(firstChild.getTop() < 0)
                                        {
                                            Log.i("ChildView", "firstVisibleitem no se dibuja" + firstVisibleItem);
                                            first++;
                                        }

                                        View lastChild = view.getChildAt(visibleItemCount - 1);
                                        if(lastChild != null)
                                        {
                                            Log.i("ChildView", "Last child view top bottom " + lastChild.getTop() + " " + lastChild.getBottom());
                                            if(lastChild.getBottom() > view.getBottom())
                                                last--;
                                        }

                                        for(int i = first; i <= last; i++)
                                        {
                                            int[] loc = new int[2];
                                            view.getChildAt(i).getLocationOnScreen(loc);
                                            Log.i("ListView:", "Posicion en pantalla X-Y" + loc[0] + " " + loc[1]);
                                            bboxex.add(bboxexComment.get(i));
                                            //obtiene las coordenadas en x,y de cada comentario mostrado en pantalla (solo visibles)
                                            //eso se debe hacer dentro de scroll listener
                                            linesFinishStart.add(loc);
                                        }

                                        Log.i("ListView:", "firstVisibleItem-visibleItemCount: " + firstVisibleItem + "-" + visibleItemCount
                                                + "view top bottom " + view.getTop() + " " + view.getBottom());
                                        //mandar a commentdrawer a dibujar comentarios
                                        prepareToDraw(bboxex, false, linesFinishStart);
                                    }
                                });

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
                //crea la actividad para escanear código QR
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;

            //sincronizar words subrayadas de documento fisico con digital y viceversa
            case R.id.action_sincronize:
                //obtener words en highlight de documento digital
                hm = DocumentReader.readHighlights(hocr.numLines);
                mPreview.setHocr(hocr);
                mPreview.setSincronizar(true);
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
                        relativeLayout.bringToFront();
                        commentDrawer.bringToFront();
                    }
                    else
                    {
                        listViewComments.setVisibility(View.INVISIBLE);
                        activarComentarios.setTitle("Activar Comentarios");
                        commentDrawer.clear();
                    }
                }
                break;

            case R.id.action_clear:
                rect.clear();
                if(listViewComments != null)
                {
                    if(listViewComments.getVisibility() == View.VISIBLE)
                    {
                        listViewComments.setVisibility(View.INVISIBLE);
                        activarComentarios.setTitle("Activar Comentarios");
                        commentDrawer.clear();
                    }
                }
                relativeLayout.bringToFront();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            mCamera = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e)
        {
            Log.i("ERROR", "No se pudo abrir la cámara!");
        }

        datapath = getFilesDir() + "/tesseract/";
        TesseractFileReader tess = new TesseractFileReader(this, datapath);
        tess.checkFile(new File(datapath + "tessdata/"));
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

        //para dibujar los rectangulos en las words
        rect = (RectangleDrawer) findViewById(R.id.rdRect);
        commentDrawer = (CommentDrawer) findViewById(R.id.cdComment);
        relativeLayout.bringToFront();

        searchButton = (Button) findViewById(R.id.btnSearch);
        searchButton.bringToFront();
        captureButton = (Button) findViewById(R.id.btnWrite);
        captureButton.bringToFront();
        editView = (EditText) findViewById(R.id.etWrite);
        editView.bringToFront();

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

    public void prepareToDraw(ArrayList<int[]> bboxesToDraw , boolean sinc, ArrayList<int[]> linesFinishStart)
    {
        //sacar razón entre previewSize/camera.width
        //y previewSize/camera.height
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

        //hacer que boundingboxes escalen al tamaño de pantalla
        for (int[] bb: bboxesToDraw)
        {
            //left y right
            bb[0] = (int) (bb[0] / reasonWidth);
            bb[2] = (int) (bb[2] / reasonWidth);

            //top y bottom
            bb[1] = (int) (bb[1] / reasonHeight);
            bb[3] = (int) (bb[3] / reasonHeight);
            Log.i("CAMERATEST: prepareToDraw", "left top right bottom: " + bb[0] + " " + bb[1] + " "+ bb[2] + " " + bb[3]);
        }

        if(linesFinishStart == null)
            rect.setParameters(bboxesToDraw, sinc);
        else
            commentDrawer.setParameters(bboxesToDraw, linesFinishStart);
        rect.bringToFront();
    }

    public void sincronizarHilight()
    {
        if(hm == null)
        {
            Toast.makeText(this, "No hay palabras subrayadas...", Toast.LENGTH_SHORT).show();
            return;
        }

        //para cada linea de higlightModel, buscar esa palabra en hocrModel
        //una vez que se encuentra, obtener su índice, y buscar ese indice en bboxes
        //ese bboxes mandarlo a prepareToDraw
        ///////para cada linea de higlight
        ArrayList<int[]> bboxesToDraw = new ArrayList<>();

        //linea
        int i = 0;

        LinkedList bboxesLineCopy = (LinkedList) hocr.bboxesLine.clone();
        //TODO top y bottom deben ser de la linea (hocr.lineTopBottomPixels)
        for (int[] indexArray: hm.getWordsAbsoluteIndex())
        {
            //para cada palabra
            for (int offsetPalabra: indexArray)
            {
                int[] bboxex = new int[4];
                bboxex[0] = hocr.bboxes.get(offsetPalabra)[0];
                bboxex[1] = hocr.bboxes.get(offsetPalabra)[1];
                bboxex[2] = hocr.bboxes.get(offsetPalabra)[2];
                bboxex[3] = hocr.bboxes.get(offsetPalabra)[3];
                bboxesToDraw.add(bboxex);

              /*  //buscar bbox correspondiente
                int[] bboxex = new int[4];
                ArrayList<int[]> bboxLine = (ArrayList<int[]>) bboxesLineCopy.get(i);

                bboxex[0] = bboxLine.get(offsetPalabra)[0];
                bboxex[1] = hocr.lineTopBottomPixels.get(i)[0];
                bboxex[2] = bboxLine.get(offsetPalabra)[2];
                bboxex[3] = hocr.lineTopBottomPixels.get(i)[1];
                bboxesToDraw.add(bboxex);*/
            }
            i++;
        }
        prepareToDraw(bboxesToDraw, true, null);
    }
}