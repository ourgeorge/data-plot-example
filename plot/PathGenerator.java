package com.waterbear.loglibrary.plot;

import android.graphics.Path;

/**
* Created by rich on 3/26/15.
*/
public abstract class PathGenerator {
    private float[][] controlPoints;
    private Path mPath;
    private boolean generated = false;

    /**
     * Generate a path from an array of [x, y] coordinates.
     *
     * @param points
     * @return
     */
    public PathGenerator generate(float[][] points) {
        generated = true;
        if (points.length<2) {
            return this;
        }
        controlPoints = createControlPoints(points);
        mPath = createPathFromPoints(points, controlPoints);
        return this;
    }

    /**
     * Generate an array of control point pairs. Each row of the returned array contains a pair
     * of points (x1,y1,x2,y2), that are coordinates of control points to be applied to the
     * generated path for each respective point.
     *
     * @param points
     * @return
     */
    protected abstract float[][] createControlPoints(float[][] points);

    /**
     * Generate path from array of x, y points and an array of control points x1, y1, x2, y2
     * that represent the control points to be applied to the respective points.
     *
     * @param points
     * @param controlPoints
     * @return
     */
    protected abstract Path createPathFromPoints(float[][] points, float[][] controlPoints);

    public Path getPath() {
        checkGenerated();
        return mPath;
    }

    /**
     * @return array of control point coordinate <i>pairs</i>, c1x, c1y, c2x, c2y
     */
    public float[][] getControlPoints() {
        checkGenerated();
        return controlPoints;
    }

    private void checkGenerated() {
        if (!generated) {
            throw new IllegalStateException("'generate' must be called before this method");
        }
    }

    public static class LinePath extends PathGenerator {

        @Override
        public Path createPathFromPoints(float[][] points, float[][] controlPoints) {
            Path path = new Path();
            for (float[] point : points) {
                if (path.isEmpty()) {
                    path.moveTo(point[0], point[1]);
                } else {
                    path.lineTo(point[0], point[1]);
                }
            }

            return path;
        }

        @Override
        public float[][] createControlPoints(float[][] points) {
            return new float[0][0];
        }

    }

    public static class NoPath extends PathGenerator {

        @Override
        protected float[][] createControlPoints(float[][] points) {
            return null;
        }

        @Override
        protected Path createPathFromPoints(float[][] points, float[][] controlPoints) {
            return null;
        }
    }

    public static class SmoothedStepPath extends PathGenerator {

        @Override
        public Path createPathFromPoints(float[][] points, float[][] controlPoints) {
            Path path = new Path();
            for (int i = 0; i < points.length; i++) {
                float[] point = points[i];
                if (path.isEmpty()) {
                    path.moveTo(point[0], point[1]);
                } else {
                    path.cubicTo(
                            controlPoints[i - 1][2],
                            controlPoints[i - 1][3],
                            controlPoints[i][0],
                            controlPoints[i][1],
                            point[0],
                            point[1]);

                }
            }

            return path;
        }

        @Override
        public float[][] createControlPoints(float[][] points) {
            float[][] control = new float[points.length][4];

            //cover edge cases where control points are same as actual points
            //these points aren't actually touched by this class, but here for
            //the sake of completeness
            control[0][0] = points[0][0];
            control[0][1] = points[0][1];
            control[points.length - 1][2] = points[points.length - 1][0];
            control[points.length - 1][3] = points[points.length - 1][1];

            for (int i = 0; i < points.length - 1; i++) {

                float[] p1 = points[i];
                float[] p2 = points[i + 1];


                int offset = (int) (p2[0] - p1[0]);
                int[] c1 = {(int) p1[0] + offset / 2, (int) p1[1]};
                int[] c2 = {(int) p2[0] - offset / 2, (int) p2[1]};

                control[i][2] = c1[0];
                control[i][3] = c1[1];

                control[i + 1][0] = c2[0];
                control[i + 1][1] = c2[1];

            }

            return control;
        }

    }

    public static class SmoothedFunctionPath extends PathGenerator {
        private final double scale;

        public SmoothedFunctionPath(double severity) {
            scale = severity;
        }

        @Override
        public Path createPathFromPoints(float[][] points, float[][] controlPoints) {
            Path path = new Path();

            for (int i = 0; i < points.length; i++) {
                float[] point = points[i];
                if (path.isEmpty()) {
                    path.moveTo(point[0], point[1]);
                } else {
                    path.cubicTo(
                            controlPoints[i - 1][2],
                            controlPoints[i - 1][3],
                            controlPoints[i][0],
                            controlPoints[i][1],
                            point[0],
                            point[1]);

                }
            }

            return path;
        }

