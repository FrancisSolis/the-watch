package com.engineering.software.thewatch.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.engineering.software.thewatch.model.db.Post;
import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.model.db.Avatar;
import com.engineering.software.thewatch.model.db.Rank;
import com.engineering.software.thewatch.model.db.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import me.originqiu.library.EditTag;

/**
 * Author: King
 * Date: 1/29/2017
 */

/**
 * Control class used to access and manipulate information within Firebase.
 * Will serve as the primary control structure used to manipulate User, Post, and other
 * relevant information used in The Watch.
 */
public class DatabaseManager {

    private DatabaseReference root;
    private StorageReference postImages;

    private GeoFire geofire;

    private final Context context;
    final int target_width = 300;
    final int target_height = 270;

    /**
     * Default and only constructor of DatabaseManager. On call, creates references to the root
     * database directory, post image storage directory, and avatar directory. Requires online
     * access to function.
     */
    public DatabaseManager(Context context) {
        root = FirebaseDatabase.getInstance().getReference();
        postImages = FirebaseStorage.getInstance().getReference().child("post_images");

        geofire = new GeoFire(root.child("geofire"));

        this.context = context;
    }

    /**
     * @return A reference to the root directory in Firebase.
     */
    public DatabaseReference getRoot() {
        return root;
    }


    // POST CONTROL

