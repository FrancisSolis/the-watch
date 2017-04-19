package com.engineering.software.thewatch.model.db;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.engineering.software.thewatch.Tag;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: King
 * Date: 1/29/2017
 */

/**
 * Container class for all Posts.
 * Used as a container when creating, retrieving, or editing a post.
 */
public class Post {
    public String userID;
    public String title;
    public String description;
    public String mood; // nullable
    public Map<String, Boolean> tags; // nullable
    public String placeID;
    public String placePrimary;
    public String placeSecondary;
    public long timestamp;

    // dummmy fields for retrieving from Firebase
    // never actually constructed outside of above situation
    public int upvote;
    public int downvote;
    public Map<String, Boolean> upvote_user;
    public Map<String, Boolean> downvote_user;
    public String url;

    @Exclude
    public Uri image; // nullable

    public String postID;

    /**
     * Default no-args constructor <br>
     * Used for the Java Model in Firebase
     */
    public Post(){}

    /**
     * INPUT CONSTRUCTOR <br>
     * Constructor used exclusively for creating a post
     * @param userID Poster's ID
     * @param title Post's title
     * @param description Post's description
     * @param placeID Post's location ID
     * @param placePrimary
     * @param placeSecondary
     * @param mood Poster's mood
     * @param tags Post's tags
     * @param image Post's image
     */
    public Post(@NonNull String userID, @NonNull String title, @NonNull String description,
                @NonNull String placeID, @NonNull String placePrimary, @NonNull String placeSecondary,
                Mood mood, List<String> tags, Uri image) {

        this.userID = userID;
        this.title = title;
        this.description = description;
        this.mood = mood.text;
        this.image = image;

        this.tags = new HashMap<>();
        for (String tag : tags)
            this.tags.put(tag, true);

        this.placeID = placeID;
        this.placePrimary = placePrimary;
        this.placeSecondary = placeSecondary;

        upvote_user = new HashMap<>();
        upvote_user.put("dummy", false);
        downvote_user = new HashMap<>();
        downvote_user.put("dummy", false);

        url = null;
    }

    /**
     * EDIT CONSTRUCTOR <br>
     * Constructor used exclusively for editing an existing post <br>
     * If no change was made to a field, set field to null
     * @param postID Post's ID
     * @param title Post's title
     * @param description Post's description
     * @param mood Post's mood
     * @param tags Post's tags
     */
    public Post(@NonNull String postID, String title, String description, Mood mood,
                List<Tag> tags) {
        this.postID = postID;
        this.title = title;
        this.description = description;
        this.mood = mood.text;
        this.tags = new HashMap<>();

        for (Tag tag : tags)
            this.tags.put(tag.text, true);
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> ret = new HashMap<>();

        ret.put("title", title);
        ret.put("description", description);
        ret.put("mood", mood);

        return ret;
    }

    public String toString() {
        return "" + userID + " "
                + title + " "
                + description + " "
                + mood + " "
                + downvote + " "
                + upvote+ " "
                + postID + " "
                + timestamp + " ";
    }

}
