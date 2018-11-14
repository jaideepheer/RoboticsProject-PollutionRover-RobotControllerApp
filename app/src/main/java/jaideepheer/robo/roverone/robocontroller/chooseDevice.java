package jaideepheer.robo.roverone.robocontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;

import java.io.IOException;
import java.util.UUID;

public class chooseDevice extends AppCompatActivity {
    private static final UUID SerialPortServiceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID

    @BindView(R.id.deviceList) public ListView deviceList;
    @BindView(R.id.connectingDeviceTV) public TextView connectingDeviceTV;
    @BindView(R.id.connectingDeviceOverlay) public FrameLayout connectingDeviceOverlay;

    // BT devices list adapter
    private ArrayAdapter<String> pairedDevices;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_device);
        ButterKnife.bind(this);

        init();
    }

    // ======================================================================
    //                          Activity Helpers
    // ======================================================================

    private void init()
    {
        pairedDevices = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1);

        initBTAdapter();
        populatePairedDevicesList();
        setUIUpdaters();
    }

    private void initBTAdapter()
    {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null)
        {
            Log.d("AdapterHandler", "BLUETOOTH NOT SUPPORTED...!");
            finish();
            return;
        }
        // check BT enabled
        if(!btAdapter.isEnabled())
        {
            requestBTEnable();
        }
    }

    private void populatePairedDevicesList()
    {
        for( BluetoothDevice dev: btAdapter.getBondedDevices())
        {
            pairedDevices.add(dev.getName()+"\n"+dev.getAddress());
        }
    }

    private void requestBTEnable()
    {
        Log.d("AdapterHandler", "Please enable bluetooth.");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
    }

    private void setUIUpdaters()
    {
        deviceList.setAdapter(pairedDevices);
        deviceList.setOnItemClickListener(getOnDeviceSelectedRunnable());
    }

    private AdapterView.OnItemClickListener getOnDeviceSelectedRunnable()
    {
        return (adapterView, view, pos, l)->{
            // Display loading overlay
               displayLoadingOverlay(adapterView.getItemAtPosition(pos).toString());
            ;
        };
    }

    public void displayLoadingOverlay(String deviceName)
    {
        runOnUiThread(()->{
            connectingDeviceTV.setText(deviceName);
            AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            connectingDeviceOverlay.setAnimation(inAnimation);
            connectingDeviceOverlay.setVisibility(View.VISIBLE);
        });
    }

    public void removeLoadingOverlay()
    {
        runOnUiThread(()->{
            Animation outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            connectingDeviceOverlay.setAnimation(outAnimation);
            connectingDeviceOverlay.setVisibility(View.GONE);
        });
    }

    private void connectToDevice(BluetoothDevice device, Runnable successCallback, Runnable failureCallback)
    {
        // Try to connect to serial socket
        new Thread(
                ()->{
                    boolean isOK = false;
                    try
                    {
                        btSocket = device.createRfcommSocketToServiceRecord(SerialPortServiceUUID);
                        try {
                            btSocket.connect();
                            Log.d("AdapterHandler", "BT connection established.");
                            isOK = true;
                        }
                        catch(IOException e)
                        {
                            Log.d("AdapterHandler", "Socket connection failed...!");
                            e.printStackTrace();
                        }
                    }
                    catch (IOException e)
                    {
                        Log.d("AdapterHandler", "Create RFComm socket failed...!");
                        e.printStackTrace();
                    }
                    if(isOK)
                    {
                        runOnUiThread(successCallback);
                    }
                    else
                    {
                        runOnUiThread(failureCallback);
                    }
                }
        , "Serial Socket Creator Thread").start();
    }
}
