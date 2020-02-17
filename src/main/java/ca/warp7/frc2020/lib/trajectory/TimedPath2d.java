package ca.warp7.frc2020.lib.trajectory;

import edu.wpi.first.wpilibj.geometry.*;
import edu.wpi.first.wpilibj.spline.PoseWithCurvature;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryParameterizer;
import edu.wpi.first.wpilibj.trajectory.constraint.TrajectoryConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Helper class for a path configuration
 */
@SuppressWarnings("unused")
public class TimedPath2d {

    /**
     * A control point in the path, determined by the pose
     */
    public static class ControlPoint {

        // The location and heading of the point
        public Pose2d pose;

        // The expected magnitude of the derivative of the path at this point
        public double headingMagnitude = 1.2;

        public ControlPoint(Pose2d pose) {
            this.pose = pose;
        }

        public ControlPoint(Pose2d pose, double headingMagnitude) {
            this.pose = pose;
            this.headingMagnitude = headingMagnitude;
        }

        @Override
        public String toString() {
            return "ControlPoint(" +
                    pose +
                    ", headingMag: " + headingMagnitude +
                    ')';
        }
    }

    private String pathName;
    private List<ControlPoint> points;
    private PathFollower follower;
    private TrajectoryConfig config;
    private boolean optimizePath;

    public TimedPath2d(String pathName, Pose2d initialPoint, Pose2d... initialPoints) {
        points = new ArrayList<>();
        points.add(new ControlPoint(initialPoint));
        for (Pose2d point : initialPoints) {
            points.add(new ControlPoint(point));
        }

        this.pathName = pathName;
    }

    private TimedPath2d(String pathName,
                        List<ControlPoint> points,
                        PathFollower follower,
                        TrajectoryConfig config,
                        boolean optimizePath) {
        this.pathName = pathName;
        this.points = points;
        this.follower = follower;
        this.config = config;
        this.optimizePath = optimizePath;
    }

    public PathFollower getFollower() {
        return follower;
    }

    public TimedPath2d setFollower(PathFollower follower) {
        this.follower = follower;
        return this;
    }

    public String getName() {
        return pathName;
    }

    public TimedPath2d setConfig(TrajectoryConfig config) {
        this.config = config;
        return this;
    }

    public TrajectoryConfig getConfig() {
        return config;
    }

    public TimedPath2d setOptimizePath(boolean optimizePath) {
        this.optimizePath = optimizePath;
        return this;
    }

    /**
     * Applies a function to the config
     *
     * @param func the function
     */
    public TimedPath2d applyConfig(Function<TrajectoryConfig, TrajectoryConfig> func) {
        Objects.requireNonNull(config, "config cannot be null");
        func.apply(config);
        return this;
    }

    /**
     * Sets the reversed flag of the trajectory.
     *
     * @param reversed Whether the trajectory should be reversed or not.
     */
    public TimedPath2d setReversed(boolean reversed) {
        getConfig().setReversed(reversed);
        return this;
    }

    /**
     * Adds a user-defined constraint to the trajectory.
     *
     * @param constraint The user-defined constraint.
     */
    public TimedPath2d addConstraint(TrajectoryConstraint... constraint) {
        config.addConstraints(Arrays.asList(constraint));
        return this;
    }

    public TimedPath2d addPoint(double x, double y, double headingDegrees) {
        points.add(new ControlPoint(new Pose2d(x, y, Rotation2d.fromDegrees(headingDegrees))));
        return this;
    }

    public TimedPath2d addRelative(double forward, double lateral, double headingChangeDegrees) {
        Pose2d previousPose = points.isEmpty() ? new Pose2d() :
                points.get(points.size() - 1).pose;
        Transform2d delta = new Transform2d(
                new Translation2d(forward, lateral),
                Rotation2d.fromDegrees(headingChangeDegrees));
        points.add(new ControlPoint(previousPose.plus(delta)));
        return this;
    }

    public TimedPath2d transformAll(Transform2d transform) {
        for (int i = 0; i < points.size(); i++) {
            points.set(i, new ControlPoint(points.get(i).pose.transformBy(transform)));
        }
        return this;
    }

    public TimedPath2d mirroredX() {
        return copyTransform(p -> new Pose2d(-p.getTranslation().getX(), p.getTranslation().getY(),
                new Rotation2d(-p.getRotation().getCos(), p.getRotation().getSin())));
    }

