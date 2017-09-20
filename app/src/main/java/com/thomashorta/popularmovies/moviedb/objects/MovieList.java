package com.thomashorta.popularmovies.moviedb.objects;

import com.google.gson.Gson;
import com.thomashorta.popularmovies.moviedb.Util;

public class MovieList extends PagedList<MovieInfo> {
    public static MovieList fromJson(String json) {
        Gson gson = Util.createTheMovieDbGson();
        return gson.fromJson(json, MovieList.class);
    }
}
