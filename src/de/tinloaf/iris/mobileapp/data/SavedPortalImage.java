package de.tinloaf.iris.mobileapp.data;

import java.io.File;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;

import de.tinloaf.iris.mobileapp.CommonUtilities;

public class SavedPortalImage {
	@DatabaseField(generatedId = true)
	public Integer id;
	
	@DatabaseField(foreign = true, unique = true, canBeNull = false)
	public SavedPortal portal;
	
	@DatabaseField
	public String imgPath;
	
	@DatabaseField
	public Date lastUsed;
	
	public boolean deleteSelf(Context ctx) {
		// OK, check external storage availabilty
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return false;
		}
		
		File myDir = ctx.getExternalFilesDir(null);
		boolean success = true;
		
		// large
		File cacheDir = new File(myDir, CommonUtilities.getPortalImageCacheDir(true));
		if (cacheDir.exists()) {
			File portalImageFile = new File(cacheDir, this.imgPath);

			if (portalImageFile.exists()) {
				success &= portalImageFile.delete();
			}
		}
		
		// small
		cacheDir = new File(myDir, CommonUtilities.getPortalImageCacheDir(false));
		if (cacheDir.exists()) {
			File portalImageFile = new File(cacheDir, this.imgPath);

			if (portalImageFile.exists()) {
				success &= portalImageFile.delete();
			}
		}
		
		return success;
	}
}
