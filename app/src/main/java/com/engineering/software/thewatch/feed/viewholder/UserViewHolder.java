package com.engineering.software.thewatch.feed.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.userinterface.profile.OtherProfileActivity;

/**
 * Author: King
 * Date: 3/16/2017
 */

public class UserViewHolder extends RecyclerView.ViewHolder {

    private TextView mName;
    private View mView;
    public UserViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mName = ((TextView) itemView.findViewById(R.id.tv_displayName));
    }

    public void setName(String userName) {
        mName.setText(userName);
    }

    public void setOnClick(final String id) {

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mView.getContext(),OtherProfileActivity.class);
                i.putExtra("userID",id);
                ((Activity) mView.getContext()).startActivity(i);
                ((Activity) mView.getContext()).finish();
            }
        });

    }
}
