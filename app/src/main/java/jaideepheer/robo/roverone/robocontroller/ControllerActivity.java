package jaideepheer.robo.roverone.robocontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ControllerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    // ======================================================================
    //                          Activity Helpers
    // ======================================================================

    void bindLayoutListeners()
    {
        findViewById(R.id.mainJoyStick);
    }
}
