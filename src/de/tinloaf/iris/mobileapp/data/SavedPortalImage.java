package de.tinloaf.iris.mobileapp.data;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;

public class SavedPortalImage {
	@DatabaseField(generatedId = true)
	public Integer id;
	
	@DatabaseField(foreign = true, unique = true, canBeNull = false)
	public SavedPortal portal;
	
	@DatabaseField
	public String imgPath;
	
	@DatabaseField
	public Date lastUsed;
}
