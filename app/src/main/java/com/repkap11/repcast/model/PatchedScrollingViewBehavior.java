package com.repkap11.repcast.model;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.appbar.AppBarLayout;

import java.util.List;

public class PatchedScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    public PatchedScrollingViewBehavior() {
        super();
    }

    public PatchedScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        if (child.getLayoutParams().height == -1) {
            List dependencies = parent.getDependencies(child);
            if (dependencies.isEmpty()) {
                return false;
            }

            AppBarLayout appBar = findFirstAppBarLayout(dependencies);
            if (appBar != null && ViewCompat.isLaidOut(appBar)) {
                if (ViewCompat.getFitsSystemWindows(appBar)) {
                    ViewCompat.setFitsSystemWindows(child, true);
                }

                int scrollRange = appBar.getTotalScrollRange();
//                int height = parent.getHeight() - appBar.getMeasuredHeight() + scrollRange;
                int parentHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
                int height = parentHeight - appBar.getMeasuredHeight() + scrollRange;
                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);
                parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
                return true;
            }
        }

        return false;
    }


    private static AppBarLayout findFirstAppBarLayout(List<View> views) {
        int i = 0;

        for (int z = views.size(); i < z; ++i) {
            View view = (View) views.get(i);
            if (view instanceof AppBarLayout) {
                return (AppBarLayout) view;
            }
        }

        return null;
    }
}
