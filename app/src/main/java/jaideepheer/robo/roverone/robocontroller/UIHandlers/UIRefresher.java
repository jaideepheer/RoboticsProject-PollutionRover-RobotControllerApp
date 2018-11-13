package jaideepheer.robo.roverone.robocontroller.UIHandlers;

import jaideepheer.robo.roverone.robocontroller.BluetoothClasses.BTSerialHandler;
import jaideepheer.robo.roverone.robocontroller.ControllerActivity;

public class UIRefresher implements Runnable {
    private ControllerActivity parent;
    private final Object toNotifyOnUpdate;
    public UIRefresher(ControllerActivity parentView, Object toNotifyOnUpdate)
    {
        parent = parentView;
        this.toNotifyOnUpdate = toNotifyOnUpdate;
    }
    @Override
    public void run() {
        // Update UI
        long latency = BTSerialHandler.getReadyMessageLatency();
        parent.latencyTV.setText("Latency: " + (latency>-1? latency + " ms.":"Disconnected."));

        // Notify that update is done
        synchronized (toNotifyOnUpdate)
        {
            toNotifyOnUpdate.notify();
        }
    }
}
