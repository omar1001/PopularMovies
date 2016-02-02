package com.example.omar.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReviewDetailFragment extends Fragment {

    public ReviewDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_review_detail, container, false);

        Intent intent = getActivity().getIntent();

        if(intent != null) {
            ((TextView)rootView.findViewById(R.id.reviewDetailName))
                    .setText(intent.getStringExtra("name"));
            ((TextView)rootView.findViewById(R.id.reviewDetailContent))
                    .setText(intent.getStringExtra("content"));
            Toast.makeText(getContext(), "done", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }
}
