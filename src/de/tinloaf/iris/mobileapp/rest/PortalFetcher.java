package de.tinloaf.iris.mobileapp.rest;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import de.tinloaf.iris.mobileapp.CommonUtilities;

public class PortalFetcher extends ApiInterface {

	private ApiInterfaceEventListener mListener;
	private List<PortalData> portals;
	
	public class PortalData {
	    public Integer id;
		public String title;
		public String description;
		public String address;
		public Integer subscription;
		public Double lat;	
		public Double lng;
		public String imgUrl;
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
		String url = CommonUtilities.SERVER_BASE_URL + "portal/";
		url = this.addIdsToURL(url, ids);
		
		this.execute(ApiInterface.REQUEST_GET, url);
	}
	
	public List<PortalData> getPortals() {
		return this.portals;
	}
	
	public PortalFetcher(RESTClient client, ApiInterfaceEventListener mListener) {
		super(client);
		this.mListener = mListener;
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
				pd.address = cur.getString("address");
				pd.description = cur.getString("description");
				Log.v("PF", "Fetched imgUrl: " + cur.getString("img_url"));
				pd.imgUrl = cur.getString("img_url");
				pd.lat = cur.getDouble("x");
				pd.lng = cur.getDouble("y");
				pd.subscription = -1; // Not supported for now.
				
				this.portals.add(pd);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		this.mListener.onLoadDone(this);
	}

}
