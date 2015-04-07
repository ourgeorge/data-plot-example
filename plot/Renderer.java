package com.waterbear.loglibrary.plot;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

/**
 * Controls how chart data is rendered onto bitmaps.
 *
 *
 * <p/>
 * Created by Ehmer, R.G. on 3/28/15.
 */
public class Renderer {

    private Plotter plotter;
    private final List<GraphTemplate> plotGraphList = new ArrayList<>();

    public Renderer(DisplayOptions options) {
        PointTemplate p = new PointTemplate(options);
        plotGraphList.add(p);

    }

    public void setPlotter(Plotter p) {
        if (plotter!=null) {
            plotter.removeGraphTemplates(plotGraphList);
        }

        plotter = p;
        for (GraphTemplate g: plotGraphList) {
            plotter.addGraphTemplate(g);
        }

    }


    public void renderOntoCanvas(Canvas c) {
        if (plotter == null) return;
            
        for (GraphTemplate g : plotGraphList) {
            Plottable[] plottables = g.getPlottables();
            for (Plottable p: plottables) {
                p.plot(c);
            }
        }
    }
}
