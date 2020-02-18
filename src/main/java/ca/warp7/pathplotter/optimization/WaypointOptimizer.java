/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/


package ca.warp7.pathplotter.optimization;

import ca.warp7.frc2020.lib.trajectory.QuinticHermiteSpline;
import ca.warp7.pathplotter.state.ControlPoint;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;

import java.util.Arrays;

public class WaypointOptimizer {
    /**
     * Returns a set of cubic splines corresponding to the provided control vectors. The
     * user is free to set the direction of the start and end point. The
     * directions for the middle waypoints are determined automatically to ensure
     * continuous curvature throughout the path.
     *
     * @param start     The starting control vector.
     * @param waypoints The middle waypoints. This can be left blank if you only
     *                  wish to create a path with two waypoints.
     * @param end       The ending control vector.
     * @return A vector of cubic hermite splines that interpolate through the
     * provided waypoints and control vectors.
     */
    public static QuinticHermiteSpline[] getOptimizedSplines(
            ControlPoint start, Translation2d[] waypoints, ControlPoint end) {

        QuinticHermiteSpline[] splines = new QuinticHermiteSpline[waypoints.length + 1];

        double dist = start.pose.getTranslation().getDistance(end.pose.getTranslation());
        double ds_start = dist * start.magMultiplier;
        double ds_end = dist * end.magMultiplier;

        double[] xInitial = toControlVectorX(start.pose, ds_start);
        double[] yInitial = toControlVectorY(start.pose, ds_start);
        double[] xFinal = toControlVectorX(end.pose, ds_end);
        double[] yFinal = toControlVectorY(end.pose, ds_end);

        if (waypoints.length > 1) {
            Translation2d[] newWaypts = new Translation2d[waypoints.length + 2];

            // Create an array of all waypoints, including the start and end.
            newWaypts[0] = new Translation2d(xInitial[0], yInitial[0]);
            System.arraycopy(waypoints, 0, newWaypts, 1, waypoints.length);
            newWaypts[newWaypts.length - 1] = new Translation2d(xFinal[0], yFinal[0]);

            // Populate tridiagonal system for clamped cubic
      /* See:
      https://www.uio.no/studier/emner/matnat/ifi/nedlagte-emner/INF-MAT4350/h08/undervisningsmateriale/chap7alecture.pdf
      */
            // Above-diagonal of tridiagonal matrix, zero-padded
            final double[] a = new double[newWaypts.length - 2];

            // Diagonal of tridiagonal matrix
            final double[] b = new double[newWaypts.length - 2];
            Arrays.fill(b, 4.0);

            // Below-diagonal of tridiagonal matrix, zero-padded
            final double[] c = new double[newWaypts.length - 2];

            // rhs vectors
            final double[] dx = new double[newWaypts.length - 2];
            final double[] dy = new double[newWaypts.length - 2];

            // solution vectors
            final double[] fx = new double[newWaypts.length - 2];
            final double[] fy = new double[newWaypts.length - 2];

            // populate above-diagonal and below-diagonal vectors
            a[0] = 0.0;
            for (int i = 0; i < newWaypts.length - 3; i++) {
                a[i + 1] = 1;
                c[i] = 1;
            }
            c[c.length - 1] = 0.0;

            // populate rhs vectors
            dx[0] = 3 * (newWaypts[2].getX() - newWaypts[0].getX()) - xInitial[1];
            dy[0] = 3 * (newWaypts[2].getY() - newWaypts[0].getY()) - yInitial[1];

            if (newWaypts.length > 4) {
                for (int i = 1; i <= newWaypts.length - 4; i++) {
                    dx[i] = 3 * (newWaypts[i + 1].getX() - newWaypts[i - 1].getX());
                    dy[i] = 3 * (newWaypts[i + 1].getY() - newWaypts[i - 1].getY());
                }
            }

            dx[dx.length - 1] = 3 * (newWaypts[newWaypts.length - 1].getX()
                    - newWaypts[newWaypts.length - 3].getX()) - xFinal[1];
            dy[dy.length - 1] = 3 * (newWaypts[newWaypts.length - 1].getY()
                    - newWaypts[newWaypts.length - 3].getY()) - yFinal[1];

            // Compute solution to tridiagonal system
            thomasAlgorithm(a, b, c, dx, fx);
            thomasAlgorithm(a, b, c, dy, fy);

            double[] newFx = new double[fx.length + 2];
            double[] newFy = new double[fy.length + 2];

            newFx[0] = xInitial[1];
            newFy[0] = yInitial[1];
            System.arraycopy(fx, 0, newFx, 1, fx.length);
            System.arraycopy(fy, 0, newFy, 1, fy.length);
            newFx[newFx.length - 1] = xFinal[1];
            newFy[newFy.length - 1] = yFinal[1];

            for (int i = 0; i < newFx.length - 1; i++) {
                splines[i] = new QuinticHermiteSpline(
                        newWaypts[i].getX(), newWaypts[i + 1].getX(),
                        newFx[i], newFx[i + 1],
                        0.0, 0.0,
                        newWaypts[i].getY(), newWaypts[i + 1].getY(),
                        newFy[i], newFy[i + 1],
                        0.0, 0.0
                );
            }
        } else if (waypoints.length == 1) {
            final var xDeriv = (3 * (xFinal[0]
                    - xInitial[0])
                    - xFinal[1] - xInitial[1])
                    / 4.0;
            final var yDeriv = (3 * (yFinal[0]
                    - yInitial[0])
                    - yFinal[1] - yInitial[1])
                    / 4.0;

            splines[0] = new QuinticHermiteSpline(
                    xInitial[0], waypoints[0].getX(),
                    xInitial[1], xDeriv,
                    0.0, 0.0,
                    yInitial[0], waypoints[0].getY(),
                    yInitial[1], yDeriv,
                    0.0, 0.0
            );

            splines[1] = new QuinticHermiteSpline(
                    waypoints[0].getX(), xFinal[0],
                    xDeriv, xFinal[1],
                    0.0, 0.0,
                    waypoints[0].getY(), yFinal[0],
                    yDeriv, yFinal[1],
                    0.0, 0.0
            );
        } else {
            splines[0] = QuinticHermiteSpline.fromPose(start.pose, end.pose,
                    start.magMultiplier, end.magMultiplier);
        }
        return splines;
    }

