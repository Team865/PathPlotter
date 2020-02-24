package ca.warp7.pathplotter.remote;

import ca.warp7.frc2020.lib.trajectory.ChassisVelocity;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Transform2d;

/**
 * A measured state from an actual trajectory
 */
public class MeasuredState {
    private double trajectoryTime;

    private Pose2d targetState;
    private Pose2d robotState;
    private Transform2d error;

    private ChassisVelocity targetVelocity;
    private ChassisVelocity correctedVelocity;

    private double leftPIDError;
    private double rightPIDError;

    public MeasuredState(
            double trajectoryTime,
            Pose2d targetState,
            Pose2d robotState,
            Transform2d error,
            ChassisVelocity targetVelocity,
            ChassisVelocity correctedVelocity,
            double leftPIDError,
            double rightPIDError
    ) {
        this.trajectoryTime = trajectoryTime;
        this.targetState = targetState;
        this.robotState = robotState;
        this.error = error;
        this.targetVelocity = targetVelocity;
        this.correctedVelocity = correctedVelocity;
        this.leftPIDError = leftPIDError;
        this.rightPIDError = rightPIDError;
    }

    public double getTrajectoryTime() {
        return trajectoryTime;
    }

    public Pose2d getTargetState() {
        return targetState;
    }

    public Pose2d getRobotState() {
        return robotState;
    }

    public Transform2d getError() {
        return error;
    }

    public ChassisVelocity getTargetVelocity() {
        return targetVelocity;
    }

    public ChassisVelocity getCorrectedVelocity() {
        return correctedVelocity;
    }

    public double getLeftPIDError() {
        return leftPIDError;
    }

    public double getRightPIDError() {
        return rightPIDError;
    }
}
