package uk.ac.lincoln.lisc.recycling;

import java.util.Random;

import org.altbeacon.beaconreference.R;

import uk.ac.lincoln.lisc.ecobeacons.EcoBeaconsApplication;
import uk.ac.lincoln.lisc.vending.NavigateToBinActivity;
import uk.ac.lincoln.lisc.vending.VendingActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class LitterActivity extends Activity{
	
	private ImageView mImageLitter;
	protected static final String TAG = "LitterActivity";
	private String mStuffDisposed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreateRanging");
		super.onCreate(savedInstanceState);
		Intent lMyIntent = getIntent();
		int lNotiToDissmiss = lMyIntent.getIntExtra("bullseye", -1);
		if(lNotiToDissmiss != -1) {
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(lNotiToDissmiss);
			mStuffDisposed = lMyIntent.getStringExtra("product_disposed");
		}
		
		setContentView(R.layout.litter_bg);
		mImageLitter = (ImageView) findViewById(R.id.youwin);
		int lRandomNum = randInt(0, 9);
		int lImageResource = getResources().getIdentifier("drawable/pic_"+lRandomNum, null, getPackageName());
		mImageLitter.setBackgroundResource(lImageResource);
		new CountDownTimer(500, 250) {
			
			@Override
			public void onTick(long millisUntilFinished) {
		
				
			}
			
			@Override
			public void onFinish() {
				AlertDialog alertDialog = new AlertDialog.Builder(LitterActivity.this).create();
				Typeface font = Typeface.createFromAsset(getBaseContext().getAssets(),
						"fonts/Maximum.ttf");
				TextView lTextView = new TextView(getApplicationContext());
				lTextView.setText("You closed the loop by disposing of your " + mStuffDisposed + "!!");
				lTextView.setTypeface(font, Typeface.NORMAL);
				lTextView.setTextSize(25);
				lTextView.setTextColor(Color.parseColor("#7FBA00"));
				alertDialog.setCustomTitle(lTextView);
				alertDialog.setMessage("Well done! Your support is appreciated!"); //TODO: Account your contribution
				alertDialog.setIcon(R.drawable.icon_loop_small_small_small_lab);
				alertDialog.setCanceledOnTouchOutside(true);
				alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						//LitterActivity.this.finish();
						
					}
				});
				alertDialog.show();
			}
		}.start();
		
	}
	
	/**
	 * Returns a pseudo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimum value
	 * @param max Maximum value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}
}
