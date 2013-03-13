package de.tinloaf.iris.mobileapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import android.graphics.Bitmap;

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
            
            row.setTag(holder);
        }
        else
        {
            holder = (NotificationHolder)row.getTag();
        }
       
        
        NotificationData notification = data.get(position);
        holder.txtTitle.setText(notification.getPortalTitle());
        holder.txtDestr.setText(notification.getDestrString());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        holder.txtDate.setText(sdf.format(notification.getDate()));
        holder.imgIcon.setTag(notification.getPortal());
        //holder.txtAttacker.setText(notification.getAttacker());
        //holder.imgIcon.setImageResource(R.drawable.ic_launcher);
        
        // Tell the worker thread to get that picture here.
        new Thread (new PortalImageLoader(this.dbHelper, this.getContext(), holder.imgIcon)).start();
        
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
