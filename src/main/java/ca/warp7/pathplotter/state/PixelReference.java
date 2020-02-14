package ca.warp7.pathplotter.state;

import edu.wpi.first.wpilibj.geometry.Translation2d;

public class PixelReference {
    private FieldConfig config;

    public FieldConfig getConfig() {
        return config;
    }

    public void setConfig(FieldConfig config) {
        this.config = config;
    }

    private double pixelPerMetreWidth = 0;
    private double pixelPerMetreHeight = 0;
    private double originX = 0;
    private double originY = 0;

    public void set(
            double viewWidth,
            double viewHeight,
            double offsetX,
            double offsetY
    ) {
        pixelPerMetreWidth = viewWidth / 8.2296 / 2; // config.getFieldLengthMetres();
        pixelPerMetreHeight = viewHeight / 8.2296; //config.getFieldWidthMetres();

        originX = offsetX;
        originY = offsetY + viewHeight / 2.0;
    }

    public Translation2d scale(Translation2d point) {
        return new Translation2d(
                pixelPerMetreWidth * point.getX(),
                -pixelPerMetreHeight * point.getY()
        );
    }

    public Translation2d inverseScale(Translation2d point) {
        return new Translation2d(
                -point.getX() / pixelPerMetreWidth,
                point.getY() / pixelPerMetreHeight
        );
    }

    public Translation2d transform(Translation2d point) {
        return new Translation2d(
                originX + pixelPerMetreWidth * point.getX(),
                originY - pixelPerMetreHeight * point.getY()
        );
    }

    public Translation2d inverseTransform(Translation2d point) {
        return new Translation2d(
                (point.getX() - originX) / pixelPerMetreWidth,
                -(point.getY() - originY) / pixelPerMetreHeight
        );
    }
}
