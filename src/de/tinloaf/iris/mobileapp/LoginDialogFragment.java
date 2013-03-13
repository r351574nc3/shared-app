package de.tinloaf.iris.mobileapp;

import com.actionbarsherlock.app.SherlockDialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class LoginDialogFragment extends SherlockDialogFragment {

	private LoginDialogListener mListener;
	
	public LoginDialogFragment() {
		
	}
	
	public interface LoginDialogListener {
		public void onDialogPositiveClick(String username, String apikey);
		public void onDialogNegativeClick();
	}

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (LoginDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement LoginDialogListener");
        }
    }

	
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        builder.setTitle("Login to IRIS");
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View dialogMainView = inflater.inflate(R.layout.login_dialog, null);
        builder.setView(dialogMainView)
        // Add action buttons
               .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                	   // Save Login
                	   EditText username = (EditText) dialogMainView.findViewById(R.id.username);
                	   EditText apikey = (EditText) dialogMainView.findViewById(R.id.apikey);
                	   
                	   mListener.onDialogPositiveClick(username.getText().toString(), apikey.getText().toString());
                   }
               })
               .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       LoginDialogFragment.this.getDialog().cancel();
                       // TODO close app?
                   }
               });      
        
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
