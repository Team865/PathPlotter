package ca.warp7.frc2020.lib.trajectory;

import edu.wpi.first.wpilibj.geometry.Transform2d;
import edu.wpi.first.wpilibj.trajectory.Trajectory;

/**
 * Calculates the resulting velocity based on the trajectory and error
 * <p>
 * Implementations should be pure functions without side effects
 */
@FunctionalInterface
public interface PathFollower {
    /**
     * Calculate the resulting drive train velocity based on the trajectory
     *
     * @param trajectory the tracking trajectory
     * @param state      the expected state at the current time
     * @param error      the 2d transform between the expected state and actual state of robot
     * @return the adjusted chassis velocity
     */
    ChassisVelocity calculateTrajectory(Trajectory trajectory, Trajectory.State state, Transform2d error);
}
