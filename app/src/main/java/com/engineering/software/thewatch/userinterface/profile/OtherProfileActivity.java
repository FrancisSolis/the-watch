package com.engineering.software.thewatch.userinterface.profile;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.util.Timekeeper;
import com.engineering.software.thewatch.feed.adapter.PostAdapter;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.engineering.software.thewatch.util.InterfaceManager;
import com.google.firebase.auth.FirebaseAuth;

public class OtherProfileActivity extends AppCompatActivity {

    DatabaseManager dm;
    InterfaceManager im;
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        dm = new DatabaseManager(this);

        ImageView iv = (ImageView) findViewById(R.id.follow_btn);
        iv.setVisibility(View.VISIBLE);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dm.follow(
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        getIntent().getExtras().getString("userID")
                );
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        im = new InterfaceManager();
        dm = new DatabaseManager(this);
        dm.linkUserPage(getIntent().getExtras().getString("userID"));

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.scrollToPositionWithOffset(0, 0);

        rv = (RecyclerView) findViewById(R.id.rv_profile);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(llm);

        PostAdapter adapter = im.userPostAdapter(getIntent().getExtras().getString("userID"), Timekeeper.NONE);

        rv.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
