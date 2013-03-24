package de.tinloaf.iris.mobileapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.tinloaf.iris.mobileapp.data.DatabaseHelper;

public class NotificationAdapter extends ArrayAdapter<NotificationData> {
	private Activity activity;
    private int layoutResourceId;   
    private ArrayList<NotificationData> data = null;
    private DatabaseHelper dbHelper;
    private Thread imageLoaderThread;
    
    public NotificationAdapter(Activity activity, int layoutResourceId, 
    		ArrayList<NotificationData> data, DatabaseHelper dbHelper) {
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
            holder.imgIcon.setImageResource(R.drawable.ic_launcher);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            holder.txtDestr = (TextView)row.findViewById(R.id.txtDestruction);
            holder.txtDate = (TextView)row.findViewById(R.id.txtDate);
            //holder.txtAttacker = (TextView)row.findViewById(R.id.txtAttacker);
            
            row.setTag(R.id.tag_holder, holder);
        }
        else
        {
            holder = (NotificationHolder)row.getTag(R.id.tag_holder);
        }
       
        
        NotificationData notification = data.get(position);
        if (notification.getPortalTitle() != null) {
            holder.txtTitle.setText(notification.getPortalTitle());        	
        	holder.txtTitle.setTypeface(null, 0);
        } else {
        	holder.txtTitle.setText("(Untitled)");
        	holder.txtTitle.setTypeface(null, Typeface.ITALIC);
        }
        holder.txtDestr.setText(notification.getDestrString());
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
        cal.setTime(notification.getDate());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("America/Phoenix"));
        holder.txtDate.setText(sdf.format(cal.getTime()));
        
        
        holder.imgIcon.setTag(notification.getPortal()); // The PortalImageLoader needs this
        row.setTag(R.id.tag_portal, notification.getPortal()); // For opening the details view
        //holder.txtAttacker.setText(notification.getAttacker());
        //holder.imgIcon.setImageResource(R.drawable.ic_launcher);
        
        // Tell the worker thread to get that picture here.
        if (notification.getPortal().imgUrl != null) {
        	new Thread (new PortalImageLoader(this.dbHelper, this.getContext(), holder.imgIcon, false)).start();
        }
        
        return row;
    }
   
    static class NotificationHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtDestr;
        TextView txtDate;
        //TextView txtAttacker;
    }
}
