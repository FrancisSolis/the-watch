package com.engineering.software.thewatch.userinterface.explore;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.engineering.software.thewatch.feed.adapter.ExploreAdapter;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.engineering.software.thewatch.util.InterfaceManager;
import com.engineering.software.thewatch.R;
import com.google.firebase.database.Query;

/**
 * Created by JKMT on 03/16/2017.
 */

public class ExploreFragment  extends android.support.v4.app.Fragment{

    View mView;
    ViewGroup mContainer;
    DatabaseManager dbm;
    InterfaceManager im;
    RecyclerView rv;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.activity_explore, container, false);

        mContainer = container;
        return mView;
    }

    public void onResume() {
        super.onResume();

        View mView = getView();

        im = new InterfaceManager();
        dbm = new DatabaseManager(mView.getContext());

        StaggeredGridLayoutManager sgl= new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        sgl.scrollToPositionWithOffset(0, 0);

        rv = (RecyclerView) mView.findViewById(R.id.rv_explore);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(sgl);
        // Get the location manager

        Query query = dbm.queryAllPosts();
        ExploreAdapter adapter = im.exploreAdapter(query);

        rv.setAdapter(adapter);
        setQueryListener(adapter);
    }

    public void setQueryListener(final ExploreAdapter adapter) {
        SearchView mSearchView = (SearchView)((CoordinatorLayout)(mContainer.getParent()))
                .findViewById(R.id.search);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("search query", newText);
                adapter.getFilter().filter(newText);
                rv.scrollToPosition(0);

                return true;
            }
        });
    }
}
