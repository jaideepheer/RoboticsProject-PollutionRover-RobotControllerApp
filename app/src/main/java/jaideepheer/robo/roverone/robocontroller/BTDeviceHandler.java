package jaideepheer.robo.roverone.robocontroller;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class BTDeviceHandler {
    private static final UUID SerialPortServiceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
    private static BluetoothAdapter adapter;
    private static BluetoothDevice connectedDevice = null;
    private static BluetoothSocket btSocket;

    public static void init()
    {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null)
        {
            staticContext.debugPrint("BLUETOOTH NOT SUPPORTED...!");
        }
        // check BT enabled
        else if(!adapter.isEnabled())
        {
            requestBTEnable();
        }
    }
    private static void requestBTEnable()
    {
        staticContext.debugPrint("Please enable bluetooth.");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(MainActivity.mainActivity,enableBtIntent, 0,null);
    }

    private static void connectToDevice(BluetoothDevice device)
    {
        try {
            btSocket = device.createRfcommSocketToServiceRecord(SerialPortServiceUUID);
            BTSerialHandler.runListenerThread(btSocket);
            staticContext.debugPrint("BT connection established.");
        } catch (IOException e) {
            staticContext.debugPrint("Create RFComm socket failed...!");
            e.printStackTrace();
        }
    }

    public static void selectBTDevice()
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
            staticContext.debugPrint("Disconnecting device: "+ connectedDevice.getName());
            try {
                if(btSocket.isConnected())btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            btSocket = null;
            connectedDevice = null;
        }
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.mainActivity);

        alertBuilder.setTitle("Paired Bluetooth Devices");

        // get paired devices
        ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>(adapter.getBondedDevices());

        // create a list of devices
        ArrayAdapter<String> BTArrayAdapter = new ArrayAdapter<>(MainActivity.mainContext, android.R.layout.simple_list_item_1);
        for(BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());

        // Button OK
        alertBuilder.setSingleChoiceItems(BTArrayAdapter,0,(dialog,which)->{
            // Item selected
            connectedDevice = pairedDevices.get(which);
            staticContext.debugPrint("Selected: "+connectedDevice.getName()+"; mac: "+ connectedDevice.getAddress());
            connectToDevice(connectedDevice);
            dialog.dismiss();
        });
        alertBuilder.setNegativeButton("Cancell", (dialog, which) -> {dialog.dismiss();
            connectedDevice = null;});

        // Create popup and show
        alertBuilder.show();
    }
}
