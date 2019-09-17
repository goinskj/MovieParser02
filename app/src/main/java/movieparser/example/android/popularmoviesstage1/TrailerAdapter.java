package movieparser.example.android.popularmoviesstage1;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import movieparser.example.android.popularmoviesstage1.R;

import java.util.ArrayList;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder>{

    private static final String TAG = TrailerAdapter.class.getSimpleName();

    ArrayList<VideoItem> mVideoData;
    Context mContext;

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final TrailerAdapter.TrailerAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface TrailerAdapterOnClickHandler {
        void onClick(Bundle movieData);
    }

    /**
     * Creates a MoviesAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public TrailerAdapter(TrailerAdapter.TrailerAdapterOnClickHandler clickHandler, Context context) {
        mClickHandler = clickHandler;
        mContext = context;
    }


    /**
     * Cache of the children views for a movies list item.
     */
    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final android.support.v7.widget.AppCompatImageView mTrailerPlayButton;
        public final TextView trailerOrder;

        public TrailerViewHolder(View view) {
            super(view);
            mTrailerPlayButton = view.findViewById(R.id.play_btn);
            trailerOrder = (TextView) view.findViewById(R.id.tv_trailer_number);
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
            mBundle.putInt("order", mVideoData.get(adapterPosition).order);
            mBundle.putString("id", mVideoData.get(adapterPosition).id);
            mBundle.putString("key",mVideoData.get(adapterPosition).key);
            mClickHandler.onClick(mBundle);
        }
    }

    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.trailer_item_layout;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new TrailerAdapter.TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder trailerViewHolder, int i) {
        String trailerOrder = "Trailer " + (i+1);
        if (mContext != null) {
            trailerViewHolder.trailerOrder.setText(trailerOrder);
        }
    }

    @Override
    public int getItemCount() {
        if (null == mVideoData) return 0;
        return mVideoData.size();
    }

    /**
     * This method is used to set the movie data on a MoviesAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MoviesAdapter to display it.
     *
     * @param trailerData The new movie data to be displayed.
     */
    public void setTrailerData(ArrayList<VideoItem> trailerData) {
        mVideoData = trailerData;
        notifyDataSetChanged();
    }
}
