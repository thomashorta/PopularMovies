package com.thomashorta.popularmovies.moviedb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class MovieInfo {
    @SerializedName("id") private Integer id;
    @SerializedName("poster_path") private String posterPath;
    @SerializedName("title") private String title;
    @SerializedName("overview") private String overview;
    @SerializedName("original_title") private String originalTitle;
    @SerializedName("original_language") private String originalLanguage;
    @SerializedName("release_date") private String releaseDate;
    @SerializedName("popularity") private Float popularity;
    @SerializedName("vote_count") private Integer voteCount;
    @SerializedName("vote_average") private Float voteAverage;
    @SerializedName("adult") private Boolean adult;
    @SerializedName("genre_ids") private Integer[] genreIds;
    @SerializedName("backdrop_path") private String backdropPath;
    @SerializedName("video") private Boolean video;

    public static MovieInfo fromJson(String jsonMovieInfo) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(jsonMovieInfo, MovieInfo.class);
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public Integer getId() {
        return id;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public Float getPopularity() {
        return popularity;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public Float getVoteAverage() {
        return voteAverage;
    }

    public Boolean getAdult() {
        return adult;
    }

    public Integer[] getGenreIds() {
        return genreIds;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public Boolean getVideo() {
        return video;
    }
}
