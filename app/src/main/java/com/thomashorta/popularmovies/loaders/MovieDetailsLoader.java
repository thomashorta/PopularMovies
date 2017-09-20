package com.thomashorta.popularmovies.loaders;

import android.support.v7.app.AppCompatActivity;

import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;
import com.thomashorta.popularmovies.moviedb.objects.MovieDetails;

public class MovieDetailsLoader extends SimpleLoader<MovieDetails> {
    public static final int LOADER_ID = 184;

    private long mMovieId;

    public MovieDetailsLoader(AppCompatActivity activity, long movieId) {
        super(activity, movieId, MovieDetails.class);
        mMovieId = movieId;
    }

    @Override
    protected String getRequestURL(long movieId) {
        return TheMovieDbHelper.buildMovieDetailsURL(mMovieId);
    }
}
