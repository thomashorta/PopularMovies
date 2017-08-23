package com.thomashorta.popularmovies;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ProgressBar;

import com.thomashorta.popularmovies.data.PopularMoviesPreferences;
import com.thomashorta.popularmovies.moviedb.MovieListLoader;
import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;
import com.thomashorta.popularmovies.moviedb.objects.MovieInfo;
import com.thomashorta.popularmovies.moviedb.objects.MovieList;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements MovieGridAdapter.OnMovieClickListener, MovieListLoader.OnLoadListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mMovieGrid;
    private ProgressBar mLoadingIndicator;
    private View mErrorReloadMessage;

    private MovieListLoader mMovieListLoader;
    private MovieGridAdapter mMovieGridAdapter;

    private int mGridColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorReloadMessage = findViewById(R.id.error_reload_message);

        mGridColumns = getResources().getInteger(R.integer.grid_columns);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, mGridColumns);

        mMovieGrid = (RecyclerView) findViewById(R.id.rv_movie_grid);
        mMovieGrid.setLayoutManager(gridLayoutManager);
        mMovieGrid.setHasFixedSize(true);

        mMovieGridAdapter = new MovieGridAdapter(this);
        mMovieGrid.setAdapter(mMovieGridAdapter);
        mMovieGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy < 0) return;

                int itemCount = gridLayoutManager.getItemCount();
                int lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();
                int visibleThreshold = getResources().getInteger(R.integer.visible_threshold);

                Log.d(TAG, "count=" + itemCount + "/lastItem=" + lastVisibleItem + "/threshold=" + visibleThreshold);
                if (!mMovieListLoader.isLoading() && mMovieListLoader.hasMore()
                        && lastVisibleItem >= itemCount - visibleThreshold) {
                    mMovieListLoader.loadMore();
                }
            }
        });

        mMovieListLoader =
                new MovieListLoader(PopularMoviesPreferences.getPreferredSortCriteria(), this);
        mMovieListLoader.loadFirst();
    }

    @Override
    public void onMovieClick(MovieInfo itemMovieInfo) {
        Log.d(TAG, "movie_id: " + itemMovieInfo.getId());
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_ID, itemMovieInfo.getId());
        startActivity(intent);
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
                        mMovieListLoader.setSortCriteria(criteria);
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

    @Override
    public void onPreLoad(boolean isFirstLoad) {
        if (isFirstLoad) {
            mMovieGridAdapter.clear();
            showMovieGridView();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadSuccess(MovieList loadedList, boolean isFirstLoad) {
        if (isFirstLoad) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            showMovieGridView();
            mMovieGridAdapter.setMovieList(loadedList);
        } else {
            mMovieGridAdapter.addMovieList(loadedList);
        }
    }

    @Override
    public void onLoadError(boolean isFirstLoad) {
        if (isFirstLoad) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            showErrorMessage();
        }
    }
}
