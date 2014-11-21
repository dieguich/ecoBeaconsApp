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
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/**
 * 
 * @author dieguich
 */
public class EcoBeaconsApplication extends Application implements
		BootstrapNotifier {

	private static final long mIntitialScanPeriod    = 2200; // milliseconds
	private static final long mIntitialScanFrequency = 3300; // milliseconds
																
	private static final long mInRegionFirstTimeScanPeriod    = 4400; // milliseconds
	private static final long mInRegionFirstTimeScanFrequency = 500; // milliseconds
	
	private static final long mInRegionNearScanPeriod        = 1100; // milliseconds Less than 10meters
	private static final long mInRegionNearTimeScanFrequency = 100; // milliseconds  Less than 10meters
																
	//private static final String mIBeaconVendingUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final String mIBeaconVendingUUID   = "A7AE2EB7-1F00-4168-B99B-A749BAC1CA64";
	private static final String mIBeaconRecyclingUUID = "A7AE2EB7-1F00-4168-B99B-A749BAC1CA64";
	private static final String TAG                   = "BeaconReferenceApplicationMod";
	private static final String mRecyclingRegName     = "Recycling";
	private static final String mVendingRegName       = "Vending";
	
	//private static final int mVendingMajorID = 50000;
	private static final int mVendingMajorID   = 111;
	private static final int mVendingMinorID   = 4;
	
	private static final int mRecyclingMajorID = 999;
	private static final int mRecyclingMinorID = 2;
	
	
	private static BeaconManager mBeaconManager;
	private static Region        mBeaconsRegion;
	private static List<Region>  mRegionList;
	@SuppressWarnings("unused")
	private static RegionBootstrap mRegionBootstrap;

	//private static boolean mAppVisible; // To control if the App is in background or foreground

	private int numRunningActivities = 0;
	private static int mRangingMode  = 0;
	private static Activity mCurrentAct;
	

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
					mCurrentAct = null;
				} else {
					Log.i(TAG, numRunningActivities + " activities remaining");
				}

			}

			@Override
			public void onActivityStarted(Activity activity) {
				Log.i(TAG, "Activity Started: " + activity.getClass().getName());
				numRunningActivities++;
				mCurrentAct = activity;
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
				if (activity.getClass().getName().contains("NavigateToBinActivity")) {
					Log.d(TAG, "NavigateActivity Created");
					//mRegionBootstrap.disable();
					mBeaconsRegion = getRegion(mRecyclingRegName);
					mRegionBootstrap = new RegionBootstrap(
							(EcoBeaconsApplication) getApplicationContext(), mBeaconsRegion);
				}
				if (activity.getClass().getName().contains("Litter")) {
					mRegionBootstrap.disable();
					//TODO: Activate again after 10mins or something like that.
				}
			}
		});
		setRegionList();
		mRegionBootstrap = new RegionBootstrap(this, getRegion(mVendingRegName));
		mBeaconManager   = BeaconManager.getInstanceForApplication(this);
		setRegionMode();
		//setRealBackgroundMode();
	}

	@Override
	public void didDetermineStateForRegion(int arg0, Region arg1) {
		Log.d(TAG, "DetermineState: " + arg0);
		// TODO Auto-generated method stub

	}

	@Override
	public void didEnterRegion(final Region region) {
		Log.d(TAG, "OnRegion: " + region.getUniqueId());
		setRegionMode();
		if(region.getUniqueId().equalsIgnoreCase(mVendingRegName)) {
			setRangingMode(1);
		}
		if(mCurrentAct != null && !mCurrentAct.getClass().getName().contains("Navigate")) {
			mCurrentAct.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					toast("READY?");
				}
			});
		}
	}

	@Override
	public void didExitRegion(final Region region) {
		Log.d(TAG, "ExitRegion");
		if(region.getUniqueId().contains("Vending")) {
			//setBackgroundMode();
		}
		if(mCurrentAct != null) {
			mCurrentAct.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					toast("Exit Region " + region.getUniqueId());
					
				}
			});
		}
	}
	public static String getCurrentActivity() {
		if(mCurrentAct != null) {
			return mCurrentAct.getClass().getName();
		}
		else {
			return null;
		}
		
	}

	/**
	 * This method returns the altBeacon Region which corresponds with the name passed as parameter. If no matchings
	 * It returns null
	 * @param regionName
	 * @return
	 */
	public static Region getRegion(String regionName) {
		Region lRegion = null;
		for (Region iterable_element : mRegionList) {
			if(iterable_element.getUniqueId().equalsIgnoreCase(regionName)){
				lRegion = iterable_element;
			}
		}
		return lRegion;
	}


	public static void setBackgroundMode() {
		Log.d(TAG, "BackgroundMODE");
		mBeaconManager.setBackgroundBetweenScanPeriod(mIntitialScanFrequency);
		mBeaconManager.setForegroundBetweenScanPeriod(mIntitialScanFrequency);
		mBeaconManager.setBackgroundScanPeriod(mIntitialScanPeriod);
		mBeaconManager.setForegroundScanPeriod(mIntitialScanPeriod);
		try {
			mBeaconManager.updateScanPeriods();
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
	}
	
	public static void setRealBackgroundMode() {
		Log.d(TAG, "REALBackgroundMODE");
		long lTime = 30*1000;
	
		mBeaconManager.setBackgroundBetweenScanPeriod(lTime);
		mBeaconManager.setForegroundBetweenScanPeriod(lTime);
		mBeaconManager.setBackgroundScanPeriod(5000);
		mBeaconManager.setForegroundScanPeriod(5000);
		try {
			mBeaconManager.updateScanPeriods();
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
	}
	
	public static void setNearMode() {
		Log.d(TAG, "NearMODE");
		mBeaconManager.setBackgroundBetweenScanPeriod(mInRegionNearTimeScanFrequency);
		mBeaconManager.setForegroundBetweenScanPeriod(mInRegionNearTimeScanFrequency);
		mBeaconManager.setBackgroundScanPeriod(mInRegionNearScanPeriod);
		mBeaconManager.setForegroundScanPeriod(mInRegionNearScanPeriod);
		try {
			mBeaconManager.updateScanPeriods();
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
	}
	
	private void setRegionMode() {
		Log.d(TAG, "OnRegionMODE");
		mBeaconManager.setBackgroundBetweenScanPeriod(mInRegionFirstTimeScanFrequency);
		mBeaconManager.setForegroundBetweenScanPeriod(mInRegionFirstTimeScanFrequency);
		mBeaconManager.setBackgroundScanPeriod(mInRegionFirstTimeScanPeriod);
		mBeaconManager.setForegroundScanPeriod(mInRegionFirstTimeScanPeriod);
		try {
			mBeaconManager.updateScanPeriods();
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
	}

	public void toast(String message) {
		
		Toast lToast = Toast.makeText(super.getApplicationContext(), message,
				Toast.LENGTH_LONG);
		lToast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 50, 25);
		lToast.show();
	}
	
	/**
	 * This method adds the Regions to the Array Region List and sets as bootstrap the "Vending" Region
	 */
	private void setRegionList() {
		mBeaconsRegion   = new Region(mVendingRegName, Identifier.parse(mIBeaconVendingUUID), 
				Identifier.fromInt(mVendingMajorID), Identifier.fromInt(mVendingMinorID));
		mRegionList = new ArrayList<Region>();
		mRegionList.add(mBeaconsRegion);
		mRegionList.add(new Region(mRecyclingRegName, Identifier.parse(mIBeaconRecyclingUUID),
				Identifier.fromInt(mRecyclingMajorID), Identifier.fromInt(mRecyclingMinorID)));
	}
	
	public static Integer getRangingMode() {
		return mRangingMode;
	}

	public static void setRangingMode(int rangingMode) {
		mRangingMode = rangingMode;
	}

}