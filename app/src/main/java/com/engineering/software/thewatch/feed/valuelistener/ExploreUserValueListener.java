package com.engineering.software.thewatch.feed.valuelistener;

import com.engineering.software.thewatch.feed.adapter.ExploreAdapter;
import com.engineering.software.thewatch.util.GlobalFilters;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.engineering.software.thewatch.util.StringWrapper;
import com.engineering.software.thewatch.model.db.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Author: King
 * Date: 3/16/2017
 */

public class ExploreUserValueListener implements ValueEventListener {

    private ExploreAdapter adapter;

    private PostInformation postInformation;

    private String postId;

    public ExploreUserValueListener(ExploreAdapter adapter, String postId,
                                    PostInformation post) {
        this.adapter = adapter;
        this.postInformation = post;
        this.postId = postId;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        User user = dataSnapshot.getValue(User.class);

        if (user != null) {
//            if ((position = adapter.effectivePosts().indexOf(new StringWrapper(postId))) >= 0) {
//                FeedFilters ff = adapter.feedFilters;
//                if (ff != null
//                        && ((ff.ranks.size() == 0q
//                        || ff.ranks.size() == 8
//                        || ff.ranks.contains(user.rank))
//                        && (ff.isFollowing
//                        == (user.follower != null
//                        && user.follower.keySet().contains(user.id))))) {
            int position = adapter.posts.indexOf(new StringWrapper(postId));
            if (GlobalFilters.exploreFilter.filter(user)
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

            } else if ( GlobalFilters.exploreFilter.filter(user)
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
