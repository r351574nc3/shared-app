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
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.data.Destruction;
import de.tinloaf.iris.mobileapp.data.SavedPortal;
import de.tinloaf.iris.mobileapp.rest.ApiInterface;
import de.tinloaf.iris.mobileapp.rest.PortalFetcher;

public class GCMIntentService extends GCMBaseIntentService {
	static final int TYPE_DESTR = 0;
	
	private DatabaseHelper databaseHelper = null;

	@Override
	public void onCreate() {
		super.onCreate();
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
	
	private static int NOTIFICATION_ID = 1;
	
	// TODO why is this static?
    private static void generateNotification(Context context) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        String message = "Here be the attacked portals";
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        int defaults = 0;
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        
        boolean wantLights = settings.getBoolean("pref_notify_lights", true);
        boolean wantSound = settings.getBoolean("pref_nofity_sound", true);
        boolean wantVibrate = settings.getBoolean("pref_notify_vibrate", true);
        
        if (wantLights) 
        	defaults |= Notification.DEFAULT_LIGHTS;
        
        if (wantSound)
        	defaults |= Notification.DEFAULT_SOUND;
        
        if (wantVibrate)
        	defaults |= Notification.DEFAULT_VIBRATE;
        
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
        .setContentTitle("I.R.I.S. Attack")
        .setContentText("Attack on portal titlegoeshere.")
        .setSmallIcon(R.drawable.ic_launcher)
        .setDefaults(defaults);
        
        notificationManager.notify(
        		NOTIFICATION_ID,
        		mBuilder.build());

    }

	
	private void displayDestructions(JSONArray destructions) {
		Intent sendDestrIntent = new Intent(CommonUtilities.getBroadcastDestructions());
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
				destr.time = new java.util.Date((long)timestamp * 1000);
				
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

			@Override
			public void onLoginFailed() {
				// TODO DO SOMETHING!
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
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

				PortalFetcher portalFetcher = new PortalFetcher(settings.getString("pref_username", ""),
						settings.getString("pref_apikey", ""), new PortalLoadListener(destrAr));
				
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
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if ((!settings.contains("gcm_key_sent")) || 
				(!settings.getString("gcm_key_sent", null).equals(GCMRegistrar.getRegistrationId(this)))) {
			// Store the key that is obviously working
			Editor editor = settings.edit();
			editor.putString("gcm_key_sent", GCMRegistrar.getRegistrationId(this));
			editor.commit();
		}
		
		switch (type) {
		case TYPE_DESTR:
			this.handleDestructions(msg.getExtras().getString("destructions"));
			break;
		}
	}

	@Override
	protected void onRegistered(Context ctx, String regId) {
		// TODO send the key!
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
}
