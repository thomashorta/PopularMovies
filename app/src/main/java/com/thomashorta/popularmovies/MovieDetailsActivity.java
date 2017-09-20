package com.thomashorta.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.thomashorta.popularmovies.adapters.ReviewListAdapter;
import com.thomashorta.popularmovies.adapters.TrailerListAdapter;
import com.thomashorta.popularmovies.data.favorites.FavoriteContract;
import com.thomashorta.popularmovies.loaders.MovieDetailsLoader;
import com.thomashorta.popularmovies.loaders.PagedListLoader;
import com.thomashorta.popularmovies.loaders.ReviewListLoader;
import com.thomashorta.popularmovies.loaders.TrailerListLoader;
import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;
import com.thomashorta.popularmovies.moviedb.objects.MovieDetails;
import com.thomashorta.popularmovies.moviedb.objects.Review;
import com.thomashorta.popularmovies.moviedb.objects.ReviewList;
import com.thomashorta.popularmovies.moviedb.objects.TrailerList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MovieDetailsActivity extends AppCompatActivity {
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();
    private static final String EXTRA_SAVED_REVIEW_LIST = "extra_saved_review_list";
    private static final String EXTRA_SAVED_SCROLL_POSITION = "extra_saved_scroll_position";

    public static final String EXTRA_MOVIE_ID = "extra_movie_id";
    public static final String EXTRA_TITLE_BG = "extra_title_bg";
    public static final String EXTRA_TITLE_COLOR = "extra_title_color";

    private ViewGroup mRootLayout;
    private NestedScrollView mScrollView;
    private ProgressBar mLoadingIndicator;
    private View mErrorReloadMessage;
    private RecyclerView mReviewList;
    private RecyclerView mTrailerList;

    private ReviewListAdapter mReviewListAdapter;
    private ReviewListLoader mReviewLoader;

    private TrailerListAdapter mTrailerListAdapter;

    private MovieDetails mMovieDetails;

    private long mMovieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mRootLayout = (ViewGroup) findViewById(R.id.root_layout);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorReloadMessage = findViewById(R.id.error_reload_message);

        ImageView reloadButton = (ImageView) findViewById(R.id.iv_reload_button);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMovieDetails();
            }
        });

        Intent intent = getIntent();

        if (!intent.hasExtra(EXTRA_MOVIE_ID)) {
            // show an error toast and go back (something went very wrong)
            Toast.makeText(this, "Invalid movie selected.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (intent.hasExtra(EXTRA_TITLE_BG)) {
            FrameLayout titleBg = (FrameLayout) findViewById(R.id.frame_detail_bg);
            titleBg.setBackgroundColor(intent.getIntExtra(EXTRA_TITLE_BG, 0));
        }

        if (intent.hasExtra(EXTRA_TITLE_COLOR)) {
            TextView titleBg = (TextView) findViewById(R.id.tv_detail_title);
            titleBg.setTextColor(intent.getIntExtra(EXTRA_TITLE_COLOR, 0));
        }

        mMovieId = intent.getIntExtra(EXTRA_MOVIE_ID, 0);
        Log.d(TAG, "Movie ID: " + mMovieId);

        mScrollView = (NestedScrollView) findViewById(R.id.scrollable_view);

        // Trailer List
        mTrailerList = (RecyclerView) findViewById(R.id.rv_trailer_list);
        mTrailerList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mTrailerList.setHasFixedSize(true);
        mTrailerList.setNestedScrollingEnabled(false);

        mTrailerListAdapter = new TrailerListAdapter();
        mTrailerList.setAdapter(mTrailerListAdapter);

        // Review List
        mReviewList = (RecyclerView) findViewById(R.id.rv_review_list);
        final LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mReviewList.setLayoutManager(reviewLayoutManager);
        mReviewList.setHasFixedSize(false);
        mReviewList.setNestedScrollingEnabled(false);
        mReviewList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy < 0) return;

                int itemCount = reviewLayoutManager.getItemCount();
                int lastVisibleItem = reviewLayoutManager.findLastVisibleItemPosition();
                int visibleThreshold = getResources().getInteger(R.integer.visible_threshold);

                if (mReviewLoader != null
                        && !mReviewLoader.isLoading() && mReviewLoader.hasMore()
                        && lastVisibleItem >= itemCount - visibleThreshold) {
                    startReviewLoader(mReviewLoader.getLastLoadedPage() + 1);
                }
            }
        });

        mReviewListAdapter = new ReviewListAdapter();
        mReviewList.setAdapter(mReviewListAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_SAVED_REVIEW_LIST)) {
            ArrayList<Review> savedReviews = savedInstanceState
                    .getParcelableArrayList(EXTRA_SAVED_REVIEW_LIST);
            if (savedReviews != null) mReviewListAdapter.setReviews(savedReviews);

            int scrollPosition = savedInstanceState.getInt(EXTRA_SAVED_SCROLL_POSITION, -1);
            if (scrollPosition > -1) mScrollView.setScrollY(scrollPosition);
        }

        loadMovieDetails();
        loadMovieTrailers();
        loadMovieReviews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save reviews list state
        ArrayList<Review> reviews = mReviewListAdapter.getReviews();
        outState.putParcelableArrayList(EXTRA_SAVED_REVIEW_LIST, reviews);

        // save scroll list scroll
        outState.putInt(EXTRA_SAVED_SCROLL_POSITION, mScrollView.getScrollY());
    }

    private void loadMovieDetails() {
        startSimpleLoader(MovieDetailsLoader.LOADER_ID);
    }

    private void loadMovieTrailers() {
        startSimpleLoader(TrailerListLoader.LOADER_ID);
    }

    private void loadMovieReviews() {
        startReviewLoader(null);
    }

    private void updateFavoriteButton() {
        ImageView ivFavorite = (ImageView) findViewById(R.id.iv_favorite);
        if (isFavorite()) {
            ivFavorite.setImageDrawable(getDrawable(R.drawable.ic_favorite_on_24dp));
            ivFavorite.setContentDescription(getString(R.string.remove_favorites));
        } else {
            ivFavorite.setImageDrawable(getDrawable(R.drawable.ic_favorite_off_24dp));
            ivFavorite.setContentDescription(getString(R.string.add_favorites));
        }
    }

    private void onClickFavorite() {
        if (isFavorite()) {
            deleteFavorite();
        } else {
            addFavorite();
        }
        updateFavoriteButton();
    }

    private boolean isFavorite() {
        Uri uriWithId = FavoriteContract.FavoriteEntry.CONTENT_URI
                .buildUpon().appendPath(String.valueOf(mMovieId)).build();
        Cursor cursor = getContentResolver().query(uriWithId,
                new String[] {FavoriteContract.FavoriteEntry._ID},
                null, null, null);

        boolean isFavorite = cursor != null && cursor.getCount() != 0;
        if (cursor != null) cursor.close();

        return isFavorite;
    }

    private void addFavorite() {
        ContentValues favorite = new ContentValues();
        favorite.put(FavoriteContract.FavoriteEntry.COLUMN_TMDB_ID, mMovieId);
        favorite.put(FavoriteContract.FavoriteEntry.COLUMN_TITLE, mMovieDetails.getTitle());
        favorite.put(FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH, mMovieDetails.getPosterPath());

        getContentResolver().insert(FavoriteContract.FavoriteEntry.CONTENT_URI, favorite);
    }

    private void deleteFavorite() {
        Uri uriWithId = FavoriteContract.FavoriteEntry.CONTENT_URI
                .buildUpon().appendPath(String.valueOf(mMovieId)).build();
        getContentResolver().delete(uriWithId, null, null);
    }

    private void hideAllViews() {
        mRootLayout.setVisibility(View.INVISIBLE);
        mErrorReloadMessage.setVisibility(View.INVISIBLE);
    }

    private void showLoadingIndicator() {
        hideAllViews();
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showMovieDetails(MovieDetails movieDetails) {
        mMovieDetails = movieDetails;

        ImageView imageViewPoster = (ImageView) findViewById(R.id.iv_detail_poster);
        Picasso.with(this).cancelRequest(imageViewPoster);
        // Load the movie poster
        Picasso.with(this)
                .load(TheMovieDbHelper.buildPosterURL(movieDetails.getPosterPath(),
                        TheMovieDbHelper.PosterSize.MEDIUM))
                .placeholder(R.drawable.placeholder_image_27x40)
                .fit()
                .centerCrop()
                .into(imageViewPoster);

        TextView textViewReference;
        Locale currentLocale = Locale.getDefault();

        // set title TextView
        textViewReference = (TextView) findViewById(R.id.tv_detail_title);
        textViewReference.setText(movieDetails.getTitle());

        // set release TextView
        textViewReference = (TextView) findViewById(R.id.tv_detail_release);
        Calendar cal = Calendar.getInstance();
        cal.setTime(movieDetails.getReleaseDate());
        textViewReference.setText(String.valueOf(cal.get(Calendar.YEAR)));

        // set duration TextView
        textViewReference = (TextView) findViewById(R.id.tv_detail_duration);
        textViewReference.setText(String.format(currentLocale, "%dmin", movieDetails.getRuntime()));

        // set score TextView
        textViewReference = (TextView) findViewById(R.id.tv_detail_score);
        textViewReference.setText(String.format(currentLocale, "%.1f/10", movieDetails.getVoteAverage()));

        // set summary TextView
        textViewReference = (TextView) findViewById(R.id.tv_detail_summary);
        textViewReference.setText(movieDetails.getOverview());

        ImageView ivFavorite = (ImageView) findViewById(R.id.iv_favorite);
        ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickFavorite();
            }
        });
        updateFavoriteButton();

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorReloadMessage.setVisibility(View.INVISIBLE);
        mRootLayout.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRootLayout.setVisibility(View.INVISIBLE);
        mErrorReloadMessage.setVisibility(View.VISIBLE);
    }

    private void showReviewMessage(String text) {
        TextView tvReview = (TextView) findViewById(R.id.tv_review_message);
        tvReview.setText(text);
        mReviewList.setVisibility(View.GONE);
        tvReview.setVisibility(View.VISIBLE);
    }

    private void showReviewList() {
        TextView tvReview = (TextView) findViewById(R.id.tv_review_message);
        tvReview.setVisibility(View.GONE);
        mReviewList.setVisibility(View.VISIBLE);
    }

    private void showTrailerMessage(String text) {
        TextView tvTrailer = (TextView) findViewById(R.id.tv_trailer_message);
        tvTrailer.setText(text);
        mTrailerList.setVisibility(View.GONE);
        tvTrailer.setVisibility(View.VISIBLE);
    }

    private void showTrailerList() {
        TextView tvTrailer = (TextView) findViewById(R.id.tv_trailer_message);
        tvTrailer.setVisibility(View.GONE);
        mTrailerList.setVisibility(View.VISIBLE);
    }

    private void startSimpleLoader(int loaderId) {
        LoaderManager lm = getSupportLoaderManager();
        lm.initLoader(loaderId, null, simpleLoaderCallbacks);
    }

    private LoaderManager.LoaderCallbacks simpleLoaderCallbacks =
            new LoaderManager.LoaderCallbacks() {
                @Override
                public Loader onCreateLoader(int id, Bundle args) {
                    // create appropriate loader for ID
                    switch (id) {
                        case TrailerListLoader.LOADER_ID:
                            return new TrailerListLoader(MovieDetailsActivity.this, mMovieId);
                        case MovieDetailsLoader.LOADER_ID:
                            showLoadingIndicator();
                            return new MovieDetailsLoader(MovieDetailsActivity.this, mMovieId);
                        default:
                            Log.w(TAG, "No implementation for loader with id=" + id);
                    }
                    return null;
                }

                @Override
                public void onLoadFinished(Loader loader, Object data) {
                    // set the right results based on loader id and data
                    int id = loader.getId();
                    switch (id) {
                        case TrailerListLoader.LOADER_ID:
                            if (data != null) {
                                // success
                                TrailerList trailerList = (TrailerList) data;
                                if (trailerList.getResults().isEmpty()) {
                                    showTrailerMessage(getString(R.string.no_trailers_available));
                                } else {
                                    showTrailerList();
                                    mTrailerListAdapter.setTrailerList(trailerList);
                                }
                            } else {
                                // error
                                showTrailerMessage(getString(R.string.error_loading_trailers));
                            }
                            break;
                        case MovieDetailsLoader.LOADER_ID:
                            if (data != null) {
                                // success
                                showMovieDetails((MovieDetails) data);
                            } else {
                                // error
                                showErrorMessage();
                            }
                            break;
                        default:
                            Log.w(TAG, "No implementation for loader with id=" + id);
                    }
                }

                @Override
                public void onLoaderReset(Loader loader) {

                }
            };

    private void startReviewLoader(Integer page) {
        LoaderManager lm = getSupportLoaderManager();
        Bundle b = new Bundle(1);
        if (page != null) b.putInt(PagedListLoader.EXTRA_PAGE_TO_LOAD, page);
        if (mReviewLoader != null) {
            mReviewLoader = (ReviewListLoader) lm.restartLoader(ReviewListLoader.LOADER_ID, b,
                    reviewLoaderCallbacks);
        } else {
            mReviewLoader = (ReviewListLoader) lm.initLoader(ReviewListLoader.LOADER_ID, b,
                    reviewLoaderCallbacks);
        }
    }

    private LoaderManager.LoaderCallbacks<ReviewList> reviewLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<ReviewList>() {
                @Override
                public Loader<ReviewList> onCreateLoader(int id, Bundle args) {
                    int page = args != null ? args.getInt(PagedListLoader.EXTRA_PAGE_TO_LOAD, 1) : 1;
                    return new ReviewListLoader(MovieDetailsActivity.this, mMovieId, page);
                }

                @Override
                public void onLoadFinished(Loader<ReviewList> loader, ReviewList data) {
                    ReviewListLoader mlLoader = (ReviewListLoader) loader;

                    if (data != null) {
                        // success
                        if (data.getResults().isEmpty()) {
                            showReviewMessage(getString(R.string.no_reviews_available));
                        } else {
                            showReviewList();
                            if (mlLoader.isFirstPage()) {
                                mReviewListAdapter.setReviewList(data);
                            } else {
                                mReviewListAdapter.addReviewList(data);
                            }
                        }
                    } else {
                        // error
                        if (mlLoader.isFirstPage()) {
                            showReviewMessage(getString(R.string.error_loading_reviews));
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<ReviewList> loader) {

                }
            };
}
