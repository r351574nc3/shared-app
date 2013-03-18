package de.tinloaf.iris.mobileapp.rest;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import de.tinloaf.iris.mobileapp.rest.RESTClient.RESTClientListenener;

public abstract class ApiInterface  {
	public static final int REQUEST_GET = 0;
	public static final int REQUEST_PUT = 1;
	
	private ApiInterfaceEventListener mListener;
	private String user;
	private String apiKey;
	
	public ApiInterface(ApiInterfaceEventListener listener, Context ctx) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		this.user = settings.getString("pref_username", null);
		this.apiKey = settings.getString("pref_apikey", null);
		this.mListener = listener;
	}
	
	public ApiInterface(String user, String apiKey, ApiInterfaceEventListener listener) {
		super();
		
		this.mListener = listener;
		this.user = user;
		this.apiKey = apiKey;
	};
		
	private class TaskRunner extends AsyncTask<Object, Object, ApiInterface.CallResult> 
			implements RESTClientListenener {
		private CallResult failureResult = null;
		private RESTClient client;
		
		public TaskRunner(String user, String apiKey) {
			this.client = new RESTClient(user, apiKey, this);
		}
		
		@Override
		protected CallResult doInBackground(Object... params) {
			try {
				// param 0 is kind of request
				Integer type = (Integer) params[0];
				
				// URL is param 1
				String url = (String)params[1];
				
				JSONObject json = null;
	
				switch (type) {
				case ApiInterface.REQUEST_GET:
					json = client.get(url);
					break;
				case ApiInterface.REQUEST_PUT:
					json = client.put(url, (JSONObject)params[2]);
					break;
				}
				
				if (this.failureResult != null) {
					return this.failureResult;
				}
				
				CallResult result = new CallResult();
				
				if (json.getString("status").equals("ok")) {
					if (json.has("data")) {
						result.data = json.getJSONArray("data");
					} else {
						result.data = new JSONArray();
					}
					result.type = type;
					result.status = CallResult.STATUS_OK;
				} else {
					result.status = CallResult.STATUS_ERROR;
					if (json.has("msg")) {
						result.msg = json.getString("msg");
					}
				}
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (this.failureResult != null) {
				return this.failureResult;
			} else {
				return new CallResult();
			}
		}
		
		protected void onPutDone() {
			mListener.onPutDone();
		};
		
		@Override
		protected void onPostExecute(CallResult result) {
			// Call the handlers in the UI thread in case we screwed up
			if (result.status == CallResult.STATUS_LOGIN_FAILED) {
				mListener.onLoginFailed();
				return;
			}
			
			if (result.status == CallResult.STATUS_ERROR) {
				mListener.onError(result.msg);
			}
			
			switch (result.type) {
			case ApiInterface.REQUEST_GET:
				handleData(result.data);
				break;
			case ApiInterface.REQUEST_PUT:
				// TODO status code validation
				this.onPutDone();
			}
		}

		@Override
		public void onUnauthorized() {
			this.failureResult = new CallResult();
			this.failureResult.status = CallResult.STATUS_LOGIN_FAILED;
		}
	}
	
	public void execute(Object... params) {
		
		TaskRunner task = new TaskRunner(this.user, this.apiKey);
		task.execute(params);
	}
	
	public class CallResult {
		public static final int STATUS_OK = 0;
		public static final int STATUS_LOGIN_FAILED = 1;
		public static final int STATUS_ERROR = 2;
		
		int status = 2;
		JSONArray data;
		int type;
		String msg;
	}

	protected abstract void handleData(JSONArray data);
	
	public interface ApiInterfaceEventListener {
		public void onLoadDone(ApiInterface apiInterface);
		public void onLoginFailed();
		public void onPutDone();
		public void onError(String message);
	}
	
}
