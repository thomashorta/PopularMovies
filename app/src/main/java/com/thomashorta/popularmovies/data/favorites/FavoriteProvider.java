package com.thomashorta.popularmovies.data.favorites;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class FavoriteProvider extends ContentProvider {

    private static final int CODE_FAVORITE = 100;
    private static final int FAVORITE_WITH_TMDB_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoriteDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoriteContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, FavoriteContract.PATH_FAVORITE, CODE_FAVORITE);
        matcher.addURI(authority, FavoriteContract.PATH_FAVORITE + "/#", FAVORITE_WITH_TMDB_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new FavoriteDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        switch(sUriMatcher.match(uri)) {
            case CODE_FAVORITE:
                cursor = db.query(
                        FavoriteContract.FavoriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVORITE_WITH_TMDB_ID:
                String tmdbId = uri.getPathSegments().get(1);
                cursor = db.query(
                        FavoriteContract.FavoriteEntry.TABLE_NAME,
                        projection,
                        FavoriteContract.FavoriteEntry.COLUMN_TMDB_ID + "=?",
                        new String[] {tmdbId},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri returnUri;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case CODE_FAVORITE:
                long _id = db.insert(FavoriteContract.FavoriteEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ContentUris
                            .withAppendedId(FavoriteContract.FavoriteEntry.CONTENT_URI, _id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int deletedEntries;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case FAVORITE_WITH_TMDB_ID:
                String tmdbId = uri.getPathSegments().get(1);
                deletedEntries = db.delete(FavoriteContract.FavoriteEntry.TABLE_NAME,
                        FavoriteContract.FavoriteEntry.COLUMN_TMDB_ID + "=?", new String[]{tmdbId});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (deletedEntries != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedEntries;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
