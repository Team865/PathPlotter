package ca.warp7.frc2020.lib.trajectory;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.geometry.Twist2d;
import edu.wpi.first.wpilibj.spline.PoseWithCurvature;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/*
 * MIT License
 *
 * Copyright (c) 2018 Team 254
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

public class QuinticHermiteSpline {
    public double x_0;
    public double x_1;
    public double dx_0;
    public double dx_1;
    public double ddx_0;
    public double ddx_1;

    public double y_0;
    public double y_1;
    public double dy_0;
    public double dy_1;
    public double ddy_0;
    public double ddy_1;

    public double x_a;
    public double x_b;
    public double x_c;
    public double x_d;
    public double x_e;
    public double x_f;

    public double y_a;
    public double y_b;
    public double y_c;
    public double y_d;
    public double y_e;
    public double y_f;

    public QuinticHermiteSpline(
            double x_0,
            double x_1,
            double dx_0,
            double dx_1,
            double ddx_0,
            double ddx_1,

            double y_0,
            double y_1,
            double dy_0,
            double dy_1,
            double ddy_0,
            double ddy_1
    ) {
        this.x_0   = x_0;
        this.x_1   = x_1;
        this.dx_0  = dx_0;
        this.dx_1  = dx_1;
        this.ddx_0 = ddx_0;
        this.ddx_1 = ddx_1;

        this.y_0   = y_0;
        this.y_1   = y_1;
        this.dy_0  = dy_0;
        this.dy_1  = dy_1;
        this.ddy_0 = ddy_0;
        this.ddy_1 = ddy_1;
        
        computeCoefficients();
    }
    
    public void computeCoefficients() {
        x_a =  -6 * x_0 - 3 * dx_0 - 0.5 * ddx_0 + 0.5 * ddx_1 - 3 * dx_1 +  6 * x_1;
        x_b =  15 * x_0 + 8 * dx_0 + 1.5 * ddx_0 -       ddx_1 + 7 * dx_1 - 15 * x_1;
        x_c = -10 * x_0 - 6 * dx_0 - 1.5 * ddx_0 + 0.5 * ddx_1 - 4 * dx_1 + 10 * x_1;
        x_d =                        0.5 * ddx_0;
        x_e =                 dx_0;
        x_f =       x_0;

        y_a =  -6 * y_0 - 3 * dy_0 - 0.5 * ddy_0 + 0.5 * ddy_1 - 3 * dy_1 +  6 * y_1;
        y_b =  15 * y_0 + 8 * dy_0 + 1.5 * ddy_0 -       ddy_1 + 7 * dy_1 - 15 * y_1;
        y_c = -10 * y_0 - 6 * dy_0 - 1.5 * ddy_0 + 0.5 * ddy_1 - 4 * dy_1 + 10 * y_1;
        y_d =                        0.5 * ddy_0;
        y_e =                 dy_0;
        y_f =       y_0;
    }

    public Point interpolate(double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double t4 = t3 * t;
        double t5 = t4 * t;
        return new Point(
                x_a * t5 + x_b * t4 + x_c * t3 + x_d * t2 + x_e * t + x_f,
                y_a * t5 + y_b * t4 + y_c * t3 + y_d * t2 + y_e * t + y_f,

                5 * x_a * t4 + 4 * x_b * t3 + 3 * x_c * t2 + 2 * x_d * t + x_e,
                5 * y_a * t4 + 4 * y_b * t3 + 3 * y_c * t2 + 2 * y_d * t + y_e,

                20 * x_a * t3 + 12 * x_b * t2 + 6 * x_c * t + 2 * x_d,
                20 * y_a * t3 + 12 * y_b * t2 + 6 * y_c * t + 2 * y_d,

                60 * x_a * t2 + 24 * x_b * t + 6 * x_c,
                60 * y_a * t2 + 24 * y_b * t + 6 * y_c
        );
    }

    public Point interpolateToSecondDerivative(double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double t4 = t3 * t;
        double t5 = t4 * t;
        return new Point(
                x_a * t5 + x_b * t4 + x_c * t3 + x_d * t2 + x_e * t + x_f,
                y_a * t5 + y_b * t4 + y_c * t3 + y_d * t2 + y_e * t + y_f,

                5 * x_a * t4 + 4 * x_b * t3 + 3 * x_c * t2 + 2 * x_d * t + x_e,
                5 * y_a * t4 + 4 * y_b * t3 + 3 * y_c * t2 + 2 * y_d * t + y_e,

                20 * x_a * t3 + 12 * x_b * t2 + 6 * x_c * t + 2 * x_d,
                20 * y_a * t3 + 12 * y_b * t2 + 6 * y_c * t + 2 * y_d,

                0,
                0
        );
    }

    public Point interpolateToFirstDerivative(double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double t4 = t3 * t;
        double t5 = t4 * t;
        return new Point(
                x_a * t5 + x_b * t4 + x_c * t3 + x_d * t2 + x_e * t + x_f,
                y_a * t5 + y_b * t4 + y_c * t3 + y_d * t2 + y_e * t + y_f,

                5 * x_a * t4 + 4 * x_b * t3 + 3 * x_c * t2 + 2 * x_d * t + x_e,
                5 * y_a * t4 + 4 * y_b * t3 + 3 * y_c * t2 + 2 * y_d * t + y_e,

                0,
                0,

                0,
                0
        );
    }

    public List<PoseWithCurvature> parameterize() {

        List<PoseWithCurvature> result = new ArrayList<>();
        Deque<Double> stack = new ArrayDeque<>();

        int iterations = 0;

        stack.addLast(1.0);
        stack.addLast(0.0);

        while (!stack.isEmpty()) {
            double t0 = stack.removeLast();
            double t1 = stack.removeLast();

            Pose2d t0_pose = interpolateToFirstDerivative(t0).getPose();
            Point t1_point = interpolateToSecondDerivative(t1);
            Pose2d t1_pose = t1_point.getPose();

            // Find the difference between the two poses
            Twist2d twist = t0_pose.log(t1_pose);

            if (twist.dx > 0.127 || twist.dy > 0.00127 || twist.dtheta > 0.0872) {
                double pivot = (t0 + t1) / 2.0;

                // Add the endpoints in reverse order
                stack.addLast(t1);
                stack.addLast(pivot);

                stack.addLast(pivot);
                stack.addLast(t0);
            } else {
                result.add(new PoseWithCurvature(t1_pose, t1_point.getCurvature()));
            }

            iterations++;
            if (iterations > 5000) {
                throw new StackOverflowError("Cannot parameterize spline");
            }
        }
        return result;
    }
    
    public double sum_dCurvature_squared() {
        double dt = 0.01;
        double sum = 0.0;

        for (double t = 0; t < 1.0; t+=dt) {
            sum += (dt * interpolate(t).get_dCurvature_squared());
        }

        return sum;
    }

    public Pose2d getStartPose() {
        return interpolateToFirstDerivative(0.0).getPose();
    }

    public Pose2d getEndPose() {
        return interpolateToFirstDerivative(1.0).getPose();
    }

    public static QuinticHermiteSpline fromPose(
            Pose2d start,
            Pose2d end,
            double startHeadingMagnitude,
            double endHeadingMagnitude
    ) {
        double distance = start.getTranslation().getDistance(end.getTranslation());
        return new QuinticHermiteSpline(
                start.getTranslation().getX(),
                end.getTranslation().getX(),
                start.getRotation().getCos() * distance * startHeadingMagnitude,
                end.getRotation().getCos() * distance * endHeadingMagnitude,
                0,
                0,
                start.getTranslation().getY(),
                end.getTranslation().getY(),
                start.getRotation().getSin() * distance * startHeadingMagnitude,
                end.getRotation().getSin() * distance * endHeadingMagnitude,
                0,
                0
        );
    }

    public static QuinticHermiteSpline fromPose(
            Pose2d start,
            Pose2d end,
            double headingMagnitude
    ) {
        return fromPose(start, end, headingMagnitude, headingMagnitude);
    }

    public static QuinticHermiteSpline fromPose(
            Pose2d start,
            Pose2d end) {
        return fromPose(start, end, 1.2);
    }

    public static List<PoseWithCurvature> parameterize(List<QuinticHermiteSpline> splines) {
        if (splines == null || splines.isEmpty()) {
            return List.of();
        }
        List<PoseWithCurvature> result = new ArrayList<>();

        Point firstPoint = splines.get(0).interpolateToSecondDerivative(0.0);
        result.add(new PoseWithCurvature(firstPoint.getPose(), firstPoint.getCurvature()));

        for (QuinticHermiteSpline spline : splines) {
            result.addAll(spline.parameterize());
        }

        return result;
    }
    
    public static double sum_dCurvature_squared(List<QuinticHermiteSpline> splines) {
        double sum = 0.0;

        for (QuinticHermiteSpline spline : splines) {
            sum += spline.sum_dCurvature_squared();
        }

        return sum;
    }


    /**
     * Makes optimization code a little more readable
     */
    private static class ControlPoint {
        private double ddx, ddy;
    }

    /**
     * Finds the optimal second derivative values for a set of splines to reduce the sum of the change in curvature
     * squared over the path
     *
     * @param splines the list of splines to optimize
     * @return the final sumDCurvature2
     */
    public static double optimizeSpline(List<QuinticHermiteSpline> splines) {
        int count = 0;
        double prev = sum_dCurvature_squared(splines);
        while (count < 100) {
            runOptimizationIteration(splines);
            double current = sum_dCurvature_squared(splines);
            if (prev - current < 1E-4)
                return current;
            prev = current;
            count++;
        }
        return prev;
    }


    private static final double kStepSize = 1.0;
    private static final double kEpsilon = 1e-5;

    /**
     * Runs a single optimization iteration
     */
    private static void runOptimizationIteration(List<QuinticHermiteSpline> splines) {
        //can't optimize anything with less than 2 splines
        if (splines.size() <= 1) {
            return;
        }

        ControlPoint[] controlPoints = new ControlPoint[splines.size() - 1];
        double magnitude = 0;

        for (int i = 0; i < splines.size() - 1; ++i) {
            //don't try to optimize colinear points
            if (isColinear(splines.get(i).getStartPose(), splines.get(i + 1).getStartPose()) ||
                    isColinear(splines.get(i).getEndPose(), splines.get(i + 1).getEndPose())) {
                continue;
            }

            double original = sum_dCurvature_squared(splines);
            QuinticHermiteSpline temp, temp1;

            temp = splines.get(i);
            temp1 = splines.get(i + 1);
            controlPoints[i] = new ControlPoint(); //holds the gradient at a control point

            //calculate partial derivatives of sumDCurvature2
            splines.set(i, new QuinticHermiteSpline(
                    temp.x_0,
                    temp.x_1,
                    temp.dx_0,
                    temp.dx_1,
                    temp.ddx_0,
                    temp.ddx_1 + kEpsilon,
                    temp.y_0,
                    temp.y_1,
                    temp.dy_0,
                    temp.dy_1,
                    temp.ddy_0,
                    temp.ddy_1)
            );

            splines.set(i + 1, new QuinticHermiteSpline(temp1.x_0,
                    temp1.x_1,
                    temp1.dx_0,
                    temp1.dx_1,
                    temp1.ddx_0 + kEpsilon,
                    temp1.ddx_1,
                    temp1.y_0,
                    temp1.y_1,
                    temp1.dy_0,
                    temp1.dy_1,
                    temp1.ddy_0,
                    temp1.ddy_1)
            );

            controlPoints[i].ddx = (sum_dCurvature_squared(splines) - original) / kEpsilon;

            splines.set(i, new QuinticHermiteSpline(temp.x_0,
                    temp.x_1,
                    temp.dx_0,
                    temp.dx_1,
                    temp.ddx_0,
                    temp.ddx_1,
                    temp.y_0,
                    temp.y_1,
                    temp.dy_0,
                    temp.dy_1,
                    temp.ddy_0,
                    temp.ddy_1 + kEpsilon)
            );

            splines.set(i + 1, new QuinticHermiteSpline(temp1.x_0,
                    temp1.x_1,
                    temp1.dx_0,
                    temp1.dx_1,
                    temp1.ddx_0,
                    temp1.ddx_1,
                    temp1.y_0,
                    temp1.y_1,
                    temp1.dy_0,
                    temp1.dy_1,
                    temp1.ddy_0 + kEpsilon,
                    temp1.ddy_1)
            );

            controlPoints[i].ddy = (sum_dCurvature_squared(splines) - original) / kEpsilon;

            splines.set(i, temp);
            splines.set(i + 1, temp1);
            magnitude += controlPoints[i].ddx * controlPoints[i].ddx + controlPoints[i].ddy * controlPoints[i].ddy;
        }

        magnitude = Math.sqrt(magnitude);

        //minimize along the direction of the gradient
        //first calculate 3 points along the direction of the gradient
        Translation2d p1, p2, p3;
        p2 = new Translation2d(0, sum_dCurvature_squared(splines)); //middle point is at the current location

        for (int i = 0; i < splines.size() - 1; ++i) { //first point is offset from the middle location by -stepSize
            if (isColinear(splines.get(i).getStartPose(), splines.get(i + 1).getStartPose()) ||
                    isColinear(splines.get(i).getEndPose(), splines.get(i + 1).getEndPose())) {
                continue;
            }
            //normalize to step size
            controlPoints[i].ddx *= kStepSize / magnitude;
            controlPoints[i].ddy *= kStepSize / magnitude;

            //move opposite the gradient by step size amount
            splines.get(i).ddx_1 -= controlPoints[i].ddx;
            splines.get(i).ddy_1 -= controlPoints[i].ddy;

            splines.get(i + 1).ddx_0 -= controlPoints[i].ddx;
            splines.get(i + 1).ddy_0 -= controlPoints[i].ddy;

            //recompute the spline's coefficients to account for new second derivatives
            splines.get(i).computeCoefficients();
            splines.get(i + 1).computeCoefficients();
        }
        p1 = new Translation2d(-kStepSize, sum_dCurvature_squared(splines));

        for (int i = 0; i < splines.size() - 1; ++i) { //last point is offset from the middle location by +stepSize
            if (isColinear(splines.get(i).getStartPose(), splines.get(i + 1).getStartPose()) ||
                    isColinear(splines.get(i).getEndPose(), splines.get(i + 1).getEndPose())) {
                continue;
            }
            //move along the gradient by 2 times the step size amount (to return to original location and move by 1
            // step)
            splines.get(i).ddx_1 += 2 * controlPoints[i].ddx;
            splines.get(i).ddy_1 += 2 * controlPoints[i].ddy;

            splines.get(i + 1).ddx_0 += 2 * controlPoints[i].ddx;
            splines.get(i + 1).ddy_0 += 2 * controlPoints[i].ddy;

            //recompute the spline's coefficients to account for new second derivatives
            splines.get(i).computeCoefficients();
            splines.get(i + 1).computeCoefficients();
        }

        p3 = new Translation2d(kStepSize, sum_dCurvature_squared(splines));

        double stepSize = fitParabola(p1, p2, p3); //approximate step size to minimize sumDCurvature2 along the gradient

        for (int i = 0; i < splines.size() - 1; ++i) {
            if (isColinear(splines.get(i).getStartPose(), splines.get(i + 1).getStartPose()) ||
                    isColinear(splines.get(i).getEndPose(), splines.get(i + 1).getEndPose())) {
                continue;
            }
            //move by the step size calculated by the parabola fit (+1 to offset for the final transformation to find
            // p3)
            controlPoints[i].ddx *= 1 + stepSize / kStepSize;
            controlPoints[i].ddy *= 1 + stepSize / kStepSize;

            splines.get(i).ddx_1 += controlPoints[i].ddx;
            splines.get(i).ddy_1 += controlPoints[i].ddy;

            splines.get(i + 1).ddx_0 += controlPoints[i].ddx;
            splines.get(i + 1).ddy_0 += controlPoints[i].ddy;

            //recompute the spline's coefficients to account for new second derivatives
            splines.get(i).computeCoefficients();
            splines.get(i + 1).computeCoefficients();
        }
    }

    /**
     * fits a parabola to 3 points
     *
     * @return the x coordinate of the vertex of the parabola
     */
    private static double fitParabola(Translation2d p1, Translation2d p2, Translation2d p3) {
        double A = (p3.getX() * (p2.getY() - p1.getY()) + p2.getX() * (p1.getY() - p3.getY())
                + p1.getX() * (p3.getY() - p2.getY()));

        double B = (p3.getX() * p3.getX() * (p1.getY() - p2.getY()) + p2.getX() *
                p2.getX() * (p3.getY() - p1.getY()) + p1.getX() * p1.getX() *
                (p2.getY() - p3.getY()));

        return -B / (2 * A);
    }

    private static boolean isColinear(Pose2d a, Pose2d b) {
        if (!isParallel(a.getRotation(), b.getRotation())) {
            return false;
        }

        Twist2d twist = a.log(b);

        return Math.abs(twist.dy) < 1E-12 && Math.abs(twist.dtheta) < 1E-12;
    }

    private static boolean isParallel(Rotation2d a, Rotation2d b) {
        return Math.abs(a.getCos() * b.getSin() - a.getSin() * b.getCos()) < 1E-12;
    }

    public static class Point {
        public double x;
        public double y;
        public double dx;
        public double dy;
        public double dd_x;
        public double dd_y;
        public double ddd_x;
        public double ddd_y;

        public Point(
                double x,
                double y,
                double dx,
                double dy,
                double dd_x,
                double dd_y,
                double ddd_x,
                double ddd_y
        ) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.dd_x = dd_x;
            this.dd_y = dd_y;
            this.ddd_x = ddd_x;
            this.ddd_y = ddd_y;
        }

        public double getCurvature() {
            double d = dx * dx + dy * dy;
            return (dx * dd_y - dd_x * dy) / (d * Math.sqrt(d));
        }

        public double get_dCurvature() {
            double dx2dy2 = dx * dx + dy * dy;
            double num = (dx * ddd_y - ddd_x * dy) * dx2dy2 -
                    3.0 * (dx * dd_y - dd_x * dy) * (dx * dd_x + dy * dd_y);
            return num / (dx2dy2 * dx2dy2 * Math.sqrt(dx2dy2));
        }

        public double get_dCurvature_dS() {
            return get_dCurvature() / Math.hypot(dx, dy);
        }

        public double get_dCurvature_squared() {
            double dCurvature = get_dCurvature();
            return dCurvature * dCurvature;
        }

        public Translation2d getPosition() {
            return new Translation2d(x, y);
        }

        public Rotation2d getHeading() {
            double mag = Math.hypot(dx, dy);
            return new Rotation2d(dx / mag, dy / mag);
        }

        public Pose2d getPose() {
            return new Pose2d(getPosition(), getHeading());
        }
    }
}
