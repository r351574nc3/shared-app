package de.tinloaf.iris.mobileapp.rest;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import de.tinloaf.iris.mobileapp.CommonUtilities;
import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.data.SavedPortal;

public class PortalFetcher extends ApiInterface {

	private ApiInterfaceEventListener mListener;
	private List<PortalData> portals;
	
	private Context ctx = null;
	private boolean toDatabase = false;
	
	public class PortalData {
	    public Integer id;
		public String title;
		public String description;
		public String address;
		public Integer subscription;
		public Double lat;	
		public Double lng;
		public String imgUrl;
		public Long stamp;
	}
	
	private String addIdsToURL(String url, List<Integer> ids) {
	    if(!url.endsWith("?"))
	        url += "?";

	    String paramString = "ids=";
	    
	    Iterator<Integer> it = ids.iterator();
	    while (it.hasNext()) {
	    	Integer cur = it.next();
	    	paramString += cur.toString();
	    	if (it.hasNext()) {
	    		paramString += ",";
	    	}
	    }
	    
	    url += paramString;
	    return url;	    
	}
	
	public void load(List<Integer> ids) {
		String url = CommonUtilities.getServerBaseUrl() + "portal/";
		url = this.addIdsToURL(url, ids);
		
		this.toDatabase = false;
		this.execute(ApiInterface.REQUEST_GET, url);
	}
	
	public void loadToDatabase(List<Integer> ids, Context ctx) {
		String url = CommonUtilities.getServerBaseUrl() + "portal/";
		url = this.addIdsToURL(url, ids);
		
		this.toDatabase = true;
		this.ctx = ctx;
		
		this.execute(ApiInterface.REQUEST_GET, url);
	}
	
	public List<PortalData> getPortals() {
		return this.portals;
	}
	
	public PortalFetcher(ApiInterfaceEventListener mListener, Context ctx) {
		super(mListener, ctx);
		this.ctx = ctx;
		this.mListener = mListener;
	}
	
	public PortalFetcher(String user, String apiKey, ApiInterfaceEventListener mListener) {
		super(user, apiKey, mListener);
		this.mListener = mListener;
	}
	
	private void writeToDatabase() {
		DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this.ctx, DatabaseHelper.class);
		try {
			Dao<SavedPortal, Integer> portalDao = databaseHelper.getPortalDao();
			
			Iterator<PortalData> it = this.portals.iterator();
			
			while (it.hasNext()) {
				PortalData portalData = it.next();
				
				if (portalDao.idExists(portalData.id)) {
					// Delete and re-insert that record
					portalDao.deleteById(portalData.id);
				}
				
				// Insert the new portal
				SavedPortal savedPortal = new SavedPortal();
				savedPortal.id = portalData.id;
				savedPortal.title = portalData.title;
				savedPortal.description = portalData.description;
				savedPortal.address = portalData.address;
				savedPortal.lat = portalData.lat;
				savedPortal.lng = portalData.lng;
				savedPortal.subscription = portalData.subscription;
				savedPortal.imgUrl = portalData.imgUrl;
				savedPortal.stamp = portalData.stamp;				// Insert the new portal
				
				portalDao.create(savedPortal);
				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		OpenHelperManager.releaseHelper();
	}

	@Override
	protected void handleData(JSONArray data) {
		if (data == null) {
			return;
		}
		this.portals = new LinkedList<PortalData> ();
		
		for (int i = 0; i < data.length(); i++) {
			try {
				JSONObject cur = data.getJSONObject(i);
				
				PortalData pd = new PortalData();
				pd.id = cur.getInt("id");
				pd.title = cur.getString("title");
				if (pd.title.equals("null")) {
					pd.title = null;
				}
				pd.address = cur.getString("address");
				if (pd.address.equals("null")) {
					pd.address = null;
				}
				pd.description = cur.getString("description");
				if (pd.description.equals("null")) {
					pd.description = null;
				}
				pd.imgUrl = cur.getString("img_url");
				if (pd.imgUrl.equals("null")) {
					pd.imgUrl = null;
				}
				pd.lat = cur.getDouble("x");
				pd.lng = cur.getDouble("y");
				pd.subscription = -1; // Not supported for now.
				pd.stamp = cur.getLong("stamp");
				
				this.portals.add(pd);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		if (this.toDatabase) {
			this.writeToDatabase();
		}
		
		this.mListener.onLoadDone(this);
	}
	
}
