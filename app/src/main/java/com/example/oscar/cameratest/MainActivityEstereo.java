package com.example.oscar.cameratest;

import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.example.oscar.CameraHelper.CameraPreview2_0;
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

public class MainActivityEstereo extends AppCompatActivity
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
    private Button btnMenu;
    private Button btnMenu2;
    private boolean sincronizar;
    private boolean activarComentarios;
    private EditText etQuery;
    private EditText etQuery2;
    private FrameLayout imagePreview;
    private FrameLayout imagePreview2;
    private ImageView imageView;
    private ImageView imageView2;
    private RelativeLayout relativeLayout;
    private RelativeLayout relativeLayout2;
    private LinearLayout ll1;
    private LinearLayout ll2;
    private ListView listViewComments;
    private ListView listViewComments2;
    private RectangleDrawer rect;
    private RectangleDrawer rect2;
    private TextView toastText;
    private TextView toastText2;
    private CommentDrawer commentDrawer;
    private CommentDrawer commentDrawer2;
    private FrameDrawer frameDrawer;
    private FrameDrawer frameDrawer2;
    private Camera mCamera = null;
    private CameraPreview2_0 mPreview2_0;
    private MediaRecorder mMediaRecorder;
    private boolean isTakingPicture = false;
    private HOCRModel hocr;
    private HighlightModel hm;
    private boolean draw = false;
    private int widthScreen;
    private int heightScreen;
    public static boolean camera1Selected;
    public static boolean appStarted = false;
    private LayoutInflater layoutInflater;
    private View layout;
    private Toast t;

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
                        {
                            toastText.setText("Palabra no encontrada...");
                            toastText2.setText("Palabra no encontrada...");
                            t.setGravity(Gravity.CENTER, 0, 0);
                            t.setDuration(Toast.LENGTH_SHORT);
                            t.setView(layout);
                            t.show();
                        }

                        else
                            prepareToDraw(s, false, null, false);
                    }
                }.execute(data, searchedWord, datapath, mPreview2_0.getPreviewSize(), 2);

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
                boolean existe = DocumentReader.docExists(barcode.displayValue.toString(), 2);
                if(existe)
                {
                    filename = barcode.displayValue.toString();
                    toastText.setText("Doc: " + "'" + barcode.displayValue.toString() + "'" + " existe");
                    toastText2.setText("Doc: " + barcode.displayValue.toString() + " existe");
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.setDuration(Toast.LENGTH_SHORT);
                    t.setView(layout);
                    t.show();
                    //hacer sincronizar clickable
                    sincronizar = (true);
                    activarComentarios = (true);
                    hocr = DocumentReader.readHOCR(filename, 2);
                    escalarHOCR();

                        //dibujar para segunda camara
                        frameDrawer.post(new Runnable() {
                            @Override
                            public void run() {
                                dibujarFrame2(true);
                                frameDrawer.bringToFront();
                            }
                        });
                        frameDrawer2.post(new Runnable() {
                            @Override
                            public void run() {
                                dibujarFrame2(false);
                                frameDrawer2.bringToFront();
                            }
                        });


                    //cargar comentarios del documento físico
                    comments = DocumentReader.readComments(filename);
                    if(comments == null)
                    {
                        activarComentarios = false;
                        toastText.setText("Error al cargar comentarios");
                        toastText2.setText("Error al cargar comentarios");
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.setDuration(Toast.LENGTH_SHORT);
                        t.setView(layout);
                        t.show();
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
                        listViewComments2.setAdapter(adapter);

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

                                        int[] locView = new int[2];
                                        view.getLocationOnScreen(locView);

                                        for(int i = first; i <= last; i++)
                                        {
                                            int[] loc = new int[2];
                                            view.getChildAt(i).getLocationOnScreen(loc);
                                            //en caso de que la pantalla no parta desde el top, hay que restar la diferencia del espacio vacio
                                            if(locView[1] > 0)
                                            {
                                                loc[1] = loc[1] - locView[1];
                                            }
                                            Log.i("ListView:", "Posicion en pantalla X-Y" + loc[0] + " " + loc[1]);
                                            bboxexCommentToDraw.add(bboxexComment.get(i));
                                            //obtiene las coordenadas en x,y de cada comentario mostrado en pantalla (solo visibles)
                                            //eso se debe hacer dentro de scroll listener
                                            linesFinishStart.add(loc);
                                        }


                                        Log.i("ListView:", "firstVisibleItem-visibleItemCount: " + firstVisibleItem + "-" + visibleItemCount
                                                + "view top bottom " + view.getTop() + " " + view.getBottom());
                                        Log.i("ListView:", "view location on screen " + locView[0] + "-" + locView[1]);
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
                {
                    toastText.setText("QR: " + barcode.displayValue + "no existe");
                    toastText2.setText("QR: " + barcode.displayValue + "no existe");
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.setDuration(Toast.LENGTH_SHORT);
                    t.setView(layout);
                    t.show();
                }
            }
        }
        relativeLayout.bringToFront();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2_imageview);

        Log.i("CAMERATEST", "ONCREATE");
        //abrir cámara
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.i("ERROR", "No se pudo abrir la cámara!");
        }
        //camara doble seleccionada
        Log.i("CAMERATEST", "camera2 selected");
        //setear xml para acreen dos imageview
        camera1Selected = false;
        //this.camera = new Camera1(getContext());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //largo y ancho de pantalla
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        widthScreen = size.x;
        heightScreen = size.y;
        // Create our Preview view and set it as the content of our activity.
        btnMenu = (Button) findViewById(R.id.btnMenu);
        btnMenu2 = (Button) findViewById(R.id.btnMenu2);
        imageView = (ImageView) findViewById(R.id.ivPreview);
        imageView2 = (ImageView) findViewById(R.id.ivPreview2);
        imagePreview = (FrameLayout) findViewById(R.id.camera_preview);
        imagePreview2 = (FrameLayout) findViewById(R.id.camera_preview2);
        mPreview2_0 = new CameraPreview2_0(MainActivityEstereo.this, mCamera, imageView, imageView2, widthScreen / 2, heightScreen / 2);
        //mPreview = new CameraPreview(this, mCamera);
        //Camera.Size previewSize = mPreview2_0.getPreviewSize();
        //preview = (FrameLayout) findViewById(R.id.camera_preview);
        relativeLayout = (RelativeLayout) findViewById(R.id.rlLayout);
        relativeLayout2 = (RelativeLayout) findViewById(R.id.rlLayout2);
        //linear layout de boton y edittext
        ll1 = (LinearLayout) findViewById(R.id.ll1);
        ll2 = (LinearLayout) findViewById(R.id.ll2);

        //custom toast
        layoutInflater = getLayoutInflater();
        layout = layoutInflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_container));
        t = new Toast(getApplicationContext());
        //custom toast text
        toastText = (TextView) layout.findViewById(R.id.tvToast);
        toastText2 = (TextView) layout.findViewById(R.id.tvToast2);

        //edit text para buscar palabra
        etQuery = (EditText) findViewById(R.id.etQuery);
        etQuery2 = (EditText) findViewById(R.id.etQuery2);
        TextView.OnEditorActionListener etListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    if(!isTakingPicture)
                    {
                        if(v.getText().toString().trim().matches(""))
                        {
                            toastText.setText("Debe introducir una palabra");
                            toastText2.setText("Debe introducir una palabra");
                            t.setGravity(Gravity.CENTER, 0, 0);
                            //toastText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            //toastText2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            //toastText.setGravity(Gravity.LEFT);
                            //toastText2.setGravity(Gravity.LEFT);
                            t.setDuration(Toast.LENGTH_SHORT);
                            t.setView(layout);
                            t.show();
                        }
                        else
                        {
                            searchedWord = v.getText().toString();
                            toastText.setText("Buscó la palabra: " + searchedWord);
                            toastText2.setText("Buscó la palabra: " + searchedWord);
                            t.setGravity(Gravity.CENTER, 0, 0);
                            t.setDuration(Toast.LENGTH_SHORT);
                            t.setView(layout);
                            t.show();
                            mCamera.takePicture(null, null, mPicture);
                            isTakingPicture = true;
                        }
                    }
                    else
                    {
                        Toast.makeText(MainActivityEstereo.this, "Procesando, espere un momento...", Toast.LENGTH_SHORT).show();
                        toastText.setText("Procesando, espere un momento...");
                        toastText2.setText("Procesando, espere un momento...");
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.setDuration(Toast.LENGTH_SHORT);
                        t.setView(layout);
                        t.show();
                    }
                    etQuery.setVisibility(View.INVISIBLE);
                    etQuery2.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        };
        etQuery.setOnEditorActionListener(etListener);
        //setear textwatcher para copiar texto en el otro edittext
        etQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etQuery2.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etQuery.setInputType(InputType.TYPE_NULL);

        //setear tamaño de views a mitad de pantalla
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthScreen / 2, heightScreen / 2);
        params.gravity = Gravity.CENTER_VERTICAL;
        imagePreview.setLayoutParams(params);
        imagePreview2.setLayoutParams(params);
        //preview.addView(mPreview2_0);
        listViewComments = (ListView) findViewById(R.id.lvComments);
        listViewComments2 = (ListView) findViewById(R.id.lvComments2);

        ll1.bringToFront();
        ll2.bringToFront();
        //para dibujar los rectangulos en las words
        rect = (RectangleDrawer) findViewById(R.id.rdRect);
        rect2 = (RectangleDrawer) findViewById(R.id.rdRect2);
        commentDrawer = (CommentDrawer) findViewById(R.id.cdComment);
        commentDrawer2 = (CommentDrawer) findViewById(R.id.cdComment2);
        frameDrawer = (FrameDrawer) findViewById(R.id.fdFrame);
        frameDrawer2 = (FrameDrawer) findViewById(R.id.fdFrame2);
        //Creating the instance of PopupMenu
        final PopupMenu popup = new PopupMenu(MainActivityEstereo.this, btnMenu, Gravity.CENTER);
        popup.getMenuInflater().inflate(R.menu.main_menu2, popup.getMenu());
        popup.setGravity(Gravity.CENTER);
        final PopupMenu popup2 = new PopupMenu(MainActivityEstereo.this, btnMenu2, Gravity.CENTER);
        popup2.getMenuInflater().inflate(R.menu.main_menu3, popup2.getMenu());
        popup2.setGravity(Gravity.CENTER);

     /*  btnMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("CameraEstereo", "buton blick");

                //Inflating the Popup using xml file

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_scan:
                                //crea la actividad para escanear código QR
                                Intent intent = new Intent(MainActivityEstereo.this, ScanActivity.class);
                                startActivityForResult(intent, REQUEST_CODE);
                                break;

                            //sincronizar words subrayadas de documento fisico con digital y viceversa
                            case R.id.action_sincronize:
                                //obtener words en highlight de documento digital
                                if(sincronizar)
                                {
                                    if (filename != null)
                                        hm = DocumentReader.readHighlights(hocr.numLines, filename);

                                    mPreview2_0.setHocr(hocr);
                                    mPreview2_0.setSincronizar(true);
                                    sincronizarHilight();
                                }

                                break;

                            case R.id.action_activarComentarios:
                                //hacer comentarios visibles
                                if (listViewComments != null && activarComentarios) {
                                    if (listViewComments.getVisibility() == View.INVISIBLE) {
                                        draw = true;
                                        listViewComments.setVisibility(View.VISIBLE);
                                        listViewComments2.setVisibility(View.VISIBLE);
                                        relativeLayout.bringToFront();
                                        relativeLayout2.bringToFront();
                                        commentDrawer.bringToFront();
                                        commentDrawer2.bringToFront();
                                        //dibujar en pantalla
                                        prepareToDraw(bboxexCommentToDraw, false, linesFinishStart, true);
                                    } else {
                                        listViewComments.setVisibility(View.INVISIBLE);
                                        listViewComments2.setVisibility(View.INVISIBLE);
                                        draw = false;
                                        commentDrawer.clear();
                                        commentDrawer2.clear();
                                    }
                                }
                                else
                                    Toast.makeText(MainActivityEstereo.this, "Debe escanear un documento primero", Toast.LENGTH_SHORT).show();
                                break;

                            case R.id.action_clear:
                                rect.clear();
                                rect2.clear();
                                if (listViewComments != null) {
                                    if (listViewComments.getVisibility() == View.VISIBLE) {
                                        listViewComments.setVisibility(View.INVISIBLE);
                                        listViewComments2.setVisibility(View.INVISIBLE);
                                        commentDrawer.clear();
                                        commentDrawer2.clear();
                                    }
                                }
                                imagePreview.bringToFront();
                                imagePreview2.bringToFront();
                                imageView.bringToFront();
                                imageView2.bringToFront();
                                break;

                            case R.id.action_search:
                                etQuery.requestFocus();
                                break;
                        }
                        popup.dismiss();
                        popup2.dismiss();
                        return true;
                    }
                });

                popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        popup2.dismiss();
                    }
                });

                popup.show();//showing popup menu
            }
        });
*/
        btnMenu2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("CameraEstereo", "click button 2");
                Log.i("CameraEstereo", "buton blick");
                //Creating the instance of PopupMenu
                //Inflating the Popup using xml file

                Log.i("CameraEstereo", "buton blick");

                //Inflating the Popup using xml file

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_scan:
                                //crea la actividad para escanear código QR
                                Intent intent = new Intent(MainActivityEstereo.this, ScanActivity2.class);
                                startActivityForResult(intent, REQUEST_CODE);
                                break;

                            //sincronizar words subrayadas de documento fisico con digital y viceversa
                            case R.id.action_sincronize:
                                //obtener words en highlight de documento digital
                                if(sincronizar)
                                {
                                    if (filename != null)
                                        hm = DocumentReader.readHighlights(hocr.numLines, filename);

                                    mPreview2_0.setHocr(hocr);
                                    mPreview2_0.setSincronizar(true);
                                    sincronizarHilight();
                                }

                                break;

                            case R.id.action_activarComentarios:
                                //hacer comentarios visibles
                                if (listViewComments != null && activarComentarios) {
                                    if (listViewComments.getVisibility() == View.INVISIBLE) {
                                        draw = true;
                                        listViewComments.setVisibility(View.VISIBLE);
                                        listViewComments2.setVisibility(View.VISIBLE);
                                        relativeLayout.bringToFront();
                                        relativeLayout2.bringToFront();
                                        commentDrawer.bringToFront();
                                        commentDrawer2.bringToFront();
                                        //dibujar en pantalla
                                        prepareToDraw(bboxexCommentToDraw, false, linesFinishStart, true);
                                    } else {
                                        listViewComments.setVisibility(View.INVISIBLE);
                                        listViewComments2.setVisibility(View.INVISIBLE);
                                        draw = false;
                                        commentDrawer.clear();
                                        commentDrawer2.clear();
                                    }
                                }
                                else
                                    Toast.makeText(MainActivityEstereo.this, "Debe escanear un documento primero", Toast.LENGTH_SHORT).show();
                                break;

                            case R.id.action_clear:
                                rect.clear();
                                rect2.clear();
                                if (listViewComments != null) {
                                    if (listViewComments.getVisibility() == View.VISIBLE) {
                                        listViewComments.setVisibility(View.INVISIBLE);
                                        listViewComments2.setVisibility(View.INVISIBLE);
                                        commentDrawer.clear();
                                        commentDrawer2.clear();
                                    }
                                }
                                imagePreview.bringToFront();
                                imagePreview2.bringToFront();
                                imageView.bringToFront();
                                imageView2.bringToFront();
                                break;

                            case R.id.action_search:
                                etQuery.setVisibility(View.VISIBLE);
                                etQuery2.setVisibility(View.VISIBLE);
                                etQuery.bringToFront();
                                etQuery2.bringToFront();
                                etQuery.requestFocus();
                                break;
                        }
                        popup.dismiss();
                        popup2.dismiss();
                        return true;
                    }
                });

                popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        popup2.dismiss();
                    }
                });

                //registering popup with OnMenuItemClickListener
                popup2.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_scan:
                                //crea la actividad para escanear código QR
                                Intent intent = new Intent(MainActivityEstereo.this, ScanActivity2.class);
                                startActivityForResult(intent, REQUEST_CODE);
                                break;

                            //sincronizar words subrayadas de documento fisico con digital y viceversa
                            case R.id.action_sincronize:
                                if(sincronizar)
                                {
                                    //obtener words en highlight de documento digital
                                    if (filename != null)
                                        hm = DocumentReader.readHighlights(hocr.numLines, filename);

                                    mPreview2_0.setHocr(hocr);
                                    mPreview2_0.setSincronizar(true);
                                    sincronizarHilight();
                                }
                                else
                                    Toast.makeText(MainActivityEstereo.this, "Debe escanear un documento primero", Toast.LENGTH_SHORT).show();

                                break;

                            case R.id.action_activarComentarios:
                                //hacer comentarios visibles
                                if (listViewComments != null && activarComentarios) {
                                    if (listViewComments.getVisibility() == View.INVISIBLE) {
                                        draw = true;
                                        listViewComments.setVisibility(View.VISIBLE);
                                        listViewComments2.setVisibility(View.VISIBLE);
                                        relativeLayout.bringToFront();
                                        relativeLayout2.bringToFront();
                                        commentDrawer.bringToFront();
                                        commentDrawer2.bringToFront();
                                        //dibujar en pantalla
                                        prepareToDraw(bboxexCommentToDraw, false, linesFinishStart, true);
                                    } else {
                                        listViewComments.setVisibility(View.INVISIBLE);
                                        listViewComments2.setVisibility(View.INVISIBLE);
                                        draw = false;
                                        commentDrawer.clear();
                                        commentDrawer2.clear();
                                    }
                                }
                                else
                                {
                                    Toast.makeText(MainActivityEstereo.this, "Debe escanear un documento primero", Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case R.id.action_clear:
                                rect.clear();
                                rect2.bringToFront();
                                if (listViewComments != null) {
                                    if (listViewComments.getVisibility() == View.VISIBLE) {
                                        listViewComments.setVisibility(View.INVISIBLE);
                                        listViewComments2.setVisibility(View.INVISIBLE);
                                        commentDrawer.clear();
                                        commentDrawer2.clear();
                                    }
                                }
                                imagePreview.bringToFront();
                                imagePreview2.bringToFront();
                                imageView.bringToFront();
                                imageView2.bringToFront();
                                break;

                            case R.id.action_search:
                                etQuery.setVisibility(View.VISIBLE);
                                etQuery2.setVisibility(View.VISIBLE);
                                etQuery.bringToFront();
                                etQuery2.bringToFront();
                                etQuery.requestFocus();
                                break;

                        }
                        popup.dismiss();
                        popup2.dismiss();
                        return true;
                    }
                });

                popup2.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu)
                    {
                        popup.dismiss();
                    }
                });

                popup.show();//showing popup menu
                popup2.show();//showing popup menu
            }
        });

        //screen click listener
        imagePreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("CameraEstereo", "touch pantalla");
                //btnMenu.performClick();
                btnMenu2.performClick();
                return false;
            }
        });
        imagePreview2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("CameraEstereo", "touch pantalla2");
                //btnMenu.performClick();
                btnMenu2.performClick();
                return false;
            }
        });

        appStarted = true;
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
            mPreview2_0.getHolder().removeCallback(mPreview2_0);
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
            mPreview2_0 = new CameraPreview2_0(MainActivityEstereo.this, mCamera, imageView, imageView2, widthScreen / 2, heightScreen / 2);
            //preview.addView(mPreview2_0);
            Log.i("CAMERATEST: ONPAUSE", "PASA nuevo new camera2_0");
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

        layoutWidth = imagePreview.getWidth();
        layoutHeight = imagePreview.getHeight();
        //tamaño del preview que capta la camara
        Camera.Size previewSize;
            //ver si es para sincronizar o buscar palabra
            if(sinc == false && comment == false)
                previewSize = mPreview2_0.getPictureSize();
            else
                previewSize = mPreview2_0.getPreviewSize();

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
            //si la camara es doble, se dibuja en el segundo view
                rect2.setParameters(bboxesToDrawAux, sinc);
                rect2.bringToFront();

        }
        else
        {
            commentDrawer.setParameters(bboxesToDrawAux, linesFinishStart);
            commentDrawer.bringToFront();
            //si la camara es doble, se dibuja en el segundo view

                commentDrawer2.setParameters(bboxesToDrawAux, linesFinishStart);
                commentDrawer2.bringToFront();
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

    private void escalarHOCR()
    {
        //metodo para escalar el hocr tomado con resolucion de pic a resolucion de preview

        //ancho y largo de picture
        Camera.Size pictureSize = mPreview2_0.getPictureSize();
        int pictureWidth = pictureSize.width;
        int pictureHeight = pictureSize.height;
        //ancho y largo del preview de la camara
        Camera.Size previewSize = mPreview2_0.getPreviewSize();
        int previewWidth = previewSize.width;
        int previewHeight = previewSize.height;

        //razón del ancho
        double reasonWidth = (double) 1600 / previewWidth;
        //double reasonWidth = (double) pictureWidth / previewWidth;
        //razón del largo
        double reasonHeight = (double) 1200 / previewHeight;
        //double reasonHeight = (double) pictureHeight / previewHeight;

        //hacer que boundingboxes escalen al tamaño de preview
        for (int[] bb: hocr.bboxes)
        {
            //left y right
            bb[0] = (int) (bb[0] / reasonWidth);
            bb[2] = (int) (bb[2] / reasonWidth);

            //top y bottom
            bb[1] = (int) (bb[1] / reasonHeight);
            bb[3] = (int) (bb[3] / reasonHeight);
            Log.i("CAMERATEST: escalarHOCR: bb[]", "left top right bottom: " + bb[0] + " " + bb[1] + " "+ bb[2] + " " + bb[3]);
        }

        //escalar bloque bounding box a preview
        hocr.blockBoundingBox[0] = (int) (hocr.blockBoundingBox[0] / reasonWidth);
        hocr.blockBoundingBox[2] = (int) (hocr.blockBoundingBox[2] / reasonWidth);
        hocr.blockBoundingBox[1] = (int) (hocr.blockBoundingBox[1] / reasonHeight);
        hocr.blockBoundingBox[3] = (int) (hocr.blockBoundingBox[3] / reasonHeight);

        //escalar line top bottom a preview
        for(int[] lineTopBot : hocr.lineTopBottomPixels)
        {
            lineTopBot[0] = (int) (lineTopBot[0] / reasonHeight);
            lineTopBot[1] = (int) (lineTopBot[1] / reasonWidth);
        }
    }

    private void dibujarFrame2(boolean cam1)
    {
        int layoutWidth = imagePreview.getWidth();
        int layoutHeight = imagePreview.getHeight();
        //tamaño del preview que capta la camara
        Camera.Size previewSize;
        previewSize = mPreview2_0.getPreviewSize();
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
        if(cam1)
        {
            frameDrawer.setParameters(lineLeft, lineTop, lineRight, lineBottom);
            frameDrawer.bringToFront();
        }
        else
        {
            frameDrawer2.setParameters(lineLeft, lineTop, lineRight, lineBottom);
            frameDrawer2.bringToFront();
        }
    }
}