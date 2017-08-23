package com.thomashorta.popularmovies.moviedb;

import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class TheMovieDbHelper {

    private static final String TAG = TheMovieDbHelper.class.getSimpleName();

    private static final String BASE_API_URL = "https://api.themoviedb.org/3";
    private static final String PATH_TOP_RATED = "movie/top_rated";
    private static final String PATH_POPULAR = "movie/popular";
    private static final String PATH_DETAIL = "movie/";

    private static final String BASE_POSTER_URL = "https://image.tmdb.org/t/p";
    private static final String PATH_POSTER_SIZE_THUMB = "w185";
    private static final String PATH_POSTER_SIZE_MEDIUM = "w500";
    private static final String PATH_POSTER_SIZE_BIG = "w780";
    private static final String PATH_POSTER_SIZE_ORIGINAL = "original";

    private static final String PARAMETER_PAGE_KEY = "page";

    private static final String PARAMETER_AUTH_API_KEY = "api_key";
    private static final String PARAMETER_AUTH_API_VALUE = ""; // put personal api key here

    public enum SortCriteria {
        TOP_RATED,
        POPULAR
    }

    public enum PosterSize {
        THUMB,
        MEDIUM,
        BIG,
        ORIGINAL
    }

    private static String buildURLString(String baseURL, HashMap<String, String> queryParameters,
                                         String... pathSegments) {
        Uri.Builder uriBuilder = Uri.parse(baseURL).buildUpon();

        for (String path : pathSegments) uriBuilder.appendEncodedPath(path);
        if (queryParameters != null) {
            for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
                uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        uriBuilder.appendQueryParameter(PARAMETER_AUTH_API_KEY, PARAMETER_AUTH_API_VALUE);

        return uriBuilder.build().toString();
    }

    public static String buildMovieListURL(SortCriteria sortCriteria, Integer page) {
        String sortPathSegment;
        switch (sortCriteria) {
            case POPULAR:
                sortPathSegment = PATH_POPULAR;
                break;
            case TOP_RATED:
            default:
                sortPathSegment = PATH_TOP_RATED;
                break;
        }

        HashMap<String, String> queryParameters = null;
        if (page != null) {
            queryParameters = new HashMap<>(1);
            queryParameters.put(PARAMETER_PAGE_KEY, String.valueOf(page));
        }

        return buildURLString(BASE_API_URL, queryParameters, sortPathSegment);
    }

    public static String buildPosterURL(String posterPath, PosterSize posterSize) {
        String sizePathSegment;
        switch (posterSize) {
            case MEDIUM:
                sizePathSegment = PATH_POSTER_SIZE_MEDIUM;
                break;
            case BIG:
                sizePathSegment = PATH_POSTER_SIZE_BIG;
                break;
            case ORIGINAL:
                sizePathSegment = PATH_POSTER_SIZE_ORIGINAL;
                break;
            case THUMB:
            default:
                sizePathSegment = PATH_POSTER_SIZE_THUMB;
                break;
        }

        return buildURLString(BASE_POSTER_URL, null, sizePathSegment, posterPath);
    }

    public static String buildMovieDetailURL(int id) {
        return buildURLString(BASE_API_URL, null, PATH_DETAIL, String.valueOf(id));
    }
}
