package ca.warp7.pathplotter;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class PathPlotterApplication extends Application {

    private static HostServices hostServices;

    public static HostServices getHostServicesInstance() {
        return hostServices;
    }

    @Override
    public void start(Stage primaryStage) {
        hostServices = getHostServices();
        new PathPlotter().show();
    }
}