    /**
     * Thomas algorithm for solving tridiagonal systems Af = d.
     *
     * @param a              the values of A above the diagonal
     * @param b              the values of A on the diagonal
     * @param c              the values of A below the diagonal
     * @param d              the vector on the rhs
     * @param solutionVector the unknown (solution) vector, modified in-place
     */
    private static void thomasAlgorithm(double[] a, double[] b,
                                        double[] c, double[] d, double[] solutionVector) {
        int N = d.length;

        double[] cStar = new double[N];
        double[] dStar = new double[N];

        // This updates the coefficients in the first row
        // Note that we should be checking for division by zero here
        cStar[0] = c[0] / b[0];
        dStar[0] = d[0] / b[0];

        // Create the c_star and d_star coefficients in the forward sweep
        for (int i = 1; i < N; i++) {
            double m = 1.0 / (b[i] - a[i] * cStar[i - 1]);
            cStar[i] = c[i] * m;
            dStar[i] = (d[i] - a[i] * dStar[i - 1]) * m;
        }
        solutionVector[N - 1] = dStar[N - 1];
        // This is the reverse sweep, used to update the solution vector f
        for (int i = N - 2; i >= 0; i--) {
            solutionVector[i] = dStar[i] - cStar[i] * solutionVector[i + 1];
        }
    }


    private static double[] toControlVectorX(Pose2d pose, double ds_mag) {
        return new double[]{pose.getTranslation().getX(), pose.getRotation().getCos() * ds_mag};
    }

    private static double[] toControlVectorY(Pose2d pose, double ds_mag) {
        return new double[]{pose.getTranslation().getY(), pose.getRotation().getSin() * ds_mag};
    }
}
