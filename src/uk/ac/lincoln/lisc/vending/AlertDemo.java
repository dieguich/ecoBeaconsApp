package uk.ac.lincoln.lisc.vending;

import org.altbeacon.beaconreference.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
 
public class AlertDemo extends DialogFragment {
 
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
 
        /** Turn Screen On and Unlock the keypad when this alert dialog is displayed */
        getActivity().getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD);
 
        /** Creating a alert dialog builder */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
        /** Setting title for the alert dialog */
        builder.setTitle("Have you already finished your food?");
 
        /** Setting the content for the alert dialog */
        builder.setMessage("Please, be kind with the environment and remind to recycle. \n" + 
        "Press the bullseye notification to find your closest recycling point.");
        
        builder.setIcon(R.drawable.icon_loop_small_small_lab);
        
 
        /** Defining an OK button event listener */
        builder.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /** Exit application on click OK */
            	Log.d("Alarm", "OnClick");
            	Log.d("Alarm", getActivity().getClass().getName());
                getActivity().finish();
            }
        });
 
        /** Creating the alert dialog window */
        return builder.create();
    }
 
    /** The application should be exit, if the user presses the back button */
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().finish();
    }
}