package ca.warp7.pathplotter.networktables;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Transform2d;

/**
 * A measured state from an actual trajectory
 */
public class MeasuredState {
    private double trajectoryTime;
    private Pose2d robotState;
    private Transform2d error;
    private double correctedLinear;
    private double correctedAngular;
    private double leftPIDError;
    private double rightPIDError;

    public MeasuredState(
            double trajectoryTime,
            Pose2d robotState,
            Transform2d error,
            double correctedLinear,
            double correctedAngular,
            double leftPIDError,
            double rightPIDError
    ) {
        this.trajectoryTime = trajectoryTime;
        this.robotState = robotState;
        this.error = error;
        this.correctedLinear = correctedLinear;
        this.correctedAngular = correctedAngular;
        this.leftPIDError = leftPIDError;
        this.rightPIDError = rightPIDError;
    }

    public double getTrajectoryTime() {
        return trajectoryTime;
    }

    public Pose2d getRobotState() {
        return robotState;
    }

    public Transform2d getError() {
        return error;
    }

    public double getCorrectedLinear() {
        return correctedLinear;
    }

    public double getCorrectedAngular() {
        return correctedAngular;
    }

    public double getLeftPIDError() {
        return leftPIDError;
    }

    public double getRightPIDError() {
        return rightPIDError;
    }
}
