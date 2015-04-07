package com.waterbear.loglibrary.plot;

/**
 * Returns plottable points.
 *
 * Created by Ehmer, R.G. on 3/28/15.
 */
public class PointTemplate implements
        GraphTemplate,
        AxisAdapter.AxisChangedListener {

    private final DisplayOptions options;
    private volatile Plottable[] series = new Plottable[0];
    private TableSeriesData tableSeriesData;

    private AxisAdapter domainAxis;
    private AxisAdapter rangeAxis;

    public PointTemplate(DisplayOptions options) {
        this.options = options;
    }

    @Override
    public Plottable[] getPlottables() {
        if (tableSeriesData == null) throw new IllegalStateException("set series first");
        return series;
    }

    @Override
    public synchronized void setSeries(Domain table, AxisAdapter domain, AxisAdapter range) {
        if (this.tableSeriesData != null) {
            domainAxis.unregisterAxisChangedListener(this);
            rangeAxis.unregisterAxisChangedListener(this);
            //throw new UnsupportedOperationException("series and listeners set already");
        }

        tableSeriesData = table.getSeries();
        domainAxis = domain;
        rangeAxis = range;
        reset();
        domainAxis.registerAxisChangedListener(this);
        rangeAxis.registerAxisChangedListener(this);

    }

    private void reset() {
        CoordinateSeries[] coordinateSeriesList = CoordinateSeries.fromTableSeriesData(tableSeriesData, domainAxis, rangeAxis);
        series = getPlottables(coordinateSeriesList);
    }

    protected Plottable[] getPlottables(CoordinateSeries[] seriesList) {
        PointPlot[] points = new PointPlot[seriesList.length];
        for (int i = 0; i < seriesList.length; i++) {
            points[i] = new PointPlot(seriesList[i], options.getPointRadius(), options.getPointPaint(), options.getPathPaint(), PointPlot.CurveType.SMOOTHED_FUNCTION);
        }
        return points;
    }


    @Override
    public void onAxisChanged() {
        reset();
    }








}
