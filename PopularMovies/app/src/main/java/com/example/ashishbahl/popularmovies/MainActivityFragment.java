package com.example.ashishbahl.popularmovies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
    final int REQ_CODE=1;
    final String LOG_TAG=MainActivityFragment.class.getSimpleName();
    private MovieIconAdapter movieAdapter;
    public final static String MOV_KEY="MovieObject";

    ArrayList<Movie> movies;

    public MainActivityFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        if(savedInstanceState == null || !savedInstanceState.containsKey("posters"))
            movies=new ArrayList<Movie>();
        else{
            movies = savedInstanceState.getParcelableArrayList("posters");
        }
        setHasOptionsMenu(true);
    }

    public void onSaveInstanceState(Bundle outState){
        outState.putParcelableArrayList("posters",movies);
        super.onSaveInstanceState(outState);
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
        movieAdapter = new MovieIconAdapter(getActivity(), movies);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView)rootView.findViewById(R.id.movie_grid);
        gridView.setAdapter(movieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*final String LOG_TAG = AdapterView.OnItemClickListener.class.getSimpleName() ;
                Log.v(LOG_TAG,"poster_path: "+ movies.get(position).getPoster());*/
                Movie x=movies.get(position);
                Intent intent = new Intent(getActivity(),DetailActivity.class)
                        .putExtra(MOV_KEY,x);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void updateData(){
        MovieDataTask updateGrid = new MovieDataTask();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_parameter = sharedPreferences.
                getString(getString(R.string.pref_sort_key),getString(R.string.pref_sort_default));
        updateGrid.execute(sort_parameter);
    }

    @Override
    public void onStart(){
        updateData();
        super.onStart();
    }

    public class MovieDataTask extends AsyncTask<String,Void,ArrayList<Movie>>{


        private final String LOG_TAG = MovieDataTask.class.getSimpleName() ;

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private ArrayList<Movie> getMovieDatafromJSONString(String movieDataStr) throws JSONException
        {
            final String MDB_RESULT = "results";
            final String MDB_PATH= "poster_path";
            final String MDB_TITLE="original_title";
            final String MDB_OVERVIEW="overview";
            final String MDB_VOTE="vote_average";
            final String MDB_DATE="release_date";
            final String MDB_URL_PREFIX= "http://image.tmdb.org/t/p/w185";

            JSONObject movieJson = new JSONObject(movieDataStr);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULT);

            ArrayList<Movie> results = new ArrayList<Movie>();

            for(int i=0;i < movieArray.length();i++) {
                String poster_url;
                String title;
                String overview;
                String vote;
                String date;
                JSONObject movie1 = movieArray.getJSONObject(i);
                poster_url = MDB_URL_PREFIX + movie1.getString(MDB_PATH);
                title=movie1.getString(MDB_TITLE);
                overview=movie1.getString(MDB_OVERVIEW);
                vote=movie1.getString(MDB_VOTE);
                date=movie1.getString(MDB_DATE);
                results.add(new Movie(poster_url,title,overview,vote,date));
            }
            return results;
        }
        private ProgressDialog dialog = new ProgressDialog(getActivity());

        /** progress dialog to show user that the backup is processing. */
        /** application context. */
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Loading");
            this.dialog.show();
        }
        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            if(params.length == 0)
            return null;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //Will contain the response as a raw JSON string.
            String movieDataStr = null;

            try{
                //Here we construct the url for the movie database query
                //http://api.themoviedb.org/3/discover/movie?

                final String BASE_URL = "http://api.themoviedb.org/3/movie/";
                //final String QUERY_PARAM = "sort_by";// sorting parameter popularity or rating
                final String APPID_PARAM = "api_key";
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(params[0])
                        //.appendQueryParameter(QUERY_PARAM,params[0]) //String key , String value
                        .appendQueryParameter(APPID_PARAM,BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());

                // Create the request to TheMovieDatabase, and open the connection
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
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
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
                return getMovieDatafromJSONString(movieDataStr);
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(ArrayList<Movie> result){
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if(result != null){
                movieAdapter.clear();
                movieAdapter.addAll(result);
                movieAdapter.notifyDataSetChanged();
            }
        }
    }
}
