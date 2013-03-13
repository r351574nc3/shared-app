package de.tinloaf.iris.mobileapp.data;

import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import de.tinloaf.iris.mobileapp.CommonUtilities;

public class CleanupService extends Service {

	private DatabaseHelper databaseHelper;
	
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
	            OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }
	    return databaseHelper;
	}
	
	private boolean deleteCachedImage(SavedPortalImage portalImage) {
		// check external storage availabilty
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return false; // could not delete, might be there.
		}
		
		File myDir = this.getExternalFilesDir(null);
		String fileName = CommonUtilities.getPortalImageCacheDir() + portalImage.imgPath;
		File portalImageFile = new File(myDir, fileName);
		if (!portalImageFile.exists()) {
			Log.v("CLEANUP", fileName + " not found");
			return true; // not there...
		}
		
		Log.v("CLEANUP", "Deleting " + fileName);		
		portalImageFile.delete();
		return true;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("CLEANUP", "Running Cleanup Service");
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		
		try {
			Dao<SavedPortalImage, SavedPortal> portalImageDao = getHelper().getPortalImageDao();
			QueryBuilder queryBuilder = portalImageDao.queryBuilder();
			int portalImageCacheTime = Integer.parseInt(settings.getString("pref_cache_portalimages", "30"));
			
			cal.setTime(now);
			cal.add(Calendar.DATE, -1 * portalImageCacheTime);
			
			Date portalImageCutoff = cal.getTime();
			
			queryBuilder.where().le("lastUsed", portalImageCutoff);
			List<SavedPortalImage> imagesToDelete = portalImageDao.query(queryBuilder.prepare());
			
			Iterator<SavedPortalImage> it = imagesToDelete.iterator();
			while (it.hasNext()) {
				SavedPortalImage cur = it.next();
				boolean deleteSuccess = this.deleteCachedImage(cur);
				
				if (! deleteSuccess) {
					// Possibly unmounted? Try again later!
					imagesToDelete.remove(cur);
				}
			}
			
			portalImageDao.delete(imagesToDelete);			
			Log.v("CLEANUP", Long.toString(portalImageDao.countOf()) + " portal images remaining");
			
			// Now, destructions
			Dao<Destruction, Integer> destrDao = getHelper().getDestructionDao();
			DeleteBuilder<Destruction, Integer> deleteBuilder = destrDao.deleteBuilder();
			
			int destrCacheTime = Integer.parseInt(settings.getString("pref_cache_destruction", "30"));
			
			cal.setTime(now);
			cal.add(Calendar.DATE, -1 * destrCacheTime);
			
			Date destrCutoff = cal.getTime();
			
			deleteBuilder.where().le("time", destrCutoff);
			destrDao.delete(deleteBuilder.prepare());
			Log.v("CLEANUP", Long.toString(destrDao.countOf()) + " destructions remaining");
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stopSelf();
		return Service.START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
