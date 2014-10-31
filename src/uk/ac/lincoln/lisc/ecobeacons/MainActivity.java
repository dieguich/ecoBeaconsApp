package uk.ac.lincoln.lisc.ecobeacons;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconData;
import org.altbeacon.beacon.BeaconDataNotifier;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.client.DataProviderException;
import org.altbeacon.beaconreference.R;

import uk.ac.lincoln.lisc.vending.VendingActivity;

/**
 * 
 * @author dieguich
 */
public class MainActivity extends Activity implements BeaconConsumer,
		RangeNotifier {

	protected static final String TAG = "RangingActivityMod";

	private BeaconManager beaconManager;

	//Map<String, TableRow> rowMap = new HashMap<String, TableRow>();

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
		beaconManager.bind(this);
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
								MainActivity.class);

						// Intent startMain = new Intent(Intent.ACTION_MAIN);
						// startMain.addCategory(Intent.CATEGORY_HOME);
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

	/**
	 * Displays a Table where the iBeacons in the proximity are shown as well as
	 * their relative distance to the Bluetooth-enabled phone.
	 * 
	 * @param iBeacon
	 * @param displayString
	 * @param updateIfExists
	 */
	/*
	private void displayTableRow(final Beacon iBeacon,
			final String displayString, final boolean updateIfExists) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TableLayout table = (TableLayout) findViewById(R.id.beacon_table);
				String key = iBeacon.getDistance() + "-" + iBeacon.getId2()
						+ "-" + iBeacon.getId3();
				TableRow tr = (TableRow) rowMap.get(key);
				if (tr == null) {
					tr = new TableRow(RangingActivity.this);
					tr.setLayoutParams(new TableRow.LayoutParams(
							TableRow.LayoutParams.WRAP_CONTENT,
							TableRow.LayoutParams.WRAP_CONTENT));
					rowMap.put(key, tr);
					table.addView(tr);
				} else {
					if (updateIfExists == false) {
						return;
					}
				}
				tr.removeAllViews();
				TextView textView = new TextView(RangingActivity.this);
				textView.setText(displayString);
				tr.addView(textView);
				

			}
		});
	}*/
}
