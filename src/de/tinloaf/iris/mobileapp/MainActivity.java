package de.tinloaf.iris.mobileapp;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;

import com.google.android.gcm.GCMRegistrar;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.rest.ApiInterface;
import de.tinloaf.iris.mobileapp.rest.MobileData;
import de.tinloaf.iris.mobileapp.rest.RESTClient;

public class MainActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener, LoginDialogFragment.LoginDialogListener,
		RESTClient.RESTFailureListener, MobileData.ApiInterfaceEventListener {

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(getActionBarThemedContextCompat(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] {
								getString(R.string.title_section1),
								getString(R.string.title_section2),
								getString(R.string.title_section3), }), this);
		
		// Set up GCM
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
		  GCMRegistrar.register(this, "339966378729");
		} else {
		  Log.v("Blubb", "Already registered");
		}
		
		this.initRest();
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
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.v("IRIS", "Paused");
	}
	
	public void onResume() {
		super.onResume();
		Log.v("IRIS", "Resume");
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		Fragment fragment = new NotificationListFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, fragment).commit();
		
		
		return true;		
	}


	private void showLoginDialog() {
		DialogFragment dialog = new LoginDialogFragment();
        dialog.show(getSupportFragmentManager(), "LoginDialogFragment");		
	}
	
	private void initRest() {
    	Log.i("MAIN", "Called initRest()");
    	
		SharedPreferences settings = getSharedPreferences("iris", 0);
		if ((! settings.contains("username")) ||
				(!settings.contains("apikey"))) {
			showLoginDialog();
		} else {
			this.restClient = new RESTClient(settings.getString("username", "null"), 
					settings.getString("apikey", null), this);
			this.md = new MobileData(this.restClient, this);
			md.load();
			
		}
	}

	@Override
	public void onDialogPositiveClick(String username, String apikey) {
 	   this.restClient = new RESTClient(username, apikey, this);
 	   
 	   // Verify that it works
 	   
	   this.md = new MobileData(this.restClient, this);
	   md.load();
	   
	   // TODO make sure stuff worked
	   
	   SharedPreferences settings = getSharedPreferences("iris", 0);
	   SharedPreferences.Editor editor = settings.edit();
	   editor.putString("username", username);
	   editor.putString("apikey", apikey);
	   editor.commit();		
	}



	@Override
	public void onDialogNegativeClick() {
		initRest();
	}



	@Override
	public void onLoginFailed() {
    	Log.v("MAIN", "Called onLoginFailed()");
		
		// Delete API key and username and show dialog
		SharedPreferences settings = getSharedPreferences("iris", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove("apikey");
		editor.remove("username");
		editor.commit();
		
		// Show dialog, lets build a new rest client.
		this.initRest();
	}



	// Load of MobileData done...
	@Override
	public void onLoadDone(ApiInterface apiInterface) {
		Log.v("MAIN", "Called onLoadDone()");
		this.md.setGcmKey(GCMRegistrar.getRegistrationId(this));
		this.md.save();
	}

}
