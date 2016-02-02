package com.example.omar.popularmovies;

/**
 * Created by omar on 1/2/16.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

public class ExpandedListView extends ListView
{
    private android.view.ViewGroup.LayoutParams params;
    private int oldCount = 0;

    public ExpandedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int count = getCount();
        if (count != oldCount)
        {
            int height = getChildAt(0).getHeight() + 1 ;
            oldCount = count;
            params = getLayoutParams();
            params.height = count * height;
            setLayoutParams(params);
        }
        super.onDraw(canvas);
    }
}