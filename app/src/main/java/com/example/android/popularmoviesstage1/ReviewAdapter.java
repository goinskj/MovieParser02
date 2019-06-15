package com.example.android.popularmoviesstage1;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{

    private static final String TAG = ReviewAdapter.class.getSimpleName();

    ArrayList<ReviewItem> mReviewData;
    Context mContext;

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final ReviewAdapter.ReviewAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface ReviewAdapterOnClickHandler {
        void onClick(Bundle reviewData);
    }

    /**
     * Creates a MoviesAdapter.
     *  @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     * @param context
     */
    public ReviewAdapter(ReviewAdapter.ReviewAdapterOnClickHandler clickHandler, Context context) {
        mClickHandler = clickHandler;
        mContext = context;
    }

    /**
     * Cache of the children views for a movies list item.
     */
    public class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView author;
        public final TextView content;

        public ReviewViewHolder(View view) {
            super(view);
            author = (TextView) view.findViewById(R.id.tv_review_writer);
            content = (TextView) view.findViewById(R.id.tv_review);
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Bundle mBundle = new Bundle();
            mBundle.putString("author", mReviewData.get(adapterPosition).author);
            mBundle.putString("content", mReviewData.get(adapterPosition).content);
            mClickHandler.onClick(mBundle);
        }
    }

    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.review_item_layout;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ReviewAdapter.ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ReviewViewHolder reviewViewHolder, int i) {

        String author = mReviewData.get(i).author;
        String content = mReviewData.get(i).content;

        if (mContext != null) {
            reviewViewHolder.author.setText(author);
            reviewViewHolder.content.setText(content);
        }
    }

    @Override
    public int getItemCount() {
        if (null == mReviewData) return 0;
        return mReviewData.size();
    }

    /**
     * This method is used to set the movie data on a MoviesAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MoviesAdapter to display it.
     *
     * @param reviewData The new movie data to be displayed.
     */
    public void setReviewData(ArrayList<ReviewItem> reviewData) {
        mReviewData = reviewData;
        notifyDataSetChanged();
    }
}
