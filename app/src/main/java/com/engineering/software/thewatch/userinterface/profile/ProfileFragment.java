package com.engineering.software.thewatch.userinterface.profile;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.feed.adapter.PostAdapter;
import com.engineering.software.thewatch.util.Timekeeper;
import com.engineering.software.thewatch.model.db.User;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.engineering.software.thewatch.util.InterfaceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by JKMT on 02/03/2017.
 */

public class ProfileFragment extends Fragment {

    DatabaseManager dm;
    InterfaceManager im;
    RecyclerView rv;

    ValueEventListener myListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_profile, container, false);
        ImageView iv = (ImageView) v.findViewById(R.id.follow_btn);
        dm = new DatabaseManager(getContext());
        iv.setVisibility(View.VISIBLE);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dm.getRoot().child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {

                            Intent i = new Intent(getContext(), EditProfileActivity.class);
                            i.putExtra("displayName", user.displayName);
                            i.putExtra("avatar", user.avatar);
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        View mView = getView();

        im = new InterfaceManager();
        dm = new DatabaseManager(getView().getContext());
        myListener = dm.linkUserPage(FirebaseAuth.getInstance().getCurrentUser().getUid());

        LinearLayoutManager llm = new LinearLayoutManager(mView.getContext());
        llm.scrollToPositionWithOffset(0, 0);

        rv = (RecyclerView) mView.findViewById(R.id.rv_profile);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(llm);

        PostAdapter adapter = im.userPostAdapter(FirebaseAuth.getInstance().getCurrentUser().getUid(), Timekeeper.NONE);

        rv.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        dm.unlinkUserPage(FirebaseAuth.getInstance().getCurrentUser().getUid(), myListener);
    }

    @Override
    public void onDestroy() {
        if (dm != null) {
            dm.unlinkUserPage(FirebaseAuth.getInstance().getCurrentUser().getUid(), myListener);
        }
        super.onDestroy();
    }
}
