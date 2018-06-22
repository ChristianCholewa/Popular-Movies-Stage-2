package com.example.android.popularmoviesstage2;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.popularmoviesstage2.database.FavoriteDatabase;
import com.example.android.popularmoviesstage2.database.FavoriteEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String LOG_TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<FavoriteEntry>> favoriteEntries;

    public MainViewModel(@NonNull Application application) {
        super(application);

        Log.d(LOG_TAG, "Call loadAllFavorites");
        FavoriteDatabase database = FavoriteDatabase.getInstance(this.getApplication());
        favoriteEntries = database.favoriteDao().loadAllFavorites();
    }

    LiveData<List<FavoriteEntry>> getTasks(){
        return favoriteEntries;
    }
}
