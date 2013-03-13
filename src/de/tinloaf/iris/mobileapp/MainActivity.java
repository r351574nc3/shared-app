package de.tinloaf.iris.mobileapp;


import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import de.tinloaf.iris.mobileapp.data.CleanupService;
import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.rest.ApiInterface;
import de.tinloaf.iris.mobileapp.rest.MobileData;
import de.tinloaf.iris.mobileapp.rest.RESTClient;

public class MainActivity extends SherlockFragmentActivity implements
		ActionBar.OnNavigationListener, LoginDialogFragment.LoginDialogListener,
		ApiInterface.ApiInterfaceEventListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	private RESTClient restClient;
	private MobileData md;
	
	
	private DatabaseHelper databaseHelper = null;

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    Log.v("MAIN", "onDestroy()");
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	}

	private void setupCleanup() {
		Intent startCleanupIntent = new Intent(this, CleanupService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, startCleanupIntent, PendingIntent.FLAG_NO_CREATE);
		
		if (pi == null) {
			AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			
			// No intent pending, set one.
			pi = PendingIntent.getService(this, 0, startCleanupIntent, 0);
			
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 
					CommonUtilities.getCleanupSeconds() * 1000, pi);
			
			Log.v("MAIN", "Set Alarm");
		}
	}
	
	private DatabaseHelper getHelper() {
	    if (databaseHelper == null) {
	        databaseHelper =
	            OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }
	    return databaseHelper;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    Log.v("MAIN", "onCreate()");
		setContentView(R.layout.activity_main);

		// Setup cleanup service
		setupCleanup();
		
		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(getActionBarThemedContextCompat(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] {
								getString(R.string.title_section1),}), this);
		
		
		
		this.initRest();
		this.initGCM();
	}
	
	

	/**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
	 * simply returns the {@link android.app.Activity} if
	 * <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getActionBar().getThemedContext();
		} else {
			return this;
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getSupportActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar()
				.getSelectedNavigationIndex());
	}

	public void onSettingsClicked(MenuItem item) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		if (getSupportFragmentManager().findFragmentByTag("NOTIFICATIONLIST_FRAGMENT") == null) {
			SherlockListFragment fragment = new NotificationListFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.container, fragment, "NOTIFICATIONLIST_FRAGMENT");
			
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, fragment, "NOTIFICATIONLIST_FRAGMENT").commit();		
		}
		
		
		return true;		
	}


	private void showLoginDialog() {
		if (getSupportFragmentManager().findFragmentByTag("LoginDialogFragment") == null) {
			
			getSupportFragmentManager().beginTransaction()
            	.add(new LoginDialogFragment(), "LoginDialogFragment")
            	.commit();
			// make it not return null anymore
			getSupportFragmentManager().executePendingTransactions();
			Log.v("MAIN", "Prompting for login");
			
		}
	}
	
	private void initGCM() {
		// Set up GCM
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
		  GCMRegistrar.register(this, CommonUtilities.getGoogleProjectId());
		}
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		// See if we already sent this key...
		if ((!settings.contains("gcm_key_sent")) ||
				(! settings.getString("gcm_key_sent", null).equals(GCMRegistrar.getRegistrationId(this)))) {
			// Remove it, just to be clear..
			
			Editor editor = settings.edit();
			editor.remove("gcm_key_sent");
			editor.commit();
			
			// Send the GCM key to the server
			this.md = new MobileData(settings.getString("pref_username", ""),
					settings.getString("pref_apikey", ""), this);
			md.load(); // will call onLoadDone and send the GCM key
		}
	};
	
	private void initRest() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if ((! settings.contains("pref_username")) ||
				(!settings.contains("pref_apikey"))) {
			showLoginDialog();
		}
	}

	@Override
	public void onDialogPositiveClick(String username, String apikey) {
 	   // Verify that it works and re-associate
 	   
	   this.md = new MobileData(username, apikey, this);
	   md.load();
	   
	   // TODO make sure stuff worked
	   
	   SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
	   SharedPreferences.Editor editor = settings.edit();
	   editor.putString("pref_username", username);
	   editor.putString("pref_apikey", apikey);
	   editor.commit();
	}



	@Override
	public void onDialogNegativeClick() {
		initRest();
	}



	@Override
	public void onLoginFailed() {
		// Delete API key and username and show dialog
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove("pref_apikey");
		editor.remove("pref_username");
		editor.commit();
		
		// Show dialog, lets build a new rest client.
		this.initRest();
	}



	// Load of MobileData done...
	@Override
	public void onLoadDone(ApiInterface apiInterface) {
		Log.v("MAIN", "Sending GCM Key");
		this.md.setGcmKey(GCMRegistrar.getRegistrationId(this));
		this.md.save();
	}

}
