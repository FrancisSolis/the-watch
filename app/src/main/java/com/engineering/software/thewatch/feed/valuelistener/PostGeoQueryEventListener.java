package com.engineering.software.thewatch.feed.valuelistener;

import com.engineering.software.thewatch.feed.adapter.PostAdapter;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.engineering.software.thewatch.util.StringWrapper;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Author: King
 * Date: 2/19/2017
 */

public class PostGeoQueryEventListener implements GeoQueryEventListener {

    private boolean ready = false;

    private PostAdapter adapter;

    private Set<String> postIds;

    private List<PostInformation> posts;

    private DatabaseReference validPosts;
    private DatabaseReference users;

    public PostGeoQueryEventListener(PostAdapter adapter, Set<String> postIds,
                                     List<PostInformation> posts, Query validPosts,
                                     DatabaseReference users) {
        this.adapter = adapter;
        this.postIds = postIds;
        this.posts = posts;
        this.validPosts = validPosts.getRef();
        this.users = users;
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        if (!ready)
            postIds.add(key);
        else
            addListener(key);
    }

    @Override
    public void onKeyExited(String key) {
        int position;
        if (ready && (position = posts.indexOf(new StringWrapper(key))) >= 0) {
            //int position = postsWithListeners.indexOf(key);
            posts.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
    }

    @Override
    public void onGeoQueryReady() {
        for (String postId : postIds)
            addListener(postId);

        ready = true;
        // posts.sort(new PostInformationComparator());
    }

    @Override
    public void onGeoQueryError(DatabaseError error) {

    }

    private void addListener(String postId) {
        validPosts.child(postId).addValueEventListener(
                new PostValueListener(adapter, postId, users, false));
    }
}
