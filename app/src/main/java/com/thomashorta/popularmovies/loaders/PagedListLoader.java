package com.thomashorta.popularmovies.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.thomashorta.popularmovies.moviedb.objects.PagedList;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class PagedListLoader<T extends PagedList>
        extends AsyncTaskLoader<T> {
    private static final String TAG = PagedListLoader.class.getSimpleName();
    public static final String EXTRA_PAGE_TO_LOAD = "page_to_load";

    private Class mClassOfT;
    private int mTotalPages;
    private int mLastLoadedPage;
    private int mPageToLoad;
    private boolean mIsLoading;

    private T mCached;

    protected PagedListLoader(Context context, Class classOfT, int page) {
        super(context);
        mClassOfT = classOfT;
        mIsLoading = false;
        mPageToLoad = page;
    }


    public int getLastLoadedPage() {
        return mLastLoadedPage;
    }

    public boolean isFirstPage() {
        return mPageToLoad == 1;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public boolean hasMore() {
        return mLastLoadedPage < mTotalPages;
    }


    protected abstract String getRequestURL(Integer page);

    @Override
    protected void onStartLoading() {
        if (mPageToLoad <= 0) mPageToLoad = 1;

        mIsLoading = true;
        if (mCached != null) {
            deliverResult(mCached);
        } else {
            forceLoad();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T loadInBackground() {
        String objectListJson;
        String requestUrl = getRequestURL(mPageToLoad);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();

            if (response.code() == HttpURLConnection.HTTP_OK && responseBody != null) {
                objectListJson = responseBody.string();
            } else {
                throw new IOException("Got error response code from server.");
            }
        } catch (IOException e) {
            objectListJson = null;
            Log.w(TAG, "An error occurred when trying to request data.", e);
        }

        T objectList = null;
        if (objectListJson != null) {
            objectList = (T) PagedList.fromJson(objectListJson, mClassOfT);
            mTotalPages = objectList.getTotalPages();
            mLastLoadedPage = objectList.getPage();
        }
        return objectList;
    }

    @Override
    public void deliverResult(T data) {
        mIsLoading = false;
        mCached = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
        mIsLoading = false;
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        mPageToLoad = 1;
        mTotalPages = 0;
        mLastLoadedPage = 0;
        mCached = null;
    }
}
