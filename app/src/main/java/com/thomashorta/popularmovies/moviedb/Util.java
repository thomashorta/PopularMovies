package com.thomashorta.popularmovies.moviedb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Util {
    public static Gson createTheMovieDbGson() {
        return new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        Gson gson = Util.createTheMovieDbGson();
        return gson.fromJson(json, classOfT);
    }
}
