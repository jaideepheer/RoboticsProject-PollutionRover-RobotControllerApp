package jaideepheer.robo.roverone.robocontroller.BluetoothClasses;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import jaideepheer.robo.roverone.robocontroller.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class BTAdapterHandler {
    private static final UUID SerialPortServiceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
    private static BluetoothAdapter adapter;
    private static BluetoothDevice connectedDevice;
    private static BluetoothSocket btSocket;
    private static boolean isConnectToDevice = false;

    public static void init()
    {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null)
        {
            Log.d("AdapterHandler", "BLUETOOTH NOT SUPPORTED...!");
            return;
        }
        // check BT enabled
        if(!adapter.isEnabled())
        {
            requestBTEnable();
        }
    }
    private static void requestBTEnable()
    {
        Log.d("AdapterHandler", "Please enable bluetooth.");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(MainActivity.mainActivity,enableBtIntent, 0,null);
    }

    private static void connectToDevice(BluetoothDevice device)
    {
        if(isConnectToDevice)return;
        isConnectToDevice = true;
        // Run serial thread
        new Thread(
                ()->{
                    try
                    {
                        btSocket = device.createRfcommSocketToServiceRecord(SerialPortServiceUUID);
                        try {
                            btSocket.connect();
                            Log.d("AdapterHandler", "BT connection established.");
                            BTSerialHandler.getListenerRunnable(btSocket).run();
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
                    Log.d("AdapterHandler", "Device disconnected.");
                    // cleanup
                    isConnectToDevice = false;
                    if(btSocket!=null && btSocket.isConnected()) {
                        try {
                            btSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    btSocket = null;
                }
        ).start();
    }

    public static void selectAndConnectBTDevice()
    {
        // check BT enabled
        if(!adapter.isEnabled())
        {
            requestBTEnable();
            return;
        }
        // check if already connected
        if(btSocket != null && btSocket.isConnected())
        {
            Log.d("AdapterHandler", "Disconnecting device: "+ connectedDevice.getName());
            try {
                if(btSocket.isConnected())btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isConnectToDevice = false;
        }

        // Create selection dialog

        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.mainActivity);
        alertBuilder.setTitle("Paired Bluetooth Devices");
        // get paired devices
        ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>(adapter.getBondedDevices());
        // create a list of paired devices
        ArrayAdapter<String> BTArrayAdapter = new ArrayAdapter<>(MainActivity.mainContext, android.R.layout.simple_list_item_1);
        for(BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
        // Set on select listener
        alertBuilder.setSingleChoiceItems(BTArrayAdapter,0,
                (dialog,which)-> {
                    // Item selected
                    connectedDevice = pairedDevices.get(which);
                    // connect to selected device
                    connectToDevice(connectedDevice);
                    dialog.dismiss();
                }
                );
        // Add cancel button
        alertBuilder.setNegativeButton("Cancell", (dialog, which) -> dialog.dismiss());

        // Create dialog and show
        alertBuilder.show();
    }
}
