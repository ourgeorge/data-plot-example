package com.waterbear.loglibrary.plot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.waterbear.loglibrary.R;

import java.util.List;

public class ChartView extends ImageView {

    private final List<BoundsChangedListener> boundsChangedListeners = Lists.newArrayList();
    private Renderer mRenderer;

    public ChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ChartView);
        DisplayOptions options = new DisplayOptions.Builder().setAttributes(attributes).build();
        mRenderer = new Renderer(options);

    }

    public void setTable(Domain data) {
        boundsChangedListeners.clear();
        Plotter.DayPlotter plotter = getDayPlotter(data);
        mRenderer.setPlotter(plotter);
        boundsChangedListeners.add(plotter);
        invalidate();
    }

    private Plotter.DayPlotter getDayPlotter(Domain data) {
        int vh = getHeight() - getPaddingTop() - getPaddingBottom();
        int vw = getWidth() - getPaddingLeft() - getPaddingRight();

        AxisAdapter<NumberValue> rangeAxis = new AxisAdapter.Builder()
                .setRangeLimits(data)
                .buildNumberValueAxis();
        AxisAdapter<DateValue> domainAxis = new AxisAdapter.Builder()
                .setDomainLimits(data)
                .buildDateValueAxis();
        rangeAxis.setBounds(0, vh);
        domainAxis.setBounds(0, vw);

        return new Plotter.DayPlotter(domainAxis, rangeAxis, data);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int vh = getHeight() - getPaddingTop() - getPaddingBottom();
        int vw = getWidth() - getPaddingLeft() - getPaddingRight();
        for (BoundsChangedListener b : boundsChangedListeners) {
            b.onBoundsChanged(vw, vh);
        }

        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getPaddingLeft(), getPaddingTop());
        mRenderer.renderOntoCanvas(canvas);
    }

    public interface BoundsChangedListener {
        public void onBoundsChanged(int w, int h);
    }


}
