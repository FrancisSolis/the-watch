package com.engineering.software.thewatch.util;

import com.engineering.software.thewatch.model.feed.PostInformation;

/**
 * Author: King
 * Date: 2/22/2017
 */

public class StringWrapper {
    private String string;

    public StringWrapper(String string) {
        this.string = string;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PostInformation)
            return string.equals(((PostInformation) obj).post.postID);
        else
            return false;
    }
}
