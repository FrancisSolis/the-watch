package com.engineering.software.thewatch.util;

import com.engineering.software.thewatch.model.db.Post;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.engineering.software.thewatch.model.db.User;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: King
 * Date: 3/9/2017
 */

public class FeedFilters implements Serializable{

    private String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public Set<Integer> ranks;
    public boolean isFollowing;
    public double distance;
    public long time;

    public FeedFilters() {
        ranks = new HashSet<>();
        isFollowing = false;
        time = Timekeeper.DAY;
        distance = 5;
    }

    public FeedFilters(Set<Integer> ranks, boolean isFollowing, double distance, long time) {
        this.ranks = ranks;
        this.isFollowing = isFollowing;
        this.distance = distance;
        this.time = time;
    }

    // following and ranking
    public boolean filter(PostInformation post) {
        return filter(post.user);
    }

    // following and ranking
    public boolean filter(User user) {
        if (user != null)
            return ((ranks.size() == 0
                    || ranks.size() == 8
                    || ranks.contains(user.rank))
                    && (!isFollowing
                    || (user.follower != null
                    && user.follower.keySet().contains(myID))));
        else
            return false;
    }

    // time restriction
    public boolean filter(Post post) {
        if (time == Timekeeper.NONE)
            return true;
        else
            return post.timestamp >= Timekeeper.currentTime - time;
    }

    // time restriction
    public boolean filter(long timestamp) {
        if (time == Timekeeper.NONE)
            return true;
        else
            return timestamp >= Timekeeper.currentTime - time;
    }
}
