package de.tinloaf.iris.mobileapp.data;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

public class SavedPortal implements Parcelable {
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

	public SavedPortal(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.title = in.readString();
		this.description = in.readString();
		this.address = in.readString();
		this.subscription = in.readInt();
		this.lat = in.readDouble();
		this.lng = in.readDouble();
		this.imgUrl = in.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(this.id);
		out.writeString(this.title);
		out.writeString(this.description);
		out.writeString(this.address);
		out.writeInt(this.subscription);
		out.writeDouble(this.lat);
		out.writeDouble(this.lng);
		out.writeString(this.imgUrl);
	}
	
	public static final Parcelable.Creator<SavedPortal> CREATOR = new Parcelable.Creator<SavedPortal>() {
		public SavedPortal createFromParcel(Parcel in) {
			return new SavedPortal(in);
		}

		@Override
		public SavedPortal[] newArray(int ct) {
			return new SavedPortal[ct];
		}
	};

}
