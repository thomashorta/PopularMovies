package com.thomashorta.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;
import com.thomashorta.popularmovies.moviedb.objects.MovieDetails;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MovieDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_ID = "extra_movie_id";
    public static final String EXTRA_TITLE_BG = "extra_title_bg";
    public static final String EXTRA_TITLE_COLOR = "extra_title_color";

    private ViewGroup mRootLayout;
    private ProgressBar mLoadingIndicator;
    private View mErrorReloadMessage;

    private int mMovieId;

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
        loadMovieDetails();
    }

    private void loadMovieDetails() {
        hideAllViews();
        new MovieDetailsTask().execute(mMovieId);
    }

    private void hideAllViews() {
        mRootLayout.setVisibility(View.INVISIBLE);
        mErrorReloadMessage.setVisibility(View.INVISIBLE);
    }

    private void showMovieDetails(MovieDetails movieDetails) {
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

        mErrorReloadMessage.setVisibility(View.INVISIBLE);
        mRootLayout.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRootLayout.setVisibility(View.INVISIBLE);
        mErrorReloadMessage.setVisibility(View.VISIBLE);
    }

    private class MovieDetailsTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            String movieDetailsJson = null;
            if (params != null && params.length > 0) {
                String requestUrl = TheMovieDbHelper.buildMovieDetailURL(params[0]);

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    ResponseBody responseBody = response.body();

                    if (response.code() == HttpURLConnection.HTTP_OK && responseBody != null) {
                        movieDetailsJson = responseBody.string();
                    } else {
                        throw new IOException("Got error response code from server.");
                    }
                } catch (IOException e) {
                    movieDetailsJson = null;
                }
            }
            return movieDetailsJson;
        }

        @Override
        protected void onPostExecute(String movieDetailsJson) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieDetailsJson != null) {
                showMovieDetails(MovieDetails.fromJson(movieDetailsJson));
            } else {
                showErrorMessage();
            }
        }
    }
}
