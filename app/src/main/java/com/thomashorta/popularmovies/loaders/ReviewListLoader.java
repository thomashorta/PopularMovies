package com.thomashorta.popularmovies.loaders;

import android.support.v7.app.AppCompatActivity;

import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;
import com.thomashorta.popularmovies.moviedb.objects.ReviewList;

public class ReviewListLoader extends PagedListLoader<ReviewList> {
    public static int LOADER_ID = 968;
    private long mMovieId;

    public ReviewListLoader(AppCompatActivity activity, long movieId, int page) {
        super(activity, ReviewList.class, page);
        mMovieId = movieId;
    }

    @Override
    protected String getRequestURL(Integer page) {
        return TheMovieDbHelper.buildMovieReviewsURL(mMovieId, page);
    }
}
