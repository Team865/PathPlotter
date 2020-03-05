package ca.warp7.pathplotter.remote;

import ca.warp7.frc2020.lib.trajectory.ChassisVelocity;
import edu.wpi.first.networktables.ConnectionNotification;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Transform2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static edu.wpi.first.networktables.EntryListenerFlags.*;
import static edu.wpi.first.wpilibj.smartdashboard.SmartDashboard.getEntry;

public class RemoteListener {


    private NetworkTableInstance nt = NetworkTableInstance.getDefault();

    private List<Consumer<ConnectionNotification>> connectionListeners = new ArrayList<>();
    private List<Consumer<EntryNotification>> entryListeners = new ArrayList<>();

    private static class Entries {
        private NetworkTableEntry trajectoryTime = getEntry("Trajectory Time");
        private NetworkTableEntry targetX = getEntry("Target X (m)");
        private NetworkTableEntry targetY = getEntry("Target Y (m)");
        private NetworkTableEntry targetAngle = getEntry("Target Angle (deg)");
        private NetworkTableEntry robotX = getEntry("Robot X (m)");
        private NetworkTableEntry robotY = getEntry("Robot Y (m)");
        private NetworkTableEntry robotAngle = getEntry("Robot Angle (deg)");
        private NetworkTableEntry errorX = getEntry("Error X (m)");
        private NetworkTableEntry errorY = getEntry("Error Y (m)");
        private NetworkTableEntry errorAngle = getEntry("Error Angle (deg)");
        private NetworkTableEntry targetLinear = getEntry("Target Linear (m/s)");
        private NetworkTableEntry targetAngular = getEntry("Target Angular (deg/s)");
        private NetworkTableEntry correctedLinear = getEntry("Corrected Linear (m/s)");
        private NetworkTableEntry correctedAngular = getEntry("Corrected Angular (deg/s)");
        private NetworkTableEntry leftPIDError = getEntry("Left PID Error (m/s)");
        private NetworkTableEntry rightPIDError = getEntry("Right PID Error (m/s)");

        private MeasuredState toMeasuredState() {
            return new MeasuredState(
                    trajectoryTime.getDouble(0.0),
                    new Pose2d(
                            new Translation2d(
                                    targetX.getDouble(0.0),
                                    targetY.getDouble(0.0)
                            ),
                            Rotation2d.fromDegrees(targetAngle.getDouble(0.0))
                    ),
                    new Pose2d(
                            new Translation2d(
                                    robotX.getDouble(0.0),
                                    robotY.getDouble(0.0)
                            ),
                            Rotation2d.fromDegrees(robotAngle.getDouble(0.0))
                    ),
                    new Transform2d(
                            new Translation2d(
                                    errorX.getDouble(0.0),
                                    errorY.getDouble(0.0)
                            ),
                            Rotation2d.fromDegrees(errorAngle.getDouble(0.0))
                    ),
                    new ChassisVelocity(
                            targetLinear.getDouble(0.0),
                            Math.toRadians(targetAngular.getDouble(0.0))
                    ),
                    new ChassisVelocity(
                            correctedLinear.getDouble(0.0),
                            Math.toRadians(correctedAngular.getDouble(0.0))
                    ),
                    leftPIDError.getDouble(0.0),
                    rightPIDError.getDouble(0.0)
            );
        }
    }

    public RemoteListener() {
        nt.addConnectionListener(connectionNotification -> {
            Platform.runLater(() -> {
                for (var listener : connectionListeners) {
                    listener.accept(connectionNotification);
                }
            });
        }, true);
        nt.addEntryListener("", entryNotification -> {
            System.out.println("hi");
            Platform.runLater(() -> {
                for (var listener : entryListeners) {
                    listener.accept(entryNotification);
                }
            });
        }, kImmediate | kNew | kDelete | kUpdate);
        nt.startClient();
        nt.startDSClient();
    }

    public void addConnectionListener(Consumer<ConnectionNotification> listener) {
        connectionListeners.add(listener);
    }
}
