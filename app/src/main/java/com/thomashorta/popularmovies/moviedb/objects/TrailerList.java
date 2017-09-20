package com.thomashorta.popularmovies.moviedb.objects;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thomashorta.popularmovies.moviedb.Util;

public class TrailerList {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("results")
    @Expose
    private ArrayList<Trailer> results = null;

    public static TrailerList fromJson(String json) {
        Gson gson = Util.createTheMovieDbGson();
        return gson.fromJson(json, TrailerList.class);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ArrayList<Trailer> getResults() {
        return results;
    }

    public void setResults(ArrayList<Trailer> results) {
        this.results = results;
    }

}