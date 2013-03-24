package de.tinloaf.iris.mobileapp;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import de.tinloaf.iris.mobileapp.data.Destruction;
import de.tinloaf.iris.mobileapp.data.SavedPortal;

public class NotificationData implements Parcelable {
	private SavedPortal portal;
	private String destrString;
	private String attacker;
	private Date date;
	
	public NotificationData(SavedPortal portal, String destrString, String attacker, Date date) {
		super();
		this.portal = portal;
		
		if (portal == null) {
			throw new IllegalArgumentException("portal may not be null");
		}
		
		this.destrString = destrString;
		this.attacker = attacker;
		this.date = date;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	public String getImgUrl() {
		return this.portal.imgUrl;
	}

	public String getPortalTitle() {
		return portal.title;
	}
	
	public SavedPortal getPortal() {
		return portal;
	}

	public String getDestrString() {
		return destrString;
	}

	public String getAttacker() {
		return attacker;
	}

	public NotificationData(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		this.portal = in.readParcelable(SavedPortal.class.getClassLoader());
		this.destrString = in.readString();
		this.attacker = in.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(this.portal, flags);
		out.writeString(this.destrString);
		out.writeString(this.attacker);
	}
	
	public static final Parcelable.Creator<Destruction> CREATOR = new Parcelable.Creator<Destruction>() {
		public Destruction createFromParcel(Parcel in) {
			return new Destruction(in);
		}

		@Override
		public Destruction[] newArray(int ct) {
			return new Destruction[ct];
		}
	};
}
