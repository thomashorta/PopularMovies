package com.thomashorta.popularmovies.data.favorites;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoriteContract {
    public static final String CONTENT_AUTHORITY = "com.thomashorta.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FAVORITE = "favorite";

    public static final class FavoriteEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITE)
                .build();

        public static final String TABLE_NAME = "favorite";

        public static final String COLUMN_TMDB_ID = "tmdb_id";
        public static final String COLUMN_TITLE = "movie_name";
        public static final String COLUMN_POSTER_PATH = "poster_path";
    }
}
