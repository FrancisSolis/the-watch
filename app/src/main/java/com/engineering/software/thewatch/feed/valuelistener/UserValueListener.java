package com.engineering.software.thewatch.feed.valuelistener;

import com.engineering.software.thewatch.feed.adapter.PostAdapter;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.engineering.software.thewatch.model.db.User;
import com.engineering.software.thewatch.util.GlobalFilters;
import com.engineering.software.thewatch.util.StringWrapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Author: King
 * Date: 2/22/2017
 */

public class UserValueListener implements ValueEventListener {

    private PostAdapter adapter;
    private PostInformation postInformation;

    private String postId;
    private boolean userPostsView;

    public UserValueListener(PostAdapter adapter, String postId,
                             PostInformation post, boolean userPostsView) {
        this.adapter = adapter;
        this.postInformation = post;
        this.postId = postId;
        this.userPostsView = userPostsView;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        User user = dataSnapshot.getValue(User.class);

        if (user != null) {
            int position = adapter.posts.indexOf(new StringWrapper(postId));
            if ((userPostsView
                    || GlobalFilters.watchFilter.filter(user))
                    && position == -1) {
                int location = 0;

                if (adapter.posts.size() > 0) {
                    for (int i = 0; i < adapter.posts.size(); i++) {
                        if (postInformation.post.timestamp > adapter.posts.get(i).post.timestamp)
                            break;
                        location++;
                    }
                }

                postInformation.user = user;
                adapter.posts.add(location, postInformation);
                adapter.notifyDataSetChanged();

            } else if ((userPostsView
                    || GlobalFilters.watchFilter.filter(user))
                    && position >= 0) {
                postInformation.user = user;
                adapter.notifyDataSetChanged();
            } else if (position >= 0) {
                adapter.posts.remove(postInformation);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
