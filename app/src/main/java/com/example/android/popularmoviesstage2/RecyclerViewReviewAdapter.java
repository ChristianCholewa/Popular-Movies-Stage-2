package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.data.MovieData;
import com.example.android.popularmoviesstage2.data.MovieReview;
import com.example.android.popularmoviesstage2.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

public class RecyclerViewReviewAdapter extends RecyclerView.Adapter<RecyclerViewReviewAdapter.ViewHolder>{

    private List<MovieReview> mMovieReviewData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;

    // data is passed into the constructor
    RecyclerViewReviewAdapter(Context context, List<MovieReview> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mMovieReviewData = data;
        mContext = context;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_reviewitem, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvContent.setText(mMovieReviewData.get(position).getContent());
        holder.tvAuthor.setText(mMovieReviewData.get(position).getAuthor());
        //TODO
//        URL posterUrl = NetworkUtils.buildPosterUrlString( mData.get(position).getPoster_path());
//        Picasso.with(mContext).load(posterUrl.toString()).into(holder.imageView);
    }

    // total number of cells
    @Override
    public int getItemCount() {
        if(mMovieReviewData == null){
            return 0;
        }else {
            return mMovieReviewData.size();
        }
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvContent;
        TextView tvAuthor;

        ViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            //TODO
//
//            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //TODO
//            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    MovieReview getItem(int id) {
        return mMovieReviewData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
