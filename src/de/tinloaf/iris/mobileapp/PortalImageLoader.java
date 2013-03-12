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
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.j256.ormlite.dao.Dao;

import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.data.SavedPortal;
import de.tinloaf.iris.mobileapp.data.SavedPortalImage;

public class PortalImageLoader extends AsyncTask<String, Integer, Bitmap> {
	private ImageView view;
	private Bitmap downloadedPicture;
	private SavedPortal portal;
	private Context ctx;
	
	private DatabaseHelper databaseHelper;
	
	public PortalImageLoader(ImageView view, SavedPortal portal, DatabaseHelper helper, Context ctx) {
		this.view = view;
		this.portal = portal;
		this.databaseHelper = helper;
		this.ctx = ctx;
		
		Log.v("PIL", "Created");
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
			Log.v("PIL", "Querying Cache..");
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
			String fileName = CommonUtilities.PORTAL_IMAGE_CACHE_DIR + "/" + portalImage.imgPath;
			File portalImageFile = new File(myDir, fileName);
			if (!portalImageFile.exists()) {
				portalImageDao.delete(portalImage);
				return null;
			}
			
			// Update last accessed attribute...
			portalImage.lastUsed = new Date();
			portalImageDao.update(portalImage);
			
			Log.v("PIL", "Returning cached image.");
			Bitmap ret = BitmapFactory.decodeFile(portalImageFile.getPath());
			return ret;
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}
	
	private void cachePicture(Bitmap picture) {
		try {
			Log.v("PIL", "Caching...");
			Dao<SavedPortalImage, SavedPortal> portalImageDao = this.databaseHelper.getPortalImageDao();
			
			List<SavedPortalImage> portalImageList = portalImageDao.queryForEq("portal_id", this.portal);
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
				portalImage.portal = this.portal;
				portalImage.imgPath = UUID.randomUUID().toString();
				portalImage.lastUsed = new Date();
				portalImageDao.create(portalImage);
			}
			
			File myDir = this.ctx.getExternalFilesDir(null);
			File cacheDir = new File(myDir, CommonUtilities.PORTAL_IMAGE_CACHE_DIR);
			cacheDir.mkdirs();
			File portalImageFile = new File(cacheDir, portalImage.imgPath);
			
			FileOutputStream out = new FileOutputStream(portalImageFile);
			picture.compress(Bitmap.CompressFormat.JPEG, 90, out);
			Log.v("PIL", "Cached image.");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private Bitmap loadBitmap() {
		try {
			Log.v("PIL", "Getting image from " + this.portal.imgUrl);
			Bitmap picture = this.downloadImage(this.portal.imgUrl);
			Bitmap scaledPicture = Bitmap.createScaledBitmap(picture, 110, 110, false);
			Log.v("PIL", "Image Dimensions: " + picture.getHeight() + "x" + picture.getWidth());
			this.downloadedPicture = scaledPicture;
			
			return scaledPicture;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap ret = null;
		Log.v("PIL", "Executing for " + this.portal.title +  "...");
		
		try {
			// First, check if we have that portal cached.
			
			ret = PortalImageLoader.getCachedImage(this.portal, this.databaseHelper, this.ctx);
			if (ret == null) {
				Log.v("PIL", "Downloading for " + this.portal.title +  "...");
				ret = loadBitmap();
				
				// Now, cache it!
				cachePicture(ret);
				// TODO Cache cleanup?
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		return ret;
	}
	
	@Override
	protected void onPostExecute(Bitmap picture)
    {
		this.view.setImageBitmap(picture);
    }
}
