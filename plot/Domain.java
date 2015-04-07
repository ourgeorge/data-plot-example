package com.waterbear.loglibrary.plot;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.query.QueryGroup;
import com.google.visualization.datasource.query.QuerySelection;
import com.google.visualization.datasource.query.ScalarFunctionColumn;
import com.google.visualization.datasource.query.SimpleColumn;
import com.google.visualization.datasource.query.engine.QueryEngine;
import com.google.visualization.datasource.query.scalarfunction.NumberMinMax;
import com.waterbear.loglibrary.content.LogColumns;
import com.waterbear.loglibrary.provider.TagInfoContract;

import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 *
 * Created by rich on 3/26/15.
 */
public abstract class Domain<D extends Value, R extends Value> {

    D low;
    D high;
    R[] range;

    DataTable data;
    private boolean loaded = false;

    public Domain(D low, D high) {
        this.low = low;
        this.high = high;
    }

    public Domain load() {
        try {
            data = loadData();
            range = getRange(data);
            loaded = true;
        } catch (TypeMismatchException | InvalidQueryException e) {
            throw new IllegalStateException(e.getLocalizedMessage());
        }

        return this;
    }

    public DataTable getTable() {
        if (!loaded) throw new IllegalStateException("load table first!");
        return data;
    }

    protected abstract R[] getRange(DataTable table);

    public abstract TableSeriesData getSeries();

    /**
     * @return domain lower limit
     */
    public D getLowerLimit() {
        return low;
    }

    /**
     * @return domain upper limit
     */
    public D getUpperLimit() {
        return high;
    }

    public R[] getRange() {
        return range;
    }

    protected abstract DataTable loadData() throws TypeMismatchException, InvalidQueryException;

    protected NumberValue[] getMinMaxValues(DataTable table, int column) {
        return getMinMaxNumbers(table, new int[]{column});
    }

    /**
     * Get min and max {@link com.google.visualization.datasource.datatable.value.NumberValue}
     * from select columns of table in the format of an array.
     *
     * @param table
     * @param columns indices of columns from which to gather min and max values
     * @return min is array[0], max is array[1]. Result may include
     * {@link com.google.visualization.datasource.datatable.value.NumberValue#NULL_VALUE}
     * if table doesn't contain a min or max in the columns requested.
     */
    protected NumberValue[] getMinMaxNumbers(DataTable table, int[] columns) {
        if (columns.length < 1) {
            return new NumberValue[]{NumberValue.getNullValue(), NumberValue.getNullValue()};
        }

        Query query = new Query();
        QuerySelection selection = new QuerySelection();

        List<AbstractColumn> minColumns = Lists.newArrayList();
        List<AbstractColumn> maxColumns = Lists.newArrayList();
        for (int idx : columns) {
            String colId = table.getColumnDescription(idx).getId();
            minColumns.add(new AggregationColumn(new SimpleColumn(colId), AggregationType.MIN));
            maxColumns.add(new AggregationColumn(new SimpleColumn(colId), AggregationType.MAX));
        }

        AbstractColumn min = new ScalarFunctionColumn(minColumns,
                NumberMinMax.getInstance(NumberMinMax.FindType.MIN));
        AbstractColumn max = new ScalarFunctionColumn(maxColumns,
                NumberMinMax.getInstance(NumberMinMax.FindType.MAX));

        selection.addColumn(min);
        selection.addColumn(max);
        query.setSelection(selection);

        DataTable limits = QueryEngine.executeQuery(query, table, Locale.getDefault());
        if (limits.getNumberOfRows() < 1) {
            return new NumberValue[]{NumberValue.getNullValue(), NumberValue.getNullValue()};
        } else {
            TableRow row = limits.getRow(0);
            return new NumberValue[]{(NumberValue) row.getCell(0).getValue(),
                    (NumberValue) row.getCell(1).getValue()};
        }
    }


}


