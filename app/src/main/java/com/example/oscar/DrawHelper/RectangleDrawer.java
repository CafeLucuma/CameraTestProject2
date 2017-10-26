package com.example.oscar.DrawHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by oscar on 11-08-17.
 */

//se encarga de dibujar en pantalla las words resaltadas

public class RectangleDrawer extends View {

    ArrayList<int[]> bboxes;
    ArrayList<int[]> linesStartFinish;
    int color;
    private Paint paint;
    private Paint linePaint;
    private Rect rect;
    private boolean paramsSeted = false;
    private boolean clearSeted = false;
    private boolean sincronizar = false;
    private boolean comentar = false;


    public RectangleDrawer(Context context) {
        super(context);
        init();
    }

    public RectangleDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RectangleDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        color = Color.RED;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#aaB22222"));
        rect = new Rect();
    }

    //sibuja en pantalla lo que se encuentra en bboxes
    //utilizado para resaltar words en visón de documento físico
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (sincronizar)
            paint.setColor(Color.parseColor("#aaffff00"));
        else
            if(comentar)
            {
                paint.setColor(Color.parseColor("#9900CED1"));
                linePaint.setColor(Color.parseColor("#9900CED1"));
                linePaint.setStrokeWidth(5);
            }
            else
                paint.setColor(Color.parseColor("#aaB22222"));

        if (clearSeted)
        {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            clearSeted = false;
            return;
        }

        if (!this.paramsSeted)
            return;

        canvas.getClipBounds(rect);

        int i = 0;
        for (int[] bb : this.bboxes)
        {
            rect.left = bb[0];
            rect.right = bb[2];
            rect.top = bb[1];
            rect.bottom = bb[3];
            canvas.drawRect(rect, paint);
            if(comentar)
            {
                canvas.drawLine(bb[2], bb[1], linesStartFinish.get(i)[0], linesStartFinish.get(i)[1], linePaint);
                Log.i("RECTANGLEDRAWER", "coordenadas de lineas: rect " + bb[2] + " " + bb[1] + " lines: "
                        + linesStartFinish.get(i)[0] + " " + linesStartFinish.get(i)[1] );
            }
            i++;
        }

        comentar = false;
        paramsSeted = false;
    }

    public void clear()
    {
        clearSeted = true;
        invalidate();
    }

    //setParameters para subrayado y highlight
    public void setParameters(ArrayList<int[]> bboxes, boolean sinc)
    {
        this.sincronizar = sinc;
        this.bboxes = bboxes;
        this.paramsSeted = true;
        invalidate();
    }

    //setParameters para comentario
    public void setParameters(ArrayList<int[]> bboxes, ArrayList<int[]> linesStartFinish)
    {
        this.bboxes = bboxes;
        this.linesStartFinish = linesStartFinish;
        this.comentar = true;
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
