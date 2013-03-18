package de.tinloaf.iris.mobileapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class EditDialogFragment extends SherlockDialogFragment {
	public static final int TYPE_TITLE = 1;
	public static final int TYPE_ADDRESS = 2;
	public static final int TYPE_DESCRIPTION = 3;
	
	private int type;
	private String initial;
	
	public interface EditDialogListener {
		public void onDialogPositiveClick(String value, int type);
		public void onDialogNegativeClick();
	}
	
	public void setData(String value, int type) {
		this.type = type;
		this.initial = value;
	}
	
	@Override 
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		
		out.putInt("EDIT_TYPE", this.type);
		out.putString("EDIT_INITIAL", this.initial);
	}
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		if (savedState != null) {
			this.type = savedState.getInt("EDIT_TYPE");
			this.initial = savedState.getString("EDIT_INITIAL");
		}
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        String title = "Edit portal ";
        switch (this.type) {
        case TYPE_TITLE:
        	title += "title";
        	break;
        case TYPE_ADDRESS:
        	title += "address";
        	break;
        case TYPE_DESCRIPTION:
        	title += "description";
        	break;
        }
        
        builder.setTitle("Edit portal");
        
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View dialogMainView = inflater.inflate(R.layout.edit_portal_dialog, null);
        
        builder.setView(dialogMainView)
        // Add action buttons
               .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                	   
                	   EditText value = (EditText) dialogMainView.findViewById(R.id.editField);
                	   
                	   final EditDialogListener mListener = (EditDialogListener) EditDialogFragment.this.getTargetFragment();
                	   mListener.onDialogPositiveClick(value.getText().toString(), type);
                   }
               })
               .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   final EditDialogListener mListener = (EditDialogListener) EditDialogFragment.this.getTargetFragment();
                	   mListener.onDialogNegativeClick();
                   }
               });      
        
        EditText value = (EditText) dialogMainView.findViewById(R.id.editField);
        value.setText(this.initial);

        TextView heading = (TextView) dialogMainView.findViewById(R.id.headingTxt);
        heading.setText(title);
        
        // Create the AlertDialog object and return it
        return builder.create();
    }

}

