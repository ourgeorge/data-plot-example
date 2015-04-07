package com.waterbear.loglibrary.plot;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;

import com.waterbear.loglibrary.R;

/**
* Created by rich on 3/26/15.
*/
public class DisplayOptions {
    private int pointColor;
    private int lineColor;
    private int lineWidth;
    private int pointRadius;

    private Paint mPathPaint;
    private Paint mPointPaint;

    public static class Builder {

        TypedArray attributes;

        public Builder setAttributes(TypedArray attributes) {
            this.attributes = attributes;
            return this;
        }
        public DisplayOptions build() {
            if (attributes==null) throw new UnsupportedOperationException("must setAttributes");
            DisplayOptions options = new DisplayOptions();
            options.parseAttributes(attributes);
            return options;
        }

    }

    private DisplayOptions() {
    }

    private void parseAttributes(TypedArray attributes) {
        lineWidth = attributes.getDimensionPixelOffset(R.styleable.ChartView_line_width, 1);
        pointRadius = attributes.getDimensionPixelOffset(R.styleable.ChartView_point_radius, 1);
        pointColor = attributes.getColor(R.styleable.ChartView_point_color, Color.RED);
        lineColor = attributes.getColor(R.styleable.ChartView_line_color, Color.RED);

        mPathPaint = new Paint();
        mPathPaint.setColor(lineColor);
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeWidth(lineWidth);

        mPointPaint = new Paint();
        mPointPaint.setAntiAlias(true);
        mPointPaint.setColor(pointColor);

    }

    protected int getPointRadius() {
        return pointRadius;
    }

    protected Paint getPathPaint() {
        return mPathPaint;
    }

    protected Paint getPointPaint() {
        return mPointPaint;
    }
}
