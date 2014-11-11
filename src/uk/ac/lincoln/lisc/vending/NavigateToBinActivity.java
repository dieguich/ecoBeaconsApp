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
import uk.ac.lincoln.lisc.recycling.Litter;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class NavigateToBinActivity extends Activity implements BeaconConsumer{
	
	protected static final String TAG = "ImageViewActivity";
	private static final int NOTIFICATION_ID = 52;
	
	private BeaconManager beaconManager;
	private ImageView imageView;
	private static int mIdValue;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "ImageViewActivity");

		// Get the Intent used to start this Activity
		Intent intent = getIntent();

		// Make a new ImageView
		imageView = new ImageView(getApplicationContext());

		// Get the ID of the image to display and set it as the image for this
		// ImageView
		Log.d(TAG, String.valueOf(intent.getIntExtra("Noti", -1)));
		if(intent.getIntExtra("Noti", -1) != -1) {
			Log.d(TAG,  "NotificationBack");
			Log.d(TAG,  String.valueOf(mIdValue));
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(52);
			imageView.setImageResource(intent.getIntExtra("NotiID", 0));
		}
		else {
			mIdValue = intent.getIntExtra(VendingActivity.EXTRA_RES_ID, 0);
			TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
			Bitmap bitmapRound = getRoundedCornerBitmap(imgs.getDrawable(mIdValue), false);
			
			imageView.setImageBitmap(bitmapRound);
			imageView.setImageAlpha(150);
			BitmapDrawable bitDraw = new BitmapDrawable(getResources(), 
					BitmapFactory.decodeResource(getResources(), R.drawable.blue_bin));
			imageView.setBackground(bitDraw);
		}
		

		setContentView(imageView);
		beaconManager = BeaconManager.getInstanceForApplication(this);
		beaconManager
				.getBeaconParsers()
				.add(new BeaconParser()
						.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
		beaconManager.bind(this);
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "OnPause");
		Log.d(TAG, String.valueOf(mIdValue));
		super.onPause();
		final Intent notificationIntent = new Intent(getApplicationContext(),
				NavigateToBinActivity.class);
		notificationIntent.putExtra("NotiId", mIdValue);
		notificationIntent.putExtra("Noti", 52);
		
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		final Notification notification = new Notification.Builder(
				getApplicationContext())
				.setSmallIcon(R.drawable.bullseye_icon)
				.setOngoing(true).setContentTitle("Litter searching")
				.setContentText("Click to see the color of your bin")
				.setContentIntent(pendingIntent).build();
		
		NotificationManager lNotificationManager = 
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		lNotificationManager.notify(NOTIFICATION_ID, notification);
		
	}
	
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroyGrid");
		super.onDestroy();
		try {
			beaconManager.stopRangingBeaconsInRegion(EcoBeaconsApplication.getRegion("Recycling"));
			beaconManager.unbind(this);
			//Intent startMain = new Intent(this, VendingActivity.class);
			//startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//startMain.putExtra("Notification", 2);
			//this.startActivity(startMain);
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
				if (beacons.iterator().hasNext()) {
					Beacon lMyBeacon = beacons.iterator().next();
					Log.d(TAG, String.valueOf(lMyBeacon.getId2()));
					String ldistance = "You are "
							+ round(lMyBeacon.getDistance(), 2)
							+ "m far to the closest litter";
					toastDistance(ldistance);
					if (lMyBeacon.getId2().toInt() == 29218
							&& lMyBeacon.getDistance() < 2) {
						Intent intent = new Intent(getApplicationContext(), Litter.class);
						startActivity(intent);
						
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
		/*runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "Runnable");
				
				//BeaconReferenceApplication variableName = (BeaconReferenceApplication) GridLayoutActivity.this
				//		.getApplication();
				//variableName.toast(ldistance);
				this.toast(ldistance);

			}
			private void toast(String message) {
				Toast.makeText(GridLayoutActivity.this.getBaseContext(), message,
						Toast.LENGTH_LONG).show();
			}
		});*/
	
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