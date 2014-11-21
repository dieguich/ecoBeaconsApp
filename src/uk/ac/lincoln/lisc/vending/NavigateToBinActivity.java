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

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.os.Vibrator;
import android.print.PrintAttributes.Margins;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ClickableViewAccessibility")
public class NavigateToBinActivity extends Activity implements BeaconConsumer{
	
	protected static final String TAG              = "NavigateToBinActivity";
	private static final String mRecyclingRegName  = "Recycling";
	private static final String mVendingRegName    = "Vending";
	
	private static final int NOTIFICATION_ID = 52;
	private final int      mRecyclingMajorID = 999;
	
	private BeaconManager mBeaconManager;
	private ImageView     mImageView;
	private Bitmap        mBitmapRound;
	private LinearLayout  mLinLayout;
	private TextView      mTextView;
	private static int mIdValue    = -1;
	private int mBullseyeNotiIcon  = 0;
	private String mBinColour      = "";
	private String mStuffBought    = "";
	private int mCounts            = 0;
	private Boolean mIsReadyToSCAN =  false;
	private Beacon mMyBinBeacon;
	private int mWaitCounter       = 0;
	
	private AlarmManager mAlarmManager;
	private static final long INITIAL_ALARM_DELAY = 120 * 1000L;
	private PendingIntent mAlarmPendingIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "OnCreate");

		// Get the Intent used to start this Activity
		Intent intent = getIntent();

		// Make a new ImageView
		mImageView = new ImageView(getApplicationContext());
		
		mBeaconManager = BeaconManager.getInstanceForApplication(this);
		mBeaconManager.getBeaconParsers()
				.add(new BeaconParser()
				.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
			
		if(intent.getIntExtra("Noti", -1) != -1) {
			mIsReadyToSCAN = true;
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(NOTIFICATION_ID);
			int lImageID = intent.getIntExtra("NotiID", -1);
			if(lImageID != -1) {
				mIdValue = lImageID;
				Log.d(TAG,  String.valueOf(mIdValue));
			}
			mBitmapRound = getRoundedCornerBitmap(getResources().getDrawable(R.drawable.concentric), true);
			mImageView.setImageBitmap(mBitmapRound);
			mImageView.setImageAlpha(150);
			mImageView.setImageResource(R.drawable.blink_frame);
			BitmapDrawable bitDraw = new BitmapDrawable(getResources(), 
					BitmapFactory.decodeResource(getResources(), getBinColor(mIdValue)));
			mImageView.setBackground(bitDraw);
			AnimationDrawable frameAnimation = (AnimationDrawable) mImageView.getDrawable();
			frameAnimation.start();
			setContentView(mImageView);  
		}
		else { //TODO: Something that onDestroy can check if mIdValue > 3
			
			mIdValue    = intent.getIntExtra(VendingActivity.EXTRA_RES_ID, 0);
			
			mLinLayout = new LinearLayout(getApplicationContext());
			LayoutParams lpView    = new LayoutParams(LayoutParams.WRAP_CONTENT, 
					LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams centerGravityParams = new LinearLayout.LayoutParams(
	                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			centerGravityParams.gravity   = Gravity.CENTER_HORIZONTAL;
			centerGravityParams.topMargin = 200;
			mLinLayout.setOrientation(LinearLayout.VERTICAL);
			
			TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
			mBitmapRound    = getRoundedCornerBitmap(imgs.getDrawable(mIdValue), false);
			imgs.recycle();
			
			mImageView.setImageBitmap(mBitmapRound);
			mImageView.setImageAlpha(140);
			mImageView.setLayoutParams(lpView);
			BitmapDrawable bitDraw = new BitmapDrawable(getResources(), 
					BitmapFactory.decodeResource(getResources(), getBinColor(mIdValue)));
			mLinLayout.setBackground(bitDraw);
			mLinLayout.addView(mImageView, centerGravityParams);
			if(mIdValue < 4) {
				mTextView = new TextView(getApplicationContext());
				Typeface font = Typeface.createFromAsset(getBaseContext().getAssets(),
						"fonts/Maximum.ttf");
				mTextView.setText("This is the bin you should look for to dispose your " + mStuffBought +
						"\n" + "This App will guide you to the closest recycling point.");
				mTextView.setTypeface(font, Typeface.NORMAL);
				mTextView.setTextSize(25);
				mTextView.setTextColor(Color.BLACK);
				centerGravityParams.topMargin = 70;
				mLinLayout.addView(mTextView, centerGravityParams);
				new CountDownTimer(4000, 2000) {
					
					@Override
					public void onTick(long millisUntilFinished) {
						// TODO Auto-generated method stub
					}
					
					@Override
					public void onFinish() {
						if(mStuffBought.equals("WEDGE")) {
							mStuffBought = "SANDWICH";
						}
						mTextView.setText(getString(R.string.recycling_text_touch) 
								+ mStuffBought + "!");
						toastToUI(getString(R.string.recycling_toast_touch) +
								mStuffBought, Toast.LENGTH_SHORT);
						toastToUI(getString(R.string.recycling_toast_touch) +
								mStuffBought, Toast.LENGTH_LONG);
					}
				}.start();
				
			}
			mLinLayout.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int action = MotionEventCompat.getActionMasked(event);
					Log.d(TAG, "Touch detected: " + action);
					Log.d(TAG, "Action UP: " + MotionEvent.ACTION_UP);
					if (action == MotionEvent.ACTION_UP) {
						//NavigateToBinActivity.this.finish();
						Intent startMain = new Intent(Intent.ACTION_MAIN);
						startMain.addCategory(Intent.CATEGORY_HOME);
						startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getApplicationContext().startActivity(startMain);
					}
					return true;
				}
			});
			setContentView(mLinLayout);
		}
		mBeaconManager.bind(this);
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
			mStuffBought      = "CAN";
			mBinColour        = "YELLOW";
			break;
		case 1:
			lBinToDisplay     = R.drawable.blue_bin;
			mBullseyeNotiIcon = R.drawable.bullseye_icon_blue;
			mBinColour        = "BLUE";
			mStuffBought      = "COFFEE";
			break;
		case 2:
			lBinToDisplay     = R.drawable.red_bin;
			mBullseyeNotiIcon = R.drawable.bullseye_icon_red;
			mBinColour        = "RED";
			mStuffBought      = "WEDGE";
			break;
		case 3:
			lBinToDisplay     = R.drawable.red_bin;
			mBullseyeNotiIcon = R.drawable.bullseye_icon_red;
			mBinColour        = "RED";
			mStuffBought      = "POP";
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
		super.onPause();
		
		if(mIdValue < 4 && !mIsReadyToSCAN) {
			Log.d(TAG, "Alarm started");
			mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			Intent lAlarmIntent = new Intent("uk.ac.lincoln.lisc.vending.demoactivity");
			mAlarmPendingIntent = PendingIntent.getActivity(getBaseContext(), 0, 
					lAlarmIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
			
            mAlarmManager.set(AlarmManager.RTC_WAKEUP  , System.currentTimeMillis() + INITIAL_ALARM_DELAY , 
            		mAlarmPendingIntent);
		}
		
		mIsReadyToSCAN =  true;
		
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
	
	/**
	 * Create bullseye Notification 
	 * @param builder
	 * @return
	 */
	private Notification getBigTextStyle(Notification.Builder builder) {
		builder.setSmallIcon(mBullseyeNotiIcon)
		//.setVibrate(pattern)
		.setLights(Color.BLUE, 1000, 1000)
		//.setSound(defaultSound)
		.setAutoCancel(true) //TODO: Change False
		.setOngoing(true);
		
		return new Notification.BigTextStyle(builder)
		.bigText(getString(R.string.recycling_notificatio_extra) + mBinColour + " bin")
		.setBigContentTitle(getString(R.string.recycling_notificatio_tittle))
		.setSummaryText(getString(R.string.app_name)).build();
	}


	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		try {
			//beaconManager.stopRangingBeaconsInRegion(EcoBeaconsApplication.getRegion("Vending"));
			if(mAlarmManager != null && mAlarmPendingIntent != null) {
				Log.d("Alarm", "DestroyAlarm");
				mAlarmManager.cancel(mAlarmPendingIntent);
			}
			mBeaconManager.unbind(this);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	@Override
	public void onBeaconServiceConnect() {
		Log.d(TAG, "onBeaconService of ActivityNavigateToBIN");
		mBeaconManager.setRangeNotifier(new RangeNotifier() {

			@Override
			public void didRangeBeaconsInRegion(Collection<Beacon> beacons,
					Region region) {
				Log.d(TAG, "Beacons detected: " + String.valueOf(beacons.size()));
				mWaitCounter++;
				if(mBullseyeNotiIcon != 0 && mIsReadyToSCAN && beacons.size() > 0 && mWaitCounter > 4) {
					Beacon lAuxBeacon;
					double lClosestDistance = 0.0;
					for (Beacon myBeacon : beacons) {
						lAuxBeacon          = myBeacon;
						//double lAuxDistance = lAuxBeacon.getDistance();
						double lAuxDistance = getEasiDistance(lAuxBeacon.getTxPower(), lAuxBeacon.getRssi());
						if(lClosestDistance == 0) {
							lClosestDistance = lAuxDistance;
							mMyBinBeacon     = lAuxBeacon;
						}
						else if(lAuxDistance < lClosestDistance){
							lClosestDistance = lAuxDistance;
							mMyBinBeacon     = lAuxBeacon;
						}
					}
						
					String lDistance = getString(R.string.recycling_toast_distance_1)
							+ round(lClosestDistance, 2)
							+ getString(R.string.recycling_toast_distance_2);
					if(EcoBeaconsApplication.getCurrentActivity() != null) {
						toastToUI(lDistance, Toast.LENGTH_LONG);
					}
					setVibrationPattern(lClosestDistance);
					
					if (mMyBinBeacon.getId2().toInt() == mRecyclingMajorID
							&& lClosestDistance < 1) {
						mCounts++;
						Log.d(TAG, "mCounts: " + mCounts);
						if(mCounts == 2 ) { //&& !EcoBeaconsApplication.getCurrentActivity().contains("Litter")
							Intent lLitterIntent = new Intent(getApplicationContext(), LitterActivity.class);
							lLitterIntent.putExtra("bullseye", NOTIFICATION_ID);
							lLitterIntent.putExtra("product_disposed", mStuffBought);
							startActivity(lLitterIntent);
							try {
								//mBeaconManager.stopMonitoringBeaconsInRegion(EcoBeaconsApplication.getRegion(mRecyclingRegName));
								mBeaconManager.stopRangingBeaconsInRegion(EcoBeaconsApplication.getRegion(mRecyclingRegName));
								mBeaconManager.unbind(NavigateToBinActivity.this);
							} catch (Exception e) {
								Log.e(TAG, e.getMessage());
							}
						}
					}
				}
			}
			/**
			 * Set vibration pattern when close to a recycling Bin
			 * @param closestDistance
			 */
			private void setVibrationPattern(double closestDistance) {
				Vibrator lVibratorPattern;
				lVibratorPattern = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
				long[] lPattern; 
				if(closestDistance < 4.0 ) {
					
					if(closestDistance > 3) {
						EcoBeaconsApplication.setNearMode();
						lPattern = new long[] {0, 500, 100};
					}
					else if(closestDistance >= 2) {
						
						lPattern = new long[] {0, 400, 300, 400, 100};
					}
					else if (closestDistance >= 1){
						lPattern = new long[] {0, 400, 300, 400, 300, 100};
					}
					else {
						lPattern = new long[] {0, 200, 200, 200, 200, 200, 200, 200, 100};
					}
					lVibratorPattern.vibrate(lPattern, -1);
				}
			}

			/**
			 * Round the distance calculated by the method getEasidistance
			 * @param value
			 * @param places
			 * @return
			 */
			public double round(double value, int places) {
				if (places < 0)
					throw new IllegalArgumentException();

				BigDecimal bd = new BigDecimal(value);
				bd = bd.setScale(places, RoundingMode.HALF_UP);
				return bd.doubleValue();
			}

		});

		try {
			mBeaconManager.stopRangingBeaconsInRegion(EcoBeaconsApplication.getRegion(mVendingRegName));
			mBeaconManager.startRangingBeaconsInRegion(EcoBeaconsApplication.getRegion(mRecyclingRegName));

		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	/**
	 * Roughly estimates the distance to the iBeacon
	 * Calculation obtained from http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing
	 *  
	 * @param txPower RSSI of the iBeacon at 1 meter
	 * @param rssi measured RSSI by the user device
	 * @return
	 */
	protected double getEasiDistance(int txPower, double rssi) {
		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}

		double ratio = rssi*1.0/txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio,10);
		}
		else {
			double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;    
			return accuracy;
		}
	} 
	
	/**
	 * Toast to the user interface information related with the distance form your current point to the closest 
	 * recycling point
	 * @param text
	 * @param lenght
	 */
	public void toastToUI(final String text, final int lenght) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				mImageView.post(new Runnable() {
					
					@Override
					public void run() {
						Toast toast = Toast.makeText(getApplicationContext(), text, lenght);
						toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0);
						toast.show();
						
					}
				});
			}
		}).start();
	}

	/**
	 * Round the images presented in the screen
	 * @param drawable
	 * @param square
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap( Drawable drawable, boolean square) {
	     int width = 0;
	     int height = 0;
	     
	     Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap() ;
	     
	     if(square){
	      if(bitmap.getWidth() < bitmap.getHeight()){
	       width  = bitmap.getWidth();
	       height = bitmap.getWidth();
	      } else {
	    	  width  = bitmap.getHeight();
	          height = bitmap.getHeight();
	      }
	     } else {
	      height = bitmap.getHeight();
	      width  = bitmap.getWidth();
	     }
	     
	        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
	        Canvas canvas = new Canvas(output);

	        final int color   = 0xff424242;
	        final Paint paint = new Paint();
	        final Rect rect   = new Rect(0, 0, width, height);
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