package com.waterbear.loglibrary.plot;

import com.google.visualization.datasource.datatable.value.Value;

import java.util.Map;


/**
 * Represents all the series from a domain.
 */
public class TableSeriesData {

    final private int count;
    final private Map<Value,Value[]> series;

    public TableSeriesData(Map<Value, Value[]> series) {
        int count = -1;
        for (Value[] item: series.values()) {
            if (count==-1){
                count = item.length;
            } else if (count!=item.length) {
                throw new IllegalArgumentException("all series elements must be the same size");
            }
        }

        this.series = series;
        this.count = count == -1 ? 0 : count;
    }

    public int getSeriesCount() {
        return count;
    }

    /**
     * This is basically meaningless data since we don't know if there is a corresponding value
     * for each argument.
     * @return
     */
    public Value[] getArguments() {
        return series.keySet().toArray(new Value[series.size()]);
    }

    public Value[][] getValues() {
        return series.values().toArray(new Value[series.size()][count]);
    }



}
