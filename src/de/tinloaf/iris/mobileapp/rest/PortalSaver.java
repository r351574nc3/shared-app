package de.tinloaf.iris.mobileapp.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import de.tinloaf.iris.mobileapp.CommonUtilities;
import de.tinloaf.iris.mobileapp.data.SavedPortal;

public class PortalSaver extends ApiInterface {
	
	private ApiInterfaceEventListener mListener;
	private SavedPortal portal;
	
	public PortalSaver(SavedPortal portal, Context ctx, ApiInterfaceEventListener mListener) {		
		super(mListener, ctx);
		this.mListener = mListener;
		this.portal = portal;
	}
	
	public void save() {
		JSONObject json = new JSONObject();
		try {
			json.put("portal_id", this.portal.id);
			if (this.portal.title != null) 
				json.put("portal_title", Base64.encodeToString(this.portal.title.getBytes(), Base64.DEFAULT));
			if (this.portal.address != null) 
				json.put("portal_address", Base64.encodeToString(this.portal.address.getBytes(), Base64.DEFAULT));
			if (this.portal.description != null) 
				json.put("portal_descr", Base64.encodeToString(this.portal.description.getBytes(), Base64.DEFAULT));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String url = CommonUtilities.getServerBaseUrl() + "portal/";
		this.execute(ApiInterface.REQUEST_PUT, url, json);
	}
	
	@Override
	protected void handleData(JSONArray data) {
	}
	
	protected void onPutDone() {
		
	}

}
