package ca.warp7.pathplotter.constraint;

import edu.wpi.first.wpilibj.trajectory.constraint.CentripetalAccelerationConstraint;
import edu.wpi.first.wpilibj.trajectory.constraint.TrajectoryConstraint;

public class CentripetalAccelerationHandler implements ConstraintHandler {

    private boolean enabled;
    private double maxCentripetalAccelerationMetresPerSecondSq;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getMaxCentripetalAccelerationMetresPerSecondSq() {
        return maxCentripetalAccelerationMetresPerSecondSq;
    }

    public void setMaxCentripetalAccelerationMetresPerSecondSq(double maxCentripetalAccelerationMetresPerSecondSq) {
        this.maxCentripetalAccelerationMetresPerSecondSq = maxCentripetalAccelerationMetresPerSecondSq;
    }

    @Override
    public TrajectoryConstraint getConstraint() {
        if (enabled) {
            return new CentripetalAccelerationConstraint(maxCentripetalAccelerationMetresPerSecondSq);
        } else {
            return null;
        }
    }
}
