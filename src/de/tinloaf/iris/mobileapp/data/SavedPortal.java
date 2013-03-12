package de.tinloaf.iris.mobileapp.data;

import com.j256.ormlite.field.DatabaseField;

public class SavedPortal {
	@DatabaseField(id = true)
    public Integer id;
	@DatabaseField
	public String title;
	@DatabaseField
	public String description;
	@DatabaseField
	public String address;
	@DatabaseField
	public Integer subscription;
	@DatabaseField
	public Double lat;
	@DatabaseField
	public Double lng;
	@DatabaseField
	public String imgUrl;
	
	@DatabaseField 
	public Integer version;
	
	public SavedPortal() {
	}

}
