package com.engineering.software.thewatch.feed.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.engineering.software.thewatch.util.ImageCallback;
import com.engineering.software.thewatch.userinterface.map.MapPostActivity;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.engineering.software.thewatch.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import static android.view.View.GONE;

/**
 * Author: King
 * Date: 3/16/2017
 */

public class ExploreViewHolder
        extends RecyclerView.ViewHolder {

    private View mItemView;

    private TextView mTitle;
    private TextView mLocation;
    private ImageView mImage;

    public ExploreViewHolder(View itemView) {
        super(itemView);

        mItemView = itemView;

        mTitle = ((TextView) mItemView.findViewById(R.id.ex_tv_title));
        mLocation = ((TextView) mItemView.findViewById(R.id.ex_tv_location));
        mImage = ((ImageView) mItemView.findViewById(R.id.ex_iv_image));
    }

    public void setView(PostInformation postInformation) {
        mTitle.setText(postInformation.post.title);
        mLocation.setText(postInformation.post.placePrimary);

        if (postInformation.post.url == null || TextUtils.isEmpty(postInformation.post.url))
            mImage.setVisibility(GONE);
        else {
            Picasso.with(mItemView.getContext()).load(postInformation.post.url)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mImage, new ImageCallback(mItemView.getContext(), mImage,
                            postInformation.post.url, 0, 0));
        }
    }

    public void setViewListener(final String postID) {
        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mItemView.getContext(), MapPostActivity.class);
                i.putExtra("postID", postID);
                ((Activity) mItemView.getContext()).startActivity(i);
            }
        });
    }
}
