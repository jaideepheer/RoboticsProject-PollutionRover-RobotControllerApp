package jaideepheer.robo.roverone.robocontroller.BluetoothClasses;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import jaideepheer.robo.roverone.robocontroller.controllerState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BTSerialHandler {
    private static final byte HEADER = (byte)0xFA, FOOTER = (byte)0xFF;
    private static final int MAX_JUNK_READ = 7;
    private static boolean stateMessageScheduled;

    // Used for latency calculation.
    private static long prevReadyTimestamp = 0L;
    private static long readyMessageLatency = -1L;

    // Message sizes, used to parse from stream and strip header and footer.
    private static final int READY_MESSAGE_SIZE = 3;

    /**
     * Returns the current ready message latency in ms.
     * @return the current ready message latency in ms.
     */
    public static long getReadyMessageLatency()
    {
        return readyMessageLatency;
    }

    /**
     * Returns a {@link Runnable} that listens to the provided {@link BluetoothSocket} and responds to messages according to the BT Protocol.
     * @param socket is the socket to communicate on.
     * @return the runnable that handles socket communication.
     */
    public static Runnable getListenerRunnable(BluetoothSocket socket)
    {
        return ()-> {
            // schedule first state message
            stateMessageScheduled = true;
            Log.d("SerialListener", "Running BT serial listener.");
            try {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                int bytesRead;
                boolean insideMessage = false;
                byte messageType = 0;
                try {
                    mainloop:
                    while (!Thread.currentThread().isInterrupted()) {
                        // check socket connection
                        if (!socket.isConnected()) {
                            // socket disconnected
                            Log.d("SerialListener", "BT socket disconnected.");
                            break;
                        }
                        // read Input
                        // reset local var
                        bytesRead = 0;
                        // get bytes till head
                        if (in.available() > 0) {
                            // input data available
                            if (!insideMessage) {
                                // read till HEADER
                                while ((byte) (in.read()) != HEADER) {
                                    ++bytesRead;
                                    if (bytesRead >= MAX_JUNK_READ || in.available() < 1) continue mainloop;
                                }
                                messageType = (byte) -1;
                                insideMessage = true;
                            } else {
                                if (messageType == -1) {
                                    messageType = (byte) in.read();
                                }
                                // read message type
                                switch (messageType) {
                                    case (byte) 0xA1:  // Ready Message
                                        // stall if all not received
                                        if (in.available() < READY_MESSAGE_SIZE - 2) continue mainloop;
                                        // check if message valid
                                        byte[] message = parseMessageFromStream(in, READY_MESSAGE_SIZE - 2);
                                        if (message == null) {
                                            // message was invalid, continue
                                            break;
                                        }
                                        // header and footer matched, parse message now
                                        HandleReadyMessage(message);
                                        break;
                                    case HEADER:    // HEADER repeated, still inside message
                                        messageType = (byte) in.read();
                                        continue mainloop;
                                    default:
                                        // invalid message type
                                        break;
                                }
                                // message successfully handled
                                insideMessage = false;
                            }
                        }
                        // Send messages
                        if(stateMessageScheduled)
                        {
                            sendSystemState(out);
                        }
                    }
                } catch (Exception e) {
                    Log.d("SerialListener", "Exception in loop.");
                    Log.d("SerialListener", e.toString());
                }
            } catch (IOException e) {
                Log.d("SerialListener", "Couldn't get I/O streams from BT socket.");
                Log.d("SerialListener", e.toString());
            }
            Log.d("SerialListener", "Disconnected");
            Log.d("SerialListener", "Listener stopped.");
            readyMessageLatency = -1;
        };
    }

    // ============================================================================================
    //                         Private Helper Code
    //=============================================================================================

    /**
     * Processes the data in the Ready message and schedules further action.
     * @param message the bytes of the received message excluding header and message type.
     */
    private static void HandleReadyMessage(byte[] message)
    {
        // calc. latency
        readyMessageLatency = System.currentTimeMillis() - prevReadyTimestamp;
        prevReadyTimestamp = System.currentTimeMillis();
        // schedule set state message
        stateMessageScheduled = true;
    }

    /**
     * Creates and sends the SetSystemStateState message to the given {@link OutputStream}.
     * See BT Protocol for more info on SetSystemStateState message.
     * @param out the {@link OutputStream} to send the message to.
     * @throws IOException throws exception if writing on the stream fails.
     */
    private static void sendSystemState(OutputStream out) throws IOException
    {
        stateMessageScheduled = false;
        byte[] message = new byte[8];
        message[0] = HEADER;
        message[1] = (byte)0xF1;    // message type
        message[7] = FOOTER;
        // fill message buffer
            message[2] = controllerState.carMove;
            message[3] = controllerState.carTurn;
            message[4] = controllerState.armJ1;
            message[5] = controllerState.armJ2;
            message[6] = controllerState.armRotate;
        // send message
        out.write(message);
    }

    /***
     * Simply check if message ends with FOOTER and doesn't contain HEADER.
     * @param in InputStream
     * @param messageLength Required message length.
     * @return message bytes or null if invalid message.
     */
    private static byte[] parseMessageFromStream(InputStream in, int messageLength) throws IOException
    {
        // mark stream position
        in.mark(messageLength+1);
        // read bytes
        byte[] bytes = new byte[messageLength];
        in.read(bytes);
        // find headers
        int hdrpos = -1;
        for(int i=0;i<messageLength;++i)
        {
            if(bytes[i]==HEADER)
            {
                hdrpos = i;
                break;
            }
        }
        if(hdrpos>-1)
        {
            // HEADER found
            // revert stream and skip to next header
            in.reset();
            in.skip(hdrpos);
            return null;
        }
        if(bytes[messageLength-1]!=FOOTER)
        {
            // last is not footer, skip all
            return null;
        }
        return bytes;
    }
}
