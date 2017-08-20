package com.thomashorta.popularmovies.moviedb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class MovieList {
    @SerializedName("page") private Integer page;
    @SerializedName("results") private MovieInfo[] movieInfoResults;
    @SerializedName("total_results") private Integer totalResults;
    @SerializedName("total_pages") private Integer totalPages;

    public static MovieList fromJson(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(json, MovieList.class);
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public Integer getPage() {
        return page;
    }

    public MovieInfo[] getMovieInfoResults() {
        return movieInfoResults;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public Integer getTotalPages() {
        return totalPages;
    }
}
