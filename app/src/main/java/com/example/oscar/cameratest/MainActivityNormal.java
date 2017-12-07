package com.example.oscar.cameratest;

import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.oscar.CameraHelper.CameraPreview;
import com.example.oscar.DocumentHelper.DocumentReader;
import com.example.oscar.DrawHelper.CommentDrawer;
import com.example.oscar.DrawHelper.FrameDrawer;
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
import java.util.List;


//TODO hacer botton para menu, telefonos ya no tienen menu button

public class MainActivityNormal extends AppCompatActivity
{
    //clases camera
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
    private ArrayList<int[]> bboxexCommentToDraw;
    private ArrayList<int[]> linesFinishStart;
    public static String datapath;
    public static String filename;
    private String searchedWord;
    private MenuItem sincronizar;
    private MenuItem activarComentarios;
    private SearchView searchView;
    private FrameLayout preview;
    private RelativeLayout relativeLayout;
    private ListView listViewComments;
    private RectangleDrawer rect;
    private CommentDrawer commentDrawer;
    private FrameDrawer frameDrawer;
    private Camera mCamera = null;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isTakingPicture;
    private HOCRModel hocr;
    private HighlightModel hm;
    private boolean draw = false;
    public static boolean camera1Selected;
    public static boolean appStarted = false;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {

                new TessAsync()
                {
                    @Override
                    protected void onPostExecute(ArrayList<int[]> s)
                    {
                        super.onPostExecute(s);

                        if(s.isEmpty())
                            Toast.makeText(MainActivityNormal.this, "No se encontró la palabra buscada...", Toast.LENGTH_SHORT).show();
                        else
                            prepareToDraw(s, false, null, false);
                    }
                }.execute(data, searchedWord, datapath, mPreview.getPreviewSize(), 1);


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
                boolean existe = DocumentReader.docExists(barcode.displayValue.toString(), 1);
                if(existe)
                {
                    filename = barcode.displayValue.toString();
                    Toast.makeText(getApplicationContext(), "QR: " + "'" + filename + "'" + " existe", Toast.LENGTH_LONG).show();
                    //hacer sincronizar clickable
                    sincronizar.setEnabled(true);
                    activarComentarios.setEnabled(true);
                    hocr = DocumentReader.readHOCR(filename, 1);


                    //dibujar frame de texto
                    frameDrawer.post(new Runnable() {
                            @Override
                            public void run() {
                                dibujarFrame();
                                frameDrawer.bringToFront();
                            }
                        });


                    //cargar comentarios del documento físico
                    comments = DocumentReader.readComments(filename);
                    if(comments == null)
                    {
                        activarComentarios.setEnabled(false);
                        Toast.makeText(this, "No se pudieron cargar los comentrios", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
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
                                Log.i("ChildView", "IndexPalabrasHighlight: " + indexPalabra);
                                int[] bboxes = new int[4];

                                bboxes[0] = hocr.bboxes.get(indexPalabra)[0];
                                bboxes[1] = hocr.bboxes.get(indexPalabra)[1];
                                bboxes[2] = hocr.bboxes.get(indexPalabra)[2];
                                bboxes[3] = hocr.bboxes.get(indexPalabra)[3];
                                Log.i("ChildView", "bbox de IndexPalabrasHighlight " + indexPalabra + " : " + bboxes[0] + " "
                                        + bboxes[1] + " " + bboxes[2] + " " + bboxes[3]);
                                bboxexComment.add(bboxes);
                            }
                        }
                        //TODO cuando las palabras comentadas son seguidas, dibujar un puro bbox
                        //TODO no contar espacios como palabras en highlight (texteditor)
                        listViewComments.post(new Runnable() {
                            @Override
                            public void run() {
                                listViewComments.setOnScrollListener(new AbsListView.OnScrollListener()
                                {
                                    @Override
                                    public void onScrollStateChanged(AbsListView view, int scrollState)
                                    {

                                    }

                                    @Override
                                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
                                    {
                                        linesFinishStart = new ArrayList<>();
                                        bboxexCommentToDraw = new ArrayList<>();
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
                                            bboxexCommentToDraw.add(bboxexComment.get(i));
                                            //obtiene las coordenadas en x,y de cada comentario mostrado en pantalla (solo visibles)
                                            //eso se debe hacer dentro de scroll listener
                                            linesFinishStart.add(loc);
                                        }

                                        Log.i("ListView:", "firstVisibleItem-visibleItemCount: " + firstVisibleItem + "-" + visibleItemCount
                                                + "view top bottom " + view.getTop() + " " + view.getBottom());
                                        //mandar a commentdrawer a dibujar comentarios
                                        if(draw)
                                            prepareToDraw(bboxexCommentToDraw, false, linesFinishStart, true);
                                    }
                                });

                            }
                        });
                    }
                    frameDrawer.bringToFront();
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
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                if(!isTakingPicture)
                {
                    if(query.trim().matches(""))
                        Toast.makeText(MainActivityNormal.this, "Debe introducir una palabra", Toast.LENGTH_SHORT).show();
                    else
                    {
                        searchedWord = query;
                        mCamera.takePicture(null, null, mPicture);
                        isTakingPicture = true;
                        Toast.makeText(MainActivityNormal.this, "Buscó la palabra: " + searchedWord, Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(MainActivityNormal.this, "Procesando, espere un momento...", Toast.LENGTH_SHORT).show();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_scan:
                //crea la actividad para escanear código QR
                Intent intent = new Intent(MainActivityNormal.this, ScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;

            //sincronizar words subrayadas de documento fisico con digital y viceversa
            case R.id.action_sincronize:
                //obtener words en highlight de documento digital
                if(filename != null)
                    hm = DocumentReader.readHighlights(hocr.numLines, filename);

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
                        draw = true;
                        listViewComments.setVisibility(View.VISIBLE);
                        relativeLayout.bringToFront();
                        commentDrawer.bringToFront();
                        activarComentarios.setTitle("Desactivar Comentarios");
                        //dibujar en pantalla
                        prepareToDraw(bboxexCommentToDraw, false, linesFinishStart, true);
                    }
                    else
                    {
                        listViewComments.setVisibility(View.INVISIBLE);
                        draw = false;
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
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i("CAMERATEST", "ONCREATE");
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.i("ERROR", "No se pudo abrir la cámara!");
        }
        //camara normal seleccionada
        Log.i("CAMERATEST", "camera1 selected");
        //setear xml
        setContentView(R.layout.activity_main);
        camera1Selected = true;
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(MainActivityNormal.this, mCamera);
        relativeLayout = (RelativeLayout) findViewById(R.id.rlLayout);
        preview.addView(mPreview);
        //ara setear los comentarios
        listViewComments = (ListView) findViewById(R.id.lvComments);

        //para dibujar los rectangulos en las words
        rect = (RectangleDrawer) findViewById(R.id.rdRect);
        commentDrawer = (CommentDrawer) findViewById(R.id.cdComment);
        frameDrawer = (FrameDrawer) findViewById(R.id.fdFrame);
        frameDrawer.bringToFront();
        relativeLayout.bringToFront();

        appStarted = true;

        //this.camera = new Camera1(getContext());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        datapath = getFilesDir() + "/tesseract/";
        TesseractFileReader tess = new TesseractFileReader(this, datapath);
        tess.checkFile(new File(datapath + "tessdata/"));
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
        if(!appStarted)
            return;
        if (mCamera == null)
        {
            Log.i("CAMERATEST: ONPAUSE", "PASA nuevo camera.open");
            mCamera = Camera.open();
            mPreview = new CameraPreview(this, mCamera);
            //mPreview = new CameraPreview(this, mCamera);
            //mPreview = new CameraPreview2(this, mCamera, preview, preview2);
            preview.addView(mPreview);
        }
    }

    public void prepareToDraw(ArrayList<int[]> bboxesToDraw, boolean sinc, ArrayList<int[]> linesFinishStart, boolean comment) {
        //sacar razón entre previewSize/camera.width
        //y previewSize/camera.height
        //y dividir boundingbox por esa razón

        //layout in the activity that the cameraView will placed in
        ArrayList<int[]> bboxesToDrawAux = new ArrayList<>();
        int layoutWidth;
        int layoutHeight;

        layoutWidth = preview.getWidth();
        layoutHeight = preview.getHeight();
        //tamaño del preview que capta la camara
        Camera.Size previewSize;
        previewSize = mPreview.getPreviewSize();
        int previewWidth = previewSize.width;
        int previewHeight = previewSize.height;

        //razón del ancho
        double reasonWidth = (double) previewWidth / layoutWidth;
        //razón del largo
        double reasonHeight = (double) previewHeight / layoutHeight;

        //hacer que boundingboxes escalen al tamaño de pantalla
        for (int[] bb: bboxesToDraw)
        {
            int[] bbAux = new int[4];

            //left y right
            bbAux[0] = (int) (bb[0] / reasonWidth);
            bbAux[2] = (int) (bb[2] / reasonWidth);

            //top y bottom
            bbAux[1] = (int) (bb[1] / reasonHeight);
            bbAux[3] = (int) (bb[3] / reasonHeight);
            bboxesToDrawAux.add(bbAux);
            Log.i("CAMERATEST: prepareToDraw", "left top right bottom: " + bb[0] + " " + bb[1] + " "+ bb[2] + " " + bb[3]);
        }

        if(!comment)
        {
            rect.setParameters(bboxesToDrawAux, sinc);
            rect.bringToFront();
        }
        else
        {
            commentDrawer.setParameters(bboxesToDrawAux, linesFinishStart);
            commentDrawer.bringToFront();
        }
    }

    public void sincronizarHilight()
    {
        if(hm == null)
        {
            Toast.makeText(this, "No hay palabras subrayadas en el documento digital...", Toast.LENGTH_SHORT).show();
            return;
        }

        //para cada linea de higlightModel, buscar esa palabra en hocrModel
        //una vez que se encuentra, obtener su índice, y buscar ese indice en bboxes
        //ese bboxes mandarlo a prepareToDraw
        ///////para cada linea de higlight
        ArrayList<int[]> bboxesToDraw = new ArrayList<>();

        //TODO top y bottom deben ser de la linea (hocr.lineTopBottomPixels)
        for (int[] indexArray: hm.getWordsAbsoluteIndex())
        {
            //para cada palabra
            for (int offsetPalabra: indexArray)
            {
                Log.i("CAMERATEST: sincronizarHighlight: ", "offset palabra: " + offsetPalabra);
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
        }
        prepareToDraw(bboxesToDraw, true, null, false);
        //prepareToDraw2(bboxesToDraw, true, null, false);
    }

    private void dibujarFrame()
    {
        int layoutWidth = preview.getWidth();
        int layoutHeight = preview.getHeight();
        //tamaño del preview que capta la camara
        Camera.Size previewSize;
        previewSize = mPreview.getPreviewSize();
        int previewWidth = previewSize.width;
        int previewHeight = previewSize.height;

        //razón del ancho
        double reasonWidth = (double) previewWidth / layoutWidth;
        //razón del largo
        double reasonHeight = (double) previewHeight / layoutHeight;

        ArrayList<Point> lineLeft = new ArrayList<>();
        ArrayList<Point> lineTop = new ArrayList<>();
        ArrayList<Point> lineRight = new ArrayList<>();
        ArrayList<Point> lineBottom = new ArrayList<>();

        int[] coorBloque = new int[4];
        //left
        coorBloque[0] = (int) (hocr.blockBoundingBox[0] / reasonWidth);
        //top
        coorBloque[1] = (int) (hocr.blockBoundingBox[1] / reasonHeight);
        //right
        coorBloque[2] = (int) (hocr.blockBoundingBox[2] / reasonWidth);
        //bottom
        coorBloque[3] = (int) (hocr.blockBoundingBox[3] / reasonHeight);

        //left,bottom
        Point leftBottom = new Point(coorBloque[0], coorBloque[3]);
        //left, top
        Point leftTop = new Point(coorBloque[0], coorBloque[1]);
        lineLeft.add(leftBottom);
        lineLeft.add(leftTop);

        //left, top
        //right, top
        Point rightTop = new Point(coorBloque[2], coorBloque[1]);
        lineTop.add(leftTop);
        lineTop.add(rightTop);

        //right, top
        //right, bottom
        Point rightBottom = new Point(coorBloque[2], coorBloque[3]);
        lineRight.add(rightTop);
        lineRight.add(rightBottom);

        //left, bottom
        //right, bottom
        lineBottom.add(leftBottom);
        lineBottom.add(rightBottom);

        frameDrawer.setParameters(lineLeft, lineTop, lineRight, lineBottom);
        frameDrawer.bringToFront();
    }

}