package jaideepheer.robo.roverone.robocontroller;

import android.widget.TextView;
import io.github.controlwear.virtual.joystick.android.JoystickView;

public class staticContext {
    public static JoystickView joystick;
    public static TextView debugTextView;
    public static TextView latencyView;

    public static void debugPrint(Object o)
    {
        System.out.println("Log: "+o);
        debugTextView.append("\nLog: "+o);
    }
}
