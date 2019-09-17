package movieparser.example.android.popularmoviesstage1;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import movieparser.example.android.popularmoviesstage1.R;

import movieparser.example.android.popularmoviesstage1.database.FavoriteDatabase;
import movieparser.example.android.popularmoviesstage1.database.FavoriteEntry;
import movieparser.example.android.popularmoviesstage1.utilities.MovieJsonUtils;
import movieparser.example.android.popularmoviesstage1.utilities.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;

public class MovieDetail extends AppCompatActivity implements
        TrailerAdapter.TrailerAdapterOnClickHandler,
        ReviewAdapter.ReviewAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks {

    private static final String TAG = MovieDetail.class.getSimpleName();

    private int mId;
    private String mTitle;
    private String mPosterPath;
    private String mOverview;
    private String mVotes;
    private String mDate;


    @BindView(R.id.tv_title) TextView mTitleDisplay;
    @BindView(R.id.iv_detailImage) ImageView mPosterDisplay;
    @BindView(R.id.tv_overview) TextView mOverviewDisplay;
    @BindView(R.id.tv_rating) TextView mVotesDisplay;
    @BindView(R.id.tv_date) TextView mDateDisplay;
    @BindView(R.id.tv_error_message_display) TextView mErrorMessageDisplay;
    @BindView(R.id.ll_main) LinearLayout mLinearLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView reviewsRecyclerView;

    @BindView(R.id.btn_favorite) Button mButton;

    private FavoriteDatabase mDb;

    private TrailerAdapter mTrailersAdapter;

    private ReviewAdapter mReviewAdapter;

    /*
     * This number will uniquely identify our Loader and is chosen arbitrarily. You can change this
     * to any number you like, as long as you use the same variable name.
     */
    private static final int VIDEOS_LOADER = 23;
    private static final int REVIEWS_LOADER = 24;

    /* A constant to save and restore the URL that is being displayed */
    private static final String VIDEO_SEARCH_QUERY_EXTRA = "videos";

    /* A constant to save and restore the URL that is being displayed */
    private static final String REVIEW_SEARCH_QUERY_EXTRA = "reviews";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        mDb = FavoriteDatabase.getInstance(getApplicationContext());

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the GridLayout and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);
        reviewsRecyclerView = (RecyclerView) findViewById(R.id.rv_reviews);

        /*
         * LinearLayoutManager can support a list like layout for recyclerview.
         */
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        LinearLayoutManager reviewLayoutManager
                = new LinearLayoutManager(this);
        reviewLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        reviewsRecyclerView.setLayoutManager(reviewLayoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the GridLayout
         */
        mRecyclerView.setHasFixedSize(true);
        reviewsRecyclerView.setHasFixedSize(true);

        /*
         * The MoviesAdapter is responsible for linking our movie data with the Views that
         * will end up displaying our movie data.
         */
        mTrailersAdapter = new TrailerAdapter((TrailerAdapter.TrailerAdapterOnClickHandler) this, MovieDetail.this);
        mReviewAdapter = new ReviewAdapter((ReviewAdapter.ReviewAdapterOnClickHandler) this, MovieDetail.this);

        /* Setting the adapter attaches it to the recyclerview in our layout. */
        mRecyclerView.setAdapter(mTrailersAdapter);
        reviewsRecyclerView.setAdapter(mReviewAdapter);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_COMPONENT_NAME)) {
                Context context = MovieDetail.this;

                Bundle extras = intentThatStartedThisActivity.getBundleExtra(Intent.EXTRA_COMPONENT_NAME);

                if (extras == null) {
                    showErrorMessage();
                } else {
                    showMovieDataView();
                    mId = extras.getInt("id");
                    populateVideos(Integer.toString(mId));
                    populateReviews(Integer.toString(mId));
                    mTitle = extras.getString("title");
                    mPosterPath = extras.getString("image");
                    String fullImagePathUrl = "http://image.tmdb.org/t/p/w185/"+mPosterPath;
                    mOverview = extras.getString("overview");
                    mVotes = extras.getString("vote_average");
                    mDate = extras.getString("release_date");
                    mTitleDisplay.setText(mTitle);

                    FavoritesViewModelFactory factory = new FavoritesViewModelFactory(mDb, mId);
                    final FavoriteViewModel viewModel =
                            ViewModelProviders.of(this, factory).get(FavoriteViewModel.class);
                    viewModel.getFavorite().observe(this, new Observer<FavoriteEntry>() {
                        @Override
                        public void onChanged(@Nullable FavoriteEntry favoriteEntry) {
                            viewModel.getFavorite().removeObserver(this);
                            populateFavoriteButtonUI(favoriteEntry);
                        }
                    });

                    Glide
                            .with(context)
                            .load(fullImagePathUrl)
                            .into(mPosterDisplay);

                    mOverviewDisplay.setText(mOverview);
                    mVotesDisplay.setText(mVotes + "/10");
                    mDateDisplay.setText(mDate);

                }
            }
        }
    }

    private void populateVideos(String id) {
        showMovieTrailersView();

        Bundle movieVideoBundle = new Bundle();
        movieVideoBundle.putString(VIDEO_SEARCH_QUERY_EXTRA, id);

        LoaderManager loaderManagerVideos = getSupportLoaderManager();

        Loader<String> movieVideoLoader = loaderManagerVideos.getLoader(VIDEOS_LOADER);

        if (movieVideoLoader == null) {
            loaderManagerVideos.initLoader(VIDEOS_LOADER, movieVideoBundle, this);
        } else {
            loaderManagerVideos.restartLoader(VIDEOS_LOADER, movieVideoBundle, this);
        }
    }

    private void populateReviews(String id) {
        showMovieReviewsView();

        Bundle movieReviewBundle = new Bundle();
        movieReviewBundle.putString(REVIEW_SEARCH_QUERY_EXTRA, id);

        LoaderManager loaderManagerReviews = getSupportLoaderManager();

        Loader<String> movieReviewLoader = loaderManagerReviews.getLoader(REVIEWS_LOADER);

        if (movieReviewLoader == null) {
            loaderManagerReviews.initLoader(REVIEWS_LOADER, movieReviewBundle, this);
        } else {
            loaderManagerReviews.restartLoader(REVIEWS_LOADER, movieReviewBundle, this);
        }
    }

    private void populateFavoriteButtonUI(FavoriteEntry favoriteEntry) {
        // COMPLETED (7) return if the entry is null
        if (favoriteEntry == null) {
            mButton.setText(R.string.favorite_btn_text_add);
            return;
        }

        // COMPLETED (8) use the variable entry to populate the UI
        mButton.setText(R.string.favorite_btn_text_minus);
    }

    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new movie data into the underlying database.
     */
    public void onSaveButtonClicked() {

        final int id = mId;
        String title = mTitle;
        String imageUrl = mPosterPath;
        String overview = mOverview;
        int vote_average = Integer.parseInt(mVotes);
        String release_date = mDate;

        final FavoriteEntry favoriteEntry = new FavoriteEntry(id, title,imageUrl, overview, vote_average,
                release_date);

        String btn_text = (String) mButton.getText();
        String btn_fav = getResources().getString(R.string.favorite_btn_text_add);
        String btn_unfav = getResources().getString(R.string.favorite_btn_text_minus);

        if (btn_text == btn_fav) {
            insertFavorite(favoriteEntry);
        } else if (btn_text == btn_unfav) {
            deleteFavorite(favoriteEntry);
        }

    }

    private void insertFavorite(final FavoriteEntry favoriteEntry) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.favoriteDao().insertFavorite(favoriteEntry);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mButton.setText(R.string.favorite_btn_text_minus);
                    }
                });

            }
        });
    }

    private void deleteFavorite(final FavoriteEntry favoriteEntry) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.favoriteDao().deleteFavorite(favoriteEntry);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mButton.setText(R.string.favorite_btn_text_add);
                    }
                });

            }
        });
    }



    /**
     * This method will make the View for the movie data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movie data is visible */
        mLinearLayout.setVisibility(View.VISIBLE);
        mButton = (Button) findViewById(R.id.btn_favorite);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });
    }

    private void showMovieTrailersView() {
        //Will make recyclerview for videos visible.
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showMovieReviewsView() {
        //Will make recyclerview for reviews visible.
        reviewsRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movie data
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mLinearLayout.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable final Bundle bundle) {

        switch (id) {
            case (VIDEOS_LOADER):
                return new AsyncTaskLoader<ArrayList<VideoItem>>(this) {

                    // COMPLETED (5) Override onStartLoading
                    @Override
                    protected void onStartLoading() {

                        // COMPLETED (6) If args is null, return.
                        /* If no arguments were passed, we don't have a query to perform. Simply return. */
                        if (bundle == null) {
                            return;
                        }

                        // COMPLETED (7) Show the loading indicator
                        /*
                         * When we initially begin loading in the background, we want to display the
                         * loading indicator to the user
                         */
                        // COMPLETED (8) Force a load
                        forceLoad();
                    }

                    // COMPLETED (9) Override loadInBackground
                    @Override
                    public ArrayList<VideoItem> loadInBackground() {

                        String videoSearch = bundle.getString(VIDEO_SEARCH_QUERY_EXTRA);
                        String apiKey = getResources().getString(R.string.movies_query_api);
                        URL moviesVideosRequestUrl = NetworkUtils.buildMovieVideoUrl(videoSearch, apiKey);

                        // COMPLETED (11) If the URL is null or empty, return null
                        /* If the user didn't enter anything, there's nothing to search for */
                        if (moviesVideosRequestUrl == null || TextUtils.isEmpty(moviesVideosRequestUrl.
                                toString())) {
                            return null;
                        }

                        try {
                            String jsonMovieVideosResponse = NetworkUtils
                                    .getResponseFromHttpUrl(moviesVideosRequestUrl);

                            ArrayList<VideoItem> jsonMovieVideoData = MovieJsonUtils
                                    .getVideoStringsFromJson(MovieDetail.this, jsonMovieVideosResponse);

                            return jsonMovieVideoData;

                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                };

            case (REVIEWS_LOADER):
                return new AsyncTaskLoader<ArrayList<ReviewItem>>(this) {

                    // COMPLETED (5) Override onStartLoading
                    @Override
                    protected void onStartLoading() {

                        // COMPLETED (6) If args is null, return.
                        /* If no arguments were passed, we don't have a query to perform. Simply return. */
                        if (bundle == null) {
                            return;
                        }

                        // COMPLETED (7) Show the loading indicator
                        /*
                         * When we initially begin loading in the background, we want to display the
                         * loading indicator to the user
                         */
                        // COMPLETED (8) Force a load
                        forceLoad();
                    }

                    // COMPLETED (9) Override loadInBackground
                    @Override
                    public ArrayList<ReviewItem> loadInBackground() {

                        String reviewSearch = bundle.getString(REVIEW_SEARCH_QUERY_EXTRA);
                        String apiKey = getResources().getString(R.string.movies_query_api);
                        URL movieReviewsRequestUrl = NetworkUtils.buildMovieReviewUrl(reviewSearch, apiKey);

                        // COMPLETED (11) If the URL is null or empty, return null
                        /* If the user didn't enter anything, there's nothing to search for */
                        if (movieReviewsRequestUrl == null || TextUtils.isEmpty(movieReviewsRequestUrl.
                                toString())) {
                            return null;
                        }

                        try {
                            String jsonMovieReviewResponse = NetworkUtils
                                    .getResponseFromHttpUrl(movieReviewsRequestUrl);

                            ArrayList<ReviewItem> jsonMovieReviewData = MovieJsonUtils
                                    .getReviewStringsFromJson(MovieDetail.this, jsonMovieReviewResponse);

                            return jsonMovieReviewData;

                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                };
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object o) {
        int id = loader.getId();// find which loader you called
        if (id == VIDEOS_LOADER ) {
            if (o != null) {
                showMovieTrailersView();
                mTrailersAdapter.setTrailerData((ArrayList<VideoItem>) o);
            } else {
                return;
            }

        } else if (id == REVIEWS_LOADER ) {
            if (o != null) {
                showMovieReviewsView();
                mReviewAdapter.setReviewData((ArrayList<ReviewItem>) o);

            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }

    @Override
    public void onClick(Bundle movieData) {

        if (movieData.containsKey("author")) {
            return;
        }

        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v="+movieData.get("key").toString())));


    }
}
