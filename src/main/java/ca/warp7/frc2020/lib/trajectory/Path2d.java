package ca.warp7.frc2020.lib.trajectory;

import edu.wpi.first.wpilibj.geometry.*;

import java.util.List;
import java.util.function.Function;


public interface Path2d {

    class Waypoint {
        public Pose2d pose;
        public double startHeadingMagnitude = 1.2;
        public double endHeadingMagnitude = 1.2;
        public String descriptor;
        public boolean optimizing;

        public Waypoint(Pose2d pose) {
            this.pose = pose;
        }
    }

    Path2d withUnit(double metresPerUnit, Function<Path2d, Path2d> func);

    default Path2d withMetres(Function<Path2d, Path2d> func) {
        return withUnit(1.0, func);
    }

    default Path2d withInches(Function<Path2d, Path2d> func) {
        return withUnit(0.0254, func);
    }

    default Path2d withFeet(Function<Path2d, Path2d> func) {
        return withUnit(0.3048, func);
    }

    Path2d moveAbsolute(double x, double y, double headingDegrees);

    default Path2d moveAbsolute(Pose2d pose) {
        return moveAbsolute(
                pose.getTranslation().getX(),
                pose.getTranslation().getY(),
                pose.getRotation().getDegrees()
        );
    }

    Path2d moveRelative(double forward, double lateral, double headingChangeDegrees);


    default Path2d moveRelative(Transform2d transform) {
        return moveRelative(transform.getTranslation().getX(),
                transform.getTranslation().getY(), transform.getRotation().getDegrees());
    }

    default Path2d translate(double forward, double lateral) {
        return moveRelative(forward, lateral, 0);
    }

    default Path2d translate(Translation2d translation) {
        return moveRelative(translation.getX(), translation.getY(), 0);
    }

    default Path2d rotate(double headingChangeDegrees) {
        return moveRelative(0, 0, headingChangeDegrees);
    }

    default Path2d rotate(Rotation2d rotation) {
        return moveRelative(0, 0, rotation.getDegrees());
    }

    default Path2d forward(double forward) {
        return moveRelative(forward, 0, 0);
    }

    default Path2d exp(Twist2d twist) {

        Pose2d exp = new Pose2d().exp(twist);
        return moveRelative(exp.getTranslation().getX(),
                exp.getTranslation().getY(), exp.getRotation().getDegrees());
    }

    List<Waypoint> collect();

    Path2d transformAll(Transform2d transform);

    Path2d mirrorX();

    Path2d mirrorY();

    static Path2d of(Pose2d... initialPoints) {
        if (initialPoints == null || initialPoints.length == 0) {
            return new ArrayPath2d(new Pose2d());
        }
        return new ArrayPath2d(initialPoints);
    }
}
