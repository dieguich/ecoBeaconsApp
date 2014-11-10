package uk.ac.lincoln.lisc.ecobeacons;

import java.util.Collection;

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
import android.os.Bundle;
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
	private static FrameLayout mHeadingsFrameLayout, mTipsFrameLayout;
	
	private final static TipsFragment mTipsFragment = new TipsFragment();
	
	private BeaconManager beaconManager;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreateRanging");
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		int notificationId = intent.getIntExtra("Notification", -1);
		if (notificationId != -1) {
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(notificationId);
			EcoBeaconsApplication.setBackgroundMode();
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(startMain);
			finish();
		}
		beaconManager = BeaconManager.getInstanceForApplication(this);
		beaconManager
				.getBeaconParsers()
				.add(new BeaconParser()
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
			fragmentTransaction.addToBackStack("Frag1");
			
			// Commit the FragmentTransaction
			fragmentTransaction.commit();
			
			// Force Android to execute the committed FragmentTransaction
			mFragmentManager.executePendingTransactions();
		}
		
		tabBar.addTab(tabBar.newTab().setText("Home").setTabListener(new TabListener(new HomeFragment())), true);
		tabBar.addTab(tabBar.newTab().setText("Tips").setTabListener(new TabListener(new HeadingsFragment())));
		beaconManager.bind(this);
	}
	
	/**
	 *  This class handles user interaction with the tabs
	 * @author dieguich
	 *
	 */
	public static class TabListener implements ActionBar.TabListener {
		
		private final Fragment mFragment;


		public TabListener(Fragment fragment) {
			mFragment = fragment;
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		// When a tab is selected, change the currently visible Fragment
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Log.i(TAG, "onTabSelected called: " + tab.getPosition() + " " + tab.getTag());

			if (mFragment != null) {
				if(tab.getPosition() == 1) {
					mTipsFrameLayout.setVisibility(View.VISIBLE);
					if (mTipsFragment.isAdded()) {
						Log.i(TAG, "Already Added");
						// Make the TitleLayout take 1/3 of the layout's width
						mHeadingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
								MATCH_PARENT, 1f));
						mHeadingsFrameLayout.setBackgroundColor(Color.parseColor("#518c05"));
						
						// Make the QuoteLayout take 2/3's of the layout's width
						mTipsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
								MATCH_PARENT, 3f));
					}
				}
				
				ft.replace(R.id.fragment_container, mFragment);
			}
		}

		// When a tab is unselected, remove the currently visible Fragment
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			Log.i(TAG, "onTabUnselected called");

			if (null != mFragment) {
				
				if(tab.getPosition() == 1) {
					mTipsFrameLayout.setVisibility(View.INVISIBLE);
					mHeadingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
							MATCH_PARENT, MATCH_PARENT));
					mHeadingsFrameLayout.setBackgroundColor(Color.WHITE);
					
				}
				ft.remove(mFragment);
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
			Log.i(TAG, "Not Added");
			// Make the TitleFragment occupy the entire layout 
			mHeadingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
					MATCH_PARENT, MATCH_PARENT));
			mTipsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
					MATCH_PARENT));
		} else {
			Log.i(TAG, "Already Added");
			// Make the TitleLayout take 1/3 of the layout's width
			mHeadingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
					MATCH_PARENT, 1f));
			mHeadingsFrameLayout.setBackgroundColor(Color.parseColor("#518c05"));
			
			// Make the QuoteLayout take 2/3's of the layout's width
			mTipsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
					MATCH_PARENT, 3f));
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResumeRanging");
		super.onResume();
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		EcoBeaconsApplication.appResumed();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPauseRanging");
		super.onPause();
		EcoBeaconsApplication.appPaused();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroyRanging");
		super.onDestroy();
		beaconManager.unbind(this);
	}

	@Override
	public void onBeaconServiceConnect() {
		Log.d(TAG, "onBeaconService");
		beaconManager.setRangeNotifier(this);
		try {
			Log.d(TAG, "onBeaconService");
			beaconManager.startRangingBeaconsInRegion(EcoBeaconsApplication.getRegion("Vending"));
		} catch (RemoteException e) {
		}
	}

	@Override
	public void didRangeBeaconsInRegion(Collection<Beacon> beacons,
			Region region) {
		//Log.d(TAG, String.valueOf(beacons.size()));

		if (beacons.size() > 0) {
			for (Beacon myBeacon : beacons) {
				myBeacon.requestData(new BeaconDataNotifier() {

					@Override
					public void beaconDataUpdate(Beacon arg0, BeaconData arg1,
							DataProviderException arg2) {

					}
				});

				// Log.d(TAG,
				// "I see an iBeacon: "+myBeacon.getId1()+"("+myBeacon.getId2()+" - "+myBeacon.getId3()+")"
				// +myBeacon.getDistance()+" meters away");
				// Log.d(TAG, String.valueOf(myBeacon.getId2().toInt()));
				if (!EcoBeaconsApplication.isAppVisible()) {
					Log.d(TAG, "Background");
					if (myBeacon.getId2().toInt() == 50000
							&& myBeacon.getDistance() < 0.5) {
						Log.d(TAG, "Notification?");
						Notification.Builder builder = new Notification.Builder(
								this);
						getBigTextStyle(builder);
						Intent resultIntent = new Intent(this,
								VendingActivity.class);

						
						Intent startMain = new Intent(this,
								MainActivity.class);
						startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startMain.putExtra("Notification", 2);
						PendingIntent homePendingIntent = PendingIntent
								.getActivity(getBaseContext(), 0, startMain,
										PendingIntent.FLAG_UPDATE_CURRENT);

						// The stack builder object will contain an artificial
						// back stack for the
						// started Activity.
						// This ensures that navigating backward from the
						// Activity leads out of
						// your application to the Home screen.
						TaskStackBuilder stackBuilder = TaskStackBuilder
								.create(this);
						// Adds the back stack for the Intent (but not the
						// Intent itself)
						stackBuilder.addParentStack(MainActivity.class);
						// Adds the Intent that starts the Activity to the top
						// of the stack
						stackBuilder.addNextIntent(resultIntent);
						// stackBuilder.addNextIntent(startMain);
						PendingIntent resultPendingIntent = stackBuilder
								.getPendingIntent(0,
										PendingIntent.FLAG_UPDATE_CURRENT);

						builder.addAction(android.R.drawable.ic_menu_help,
								"Yes", resultPendingIntent);
						builder.addAction(android.R.drawable.ic_menu_delete,
								"No", homePendingIntent);
						// builder.setContentIntent(resultPendingIntent);
						NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						// mId allows you to update the notification later on.
						mNotificationManager.notify(2, builder.build());

					}
				} else {
					String displayString = myBeacon.getDistance() + " "
							+ myBeacon.getId2() 
							+ "\n";
					Log.d(TAG, displayString);

					//displayTableRow(myBeacon, displayString, false);

				}

				// Log.d(TAG,
				// "This is my favourite iBeacon: "+myBeacon.getId1());
				// Log.d(TAG, "It is : "+myBeacon.getDistance()+" meters away");

				// String displayString =
				// iBeacon.getProximityUuid()+" "+iBeacon.getMajor()+" "+iBeacon.getMinor()+"\n";
				// displayTableRow(iBeacon, displayString, false);
				// Log.i(TAG,
				// "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");

			}
		}

	}

	private void displayToast(double distance) {
		int offsetX = 50;
		int offsetY = 25;

		Toast lToast = Toast.makeText(
				(EcoBeaconsApplication) getBaseContext(), "you are "
						+ distance + " far to the closest litter",
				Toast.LENGTH_SHORT);
		lToast.setGravity(Gravity.RIGHT | Gravity.TOP, offsetX, offsetY);
		lToast.show();
	}

	/**
	 * 
	 * @param builder
	 * @return Notification when an iBeacon is in the Immediate field
	 */
	private Notification getBigTextStyle(Notification.Builder builder) {

		long[] pattern = new long[]{1000,50,1000};
		// Uri defaultSound =
		// RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		builder.setContentTitle("Reduced BigText title")
				.setContentText("Reduced content").setContentInfo("Info")
				.setSmallIcon(R.drawable.icon_loop_small_small)
				//.setVibrate(pattern)
				.setLights(Color.BLUE, 1, 0)
				//.setSound(defaultSound)
				.setAutoCancel(true);
				

		return new Notification.BigTextStyle(builder)
				.bigText("Either something to eat or drink? You can learn how and where to recycle with this App!!")
				.setBigContentTitle("Have you bought something?")
				.setSummaryText("Close the loop").build();
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
		Log.i(TAG, "index selected: "+ index);
		
		if(!mTipsFragment.isAdded()) {		
			Log.i(TAG, "starting transaction: "+ index);
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
			Log.i(TAG, "finished transaction: ");
		}
		if (mTipsFragment.getShownIndex() != index) {
			Log.i(TAG, "show the text: "+ index);
			// Tell the QuoteFragment to show the quote string at position index
			mTipsFragment.showQuoteAtIndex(index);
		}
		
	}
}
