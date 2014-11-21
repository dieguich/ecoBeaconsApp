package uk.ac.lincoln.lisc.ecobeacons;

import java.util.Collection;
import java.util.HashMap;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconData;
import org.altbeacon.beacon.BeaconDataNotifier;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.client.DataProviderException;
import org.altbeacon.beaconreference.R;

import uk.ac.lincoln.lisc.ecobeacons.HeadingsFragment.ListSelectionListener;
import uk.ac.lincoln.lisc.vending.VendingActivity;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * 
 * @author dieguich
 */
public class MainActivity extends FragmentActivity implements BeaconConsumer,
		RangeNotifier, ListSelectionListener{

	protected static final String TAG     = "RangingActivityMod";
	private static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;
	
	public static String[] mTipHeadingsArray;
	public static String[] mTipsDescArray;

	private static FragmentManager mFragmentManager;
	private static FrameLayout     mHeadingsFrameLayout, mTipsFrameLayout;
	
	private BeaconManager mBeaconManager;
	private int mCurrentTimesInRange;
	
	private final String mVendingRegion      = "Vending";
	private final int mVendingMajorID        = 111;
	private final double mDistanceToVending  = 2.0;
	private Boolean mIsFirstTimeInLoop       = true;
	private Boolean mIsNotificationTriggered = false;
	
	private final static TipsFragment mTipsFragment    = new TipsFragment();
	private HashMap<Beacon, Integer> mMyBeaconsInLoop = new HashMap<Beacon, Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreateMainActivity");
		super.onCreate(savedInstanceState);
		
		mBeaconManager = BeaconManager.getInstanceForApplication(this);
		mBeaconManager.getBeaconParsers().add(new BeaconParser()
						.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
		setContentView(R.layout.close_the_loop);
		verifyBluetooth();
		
		mTipHeadingsArray = getResources().getStringArray(R.array.TipNumber);
		mTipsDescArray    = getResources().getStringArray(R.array.Tips);
		
		// Get a reference to the FragmentManager
		mFragmentManager     = getFragmentManager();
		
		mFragmentManager
			.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
				public void onBackStackChanged() {
					Log.i(TAG, "BackStackChanged");
					setLayout();
				}
		});
		
		mHeadingsFrameLayout = (FrameLayout) findViewById(R.id.fragment_container);
		mTipsFrameLayout	 = (FrameLayout) findViewById(R.id.tips_fragment_container);
		
		final ActionBar tabBar = getActionBar();
		tabBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		if (savedInstanceState == null) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.fragment_container, new PlaceholderFragment());
			fragmentTransaction.addToBackStack(null);
			
			// Commit the FragmentTransaction
			fragmentTransaction.commit();
			
			// Force Android to execute the committed FragmentTransaction
			mFragmentManager.executePendingTransactions();
		}
		
		tabBar.addTab(tabBar.newTab().setText("Home").setTabListener(new TabListener(new HomeFragment())), true);
		tabBar.addTab(tabBar.newTab().setText("Why reclycling?").setTabListener(new TabListener(new HeadingsFragment())));
		mBeaconManager.bind(this);
	}
	
	/**
	 *  This class handles user interaction with the tabs
	 * @author dieguich
	 *
	 */
	public static class TabListener implements ActionBar.TabListener {
		
		private final Fragment miFragment;


		public TabListener(Fragment fragment) {
			miFragment = fragment;
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		// When a tab is selected, change the currently visible Fragment
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {

			if (miFragment != null) {
				ft.replace(R.id.fragment_container, miFragment);
				
				if(tab.getPosition() == 1) {
					mTipsFrameLayout.setVisibility(View.VISIBLE);
					if (mTipsFragment.isAdded()) {
						// Make the TitleLayout take 1/3 of the layout's width
						mHeadingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
								MATCH_PARENT, 1f));
						mHeadingsFrameLayout.setBackgroundColor(Color.parseColor("#2e332e"));
						
						// Make the QuoteLayout take 2/3's of the layout's width
						mTipsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
								MATCH_PARENT, 3f));
					}
				}
				//ft.replace(R.id.fragment_container, mFragment);
			}
		}

		// When a tab is unselected, remove the currently visible Fragment
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {

			if (null != miFragment) {
				ft.remove(miFragment);
				if(tab.getPosition() == 1) {
					mTipsFrameLayout.setVisibility(View.INVISIBLE);
					mHeadingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
							MATCH_PARENT, MATCH_PARENT));
					mHeadingsFrameLayout.setBackgroundColor(Color.WHITE);
					
				}
				
			}
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	private void setLayout() {
		
		
		// Determine whether the QuoteFragment has been added
		if (!mTipsFragment.isAdded()) {
			// Make the TitleFragment occupy the entire layout 
			mHeadingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
					MATCH_PARENT, MATCH_PARENT));
			mTipsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
					MATCH_PARENT));
		} else {
			// Make the TitleLayout take 1/3 of the layout's width
			mHeadingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
					MATCH_PARENT, 1f));
			mHeadingsFrameLayout.setBackgroundColor(Color.parseColor("#2e332e"));
			
			// Make the QuoteLayout take 2/3's of the layout's width
			mTipsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
					MATCH_PARENT, 3f));
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResumeMainActivity");
		super.onResume();
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//EcoBeaconsApplication.appResumed();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPauseMainActivity");
		super.onPause();
		//EcoBeaconsApplication.appPaused();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroyMainActivity");
		super.onDestroy();
		mBeaconManager.unbind(this);
	}

	@Override
	public void onBeaconServiceConnect() {
		Log.d(TAG, "onBeaconServiceMainActivity");
		mBeaconManager.setRangeNotifier(this);
		try {
			mBeaconManager.startRangingBeaconsInRegion(EcoBeaconsApplication.getRegion(mVendingRegion));
		} catch (RemoteException e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
		//Log.d(TAG, String.valueOf(beacons.size()));

		if (beacons.size() > 0) {
			for (Beacon myBeacon : beacons) {
			
				double lDistanceFromBeacon = getEasiDistance(myBeacon.getTxPower(), myBeacon.getRssi()) ;
				
				if(myBeacon.getId2().toInt() == mVendingMajorID && lDistanceFromBeacon < mDistanceToVending) { 
					if(mIsFirstTimeInLoop) {
						String lCurrentActivity = EcoBeaconsApplication.getCurrentActivity(); 
						if(lCurrentActivity != null && lCurrentActivity.contains("Vending")) {
							try {
								Log.d(TAG, "Stop Ranging");
								mBeaconManager.stopRangingBeaconsInRegion(EcoBeaconsApplication.getRegion(mVendingRegion));
								mBeaconManager.unbind(this);
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}else{
							Log.d(TAG, "FIRST TIME in LOOP");
							mIsFirstTimeInLoop = !mIsFirstTimeInLoop;
						}
					}
					else {
						if(!mMyBeaconsInLoop.containsKey(myBeacon)) {
							mMyBeaconsInLoop.put(myBeacon, 1);
							mCurrentTimesInRange = 1;
							Log.d(TAG, "FIRST TIME Beacon: " + myBeacon.getId3());
						}
						else {
							int lCurrentTimesInLoopBeacon = mMyBeaconsInLoop.get(myBeacon);
							lCurrentTimesInLoopBeacon++;
							mCurrentTimesInRange++;
							Log.d(TAG, "Distance: " + lDistanceFromBeacon + " ID: " + myBeacon.getId2().toInt());
							String lToToast = "Beacon: (" + myBeacon.getId2() + ", " + myBeacon.getId3() + ")" + ". Distance: " + 
									getEasiDistance(myBeacon.getTxPower(), myBeacon.getRssi()) + "m  far";
							Log.d(TAG, lToToast);
							toast(lToToast);
							if (mCurrentTimesInRange == 2 && EcoBeaconsApplication.getRangingMode() == 1) {
								EcoBeaconsApplication.setRangingMode(2);
								//EcoBeaconsApplication.setNearMode();
							}
							if(mCurrentTimesInRange == 3) {
								if(!mIsNotificationTriggered) {
									createNotification();
								}
								mIsFirstTimeInLoop = true;
								mMyBeaconsInLoop.clear();
								mCurrentTimesInRange = 0;
								//EcoBeaconsApplication.setRealBackgroundMode();
								break;
							}
							mMyBeaconsInLoop.put(myBeacon, lCurrentTimesInLoopBeacon);
						}
					}			
				}
			}
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
	 * Toast for debugging mode.
	 * @param stringToToast
	 */
	public void toast(final String stringToToast) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
				mainHandler.post(new Runnable() {
					
					@Override
					public void run() {
						Toast toast = Toast.makeText(getApplicationContext(), stringToToast, Toast.LENGTH_SHORT);
						toast.show();
						
					}
				});
			}
		}).start();
	}

	private void createNotification() {
		mIsNotificationTriggered = true;
		Log.d(TAG, "CreateNotification");
		Notification.Builder lBuilder = new Notification.Builder(this);
		getBigTextStyle(lBuilder);
		
		Intent lVendingIntent = new Intent(this, VendingActivity.class);
		Intent lHomeIntent    = new Intent();
		lHomeIntent.setAction("ecobeacons.NO_SELECTION");
		lHomeIntent.putExtra("Notification", 2);
		PendingIntent homePendingIntent = PendingIntent.getBroadcast(this, 12345, 
				lHomeIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		// The stack builder object will contain an artificial
		// back stack for the
		// started Activity.
		// This ensures that navigating backward from the
		// Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the
		// Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top
		// of the stack
		stackBuilder.addNextIntent(lVendingIntent);
		// stackBuilder.addNextIntent(startMain);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
						PendingIntent.FLAG_UPDATE_CURRENT);

		lBuilder.addAction(android.R.drawable.checkbox_on_background,
				"Yes", resultPendingIntent);
		lBuilder.addAction(android.R.drawable.ic_notification_clear_all,
				"No", homePendingIntent);
		// builder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) 
				getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(2, lBuilder.build());
	}

	/**
	 * 
	 * @param builder
	 * @return Notification when an iBeacon is in the Immediate field
	 */
	private Notification getBigTextStyle(Notification.Builder builder) {

		long[] lPattern   = new long[]{1000,500,1000, 500};
		Uri lDefaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		builder.setContentTitle("Reduced BigText title")
				.setContentText("Reduced content").setContentInfo("Info")
				.setSmallIcon(R.drawable.icon_loop_small_small)
				.setVibrate(lPattern)
				.setLights(Color.BLUE, 1, 0)
				.setSound(lDefaultSound)
				.setAutoCancel(false)
				.setOngoing(true);
				

		return new Notification.BigTextStyle(builder)
			.bigText(getString(R.string.vending_notificatio_extra))
			.setBigContentTitle(getString(R.string.vending_notificatio_tittle))
			.setSummaryText(getString(R.string.app_name)).build();
	}

	/**
	 * Verify if the phone supports Bluetooth LE
	 */
	private void verifyBluetooth() {

		try {
			if (!BeaconManager.getInstanceForApplication(this)
					.checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						this);
				builder.setTitle("Bluetooth not enabled");
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
						System.exit(0);
					}
				});
				builder.show();
			}
		} catch (RuntimeException e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
					System.exit(0);
				}

			});
			builder.show();

		}
	}

	@Override
	public void onListSelection(int index) {
	
		
		if(!mTipsFragment.isAdded()) {		
	
			// Start a new FragmentTransaction
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			
			fragmentTransaction.add(R.id.tips_fragment_container, mTipsFragment);
			// Add this FragmentTransaction to the backstack
			fragmentTransaction.addToBackStack("Frag2");
			
			// Commit the FragmentTransaction
			fragmentTransaction.commit();
			
			// Force Android to execute the committed FragmentTransaction
			mFragmentManager.executePendingTransactions();

		}
		if (mTipsFragment.getShownIndex() != index) {
			// Tell the QuoteFragment to show the quote string at position index
			mTipsFragment.showQuoteAtIndex(index);
		}
		
	}
}
