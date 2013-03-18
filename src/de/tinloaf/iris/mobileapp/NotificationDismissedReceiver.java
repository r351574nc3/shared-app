package de.tinloaf.iris.mobileapp;

import java.sql.SQLException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;

import de.tinloaf.iris.mobileapp.data.DatabaseHelper;
import de.tinloaf.iris.mobileapp.data.Destruction;

public class NotificationDismissedReceiver extends BroadcastReceiver {

	
	@Override
	public void onReceive(Context context, Intent intent) {
		DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		try {
			Dao<Destruction, Integer> destrDao = helper.getDestructionDao();
			UpdateBuilder updateBuilder = destrDao.updateBuilder();
			updateBuilder.updateColumnValue("displayed", true);
			destrDao.update(updateBuilder.prepare());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		OpenHelperManager.releaseHelper();
	}

}
