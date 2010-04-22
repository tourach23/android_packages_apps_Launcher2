package com.android.launcher2;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

class HelixDBHelper extends SQLiteOpenHelper {

    private final Context mContext;

    HelixDBHelper(Context context) {
		//TABLE_FAVORITES
        super(context, LauncherProvider.DATABASE_NAME, null, LauncherProvider.DATABASE_VERSION);
        mContext = context;
    }

	@Override
    public void onCreate(SQLiteDatabase db) {
	}
	
	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
