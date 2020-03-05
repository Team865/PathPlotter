package ca.warp7.pathplotter.state;

import edu.wpi.first.wpilibj.geometry.Pose2d;

import java.util.Objects;

public class ControlPoint {
    public Pose2d pose;
    public boolean isSelected = false;
    public double magMultiplier = 1.2;

    public ControlPoint(Pose2d pose) {
        this.pose = pose;
    }

    public ControlPoint(Pose2d pose, boolean isSelected, double magMultiplier) {
        this.pose = pose;
        this.isSelected = isSelected;
        this.magMultiplier = magMultiplier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ControlPoint controlPoint = (ControlPoint) o;
        return pose.equals(controlPoint.pose);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pose);
    }
}
