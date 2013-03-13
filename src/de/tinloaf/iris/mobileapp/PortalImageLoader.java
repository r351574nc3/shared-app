package de.tinloaf.iris.mobileapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.j256.ormlite.dao.Dao;

import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.data.SavedPortal;
import de.tinloaf.iris.mobileapp.data.SavedPortalImage;

public class PortalImageLoader implements Runnable {
	private Handler mHandler;
	private Context ctx;
	private ImageView view;
	
	private DatabaseHelper databaseHelper;
	
	public PortalImageLoader(DatabaseHelper helper, Context ctx, ImageView view) {
		this.databaseHelper = helper;		
		this.ctx = ctx;
		this.view = view;
	}
	
	private InputStream openHttpConnection(String urlString) throws IOException
	{
	        InputStream in = null;
	        int response = -1;
	                
	        URL url = new URL(urlString);
	        URLConnection conn = url.openConnection();
	                  
	        if (!(conn instanceof HttpURLConnection)) throw new IOException("Not an HTTP connection");
	         
	        HttpURLConnection httpConn = (HttpURLConnection) conn;
	        httpConn.setAllowUserInteraction(false);
	        httpConn.setInstanceFollowRedirects(true);
	        httpConn.setRequestMethod("GET");
	        httpConn.connect();
	 
	        response = httpConn.getResponseCode();                
	        if (response == HttpURLConnection.HTTP_OK) {
	        	in = httpConn.getInputStream();                                
	        }                    
	        
	        return in;    
	}
	
	private Bitmap downloadImage(String URL)
    {       
        Bitmap bitmap = null;
        InputStream in = null;       
        try {
            in = openHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return bitmap;               
    }
	
	public static Bitmap getCachedImage(SavedPortal portal, DatabaseHelper dbHelper, Context ctx) {
		try {
			Dao<SavedPortalImage, SavedPortal> portalImageDao = dbHelper.getPortalImageDao();
			
			List<SavedPortalImage> portalImageList = portalImageDao.queryForEq("portal_id", portal);
			if (portalImageList.size() == 0) {
				return null;
			}
			
			SavedPortalImage portalImage = portalImageList.get(0);
			
			// OK, check external storage availabilty
			String state = Environment.getExternalStorageState();
			if (!Environment.MEDIA_MOUNTED.equals(state)) {
				return null;
			}
			
			File myDir = ctx.getExternalFilesDir(null);
			String fileName = CommonUtilities.getPortalImageCacheDir() + portalImage.imgPath;
			File portalImageFile = new File(myDir, fileName);
			if (!portalImageFile.exists()) {
				portalImageDao.delete(portalImage);
				return null;
			}
			
			// Update last accessed attribute...
			portalImage.lastUsed = new Date();
			portalImageDao.update(portalImage);
			
			Bitmap ret = BitmapFactory.decodeFile(portalImageFile.getPath());
			return ret;
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}
	
	private void cachePicture(Bitmap picture, SavedPortal portal) {
		try {
			Dao<SavedPortalImage, SavedPortal> portalImageDao = this.databaseHelper.getPortalImageDao();
			
			List<SavedPortalImage> portalImageList = portalImageDao.queryForEq("portal_id", portal);
			if (portalImageList.size() > 0) {
				return;
			}
			
			// OK, check external storage availabilty
			String state = Environment.getExternalStorageState();
			if (!Environment.MEDIA_MOUNTED.equals(state)) {
				return;
			}

			SavedPortalImage portalImage = null;
			if (portalImageList.size() != 0) {
				portalImage = portalImageList.get(0);
			} else {
				portalImage = new SavedPortalImage();
				portalImage.portal = portal;
				portalImage.imgPath = UUID.randomUUID().toString();
				portalImage.lastUsed = new Date();
				portalImageDao.create(portalImage);
			}
			
			File myDir = this.ctx.getExternalFilesDir(null);
			File cacheDir = new File(myDir, CommonUtilities.getPortalImageCacheDir());
			cacheDir.mkdirs();
			File portalImageFile = new File(cacheDir, portalImage.imgPath);
			Log.v("PIL", "Caching to " + portalImageFile.getAbsolutePath());
			FileOutputStream out = new FileOutputStream(portalImageFile);
			picture.compress(Bitmap.CompressFormat.JPEG, 90, out);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private Bitmap loadBitmap(SavedPortal portal) {
		try {
			Log.v("PIL", "Getting image from " + portal.imgUrl);
			Bitmap picture = this.downloadImage(portal.imgUrl);
			// TODO do the scaling after caching?
			Bitmap scaledPicture = Bitmap.createScaledBitmap(picture, 110, 110, false);
			
			return scaledPicture;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}

	@Override
	public void run() {
		Bitmap ret = null;
		SavedPortal portal = (SavedPortal) view.getTag();
		
		try {
			// First, check if we have that portal cached.
			
			ret = PortalImageLoader.getCachedImage(portal, this.databaseHelper, this.ctx);
			if (ret == null) {
				Log.v("PIL", "Downloading for " + portal.title +  "...");
				ret = loadBitmap(portal);
				
				// Now, cache it!
				cachePicture(ret, portal);
				// TODO Cache cleanup?
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		final Bitmap picture = ret;
		final ImageView viewToSet = view;
		
		viewToSet.post(new Runnable() {
			public void run() {
				viewToSet.setImageBitmap(picture);

			}
		});		
	}	
}
