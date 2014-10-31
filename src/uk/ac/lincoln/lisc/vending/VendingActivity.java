package uk.ac.lincoln.lisc.vending;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
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

import uk.ac.lincoln.lisc.ecobeacons.EcoBeaconsApplication;

//This application uses some deprecated methods. 
//See UIViewPager for a more modern version of this application

public class VendingActivity extends Activity {

	protected static final String TAG = "GridActivity";
	protected static final String EXTRA_RES_ID = "POS";
	
	private GridView gridview;


	private ArrayList<Integer> mThumbIdsFlowers = new ArrayList<Integer>(
			Arrays.asList(R.drawable.cans, R.drawable.disposable_mugs,
					R.drawable.sandwich_wedges, R.drawable.soda_bottles,
					R.drawable.polystyrene_box, R.drawable.candy_wrappers));

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreateGrid");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vending);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.cancel(2);

		gridview = (GridView) findViewById(R.id.gridview);

		// Create a new ImageAdapter and set it as the Adapter for this GridView
		gridview.setAdapter(new ImageAdapter(this, mThumbIdsFlowers));

		// Set an setOnItemClickListener on the GridView
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				// Create an Intent to start the ImageViewActivity
				Intent intent = new Intent(VendingActivity.this,
						NavigateToBinActivity.class);

				// Add the ID of the thumbnail to display as an Intent Extra
				intent.putExtra(EXTRA_RES_ID, (int) id);

				// Start the ImageViewActivity
				startActivity(intent);
			}
		});
		
		ImageView image = (ImageView) findViewById(R.id.otherstuff);
		image.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(VendingActivity.this,
						NavigateToBinActivity.class);

				// Add the ID of the thumbnail to display as an Intent Extra
				AlertDialog alertDialog = new AlertDialog.Builder(VendingActivity.this).create();
				alertDialog.setTitle("Nothing to recycle..");
				alertDialog.setMessage("Just throw it to any litter");
				alertDialog.setIcon(R.drawable.icon_loop_small);
				alertDialog.setCanceledOnTouchOutside(true);
				alertDialog.show();
				
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
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
		Log.d(TAG, "onDestroyGrid");
		super.onDestroy();
		/*try {
			Intent startMain = new Intent(this, VendingActivity.class);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startMain.putExtra("Notification", 2);
			this.startActivity(startMain);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}*/
	}
}