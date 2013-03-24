package de.tinloaf.iris.mobileapp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import android.support.v4.app.NotificationCompat.Style;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.data.Destruction;
import de.tinloaf.iris.mobileapp.data.SavedPortal;
import de.tinloaf.iris.mobileapp.data.SavedPortalImage;
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
        Log.v("GIS", "Service shutting down");
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
    private void generateNotification(Context context, List<Destruction> destrList) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        int defaults = 0;

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("IS_NOTIFICATION", true);
        PendingIntent pIntent = PendingIntent.getActivity(context, 1, intent, 0);
     
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        DatabaseHelper helper = getHelper();
        Dao<SavedPortal, Integer> portalDao = null;
        Dao<Destruction, Integer> destrDao = null;
        try {
            portalDao = helper.getPortalDao();
            destrDao = helper.getDestructionDao();
            
            QueryBuilder queryBuilder = destrDao.queryBuilder();
            queryBuilder.where().eq("displayed", false);
            queryBuilder.orderBy("time", false);

            List<Destruction> destructions = destrDao.query(queryBuilder.prepare());

            String title = " Attacks";
            String message = "1";
            Iterator it = destructions.iterator();
            int titled = 0;
            int untitled = 0;
            List<Integer> orderedPortalIds = new LinkedList<Integer>();
            Map<Integer, Integer> destrCounts = new HashMap<Integer,Integer>();
            // Map portal IDs to number of destructions
            int totalAttacks = 0;
            while (it.hasNext()) {
                Destruction cur = (Destruction) it.next();
                
                totalAttacks += 1;
                
                if (destrCounts.containsKey(cur.portalId)) {
                    destrCounts.put(cur.portalId, destrCounts.get(cur.portalId) + cur.count);
                } else {
                    destrCounts.put(cur.portalId, cur.count);
                    orderedPortalIds.add(cur.portalId);
                }
            }
            
            
            // Now, construct the messages in portal order
            it = orderedPortalIds.iterator();
            while (it.hasNext()) {
                Integer portalId = (Integer) it.next();
                SavedPortal portal;
                portal = portalDao.queryForId(portalId);

                if (portal.title != null) {
                    if (titled == 0) {
                        title += ": " + destrCounts.get(portalId) + "@" + portal.title;
                    } else {
                        if (titled == 1) {
                            title += ", ...";
                            message += destrCounts.get(portalId) + "@" + portal.title;                      
                        } else {
                            message += ", " + destrCounts.get(portalId) + "@" + portal.title;
                        }
                    }
                    titled++;
                } else {
                    untitled++;
                }
            }

            if (untitled > 0) {
                if (titled > 1) {
                    message += " +";
                }
                message += Integer.toString(untitled) + " untitled";
            }
            
            title = Integer.toString(totalAttacks) + title;

            boolean wantLights = settings.getBoolean("pref_notify_lights", true);
            boolean wantSound = settings.getBoolean("pref_notifiy_sound", true);
            boolean wantVibrate = settings.getBoolean("pref_notify_vibrate", true);

            if (wantLights) 
                defaults |= Notification.DEFAULT_LIGHTS;
            else 
                defaults &= ~Notification.DEFAULT_LIGHTS;

            if (wantSound)
                defaults |= Notification.DEFAULT_SOUND;
            else 
                defaults &= ~Notification.DEFAULT_SOUND;

            if (wantVibrate)
                defaults |= Notification.DEFAULT_VIBRATE;
            else 
                defaults &= ~Notification.DEFAULT_VIBRATE;


            Style bigStyle = null; //new NotificationCompat.BigTextStyle().bigText(message);
            
            Intent deleteIntent = new Intent(CommonUtilities.getBroadcastClearnotification());
            PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, 2, deleteIntent, 0);
            
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher)
            .setDefaults(defaults)
            .setContentIntent(pIntent)
            .setAutoCancel(true)
            .setStyle(bigStyle)
            .setDeleteIntent(pDeleteIntent);

            /*
            notificationManager.notify(
                    NOTIFICATION_ID,
                    mBuilder.build());
            */

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void displayDestructions(List<Destruction> destructions) {
        ArrayList<Integer> destrIds = new ArrayList<Integer>();
        Intent sendDestrIntent = new Intent(CommonUtilities.getBroadcastDestructions());
        
        for (final Destruction cur : destructions) {
            destrIds.add(cur.id);
        }
        
        sendDestrIntent.putIntegerArrayListExtra("DESTR_IDS", destrIds);
        this.sendBroadcast(sendDestrIntent);
    }
    
    private List<Destruction> createDestructions(JSONArray destructions) {
        Dao<Destruction, Integer> destructionDao;
        List<Destruction> destrList = new LinkedList<Destruction>();
        
        try {
            destructionDao = getHelper().getDestructionDao();
        } catch (SQLException e1) {
            e1.printStackTrace();
            return null;
        }
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
                destrList.add(destr);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SQLException e) {
            // TODO Auto-generated c    atch block
                e.printStackTrace();
            }
        }
        
        return destrList;
    }
    
    private void handleDestructions(String destructions) {
        
        class PortalLoadListener implements ApiInterface.ApiInterfaceEventListener {
            JSONArray destructionArray;
            
            public PortalLoadListener(JSONArray destructionArray) {
                this.destructionArray = destructionArray;
            }
            
            @Override
            public void onLoadDone(ApiInterface apiInterface) {
                // portals should have been loaded to the database
                
                List<Destruction> destrList = GCMIntentService.this.createDestructions(this.destructionArray);
                
                displayDestructions(destrList);
                generateNotification(GCMIntentService.this, destrList);
            }

            @Override
            public void onLoginFailed() {
                // TODO DO SOMETHING!
            }

            @Override
            public void onPutDone() {
                // Nothing to do here. :)
            }

            @Override
            public void onError(String message) {
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
                long portal_timestamp = Long.parseLong(cur.getString("portal_stamp"));
                if (!portalDao.idExists(portal_id)) {
                    unknownPortals.add(portal_id);
                    oneUnknown = true;
                } else {
                    // TODO we're retrieving them twice...fix tihs.
                    SavedPortal portal = portalDao.queryForId(portal_id);
                    if (portal.stamp < portal_timestamp) {
                        Log.v("GIS", "Trying to reload outdated portal data");
                        // WARNING Do not delete that portal here! If reloading fails,
                        // we have destructions refering to a not loaded portal. That is bad.
                        unknownPortals.add(portal_id);
                        oneUnknown = true;
                    }
                }
                
                if (! cur.isNull("portal_end")) {
                    int portal_end_id = cur.getInt("portal_end");
                    long portal_end_timestamp = Long.parseLong(cur.getString("portal_end_stamp"));
                    if (!portalDao.idExists(portal_end_id)) {
                        unknownPortals.add(portal_end_id);
                        oneUnknown = true;
                    } else {
                        // TODO we're retrieving them twice...fix tihs.
                        SavedPortal portal = portalDao.queryForId(portal_end_id);
                        if (portal.stamp < portal_end_timestamp) {
                            portalDao.delete(portal);
                            unknownPortals.add(portal_end_id);
                            oneUnknown = true;
                        }
                    }
                }
            }
            
            if (oneUnknown) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

                PortalFetcher portalFetcher = new PortalFetcher(settings.getString("pref_username", ""),
                        settings.getString("pref_apikey", ""), new PortalLoadListener(destrAr));
                
                portalFetcher.loadToDatabase(unknownPortals, this);
            } else {
                List<Destruction> destrList = this.createDestructions(destrAr);
                this.displayDestructions(destrList);
                generateNotification(this, destrList);
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
