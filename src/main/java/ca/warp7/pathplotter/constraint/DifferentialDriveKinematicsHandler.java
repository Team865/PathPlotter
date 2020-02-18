package ca.warp7.pathplotter.constraint;

import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.wpilibj.trajectory.constraint.DifferentialDriveKinematicsConstraint;
import edu.wpi.first.wpilibj.trajectory.constraint.TrajectoryConstraint;

public class DifferentialDriveKinematicsHandler implements ConstraintHandler {

    private boolean enabled;
    private double trackWidthMetres;
    private double maxSpeedMetresPerSecond;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getTrackWidthMetres() {
        return trackWidthMetres;
    }

    public void setTrackWidthMetres(double trackWidthMetres) {
        this.trackWidthMetres = trackWidthMetres;
    }

    public double getMaxSpeedMetresPerSecond() {
        return maxSpeedMetresPerSecond;
    }

    public void setMaxSpeedMetresPerSecond(double maxSpeedMetresPerSecond) {
        this.maxSpeedMetresPerSecond = maxSpeedMetresPerSecond;
    }

    @Override
    public TrajectoryConstraint getConstraint() {
        if (enabled) {
            return new DifferentialDriveKinematicsConstraint(
                    new DifferentialDriveKinematics(trackWidthMetres), maxSpeedMetresPerSecond);
        } else {
            return null;
        }
    }
}
