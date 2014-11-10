package uk.ac.lincoln.lisc.ecobeacons;

import org.altbeacon.beaconreference.R;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class HomeFragment extends Fragment {
	
	private static final String TAG = "LoopFragment";
	
	private ImageView mImageview;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onAttach()");
		View view =  inflater.inflate(R.layout.fragment_main, container, false);
		mImageview = (ImageView) view.findViewById(R.id.home_image);  
		return view;	
	}
}
