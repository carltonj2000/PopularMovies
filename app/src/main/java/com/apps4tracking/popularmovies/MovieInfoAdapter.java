package com.apps4tracking.popularmovies;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MovieInfoAdapter extends ArrayAdapter<MovieInfo> {

    private final String LOG_TAG = MovieInfoAdapter.class.getSimpleName();
    private static final String BASE_URL = "image.tmdb.org";
    private static final String POSTER_SIZE = "w185";

    public MovieInfoAdapter(Activity context, ArrayList<MovieInfo> movies) {
        super(context, 0, movies);
    }

    private static class ViewHolder {
        ImageView imageView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieInfo movie = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
            viewHolder.imageView = (ImageView) convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(getContext())
                .load(getUrl(movie))
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .fit()
                .into(viewHolder.imageView);
        return convertView;
    }

    public static String getUrl(MovieInfo movie) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("http")
                .authority(BASE_URL)
                .appendPath("t")
                .appendPath("p")
                .appendPath(POSTER_SIZE)
                .appendPath(movie.poster_path.substring(1));
        String url = uriBuilder.build().toString();
        return url;
    }
}
