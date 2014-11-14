package uk.ac.lincoln.lisc.vending;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beaconreference.R;


import uk.ac.lincoln.lisc.ecobeacons.EcoBeaconsApplication;
import uk.ac.lincoln.lisc.recycling.LitterActivity;

import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class NavigateToBinActivity extends Activity implements BeaconConsumer{
	
	protected static final String TAG = "NavigateToBinActivity";
	private static final int NOTIFICATION_ID = 52;
	
	private BeaconManager beaconManager;
	private ImageView imageView;
	private static int mIdValue   = -1;
	private int mBullseyeNotiIcon = 0;
	private String mBinColour     = "";
	private int mCounts           = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "ImageViewActivity");

		// Get the Intent used to start this Activity
		Intent intent = getIntent();

		// Make a new ImageView
		imageView = new ImageView(getApplicationContext());

		beaconManager = BeaconManager.getInstanceForApplication(this);
		beaconManager
				.getBeaconParsers()
				.add(new BeaconParser()
						.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
		
		TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
		Bitmap bitmapRound;
		if(intent.getIntExtra("Noti", -1) != -1) {
			Log.d(TAG,  "NotificationBack");
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(NOTIFICATION_ID);
			int lImageID = intent.getIntExtra("NotiID", -1);
			if(lImageID != -1) {
				mIdValue = lImageID;
				Log.d(TAG,  String.valueOf(mIdValue));
			}
			bitmapRound = getRoundedCornerBitmap(getResources().getDrawable(R.drawable.concentric), true);
			imageView.setImageBitmap(bitmapRound);
			imageView.setImageAlpha(150);
			imageView.setImageResource(R.drawable.blink_frame);
			BitmapDrawable bitDraw = new BitmapDrawable(getResources(), 
					BitmapFactory.decodeResource(getResources(), getBinColor(mIdValue)));
			imageView.setBackground(bitDraw);
			AnimationDrawable frameAnimation = (AnimationDrawable) imageView.getDrawable();
			frameAnimation.start();
			setContentView(imageView);
			  
		}
		else {
			mIdValue = intent.getIntExtra(VendingActivity.EXTRA_RES_ID, 0);
			bitmapRound = getRoundedCornerBitmap(imgs.getDrawable(mIdValue), false);
			imageView.setImageBitmap(bitmapRound);
			imageView.setImageAlpha(150);
			BitmapDrawable bitDraw = new BitmapDrawable(getResources(), 
					BitmapFactory.decodeResource(getResources(), getBinColor(mIdValue)));
			imageView.setBackground(bitDraw);
			setContentView(imageView);
		}
		beaconManager.bind(this);
	}
	

	/**
	 * Get The bin colour where the user should throw the stuff bought.
	 * @param imageSelected
	 * @return The reference of the image to be displayed in background
	 */
	private int getBinColor(int imageSelected) {
		int lBinToDisplay = 0;
		
		switch (imageSelected) {
		case 0:
			lBinToDisplay     = R.drawable.yellow_bin;
			mBullseyeNotiIcon = R.drawable.bullseye_icon_yellow;
			mBinColour        = "YELLOW";
			break;
		case 1:
			lBinToDisplay     = R.drawable.blue_bin;
			mBullseyeNotiIcon = R.drawable.bullseye_icon_blue;
			mBinColour        = "BLUE";
			break;
		case 2:
			lBinToDisplay     = R.drawable.red_bin;
			mBullseyeNotiIcon = R.drawable.bullseye_icon_red;
			mBinColour        = "RED";
			break;
		case 3:
			lBinToDisplay     = R.drawable.red_bin;
			mBullseyeNotiIcon = R.drawable.bullseye_icon_red;
			mBinColour        = "RED";
			break;
		case 4:
			lBinToDisplay     = R.drawable.no_recyclable;
			mBullseyeNotiIcon = 0;
			break;
		case 5:
			lBinToDisplay     = R.drawable.no_recyclable;
			mBullseyeNotiIcon = 0;
			break;
		default:
			break;
		}
		return lBinToDisplay;
	}
	

	@Override
	public void onPause() {
		Log.d(TAG, "OnPause Activity");
		Log.d(TAG, "Value of Image: " + String.valueOf(mIdValue));
		super.onPause();
		if(mBullseyeNotiIcon != 0) {
			final Intent notificationIntent = new Intent(getApplicationContext(),
					NavigateToBinActivity.class);
			notificationIntent.putExtra("NotiId", mIdValue);
			notificationIntent.putExtra("Noti", NOTIFICATION_ID);
			
			final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			
			Notification.Builder lBuilder = new Notification.Builder(this);
			getBigTextStyle(lBuilder);
			NotificationManager lNotificationManager = 
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			lNotificationManager.notify(NOTIFICATION_ID, lBuilder.setContentIntent(pendingIntent).build());
		}
		
	}
	
	
	private Notification getBigTextStyle(Notification.Builder builder) {
		builder.setSmallIcon(mBullseyeNotiIcon)
		//.setVibrate(pattern)
		.setLights(Color.BLUE, 1, 0)
		//.setSound(defaultSound)
		.setAutoCancel(false)
		.setOngoing(true);
		
		return new Notification.BigTextStyle(builder)
		.bigText("Click here to see how far you are to the closest " + mBinColour + " bin")
		.setBigContentTitle("Recycling Bin radar")
		.setSummaryText("Close the loop!").build();
	}


	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroyGrid");
		super.onDestroy();
		try {
			beaconManager.stopRangingBeaconsInRegion(EcoBeaconsApplication.getRegion("Vending"));
			beaconManager.unbind(this);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	@Override
	public void onBeaconServiceConnect() {
		Log.d(TAG, "onBeaconService");
		beaconManager.setRangeNotifier(new RangeNotifier() {

			@Override
			public void didRangeBeaconsInRegion(Collection<Beacon> beacons,
					Region region) {
				Log.d(TAG, String.valueOf(beacons.size()));
				if(mBullseyeNotiIcon != 0) {
					if (beacons.iterator().hasNext()) {
						Beacon lMyBeacon = beacons.iterator().next();
						Log.d(TAG, String.valueOf(lMyBeacon.getId2()));
						String ldistance = "You are "
								+ round(lMyBeacon.getDistance(), 2)
								+ "m far to the closest litter";
						toastDistance(ldistance);
						if (lMyBeacon.getId2().toInt() == 70
								&& lMyBeacon.getDistance() < 2) {
							mCounts++;
							Log.d(TAG, "mCounts: " + mCounts);
							if(mCounts > 1 && !EcoBeaconsApplication.getCurrentActivity().contains("Litter")) {
								Log.d(TAG, "Start Litter Intent");
								mCounts = 0;
								Intent lLitterIntent = new Intent(getApplicationContext(), LitterActivity.class);
								lLitterIntent.putExtra("bullseye", NOTIFICATION_ID);
								startActivity(lLitterIntent);
							}
						}
					}
				}
			}

			public double round(double value, int places) {
				if (places < 0)
					throw new IllegalArgumentException();

				BigDecimal bd = new BigDecimal(value);
				bd = bd.setScale(places, RoundingMode.HALF_UP);
				return bd.doubleValue();
			}

		});

		try {
			beaconManager.startRangingBeaconsInRegion(EcoBeaconsApplication.getRegion("Vending"));

		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	public void toastDistance(final String ldistance) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				imageView.post(new Runnable() {
					
					@Override
					public void run() {
						Toast toast = Toast.makeText(getApplicationContext(), ldistance, Toast.LENGTH_SHORT);
						toast.show();
						
					}
				});
			}
		}).start();
	}

	public static Bitmap getRoundedCornerBitmap( Drawable drawable, boolean square) {
	     int width = 0;
	     int height = 0;
	     
	     Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap() ;
	     
	     if(square){
	      if(bitmap.getWidth() < bitmap.getHeight()){
	       width = bitmap.getWidth();
	       height = bitmap.getWidth();
	      } else {
	       width = bitmap.getHeight();
	          height = bitmap.getHeight();
	      }
	     } else {
	      height = bitmap.getHeight();
	      width = bitmap.getWidth();
	     }
	     
	        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
	        Canvas canvas = new Canvas(output);

	        final int color = 0xff424242;
	        final Paint paint = new Paint();
	        final Rect rect = new Rect(0, 0, width, height);
	        final RectF rectF = new RectF(rect);
	        final float roundPx = 90; 

	        paint.setAntiAlias(true);
	        canvas.drawARGB(0, 0, 0, 0);
	        paint.setColor(color);
	        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

	        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	        canvas.drawBitmap(bitmap, rect, rect, paint);

	        return output;
	}
	
}