package uk.ac.lincoln.lisc.ecobeacons;


import org.altbeacon.beaconreference.R;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HeadingsFragment extends ListFragment {
	
	private static final String TAG = "HeadingsFragment";
	private ListSelectionListener mListener = null;
	
	
	// Callback interface that allows this Fragment to notify the QuoteViewerActivity when  
	// user clicks on a List Item  
	public interface ListSelectionListener {
		public void onListSelection(int index);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {

		// Indicates the selected item has been checked
		getListView().setItemChecked(pos, true);
		
		// Inform the MainActivity that the item in position pos has been selected
		mListener.onListSelection(pos);
	}

	@Override
	public void onAttach(Activity activity) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onAttach()");
		super.onAttach(activity);
		
		try {
			// Set the ListSelectionListener for communicating with the QuoteViewerActivity
			mListener = (ListSelectionListener) activity;
		
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnArticleSelectedListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onCreateView()");
		return super.onCreateView(inflater, container, savedInstanceState);
	}


	@Override
	public void onActivityCreated(Bundle savedState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onActivityCreated()");
		super.onActivityCreated(savedState);
		
		// Set the list adapter for the ListView 
		// Discussed in more detail in the user interface classes lesson  
		setListAdapter(new ArrayAdapter<String>(getActivity(),
				R.layout.title_item, MainActivity.mTipHeadingsArray));

		// Set the list choice mode to allow only one selection at a time
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
}
