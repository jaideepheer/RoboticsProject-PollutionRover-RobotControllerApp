package jaideepheer.robo.roverone.robocontroller;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public static Context mainContext;
    public static Activity mainActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainContext = getApplicationContext();
        mainActivity = MainActivity.this;
        init();
    }
    private void init()
    {
        // get ui elements
        staticContext.debugTextView = findViewById(R.id.debugTextView);
        staticContext.joystick = findViewById(R.id.mainJoyStick);
        (findViewById(R.id.select_bt)).setOnClickListener((view->{
            BTDeviceHandler.selectBTDevice();}));
        // init.
        BTDeviceHandler.init();
        // set joystick update func.
        staticContext.joystick.setOnMoveListener(controllerState.getJoystickUpdater());
    }
}
