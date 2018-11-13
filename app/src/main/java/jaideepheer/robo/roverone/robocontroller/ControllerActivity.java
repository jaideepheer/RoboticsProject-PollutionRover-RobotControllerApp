package jaideepheer.robo.roverone.robocontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.controlwear.virtual.joystick.android.JoystickView;
import jaideepheer.robo.roverone.robocontroller.UIHandlers.UIRefresher;

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
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this); // process ButterKnife annotations
        setUIUpdaters(); // set UI listeners
    }

    // ======================================================================
    //                          Activity Helpers
    // ======================================================================

    private void setUIUpdaters()
    {
        joystickView.setOnMoveListener(controllerState.getJoystickUpdater());   // set joystick to update controllerState on movement

        // start latency refresh task loop
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
