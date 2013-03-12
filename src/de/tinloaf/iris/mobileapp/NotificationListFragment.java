package de.tinloaf.iris.mobileapp;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.data.Destruction;
import de.tinloaf.iris.mobileapp.data.SavedPortal;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class NotificationListFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	private DatabaseHelper databaseHelper = null;

    private BroadcastReceiver mHandleMessageReceiver;
    
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	}

	private DatabaseHelper getHelper() {
	    if (databaseHelper == null) {
	        databaseHelper =
	            OpenHelperManager.getHelper(this.getActivity(), DatabaseHelper.class);
	    }
	    return databaseHelper;
	}

    private class DestructionReceiver extends BroadcastReceiver {
    	private NotificationAdapter adapter;
    	
    	public DestructionReceiver(NotificationAdapter adapter) {
    		super();
    		this.adapter = adapter;
    	}
    	
        @Override
        public void onReceive(Context context, Intent intent) {
        	try {
	        	Log.v("NLF", "Received Destruction-Broadcast");
	        	ArrayList<Destruction> destructions = intent.getParcelableArrayListExtra("DESTRUCTIONS");
	        	
	        	Iterator<Destruction> it = destructions.iterator();
	        	while (it.hasNext()) {
	        		Destruction cur = it.next();
	        		
	        		// Get the portal
	        		Dao<SavedPortal, Integer> portalDao = getHelper().getPortalDao();
	        		
	        		SavedPortal portal = portalDao.queryForId(cur.portalId);
	        		// TODO what if not there?
	        		
	        		String destrString = Integer.toString(cur.count) + " ";
	        		switch (cur.kind) {
	        		case Destruction.KIND_LINK:
	        			destrString += "link(s)";
	        			break;
	        		case Destruction.KIND_RESONATOR:
	        			destrString += "resonator(s)";
	        			break;
	        		case Destruction.KIND_MOD:
	        			destrString += "mod(s)";
	        			break;
	        		}
	        		
	        		Notification notification = new Notification(portal, destrString, "");
	        		this.adapter.add(notification);
	        	}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
    };
	
	public NotificationListFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.v("NLF", "onCreateView");

		
		View rootView = inflater.inflate(R.layout.fragment_notification_list,
				container, false);
		ListView notificationList = (ListView)rootView
				.findViewById(R.id.notification_list);
		
		ArrayList<Notification> notifyData = new ArrayList<Notification>();
       
        NotificationAdapter adapter = new NotificationAdapter(this.getActivity(),
        		R.layout.notification_list_item, notifyData, this.getHelper());
		
        notificationList.setAdapter(adapter);

		this.mHandleMessageReceiver = new DestructionReceiver(adapter);
		getActivity().registerReceiver(this.mHandleMessageReceiver, 
				new IntentFilter(CommonUtilities.BROADCAST_DESTRUCTIONS));
		Log.v("NLF", "Registered Receiver");
		
		return rootView;
	}
	
	public void loadStuff(View view) {
		Log.v("IRIS", "Load stuff!");
	}
}