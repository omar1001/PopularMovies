package com.example.omar.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class DetailFragment extends Fragment {

    public static String DETAIL_TAG = "detail tag";

    String JsonReviews;
    TrailerAdapter mTrailerAdapter;
    ReviewsAdapter mReviewAdapter;
    ExpandedListView reviewListView;
    ExpandedListView trailerListView;
    DBHandler mDBHandler;
    Container movie;
    ImageButton favoriteButton;
    boolean fetchEnded = false;
    boolean favoriteButtonClicked = false;
    boolean wasFavorite = false;

    private ShareActionProvider mShareActionProvider;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public interface Callback {
        void updateAdapterAfterUnMarkFavorite(int position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mDBHandler = new DBHandler(getContext());
        Bundle arguments = getArguments();

        if ( null != arguments ) {

            movie = (Container) arguments.getSerializable(PosterFragment.MOVIE_TAG);

            if(arguments.containsKey(DETAIL_TAG)) {
                movie.fromTwoPane = false;
            }
            else {
                movie.fromTwoPane = true;
            }
            getActivity().setTitle(movie.name);

            ImageView poster = (ImageView) rootView.findViewById(R.id.imageView);
            poster.setLayoutParams(new RelativeLayout.LayoutParams(250, 300));

            final String URL_BASE = "http://image.tmdb.org/t/p/w185/";
            Uri imageUrl = Uri.parse(URL_BASE).buildUpon()
                    .appendPath(movie.imageURL)
                    .build();
            Picasso
                    .with(getActivity())
                    .load(imageUrl)
                    .placeholder(R.drawable.preload)
                    .error(R.drawable.preload_error)
                    .into(poster);

            String OVERVIEW = "Overview:\n" + movie.overView;
            String VOTE = "Vote: " + movie.voteAverage;

            ((TextView) rootView.findViewById(R.id.titleText))
                    .setText(movie.name);
            ((TextView) rootView.findViewById(R.id.overViewText))
                    .setText(OVERVIEW);
            ((TextView) rootView.findViewById(R.id.releaseDateText))
                    .setText(movie.releaseDate);
            ((TextView) rootView.findViewById(R.id.voteAverageText))
                    .setText(VOTE);

            //movieID = movie.id;

            //Trailer listView
            trailerListView = (ExpandedListView)rootView.findViewById(R.id.listView);
            mTrailerAdapter = new TrailerAdapter(getActivity());
            trailerListView.setAdapter(mTrailerAdapter);
            trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    String video_path = "http://www.youtube.com/watch?v="
                            + mTrailerAdapter.getItem(position).key;
                    Uri uri = Uri.parse(video_path);
                    uri = Uri.parse("vnd.youtube:" + uri.getQueryParameter("v"));
                    Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(youtubeIntent);
                }
            });

            //Review listView
            reviewListView = (ExpandedListView)rootView.findViewById(R.id.listView2);
            mReviewAdapter = new ReviewsAdapter(getActivity());
            reviewListView.setAdapter(mReviewAdapter);
            reviewListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    Intent detailIntent = new Intent(getActivity(), ReviewDetail.class)
                            .putExtra("name", mReviewAdapter.getItem(position).name)
                            .putExtra("content", mReviewAdapter.getItem(position).review);
                    startActivity(detailIntent);
                }
            });

            favoriteButton = (ImageButton) rootView.findViewById(R.id.favoriteButton);

            wasFavorite = movie.isFavorite;
            new FindMovieInDB().execute();

            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(movie.isFavorite) {
                        removeMovieFromFavorites();
                    } else {
                        favoriteButtonClicked = true;
                        addMovieToFavorites();
                    }
                }
            });

            //Fetch Trailers then reviews
            if(!movie.isFavorite) {
                if(isNetworkAvailable()) {
                    new FetchJsonTask().execute("videos");
                }
            } else if(movie.reviewsJsonArray != null
                        && movie.videosJsonArray != null) {
                sendResultsToTrailers();
                sendResultsToReviews();
            }
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null && mTrailerAdapter != null ) {
            String key = getFromJson(movie.videosJsonArray, 0, "key");
            if(key == null) {
                key = movie.name;
            }
            mShareActionProvider.setShareIntent(createShareForecastIntent(key));
        }
    }

    private Intent createShareForecastIntent(String key) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                 movie.name + "\nTrailer: http://www.youtube.com/watch?v="
                        + key);
        return shareIntent;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void addMovieToFavorites() {
        //This method checks if trailers and reviews
        //are fetched first then add to DB after favorite clicked

        if((fetchEnded || wasFavorite) && favoriteButtonClicked) {
            mDBHandler.addMovie(movie);
            toggleFavorite(true);
            Toast.makeText(getActivity(), "Added to favorites", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void removeMovieFromFavorites() {
        if(movie.fromTwoPane) {
            ((Callback) getActivity())
                    .updateAdapterAfterUnMarkFavorite(
                            movie.positionInAdapter);
        }
        mDBHandler.deleteMovie(movie.id);
        toggleFavorite(false);
        Toast.makeText(getActivity(), "Removed from favorites", Toast.LENGTH_SHORT)
                .show();
    }

    public void toggleFavorite(boolean flag) {
        if(flag) {
            favoriteButton.setImageResource(R.drawable.favorite_on);
        } else {
            favoriteButton.setImageResource(R.drawable.favorite_off);
        }
        movie.isFavorite = flag;
    }

    private String getFromJson(String Json, int position, String demand) {
        try{
            JSONObject moviesJson = new JSONObject(Json);
            JSONArray moviesJsonArray = moviesJson.getJSONArray("results");

            if(demand.equals("length"))
                return Integer.toString(moviesJsonArray.length());

            JSONObject movieByIndex = moviesJsonArray.getJSONObject(position);
            return movieByIndex.getString(demand);

        }catch(Exception e){
            //Log.e("getFromJson " + Integer.toString(position) + demand, Json, e);
        }
        return null;
    }

    private class TrailerLinkContainer {
        String key = null;
        public TrailerLinkContainer(int position) {
            key = getFromJson(movie.videosJsonArray, position, "key");

            if(position == 0 && key != null && mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent(key));
            }
        }
    }

    public class TrailerAdapter extends ArrayAdapter<TrailerLinkContainer>{
        Context context;
        private LayoutInflater inflater = null;


        public TrailerAdapter(Activity context){
            super(context, 0);
            this.context = context;
            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.trailer_item, null);
            }

            String TRAILER = "Trailer " + Integer.toString(position + 1);

            ((TextView) convertView.findViewById(R.id.trailerText))
                    .setText(TRAILER);
            return convertView;
        }
    }

    public class ReviewsContainer {
        String review;
        String name;
        public ReviewsContainer(int position) {
            name = getFromJson(movie.reviewsJsonArray, position, "author");
            review = getFromJson(movie.reviewsJsonArray, position, "content");
        }
    }

    public class ReviewsAdapter extends ArrayAdapter<ReviewsContainer> {
        Context context;
        private LayoutInflater inflater = null;

        public ReviewsAdapter(Activity context){
            super(context, 0);
            this.context = context;
            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.review_item, null);
            }
            ReviewsContainer r = getItem(position);

            ((TextView) convertView.findViewById(R.id.authorNameText))
                    .setText(r.name);

            String review = r.review.substring(0, Math.min(80, r.review.length()))
                    + "....";
            ((TextView) convertView.findViewById(R.id.contentText))
                    .setText(review);

            return convertView;
        }
    }


    public class FetchJsonTask extends AsyncTask<String, Void, String> {
        String flag = null;
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuffer buffer = new StringBuffer();
            String result = null;
            flag = params[0];

            try {

                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String TASK = params[0];
                final String API_KEY = "api_key";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendPath(movie.id)
                        .appendPath(TASK)
                        .appendQueryParameter(API_KEY, PosterFragment.API_KEY_HIDED)
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

                result = buffer.toString();

            } catch (IOException e) {
                Log.e("DetailActivity", "Connection Failed", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("DetailActivity", "Error closing stream", e);
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            if(flag.equals("videos")) {
                movie.videosJsonArray = result;
                sendResultsToTrailers();
                //Fetch reviews
                new FetchJsonTask().execute("reviews");
            }

            else {
                JsonReviews = result;
                movie.reviewsJsonArray = result;
                sendResultsToReviews();
                fetchEnded = true;
                //Check if the favorite button has clicked before fetching
                addMovieToFavorites();
            }
        }
    }

    public void sendResultsToReviews() {
        int lim = Integer.parseInt(getFromJson(movie.reviewsJsonArray, 0, "length"));
        for(int i=0; i<lim; i++) {
            mReviewAdapter.add(new ReviewsContainer(i));
        }
    }
    public void sendResultsToTrailers() {
        int lim = Integer.parseInt(getFromJson(movie.videosJsonArray, 0, "length"));
        for(int i=0; i<lim; i++) {
            mTrailerAdapter.add(new TrailerLinkContainer(i));
        }
    }

    public class FindMovieInDB extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return mDBHandler.findMovie(movie.id);
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            //Update the button according to the result

            toggleFavorite(flag);
        }
    }
}
