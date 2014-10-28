package uk.ac.lincoln.lisc.vending;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beaconreference.R;

import uk.ac.lincoln.lisc.ecobeacons.BeaconReferenceApplication;
import uk.ac.lincoln.lisc.ecobeacons.RangingActivity;

//This application uses some deprecated methods. 
//See UIViewPager for a more modern version of this application

public class GridLayoutActivity extends Activity implements BeaconConsumer {

	protected static final String TAG = "GridActivity";
	protected static final String EXTRA_RES_ID = "POS";

	private BeaconManager beaconManager;

	private ArrayList<Integer> mThumbIdsFlowers = new ArrayList<Integer>(
			Arrays.asList(R.drawable.cans, R.drawable.disposable_mugs,
					R.drawable.sandwich_wedges, R.drawable.soda_bottles,
					R.drawable.polystyrene_box, R.drawable.candy_wrappers,
					R.drawable.other_stuff));

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreateGrid");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vending);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.cancel(2);

		GridView gridview = (GridView) findViewById(R.id.gridview);

		// Create a new ImageAdapter and set it as the Adapter for this GridView
		gridview.setAdapter(new ImageAdapter(this, mThumbIdsFlowers));

		// Set an setOnItemClickListener on the GridView
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				// Create an Intent to start the ImageViewActivity
				Intent intent = new Intent(GridLayoutActivity.this,
						ImageViewActivity.class);

				// Add the ID of the thumbnail to display as an Intent Extra
				intent.putExtra(EXTRA_RES_ID, (int) id);

				// Start the ImageViewActivity
				startActivity(intent);
			}
		});

		// BeaconReferenceApplication.setRegion(BeaconReferenceApplicationRef,
		// BeaconReferenceApplication.getRegion());
		beaconManager = BeaconManager.getInstanceForApplication(this);
		beaconManager
				.getBeaconParsers()
				.add(new BeaconParser()
						.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
		beaconManager.bind(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		BeaconReferenceApplication.appResumed();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPauseRanging");
		super.onPause();
		BeaconReferenceApplication.appPaused();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroyGrid");
		super.onDestroy();
		try {
			// beaconManager.stopRangingBeaconsInRegion(region);
			beaconManager.unbind(this);
			Intent startMain = new Intent(this, RangingActivity.class);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startMain.putExtra("Notification", 2);
			this.startActivity(startMain);
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

				}
			}

			public double round(double value, int places) {
				if (places < 0)
					throw new IllegalArgumentException();

				BigDecimal bd = new BigDecimal(value);
				bd = bd.setScale(places, RoundingMode.HALF_UP);
				return bd.doubleValue();
			}

			private void toastDistance(final String ldistance) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Log.d(TAG, "Runnable");
						BeaconReferenceApplication variableName = (BeaconReferenceApplication) GridLayoutActivity.this
								.getApplication();
						variableName.toast(ldistance);

					}
				});

			}
		});

		try {
			beaconManager
					.startRangingBeaconsInRegion(BeaconReferenceApplication
							.getRegion());

		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage());
		}
	}
}