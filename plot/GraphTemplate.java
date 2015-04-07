package com.waterbear.loglibrary.plot;

/**
 * Translates data into a plottable objects.
 *
 * Created by Ehmer, R.G. on 3/28/15.
 */
public interface GraphTemplate {

    Plottable[] getPlottables();

    void setSeries(Domain table, AxisAdapter domainAxis, AxisAdapter rangeAxis);


}
