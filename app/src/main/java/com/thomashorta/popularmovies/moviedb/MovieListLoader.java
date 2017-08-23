package com.thomashorta.popularmovies.moviedb;

import android.os.AsyncTask;
import android.util.Log;

import com.thomashorta.popularmovies.moviedb.objects.MovieList;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MovieListLoader {
    private static final String TAG = MovieListLoader.class.getSimpleName();

    private int mTotalPages;
    private int mLastLoadedPage;
    private boolean mIsLoading;

    private TheMovieDbHelper.SortCriteria mSortCriteria;
    private OnLoadListener mOnLoadListener;

    public interface OnLoadListener {
        void onPreLoad(boolean isFirstLoad);
        void onLoadSuccess(MovieList loadedList, boolean isFirstLoad);
        void onLoadError(boolean isFirstLoad);
    }

    public MovieListLoader(TheMovieDbHelper.SortCriteria sortCriteria,
                           OnLoadListener loadListener) {
        mSortCriteria = sortCriteria;
        mOnLoadListener = loadListener;
        mIsLoading = false;
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    public void setTotalPages(int totalPages) {
        this.mTotalPages = totalPages;
    }

    public int getLoadedPages() {
        return mLastLoadedPage;
    }

    public void setLoadedPages(int loadedPages) {
        this.mLastLoadedPage = loadedPages;
    }

    public TheMovieDbHelper.SortCriteria getSortCriteria() {
        return mSortCriteria;
    }

    public void setSortCriteria(TheMovieDbHelper.SortCriteria sortCriteria) {
        this.mSortCriteria = sortCriteria;
    }

    public void setLoadListener(OnLoadListener loadListener) {
        mOnLoadListener = loadListener;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public boolean hasMore() {
        return mLastLoadedPage < mTotalPages;
    }

    public void loadFirst() {
        new FetchMoviesTask(null).execute();
    }

    public void loadMore() {
        if (hasMore()) new FetchMoviesTask(mLastLoadedPage + 1).execute();
    }

    private class FetchMoviesTask extends AsyncTask<Void, Void, String> {
        Integer mPage = null;

        private FetchMoviesTask(Integer page) {
            // page is null or 1 for first page
            mPage = page;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsLoading = true;
            if (mOnLoadListener != null) mOnLoadListener.onPreLoad(isFirstLoad());
        }

        @Override
        protected String doInBackground(Void... params) {
            String movieListJson;

            String requestUrl = TheMovieDbHelper.buildMovieListURL(mSortCriteria, mPage);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();

                if (response.code() == HttpURLConnection.HTTP_OK && responseBody != null) {
                    movieListJson = responseBody.string();
                } else {
                    throw new IOException("Got error response code from server.");
                }
            } catch (IOException e) {
                movieListJson = null;
                Log.w(TAG, "An error occurred when trying to request data.", e);
            }

            return movieListJson;
        }

        @Override
        protected void onPostExecute(String movieListJson) {
            mIsLoading = false;
            if (movieListJson != null) {
                MovieList movieList = MovieList.fromJson(movieListJson);
                mTotalPages = movieList.getTotalPages();
                mLastLoadedPage = movieList.getPage();
                if (mOnLoadListener != null) mOnLoadListener.onLoadSuccess(movieList, isFirstLoad());
            } else {
                if (mOnLoadListener != null) mOnLoadListener.onLoadError(isFirstLoad());
            }
        }

        private boolean isFirstLoad() {
            return mPage == null || mPage == 1;
        }
    }
}
