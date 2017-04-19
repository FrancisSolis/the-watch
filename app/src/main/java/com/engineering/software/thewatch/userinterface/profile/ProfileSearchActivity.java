package com.engineering.software.thewatch.userinterface.profile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.feed.adapter.UserAdapter;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.engineering.software.thewatch.util.InterfaceManager;
import com.google.firebase.database.Query;

public class ProfileSearchActivity extends AppCompatActivity {

    DatabaseManager dbm;
    InterfaceManager im;
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        im = new InterfaceManager();
        dbm = new DatabaseManager(this);

        LinearLayoutManager sgl = new LinearLayoutManager(this);
        sgl.scrollToPositionWithOffset(0, 0);

        rv = (RecyclerView) findViewById(R.id.listView);
        //rv.setHasFixedSize(true);
        rv.setLayoutManager(sgl);
        // Get the location manager

        Query query = dbm.getRoot().child("user").orderByKey();
        UserAdapter adapter = im.userAdapter(query);

        rv.setAdapter(adapter);
        setQueryListener(adapter);
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void setQueryListener(final UserAdapter adapter) {
        SearchView mSearchView = (SearchView) findViewById(R.id.search);
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
