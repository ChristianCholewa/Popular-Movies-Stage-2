package com.example.android.popularmoviesstage2.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoriteEntry {

    @PrimaryKey(autoGenerate = true)
    private int entryId;
    private int id;
    private String title;
    private String poster_path;

    @Ignore
    public FavoriteEntry(int id, String title, String poster_path){
        this.id = id;
        this.title = title;
        this.poster_path = poster_path;
    }

    public FavoriteEntry(int entryId, int id, String title, String poster_path){
        this.entryId = entryId;
        this.id = id;
        this.title = title;
        this.poster_path = poster_path;
    }

    public int getEntryId() {
        return entryId;
    }

    public int getId() {
        return id;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getTitle() {
        return title;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
