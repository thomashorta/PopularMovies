package com.thomashorta.popularmovies.moviedb.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.thomashorta.popularmovies.moviedb.Util;

import java.util.ArrayList;
import java.util.Date;

public class MovieInfo implements Parcelable {
    @SerializedName("id") private Integer id;
    @SerializedName("poster_path") private String posterPath;
    @SerializedName("title") private String title;
    @SerializedName("overview") private String overview;
    @SerializedName("original_title") private String originalTitle;
    @SerializedName("original_language") private String originalLanguage;
    @SerializedName("release_date") private Date releaseDate;
    @SerializedName("popularity") private Double popularity;
    @SerializedName("vote_count") private Integer voteCount;
    @SerializedName("vote_average") private Double voteAverage;
    @SerializedName("adult") private Boolean adult;
    @SerializedName("genre_ids") private ArrayList<Integer> genreIds;
    @SerializedName("backdrop_path") private String backdropPath;
    @SerializedName("video") private Boolean video;

    public static final Creator<MovieInfo> CREATOR = new Creator<MovieInfo>() {
        @Override
        @SuppressWarnings("unchecked")
        public MovieInfo createFromParcel(Parcel in) {
            MovieInfo instance = new MovieInfo();
            instance.voteCount = ((Integer) in.readValue((Integer.class.getClassLoader())));
            instance.id = ((Integer) in.readValue((Integer.class.getClassLoader())));
            instance.video = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
            instance.voteAverage = ((Double) in.readValue((Double.class.getClassLoader())));
            instance.title = ((String) in.readValue((String.class.getClassLoader())));
            instance.popularity = ((Double) in.readValue((Double.class.getClassLoader())));
            instance.posterPath = ((String) in.readValue((String.class.getClassLoader())));
            instance.originalLanguage = ((String) in.readValue((String.class.getClassLoader())));
            instance.originalTitle = ((String) in.readValue((String.class.getClassLoader())));
            in.readList(instance.genreIds, (java.lang.Integer.class.getClassLoader()));
            instance.backdropPath = ((String) in.readValue((String.class.getClassLoader())));
            instance.adult = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
            instance.overview = ((String) in.readValue((String.class.getClassLoader())));
            instance.releaseDate = ((Date) in.readValue((Date.class.getClassLoader())));
            return instance;
        }

        @Override
        public MovieInfo[] newArray(int size) {
            return new MovieInfo[size];
        }
    };

    public MovieInfo() {

    }

    // used to create a movie info object from favorites provider
    public MovieInfo(int id, String title, String posterPath) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
    }

    public static MovieInfo fromJson(String jsonMovieInfo) {
        Gson gson = Util.createTheMovieDbGson();
        return gson.fromJson(jsonMovieInfo, MovieInfo.class);
    }

    public String toJson() {
        Gson gson = Util.createTheMovieDbGson();
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

    public Date getReleaseDate() {
        return releaseDate;
    }

    public Double getPopularity() {
        return popularity;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public Boolean getAdult() {
        return adult;
    }

    public ArrayList<Integer> getGenreIds() {
        return genreIds;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public Boolean getVideo() {
        return video;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(voteCount);
        dest.writeValue(id);
        dest.writeValue(video);
        dest.writeValue(voteAverage);
        dest.writeValue(title);
        dest.writeValue(popularity);
        dest.writeValue(posterPath);
        dest.writeValue(originalLanguage);
        dest.writeValue(originalTitle);
        dest.writeList(genreIds);
        dest.writeValue(backdropPath);
        dest.writeValue(adult);
        dest.writeValue(overview);
        dest.writeValue(releaseDate);
    }
}
