package com.thomashorta.popularmovies.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thomashorta.popularmovies.R;
import com.thomashorta.popularmovies.moviedb.objects.Trailer;
import com.thomashorta.popularmovies.moviedb.objects.TrailerList;

import java.util.ArrayList;

public class TrailerListAdapter
        extends RecyclerView.Adapter<TrailerListAdapter.TrailerViewHolder> {
    private static final String TAG = TrailerListAdapter.class.getSimpleName();

    private ArrayList<Trailer> mTrailerList;

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        String name = mTrailerList.get(position).getName();
        holder.setName(name);
    }

    @Override
    public int getItemCount() {
        return mTrailerList != null ? mTrailerList.size() : 0;
    }

    public void setTrailerList(TrailerList trailerList) {
        mTrailerList = trailerList != null ? trailerList.getResults() : null;
        notifyDataSetChanged();
    }

    public void addTrailerList(TrailerList trailerList) {
        if (trailerList != null) {
            mTrailerList.addAll(trailerList.getResults());
            notifyDataSetChanged();
        }
    }

    public ArrayList<Trailer> getTrailers() {
        return mTrailerList;
    }

    public void setTrailers(ArrayList<Trailer> trailers) {
        mTrailerList = trailers;
        notifyDataSetChanged();
    }

    public void clear() {
        setTrailerList(null);
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView mTextViewName;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTextViewName = (TextView) itemView.findViewById(R.id.tv_trailer_name);
        }

        public void setName(String name) {
            mTextViewName.setText(name);
        }

        @Override
        public void onClick(View v) {
            // open youtube or fallback to browser
            Context context = v.getContext();
            Trailer trailer = mTrailerList.get(getAdapterPosition());

            if (trailer.getSite().toLowerCase().contains("youtube")) {
                String key = trailer.getKey();
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + key));
                try {
                    context.startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    context.startActivity(webIntent);
                }
            } else {
                Log.w(TAG, "Unknown video site: " + trailer.getSite() + " / key: " + trailer.getKey());
            }
        }
    }

}
