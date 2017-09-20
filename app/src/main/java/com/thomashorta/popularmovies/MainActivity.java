package com.thomashorta.popularmovies;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.thomashorta.popularmovies.adapters.MovieGridAdapter;
import com.thomashorta.popularmovies.data.PopularMoviesPreferences;
import com.thomashorta.popularmovies.data.favorites.FavoriteContract;
import com.thomashorta.popularmovies.loaders.MovieListLoader;
import com.thomashorta.popularmovies.loaders.PagedListLoader;
import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;
import com.thomashorta.popularmovies.moviedb.objects.MovieInfo;
import com.thomashorta.popularmovies.moviedb.objects.MovieList;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements MovieGridAdapter.OnMovieClickListener {
    public static final String EXTRA_SAVED_MOVIE_LIST = "saved_movie_info_list";
    private static final int FAVORITE_LOADER_ID = 984;

    private RecyclerView mMovieGrid;
    private ProgressBar mLoadingIndicator;
    private View mErrorReloadMessage;

    private MovieListLoader mMovieListLoader;
    private MovieGridAdapter mMovieGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorReloadMessage = findViewById(R.id.error_reload_message);

        int gridColumns = getResources().getInteger(R.integer.grid_columns);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, gridColumns);

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

                if (mMovieListLoader != null
                        && !mMovieListLoader.isLoading() && mMovieListLoader.hasMore()
                        && lastVisibleItem >= itemCount - visibleThreshold) {
                    startLoader(mMovieListLoader.getLastLoadedPage() + 1);
                }
            }
        });

        // reload adapter state
        if (savedInstanceState != null) {
            ArrayList<MovieInfo> savedMovieList = savedInstanceState
                    .getParcelableArrayList(EXTRA_SAVED_MOVIE_LIST);
            if (savedMovieList != null) mMovieGridAdapter.setMovieInfoList(savedMovieList);
        }

        startLoader(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<MovieInfo> movieInfoList = mMovieGridAdapter.getMovieInfoList();
        outState.putParcelableArrayList(EXTRA_SAVED_MOVIE_LIST, movieInfoList);
    }

    @Override
    public void onMovieClick(MovieInfo itemMovieInfo, ImageView itemImageView) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE_ID, itemMovieInfo.getId());

        Bitmap posterBitmap = ((BitmapDrawable) itemImageView.getDrawable()).getBitmap();
        if (posterBitmap != null) {
            Palette palette = Palette.from(posterBitmap).generate();
            int titleBgColor = palette.getDarkMutedColor(getResources().getColor(R.color.default_title_bg));
            int titleTextColor = palette.getLightVibrantColor(getResources().getColor(R.color.default_title_text));

            intent.putExtra(MovieDetailsActivity.EXTRA_TITLE_BG, titleBgColor);
            intent.putExtra(MovieDetailsActivity.EXTRA_TITLE_COLOR, titleTextColor);
        }

        startActivity(intent);
    }

    private void startLoader(Integer page) {
        LoaderManager lm = getSupportLoaderManager();
        if (PopularMoviesPreferences.getPreferredSortCriteria() ==
                TheMovieDbHelper.SortCriteria.FAVORITES) {
            mMovieListLoader = null;
            lm.initLoader(FAVORITE_LOADER_ID, null, mFavoritesLoaderCallbacks);
        } else {
            Bundle b = new Bundle(1);
            if (page != null) b.putInt(PagedListLoader.EXTRA_PAGE_TO_LOAD, page);
            if (mMovieListLoader != null) {
                mMovieListLoader = (MovieListLoader) lm.restartLoader(MovieListLoader.LOADER_ID, b,
                        mMovieListLoaderCallbacks);
            } else {
                mMovieListLoader = (MovieListLoader) lm.initLoader(MovieListLoader.LOADER_ID, b,
                        mMovieListLoaderCallbacks);
            }
        }
    }

    private void showLoadingIndicator() {
        mMovieGridAdapter.clear();
        showMovieGridView();
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showMovieGridView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorReloadMessage.setVisibility(View.INVISIBLE);
        mMovieGrid.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
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
        stringOptions.add(getString(R.string.option_favorites));
        sortCriteriaOptions.add(TheMovieDbHelper.SortCriteria.FAVORITES);

        int selectedItem = sortCriteriaOptions.indexOf(
                PopularMoviesPreferences.getPreferredSortCriteria());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_choose_display_criteria_title);
        builder.setSingleChoiceItems(stringOptions.toArray(new String[3]), selectedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TheMovieDbHelper.SortCriteria criteria = sortCriteriaOptions.get(which);
                        PopularMoviesPreferences.setPreferredSortCriteria(criteria);
                        startLoader(null);
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

    private LoaderManager.LoaderCallbacks<Cursor> mFavoritesLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    showLoadingIndicator();
                    return new CursorLoader(MainActivity.this,
                            FavoriteContract.FavoriteEntry.CONTENT_URI, null, null, null, null);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    if (data != null) {
                        // success
                        if (data.getCount() != 0) {
                            // not empty
                            showMovieGridView();
                            mMovieGridAdapter.setFavoritesCursor(data);
                        } else {
                            // just show empty
                            mMovieGridAdapter.clear();
                            showMovieGridView();
                        }
                    } else {
                        // error
                        showErrorMessage();
                    }
                    loader.abandon();
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    private LoaderManager.LoaderCallbacks<MovieList> mMovieListLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<MovieList>() {
                @Override
                public Loader<MovieList> onCreateLoader(int id, Bundle args) {
                    int page = args != null ? args.getInt(PagedListLoader.EXTRA_PAGE_TO_LOAD, 1) : 1;
                    if (page == 1) showLoadingIndicator();
                    return new MovieListLoader(MainActivity.this,
                            PopularMoviesPreferences.getPreferredSortCriteria(), page);
                }

                @Override
                public void onLoadFinished(Loader<MovieList> loader, MovieList data) {
                    MovieListLoader mlLoader = (MovieListLoader) loader;

                    if (data != null) {
                        // success
                        if (mlLoader.isFirstPage()) {
                            showMovieGridView();
                            mMovieGridAdapter.setMovieList(data);
                        } else {
                            mMovieGridAdapter.addMovieList(data);
                        }
                    } else {
                        // error
                        if (mlLoader.isFirstPage()) {
                            mLoadingIndicator.setVisibility(View.INVISIBLE);
                            showErrorMessage();
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<MovieList> loader) {

                }
            };
}
