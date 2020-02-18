package ca.warp7.pathplotter.remote;

import edu.wpi.first.networktables.ConnectionNotification;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static edu.wpi.first.networktables.EntryListenerFlags.*;

public class RemoteListener {


    private NetworkTableInstance nt = NetworkTableInstance.getDefault();

    private List<Consumer<ConnectionNotification>> connectionListeners = new ArrayList<>();
    private List<Consumer<EntryNotification>> entryListeners = new ArrayList<>();

    public RemoteListener() {
        nt.addConnectionListener(connectionNotification -> {
            Platform.runLater(() -> {
                for (var listener : connectionListeners) {
                    listener.accept(connectionNotification);
                }
            });
        }, true);
        nt.addEntryListener("", entryNotification -> {
            System.out.println("hi");
            Platform.runLater(() -> {
                for (var listener : entryListeners) {
                    listener.accept(entryNotification);
                }
            });
        }, kImmediate | kNew | kDelete | kUpdate);
        nt.startClient();
        nt.startDSClient();
    }

    public void addConnectionListener(Consumer<ConnectionNotification> listener) {
        connectionListeners.add(listener);
    }

    public void addEntryListener(Consumer<EntryNotification> listener) {
        entryListeners.add(listener);
    }

    public void shutdown() {
        nt.close();
    }
}
