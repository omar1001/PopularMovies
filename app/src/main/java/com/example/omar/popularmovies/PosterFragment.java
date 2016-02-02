package com.example.omar.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.omar.popularmovies.data.DBHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by omar on 12/11/15.
 */
public class PosterFragment extends Fragment{

    public static final String PF_TAG = "tag";
    public static final String MOVIE_TAG = "MOVIE";
    public static final String SAVED_JSONOBJECTS_KEY = "JOK";
    private static final String SAVED_CURRENTPREF_KEY = "SCK";
    private static final String SAVED_CURRENTPAGE_KEY = "SCP";
    private static final String SAVED_GRIDVIEW_POS_KEY = "SGPK";
    public static final String API_KEY_HIDED = BuildConfig.TMDB_API_KEY;
    public String MoviesJsonString = null;
    GridView mGridView;
    ImageAdapter mImageAdapter;
    JSONArray moviesJsonArray = null;
    String currentPref = null;
    boolean scrollFlag = false;
    boolean DBHasMore = true;
    boolean TwoPaneFlag = false;
    String imageSize;
    int currentPage = 1;
    DBHandler mDBHandler;


    public PosterFragment(){
    }

    public interface Callback {
        void onItemSelected(Container movie);
        void putFirstMovieInDetailsFragment(Container movie);
    }

