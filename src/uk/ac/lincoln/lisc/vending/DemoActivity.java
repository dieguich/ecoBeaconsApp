package uk.ac.lincoln.lisc.vending;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class DemoActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DemoActivity", "OnCreate");
        /** Creating an Alert Dialog Window */
        AlertDemo alert = new AlertDemo();
 
        /** Opening the Alert Dialog Window. This will be opened when the alarm goes off */
        alert.show(getSupportFragmentManager(), "AlertDemo");
    }
}