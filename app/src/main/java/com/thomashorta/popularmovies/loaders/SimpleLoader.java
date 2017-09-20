package com.thomashorta.popularmovies.loaders;

import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;

import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;
import com.thomashorta.popularmovies.moviedb.Util;
import com.thomashorta.popularmovies.moviedb.objects.MovieDetails;
import com.thomashorta.popularmovies.moviedb.objects.TrailerList;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class SimpleLoader<T> extends AsyncTaskLoader<T> {
    public static final int LOADER_ID = 841;

    private long mMovieId;
    private T mCached;
    private Class mClassOfT;

    public SimpleLoader(AppCompatActivity activity, long movieId, Class<T> classOfT) {
        super(activity);
        mMovieId = movieId;
        mClassOfT = classOfT;
    }

    protected abstract String getRequestURL(long movieId);

    @Override
    protected void onStartLoading() {
        if (mCached != null) {
            deliverResult(mCached);
        } else {
            forceLoad();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T loadInBackground() {
        String simpleJson = null;

        String requestUrl = getRequestURL(mMovieId);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();

            if (response.code() == HttpURLConnection.HTTP_OK && responseBody != null) {
                simpleJson = responseBody.string();
            } else {
                throw new IOException("Got error response code from server.");
            }
        } catch (IOException e) {
            simpleJson = null;
        }

        return simpleJson != null ? (T) Util.fromJson(simpleJson, mClassOfT) : null;
    }

    @Override
    public void deliverResult(T data) {
        mCached = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        mCached = null;
    }
}
