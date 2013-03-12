package de.tinloaf.iris.mobileapp.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
 
public class RESTClient {
	private String user;
	private String apiKey;
	private DefaultHttpClient client;
	private RESTFailureListener failureListener;
	
	public RESTClient(String user, String apiKey, RESTFailureListener failureHandler) {
		this.user = user;
		this.apiKey = apiKey;
		this.client = new DefaultHttpClient();
		Credentials creds = new UsernamePasswordCredentials(user, apiKey);
		client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
		this.failureListener = failureHandler;
	}
	
	public interface RESTFailureListener {
		public void onLoginFailed();
	}
	
    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
 
    private JSONArray execute(HttpRequestBase request) {
        // Execute the request
        HttpResponse response;
        try {
            response = this.client.execute(request);
            // Examine the response status
            Log.i("REST",response.getStatusLine().toString());
            
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            	Log.i("REST", "Trying to reauthenticate.");
            	this.failureListener.onLoginFailed();
            	return null;
            }
 
            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release
 
            if (entity != null) {
 
                InputStream instream = entity.getContent();
                String result= convertStreamToString(instream);
                Log.i("TOJSONARRAY",result);
 
                // TODO give back everything?
                JSONArray json = new JSONArray(result);
                
                return json;
            } else {
            	return new JSONArray();
            }
            
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
		return new JSONArray();    	
    }
    
    public void put(String url, JSONObject json) {
        // Prepare a request object
        HttpPut httpput = new HttpPut(url); 
        
        httpput.addHeader("Content-Type", "application/json");
        httpput.addHeader("Accept", "application/json");
        
        try {
			httpput.setEntity(new StringEntity(json.toString()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        this.execute(httpput);
    }
    
    public JSONArray get(String url)
    { 
        // Prepare a request object
        HttpGet httpget = new HttpGet(url); 
 
        return this.execute(httpget);
   }
 
}