    public void updateAdapter(int position) {
        if(currentPref.equals(getString(R.string.pref_sort_favorites))) {
            mImageAdapter.remove(mImageAdapter.getItem(position));
            mImageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.poster_fragment, container, false);

        TwoPaneFlag = true;


        mGridView = (GridView) rootView.findViewById(R.id.gridview);

        mImageAdapter = new ImageAdapter(getActivity());

        mDBHandler = new DBHandler(getContext());

        mGridView.setAdapter(mImageAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Container movie = mImageAdapter.getItem(position).setDetails();
                movie.positionInAdapter = position;
                ((Callback) getActivity())
                        .onItemSelected(movie);
            }
        });

        //Update the gridView after scrolling to the end
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem
                    , int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem + visibleItemCount >= totalItemCount && scrollFlag
                        && totalItemCount != 0) {
                    //ScrollFlag if assigned false to avoid repeating
                    //and it will be updated true after fetching
                    scrollFlag = false;
                    updateMovies(currentPage + 1);
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        if(savedInstanceState != null &&
                savedInstanceState.containsKey(SAVED_JSONOBJECTS_KEY)) {

            Log.v("---", "s");

            currentPage = savedInstanceState.getInt(SAVED_CURRENTPAGE_KEY);
            currentPref = savedInstanceState.getString(SAVED_CURRENTPREF_KEY);
            int firstVisible = savedInstanceState.getInt(SAVED_GRIDVIEW_POS_KEY);

            ArrayList<Container> lastListOfAdapter = new ArrayList<>();
            ArrayList<String> savedStringList = savedInstanceState
                    .getStringArrayList(SAVED_JSONOBJECTS_KEY);
            for (int i = 0; i < savedStringList.size(); i++) {
                lastListOfAdapter.add(new Container(savedStringList.get(i)));
            }

            mImageAdapter.addAll(lastListOfAdapter);
            mGridView.smoothScrollToPosition(firstVisible);
            scrollFlag = true;
        }

        imageSize = TwoPaneFlag? "w185": "w185";

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_top_rated));

        //Start new adapter if the settings changed
        if(!sortBy.equals(currentPref)) {
            currentPref = sortBy;
            currentPage = 1;
            DBHasMore = true;
            mImageAdapter.clear();
            updateMovies(currentPage);
        }
    }

    public void updateMovies(int page) {
        if(!currentPref.equals("favorites")) {
            new FetchMoviesTask().execute(page);
        }
        else if(currentPref.equals("favorites") && DBHasMore) {
            new FetchFavorites().execute();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        ArrayList<String> moviesJsonObjects = new ArrayList<>();
        for(int i=0; i<mImageAdapter.getCount(); i++) {
            Container movie = mImageAdapter.getItem(i);
            if(movie.detailsJsonObject != null) {
                moviesJsonObjects.add(movie.detailsJsonObject);
            }
        }

        outState.putStringArrayList(SAVED_JSONOBJECTS_KEY, moviesJsonObjects);
        outState.putString(SAVED_CURRENTPREF_KEY, currentPref);
        outState.putInt(SAVED_GRIDVIEW_POS_KEY, mGridView.getFirstVisiblePosition());
        outState.putInt(SAVED_CURRENTPAGE_KEY, currentPage);

        super.onSaveInstanceState(outState);
    }

    public class ImageAdapter extends ArrayAdapter<Container> {
        Activity context;
        LayoutInflater inflater;
        //View rootView;
        public ImageAdapter(Activity context){
            super(context, 0);
            this.context = context;
            inflater  = (LayoutInflater)context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;
            if(convertView == null) {
                //rootView = inflater.inflate(R.layout.poster_image, null);
                //image = (ImageView) rootView.findViewById(R.id.posterImage);
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(240, 361));
                //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                //imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView)convertView;
            }

            final String URL_BASE = "http://image.tmdb.org/t/p/" + imageSize +"/";
            Uri imageUrl = Uri.parse(URL_BASE).buildUpon()
                    .appendPath(getItem(position).imageURL)
                    .build();

            Picasso
                    .with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.preload)
                    .error(R.drawable.preload_error)
                    .into(imageView);

            return imageView;
        }
    }

    public class FetchMoviesTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuffer buffer = new StringBuffer();
            Integer flag = 1;

            try {

                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String SORT_BY = currentPref;
                final String PAGE = "page";
                final String API_KEY = "api_key";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendPath(SORT_BY)
                        .appendQueryParameter(API_KEY, API_KEY_HIDED)
                        .appendQueryParameter(PAGE, Integer.toString(params[0]))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                if(inputStream == null)
                    return null;

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null)
                    buffer.append(line + '\n');
                if(buffer.length() == 0)
                    return null;

                MoviesJsonString = buffer.toString();

            } catch (IOException e) {
                Log.e(getActivity().toString(), "Connection Failed", e);
                flag = 0;

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(getActivity().toString(), "Error closing stream", e);
                    }
                }
            }
            return flag;
        }

        @Override
        protected void onPostExecute(Integer flag){
            //Add to gridView adapter Json objects of each movie
            if(flag == 1) {
                moviesJsonArray = null;
                try {
                    JSONObject forecastJson = new JSONObject(MoviesJsonString);
                    moviesJsonArray = forecastJson.getJSONArray("results");

                    for (int i = 0; i < 20; i++) {
                        JSONObject movieByIndex = moviesJsonArray.getJSONObject(i);
                        Container container = new Container(movieByIndex);
                        container.positionInAdapter = mImageAdapter.getCount();
                        container.fromTwoPane = TwoPaneFlag;
                        mImageAdapter.add(container);
                    }

                    if(currentPage == 1 && mImageAdapter.getCount() > 0) {
                        ((Callback) getActivity())
                                .putFirstMovieInDetailsFragment(
                                        mImageAdapter.getItem(0).setDetails());
                    }
                    scrollFlag = true;
                    currentPage++;

                } catch (Exception e) {
                    Log.e("JsonParsing", "onPostExecute posterFragment", e);
                }
            }
        }
    }

    public class FetchFavorites extends AsyncTask<Void, Void, List<Container>> {
        @Override
        protected List<Container> doInBackground(Void... params) {
            return mDBHandler.getMoviesFromDatabase(currentPage);
        }

        @Override
        protected void onPostExecute(List<Container> result){

            for(int i=0; i<result.size(); i++) {
                Container container = result.get(i);
                container.positionInAdapter = mImageAdapter.getCount();
                container.fromTwoPane = TwoPaneFlag;
                mImageAdapter.add(container.setDetails());
            }

            if(currentPage == 1 && mImageAdapter.getCount() > 0) {
                ((Callback) getActivity())
                        .putFirstMovieInDetailsFragment(
                                mImageAdapter.getItem(0).setDetails());
            }

            DBHasMore = result.size() == 20;
            scrollFlag = DBHasMore;
            currentPage++;
        }
    }

    public class download_image extends AsyncTask<String, Void, Bitmap>{

        private ImageView view;

        public download_image(ImageView view){
            this.view = view;
        }

        @Override
        protected Bitmap doInBackground(String... params){

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                if(inputStream == null)
                    return null;

                Bitmap bm = BitmapFactory.decodeStream(inputStream);

                return bm;

            } catch (IOException e) {
                Log.e(getActivity().toString(), "Error while D. image", e);

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(getActivity().toString(), "Error closing stream, in image", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            view.setImageBitmap(bm);
        }

    }
}
