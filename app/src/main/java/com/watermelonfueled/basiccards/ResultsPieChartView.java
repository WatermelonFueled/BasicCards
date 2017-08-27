package com.watermelonfueled.basiccards;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by dapar on 2017-05-30.
 */

public class ResultsPieChartView extends View {
    private static String TAG = "ResultsPieChartView";

    private RectF bounds;
    private float correctPercent;

    public ResultsPieChartView(Context context) {
        super(context);
    }

    public ResultsPieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        createPieChart(canvas, correctPercent, 100 - correctPercent);
    }

    public void createPieChart(Canvas canvas, float... values) {
        Log.d(TAG, "Rect bounds: " + bounds.toShortString());

        float total = 0f;
        for (float value: values) {
            total += value;
            Log.d(TAG, "sum: " + total);
        }

        float startAngle = 0;
        for (int i = 0; i < values.length; i++) {
            float sweepAngle = values[i]/total*360;
            Log.d(TAG, "start angle: " + startAngle + " sweep angle: " + sweepAngle + " value: " + values[i]);

            canvas.drawArc(bounds, startAngle-90, sweepAngle, true, getNextPaint(i));

            startAngle += sweepAngle;
        }
    }

    public void setCorrectPercent(float percent) {
        correctPercent = percent;
        invalidate();
    }

    private Paint getNextPaint(int iteration) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        if (iteration % 2 == 0) {
            paint.setColor(getResources().getColor(R.color.pie_chart_correct));
        } else {
            paint.setColor(getResources().getColor(R.color.pie_chart_incorrect));
        }
        paint.setAlpha(200);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //width based
        /*        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        int minh = MeasureSpec.getSize(w) + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0);*/

        //height based
        int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        int minw = getPaddingLeft() + getPaddingRight() + MeasureSpec.getSize(h);
        int w = resolveSizeAndState(MeasureSpec.getSize(h), widthMeasureSpec, 1);

        setMeasuredDimension(w,h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float scale = 0.8f;
        float shorter = scale * (float) Math.min(w,h);
        float x = (w - shorter)/2;
        float y = (h - shorter)/2;

        bounds = new RectF(x,y,x+shorter,y+shorter);
    }


}
