package fr.lvldev.popularmovieapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment {

    ImageGridAdapter mMoviesAdapter;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMoviesAdapter = new ImageGridAdapter(getActivity(),new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView)rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMoviesAdapter);

        return rootView;
    }

    private void updateMovies(){
        FetchMovies moviesTask = new FetchMovies();
        moviesTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    public class FetchMovies extends AsyncTask<Void,Void, String[]> {

        private final String LOG_TAG = FetchMovies.class.getSimpleName();

        @Override
        protected String[] doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJasonStr = null;

            try {
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, "popularity.desc")
                        .appendQueryParameter(KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                moviesJasonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getImageLinkFromJson(moviesJasonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Get Poster path from JSON file
         * @param moviesJsonStr JSON file that contain list of movies data
         * @return String array of path
         * @throws JSONException
         */
        private String[] getImageLinkFromJson(String moviesJsonStr) throws JSONException {

            final String M_LIST = "results";
            final String M_PATH = "poster_path";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(M_LIST);

            String[] links = new String[moviesArray.length()];

            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movieObject = moviesArray.getJSONObject(i);
                String link_end = movieObject.getString(M_PATH);
                links[i]="http://image.tmdb.org/t/p/"+"w185/"+link_end;
            }

            return links;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                mMoviesAdapter.clear();
                for (String dayForecastStr : strings) {
                    mMoviesAdapter.add(dayForecastStr);

                    Log.d(LOG_TAG,dayForecastStr);
                }

            }
        }
    }

}
