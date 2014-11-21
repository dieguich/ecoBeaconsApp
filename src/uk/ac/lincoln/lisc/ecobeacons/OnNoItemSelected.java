package uk.ac.lincoln.lisc.ecobeacons;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class OnNoItemSelected extends BroadcastReceiver {

	final String mNoSelection = "ecobeacons.NO_SELECTION";
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		int lNotificationValue = intent.getIntExtra("Notification", 0);
	    if(mNoSelection.equals(action)) {
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(lNotificationValue);
			//EcoBeaconsApplication.setRealBackgroundMode();
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(startMain);
	    }
	}
}
