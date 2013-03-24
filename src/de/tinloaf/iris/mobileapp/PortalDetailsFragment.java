package de.tinloaf.iris.mobileapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import de.tinloaf.iris.mobileapp.EditDialogFragment.EditDialogListener;
import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.data.SavedPortal;
import de.tinloaf.iris.mobileapp.rest.ApiInterface;
import de.tinloaf.iris.mobileapp.rest.ApiInterface.ApiInterfaceEventListener;
import de.tinloaf.iris.mobileapp.rest.PortalSaver;

public class PortalDetailsFragment extends SherlockFragment 
		implements EditDialogListener, ApiInterfaceEventListener {
	private SavedPortal portal;
	
	private DatabaseHelper databaseHelper = null;
	
	public PortalDetailsFragment() {
		super();
	}
	
	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		
		out.putParcelable("DETAILS_PORTAL", this.portal);
	}
	
	public PortalDetailsFragment(SavedPortal portal) {
		super();
		this.portal = portal;
	}
	
	@Override
	public void onCreate(Bundle state) {
		super.onActivityCreated(state);
		
		if ((state != null) && (state.containsKey("DETAILS_PORTAL"))) {
			this.portal = (SavedPortal) state.getParcelable("DETAILS_PORTAL");
		}
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	}
	

	private DatabaseHelper getHelper() {
	    if (databaseHelper == null) {
	        databaseHelper =
	            OpenHelperManager.getHelper(this.getActivity(), DatabaseHelper.class);
	    }
	    return databaseHelper;
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View ret = inflater.inflate(R.layout.fragment_portal_details, container, false);	
		
		TextView titleView = (TextView) ret.findViewById(R.id.portalTitleTxt);
		titleView.setText(portal.title);
		
		TextView addressView = (TextView) ret.findViewById(R.id.portalAddressTxt);
		addressView.setText(portal.address);
		
		TextView descrView = (TextView) ret.findViewById(R.id.portalDescrTxt);
		descrView.setText(portal.description);
		
		Button btn = (Button) ret.findViewById(R.id.button1);
		OnClickListener buttonListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PortalDetailsFragment.this.showEditDialog(v);
			}
		};
		
		btn.setOnClickListener(buttonListener);
		btn = (Button) ret.findViewById(R.id.button2);
		btn.setOnClickListener(buttonListener);
		btn = (Button) ret.findViewById(R.id.button3);
		btn.setOnClickListener(buttonListener);		
		
		
        // Tell the worker thread to get that picture here.
		if (portal.imgUrl != null) {
			ImageView imageView = (ImageView) ret.findViewById(R.id.portalImageView);
			imageView.setTag(portal);
        	new Thread (new PortalImageLoader(getHelper(), this.getActivity(), imageView, true)).start();
			
		}

		
		return ret;
	}

	@Override
	public void onDialogPositiveClick(String value, int type) {
		switch (type) {
		case EditDialogFragment.TYPE_TITLE:
			this.portal.title = value;
			TextView titleView = (TextView) this.getView().findViewById(R.id.portalTitleTxt);
			titleView.setText(portal.title);
			break;
		case EditDialogFragment.TYPE_ADDRESS:
			this.portal.address = value;
			TextView addressView = (TextView) this.getView().findViewById(R.id.portalAddressTxt);
			addressView.setText(portal.address);
			break;
		case EditDialogFragment.TYPE_DESCRIPTION:
			this.portal.description = value;
			TextView descrView = (TextView) this.getView().findViewById(R.id.portalDescrTxt);
			descrView.setText(portal.description);
			break;
		}
		
		Log.v("PDF", "Saving " + this.portal.toString());
		
		PortalSaver saver = new PortalSaver(this.portal, this.getActivity(), this);
		saver.save();
	}
	
	public void showEditDialog(View v) {
		int type = Integer.parseInt((String) v.getTag());
		
		if (getActivity().getSupportFragmentManager().findFragmentByTag("EditPortalDialog") == null) {
			
			EditDialogFragment dialog = new EditDialogFragment();
			dialog.setTargetFragment(this, 0);
			
			String value = null;
			switch (type) {
			case EditDialogFragment.TYPE_TITLE:
				value = this.portal.title;
				break;
			case EditDialogFragment.TYPE_ADDRESS:
				value = this.portal.address;
				break;
			case EditDialogFragment.TYPE_DESCRIPTION:
				value = this.portal.description;
				break;
			}
			
			dialog.setData(value, type);
			
			getActivity().getSupportFragmentManager().beginTransaction()
            	.add(dialog, "EditPortalDialog")
            	.commit();
			// make it not return null anymore
		}	
	}

	@Override
	public void onDialogNegativeClick() {
	}

	@Override
	public void onLoadDone(ApiInterface apiInterface) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoginFailed() {
		// TODO react!
	}

	@Override
	public void onPutDone() {
		// We assumed that everything worked out earlier...
	}

	@Override
	public void onError(String message) {
	    AlertDialog ad = new AlertDialog.Builder(this.getActivity()).create();  
	    ad.setCancelable(false); // This blocks the 'BACK' button  
	    ad.setMessage("Error saving Portal: " + message);  
	    ad.setButton("OK", new DialogInterface.OnClickListener() {  
	        @Override  
	        public void onClick(DialogInterface dialog, int which) {  
	            dialog.dismiss();                      
	        }  
	    });  
	    ad.show();  
	}
}
