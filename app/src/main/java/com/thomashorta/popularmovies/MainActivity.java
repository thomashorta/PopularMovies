package com.thomashorta.popularmovies;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.thomashorta.popularmovies.data.PopularMoviesPreferences;
import com.thomashorta.popularmovies.moviedb.MovieInfo;
import com.thomashorta.popularmovies.moviedb.MovieList;
import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity
        implements MovieGridAdapter.OnMovieClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int NUMBER_OF_ROWS = 2;

    private RecyclerView mMovieGrid;
    private ProgressBar mLoadingIndicator;
    private View mErrorReloadMessage;

    private MovieGridAdapter mMovieGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorReloadMessage = findViewById(R.id.error_reload_message);

        mMovieGrid = (RecyclerView) findViewById(R.id.rv_movie_grid);
        mMovieGrid.setLayoutManager(new GridLayoutManager(this, NUMBER_OF_ROWS));
        mMovieGrid.setHasFixedSize(true);

        mMovieGridAdapter = new MovieGridAdapter(this);
        mMovieGrid.setAdapter(mMovieGridAdapter);

        ImageView reloadButton = (ImageView) findViewById(R.id.iv_reload_button);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMovieGridAdapter.clear();
                loadMovieList();
            }
        });

        loadMovieList();
    }

    @Override
    public void onMovieClick(MovieInfo itemMovieInfo) {
        // for now just display a Toast
        Toast.makeText(this, itemMovieInfo.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void loadMovieList() {
        showMovieGridView();

        TheMovieDbHelper.SortCriteria sortCriteria = PopularMoviesPreferences.getPreferredSortCriteria();
        new FetchMoviesTask(sortCriteria).execute();
    }

    private void showMovieGridView() {
        mErrorReloadMessage.setVisibility(View.INVISIBLE);
        mMovieGrid.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mMovieGrid.setVisibility(View.INVISIBLE);
        mErrorReloadMessage.setVisibility(View.VISIBLE);
    }

    private void showSortCriteriaDialog() {
        List<String> stringOptions = new ArrayList<>(2);
        final List<TheMovieDbHelper.SortCriteria> sortCriteriaOptions = new ArrayList<>(2);

        stringOptions.add(getString(R.string.option_top_rated));
        sortCriteriaOptions.add(TheMovieDbHelper.SortCriteria.TOP_RATED);
        stringOptions.add(getString(R.string.option_popular));
        sortCriteriaOptions.add(TheMovieDbHelper.SortCriteria.POPULAR);

        int selectedItem = sortCriteriaOptions.indexOf(
                PopularMoviesPreferences.getPreferredSortCriteria());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_choose_sort_criteria_title);
        builder.setSingleChoiceItems(stringOptions.toArray(new String[2]), selectedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TheMovieDbHelper.SortCriteria criteria = sortCriteriaOptions.get(which);
                        PopularMoviesPreferences.setPreferredSortCriteria(criteria);
                        mMovieGridAdapter.clear();
                        loadMovieList();
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_criteria) {
            showSortCriteriaDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchMoviesTask extends AsyncTask<Integer, Void, String> {

        private TheMovieDbHelper.SortCriteria mSortCriteria;

        public FetchMoviesTask(TheMovieDbHelper.SortCriteria sortCriteria) {
            mSortCriteria = sortCriteria;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            String movieListJson;

            Integer page = (params != null && params.length != 0) ? params[0] : null;
            String requestUrl = TheMovieDbHelper.buildMovieListURL(mSortCriteria, page);

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
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieListJson != null) {
                showMovieGridView();
                mMovieGridAdapter.setMovieList(MovieList.fromJson(movieListJson));
            } else {
                showErrorMessage();
            }
        }
    }
}
