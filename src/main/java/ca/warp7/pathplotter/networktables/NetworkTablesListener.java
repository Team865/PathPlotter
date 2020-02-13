package ca.warp7.pathplotter.networktables;

import edu.wpi.first.networktables.NetworkTableInstance;

public class NetworkTablesListener {

    private static final NetworkTablesListener s_instance = new NetworkTablesListener();

    public static NetworkTablesListener getInstance() {
        return s_instance;
    }

    private NetworkTableInstance nt = NetworkTableInstance.getDefault();

}