    public TimedPath2d mirroredY() {
        return copyTransform(p -> new Pose2d(-p.getTranslation().getX(), p.getTranslation().getY(),
                new Rotation2d(-p.getRotation().getCos(), p.getRotation().getSin())));
    }

    public TimedPath2d scaled(double factor) {
        return copyTransform(p -> new Pose2d(p.getTranslation().times(factor), p.getRotation()));
    }

    public TimedPath2d copyTransform(Function<Pose2d, Pose2d> transform) {
        List<ControlPoint> newPoints = new ArrayList<>();
        for (ControlPoint point : points) {
            newPoints.add(new ControlPoint(transform.apply(point.pose)));
        }
        return new TimedPath2d(pathName, newPoints, follower, config, optimizePath);
    }

    public TimedPath2d addPoint(Pose2d pose) {
        return addPoint(
                pose.getTranslation().getX(),
                pose.getTranslation().getY(),
                pose.getRotation().getDegrees()
        );
    }

    public TimedPath2d addRelative(Transform2d transform) {
        return addRelative(transform.getTranslation().getX(),
                transform.getTranslation().getY(), transform.getRotation().getDegrees());
    }

    public TimedPath2d addTranslate(double forward, double lateral) {
        return addRelative(forward, lateral, 0);
    }

    public TimedPath2d addTranslate(Translation2d translation) {
        return addRelative(translation.getX(), translation.getY(), 0);
    }

    public TimedPath2d addForward(double forward) {
        return addRelative(forward, 0, 0);
    }

    public TimedPath2d expTo(Twist2d twist) {
        Pose2d exp = new Pose2d().exp(twist);
        return addRelative(exp.getTranslation().getX(),
                exp.getTranslation().getY(), exp.getRotation().getDegrees());
    }

    public List<Trajectory> asTrajectory() {
        Objects.requireNonNull(config, "config cannot be null");
        if (points.size() < 2) {
            throw new IllegalArgumentException("<2 points cannot be made into a path");
        }
        return generateTrajectory(points, config, optimizePath);
    }

    public List<ControlPoint> getPoints() {
        return points;
    }

    /**
     * This helps with builder-style programming
     */
    public <T> T convertTo(Function<TimedPath2d, T> function) {
        return function.apply(this);
    }

    private static List<Trajectory> generateTrajectory(
            List<ControlPoint> points,
            TrajectoryConfig config,
            boolean optimizePath
    ) {
        List<Trajectory> trajectories = new ArrayList<>();
        List<QuinticHermiteSpline> splines = new ArrayList<>();

        boolean reversed = config.isReversed();

        for (int i = 0; i < points.size() - 1; i++) {
            ControlPoint a = points.get(i);
            ControlPoint b = points.get(i + 1);
            if (a.pose.getTranslation().equals(b.pose.getTranslation())) {
                if (isReversed(a.pose.getRotation(), b.pose.getRotation())) {
                    if (!splines.isEmpty()) {
                        trajectories.add(generateTrajectory(splines, config, optimizePath, reversed));
                        splines.clear();
                    }
                    reversed = !reversed;
                } else {
                    throw new IllegalArgumentException("Cannot have two pose at the same location" +
                            "without reversing the direction");
                }
            } else {
                splines.add(QuinticHermiteSpline.fromPose(a.pose, b.pose,
                        a.headingMagnitude, a.headingMagnitude));
            }
        }

        if (!splines.isEmpty()) {
            trajectories.add(generateTrajectory(splines, config, optimizePath, reversed));
        }

        return trajectories;
    }

    private static Trajectory generateTrajectory(
            List<QuinticHermiteSpline> splines,
            TrajectoryConfig config,
            boolean optimizePath,
            boolean reversed
    ) {
        if (optimizePath) {
            QuinticHermiteSpline.optimizeSpline(splines);
        }
        List<PoseWithCurvature> points = QuinticHermiteSpline.parameterize(splines);
        return TrajectoryParameterizer.timeParameterizeTrajectory(points, config.getConstraints(),
                config.getStartVelocity(), config.getEndVelocity(), config.getMaxVelocity(),
                config.getMaxAcceleration(), reversed);
    }

    /**
     * Check if two rotations are the reverse (i.e. 180 degrees) from each other
     */
    private static boolean isReversed(Rotation2d a, Rotation2d b) {
        return b.equals(a.plus(new Rotation2d(-1, 0)));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TimedPath2d([\n");
        for (ControlPoint point : points) {
            builder.append("\t");
            builder.append(point);
            builder.append('\n');
        }
        builder.append("])");
        return builder.toString();
    }
}
