package com.engineering.software.thewatch.util;

/**
 * Author: King
 * Date: 3/14/2017
 */

public class GlobalFilters {

    public static FeedFilters watchFilter;
    public static FeedFilters mapFilter;
    public static FeedFilters exploreFilter;

    private GlobalFilters() {}

    public static void initializeFilters() {
        watchFilter = new FeedFilters();
        mapFilter = new FeedFilters();
        exploreFilter = new FeedFilters();
    }
}
