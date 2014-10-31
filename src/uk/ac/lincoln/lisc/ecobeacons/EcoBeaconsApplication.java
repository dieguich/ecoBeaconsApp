package uk.ac.lincoln.lisc.ecobeacons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import uk.ac.lincoln.lisc.vending.VendingActivity;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author dieguich
 */
public class EcoBeaconsApplication extends Application implements
		BootstrapNotifier {

	private static final long mIntitialScanPeriod    = 2000; // milliseconds
	private static final long mIntitialScanFrequency = 9200; // milliseconds
																
	private static final long mInRegionFirstTimeScanPeriod    = 2000; // milliseconds
	private static final long mInRegionFirstTimeScanFrequency = 5200; // milliseconds
	
	private static final long mInRegionNearScanPeriod        = 1100; // milliseconds Less than 10meters
	private static final long mInRegionNearTimeScanFrequency = 2200; // milliseconds  Less than 10meters
																
	private static final String mIBeaconVendingUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final String TAG                 = "BeaconReferenceApplicationMod";
	
	private static final int mVendingMajorID = 50000;
	
	private static BeaconManager mBeaconManager;
	private static Region        mBeaconsRegion;
	private static List<Region>  mRegionList;
	@SuppressWarnings("unused")
	private static RegionBootstrap mRegionBootstrap;

	private static boolean mAppVisible; // To control if the App is in background or foreground

	private int numRunningActivities = 0;

	@Override
	public void onCreate() {
		Log.d(TAG, "OnCreateApp");
		this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

			@Override
			public void onActivityStopped(Activity activity) {
				Log.i(TAG, "Activity Stopped: " + activity.getClass().getCanonicalName());
				numRunningActivities--;

				if (numRunningActivities == 0) {
					Log.i(TAG, "No running activities left, app has likely entered the background.");
				} else {
					Log.i(TAG, numRunningActivities + " activities remaining");
				}

			}

			@Override
			public void onActivityStarted(Activity activity) {
				Log.i(TAG, "Activity Started: " + activity.getClass().getName());
				numRunningActivities++;
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity,
					Bundle outState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onActivityResumed(Activity activity) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onActivityPaused(Activity activity) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onActivityCreated(Activity activity,
					Bundle savedInstanceState) {
				if (activity.getClass().getName()
						.contains("GridLayoutActivity")) {
					Log.i(TAG, "GridActivity: " + activity.getClass().getName());
					// ((GridLayoutActivity)activity).unbindService((ServiceConnection)
					// activity);
					mRegionBootstrap.disable();
					mBeaconsRegion = new Region("Recycling", Identifier
							.parse(mIBeaconVendingUUID), Identifier
							.fromInt(29218), null);
					mRegionBootstrap = new RegionBootstrap(
							(EcoBeaconsApplication) getApplicationContext(),
							mBeaconsRegion);
				}

			}
		});
		setRegionList();
		mRegionBootstrap = new RegionBootstrap(this, getRegion("Vending"));
		mBeaconManager   = BeaconManager.getInstanceForApplication(this);
		mBeaconManager.setBackgroundBetweenScanPeriod(mIntitialScanFrequency);
		mBeaconManager.setBackgroundScanPeriod(mIntitialScanPeriod);
	}

	@Override
	public void didDetermineStateForRegion(int arg0, Region arg1) {
		Log.d(TAG, "DetermineState: " + arg0);
		// TODO Auto-generated method stub

	}

	@Override
	public void didEnterRegion(Region region) {
		Log.d(TAG, "OnRegion: " + region.getUniqueId());
		mBeaconManager.setBackgroundBetweenScanPeriod(mInRegionFirstTimeScanFrequency);
		mBeaconManager.setBackgroundScanPeriod(mInRegionFirstTimeScanPeriod);
	}

	@Override
	public void didExitRegion(Region arg0) {
		Log.d(TAG, "ExitRegion");
		mBeaconManager.setBackgroundBetweenScanPeriod(mIntitialScanFrequency);
		mBeaconManager.setBackgroundScanPeriod(mIntitialScanPeriod);
	}

	public static Region getRegion(String regionName) {
		Region lRegion = null;
		for (Region iterable_element : mRegionList) {
			if(iterable_element.getUniqueId().equalsIgnoreCase(regionName)){
				lRegion = iterable_element;
			}
		}
		return lRegion;
	}

	public static boolean isAppVisible() {
		return mAppVisible;
	}

	public static void appResumed() {
		Log.d(TAG, "App RESUMED");
		mAppVisible = true;
	}

	public static void appPaused() {
		Log.d(TAG, "App PAUSED");
		mAppVisible = false;
	}

	public static void setBackgroundMode() {
		mBeaconManager.setBackgroundBetweenScanPeriod(mIntitialScanFrequency);
		mBeaconManager.setBackgroundScanPeriod(mIntitialScanPeriod);
	}

	public void toast(String message) {
		Toast.makeText(super.getApplicationContext(), message,
				Toast.LENGTH_LONG).show();
	}
	
	private void setRegionList() {
		
		//Identifier.parse(mIBeaconVendingUUID)
		//mVendingidentifiers = new ArrayList<Identifier>();
		//mVendingidentifiers.add(Identifier.parse(mIBeaconVendingUUID));
		//mVendingidentifiers.add(Identifier.fromInt(mVendingMajorID));
		//mVendingidentifiers.add(null);
		//mBeaconsRegion   = new Region("Vending", mVendingidentifiers);
		//mRegionBootstrapTest = new Region("Other", Identifier.parse(mIBeaconVendingUUID), 
		//		Identifier.fromInt(35451), null);
		mBeaconsRegion   = new Region("Vending", Identifier.parse(mIBeaconVendingUUID), 
				Identifier.fromInt(mVendingMajorID), null);
		mRegionList = new ArrayList<Region>();
		mRegionList.add(mBeaconsRegion);
		//lRegionList.add(mRegionBootstrapTest);
	}

}