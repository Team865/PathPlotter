package ca.warp7.pathplotter.state;

import javafx.scene.image.Image;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
            var input = FieldConfig.class.getResourceAsStream(resPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }

            var json = new JSONObject(result.toString());
            var fc = json.getJSONObject("field-corners");
            var image = new Image(FieldConfig.class
                    .getResourceAsStream("/" + json.getString("field-image")));
            return new FieldConfig(
                    json.getString("game"),
                    image,
                    fc.getJSONArray("top-left").getInt(1),
                    fc.getJSONArray("bottom-right").getInt(0),
                    fc.getJSONArray("bottom-right").getInt(1),
                    fc.getJSONArray("top-left").getInt(0),
                    json.getJSONArray("field-size").getDouble(0) * 0.3048,
                    json.getJSONArray("field-size").getDouble(1) * 0.3048
            );
        } catch (IOException e) {
            e.printStackTrace();
            return DEFAULT;
        }
    }

    public static FieldConfig fromExternalFile(String pathStr) {
        try {
            var data = Files.readString(Path.of(pathStr));
            var json = new JSONObject(data);
            var fc = json.getJSONObject("field-corners");
            return new FieldConfig(
                    json.getString("game"),
                    new Image(new FileInputStream(json.getString("field-image"))),
                    fc.getJSONArray("top-left").getInt(1),
                    fc.getJSONArray("bottom-right").getInt(0),
                    fc.getJSONArray("bottom-right").getInt(1),
                    fc.getJSONArray("top-left").getInt(0),
                    json.getJSONArray("field-size").getDouble(0) * 0.3048,
                    json.getJSONArray("field-size").getDouble(1) * 0.3048
            );
        } catch (IOException e) {
            e.printStackTrace();
            return DEFAULT;
        }
    }

    @Override
    public String toString() {
        return "FieldConfig{" +
                "name='" + name + '\'' +
                ", image=" + image +
                ", topPx=" + topPx +
                ", rightPx=" + rightPx +
                ", bottomPx=" + bottomPx +
                ", leftPx=" + leftPx +
                ", fieldLengthMetres=" + fieldLengthMetres +
                ", fieldWidthMetres=" + fieldWidthMetres +
                '}';
    }

    public static final FieldConfig DEFAULT = new FieldConfig("None", null,
            0, 0, 0, 0, 0, 0);
}
