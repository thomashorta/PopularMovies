package com.thomashorta.popularmovies.data.favorites;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.thomashorta.popularmovies.data.favorites.FavoriteContract.FavoriteEntry;

public class FavoriteDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";

    private static final int DATABASE_VERSION = 1;

    public FavoriteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITE_TABLE =
                "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoriteEntry.COLUMN_TMDB_ID + " INTEGER NOT NULL, " +
                FavoriteEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                "UNIQUE (" + FavoriteEntry.COLUMN_TMDB_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        onCreate(db);
    }
}
