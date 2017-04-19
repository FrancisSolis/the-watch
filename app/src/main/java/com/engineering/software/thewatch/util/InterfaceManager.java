package com.engineering.software.thewatch.util;

import com.engineering.software.thewatch.feed.valuelistener.PostGeoQueryEventListener;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.engineering.software.thewatch.feed.valuelistener.UserPostValueListener;
import com.engineering.software.thewatch.feed.adapter.ExploreAdapter;
import com.engineering.software.thewatch.feed.adapter.PostAdapter;
import com.engineering.software.thewatch.feed.adapter.UserAdapter;
import com.engineering.software.thewatch.feed.valuelistener.ExploreValueListener;
import com.firebase.geofire.GeoQuery;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: King
 * Date: 2/14/2017
 */

public class InterfaceManager {

    DatabaseManager db = new DatabaseManager(null);
    static GeoQuery watchFeedListener = null;

    public PostAdapter watchFeedAdapter(GeoQuery geoQuery, Query query, long timeLimit) {
        Set<String> postIds = new HashSet<>();
        List<PostInformation> posts = new ArrayList<>();

        PostAdapter adapter = new PostAdapter(db, posts, timeLimit);

        if (watchFeedListener != null) {
            watchFeedListener.removeAllListeners();
            watchFeedListener.setLocation(geoQuery.getCenter(), geoQuery.getRadius());
        }
        else
            watchFeedListener = geoQuery;

        watchFeedListener.addGeoQueryEventListener(
                new PostGeoQueryEventListener(
                adapter, postIds, posts, query,
                db.getRoot().child("user")));

        return adapter;
    }

    public PostAdapter userPostAdapter(String userID, long timeLimit) {
        List<PostInformation> posts = new ArrayList<>();

        PostAdapter adapter = new PostAdapter(db, posts, timeLimit);

        DatabaseReference postReference = db.getRoot().child("post");
        DatabaseReference userReference = db.getRoot().child("user");

        db.getRoot().child("user-post").child(userID).addValueEventListener(
                new UserPostValueListener(adapter, userID, posts, postReference, userReference));

        return adapter;
    }

    public ExploreAdapter exploreAdapter(Query query) {
        DatabaseReference userReference = db.getRoot().child("user");
        DatabaseReference postReference = query.getRef();

        ExploreAdapter adapter = new ExploreAdapter(db, new ArrayList<PostInformation>());

        postReference.addValueEventListener(
                new ExploreValueListener(postReference, userReference, adapter));

        return adapter;
    }

    public UserAdapter userAdapter(Query query) {
        DatabaseReference userReference = query.getRef();

        final UserAdapter adapter = new UserAdapter(new HashMap<String, String>());

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    adapter.map.clear();
                    adapter.userNames.clear();

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String displayName = ds.child("displayName").getValue(String.class);

                        adapter.map.put(displayName, ds.getKey());
                        adapter.userNames.add(displayName);
                        //adapter.notifyItemChanged(adapter.userNames.size() - 1);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return adapter;
    }
}
