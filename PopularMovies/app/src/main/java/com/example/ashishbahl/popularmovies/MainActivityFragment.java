package com.example.ashishbahl.popularmovies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
public class MainActivityFragment extends Fragment {

    private MovieIconAdapter movieAdapter;

    ArrayList<String> movieUrls = new ArrayList<>();

    public MainActivityFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        movieAdapter = new MovieIconAdapter(getActivity(), movieUrls);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView)rootView.findViewById(R.id.movie_grid);
        gridView.setAdapter(movieAdapter);
        return rootView;
    }

    private void updateData(){
        MovieDataTask updateGrid = new MovieDataTask();
        updateGrid.execute();
    }

    @Override
    public void onStart(){
        super.onStart();
        updateData();
    }

    public class MovieDataTask extends AsyncTask<Void,Void,String[]>{


        private final String LOG_TAG = MovieDataTask.class.getSimpleName() ;

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getMovieIconfromJSONString(String movieDataStr) throws JSONException
        {
            final String MDB_RESULT = "results";
            final String MDB_PATH= "poster_path";
            final String URL_PREFIX= "http://image.tmdb.org/t/p/w185";

            JSONObject movieJson = new JSONObject(movieDataStr);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULT);

            String[] resultURLs = new String[movieArray.length()];

            for(int i=0;i < movieArray.length();i++) {
                String poster_url;
                JSONObject movie1 = movieArray.getJSONObject(i);
                poster_url = movie1.getString(MDB_PATH);
                resultURLs[i] = URL_PREFIX + poster_url;
            }
            //Log.v(LOG_TAG,"Links to the poster " + resultURLs[15]);
            return resultURLs;
        }
        @Override
        protected String[] doInBackground(Void... params) {

            /*if(params.length == 0)
            return null;*/

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //Will contain the response as a raw JSON string.
            String movieDataStr = null;

            try{
                //Here we construct the url for the movie database query
                //http://api.themoviedb.org/3/discover/movie?

                /*final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String QUERY_PARAM = "sort_by";// sorting parameter popularity or rating
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0]) //String key , String value
                        .appendQueryParameter(APPID_PARAM,BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());*/

                URL url = new URL("http://api.themoviedb.org/3/discover/movie?api_key=4a1e6d0b399a527d9ae0b4f25a19d5fa");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //read the input stream into a string
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) {
                    //Do Nothing
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine())!= null){
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0) {
                    //Stream was empty.No point in parsing.
                    return null;
                }

                movieDataStr = buffer.toString();

            }
            catch (IOException e){
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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

            try{
                return getMovieIconfromJSONString(movieDataStr);
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String[] result){
            if(result != null){
                movieAdapter.clear();
                for(String abc : result){
                    movieUrls.add(abc);
                }
                movieAdapter.add(movieUrls);
            }
        }
    }
}
