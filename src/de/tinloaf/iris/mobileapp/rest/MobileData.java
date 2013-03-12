package de.tinloaf.iris.mobileapp.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.tinloaf.iris.mobileapp.CommonUtilities;

public class MobileData extends ApiInterface {
	private String gcmKey;

	private ApiInterfaceEventListener mListener;
	
	public MobileData(RESTClient client, ApiInterfaceEventListener mListener) {
		super(client);
		this.mListener = mListener;
	}
	
	public void load() {
		String url = CommonUtilities.SERVER_BASE_URL + "mdata/";
		this.execute(ApiInterface.REQUEST_GET, url);
	}
	
	public void setGcmKey(String gcmKey) {
		this.gcmKey = gcmKey;
	}
	
	public void save() {
		JSONObject json = new JSONObject();
		try {
			json.put("gcm_key", this.gcmKey);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.v("SENT_JSON", json.toString());
		
		String url = CommonUtilities.SERVER_BASE_URL + "mdata/";
		this.execute(ApiInterface.REQUEST_PUT, url, json);
	}

	@Override
	protected void handleData(JSONArray data) {
		if (data == null) {
			// Something went wrong
			return;
		}
		
		if (data.length() == 0) {
			return;
		}
		
		try {
			this.gcmKey = data.getJSONObject(0).getString("gcm_key");
			Log.v("DAta", this.gcmKey);
			Log.v("DAta", "Initialized.");			
		} catch (JSONException e) {
			this.gcmKey = null;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.mListener.onLoadDone(this);
	}
}
