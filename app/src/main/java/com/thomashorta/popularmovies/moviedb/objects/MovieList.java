package com.thomashorta.popularmovies.moviedb.objects;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.thomashorta.popularmovies.moviedb.Util;

import java.util.ArrayList;

public class MovieList {
    @SerializedName("page") private Integer page;
    @SerializedName("results") private ArrayList<MovieInfo> movieInfoResults;
    @SerializedName("total_results") private Integer totalResults;
    @SerializedName("total_pages") private Integer totalPages;

    public static MovieList fromJson(String json) {
        Gson gson = Util.createTheMovieDbGson();
        return gson.fromJson(json, MovieList.class);
    }

    public String toJson() {
        Gson gson = Util.createTheMovieDbGson();
        return gson.toJson(this);
    }

    public Integer getPage() {
        return page;
    }

    public ArrayList<MovieInfo> getMovieInfoResults() {
        return movieInfoResults;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public Integer getTotalPages() {
        return totalPages;
    }
}
