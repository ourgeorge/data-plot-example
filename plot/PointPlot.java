package com.waterbear.loglibrary.plot;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.google.common.collect.Lists;

import java.util.List;

public class PointPlot implements Plottable {

    private final CoordinateSeries series;
    private final int pointRadius;
    private final Paint pointPaint;
    private final Paint pathPaint;
    private final PathGenerator pathGenerator;
    private final String lineType;
    private List<float[]> controlPoints;

    public PointPlot(CoordinateSeries series, int pointRadius, Paint pointPaint, Paint pathPaint, String curveType) {
        this.pointRadius = pointRadius;
        this.pointPaint = pointPaint;
        this.series = series;
        this.pathPaint = pathPaint;
        this.lineType = curveType;
        this.pathGenerator = createPathGenerator(series);

    }

    @Override
    public void plot(Canvas canvas) {
        Path p = pathGenerator.getPath();
        if (p != null) {
            canvas.drawPath(p, pathPaint);

            List<float[]> control = getControlPoints();
            for (float[] point : control) {
                //canvas.drawCircle(point[0], point[1], pointRadius / 2, pointPaint);
            }
        }

        for (float[] point : series.getCoordinates()) {
            canvas.drawCircle(point[0], point[1], pointRadius, pointPaint);
        }
    }

    public List<float[]> getControlPoints() {
        if (controlPoints == null) {
            float[][] controls = pathGenerator.getControlPoints();
            controlPoints = Lists.newArrayList();
            for (float[] pair : controls) {
                controlPoints.add(new float[]{pair[0], pair[1]});
                controlPoints.add(new float[]{pair[2], pair[3]});
            }
        }

        return controlPoints;
    }

    public PathGenerator createPathGenerator(CoordinateSeries data) {
        float[][] series = data.getCoordinates();
        switch (lineType) {
            case CurveType.NONE:
                return new PathGenerator.LinePath().generate(series);
            case CurveType.SMOOTHED_STEP:
                return new PathGenerator.SmoothedStepPath().generate(series);
            case CurveType.SMOOTHED_FUNCTION:
                return new PathGenerator.SmoothedFunctionPath(0.30).generate(series);
            default:
                return new PathGenerator.NoPath();
        }


    }

    public static interface CurveType {
        public String NONE = "none";
        public String SMOOTHED_FUNCTION = "smoothedFunction";
        public String SMOOTHED_STEP = "smoothedStep";
    }


}
