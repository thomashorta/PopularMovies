package com.thomashorta.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.thomashorta.popularmovies.moviedb.TheMovieDbHelper;
import com.thomashorta.popularmovies.moviedb.objects.MovieInfo;
import com.thomashorta.popularmovies.moviedb.objects.MovieList;

import java.util.ArrayList;

public class MovieGridAdapter
        extends RecyclerView.Adapter<MovieGridAdapter.MoviePosterViewHolder> {

    private ArrayList<MovieInfo> mMovieInfoList;
    private OnMovieClickListener mMovieClickListener;

    public interface OnMovieClickListener {
        void onMovieClick(MovieInfo itemMovieInfo, ImageView itemImageView);
    }

    public MovieGridAdapter(OnMovieClickListener listener) {
        mMovieClickListener = listener;
    }

    @Override
    public MoviePosterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.movie_grid_item, parent, false);
        return new MoviePosterViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MoviePosterViewHolder holder, int position) {
        String posterPath = mMovieInfoList.get(position).getPosterPath();
        holder.setMoviePosterPath(posterPath);
    }

    @Override
    public int getItemCount() {
        return mMovieInfoList != null ? mMovieInfoList.size() : 0;
    }

    public void setMovieList(MovieList movieList) {
        mMovieInfoList = movieList != null ? movieList.getMovieInfoResults() : null;
        notifyDataSetChanged();
    }

    public void addMovieList(MovieList movieList) {
        if (movieList != null) {
            mMovieInfoList.addAll(movieList.getMovieInfoResults());
            notifyDataSetChanged();
        }
    }

    public void clear() {
        setMovieList(null);
    }

    public class MoviePosterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        ImageView mImageViewPoster;

        public MoviePosterViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mImageViewPoster = (ImageView) itemView.findViewById(R.id.iv_movie_poster);
        }

        public void setMoviePosterPath(String posterPath) {
            Context context = mImageViewPoster.getContext();
            // Cancel any current requests (as this view is being recycled and used again)
            Picasso.with(context).cancelRequest(mImageViewPoster);
            // Load the current movie poster
            Picasso.with(context)
                    .load(TheMovieDbHelper.buildPosterURL(posterPath,
                            TheMovieDbHelper.PosterSize.THUMB))
                    .placeholder(R.drawable.placeholder_image_27x40)
                    .fit()
                    .centerCrop()
                    .into(mImageViewPoster);
        }

        @Override
        public void onClick(View v) {
            if (mMovieClickListener != null) {
                mMovieClickListener.onMovieClick(mMovieInfoList.get(getAdapterPosition()),
                        mImageViewPoster);
            }
        }
    }

}
