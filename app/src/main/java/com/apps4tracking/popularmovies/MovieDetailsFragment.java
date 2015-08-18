package com.apps4tracking.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsFragment extends Fragment {

    private final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        Intent intent = getActivity().getIntent();
        MovieInfo movie = intent.getParcelableExtra("movie");

        ((TextView) rootView.findViewById(R.id.original_title)).setText(movie.original_title);
        ((TextView) rootView.findViewById(R.id.release_date)).setText(movie.release_date);
        ((TextView) rootView.findViewById(R.id.vote_average)).setText(movie.vote_average + "/10");
        ((TextView) rootView.findViewById(R.id.overview)).setText(movie.overview);

        Picasso.with(getActivity())
                .load(MovieInfoAdapter.getUrl(movie))
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .fit()
                .into(((ImageView) rootView.findViewById(R.id.poster_path)));
        return rootView;
    }
}
