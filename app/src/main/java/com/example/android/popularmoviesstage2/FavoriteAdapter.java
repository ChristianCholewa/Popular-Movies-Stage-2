package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.database.FavoriteEntry;
import com.example.android.popularmoviesstage2.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<FavoriteEntry> favoriteEntries;
    private Context context;

    public FavoriteAdapter(Context context) {
        this.context = context;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new FavoriteViewHolder that holds the view for each task
     */
    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.favorite_item, parent, false);

        return new FavoriteViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(FavoriteViewHolder holder, int position) {
        // Determine the values of the wanted data
        FavoriteEntry taskEntry = favoriteEntries.get(position);
        String title = taskEntry.getTitle();
        String poster_path = taskEntry.getPoster_path();

        //Set values
        holder.tvTitle.setText(title);
        URL posterUrl = NetworkUtils.buildPosterUrlString(poster_path);
        Picasso.with(context).load(posterUrl.toString()).into(holder.ivImage);
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (favoriteEntries == null) {
            return 0;
        }
        return favoriteEntries.size();
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setFavorites(List<FavoriteEntry> favoriteEntries) {
        this.favoriteEntries = favoriteEntries;
        notifyDataSetChanged();
    }

    public List<FavoriteEntry> getFavorites() {
        return this.favoriteEntries;
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    class FavoriteViewHolder extends RecyclerView.ViewHolder {

        // Class variables for the picture and the title
        TextView tvTitle;
        ImageView ivImage;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public FavoriteViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_favorite_title);
            ivImage = itemView.findViewById(R.id.iv_favorite_poster);
        }
    }
}
