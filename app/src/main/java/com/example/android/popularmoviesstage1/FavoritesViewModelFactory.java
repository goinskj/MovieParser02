package com.example.android.popularmoviesstage1;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.android.popularmoviesstage1.database.FavoriteDatabase;

public class FavoritesViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final FavoriteDatabase mDb;
    private final int mFavoriteId;

    public FavoritesViewModelFactory(FavoriteDatabase mDb, int mFavoriteId) {
        this.mDb = mDb;
        this.mFavoriteId = mFavoriteId;
    }

    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new FavoriteViewModel(mDb, mFavoriteId);
    }
}
