package uk.ac.lincoln.lisc.recycling;

import org.altbeacon.beaconreference.R;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class Litter extends Activity{
	
	protected static final String TAG = "Litter";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreateRanging");
		super.onCreate(savedInstanceState);
		
		ImageView image = new ImageView(getApplicationContext());
	
		image.setImageResource(R.id.youwin);
	}
}
