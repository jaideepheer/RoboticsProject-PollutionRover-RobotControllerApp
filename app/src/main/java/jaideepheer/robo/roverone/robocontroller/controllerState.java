package jaideepheer.robo.roverone.robocontroller;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class controllerState {
    /*
    NOTE: JAVA bytes are signed and hence have a range from -128 to 127.
            arm* variables must by appropriately handled.
     */
    public static byte carMove = 0x00, carTurn = 0x00, armJ1 = 0x00, armJ2 = 0x00, armRotate = 0x00;
    public static JoystickView.OnMoveListener getJoystickUpdater() {
        return (angle, strength) -> {
            // angle and strength are integers
            // calc. x and y
            double x, y;
            x = strength * Math.cos(Math.toRadians(angle));
            y = strength * Math.sin(Math.toRadians(angle));
            // set carMove and carTurn
            carMove = (byte) ((int) y);
            carTurn = (byte) ((int) x);
            // set debug text
            staticContext.debugTextView.setText("carMove: " + carMove+"; x:" +x+"; y: "+y+"; angle: "+angle);
        };
    }
}
