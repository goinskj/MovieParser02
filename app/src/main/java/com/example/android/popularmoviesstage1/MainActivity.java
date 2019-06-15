/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.popularmoviesstage1;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmoviesstage1.database.FavoriteDatabase;
import com.example.android.popularmoviesstage1.database.FavoriteEntry;
import com.example.android.popularmoviesstage1.utilities.MovieJsonUtils;
import com.example.android.popularmoviesstage1.utilities.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements MoviesAdapter.MoviesAdapterOnClickHandler,
        FavoritesAdapter.ItemClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<MovieItem>> {

    private static final String TAG = MainActivity.class.getSimpleName();

    /*
     * This number will uniquely identify our Loader and is chosen arbitrarily. You can change this
     * to any number you like, as long as you use the same variable name.
     */
    private static final int MOVIES_LOADER = 22;

    /* A constant to save and restore the URL that is being displayed */
    private static final String START_SEARCH_QUERY_EXTRA = "popular";

    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private FavoritesAdapter mFavoritesAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private FavoriteDatabase mDb;

    private GridLayoutManager mGridLayoutManager;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recycler_layout";
    private static final String MOVIES_PARCELABLE = "movies_parcelable";
    private static final String FAVORITES_PARCELABLE = "favorites_parcelable";
    private ArrayList<MovieItem> cachedMovies;
    private List<FavoriteEntry> cachedFavorites;
    Parcelable savedRecyclerLayoutState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the GridLayout and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        /*
         * GridLayoutManager can support a grid like layout for recyclerview.
         */
        mGridLayoutManager = new GridLayoutManager(this, 2);
        mGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(mGridLayoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the GridLayout
         */
        mRecyclerView.setHasFixedSize(true);

        /*
         * The MoviesAdapter is responsible for linking our movie data with the Views that
         * will end up displaying our movie data.
         */
        mMoviesAdapter = new MoviesAdapter(this, MainActivity.this);

        mFavoritesAdapter = new FavoritesAdapter(MainActivity.this,this);

        mDb = FavoriteDatabase.getInstance(getApplicationContext());

        /* Setting the adapter attaches it to the recyclerview in our layout. */
        mRecyclerView.setAdapter(mMoviesAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        if(savedInstanceState != null) {
            if (savedInstanceState.containsKey(FAVORITES_PARCELABLE)) {
                mRecyclerView.setAdapter(mFavoritesAdapter);
                setupViewModel();
            } else {
                savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
                cachedMovies = savedInstanceState.getParcelableArrayList(MOVIES_PARCELABLE);
                mMoviesAdapter.setMovieData(cachedMovies);
                mMoviesAdapter.notifyDataSetChanged();
            }

        } else {
            /* Once all of our views are setup, we can load the movie data. */
            loadMovieData(getResources().getString(R.string.popular_movies));
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mRecyclerView.getAdapter() instanceof FavoritesAdapter) {
            Log.v(TAG, "In favorites mode while flipping screen");
            outState.putParcelableArrayList(FAVORITES_PARCELABLE,
                    (ArrayList<FavoriteEntry>) cachedFavorites);
            outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager()
                    .onSaveInstanceState());
        } else {
            outState.putParcelableArrayList(MOVIES_PARCELABLE, cachedMovies);
            outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager()
                    .onSaveInstanceState());
        }

        super.onSaveInstanceState(outState);


    }


    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getFavorites().observe(this, new Observer<List<FavoriteEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavoriteEntry> favoriteEntries) {
                Log.d(TAG, "Update list of favorite movies from LiveData in ViewModel");
                mFavoritesAdapter.setFavorites(favoriteEntries);
                mFavoritesAdapter.notifyDataSetChanged();

            }
        });
    }

    /**
     * This method will tell some background method to get the movie data in the background with
     * the sort key as a parameter.
     */
    private void loadMovieData(String sortView) {

        showMovieDataView();

        setupViewModel();

        Bundle movieBundle = new Bundle();
        movieBundle.putString(START_SEARCH_QUERY_EXTRA, sortView);

        LoaderManager loaderManager = getSupportLoaderManager();

        Loader<String> movieLoader = loaderManager.getLoader(MOVIES_LOADER);

        if (movieLoader == null) {
            loaderManager.initLoader(MOVIES_LOADER, movieBundle, this);
        } else {
            loaderManager.restartLoader(MOVIES_LOADER,movieBundle, this);
        }
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param itemData The product data for the day that was clicked
     */
    @Override
    public void onClick(Bundle itemData) {
        Context context = this;
        Class destinationClass = MovieDetail.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_COMPONENT_NAME, itemData);
        startActivity(intentToStartDetailActivity);
    }

    @Override
    public void onItemClickListener(Bundle movieData) {
        Context context = this;
        Class destinationClass = MovieDetail.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_COMPONENT_NAME, movieData);
        startActivity(intentToStartDetailActivity);
    }

    /**
     * This method will make the View for the product data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movie data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);

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
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public Loader<ArrayList<MovieItem>> onCreateLoader(int i, @Nullable final Bundle bundle) {
        return new AsyncTaskLoader<ArrayList<MovieItem>>(this) {

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
                mLoadingIndicator.setVisibility(View.VISIBLE);
                // COMPLETED (8) Force a load
                forceLoad();
            }

            // COMPLETED (9) Override loadInBackground
            @Override
            public ArrayList<MovieItem> loadInBackground() {

                String sortPref = bundle.getString(START_SEARCH_QUERY_EXTRA);
                String apiKey = getResources().getString(R.string.movies_query_api);
                URL moviesRequestUrl = NetworkUtils.buildQueryUrl(sortPref, apiKey);

                // COMPLETED (11) If the URL is null or empty, return null
                /* If the user didn't enter anything, there's nothing to search for */
                if (moviesRequestUrl == null || TextUtils.isEmpty(moviesRequestUrl.toString())) {
                    return null;
                }

                try {
                    String jsonMovieResponse = NetworkUtils
                            .getResponseFromHttpUrl(moviesRequestUrl);

                    ArrayList<MovieItem> jsonMovieData = MovieJsonUtils
                            .getMovieStringsFromJson(MainActivity.this, jsonMovieResponse);

                    return jsonMovieData;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<MovieItem>> loader, ArrayList<MovieItem> s) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (s != null) {
            if (s.size() > 0) {
                showMovieDataView();
                mMoviesAdapter.setMovieData(s);
                cachedMovies = s;
                mGridLayoutManager.onRestoreInstanceState(savedRecyclerLayoutState);
            } else {
                showErrorMessage();
            }
        } else {
            showErrorMessage();
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<MovieItem>> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.sort, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sort_popular) {
            mRecyclerView.setAdapter(mMoviesAdapter);
            loadMovieData(getResources().getString(R.string.popular_movies));
            return true;
        }

        if (id == R.id.sort_rated) {
            mRecyclerView.setAdapter(mMoviesAdapter);
            loadMovieData(getResources().getString(R.string.top_rated_movies));
            return true;
        }

        if (id == R.id.favorites) {
            mRecyclerView.setAdapter(mFavoritesAdapter);
            setupViewModel();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
