package com.waterbear.loglibrary.plot;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.query.scalarfunction.DateDiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Translate data to the plot domain, e.g.
 */
public abstract class AxisAdapter<E extends Value> {

    protected int plotLimitLow;
    protected int plotLimitHigh;
    private boolean inverted;
    private boolean boundsSet = false;
    private List<AxisChangedListener> listeners = new ArrayList<>();

    public interface AxisChangedListener {
        void onAxisChanged();
    }

    public static class Builder {
        private boolean isRange = false;
        private boolean limitsSet = false;
        private Value lower;
        private Value upper;

        public Builder setDomainLimits(Domain d) {
            isRange = false;
            lower = d.getLowerLimit();
            upper = d.getUpperLimit();
            limitsSet = true;
            return this;
        }

        public Builder setRangeLimits(Domain d) {
            isRange = true;
            Value[] range = d.getRange();
            lower = range[0];
            upper = range[1];
            limitsSet = true;
            return this;

        }

        private void checkLimitsSetOrThrow() {
            if (!limitsSet) throw new IllegalStateException("set limits first");
        }

        public AxisAdapter<NumberValue> buildNumberValueAxis() {
            checkLimitsSetOrThrow();
            if (lower.isNull() || upper.isNull()) return new ExplodingAxisAdapter<NumberValue>();
            AxisAdapter<NumberValue> adapter = new NumberAxisAdapter(lower, upper);
            adapter.setInverted(isRange);
            return adapter;
        }

        public AxisAdapter<DateValue> buildDateValueAxis() {
            checkLimitsSetOrThrow();
            if (lower.isNull() || upper.isNull()) return new ExplodingAxisAdapter<DateValue>();
            AxisAdapter<DateValue> adapter = new DateAxisAdapter(lower, upper);
            adapter.setInverted(isRange);
            return adapter;
        }
    }


    public void registerAxisChangedListener(AxisChangedListener listener) {
        listeners.add(listener);
    }

    public void unregisterAxisChangedListener(AxisChangedListener listener) {
        listeners.remove(listener);
    }

    private void setInverted(boolean inverted) {
        if (this.inverted != inverted) {
            if (boundsSet) {
                throw new IllegalStateException("inversion can't be set after bounds set.");
            }
        }
        this.inverted = inverted;
    }

    public void setBounds(int lowerBound, int upperBound) {
        plotLimitLow = inverted ? upperBound : lowerBound;
        plotLimitHigh = inverted ? lowerBound : upperBound;
        boundsSet = true;
        for (AxisChangedListener l:listeners) {
            l.onAxisChanged();
        }
    }

    protected int getScaledValue(double value, double valLow, double valHigh) {
        if (valLow > valHigh) {
            String message = String.format("invalid source range [%s, %s]",
                    valLow, valHigh);
            throw new IllegalArgumentException(message);
        }
        double factor = valLow != valHigh ? (value - valLow) / (valHigh - valLow) : 0.5f;
        int position = (int) (Math.round(factor * (plotLimitHigh - plotLimitLow)));
        return plotLimitLow + position;

    }

    @SuppressWarnings("unchecked")
    public float getPlotPosition(Value value) {
        if (!boundsSet) {
            throw new IllegalStateException("Bounds must be set before calling this method.");
        }

        return getPositionForValue((E)value);
    }

    protected abstract float getPositionForValue(E value);

    /**
     * Plotter that returns plot positions based on zero-based 'position' values (a.k.a. discrete
     * domain points). For example to obtain the plot position for the first discrete data point,
     * call {@link #getPositionForValue(com.google.visualization.datasource.datatable.value.Value)} with a
     * {@link com.google.visualization.datasource.datatable.value.NumberValue} of 0.
     */
    public static class DiscreteAxisAdapter extends AxisAdapter<NumberValue> {

        private final double srcLow;
        private final double srcHigh;

        public DiscreteAxisAdapter(int discreteCount) {
            srcLow = 0.5;
            srcHigh = discreteCount + 0.5;
        }

        @Override
        protected float getPositionForValue(NumberValue value) {
            double plotValue = value.getValue() + 1;
            return (float) this.getScaledValue(plotValue, srcLow, srcHigh);
        }
    }

    public static class DateAxisAdapter extends AxisAdapter<DateValue> {
        private final DateValue baseDate;
        private final int dateSpan;

        protected DateAxisAdapter(Value start, Value end) {

            NumberValue diff = (NumberValue) DateDiff.getInstance().evaluate(
                    Lists.newArrayList(start, end));

            this.dateSpan = (int) diff.getValue();
            if (this.dateSpan < 0) {
                throw new IllegalArgumentException("Invalid range " + start + " - " + end);
            }
            baseDate = (DateValue)start;

        }

        @Override
        protected float getPositionForValue(DateValue value) {
            NumberValue valDiff = (NumberValue) DateDiff.getInstance().evaluate(
                    Lists.newArrayList((Value)baseDate, value));
            return (float) getScaledValue(valDiff.getValue(), -0.5, dateSpan + 0.5);
        }
    }

    public static class NumberAxisAdapter extends AxisAdapter<NumberValue> {
        private final double srcLow;
        private final double srcHigh;

        protected NumberAxisAdapter(Value srcLow, Value srcHigh) {
            this.srcLow = ((NumberValue) srcLow).getValue();
            this.srcHigh = ((NumberValue) srcHigh).getValue();
            if (this.srcLow > this.srcHigh) {
                throw new IllegalArgumentException(String.format("Invalid Source Range: %s-%s",
                        srcLow, srcHigh));
            }
        }

        @Override
        protected float getPositionForValue(NumberValue value) {
            return (float) getScaledValue(value.getValue(), srcLow, srcHigh);
        }
    }

    /**
     * Lazy-error scale object that will blow up if caller attempts to scale any objects with it.
     */
    public static class ExplodingAxisAdapter<T extends Value> extends AxisAdapter {

        @Override
        protected float getPositionForValue(Value value) {
            throw new UnsupportedOperationException("This object was not expected to be " +
                    "used due to lack of min or max values during instantiation");
        }
    }
}
