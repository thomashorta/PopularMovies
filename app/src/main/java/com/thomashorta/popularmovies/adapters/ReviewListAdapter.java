package com.thomashorta.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thomashorta.popularmovies.R;
import com.thomashorta.popularmovies.moviedb.objects.MovieInfo;
import com.thomashorta.popularmovies.moviedb.objects.MovieList;
import com.thomashorta.popularmovies.moviedb.objects.Review;
import com.thomashorta.popularmovies.moviedb.objects.ReviewList;

import java.util.ArrayList;

public class ReviewListAdapter
        extends RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder> {

    private ArrayList<Review> mReviewList;

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.review_list_item, parent, false);
        return new ReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        String author = mReviewList.get(position).getAuthor();
        String content = mReviewList.get(position).getContent();
        holder.setReview(author, content);
    }

    @Override
    public int getItemCount() {
        return mReviewList != null ? mReviewList.size() : 0;
    }

    public void setReviewList(ReviewList reviewList) {
        mReviewList = reviewList != null ? reviewList.getResults() : null;
        notifyDataSetChanged();
    }

    public void addReviewList(ReviewList reviewList) {
        if (reviewList != null) {
            mReviewList.addAll(reviewList.getResults());
            notifyDataSetChanged();
        }
    }

    public ArrayList<Review> getReviews() {
        return mReviewList;
    }

    public void setReviews(ArrayList<Review> reviews) {
        mReviewList = reviews;
        notifyDataSetChanged();
    }

    public void clear() {
        setReviewList(null);
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView mTextViewAuthor;
        TextView mTextViewContent;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            mTextViewAuthor = (TextView) itemView.findViewById(R.id.tv_review_author);
            mTextViewContent = (TextView) itemView.findViewById(R.id.tv_review_content);
        }

        public void setReview(String author, String content) {
            mTextViewAuthor.setText(author);
            mTextViewContent.setText(content);
        }
    }

}
