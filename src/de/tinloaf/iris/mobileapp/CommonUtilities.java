package de.tinloaf.iris.mobileapp;


public final class CommonUtilities {
	
	public static final int PORTALIMAGE_SMALL_X = 120;
	public static final int PORTALIMAGE_SMALL_Y = 120;
	public static final int PORTALIMAGE_LARGE_X = 350;
	public static final int PORTALIMAGE_LARGE_Y = 350;
	
	private static boolean isDebugBuild() {
		return BuildConfig.DEBUG;
	}
	
	private final static class DEBUG_CONSTS {
		private static final String BROADCAST_DESTRUCTIONS = "de.tinloaf.iris.mobileapp.broadcasts.DESTRUCTIONS";
		private static final String BROADCAST_CLEARNOTIFICATION = "de.tinloaf.iris.mobileapp.broadcasts.CLEARNOTIFICATION";
	
		private static final String SERVER_BASE_URL = "http://192.168.0.4:8000/api/";
	
		private static final String PORTAL_IMAGE_CACHE_DIR = "portal_image_cache/";
	
		private static final int CLEANUP_SECONDS = 600;
		
		private static final String GOOGLE_PROJECT_ID = "708580480954";
	}
	
	private final static class PRODUCTION_CONSTS {
		private static final String BROADCAST_DESTRUCTIONS = "de.tinloaf.iris.mobileapp.broadcasts.DESTRUCTIONS";
		private static final String BROADCAST_CLEARNOTIFICATION = "de.tinloaf.iris.mobileapp.broadcasts.CLEARNOTIFICATION";
	
		private static final String SERVER_BASE_URL = "http://iris.tinloaf.de/api/";
	
		private static final String PORTAL_IMAGE_CACHE_DIR = "portal_image_cache/";
	
		private static final int CLEANUP_SECONDS = 86400; // one day
		
		private static final String GOOGLE_PROJECT_ID = "339966378729";
	}
	
	public static String getGoogleProjectId() {
		if (isDebugBuild()) {
			return DEBUG_CONSTS.GOOGLE_PROJECT_ID;
		} else {
			return PRODUCTION_CONSTS.GOOGLE_PROJECT_ID;			
		}
	}
	
	public static String getBroadcastDestructions() {
		if (isDebugBuild()) {
			return DEBUG_CONSTS.BROADCAST_DESTRUCTIONS;
		} else {
			return PRODUCTION_CONSTS.BROADCAST_DESTRUCTIONS;			
		}
	}

	public static String getBroadcastClearnotification() {
		if (isDebugBuild()) {
			return DEBUG_CONSTS.BROADCAST_CLEARNOTIFICATION;
		} else {
			return PRODUCTION_CONSTS.BROADCAST_CLEARNOTIFICATION;
		}
	}

	public static String getServerBaseUrl() {
		if (isDebugBuild()) {
			return DEBUG_CONSTS.SERVER_BASE_URL;
		} else {
			return PRODUCTION_CONSTS.SERVER_BASE_URL;
		}
	}

	public static String getPortalImageCacheDir(boolean large) {
		String appendix = null;
		if (large) {
			appendix = "large/";
		} else {
			appendix = "small/";
		}
		
		if (isDebugBuild()) {
			return DEBUG_CONSTS.PORTAL_IMAGE_CACHE_DIR + appendix;
		} else {
			return PRODUCTION_CONSTS.PORTAL_IMAGE_CACHE_DIR + appendix;
		}
	}

	public static int getCleanupSeconds() {
		if (isDebugBuild()) {
			return DEBUG_CONSTS.CLEANUP_SECONDS;
		} else {
			return PRODUCTION_CONSTS.CLEANUP_SECONDS;
		}
	}
}
