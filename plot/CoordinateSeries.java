package com.waterbear.loglibrary.plot;

import com.google.visualization.datasource.datatable.value.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * translate value data into coordinate data
 */
class CoordinateSeries {

    private final float[][] coordinates;

    private CoordinateSeries(List<float[]> coordinates) {
        this.coordinates = coordinates.toArray(new float[coordinates.size()][2]);
    }

    public static CoordinateSeries[] fromTableSeriesData(
            TableSeriesData data,
            AxisAdapter domain,
            AxisAdapter range) {


        final int seriesCount = data.getSeriesCount();
        List<CoordinateSeries> seriesList = new ArrayList<>(seriesCount);
        Value[] arguments = data.getArguments();
        Value[][] values = data.getValues();

        float[] xVals = new float[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            xVals[i] = domain.getPlotPosition(arguments[i]);
        }

        for (int i = 0; i < seriesCount; i++) {
            List<float[]> coordinates = new ArrayList<>();
            for (int j = 0; j < values.length; j++) {
                Value v = values[j][i];
                if (!v.isNull()) {
                    float y = range.getPlotPosition(v);
                    coordinates.add(new float[]{xVals[j], y});
                }
            }

            seriesList.add(new CoordinateSeries(coordinates));
        }

        return seriesList.toArray(new CoordinateSeries[seriesList.size()]);

    }

    public float[][] getCoordinates() {
        return coordinates;
    }

}
