package com.example.android.popularmoviesstage1;

import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.popularmoviesstage1.database.FavoriteDatabase;
import com.example.android.popularmoviesstage1.database.FavoriteEntry;

public class FavoriteViewModel extends ViewModel {

    private LiveData<FavoriteEntry> favorite;

    public FavoriteViewModel(FavoriteDatabase database, int favoriteId) {
        favorite = database.favoriteDao().loadFavoriteById(favoriteId);

    }

    public LiveData<FavoriteEntry> getFavorite() {
        return favorite;
    }
}
