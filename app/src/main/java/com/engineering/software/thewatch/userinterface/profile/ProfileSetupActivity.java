package com.engineering.software.thewatch.userinterface.profile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.model.db.Avatar;
import com.engineering.software.thewatch.model.db.User;
import com.engineering.software.thewatch.userinterface.MainActivity;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileSetupActivity extends AppCompatActivity {

    FirebaseUser mCurrentUser;
    DatabaseManager dm;

    FrameLayout prevSelected;
    ImageView prevImage;
    EditText mDisplayName;

    int current_selected = 8;

    private int[] avatarID = {
            R.id.avatar_0,
            R.id.avatar_1,
            R.id.avatar_2,
            R.id.avatar_3,
            R.id.avatar_4,
            R.id.avatar_5,
            R.id.avatar_6,
            R.id.avatar_7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        mDisplayName = (EditText) findViewById(R.id.txtDisplayName);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        prevSelected = null;
        prevImage = null;
        mDisplayName.setText(mCurrentUser.getDisplayName().toString());
        dm = new DatabaseManager(this);
        try {
            dm.linkUserSetup();
        } catch (Exception e) {
            Log.d("tag", e.getMessage());
        }

    }

    public void selectAvatar(View v) {
        if (prevSelected == null) {
            prevSelected = (FrameLayout) ((ViewGroup) v).getChildAt(1);
            prevImage = (ImageView) ((ViewGroup) v).getChildAt(0);
        }
        prevSelected.setVisibility(View.INVISIBLE);
        prevImage.setAlpha(1.0f);
        ((FrameLayout) ((ViewGroup) v).getChildAt(1)).setVisibility(View.VISIBLE);
        ((ImageView) ((ViewGroup) v).getChildAt(0)).setAlpha(0.5f);
        current_selected = Integer.parseInt(v.getTag().toString());
        prevSelected = (FrameLayout) ((ViewGroup) v).getChildAt(1);
        prevImage = (ImageView) ((ViewGroup) v).getChildAt(0);

    }

    public void complete(View view) {
        if (!TextUtils.isEmpty(mDisplayName.getText())) {
            User user = new User(mCurrentUser.getUid(), mDisplayName.getText().toString(), mCurrentUser.getEmail(), Avatar.get(current_selected));
            dm.createUser(user);
            startActivity(new Intent(this, MainActivity.class));
        } else {
            mDisplayName.setError("This field is required!");
        }

    }

    public void skip(View view) {
        if (!TextUtils.isEmpty(mDisplayName.getText())) {
            User user = new User(mCurrentUser.getUid(), mCurrentUser.getDisplayName(), mCurrentUser.getEmail(), Avatar.get(8));
            dm.createUser(user);
            startActivity(new Intent(this, MainActivity.class));
        } else {
            mDisplayName.setError("This field is required!");
        }
    }
}
