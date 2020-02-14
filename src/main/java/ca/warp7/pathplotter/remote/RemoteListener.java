package ca.warp7.pathplotter.remote;

import edu.wpi.first.networktables.NetworkTableInstance;

import static edu.wpi.first.networktables.EntryListenerFlags.*;

public class RemoteListener {

    private static final RemoteListener s_instance = new RemoteListener();

    public static RemoteListener getInstance() {
        return s_instance;
    }

    private NetworkTableInstance nt = NetworkTableInstance.create();

    {
        nt.addConnectionListener(conn -> System.out.println("Connection " + conn), true);
        nt.addEntryListener("", entry -> {
            System.out.println(entry.name + "=" + entry.value);
        }, kImmediate | kNew | kDelete | kUpdate);
        nt.startClient();
        nt.startDSClient();
    }
}
