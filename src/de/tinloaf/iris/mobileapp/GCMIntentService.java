package de.tinloaf.iris.mobileapp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.data.Destruction;
import de.tinloaf.iris.mobileapp.data.SavedPortal;
import de.tinloaf.iris.mobileapp.rest.ApiInterface;
import de.tinloaf.iris.mobileapp.rest.PortalFetcher;
import de.tinloaf.iris.mobileapp.rest.RESTClient;

public class GCMIntentService extends GCMBaseIntentService implements RESTClient.RESTFailureListener {
	static final int TYPE_DESTR = 0;
	
	private DatabaseHelper databaseHelper = null;
	private RESTClient restClient;

	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferences settings = this.getSharedPreferences("iris", 0);
		this.restClient = new RESTClient(settings.getString("username", "null"), 
					settings.getString("apikey", null), this);
	}
	
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
	            OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }
	    return databaseHelper;
	}
	
    private static void generateNotification(Context context) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        String message = "Here be the attacked portals";
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);

    }

	
	private void displayDestructions(JSONArray destructions) {
		Log.v("GCM", "Called displayDestruction");
		
		Intent sendDestrIntent = new Intent(CommonUtilities.BROADCAST_DESTRUCTIONS);
		Dao<Destruction, Integer> destructionDao;
		try {
			destructionDao = getHelper().getDestructionDao();
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
		ArrayList<Integer> destrIds = new ArrayList<Integer>(destructions.length());
		for (int i = 0; i < destructions.length(); i++) {
			JSONObject cur;
			try {
				Destruction destr = new Destruction();
				cur = destructions.getJSONObject(i);
				
				destr.portalId = cur.getInt("portal");
				if (! cur.isNull("portal_end")) {
					destr.portalEndId = cur.getInt("portal_end");
				} else {
					destr.portalEndId = -1;
				}
				// TODO make that string work
				//parcels[i].attacker = fields.getString("attacker");
				destr.kind = cur.getInt("kind");
				destr.count = cur.getInt("count");
				destr.attacker = cur.getString("attacker");
				int timestamp = cur.getInt("date");
				destr.time = new java.util.Date(timestamp * 1000);
				
				destructionDao.create(destr);
				destrIds.add(destr.id);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
			// TODO Auto-generated c	atch block
				e.printStackTrace();
			}
		}
		
		Log.v("GCM", "Sending Destruction Broadcast");
		sendDestrIntent.putIntegerArrayListExtra("DESTR_IDS", destrIds);
		this.sendBroadcast(sendDestrIntent);
	}
	
	private void handleDestructions(String destructions) {
		Log.v("GCM", "Called handleDestructions");
		
		class PortalLoadListener implements ApiInterface.ApiInterfaceEventListener {
			JSONArray destructionArray;
			
			public PortalLoadListener(JSONArray destructionArray) {
				this.destructionArray = destructionArray;
			}
			
			@Override
			public void onLoadDone(ApiInterface apiInterface) {
				PortalFetcher portalFetcher = (PortalFetcher) apiInterface;
				
				// Insert them into the database
				DatabaseHelper helper = getHelper();
				Dao<SavedPortal, Integer> portalDao = null;
				try {
					portalDao = helper.getPortalDao();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				List<PortalFetcher.PortalData> portals = portalFetcher.getPortals();
				Iterator<PortalFetcher.PortalData> it = portals.iterator();
				while (it.hasNext()) {
					PortalFetcher.PortalData portalData = it.next();
					
					SavedPortal savedPortal = new SavedPortal();
					savedPortal.id = portalData.id;
					savedPortal.title = portalData.title;
					savedPortal.description = portalData.description;
					savedPortal.address = portalData.address;
					savedPortal.lat = portalData.lat;
					savedPortal.lng = portalData.lng;
					savedPortal.subscription = portalData.subscription;
					savedPortal.imgUrl = portalData.imgUrl;
					
					try {
						portalDao.create(savedPortal);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						// TODO what if already there?
						e.printStackTrace();
					}
				}
				
				displayDestructions(this.destructionArray);
				generateNotification(GCMIntentService.this);
			}
			
		}
		try {
			JSONArray destrAr = new JSONArray(destructions);
			DatabaseHelper helper = getHelper();
			Dao<SavedPortal, Integer> portalDao = null;
			portalDao = helper.getPortalDao();
			
			// Make sure we have all the portals loaded
			List<Integer> unknownPortals = new LinkedList<Integer>();
			boolean oneUnknown = false;
			
			for (int i = 0; i < destrAr.length(); i++) {
				JSONObject cur = destrAr.getJSONObject(i);
				
				int portal_id = cur.getInt("portal");
				if (!portalDao.idExists(portal_id)) {
					unknownPortals.add(portal_id);
					oneUnknown = true;
				}
				
				if (! cur.isNull("portal_end")) {
					int portal_end_id = cur.getInt("portal_end");
					if (!portalDao.idExists(portal_end_id)) {
						unknownPortals.add(portal_end_id);
						oneUnknown = true;
					}
				}
			}
			
			if (oneUnknown) {
				PortalFetcher portalFetcher = new PortalFetcher(this.restClient, new PortalLoadListener(destrAr));
				
				portalFetcher.load(unknownPortals);
			} else {
				this.displayDestructions(destrAr);
				generateNotification(this);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();			
		} 
	}
	
	@Override
	protected void onError(Context ctx, String errId) {
		// TODO Auto-generated method stub
		System.out.println(errId);
	}

	@Override
	protected void onMessage(Context ctx, Intent msg) {
		// TODO Why is this a string?
		int type = Integer.parseInt(msg.getExtras().getString("type"));
		
		Log.v("THEBUNDLE", msg.getExtras().toString());
		
		switch (type) {
		case TYPE_DESTR:
			this.handleDestructions(msg.getExtras().getString("destructions"));
			break;
		}
	}

	@Override
	protected void onRegistered(Context ctx, String regId) {
		// TODO Auto-generated method stub
		System.out.println(regId);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoginFailed() {
		// TODO IMPLEMENT ME
		
	}

}
