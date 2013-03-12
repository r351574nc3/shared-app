package de.tinloaf.iris.mobileapp;

import de.tinloaf.iris.mobileapp.data.SavedPortal;

public class Notification {
	private SavedPortal portal;
	private String destrString;
	private String attacker;
	
	public Notification(SavedPortal portal, String destrString, String attacker) {
		super();
		this.portal = portal;
		this.destrString = destrString;
		this.attacker = attacker;
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
}
