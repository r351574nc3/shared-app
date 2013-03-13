package de.tinloaf.iris.mobileapp.rest;

import org.json.JSONArray;
import org.json.JSONObject;

import de.tinloaf.iris.mobileapp.rest.RESTClient.RESTClientListenener;

import android.os.AsyncTask;
import android.util.Log;

public abstract class ApiInterface  {
	public static final int REQUEST_GET = 0;
	public static final int REQUEST_PUT = 1;
	
	private ApiInterfaceEventListener mListener;
	private String user;
	private String apiKey;
	
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
				
				JSONArray json = null;
	
				switch (type) {
				case ApiInterface.REQUEST_GET:
					json = client.get(url);
					break;
				case ApiInterface.REQUEST_PUT:
					client.put(url, (JSONObject)params[2]);
					break;
				}
				
				if (this.failureResult != null) {
					return this.failureResult;
				}
				
				CallResult result = new CallResult();
				result.data = json;
				result.type = type;
				
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
		};
		
		@Override
		protected void onPostExecute(CallResult result) {
			// Call the handlers in the UI thread in case we screwed up
			if (result.status == CallResult.STATUS_LOGIN_FAILED) {
				mListener.onLoginFailed();
				return;
			}
			
			switch (result.type) {
			case ApiInterface.REQUEST_GET:
				handleData(result.data);
				break;
			case ApiInterface.REQUEST_PUT:
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
		
		int status;
		JSONArray data;
		int type;
	}

	protected abstract void handleData(JSONArray data);
	
	public interface ApiInterfaceEventListener {
		public void onLoadDone(ApiInterface apiInterface);
		public void onLoginFailed();
	}
	
}