        /**
         * Calculates bezier control points that should surround each given point.
         *
         * @param points
         * @return array of bezier control points where idx 0, 1, 2, and 3 correspond with
         * control point 1 (cp1) x, cp1 y, cp2 x, and cp2 y.
         */
        @Override
        public float[][] createControlPoints(float[][] points) {
            float[][] control = new float[points.length][4];

            //cover edge cases where control points are same as actual points
            float[] start = getHalfControlPoint(points[0], points[1]);
            control[0][0] = points[0][0];
            control[0][1] = points[0][1];
            control[0][2] = start[0];
            control[0][3] = start[1];

            int lastIdx = points.length - 1;
            float[] end = getHalfControlPoint(points[lastIdx], points[lastIdx - 1]);
            control[lastIdx][0] = end[0];
            control[lastIdx][1] = end[1];
            control[lastIdx][2] = points[lastIdx][0];
            control[lastIdx][3] = points[lastIdx][1];

            for (int i = 1; i < lastIdx; i++) {
                float[] p2 = points[i];
                float[] p1 = points[i - 1];
                float[] p3 = points[i + 1];

                // length of vectors from point 2 to neighbor points
                float[] v1 = new float[]{p1[0] - p2[0], p1[1] - p2[1]};
                float[] v2 = new float[]{p3[0] - p2[0], p3[1] - p2[1]};
                double m1 = Math.sqrt(Math.pow(v1[0], 2) + Math.pow(v1[1], 2));
                double m2 = Math.sqrt(Math.pow(v2[0], 2) + Math.pow(v2[1], 2));
                double ratio = m1 / (m1 + m2);

                float crossProduct = v1[0] * v2[1] - v2[0] * v1[1];

                // a scaled vector between tips of v2 and v2
                double[] r = new double[]{(v2[0] - v1[0]) * ratio, (v2[1] - v1[1]) * ratio};

                double theta;
                if ((m1 + m2) == 0) {
                    // all 3 points are the same
                    control[i][0] = points[i][0];
                    control[i][1] = points[i][1];
                    control[i][2] = points[i][0];
                    control[i][3] = points[i][1];
                } else {
                    if (crossProduct == 0) {
                        //vectors are parallel (3 points lie on a straight line)
                        if (r[0] != 0) {
                            theta = Math.atan(r[1] / r[0]);
                        } else {
                            // points lie on a vertical line
                            theta = r[1] > 0 ? Math.PI : -Math.PI;
                        }

                    } else {
                        double qx = v1[0] + r[0];
                        double qy = v1[1] + r[1];
                        // rotate vector 90 deg
                        double qtx = -qy;
                        double qty = qx;
                        // qtx==0 only occurs when qty==0, which would result in theta== NaN, but
                        // this possibility only occurs when points lie on the same line, (i.e.
                        // cross product == 0), a case covered above.
                        theta = Math.atan(qty / qtx);
                    }


                    double[] c1 = getControlPoints(theta, scale, p2, p1, p3);

                    control[i][0] = (float) c1[0];
                    control[i][1] = (float) c1[1];
                    control[i][2] = (float) c1[2];
                    control[i][3] = (float) c1[3];
                }


            }

            return control;
        }

        /**
         * Makes a control point for the termination point of a path. Without a control
         * control point on the termination point of a curve, the termination segment
         * curve is dominated by the second-to-last point's control points, leading to
         * a massive arch in some cases.
         *
         * @param begin start of control point vector
         * @param end   end of control point vector
         * @return
         */
        private float[] getHalfControlPoint(float[] begin, float[] end) {

            float vy = end[1] - begin[1];
            float vx = end[0] - begin[0];

            float[] result = new float[2];
            result[0] = begin[0] + vx * (float) scale / 2;
            result[1] = begin[1] + vy * (float) scale / 2;
            return result;


        }


        /**
         * Get coordinates tip of a vector representing component of the vector between
         * points o and point p that lies along an axis rotated by θ
         *
         * @param theta angle of axis offset from normal x axis
         * @param o     origin of vector, x = o[0], y= o[1]
         * @param p1    coordinate of tip of point 1
         * @param p3    coordinate of tip of point 3
         * @return
         */
        private double[] getControlPoints(double theta, double scale, float[] o, float[] p1, float[] p3) {
            double[] result = new double[4];
            //c1x =   S・(cos(θ)・vx + sin(θ)・vy )・cos(-θ)
            //c1y = - S・(cos(θ)・vx + sin(θ)・vy )・sin(-θ)
            //[where vx = px-ox and vy = py-oy]
            double cosTheta = Math.cos(theta);
            double sinTheta = Math.sin(theta);
            double cosThetaN = Math.cos(-theta);
            double sinThetaN = Math.sin(-theta);

            // control 1
            float vx = p1[0] - o[0];
            float vy = p1[1] - o[1];
            // magnitude of control point 1 on theta i axis
            double vxp1 = scale * cosTheta * vx;
            double commonTerm = vxp1 + scale * sinTheta * vy;
            result[0] = commonTerm * cosThetaN;
            result[1] = -commonTerm * sinThetaN;

            // control 2
            vx = p3[0] - o[0];
            vy = p3[1] - o[1];
            // magnitude of control point 2 on theta i axis
            double vxp2 = scale * cosTheta * vx;
            commonTerm = vxp2 + scale * sinTheta * vy;
            result[2] = commonTerm * cosThetaN;
            result[3] = -commonTerm * sinThetaN;

            /*
            //normalize control points to the same length
            double c1 = Math.sqrt(result[0]*result[0] + result[1]*result[1]);
            double c2 = Math.sqrt(result[2]*result[2] + result[3]*result[3]);
            double ratio = Math.abs(c1) / Math.abs(c2);
            if (ratio > 1) {
                result[0] = result[0] / ratio;
                result[1] = result[1] / ratio;
            } else {
                result[2] = result[2] * ratio;
                result[3] = result[3] * ratio;
            }
            */

            // translate vector back to o coordinate space
            result[0] = result[0] + o[0];
            result[1] = result[1] + o[1];
            result[2] = result[2] + o[0];
            result[3] = result[3] + o[1];
            return result;
        }

    }
}
