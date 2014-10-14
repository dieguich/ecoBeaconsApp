package uk.ac.lincoln.lisc.ecobeacons;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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



/**
 * 
 * @author dyoung
 * @author Matt Tyler
 */
public class MonitoringActivity extends Activity implements BeaconConsumer, RangeNotifier{
	protected static final String TAG = "RangingActivityMod";
    private BeaconManager beaconManager;
    Map<String,TableRow> rowMap = new HashMap<String,TableRow>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
               setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); 
        //beaconManager.setBackgroundBetweenScanPeriod(10000);
		setContentView(R.layout.activity_monitoring);
		verifyBluetooth();
		beaconManager.bind(this);
	}
	

	private void verifyBluetooth() {

		try {
			if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
		}
		catch (RuntimeException e) {
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
    protected void onDestroy() {
    	Log.d(TAG, "onDestroy");
		super.onDestroy();
		beaconManager.unbind(this);
		
    }
    @Override 
    protected void onPause() {
    	super.onPause();
    	BeaconReferenceApplication.activityPaused();
    }
    @Override 
    protected void onResume() {
    	Log.d(TAG, "onResume");
    	super.onResume();
    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    	BeaconReferenceApplication.activityResumed();
    }    
    
    /*private void logToDisplay(final String line) {
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	    	EditText editText = (EditText)MonitoringActivity.this
    					.findViewById(R.id.monitoringText);
       	    	editText.append(line+"\n");            	    	    		
    	    }
    	});
    }*/
    

	@Override
	public void onBeaconServiceConnect() {
		Log.d(TAG, "onBeaconService");
		beaconManager.setRangeNotifier(this); 
		try {
			beaconManager.startRangingBeaconsInRegion(new Region("otherRegion", null, null, null));
			
		} catch (RemoteException e) {	}
	}
	
	
	private void displayTableRow(final Beacon iBeacon, final String displayString, final boolean updateIfExists) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TableLayout table = (TableLayout) findViewById(R.id.beacon_table);
                String key = iBeacon.getDistance() + "-" + iBeacon.getId2() + "-" + iBeacon.getId3();
                TableRow tr = (TableRow) rowMap.get(key);
                if (tr == null) {
                    tr = new TableRow(MonitoringActivity.this);
                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    rowMap.put(key, tr);
                    table.addView(tr);
                }
                else {
                    if (updateIfExists == false) {
                        return;
                    }
                }
                tr.removeAllViews();
                TextView textView=new TextView(MonitoringActivity.this);
                textView.setText(displayString);
                tr.addView(textView);

            }
        });

    }





	@Override
	public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
		Log.i(TAG, String.valueOf(beacons.size()));
		if (beacons.size() > 0) {
			for (Beacon myBeacon: beacons) {
				myBeacon.requestData( new BeaconDataNotifier() {

					@Override
					public void beaconDataUpdate(Beacon arg0,
							BeaconData arg1, DataProviderException arg2) {
						// TODO Auto-generated method stub
						
					}
				});
				
				//Log.d(TAG, "I see an iBeacon: "+myBeacon.getId1()+"("+myBeacon.getId2()+" - "+myBeacon.getId3()+")" +myBeacon.getDistance()+" meters away");
				//Log.d(TAG, String.valueOf(myBeacon.getId2().toInt()));
				if(!BeaconReferenceApplication.isActivityVisible()){
					//Log.d(TAG, String.valueOf(myBeacon.getDistance()));
					if(myBeacon.getId2().toInt() == 50000 && myBeacon.getDistance() < 1.0){
						Log.d(TAG, "Notification?");
						Notification.Builder builder = new Notification.Builder(this);
						getBigTextStyle(builder);
						Intent resultIntent = new Intent(this, MonitoringActivity.class);
						
						
						
						// The stack builder object will contain an artificial back stack for the
						// started Activity.
						// This ensures that navigating backward from the Activity leads out of
						// your application to the Home screen.
						TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
						// Adds the back stack for the Intent (but not the Intent itself)
						stackBuilder.addParentStack(MonitoringActivity.class);
						// Adds the Intent that starts the Activity to the top of the stack
						stackBuilder.addNextIntent(resultIntent);
						PendingIntent resultPendingIntent =
						        stackBuilder.getPendingIntent(
						            0,
						            PendingIntent.FLAG_UPDATE_CURRENT
						        );
						builder.setContentIntent(resultPendingIntent);
						NotificationManager mNotificationManager =
						    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						// mId allows you to update the notification later on.
						mNotificationManager.notify(666, builder.build());
					}
				}
				else{
					String displayString = myBeacon.getDistance()+" "+myBeacon.getId2()+" "+myBeacon.getId3()+"\n";
					displayTableRow(myBeacon, displayString, false);
				}
				
					//Log.d(TAG, "This is my favourite iBeacon: "+myBeacon.getId1());
					//Log.d(TAG, "It is : "+myBeacon.getDistance()+" meters away");
					
				
				//String displayString = iBeacon.getProximityUuid()+" "+iBeacon.getMajor()+" "+iBeacon.getMinor()+"\n";
	            //displayTableRow(iBeacon, displayString, false);
				//Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
				
			}
		}
		
	}
	
	private Notification getBigTextStyle(Notification.Builder builder) {
        builder
                .setContentTitle("Reduced BigText title")
                .setContentText("Reduced content")
                .setContentInfo("Info")
                .setSmallIcon(R.drawable.ic_launcher);
 
        return new Notification.BigTextStyle(builder)
                .bigText("(TV, DVD player, stereo system) into a single multi-socket electrical bar. " +
                		"When not in use, simply switch off the bar and save on the electrical consumption by as much as 10 %. " +
                		"Appliances left on standby still use quite a lot of electricity.")
                .setBigContentTitle("Plug the electronics in your living-room")
                .setSummaryText("How to save energy")
                .build();
    }
}
