package de.tinloaf.iris.mobileapp.data;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.*;

public class Destruction implements Parcelable {

	public static final int KIND_RESONATOR = 1;
	public static final int KIND_LINK = 2;
	public static final int KIND_MOD = 3;
	
	public int portalId;
	public int portalEndId;
	public int kind;
	public String attacker;
	public Date time;
	public int count;
	
	public Destruction() {
	}
	
	public Destruction(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		this.portalId = in.readInt();
		this.portalEndId = in.readInt();
		this.kind = in.readInt();
		this.attacker = in.readString();
		this.time = (Date) in.readSerializable();
		this.count = in.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(this.portalId);
		out.writeInt(this.portalEndId);
		out.writeInt(kind);
		out.writeString(attacker);
		out.writeSerializable(this.time);
		out.writeInt(this.count);
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
