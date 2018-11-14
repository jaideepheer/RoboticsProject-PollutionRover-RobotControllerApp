package jaideepheer.robo.roverone.robocontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.controlwear.virtual.joystick.android.JoystickView;
import jaideepheer.robo.roverone.robocontroller.UIHandlers.UIRefresher;

import static jaideepheer.robo.roverone.robocontroller.staticGlobalVars.btAdapter;
import static jaideepheer.robo.roverone.robocontroller.staticGlobalVars.btSocket;
import static jaideepheer.robo.roverone.robocontroller.staticGlobalVars.btDevice;

public class ControllerActivity extends AppCompatActivity {

    // Use ButterKnife to get all views.
    @BindView(R.id.deviceNameTV) public TextView deviceNameTV;
    @BindView(R.id.latencyTV) public TextView latencyTV;
    @BindView(R.id.temperatureTV) public TextView temperatureTV;
    @BindView(R.id.airTV) public TextView airTV;
    @BindView(R.id.joystickView) public JoystickView joystickView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check vars
        if(btAdapter==null || btDevice==null || btSocket==null
                || !btAdapter.isEnabled()
                || !btSocket.isConnected())
        {
            Log.d("OnCreate", "kuch toh gadbad hai.");
            Intent myIntent = new Intent(this, chooseDevice.class);
            startActivity(myIntent);
            finish();
        }
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this); // process ButterKnife annotations
        setUIUpdaters(); // set UI listeners
    }

    // ======================================================================
    //                          Activity Helpers
    // ======================================================================

    private void setUIUpdaters()
    {
        // set joystick onMove listener
        joystickView.setOnMoveListener(controllerState.getJoystickUpdater());   // set joystick to update controllerState on movement

        // start latency view refresh loop
        new Thread(()->{
            Log.d("UIRefresher Thread","Started.");
            Thread curt = Thread.currentThread();
            UIRefresher uir = new UIRefresher(this, curt);
            while(!(this.isDestroyed() || this.isFinishing()))
            {
                synchronized (curt) {
                    this.latencyTV.post(uir);
                    try {
                        curt.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.d("UIRefresher Thread","Stopped.");
        },"UIRefresher Thread").start();
    }
}
