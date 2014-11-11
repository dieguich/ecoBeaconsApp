package uk.ac.lincoln.lisc.vending;

import java.util.ArrayList;
import java.util.Arrays;

import org.altbeacon.beaconreference.R;

import uk.ac.lincoln.lisc.ecobeacons.EcoBeaconsApplication;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

//This application uses some deprecated methods. 
//See UIViewPager for a more modern version of this application

public class VendingActivity extends Activity {

	protected static final String TAG = "GridActivity";
	protected static final String EXTRA_RES_ID = "POS";
	
	private GridView gridView;
	private GridViewAdapter customGridAdapter;

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

		gridView = (GridView) findViewById(R.id.gridView);

		// Create a new ImageAdapter and set it as the Adapter for this GridView
		customGridAdapter = new GridViewAdapter(this, R.layout.row_grid, getData());
		gridView.setAdapter(customGridAdapter);

		// Set an setOnItemClickListener on the GridView
		gridView.setOnItemClickListener(new OnItemClickListener() {
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
				//Intent intent = new Intent(VendingActivity.this,
				//		NavigateToBinActivity.class);

				// Add the ID of the thumbnail to display as an Intent Extra
				AlertDialog alertDialog = new AlertDialog.Builder(VendingActivity.this).create();
				alertDialog.setTitle("Nothing to recycle..");
				alertDialog.setMessage("Just throw it to any litter");
				alertDialog.setIcon(R.drawable.icon_loop_small);
				alertDialog.setCanceledOnTouchOutside(true);
				alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						VendingActivity.this.finish();
						
					}
				});
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
	
	private ArrayList<ImageItem> getData() {
		final ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
		final String[] lImageCaptions = getResources().getStringArray(R.array.image_captions);
		// retrieve String drawable array
		TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
		for (int i = 0; i < imgs.length(); i++) {
			Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
					imgs.getResourceId(i, -1));
			imageItems.add(new ImageItem(bitmap, lImageCaptions[i]));
		}

		return imageItems;

	}
}