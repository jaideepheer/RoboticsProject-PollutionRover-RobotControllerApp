package jaideepheer.robo.roverone.robocontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import jaideepheer.robo.roverone.robocontroller.BluetoothClasses.BTAdapterHandler;

public class MainActivity extends AppCompatActivity {
    public static Context mainContext;
    public static Activity mainActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.mainpage);
        mainContext = getApplicationContext();
        mainActivity = MainActivity.this;
        //init();
    }
    private void init()
    {
        // get ui elements
        staticContext.latencyView = findViewById(R.id.latencyView);
        staticContext.debugTextView = findViewById(R.id.debugTextView);
        staticContext.debugTextView.setMovementMethod(new ScrollingMovementMethod());
        staticContext.joystick = findViewById(R.id.mainJoyStick);
        (findViewById(R.id.select_bt)).setOnClickListener((view->{
            BTAdapterHandler.selectAndConnectBTDevice();}));
        // init.
        BTAdapterHandler.init();
        // set joystick update func.
        staticContext.joystick.setOnMoveListener(controllerState.getJoystickUpdater());
    }
    private void initIntentFilter()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
             //Device found
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
             //Device is now connected
                staticContext.debugPrint("Device connect intent.");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
             //Done searching
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
             //Device is about to disconnect
                staticContext.debugPrint("Device disconnect requested intent.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
             //Device has disconnected
                staticContext.debugPrint("Device disconnected intent.");
            }
        }
    };
}
