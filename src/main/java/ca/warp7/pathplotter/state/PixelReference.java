package ca.warp7.pathplotter.state;

import edu.wpi.first.wpilibj.geometry.Translation2d;

public class PixelReference {

    private double pixelPerMetreWidth = 0;
    private double pixelPerMetreHeight = 0;
    private double originX = 0;
    private double originY = 0;

    public void set(
            FieldConfig config,
            double view_width,
            double view_height,
            double offsetX,
            double offsetY
    ) {
        var field_width_pixels = config.getRightPx() - config.getLeftPx();
        var field_height_pixels = config.getBottomPx() - config.getTopPx();
        var image_width = config.getImage().getWidth();
        var image_height = config.getImage().getHeight();

        var field_in_view_width = view_width / image_width * field_width_pixels;
        var field_in_view_height = view_height / image_height * field_height_pixels;

        pixelPerMetreWidth = field_in_view_width / config.getFieldLengthMetres();
        pixelPerMetreHeight = field_in_view_height / config.getFieldWidthMetres();

        var centerY = (config.getTopPx() + config.getBottomPx()) / 2.0;

        originX = offsetX + config.getLeftPx() / image_width * view_width;
        originY = offsetY + centerY / image_height * view_height;
    }

    public Translation2d scale(Translation2d point) {
        return new Translation2d(
                pixelPerMetreWidth * point.getX(),
                -pixelPerMetreHeight * point.getY()
        );
    }

    @SuppressWarnings("unused")
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
