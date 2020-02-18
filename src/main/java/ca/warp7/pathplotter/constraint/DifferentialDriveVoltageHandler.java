package ca.warp7.pathplotter.constraint;

import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.wpilibj.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.wpilibj.trajectory.constraint.TrajectoryConstraint;

public class DifferentialDriveVoltageHandler implements ConstraintHandler {

    private boolean enabled;
    private double ks;
    private double kv;
    private double ka;
    private double trackWidthMetres;
    private double maxVoltage;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getKs() {
        return ks;
    }

    public void setKs(double ks) {
        this.ks = ks;
    }

    public double getKv() {
        return kv;
    }

    public void setKv(double kv) {
        this.kv = kv;
    }

    public double getKa() {
        return ka;
    }

    public void setKa(double ka) {
        this.ka = ka;
    }

    public double getMaxVoltage() {
        return maxVoltage;
    }

    public void setMaxVoltage(double maxVoltage) {
        this.maxVoltage = maxVoltage;
    }

    public double getTrackWidthMetres() {
        return trackWidthMetres;
    }

    public void setTrackWidthMetres(double trackWidthMetres) {
        this.trackWidthMetres = trackWidthMetres;
    }

    @Override
    public TrajectoryConstraint getConstraint() {
        if (enabled) {
            return new DifferentialDriveVoltageConstraint(
                    new SimpleMotorFeedforward(ks, kv, ka),
                    new DifferentialDriveKinematics(trackWidthMetres),
                    maxVoltage
            );
        }
        return null;
    }
}
