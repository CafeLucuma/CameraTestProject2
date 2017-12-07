package com.example.oscar.DrawHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Oscar on 26-10-2017.
 */


//se encarga de dibujar en pantalla las lineas del frame
public class FrameDrawer extends View {
    private ArrayList<Point> lineLeft;
    private ArrayList<Point> lineTop;
    private ArrayList<Point> lineRight;
    private ArrayList<Point> lineBottom;
    int color;
    private Paint paint;
    private boolean paramsSeted = false;
    private boolean clearSeted = false;

    public FrameDrawer(Context context) {
        super(context);
        init();
    }

    public FrameDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrameDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        color = Color.BLACK;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
    }

    //sibuja en pantalla lo que se encuentra en bboxes
    //utilizado para resaltar words en visón de documento físico
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (clearSeted)
        {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            clearSeted = false;
            return;
        }

        if (!this.paramsSeted)
            return;

        canvas.drawLine(lineLeft.get(0).x, lineLeft.get(0).y, lineLeft.get(1).x, lineLeft.get(1).y, paint);
        Log.i("FrameDrawer", "left: " + lineLeft.get(0).x + " "
                + lineLeft.get(0).y + " " + lineLeft.get(1).x + " " + lineLeft.get(1).y);

        canvas.drawLine(lineTop.get(0).x, lineTop.get(0).y, lineTop.get(1).x, lineTop.get(1).y, paint);
        Log.i("FrameDrawer", "top: " + lineTop.get(0).x + " "
                + lineTop.get(0).y + " " + lineTop.get(1).x + " " + lineTop.get(1).y);

        canvas.drawLine(lineRight.get(0).x, lineRight.get(0).y, lineRight.get(1).x, lineRight.get(1).y, paint);
        Log.i("FrameDrawer", "right: " + lineRight.get(0).x + " "
                + lineRight.get(0).y + " " + lineRight.get(1).x + " " + lineRight.get(1).y);

        canvas.drawLine(lineBottom.get(0).x, lineBottom.get(0).y, lineBottom.get(1).x, lineBottom.get(1).y, paint);
        Log.i("FrameDrawer", "bottom: " + lineBottom.get(0).x + " " + lineBottom.get(0).y
                + " " + lineBottom.get(1).x + " " + lineBottom.get(1).y);

        //Log.i("RECTANGLEDRAWER", "coordenadas de lineas: rect " + bb[2] + " " + bb[1] + " lines: "
          //              + linesStartFinish.get(i)[0] + " " + linesStartFinish.get(i)[1] );
        paramsSeted = false;
    }

    public void clear()
    {
        clearSeted = true;
        invalidate();
    }

    //setParameters para comentario
    public void setParameters(ArrayList<Point> lineLeft, ArrayList<Point> lineTop, ArrayList<Point> lineRight, ArrayList<Point> lineBottom)
    {
        this.lineLeft = lineLeft;
        this.lineTop = lineTop;
        this.lineRight = lineRight;
        this.lineBottom = lineBottom;
        this.paramsSeted = true;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int givenHeight = MeasureSpec.getSize(heightMeasureSpec);
        int givenWidth = MeasureSpec.getSize(widthMeasureSpec);

        switch (heightMode)
        {
            case MeasureSpec.EXACTLY:
                setMeasuredDimension(givenWidth, givenHeight);
                return;

            case MeasureSpec.AT_MOST:
                int ourHeight = getKnownHeight();
                int height = Math.min(givenHeight, ourHeight);
                setMeasuredDimension(givenWidth, height);
                return;

            case MeasureSpec.UNSPECIFIED:
                setMeasuredDimension(givenWidth, getKnownHeight());
                return;
        }
    }

    private int getKnownHeight()
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getContext().getResources().getDisplayMetrics());
    }
}
