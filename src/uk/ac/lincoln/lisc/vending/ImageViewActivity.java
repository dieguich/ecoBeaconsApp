package uk.ac.lincoln.lisc.vending;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class ImageViewActivity extends Activity {
	protected static final String TAG = "ImageViewActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "ImageViewActivity");

		// Get the Intent used to start this Activity
		Intent intent = getIntent();

		// Make a new ImageView
		ImageView imageView = new ImageView(getApplicationContext());

		// Get the ID of the image to display and set it as the image for this
		// ImageView
		imageView.setImageResource(intent.getIntExtra(
				GridLayoutActivity.EXTRA_RES_ID, 0));

		setContentView(imageView);
	}
}