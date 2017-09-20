
package com.thomashorta.popularmovies.moviedb.objects;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thomashorta.popularmovies.moviedb.Util;

public class ReviewList extends PagedList<Review> {
    @SerializedName("id")
    @Expose
    private Integer id;

    public Integer getId() {
        return id;
    }
}