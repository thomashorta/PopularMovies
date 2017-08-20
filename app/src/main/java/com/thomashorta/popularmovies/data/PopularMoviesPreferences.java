package com.thomashorta.popularmovies.data;

import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper.SortCriteria;

public class PopularMoviesPreferences {
    private static final SortCriteria DEFAULT_SORT_CRITERIA = SortCriteria.TOP_RATED;

    // for now use only static variables instead of SharedPrefs
    private static SortCriteria sPreferredSortCriteria;

    private static SortCriteria getDefaultSortCriteria() {
        return DEFAULT_SORT_CRITERIA;
    }

    public static SortCriteria getPreferredSortCriteria() {
        return sPreferredSortCriteria != null ? sPreferredSortCriteria : getDefaultSortCriteria();
    }

    public static void setPreferredSortCriteria(SortCriteria sortCriteria) {
        sPreferredSortCriteria = sortCriteria;
    }
}
