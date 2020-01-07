package ca.warp7.frc2020.lib.trajectory;

import edu.wpi.first.wpilibj.spline.PoseWithCurvature;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryParameterizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class TrajectoryController {

    private Path2d path;
    private TrajectoryConfig config;
    private List<Trajectory> trajectories;
    private FutureTask<List<Trajectory>> generator;
    private double generationTimeMs;
    private int generationLoopCount;

    public TrajectoryController(Path2d path, TrajectoryConfig config) {
        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(config, "config cannot be null");
        this.path = path;
        this.config = config;
    }

    public double getGenerationTimeMs() {
        return generationTimeMs;
    }

    public int getGenerationLoopCount() {
        return generationLoopCount;
    }

    public List<Trajectory> getTrajectories() {
        return trajectories;
    }

    public void initTrajectory() {
        if (generator != null) {
            throw new IllegalStateException("Trajectory is already generated");
        }
        List<Path2d.Waypoint> points = path.collect();
        generator = new FutureTask<>(() -> {
            long initialTime = System.nanoTime();
            List<Trajectory> result = generateTrajectory(points, config);
            generationTimeMs = (System.nanoTime() - initialTime) / 1E6;
            return result;
        });
        generator.run();
    }

    public boolean tryFinishGeneratingTrajectory() {
        if (generator == null || !generator.isDone()) {
            generationLoopCount++;
            return false;
        }
        try {
            trajectories = generator.get();
            generator = null;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static Trajectory generateTrajectory(
            List<QuinticHermiteSpline> splines,
            TrajectoryConfig config,
            boolean optimizePath
    ) {
        if (optimizePath) {
            QuinticHermiteSpline.optimizeSpline(splines);
        }
        List<PoseWithCurvature> points = QuinticHermiteSpline.parameterize(splines);

        return TrajectoryParameterizer.timeParameterizeTrajectory(points, config.getConstraints(),
                config.getStartVelocity(), config.getEndVelocity(), config.getMaxVelocity(),
                config.getMaxAcceleration(), config.isReversed());
    }

    private static List<Trajectory> generateTrajectory(List<Path2d.Waypoint> points, TrajectoryConfig config) {
        Objects.requireNonNull(points, "points cannot be null");
        Objects.requireNonNull(config, "config cannot be null");
        if (points.size() < 2) {
            throw new IllegalArgumentException("points cannot be made into a path");
        }

        List<Trajectory> trajectories = new ArrayList<>();
        List<QuinticHermiteSpline> splines = new ArrayList<>();
        boolean optimizePath = true;

        for (int i = 0; i < points.size() - 1; i++) {
            Path2d.Waypoint a = points.get(i);
            Path2d.Waypoint b = points.get(i + 1);
            if (!a.pose.getTranslation().equals(b.pose.getTranslation())) {
                splines.add(QuinticHermiteSpline.fromPose(a.pose, b.pose,
                        a.endHeadingMagnitude, b.startHeadingMagnitude));
                optimizePath = optimizePath && a.optimizing;
            } else {
                if (!splines.isEmpty()) {
                    trajectories.add(generateTrajectory(splines, config, optimizePath));
                    splines.clear();
                }
            }
        }

        if (!splines.isEmpty()) {
            trajectories.add(generateTrajectory(splines, config, optimizePath));
            splines.clear();
        }

        return trajectories;
    }
}
