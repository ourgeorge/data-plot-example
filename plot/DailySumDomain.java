package com.waterbear.loglibrary.plot;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.query.QueryGroup;
import com.google.visualization.datasource.query.QuerySelection;
import com.google.visualization.datasource.query.SimpleColumn;
import com.google.visualization.datasource.query.engine.QueryEngine;
import com.waterbear.loglibrary.content.LogColumns;
import com.waterbear.loglibrary.provider.TagInfoContract;

import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Domain object that represents daily sums.
 *
 * Created by Ehmer, R.G. on 4/7/15.
 */
public class DailySumDomain extends Domain<DateValue, NumberValue> {
    private static final int SUM_COLUMN = 1;
    private final Context context;
    private final int buttonId;

    public DailySumDomain(DateValue low, DateValue high, Context context, int buttonId) {
        super(low, high);
        this.context = context;
        this.buttonId = buttonId;
    }

    @Override
    protected NumberValue[] getRange(DataTable table) {
        return getMinMaxValues(table, SUM_COLUMN);
    }

    @Override
    public TableSeriesData getSeries() {
        Map<Value, Value[]> series = new LinkedHashMap<>();
        int dIdx = data.getColumnIndex("date");
        int nIdx = data.getColumnIndex("sum-quantity");
        List<TableRow> rows = data.getRows();
        for (TableRow row : rows) {
            final DateValue date = (DateValue) row.getCell(dIdx).getValue();
            final NumberValue[] sum = {(NumberValue) row.getCell(nIdx).getValue()};
            series.put(date, sum);
        }

        return new TableSeriesData(series);

    }

    protected DataTable loadData() throws TypeMismatchException, InvalidQueryException {
        long sevenDaysAgo = low.getObjectToFormat().getTimeInMillis();

        String sel = LogColumns.MILLISECONDS + " > ?";
        String[] selectionArgs = new String[]{Long.toString(sevenDaysAgo)};
        String sort = LogColumns.MILLISECONDS + " ASC";
        String[] projection = TagInfoContract.LogInfo.getLogChartProjection();

        Uri logUri = TagInfoContract.LogInfo.buildLogDirectoryUri(buttonId, true);
        Cursor c = context.getContentResolver()
                .query(logUri, projection, sel, selectionArgs, sort);

        DataTable table = new DataTable();
        table.addColumn(new ColumnDescription("quantity", ValueType.NUMBER, "Quantity"));
        table.addColumn(new ColumnDescription("date", ValueType.DATE, "Date"));
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));

        int qIdx = c.getColumnIndexOrThrow(LogColumns.NUMBER);
        int tIdx = c.getColumnIndexOrThrow(LogColumns.MILLISECONDS);
        int oIdx = c.getColumnIndexOrThrow(LogColumns.TIMEZONE_OFFSET);
        while (c.moveToNext()) {
            Value quan = c.isNull(qIdx) ? NumberValue.getNullValue() : new NumberValue(c.getDouble(qIdx));
            long gmtTime = c.getLong(tIdx) + c.getLong(oIdx);
            calendar.setTimeInMillis(gmtTime);
            Value date = new DateValue(calendar);
            TableRow row = new TableRow();
            row.addCell(quan);
            row.addCell(date);
            table.addRow(row);
        }
        c.close();

        Query query = new Query();
        QuerySelection selection = new QuerySelection();
        selection.addColumn(new SimpleColumn("date"));
        selection.addColumn(new AggregationColumn(new SimpleColumn("quantity"), AggregationType.SUM));
        query.setSelection(selection);

        QueryGroup group = new QueryGroup();
        group.addColumn(new SimpleColumn("date"));
        query.setGroup(group);
        query.validate();

        return QueryEngine.executeQuery(query, table, Locale.getDefault());

    }
}
