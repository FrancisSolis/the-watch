package com.engineering.software.thewatch.userinterface.profile;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.model.db.Avatar;
import com.engineering.software.thewatch.model.db.User;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditProfileActivity extends AppCompatActivity {

    FirebaseUser mCurrentUser;
    DatabaseManager dm;

    FrameLayout prevSelected;
    ImageView prevImage;
    EditText mDisplayName;

    int current_selected = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        dm = new DatabaseManager(this);

        mDisplayName = (EditText) findViewById(R.id.txtDisplayName);
        mDisplayName.setText(getIntent().getStringExtra("displayName"));
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        prevSelected = null;
        prevImage = null;

        try {
            dm.linkUserSetup();
        } catch (Exception e) {
            Log.d("tag", e.getMessage());
        }

        initAvatar(getIntent().getIntExtra("avatar", 8));
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

    public void Cancel(View view) {
        finish();
    }

    public void selectAvatar(View v) {
        if (prevSelected == null) {
            prevSelected = (FrameLayout) ((ViewGroup) v).getChildAt(1);
            prevImage = (ImageView) ((ViewGroup) v).getChildAt(0);
        }

        prevSelected.setVisibility(View.INVISIBLE);
        prevImage.setAlpha(1.0f);

        (((ViewGroup) v).getChildAt(1)).setVisibility(View.VISIBLE);
        (((ViewGroup) v).getChildAt(0)).setAlpha(0.5f);

        current_selected = Integer.parseInt(v.getTag().toString());
        prevSelected = (FrameLayout) ((ViewGroup) v).getChildAt(1);
        prevImage = (ImageView) ((ViewGroup) v).getChildAt(0);
    }

    public void initAvatar(int avatar) {
        Log.d("tag", avatar + "   <<<< avatar");
        switch (avatar) {
            case 0:
                prevSelected = (FrameLayout) findViewById(R.id.selected_0);
                prevImage = (ImageView) findViewById(R.id.avatar_0);
                break;
            case 1:
                prevSelected = (FrameLayout) findViewById(R.id.selected_1);
                prevImage = (ImageView) findViewById(R.id.avatar_1);
                break;
            case 2:
                prevSelected = (FrameLayout) findViewById(R.id.selected_2);
                prevImage = (ImageView) findViewById(R.id.avatar_2);
                break;
            case 3:
                prevSelected = (FrameLayout) findViewById(R.id.selected_3);
                prevImage = (ImageView) findViewById(R.id.avatar_3);
                break;
            case 4:
                prevSelected = (FrameLayout) findViewById(R.id.selected_4);
                prevImage = (ImageView) findViewById(R.id.avatar_4);
                break;
            case 5:
                prevSelected = (FrameLayout) findViewById(R.id.selected_5);
                prevImage = (ImageView) findViewById(R.id.avatar_5);
                break;
            case 6:
                prevSelected = (FrameLayout) findViewById(R.id.selected_6);
                prevImage = (ImageView) findViewById(R.id.avatar_6);
                break;
            case 7:
                prevSelected = (FrameLayout) findViewById(R.id.selected_7);
                prevImage = (ImageView) findViewById(R.id.avatar_7);
                break;
            default:
        }
        prevSelected.setVisibility(View.VISIBLE);
        prevImage.setAlpha(0.5f);
    }

    public void complete(View view) {
        if (!TextUtils.isEmpty(mDisplayName.getText())) {
            User user = new User(mCurrentUser.getUid(), mDisplayName.getText().toString(),
                    mCurrentUser.getEmail(), Avatar.get(current_selected));
            dm.editUser(user);

            finish();
        } else {
            mDisplayName.setError("This field is required!");
        }

    }
}
