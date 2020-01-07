/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ca.warp7.frc2020.lib.trajectory;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.trajectory.constraint.TrajectoryConstraint;

/**
 * A class that enforces constraints on the differential drive kinematics.
 * This can be used to ensure that the trajectory is constructed so that the
 * commanded velocities for both sides of the drivetrain stay below a certain
 * limit.
 */
public class SimpleDriveKinematicsConstraint implements TrajectoryConstraint {
  private final double maxVelocity;
  private final double wheelbaseRadius;

  /**
   * Constructs a differential drive dynamics constraint.
   *
   * @param wheelbaseRadius the wheelbase radius
   * @param maxVelocity The max speed that a side of the robot can travel at.
   */
  public SimpleDriveKinematicsConstraint(double wheelbaseRadius,
                                         double maxVelocity) {
    this.maxVelocity = maxVelocity;
    this.wheelbaseRadius = wheelbaseRadius;
  }


  /**
   * Solves the maximum forward velocity the robot can go on a curve,
   * given a measured max straight velocity. The parameters and
   * return values are unsigned. It must be multiplied by the
   * signed curvature to get the signed angular velocity
   *
   * Velocity constrained by these equations:
   * eqn 1. w = (right - left) / (2 * L)
   * eqn 2. v = (left + right) / 2
   *
   * 1. Rearrange equation 1:
   *        w(2 * L) = right - left;
   *        left = right - w(2 * L);
   * 2. Assuming the right side is at max velocity:
   *        right = V_max;
   *        left = V_max - w(2 * L)
   * 3. Substitute left and right into equation 2:
   *        v = (2 * V_max - w(2 * L)) / 2
   * 5. Substitute w = v * k into equation 2:
   *        v = (2 * V_max-v * k * 2 * L) / 2
   * 6. Rearrange to solve:
   *        v = V_max - v * k * L;
   *        v + v * k * L = V_max;
   *        v * (1 + k * L) = V_max;
   *        v = V_max / (1 + k * L);
   *
   * @param poseMeters              The pose at the current point in the trajectory.
   * @param curvatureRadPerMeter    The curvature at the current point in the trajectory.
   * @param velocityMetersPerSecond The velocity at the current point in the trajectory before
   *                                constraints are applied.
   * @return The absolute maximum velocity.
   */
  @Override
  public double getMaxVelocityMetersPerSecond(Pose2d poseMeters, double curvatureRadPerMeter,
                                              double velocityMetersPerSecond) {
    return maxVelocity / (1 + Math.abs(curvatureRadPerMeter) * wheelbaseRadius);
  }

  /**
   * Returns the minimum and maximum allowable acceleration for the trajectory
   * given pose, curvature, and speed.
   *
   * @param poseMeters              The pose at the current point in the trajectory.
   * @param curvatureRadPerMeter    The curvature at the current point in the trajectory.
   * @param velocityMetersPerSecond The speed at the current point in the trajectory.
   * @return The min and max acceleration bounds.
   */
  @Override
  public MinMax getMinMaxAccelerationMetersPerSecondSq(Pose2d poseMeters,
                                                       double curvatureRadPerMeter,
                                                       double velocityMetersPerSecond) {
    return new MinMax();
  }
}
