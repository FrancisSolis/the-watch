package com.engineering.software.thewatch.feed.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.model.db.Avatar;
import com.engineering.software.thewatch.model.db.Post;
import com.engineering.software.thewatch.model.db.Rank;
import com.engineering.software.thewatch.model.db.User;
import com.engineering.software.thewatch.userinterface.profile.OtherProfileActivity;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.engineering.software.thewatch.util.ImageCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import me.originqiu.library.EditTag;

import static android.view.View.GONE;

/**
 * Author: King
 * Date: 2/8/2017
 */
public class PostViewHolder extends RecyclerView.ViewHolder {

    View mItemView;
    ImageView iv_down;
    ImageView iv_up;
    int voteStatus;
    DatabaseManager db;


    public PostViewHolder(View itemView) {
        super(itemView);
        mItemView = itemView;
        iv_down = (ImageView) mItemView.findViewById(R.id.iv_downvote);
        iv_up = (ImageView) mItemView.findViewById(R.id.iv_upvote);
        db = new DatabaseManager(itemView.getContext());

    }

    public void setPostData(Post post) {
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setTitle(post.title);
        setDescription(post.description);
        setLocation(post.placePrimary, post.placeSecondary);
        setMood(post.mood);
        setTags(post.tags);
        setTime(dateAndTime(post.timestamp));
        setImage(post.url);
        setPoints(String.valueOf(post.upvote - post.downvote));

        if (post.upvote_user.containsKey(user))
            voteStatus = 1;
        else if (post.downvote_user.containsKey(user))
            voteStatus = -1;
        else
            voteStatus = 0;

        setVoteStatus(voteStatus);
    }

    public void setUserData(User user) {
        setAvatar(Avatar.get(user.avatar));
        setDisplayName(user.displayName);
        setRank(Rank.get(user.rank));
    }

    public void setImage(String url) {
        if (url == null || TextUtils.isEmpty(url))
            ((ImageView) mItemView.findViewById(R.id.iv_image)).setVisibility(GONE);
        else {
            Picasso.with(mItemView.getContext()).load(url)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(((ImageView) mItemView.findViewById(R.id.iv_image)),
                            new ImageCallback(mItemView.getContext(), (ImageView) mItemView.findViewById(R.id.iv_image), url, 0, 0));
        }
    }

    public void setTitle(String title) {
        ((TextView) mItemView.findViewById(R.id.tv_title)).setText(title);
    }

    public void setDescription(String description) {
        ((TextView) mItemView.findViewById(R.id.tv_description)).setText(description);
    }

    public void setMood(String mood) {
        ((TextView) mItemView.findViewById(R.id.tv_mood)).setText(mood);
        // set emoji here
    }

    public void setTags(Map<String, Boolean> tags) {
        ArrayList<String> tagList = new ArrayList<>();

        if (tags != null) {
            ((EditTag) mItemView.findViewById(R.id.tv_tags)).setTagList(new ArrayList<String>(0));
            for (String tag : tags.keySet()) {
                tagList.add(tag);
            }
            ((EditTag) mItemView.findViewById(R.id.tv_tags)).setTagList(tagList);
            ((EditTag) mItemView.findViewById(R.id.tv_tags)).setEditable(false);


        } else {
            (mItemView.findViewById(R.id.iv_tags)).setVisibility(GONE);
            (mItemView.findViewById(R.id.tv_tags)).setVisibility(GONE);
            ((EditTag) mItemView.findViewById(R.id.tv_tags)).setTagList(new ArrayList<String>(0));
        }
    }

    public void setTime(String date) {
        ((TextView) mItemView.findViewById(R.id.tv_date_and_time)).setText(date);
    }

    public void setPoints(String rating) {
        ((TextView) mItemView.findViewById(R.id.tv_rating)).setText(rating);
    }

    public void setLocation(String primary, String secondary) {
        ((TextView) mItemView.findViewById(R.id.tv_location)).setText(primary);
        ((TextView) mItemView.findViewById(R.id.tv_secondary_location)).setText(secondary);
    }

    public void setDisplayName(String displayName) {
        ((TextView) mItemView.findViewById(R.id.tv_name)).setText(displayName);
    }

    public void setAvatar(Avatar avatar) {
        ImageView iv_avatar = (ImageView) mItemView.findViewById(R.id.iv_avatar);

        Picasso.with(mItemView.getContext()).load(avatar.url)
                .networkPolicy(NetworkPolicy.OFFLINE).resize(100, 90).centerCrop()
                .into(((ImageView) mItemView.findViewById(R.id.iv_avatar)),
                        new ImageCallback(mItemView.getContext(), iv_avatar, avatar.url, 100, 90));
    }

    public void setRank(Rank rank) {
        final FrameLayout rankFrame = (FrameLayout) mItemView.findViewById(R.id.iv_avatar_frame);
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

    /**
     * @param status -1 : downvote
     *               0 : no votes
     *               1 : upvote
     */
    private void setVoteStatus(int status) {
        switch (status) {
            case -1:
                iv_down.setImageDrawable(mItemView.getResources().getDrawable(R.drawable.downvote_active));
                iv_up.setImageDrawable(mItemView.getResources().getDrawable(R.drawable.upvote));
                break;
            case 0:
                iv_down.setImageDrawable(mItemView.getResources().getDrawable(R.drawable.downvote));
                iv_up.setImageDrawable(mItemView.getResources().getDrawable(R.drawable.upvote));
                break;
            case 1:
                iv_down.setImageDrawable(mItemView.getResources().getDrawable(R.drawable.downvote));
                iv_up.setImageDrawable(mItemView.getResources().getDrawable(R.drawable.upvote_active));
                break;
            default:
        }
    }

    public void setVoteListeners(final String postID) {
        final String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        iv_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.votePost(postID, user, 1);
            }
        });
        iv_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.votePost(postID, user, -1);
            }
        });
    }

    public void setProfileListener(final String id) {
        ImageView iv = (ImageView) mItemView.findViewById(R.id.iv_avatar);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    Intent i = new Intent(mItemView.getContext(), OtherProfileActivity.class);
                    i.putExtra("userID", id);
                    ((Activity) mItemView.getContext()).startActivity(i);
                }
            }
        });
    }
}
