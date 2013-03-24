package de.tinloaf.iris.mobileapp.data;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "iris.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;

	// the DAO object we use to access the Portal table
	private Dao<SavedPortal, Integer> portalDao = null;
	private RuntimeExceptionDao<SavedPortal, Integer> portalRuntimeDao = null;

	private Dao<SavedPortalImage, SavedPortal> portalImageDao = null;
	private RuntimeExceptionDao<SavedPortalImage, SavedPortal> portalImageRuntimeDao = null;
	
	private Dao<Destruction, Integer> destructionDao = null;
	private RuntimeExceptionDao<Destruction, Integer> destructionRuntimeDao = null;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, SavedPortal.class);
			TableUtils.createTable(connectionSource, SavedPortalImage.class);
			TableUtils.createTable(connectionSource, Destruction.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, SavedPortal.class, true);
			TableUtils.dropTable(connectionSource, SavedPortalImage.class, true);
			TableUtils.dropTable(connectionSource, Destruction.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the Database Access Object (DAO) for our Portal class. It will create it or just give the cached
	 * value.
	 */
	public Dao<SavedPortal, Integer> getPortalDao() throws SQLException {
		if (portalDao == null) {
			portalDao = getDao(SavedPortal.class);
		}
		return portalDao;
	}
	
	public Dao<Destruction, Integer> getDestructionDao() throws SQLException {
		if (destructionDao == null) {
			destructionDao = getDao(Destruction.class);
		}
		return destructionDao;
	}
	
	public Dao<SavedPortalImage, SavedPortal> getPortalImageDao() throws SQLException {
		if (portalImageDao == null) {
			portalImageDao = getDao(SavedPortalImage.class);
		}
		return portalImageDao;
	}

	/**
	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our Portal class. It will
	 * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<SavedPortal, Integer> getPortalRuntimeDao() {
		if (portalRuntimeDao == null) {
			portalRuntimeDao = getRuntimeExceptionDao(SavedPortal.class);
		}
		return portalRuntimeDao;
	}

	public RuntimeExceptionDao<SavedPortalImage, SavedPortal> getPortalImageRuntimeDao() {
		if (portalImageRuntimeDao == null) {
			portalImageRuntimeDao = getRuntimeExceptionDao(SavedPortalImage.class);
		}
		return portalImageRuntimeDao;
	}
	
	public RuntimeExceptionDao<Destruction, Integer> getDestructionRuntimeDao() {
		if (destructionRuntimeDao == null) {
			destructionRuntimeDao = getRuntimeExceptionDao(Destruction.class);
		}
		return destructionRuntimeDao;
	}
	
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		portalRuntimeDao = null;
		portalImageRuntimeDao = null;
	}
}