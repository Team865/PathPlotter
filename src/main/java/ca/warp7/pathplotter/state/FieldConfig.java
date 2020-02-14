package ca.warp7.pathplotter.state;

import javafx.scene.image.Image;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FieldConfig {
    private String name;
    private Image image;
    private int topPx;
    private int rightPx;
    private int bottomPx;
    private int leftPx;
    private double fieldLengthMetres;
    private double fieldWidthMetres;

    public FieldConfig(
            String name,
            Image image,
            int topPx,
            int rightPx,
            int bottomPx,
            int leftPx,
            double fieldLengthMetres,
            double fieldWidthMetres
    ) {
        this.name = name;
        this.image = image;
        this.topPx = topPx;
        this.rightPx = rightPx;
        this.bottomPx = bottomPx;
        this.leftPx = leftPx;
        this.fieldLengthMetres = fieldLengthMetres;
        this.fieldWidthMetres = fieldWidthMetres;
    }

    public String getName() {
        return name;
    }

    public Image getImage() {
        return image;
    }

    public int getTopPx() {
        return topPx;
    }

    public int getRightPx() {
        return rightPx;
    }

    public int getBottomPx() {
        return bottomPx;
    }

    public int getLeftPx() {
        return leftPx;
    }

    public double getFieldLengthMetres() {
        return fieldLengthMetres;
    }

    public double getFieldWidthMetres() {
        return fieldWidthMetres;
    }

    public static FieldConfig fromResources(String resPath) {
        try {
            var data = Files.readString(Path.of(FieldConfig.class.getResource(resPath).toURI()));
            var json = new JSONObject(data);
            var fc = json.getJSONObject("field-corners");
            return new FieldConfig(
                    json.getString("game"),
                    new Image(FieldConfig.class.getResourceAsStream(json.getString("field_image"))),
                    fc.getJSONArray("top-left").getInt(1),
                    fc.getJSONArray("bottom-right").getInt(0),
                    fc.getJSONArray("top-left").getInt(0),
                    fc.getJSONArray("bottom-right").getInt(1),
                    json.getJSONArray("field-size").getDouble(0) * 0.3048,
                    json.getJSONArray("field-size").getDouble(1) * 0.3048
            );
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static FieldConfig fromExternalFile(String pathStr) {
        try {
            var data = Files.readString(Path.of(pathStr));
            var json = new JSONObject(data);
            var fc = json.getJSONObject("field-corners");
            return new FieldConfig(
                    json.getString("game"),
                    new Image(new FileInputStream(json.getString("field_image"))),
                    fc.getJSONArray("top-left").getInt(1),
                    fc.getJSONArray("bottom-right").getInt(0),
                    fc.getJSONArray("top-left").getInt(0),
                    fc.getJSONArray("bottom-right").getInt(1),
                    json.getJSONArray("field-size").getDouble(0) * 0.3048,
                    json.getJSONArray("field-size").getDouble(1) * 0.3048
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
