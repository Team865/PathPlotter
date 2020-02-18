package ca.warp7.pathplotter.constraint;

import edu.wpi.first.wpilibj.trajectory.constraint.TrajectoryConstraint;

public interface ConstraintHandler {
    TrajectoryConstraint getConstraint();
}
