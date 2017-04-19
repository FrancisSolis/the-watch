package com.engineering.software.thewatch.model.feed;

import com.engineering.software.thewatch.model.db.Post;
import com.engineering.software.thewatch.model.db.User;

/**
 * Author: King
 * Date: 2/19/2017
 */

public class PostInformation {
    public Post post;
    public User user;

    public PostInformation(Post post) {
        this.post = post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public void setUser(User user) {
        this.user= user;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PostInformation)
            return (post.equals(((PostInformation) o).post));
        else if (o instanceof Post)
            return (post.equals(o));
        else if (o instanceof String)
            return (post.postID.equals(o));
        else
            return false;
    }
}
