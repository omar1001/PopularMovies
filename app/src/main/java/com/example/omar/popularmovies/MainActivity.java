package com.example.omar.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements PosterFragment.Callback,
                                                                DetailFragment.Callback {

    PosterFragment pf;
    private static final String DETAIL_FRAGMENT_TAG = "DF_TAG";
    public boolean mTwoPane = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pf = (PosterFragment)getSupportFragmentManager().findFragmentById(R.id.container);

        if(findViewById(R.id.detailActivityContainer) != null) {
            mTwoPane = true;
            if(savedInstanceState == null) {
                DetailFragment df = new DetailFragment();
                Bundle args = new Bundle();
                args.putString("not", "false");
                //df.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detailActivityContainer, df
                                , DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
/*
        PosterFragment pf = new PosterFragment();
        Bundle arguments = new Bundle();

        if(mTwoPane) {
            arguments.putString(PosterFragment.PF_TAG, "true");
        }
        pf.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, pf, "o").commit();
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onItemSelected(Container movie) {
        if(mTwoPane) {

            FragmentStartHelper(movie);

        } else {

            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(PosterFragment.MOVIE_TAG
                            , movie);
            startActivity(intent);
        }
    }

    public void putFirstMovieInDetailsFragment(Container movie) {
        if(mTwoPane) {
            FragmentStartHelper(movie);
        }
    }

    private void FragmentStartHelper(Container movie) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(PosterFragment.MOVIE_TAG, movie);

        DetailFragment df = new DetailFragment();
        df.setArguments(arguments);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detailActivityContainer, df
                        , DETAIL_FRAGMENT_TAG)
                .commit();
    }

    public void updateAdapterAfterUnMarkFavorite(int position) {
        pf.updateAdapter(position);
    }
}
