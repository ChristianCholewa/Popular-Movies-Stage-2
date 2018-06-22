package com.example.android.popularmoviesstage2.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Query("SELECT * FROM favorites")
    List<FavoriteEntry> loadAllFavorites();

    @Query("SELECT * FROM favorites where entryId = :entryId")
    FavoriteEntry getFavoriteByEntryId(long entryId);

    @Query("SELECT * FROM favorites where id = :movieId")
    FavoriteEntry getFavoriteByMovieId(int movieId);

    @Insert
    void insertFavorite(FavoriteEntry favoriteEntry);

//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    void updateFavorite(FavoriteEntry favoriteEntry);

    @Delete
    void deleteFavorite(FavoriteEntry favoriteEntry);
}
