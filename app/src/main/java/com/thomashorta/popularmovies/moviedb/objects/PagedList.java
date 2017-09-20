package com.thomashorta.popularmovies.moviedb.objects;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thomashorta.popularmovies.moviedb.Util;

import java.util.ArrayList;
import java.util.List;

public abstract class PagedList<T> {
    @SerializedName("page")
    @Expose
    private Integer page;
    @SerializedName("results")
    @Expose
    private ArrayList<T> results = null;
    @SerializedName("total_pages")
    @Expose
    private Integer totalPages;
    @SerializedName("total_results")
    @Expose
    private Integer totalResults;

    public static <L extends PagedList> L fromJson(String json, Class<L> classOfL) {
        Gson gson = Util.createTheMovieDbGson();
        return gson.fromJson(json, classOfL);
    }

    public String toJson() {
        Gson gson = Util.createTheMovieDbGson();
        return gson.toJson(this);
    }

    public void addResult(T result) {
        if (results == null) results = new ArrayList<>();
        results.add(result);
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public ArrayList<T> getResults() {
        return results;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Integer getTotalResults() {
        return totalResults;
    }
}
