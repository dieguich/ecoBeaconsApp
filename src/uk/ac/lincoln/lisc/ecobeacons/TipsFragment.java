package uk.ac.lincoln.lisc.ecobeacons;


import org.altbeacon.beaconreference.R;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TipsFragment extends Fragment {
	
	private static final String TAG = "TipsFragment";

	private TextView mTipsView = null;
	private int mCurrIdx = -1;
	private int mTipsArrLen;

	int getShownIndex() {
		return mCurrIdx;
	}
	
	// Show the Quote string at position newIndex
	void showQuoteAtIndex(int newIndex) {
		if (newIndex < 0 || newIndex >= mTipsArrLen)
			return;
		mCurrIdx = newIndex;
		mTipsView.setText(MainActivity.mTipsDescArray[mCurrIdx]);
	}
	
	// Called to create the content view for this Fragment
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onCreateView()");

		// Inflate the layout defined in quote_fragment.xml
		// The last parameter is false because the returned view does not need to be attached to the container ViewGroup
		return inflater.inflate(R.layout.tips_description, container, false);
	}
	
	// Set up some information about the mQuoteView TextView 
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onActivityCreated()");
		super.onActivityCreated(savedInstanceState);

		mTipsView   = (TextView) getActivity().findViewById(R.id.tipsView);
		mTipsArrLen = MainActivity.mTipsDescArray.length;
	}
}
