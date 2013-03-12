package de.tinloaf.iris.mobileapp;

import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.tinloaf.iris.mobileapp.data.DatabaseHelper;

public class NotificationAdapter extends ArrayAdapter<Notification> {
	private Activity activity;
    private int layoutResourceId;   
    private ArrayList<Notification> data = null;
    private DatabaseHelper dbHelper;
    
    public NotificationAdapter(Activity activity, int layoutResourceId, 
    		ArrayList<Notification> data, DatabaseHelper dbHelper) {
        super(activity, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.activity = activity;
        this.data = data;
        this.dbHelper = dbHelper;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        NotificationHolder holder = null;
       
        if(row == null)
        {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
           
            holder = new NotificationHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            holder.txtDestr = (TextView)row.findViewById(R.id.txtDestruction);
            holder.txtAttacker = (TextView)row.findViewById(R.id.txtAttacker);
           
            row.setTag(holder);
        }
        else
        {
            holder = (NotificationHolder)row.getTag();
        }
       
        
        Notification notification = data.get(position);
        holder.txtTitle.setText(notification.getPortalTitle());
        holder.txtDestr.setText(notification.getDestrString());
        holder.txtAttacker.setText(notification.getAttacker());
        holder.imgIcon.setImageResource(R.drawable.ic_launcher);
        
        Log.v("NA", "Returning a View");
        
        PortalImageLoader portalImageLoader = new PortalImageLoader(holder.imgIcon, notification.getPortal(), 
        		this.dbHelper, this.getContext());
        portalImageLoader.execute();
        return row;
    }
   
    static class NotificationHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtDestr;
        TextView txtAttacker;
    }
}
