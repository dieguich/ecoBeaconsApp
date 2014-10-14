package uk.ac.lincoln.lisc.ecobeacons;

import java.util.Collection;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;


import android.app.Application;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

public class BeaconReferenceApplication extends Application implements BootstrapNotifier, RangeNotifier {
	private static final String TAG = "BeaconReferenceApplicationMod";
	private BeaconManager mBeaconManager;
	private Region mAllBeaconsRegion;
	private static boolean activityVisible;
	@SuppressWarnings("unused")
	private RegionBootstrap mRegionBootstrap;

	
	@Override 
	public void onCreate() {
		Log.d(TAG, "OnCreateApp");
		mAllBeaconsRegion = new Region("My first Region",
				Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.setBackgroundBetweenScanPeriod(10000l);
        mBeaconManager.setBackgroundScanPeriod(5000l);
        mRegionBootstrap = new RegionBootstrap(this, mAllBeaconsRegion);
				
	}
	
	@Override
	public void didRangeBeaconsInRegion(Collection<Beacon> arg0, Region arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void didDetermineStateForRegion(int arg0, Region arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didEnterRegion(Region arg0) {
		Log.d(TAG, "OnRegion");
		//mRegionBootstrap.disable();
		Intent intent = new Intent(this, MonitoringActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);	
	}

	@Override
	public void didExitRegion(Region arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public static boolean isActivityVisible() {
	    return activityVisible;
	}  

	public static void activityResumed() {
		activityVisible = true;
	}

	public static void activityPaused() {
		activityVisible = false;
	}
}