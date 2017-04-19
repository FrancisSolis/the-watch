package com.engineering.software.thewatch.feed.valuelistener;

import com.engineering.software.thewatch.feed.adapter.ExploreAdapter;
import com.engineering.software.thewatch.util.GlobalFilters;
import com.engineering.software.thewatch.model.db.Post;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.engineering.software.thewatch.util.StringWrapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Author: King
 * Date: 3/16/2017
 */

public class ExplorePostValueListener
        implements ValueEventListener {

    private DatabaseReference userReference;
    private ExploreAdapter adapter;

    public ExplorePostValueListener(DatabaseReference userReference, ExploreAdapter adapter) {
        this.userReference = userReference;
        this.adapter = adapter;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Post post = dataSnapshot.getValue(Post.class);

        if (post != null
                && (GlobalFilters.exploreFilter.filter(post))) {
            post.postID = dataSnapshot.getKey();

            PostInformation newPost = new PostInformation(post);

            int position;

            if ((position = adapter.effectivePosts().indexOf(new StringWrapper(dataSnapshot.getKey()))) >= 0) {
                adapter.posts.get(position).post = post;
                adapter.notifyItemChanged(position);
            } else { // if post does not exist, add to list, ordered by timestamp
//                int location = 0;

                userReference.child(post.userID).addValueEventListener(
                        new ExploreUserValueListener(adapter, dataSnapshot.getKey(), newPost));
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
