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
	private Context ctx;
	private ImageView view;
	
	private DatabaseHelper databaseHelper;
	private boolean wantLarge;
	
	public PortalImageLoader(DatabaseHelper helper, Context ctx, ImageView view, boolean wantLarge) {
		this.databaseHelper = helper;		
		this.ctx = ctx;
		this.view = view;
		this.wantLarge = wantLarge;
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
	
	public static Bitmap getCachedImage(SavedPortal portal, DatabaseHelper dbHelper, Context ctx,
			boolean wantLarge) {
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
			File sizeDir = null;
			String fileName = CommonUtilities.getPortalImageCacheDir(wantLarge) + portalImage.imgPath;
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
			
			if (portal == null) {
				throw new IllegalArgumentException("Cannot cache for null portal!");
			}
			
			List<SavedPortalImage> portalImageList = portalImageDao.queryForEq("portal_id", portal.id);
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

				// FIXME a problem arises here if one portals is being loaded twice in parallel...
				portalImageDao.create(portalImage);
			}
			
			File myDir = this.ctx.getExternalFilesDir(null);
			
			
			File largeCacheDir = new File(myDir, CommonUtilities.getPortalImageCacheDir(true));
			File smallCacheDir = new File(myDir, CommonUtilities.getPortalImageCacheDir(false));
			largeCacheDir.mkdirs();
			smallCacheDir.mkdirs();
			
			// Small picture
			File portalImageFile = new File(smallCacheDir, portalImage.imgPath);
			Log.v("PIL", "Caching to " + portalImageFile.getAbsolutePath());
			FileOutputStream out = new FileOutputStream(portalImageFile);
			this.scaleBitmap(picture, false).compress(Bitmap.CompressFormat.JPEG, 90, out);

			// Large picture
			portalImageFile = new File(largeCacheDir, portalImage.imgPath);
			out = new FileOutputStream(portalImageFile);
			this.scaleBitmap(picture, true).compress(Bitmap.CompressFormat.JPEG, 90, out);			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private Bitmap scaleBitmap(Bitmap bitmap, boolean large) {
		if (large) {
			return Bitmap.createScaledBitmap(bitmap, CommonUtilities.PORTALIMAGE_LARGE_X, 
					CommonUtilities.PORTALIMAGE_LARGE_Y, false);
		} else {
			return Bitmap.createScaledBitmap(bitmap, CommonUtilities.PORTALIMAGE_SMALL_X, 
					CommonUtilities.PORTALIMAGE_SMALL_Y, false);			
		}
	}
	
	private Bitmap loadBitmap(SavedPortal portal) {
		try {
			Log.v("PIL", "Getting image from " + portal.imgUrl);
			Bitmap picture = this.downloadImage(portal.imgUrl);
			
			return picture;
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
			
			ret = PortalImageLoader.getCachedImage(portal, this.databaseHelper, this.ctx, this.wantLarge);
			if (ret == null) {
				ret = loadBitmap(portal);
				
				// Now, cache it!
				cachePicture(ret, portal);
				
				// And scale down .. TODO this is done twice...
				ret = this.scaleBitmap(ret, this.wantLarge);
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
