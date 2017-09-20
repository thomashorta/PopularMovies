package com.thomashorta.popularmovies.loaders;

import android.support.v7.app.AppCompatActivity;

import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;
import com.thomashorta.popularmovies.moviedb.objects.TrailerList;

public class TrailerListLoader extends SimpleLoader<TrailerList> {
    public static final int LOADER_ID = 841;

    private long mMovieId;

    public TrailerListLoader(AppCompatActivity activity, long movieId) {
        super(activity, movieId, TrailerList.class);
        mMovieId = movieId;
    }

    @Override
    protected String getRequestURL(long movieId) {
        return TheMovieDbHelper.buildMovieTrailersURL(mMovieId);
    }
}
