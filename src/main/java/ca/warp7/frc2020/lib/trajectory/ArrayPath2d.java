package ca.warp7.frc2020.lib.trajectory;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Transform2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class ArrayPath2d implements Path2d {

    private List<Waypoint> points;
    private double metresPerUnit;

    private ArrayPath2d(List<Waypoint> points, double metresPerUnit) {
        this.points = points;
        this.metresPerUnit = metresPerUnit;
    }

    public ArrayPath2d(Pose2d... initialPoints) {
        points = Arrays.stream(initialPoints).map(Waypoint::new).collect(Collectors.toList());
        this.metresPerUnit = 1.0;
    }

    @Override
    public Path2d withUnit(double metresPerUnit, Function<Path2d, Path2d> func) {
        if (this.metresPerUnit != 1.0) {
            throw new IllegalStateException("Cannot nest withUnit");
        }
        func.apply(new ArrayPath2d(points, metresPerUnit));
        return this;
    }

    @Override
    public Path2d moveAbsolute(double x, double y, double headingDegrees) {
        points.add(new Waypoint(new Pose2d(x * metresPerUnit, y * metresPerUnit,
                Rotation2d.fromDegrees(headingDegrees))));
        return this;
    }

    @Override
    public Path2d moveRelative(double forward, double lateral, double headingChangeDegrees) {
        Transform2d delta = new Transform2d(
                new Translation2d(forward * metresPerUnit, lateral * metresPerUnit),
                Rotation2d.fromDegrees(headingChangeDegrees));
        points.add(new Waypoint(points.get(points.size() - 1).pose.plus(delta)));
        return this;
    }

    @Override
    public List<Waypoint> collect() {
        return List.copyOf(points);
    }

    @Override
    public Path2d transformAll(Transform2d transform) {
        for (int i = 0; i < points.size(); i++) {
            points.set(i, new Waypoint(points.get(i).pose.transformBy(transform)));
        }
        return this;
    }

    @Override
    public Path2d mirrorX() {
        for (int i = 0; i < points.size(); i++) {
            Pose2d p = points.get(i).pose;
            Pose2d p2 = new Pose2d(p.getTranslation().getX(), -p.getTranslation().getY(),
                    new Rotation2d(p.getRotation().getCos(), -p.getRotation().getSin()));
            points.set(i, new Waypoint(p2));
        }
        return this;
    }

    @Override
    public Path2d mirrorY() {
        for (int i = 0; i < points.size(); i++) {
            Pose2d p = points.get(i).pose;
            Pose2d p2 = new Pose2d(-p.getTranslation().getX(), p.getTranslation().getY(),
                    new Rotation2d(-p.getRotation().getCos(), p.getRotation().getSin()));
            points.set(i, new Waypoint(p2));
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Path2d([\n");
        for (Waypoint point : points) {
            builder.append(point.descriptor);
            builder.append('\n');
        }
        builder.append("])");
        return builder.toString();
    }
}
