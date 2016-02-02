package com.example.omar.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ReviewDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activityReviewDetail, new ReviewDetailFragment())
                    .commit();
        }
    }

}
