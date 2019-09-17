package movieparser.example.android.popularmoviesstage1;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import movieparser.example.android.popularmoviesstage1.R;

import movieparser.example.android.popularmoviesstage1.database.FavoriteEntry;

import java.util.List;


/**
 * This FavoritesAdapter creates and binds ViewHolders, that hold the description and priority of a
 * favorite movies to a RecyclerView to efficiently display data.
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>{

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    // Class variables for the List that holds favorite data and the Context
    private List<FavoriteEntry> mFavoriteEntries;
    private Context mContext;

    /**
     * Constructor for the TaskAdapter that initializes the Context.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    public FavoritesAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }


    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new TaskViewHolder that holds the view for each task
     */
    @Override
    public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.movie_item_layout, parent, false);

        return new FavoritesViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(FavoritesViewHolder holder, int position) {

        String itemImage = mFavoriteEntries.get(position).getImageUrl();
        if (mContext != null) {
            Glide
                    .with(mContext)
                    .load("http://image.tmdb.org/t/p/w185/"+itemImage)
                    .into(holder.mMovieImage);
        }

    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mFavoriteEntries == null) {
            return 0;
        }
        return mFavoriteEntries.size();
    }

    /**
     * When data changes, this method updates the list of favoriteEntries
     * and notifies the adapter to use the new values on it
     */
    public void setFavorites(List<FavoriteEntry> favoriteEntries) {
        mFavoriteEntries = favoriteEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(Bundle movieData);
    }

    // Inner class for creating ViewHolders
    class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables for the task description and priority TextViews
        public final ImageView mMovieImage;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public FavoritesViewHolder(View itemView) {
            super(itemView);

            mMovieImage = (ImageView) itemView.findViewById(R.id.iv_movie);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Bundle mBundle = new Bundle();
            mBundle.putInt("id", mFavoriteEntries.get(adapterPosition).getId());
            mBundle.putString("title", mFavoriteEntries.get(adapterPosition).getTitle());
            mBundle.putString("image",mFavoriteEntries.get(adapterPosition).getImageUrl());
            mBundle.putString("overview", mFavoriteEntries.get(adapterPosition).getOverview());
            mBundle.putString("vote_average", String.valueOf(mFavoriteEntries.get(adapterPosition)
                    .getVote_average()));
            mBundle.putString("release_date", mFavoriteEntries.get(adapterPosition).getRelease_date());
            mItemClickListener.onItemClickListener(mBundle);
        }
    }



}
