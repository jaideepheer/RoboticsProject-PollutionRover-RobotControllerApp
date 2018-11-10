package jaideepheer.robo.roverone.robocontroller;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BTSerialHandler {
    private static final byte HEADER = (byte)0xFA, FOOTER = (byte)0xFF;
    private static final int MAX_JUNK_READ = 7;

    // Message sizes used to parse from stream and strip header and footer.
    private static final int READY_MESSAGE_SIZE = 3;

    public static void HandleReadyMessage(OutputStream out, byte[] message) throws IOException
    {
        sendSystemState(out);
    }

    public static void sendSystemState(OutputStream out) throws IOException
    {
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
    public static byte[] parseMessageFromStream(InputStream in, int messageLength) throws IOException
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
    public static void runListenerThread(BluetoothSocket socket)
    {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            new Thread(() -> {
                int bytesRead;
                boolean insideMessage = false;
                byte messageType = 0;
                try {
                    mainloop: while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                        // reset local var
                        bytesRead = 0;
                        // get bytes till head
                        if (in.available()>0) {
                            // input data available
                            if(!insideMessage) {
                                // read till HEADER
                                while ((byte)(in.read()) != HEADER) {
                                    ++bytesRead;
                                    if (bytesRead >= MAX_JUNK_READ || in.available() < 1) continue mainloop;
                                }
                                messageType = (byte)-1;
                                insideMessage = true;
                            }
                            else {
                                if(messageType == -1)
                                {
                                    messageType = (byte)in.read();
                                }
                                // read message type
                                switch_case: switch (messageType) {
                                    case (byte) 0xA1:  // Ready Message
                                        // stall if all not received
                                        if(in.available()<READY_MESSAGE_SIZE-2)continue mainloop;
                                        // check if message valid
                                        byte[] message = parseMessageFromStream(in,READY_MESSAGE_SIZE-2);
                                        if(message == null)
                                        {
                                            // message was invalid, continue
                                            break switch_case;
                                        }
                                        // header and footer matched, parse message now
                                        HandleReadyMessage(out, message);
                                        break;
                                    case HEADER:    // HEADER repeated, still inside message
                                        messageType = (byte)in.read();
                                        continue mainloop;
                                    default:
                                        // invalid message type
                                        break;
                                }
                                // message successfully handled
                                insideMessage = false;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    staticContext.debugPrint("BTSerialListener: Exception in loop.");
                    staticContext.debugPrint(e);
                }
            }).run();
        }
        catch (IOException e)
        {
            staticContext.debugPrint("BTSerialListener: Couldn't get I/O streams from BT socket.");
            staticContext.debugPrint(e);
        }
    }
}