    public void uploadPost(final Post post, final GeoLocation geoLocation) {
        final DatabaseReference postReference = root.child("post").push();

        String postID = postReference.getKey();

        post.postID = postID;

        // store post
        postReference.setValue(post);

        // reference post in TAGS
        for (String tag : post.tags.keySet())
            root.child("tag").child(tag).child(postID).setValue(true);

        // reference post in MOODS
        root.child("mood").child(post.mood).child(postID).setValue(true);

        // set dummy values for upvote and downvote references
        postReference.child("upvote_user").child("dummy");
        postReference.child("downvote_user").child("dummy");

        // set timestamp
        postReference.child("timestamp").setValue(ServerValue.TIMESTAMP);

        // increment post count of poster
        incrementPostCount(post.userID, 1);

        // set geolocation of post
        geofire.setLocation(postID, geoLocation);

        //setTimestamp(postID);

        //Log.d("DatabaseManager", "pre-image");

        // uploading image to Storage
        if (post.image != null) {
            //Log.d("DatabaseManager", "image");
            postImages.child(postID).putFile(post.image) // if each post is limited to 1 image each
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // store image of uploaded image
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            postReference.child("url").setValue(downloadUrl.toString());

                            Log.d("DBM", downloadUrl.toString());
                        }
                    });
        }
        //Log.d("DatabaseManager", "post-image");
        // user to post index
        root.child("user-post").child(post.userID).child(postID).setValue(true);
    }

    private void incrementPostCount(final String userID, final int increment) {
        root.child("user").child(userID).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                User user = mutableData.getValue(User.class);

                Log.d("DatabaseManager", "half working");
                if (user == null)
                    return Transaction.success(mutableData);

                Log.d("DatabaseManager", user.toString());
                user.post_count += increment;

                mutableData.setValue(user);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                Log.i("DatabaseManager", "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void incrementRating(final String userID, final int increment) {
        root.child("user").child(userID).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                User user = mutableData.getValue(User.class);

                if (user == null)
                    return Transaction.success(mutableData);

                if (increment > 0)
                    user.rating++;
                else if (increment < 0)
                    user.rating--;

                if (user.rating >= Rank.get(user.rank).max)
                    user.rank++;
                else if (user.rating < Rank.get(user.rank).min)
                    user.rank--;

                mutableData.setValue(user);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                Log.i("DatabaseManager", "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    public void votePost(final String postID, final String userID, final int increment) {
        root.child("post").child(postID).runTransaction(new Transaction.Handler() {

            boolean finished = false;

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post post = mutableData.getValue(Post.class);

                Log.d("DatabaseManager", mutableData.toString());
                if (post == null && !finished)
                    return Transaction.success(mutableData);

                boolean inc = false;

                if (increment > 0) {
                    if (!post.upvote_user.containsKey(userID)) {
                        if (post.downvote_user.containsKey(userID)) {
                            // undo downvote
                            post.downvote_user.remove(userID);
                            post.downvote--;
                            incrementRating(post.userID, 1);
                        }
                        post.upvote_user.put(userID, true);
                        post.upvote++;
                        incrementRating(post.userID, 1);
                        inc = true;
                    } else {
                        post.upvote_user.remove(userID);
                        post.upvote--;
                        incrementRating(post.userID, -1);
                        inc = false;
                    }
                } else if (increment < 0) {
                    if (!post.downvote_user.keySet().contains(userID)) {
                        if (post.upvote_user.keySet().contains(userID)) {
                            post.upvote_user.remove(userID);
                            post.upvote--;
                            incrementRating(post.userID, -1);
                        }
                        post.downvote_user.put(userID, true);
                        post.downvote++;
                        incrementRating(post.userID, -1);
                        inc = false;
                    } else {
                        post.downvote_user.remove(userID);
                        post.downvote--;
                        incrementRating(post.userID, 1);
                        inc = true;
                    }
                }
//                if (increment != 0) {
//                    if (inc)
//                        incrementRating(post.userID, 1);
//                    else
//                        incrementRating(post.userID, -1);
//                }

//                finished = true;
                mutableData.setValue(post);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                finished = true;
                Log.i("DatabaseManager", "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    public Query queryAllPosts() {
        return root.child("post").orderByChild("timestamp");
    }

    public GeoQuery geoQuery(double latitude, double longitude, double radius) {
        Log.d("distance", radius + "");
        return geofire.queryAtLocation(new GeoLocation(latitude, longitude), radius);
    }


    // USER CONTROL

    public void createUser(User user) {
        DatabaseReference userReference = root.child("user").child(user.id);
        userReference.keepSynced(true);
        userReference.setValue(user);
        userReference.child("rank").setValue(Rank.WATCHER.num);
        userReference.child("rating").setValue(0);
        userReference.child("post_count").setValue(0);
    }

    public void updateUser(String userID) {

        final ImageView avatar = (ImageView) ((Activity) context).findViewById(R.id.iv_avatar);
        final TextView name = (TextView) ((Activity) context).findViewById(R.id.tv_profile_name);
        final TextView rank = (TextView) ((Activity) context).findViewById(R.id.tv_profile_rank);
        final ProgressBar pb_nayslayer = (ProgressBar) ((Activity) context).findViewById(R.id.pb_naysayer);
        final ProgressBar pb_watcher = (ProgressBar) ((Activity) context).findViewById(R.id.pb_watcher);
        final ProgressBar pb_spotter = (ProgressBar) ((Activity) context).findViewById(R.id.pb_spotter);
        final ProgressBar pb_scout = (ProgressBar) ((Activity) context).findViewById(R.id.pb_scout);
        final ProgressBar pb_spy = (ProgressBar) ((Activity) context).findViewById(R.id.pb_spy);
        final ProgressBar pb_chronicler = (ProgressBar) ((Activity) context).findViewById(R.id.pb_chronicler);
        final ProgressBar pb_maven = (ProgressBar) ((Activity) context).findViewById(R.id.pb_maven);
        final ProgressBar pb_sentinel = (ProgressBar) ((Activity) context).findViewById(R.id.pb_sentinel);

        final RelativeLayout profileActivityBG = (RelativeLayout) ((Activity) context).findViewById(R.id.activity_profile);
        final FrameLayout profileImageBG = (FrameLayout) ((Activity) context).findViewById(R.id.profile_image_bg);

        root.child("user").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name.setText((String) dataSnapshot.child("displayName").getValue());
                Rank temp = Rank.get(Integer.parseInt(dataSnapshot.child("rank").getValue().toString()));
                final int prog = Integer.parseInt(dataSnapshot.child("rating").getValue().toString());
                rank.setText(temp.text);
                switch (temp) {
                    case NAYSAYER:
                        profileActivityBG.setBackgroundResource(R.color.rank_naysayer);
                        profileImageBG.setBackgroundResource(R.drawable.profile_avatar_nayslayer_bg);
                        pb_nayslayer.setVisibility(View.VISIBLE);
                        pb_watcher.setVisibility(View.GONE);
                        pb_spotter.setVisibility(View.GONE);
                        pb_scout.setVisibility(View.GONE);
                        pb_spy.setVisibility(View.GONE);
                        pb_chronicler.setVisibility(View.GONE);
                        pb_maven.setVisibility(View.GONE);
                        pb_sentinel.setVisibility(View.GONE);
                        break;
                    case WATCHER:
                        profileActivityBG.setBackgroundResource(R.color.rank_watcher);
                        profileImageBG.setBackgroundResource(R.drawable.profile_avatar_watcher_bg);
                        pb_nayslayer.setVisibility(View.GONE);
                        pb_watcher.setVisibility(View.VISIBLE);
                        pb_watcher.setProgress(prog);
                        pb_spotter.setVisibility(View.GONE);
                        pb_scout.setVisibility(View.GONE);
                        pb_spy.setVisibility(View.GONE);
                        pb_chronicler.setVisibility(View.GONE);
                        pb_maven.setVisibility(View.GONE);
                        pb_sentinel.setVisibility(View.GONE);
                        break;
                    case SPOTTER:
                        profileActivityBG.setBackgroundResource(R.color.rank_spotter);
                        profileImageBG.setBackgroundResource(R.drawable.profile_avatar_spotter_bg);
                        pb_nayslayer.setVisibility(View.GONE);
                        pb_watcher.setVisibility(View.GONE);
                        pb_spotter.setVisibility(View.VISIBLE);
                        pb_spotter.setProgress(prog);
                        pb_scout.setVisibility(View.GONE);
                        pb_spy.setVisibility(View.GONE);
                        pb_chronicler.setVisibility(View.GONE);
                        pb_maven.setVisibility(View.GONE);
                        pb_sentinel.setVisibility(View.GONE);
                        break;
                    case SCOUT:
                        profileActivityBG.setBackgroundResource(R.color.rank_scout);
                        profileImageBG.setBackgroundResource(R.drawable.profile_avatar_scout_bg);
                        pb_scout.setProgress(prog);
                        pb_nayslayer.setVisibility(View.GONE);
                        pb_watcher.setVisibility(View.GONE);
                        pb_spotter.setVisibility(View.GONE);
                        pb_scout.setVisibility(View.VISIBLE);
                        pb_spy.setVisibility(View.GONE);
                        pb_chronicler.setVisibility(View.GONE);
                        pb_maven.setVisibility(View.GONE);
                        pb_sentinel.setVisibility(View.GONE);
                        break;
                    case SPY:
                        profileActivityBG.setBackgroundResource(R.color.rank_spy);
                        profileImageBG.setBackgroundResource(R.drawable.profile_avatar_spy_bg);
                        pb_spy.setProgress(prog);
                        pb_nayslayer.setVisibility(View.GONE);
                        pb_watcher.setVisibility(View.GONE);
                        pb_spotter.setVisibility(View.GONE);
                        pb_scout.setVisibility(View.GONE);
                        pb_spy.setVisibility(View.VISIBLE);
                        pb_chronicler.setVisibility(View.GONE);
                        pb_maven.setVisibility(View.GONE);
                        pb_sentinel.setVisibility(View.GONE);
                        break;
                    case CHRONICLER:
                        profileActivityBG.setBackgroundResource(R.color.rank_chronicler);
                        profileImageBG.setBackgroundResource(R.drawable.profile_avatar_chronicler_bg);
                        pb_chronicler.setProgress(prog);
                        pb_nayslayer.setVisibility(View.GONE);
                        pb_watcher.setVisibility(View.GONE);
                        pb_spotter.setVisibility(View.GONE);
                        pb_scout.setVisibility(View.GONE);
                        pb_spy.setVisibility(View.GONE);
                        pb_chronicler.setVisibility(View.VISIBLE);
                        pb_maven.setVisibility(View.GONE);
                        pb_sentinel.setVisibility(View.GONE);
                        break;
                    case MAVEN:
                        profileActivityBG.setBackgroundResource(R.color.rank_maven);
                        profileImageBG.setBackgroundResource(R.drawable.profile_avatar_maven_bg);
                        pb_maven.setProgress(prog);
                        pb_nayslayer.setVisibility(View.GONE);
                        pb_watcher.setVisibility(View.GONE);
                        pb_spotter.setVisibility(View.GONE);
                        pb_scout.setVisibility(View.GONE);
                        pb_spy.setVisibility(View.GONE);
                        pb_chronicler.setVisibility(View.GONE);
                        pb_maven.setVisibility(View.VISIBLE);
                        pb_sentinel.setVisibility(View.GONE);
                        break;
                    case SENTINEL:
                        profileActivityBG.setBackgroundResource(R.color.rank_sentinel);
                        profileImageBG.setBackgroundResource(R.drawable.profile_avatar_sentinel);
                        pb_sentinel.setProgress(prog);
                        pb_nayslayer.setVisibility(View.GONE);
                        pb_watcher.setVisibility(View.GONE);
                        pb_spotter.setVisibility(View.GONE);
                        pb_scout.setVisibility(View.GONE);
                        pb_spy.setVisibility(View.GONE);
                        pb_chronicler.setVisibility(View.GONE);
                        pb_maven.setVisibility(View.GONE);
                        pb_sentinel.setVisibility(View.VISIBLE);
                        break;
                }
                Log.d("tag", dataSnapshot.child("avatar").getValue().toString());
                final int avatar_num = Integer.parseInt(dataSnapshot.child("avatar").getValue().toString());
                Log.d("tag", "long: " + avatar_num);
                Picasso.with(context).load(Avatar.get(avatar_num)
                        .url).networkPolicy(NetworkPolicy.OFFLINE).resize(target_width, target_height).centerInside().into(avatar, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(Avatar.get(avatar_num)
                                .url).resize(target_width, target_height).centerInside().into(avatar);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void editUser(final User user){

        final Map<String, Object> newUser = new HashMap<>();

        newUser.put("displayName", user.displayName);
        newUser.put("avatar", user.avatar);

        root.child("user").child(user.id).updateChildren(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Successfully edited profile.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void follow(final String myID, final String otherID) {
        root.child("user").child(otherID).child("follower").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Boolean>> type = new GenericTypeIndicator<Map<String, Boolean>>() {
                };

                if (dataSnapshot != null) {
                    Map<String, Boolean> followers = dataSnapshot.getValue(type);

                    // follow
                    if (followers == null || !followers.keySet().contains(myID)) {
                        root.child("user").child(myID).child("following").child(otherID).setValue(true);
                        root.child("user").child(otherID).child("follower").child(myID).setValue(true);
                    } else { // unfollow
                        root.child("user").child(myID).child("following").child(otherID).setValue(null);
                        root.child("user").child(otherID).child("follower").child(myID).setValue(null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class AvatarCallback implements Callback {
        Context context;
        ImageView mImg;
        Avatar avatar;

        public AvatarCallback(Context context, ImageView mImg, Avatar avatar) {
            this.context = context;
            this.mImg = mImg;
            this.avatar = avatar;
        }

        @Override
        public void onSuccess() {

        }

        @Override
        public void onError() {
            Picasso.with(this.context).load(avatar.url).resize(target_width, target_height).centerInside().into(mImg);
        }
    }

    public void linkUserSetup() {

        ImageView a0 = (ImageView) ((Activity) context).findViewById(R.id.avatar_0);
        ImageView a1 = (ImageView) ((Activity) context).findViewById(R.id.avatar_1);
        ImageView a2 = (ImageView) ((Activity) context).findViewById(R.id.avatar_2);
        ImageView a3 = (ImageView) ((Activity) context).findViewById(R.id.avatar_3);
        ImageView a4 = (ImageView) ((Activity) context).findViewById(R.id.avatar_4);
        ImageView a5 = (ImageView) ((Activity) context).findViewById(R.id.avatar_5);
        ImageView a6 = (ImageView) ((Activity) context).findViewById(R.id.avatar_6);
        ImageView a7 = (ImageView) ((Activity) context).findViewById(R.id.avatar_7);

        Picasso.with(context).load(Avatar.A0.url).networkPolicy(NetworkPolicy.OFFLINE)
                .resize(target_width, target_height).centerInside().into(a0, new AvatarCallback(context, a0, Avatar.A0));
        Picasso.with(context).load(Avatar.A1.url).networkPolicy(NetworkPolicy.OFFLINE)
                .resize(target_width, target_height).centerInside().into(a1, new AvatarCallback(context, a1, Avatar.A1));
        Picasso.with(context).load(Avatar.A2.url).networkPolicy(NetworkPolicy.OFFLINE)
                .resize(target_width, target_height).centerInside().into(a2, new AvatarCallback(context, a2, Avatar.A2));
        Picasso.with(context).load(Avatar.A3.url).networkPolicy(NetworkPolicy.OFFLINE)
                .resize(target_width, target_height).centerInside().into(a3, new AvatarCallback(context, a3, Avatar.A3));
        Picasso.with(context).load(Avatar.A4.url).networkPolicy(NetworkPolicy.OFFLINE)
                .resize(target_width, target_height).centerInside().into(a4, new AvatarCallback(context, a4, Avatar.A4));
        Picasso.with(context).load(Avatar.A5.url).networkPolicy(NetworkPolicy.OFFLINE)
                .resize(target_width, target_height).centerInside().into(a5, new AvatarCallback(context, a5, Avatar.A5));
        Picasso.with(context).load(Avatar.A6.url).networkPolicy(NetworkPolicy.OFFLINE)
                .resize(target_width, target_height).centerInside().into(a6, new AvatarCallback(context, a6, Avatar.A6));
        Picasso.with(context).load(Avatar.A7.url).networkPolicy(NetworkPolicy.OFFLINE)
                .resize(target_width, target_height).centerInside().into(a7, new AvatarCallback(context, a7, Avatar.A7));
    }

    public void linkWatchPost(String postID) {
        ValueEventListener listener = new ValueEventListener() {

            TextView title = (TextView) ((Activity) context).findViewById(R.id.wp_tv_title);
            TextView description = (TextView) ((Activity) context).findViewById(R.id.wp_tv_description);
            TextView dateTime = (TextView) ((Activity) context).findViewById(R.id.wp_tv_date_and_time);
            TextView location = (TextView) ((Activity) context).findViewById(R.id.wp_tv_location);
            TextView secondaryLocation = (TextView) ((Activity) context).findViewById(R.id.wp_tv_secondary_location);

            ImageView image = (ImageView) ((Activity) context).findViewById(R.id.wp_iv_image);

            TextView points = (TextView) ((Activity) context).findViewById(R.id.wp_tv_rating);
            TextView mood = (TextView) ((Activity) context).findViewById(R.id.wp_tv_mood);
            EditTag tag = (EditTag) ((Activity) context).findViewById(R.id.wp_tv_tags);

            TextView name = (TextView) ((Activity) context).findViewById(R.id.wp_tv_name);
            ImageView avatarImage = (ImageView) ((Activity) context).findViewById(R.id.wp_iv_avatar);
            FrameLayout rankFrame = (FrameLayout) ((Activity) context).findViewById(R.id.wp_iv_avatar_frame);

            ImageView iv_down = (ImageView) ((Activity) context).findViewById(R.id.wp_iv_downvote);
            ImageView iv_up = (ImageView) ((Activity) context).findViewById(R.id.wp_iv_upvote);

            private void setVoteStatus(int status) {
                switch (status) {
                    case -1:
                        iv_down.setImageDrawable(context.getResources().getDrawable(R.drawable.downvote_active));
                        iv_up.setImageDrawable(context.getResources().getDrawable(R.drawable.upvote));
                        break;
                    case 0:
                        iv_down.setImageDrawable(context.getResources().getDrawable(R.drawable.downvote));
                        iv_up.setImageDrawable(context.getResources().getDrawable(R.drawable.upvote));
                        break;
                    case 1:
                        iv_down.setImageDrawable(context.getResources().getDrawable(R.drawable.downvote));
                        iv_up.setImageDrawable(context.getResources().getDrawable(R.drawable.upvote_active));
                        break;
                    default:
                }
            }

            public void setVoteListeners(final Post post) {
                final String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
                iv_up.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (post.upvote_user.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            post.upvote_user.remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            setVoteStatus(0);
                        }else{
                            post.upvote_user.put(FirebaseAuth.getInstance().getCurrentUser().getUid(),true);
                            setVoteStatus(1);
                        }
                        votePost(post.postID, FirebaseAuth.getInstance().getCurrentUser().getUid(), 1);


                    }
                });
                iv_down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (post.downvote_user.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            post.downvote_user.remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            setVoteStatus(0);
                        }else{
                            post.downvote_user.put(FirebaseAuth.getInstance().getCurrentUser().getUid(),true);
                            setVoteStatus(-1);
                        }
                        votePost(post.postID, FirebaseAuth.getInstance().getCurrentUser().getUid(), -1);

                    }
                });

            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*final TextView numRating = */
                //String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Post post = dataSnapshot.getValue(Post.class);
                if (dataSnapshot != null && post != null) {
//                    ((TextView) ((Activity) context).findViewById(R.id.tv_number_of_rating))
//                            .setText(Integer.parseInt(dataSnapshot.child("rating").getValue().toString()) + " ");
//                    ((TextView) ((Activity) context).findViewById(R.id.tv_number_of_post))
//                            .setText(Integer.parseInt(dataSnapshot.child("post_count").getValue().toString()) + " ");
                    title.setText(post.title);
                    description.setText(post.description);
                    dateTime.setText(dateAndTime(post.timestamp));
                    location.setText(post.placePrimary);
                    secondaryLocation.setText(post.placeSecondary);

                    points.setText(String.valueOf(post.upvote - post.downvote));
                    mood.setText(post.mood);

                    setImage(post.url);
                    setTags(post.tags);
                    int voteStatus;
                    if (post.upvote_user.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        voteStatus = 1;
                    else if (post.downvote_user.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        voteStatus = -1;
                    else
                        voteStatus = 0;
                    setVoteStatus(voteStatus);
                    setVoteListeners(post);

                    root.child("user").child(post.userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            if (dataSnapshot != null && user != null) {
                                name.setText(user.displayName);
                                setAvatar(Avatar.get(user.avatar));
                                setRankFrame(Rank.get(user.rank));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                        public void setAvatar(Avatar avatar) {
                            Picasso.with(context).load(avatar.url).networkPolicy(NetworkPolicy.OFFLINE)
                                    .resize(100, 90).centerCrop().into(avatarImage,
                                    new ImageCallback(context, avatarImage, avatar.url, 100, 90));
                        }

                        public void setRankFrame(Rank rank) {
                            switch (rank) {
                                case NAYSAYER:
                                    rankFrame.setBackgroundResource(R.drawable.profile_avatar_nayslayer_bg);
                                    break;
                                case WATCHER:
                                    rankFrame.setBackgroundResource(R.drawable.profile_avatar_watcher_bg);
                                    break;
                                case SPOTTER:
                                    rankFrame.setBackgroundResource(R.drawable.profile_avatar_spotter_bg);
                                    break;
                                case SCOUT:
                                    rankFrame.setBackgroundResource(R.drawable.profile_avatar_scout_bg);
                                    break;
                                case SPY:
                                    rankFrame.setBackgroundResource(R.drawable.profile_avatar_spy_bg);
                                    break;
                                case CHRONICLER:
                                    rankFrame.setBackgroundResource(R.drawable.profile_avatar_chronicler_bg);
                                    break;
                                case MAVEN:
                                    rankFrame.setBackgroundResource(R.drawable.profile_avatar_maven_bg);
                                    break;
                                case SENTINEL:
                                    rankFrame.setBackgroundResource(R.drawable.profile_avatar_sentinel);
                                    break;
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

            private String dateAndTime(long timestamp) {
                Calendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(timestamp);

                NumberFormat formatter = new DecimalFormat("00");

                return getMonth(calendar.get(Calendar.MONTH)) + " "
                        + calendar.get(Calendar.DATE) + ", "
                        + formatter.format(calendar.get(Calendar.HOUR)) + ":"
                        + formatter.format(calendar.get(Calendar.MINUTE));
                //+ calendar.get(Calendar.AM_PM);
            }

            private String getMonth(int month) {
                switch (month) {
                    case Calendar.JANUARY:
                        return "January";
                    case Calendar.FEBRUARY:
                        return "February";
                    case Calendar.MARCH:
                        return "March";
                    case Calendar.APRIL:
                        return "April";
                    case Calendar.MAY:
                        return "May";
                    case Calendar.JUNE:
                        return "June";
                    case Calendar.JULY:
                        return "July";
                    case Calendar.AUGUST:
                        return "August";
                    case Calendar.SEPTEMBER:
                        return "September";
                    case Calendar.OCTOBER:
                        return "October";
                    case Calendar.NOVEMBER:
                        return "November";
                    case Calendar.DECEMBER:
                        return "December";
                    default:
                        return null;
                }
            }

            private void setImage(String url) {
                if (url == null || TextUtils.isEmpty(url))
                    image.setVisibility(View.GONE);
                else
                    Picasso.with(context).load(url).networkPolicy(NetworkPolicy.OFFLINE)
                            .into((image), new ImageCallback(context,image, url, 100, 90));
            }

            public void setTags(Map<String, Boolean> tags) {
                ArrayList<String> tagList = new ArrayList<>();

                if (tags != null) {
                    tag.setTagList(new ArrayList<String>(0));
                    for (String tag : tags.keySet()) {
                        tagList.add(tag);
                    }
                    tag.setTagList(tagList);
                    tag.setEditable(false);


                } else {
                    tag.setVisibility(View.GONE);
                    tag.setTagList(new ArrayList<String>(0));
                }
            }
        };

        root.child("post").child(postID).addListenerForSingleValueEvent(listener);

        //updateUser(userID);
    }

    public ValueEventListener linkUserPage(String userID) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*final TextView numRating = */
                String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                User user = dataSnapshot.getValue(User.class);
                if (dataSnapshot != null && user != null) {
//                    ((TextView) ((Activity) context).findViewById(R.id.tv_number_of_rating))
//                            .setText(Integer.parseInt(dataSnapshot.child("rating").getValue().toString()) + " ");
//                    ((TextView) ((Activity) context).findViewById(R.id.tv_number_of_post))
//                            .setText(Integer.parseInt(dataSnapshot.child("post_count").getValue().toString()) + " ");
                    ((TextView) ((Activity) context).findViewById(R.id.tv_number_of_rating))
                            .setText(user.rating + " ");
                    ((TextView) ((Activity) context).findViewById(R.id.tv_number_of_post))
                            .setText(user.post_count + " ");
                    updateProfileSkin(dataSnapshot);

                    if (user.follower != null) {
                        ((TextView) ((Activity) context).findViewById(R.id.tv_number_of_followers))
                                .setText(user.follower.size() + " ");
                    }

                    if (!dataSnapshot.getKey().equals(myID)) {
                        if (user.follower == null || !user.follower.keySet().contains(myID)) {
                            ((ImageView) ((Activity) context).findViewById(R.id.follow_btn))
                                    .setImageResource(R.drawable.follow);
                        } else {
                            ((ImageView) ((Activity) context).findViewById(R.id.follow_btn))
                                    .setImageResource(R.drawable.unfollow);
                        }
                    } else {
                        ((ImageView) ((Activity) context).findViewById(R.id.follow_btn))
                                .setImageResource(R.drawable.editprofile);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        root.child("user").child(userID).addValueEventListener(listener);

        updateUser(userID);
        return listener;
    }

    public void unlinkUserPage(String userID, ValueEventListener vel) {
        root.child("user").child(userID).removeEventListener(vel);
    }

    public void updateProfileSkin(DataSnapshot dataSnapshot) {

        final TextView rank = (TextView) ((Activity) context).findViewById(R.id.tv_profile_rank);
        final RelativeLayout profileActivityBG = (RelativeLayout) ((Activity) context).findViewById(R.id.activity_profile);
        final FrameLayout profileImageBG = (FrameLayout) ((Activity) context).findViewById(R.id.profile_image_bg);
        Rank temp = Rank.get(Integer.parseInt(dataSnapshot.child("rank").getValue().toString()));
        rank.setText(temp.text);
        switch (temp) {
            case NAYSAYER:
                profileActivityBG.setBackgroundResource(R.color.rank_naysayer);
                profileImageBG.setBackgroundResource(R.drawable.profile_avatar_nayslayer_bg);

                break;
            case WATCHER:
                profileActivityBG.setBackgroundResource(R.color.rank_watcher);
                profileImageBG.setBackgroundResource(R.drawable.profile_avatar_watcher_bg);

                break;
            case SPOTTER:
                profileActivityBG.setBackgroundResource(R.color.rank_spotter);
                profileImageBG.setBackgroundResource(R.drawable.profile_avatar_spotter_bg);

                break;
            case SCOUT:
                profileActivityBG.setBackgroundResource(R.color.rank_scout);
                profileImageBG.setBackgroundResource(R.drawable.profile_avatar_scout_bg);

                break;
            case SPY:
                profileActivityBG.setBackgroundResource(R.color.rank_spy);
                profileImageBG.setBackgroundResource(R.drawable.profile_avatar_spy_bg);

                break;
            case CHRONICLER:
                profileActivityBG.setBackgroundResource(R.color.rank_chronicler);
                profileImageBG.setBackgroundResource(R.drawable.profile_avatar_chronicler_bg);

                break;
            case MAVEN:
                profileActivityBG.setBackgroundResource(R.color.rank_maven);
                profileImageBG.setBackgroundResource(R.drawable.profile_avatar_maven_bg);

                break;
            case SENTINEL:
                profileActivityBG.setBackgroundResource(R.color.rank_sentinel);
                profileImageBG.setBackgroundResource(R.drawable.profile_avatar_sentinel);

                break;
        }
    }
}
