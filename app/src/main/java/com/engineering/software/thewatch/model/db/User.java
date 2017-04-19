package com.engineering.software.thewatch.model.db;

/**
 * Author: King
 * Date: 1/29/2017
 */


import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Container class for User information. Used when creating, retrieving or editing user information.
 * Does not contain a UserID because a UserID will be needed to access User information.
 */
public class User {
    public String displayName;
    public String email;
    public int rank;
    public int avatar;

    public String id; // parent directory IS the id
    public int rating;
    public int post_count;

    public Map<String,Boolean> follower;
    public Map<String,Boolean> following;


    /**
     * Default no-args constructor <br>
     * Used for the Java Model in Firebase
     */
    public User() {}

    /**
     * INPUT CONSTRUCTOR <br>
     * Constructor used when creating a User within Firebase for the first time.
     * @param id User's ID
     * @param displayName User's display name
     * @param email User's email
     */
    public User(String id, String displayName, String email, Avatar avatar) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.avatar = avatar.num;
        rating = 0;
        post_count = 0;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> ret = new HashMap<>();

        ret.put("id", displayName);
        ret.put("email", email);
        ret.put("avatar", avatar);

        return ret;
    }

    public String toString() {
        return "" + id + " "
                + displayName + " "
                + email + " "
                + rank + " "
                + avatar + " "
                + rating + " "
                + post_count + " ";
    }
}
