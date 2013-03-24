package de.tinloaf.iris.mobileapp.data;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.*;

import com.j256.ormlite.field.DatabaseField;

public class Destruction implements Parcelable {

	public static final int KIND_RESONATOR = 1;
	public static final int KIND_LINK = 2;
	public static final int KIND_FIELD = 3;
	public static final int KIND_MOD = 4;
	
	@DatabaseField(generatedId = true)
    public Integer id;
	
	@DatabaseField
	public int portalId;
	@DatabaseField
	public int portalEndId;
	@DatabaseField
	public int kind;
	@DatabaseField
	public String attacker;
	@DatabaseField
	public Date time;
	@DatabaseField
	public int count;
	// TODO make this three-state to avoid race conditions: none, displayed, dismissed
	@DatabaseField
	public boolean displayed;
	
	public Destruction() {
		this.displayed = false;
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
		this.displayed = (in.readByte() == 1);
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
		out.writeByte((byte)(this.displayed? 1 : 0));
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
