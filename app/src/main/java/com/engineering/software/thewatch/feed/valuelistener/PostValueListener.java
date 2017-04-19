package com.engineering.software.thewatch.feed.valuelistener;

import android.util.Log;

import com.engineering.software.thewatch.feed.adapter.PostAdapter;
import com.engineering.software.thewatch.model.db.Post;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.engineering.software.thewatch.util.GlobalFilters;
import com.engineering.software.thewatch.util.StringWrapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Author: King
 * Date: 2/19/2017
 */

public class PostValueListener implements ValueEventListener {

    private PostAdapter adapter;
    private DatabaseReference users;

    private String postId;
    private boolean userPostsView;

    public PostValueListener(PostAdapter adapter, String postId,
                             DatabaseReference users, boolean userPostsView) {
        this.adapter = adapter;
        this.postId = postId;
        this.users = users;
        this.userPostsView = userPostsView;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Post post = dataSnapshot.getValue(Post.class);

//        Log.d("insert", posts.size() + "");
//        Log.d("time", "post time" + post.timestamp);
//        Log.d("time", "server time" + (Timekeeper.currentTime - 86400000));

        // if post is not null and either:
        // there is NO time limit OR
        // the post is within the time limit
        Log.d("filter", "time filter" + ((GlobalFilters.watchFilter.filter(post)) ? "true" : "false"));
        Log.d("filter", "time filter " + post.timestamp + " >= " + GlobalFilters.watchFilter.time);

        if (post != null
                && (userPostsView
                || (GlobalFilters.watchFilter.filter(post)))) {
            post.postID = postId;

            PostInformation newPost = new PostInformation(post);

            int position;

            if ((position = adapter.effectivePosts().indexOf(new StringWrapper(postId))) >= 0) {
                adapter.posts.get(position).post = post;
                adapter.notifyItemChanged(position);
            } else { // if post does not exist, add to list, ordered by timestamp
                users.child(post.userID).addValueEventListener(
                        new UserValueListener(adapter, postId, newPost, userPostsView));
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
