package com.waterbear.loglibrary.plot;

import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;

import java.util.ArrayList;
import java.util.List;

/**
* Created by rich on 3/26/15.
*/
public abstract class Plotter<D extends Value, R extends Value> implements ChartView.BoundsChangedListener {
    private final AxisAdapter<D> domainAxis;
    private final AxisAdapter<R> rangeAxis;
    private final Domain<D,R> domain;
    private final List<GraphTemplate> graphs = new ArrayList<>();

    public Plotter(AxisAdapter<D> domainAxis, AxisAdapter<R> rangeAxis, Domain<D, R> domain) {
        this.domainAxis = domainAxis;
        this.rangeAxis = rangeAxis;
        this.domain = domain;
    }

    public void onBoundsChanged(int w, int h) {
        domainAxis.setBounds(0, w);
        rangeAxis.setBounds(0, h);
    }

    public float[] getCoordinates(D x, R y) {
        float xf = domainAxis.getPlotPosition(x);
        float yf = rangeAxis.getPlotPosition(y);
        return new float[]{xf, yf};
    }


    public void addGraphTemplate(GraphTemplate g) {
        g.setSeries(domain, domainAxis, rangeAxis);
        graphs.add(g);

    }

    public void removeGraphTemplates(List<GraphTemplate> g) {
        graphs.removeAll(g);
    }

    public static class DayPlotter extends Plotter<DateValue, NumberValue> {

        public DayPlotter(AxisAdapter<DateValue> domainAxis, AxisAdapter<NumberValue> rangeAxis, Domain<DateValue, NumberValue> domain) {
            super(domainAxis,rangeAxis, domain);
        }

    }
}


