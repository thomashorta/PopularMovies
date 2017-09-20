package com.thomashorta.popularmovies.loaders;

import android.content.Context;

import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;
import com.thomashorta.popularmovies.moviedb.objects.MovieList;

public class MovieListLoader extends PagedListLoader<MovieList> {
    public static int LOADER_ID = 217;

    private TheMovieDbHelper.SortCriteria mSortCriteria;

    public MovieListLoader(Context context, TheMovieDbHelper.SortCriteria sortCriteria, int page) {
        super(context, MovieList.class, page);
        mSortCriteria = sortCriteria;
    }

    @Override
    protected String getRequestURL(Integer page) {
        return TheMovieDbHelper.buildMovieListURL(mSortCriteria, page);
    }
}
