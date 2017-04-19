package com.engineering.software.thewatch.feed.valuelistener;

import com.engineering.software.thewatch.feed.adapter.PostAdapter;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

/**
 * Author: King
 * Date: 3/7/2017
 */

public class UserPostValueListener implements ValueEventListener {

    PostAdapter adapter;

    List<PostInformation> posts;

    DatabaseReference postReference;
    DatabaseReference userReference;

    String userID;

    public UserPostValueListener(PostAdapter adapter, String userID,
                             List<PostInformation> posts, DatabaseReference postReference,
                             DatabaseReference userReference) {
        this.adapter = adapter;
        this.posts = posts;
        this.userID = userID;
        this.postReference = postReference;
        this.userReference = userReference;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        GenericTypeIndicator<Map<String, Boolean>> type = new GenericTypeIndicator<Map<String, Boolean>>(){};
        Map<String, Boolean> postIDs = dataSnapshot.getValue(type);
        adapter.posts.clear();

        if (postIDs != null) {
            for (String postID : postIDs.keySet()) {
                postReference.child(postID).addValueEventListener(
                        new PostValueListener(adapter, postID, userReference, true));
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
