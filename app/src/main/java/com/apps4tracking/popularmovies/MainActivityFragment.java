package com.apps4tracking.popularmovies;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivityFragment extends Fragment {

    MovieInfoAdapter movieInfoAdapter;
    private final ArrayList<MovieInfo> moviesInfo = new ArrayList<>();
    private final String MOVIES_KEY = "movies_key";
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final int RESULT_SETTINGS = 1;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIES_KEY, moviesInfo);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIES_KEY))
            getMovies();
        else {
            ArrayList<MovieInfo> movies = savedInstanceState.getParcelableArrayList(MOVIES_KEY);
            moviesInfo.clear();
            moviesInfo.addAll(movies);
        }
    }

    public void  getMovies() {
        String api_key = getApiKey();
        if (api_key.length() != 0)
            new FetchMoviesTask().execute(new MoviesTaskParams(api_key, getSortOrder()));
    }
    private String getApiKey() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String apiKey = preferences.getString(getString(R.string.pref_api_key_key), getString(R.string.pref_api_key_default));
        return apiKey;
    }

    private String getSortOrder() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = preferences.getString(getString(R.string.pref_movie_sort_order_list_key), getString(R.string.pref_sort_movies_by_default_pop));
        return sortOrder;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
        TextView textView = (TextView) rootView.findViewById(R.id.textView);
        if (getApiKey().length() == 0) gridView.setVisibility(View.GONE);
        else textView.setVisibility(View.GONE);
        movieInfoAdapter = new MovieInfoAdapter(getActivity(), moviesInfo);
        gridView.setAdapter(movieInfoAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieInfo movie = (MovieInfo) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), MovieDetails.class);
                intent.putExtra("movie", movie);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private static class MoviesTaskParams {
        String apiKey; String sortOrder;
        MoviesTaskParams(String apiKey, String sortOrder) {
            this.apiKey = apiKey; this.sortOrder = sortOrder;
        }
    }

    private class FetchMoviesTask extends AsyncTask<MoviesTaskParams, Void, ArrayList<MovieInfo>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        protected ArrayList<MovieInfo> doInBackground(MoviesTaskParams... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String topMoviesJsonStr = null;

            String sortOrder;
            if (params[0].sortOrder.equals(getString(R.string.pref_sort_movies_by_default_pop)))
                sortOrder = "popularity";
            else
                sortOrder = "vote_average";
            try {
                Uri.Builder uriBuilder = new Uri.Builder();
                uriBuilder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", sortOrder + ".desc")
                        .appendQueryParameter("api_key",params[0].apiKey);
                String dynamicUrlStr = uriBuilder.build().toString();
                URL url = new URL(dynamicUrlStr);
                // Log.v(LOG_TAG, dynamicUrlStr);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) return null;
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) buffer.append(line + "\n");

                if (buffer.length() == 0) return null;
                topMoviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
                if (reader != null)
                    try { reader.close(); } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
            }
            // Log.v(LOG_TAG, topMoviesJsonStr);
            ArrayList<MovieInfo> movies = null;
            try {
                movies = geTopMoviesFromJson(topMoviesJsonStr);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Top Movies Json parse failed", e);
            }
            return movies;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieInfo> moviesIn) {
            super.onPostExecute(moviesIn);
            if (moviesIn == null) return;
            moviesInfo.clear();
            moviesInfo.addAll(moviesIn);
            movieInfoAdapter.notifyDataSetChanged();
        }

        private  ArrayList<MovieInfo> geTopMoviesFromJson(String forecastJsonStr)
                throws JSONException {

            ArrayList<MovieInfo> movies = new ArrayList<>();

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray movieArray = forecastJson.getJSONArray("results");

            for(int i = 0; i < movieArray.length(); i++) {
                JSONObject movieInfo = movieArray.getJSONObject(i);

                String id = movieInfo.getString("id");
                String original_title = movieInfo.getString("original_title");
                String poster_path = movieInfo.getString("poster_path");
                String overview = movieInfo.getString("overview");
                String vote_average = movieInfo.getString("vote_average");
                String release_date = movieInfo.getString("release_date");

                movies.add(new MovieInfo(id, original_title, poster_path, overview, vote_average, release_date));
            }

            /*for (MovieInfo movie : movies) {
                Log.v(LOG_TAG, movie.getId() + ", " + movie.getOriginal_title() + ", " + movie.getPoster_path());
            }*/
            return movies;

        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(getActivity(), SettingsActivity.class), RESULT_SETTINGS);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SETTINGS:
                getMovies();
                break;

        }

    }
}
