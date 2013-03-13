package de.tinloaf.iris.mobileapp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.data.Destruction;
import de.tinloaf.iris.mobileapp.data.SavedPortal;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class NotificationListFragment extends SherlockListFragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	private DatabaseHelper databaseHelper = null;

    private BroadcastReceiver mHandleMessageReceiver;
    private NotificationAdapter adapter;
    private ArrayList<NotificationData> notificationData;
    
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	    
	    getActivity().unregisterReceiver(this.mHandleMessageReceiver);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("NOTIFICATION_LIST", this.notificationData);
	}

	private DatabaseHelper getHelper() {
	    if (databaseHelper == null) {
	        databaseHelper =
	            OpenHelperManager.getHelper(this.getActivity(), DatabaseHelper.class);
	    }
	    return databaseHelper;
	}

	protected NotificationData destructionToNotification(Destruction destruction) throws SQLException {
		// TODO put this into destruction? Or NotificationData?
		
		// Get the portal
		Dao<SavedPortal, Integer> portalDao = getHelper().getPortalDao();
		
		SavedPortal portal = portalDao.queryForId(destruction.portalId);
		// TODO what if not there?
		
		String destrString = Integer.toString(destruction.count) + " ";
		switch (destruction.kind) {
		case Destruction.KIND_LINK:
			destrString += "link(s)";
			break;
		case Destruction.KIND_RESONATOR:
			destrString += "resonator(s)";
			break;
		case Destruction.KIND_MOD:
			destrString += "mod(s)";
			break;
		case Destruction.KIND_FIELD:
			destrString += "field(s)";
			break;
		}
		
		Date date = destruction.time;
		
		NotificationData notification = new NotificationData(portal, destrString, "", date);
		return notification;
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
	        	ArrayList<Integer> destrIds = intent.getIntegerArrayListExtra("DESTR_IDS");

	        	Dao<Destruction, Integer> destrDao = getHelper().getDestructionDao();
	        	QueryBuilder<Destruction, Integer> destrQueryBuilder = destrDao.queryBuilder();
	        	destrQueryBuilder.where().in("id", destrIds);
	        	List<Destruction> destructions = destrDao.query(destrQueryBuilder.prepare());
	        	
	        	Iterator<Destruction> it = destructions.iterator();
	        	while (it.hasNext()) {
	        		Destruction cur = it.next();
	        		NotificationData notification = destructionToNotification(cur);
	        		this.adapter.insert(notification, 0);
	        	}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
    };
	
    @Override
    public void onDetach() {
    	super.onDetach();
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		if ((savedInstanceState != null) && (savedInstanceState.containsKey("NOTIFICATION_LIST"))) {
			this.notificationData = savedInstanceState.getParcelableArrayList("NOTIFICATION_LIST");
		} else {
			this.notificationData = new ArrayList<NotificationData>();
			/* Get from the database */
			try {
				Dao<Destruction, Integer> destrDao = getHelper().getDestructionDao();
				QueryBuilder destrQueryBuilder = destrDao.queryBuilder();
				destrQueryBuilder.orderBy("time", false).limit(20); // TODO make this a constant somewhere
				List<Destruction> destructions = destrDao.query(destrQueryBuilder.prepare());
				
				Iterator<Destruction> it = destructions.iterator();
				while (it.hasNext()) {
					Destruction cur = it.next();
					
					NotificationData notification = destructionToNotification(cur);
					this.notificationData.add(notification);
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
        this.adapter = new NotificationAdapter(this.getActivity(),
        		R.layout.notification_list_item, this.notificationData, this.getHelper());
        setListAdapter(this.adapter);
        
        this.mHandleMessageReceiver = new DestructionReceiver(adapter);
        	
        getActivity().registerReceiver(this.mHandleMessageReceiver, 
        		new IntentFilter(CommonUtilities.getBroadcastDestructions()));
        
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_notification_list, container, false);		
	}	
}