package com.engineering.software.thewatch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.engineering.software.thewatch.model.db.Mood;
import com.engineering.software.thewatch.model.db.Post;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.engineering.software.thewatch.util.PlaceAutocompleteAdapter;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import id.zelory.compressor.Compressor;
import me.originqiu.library.EditTag;

public class PostActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final LatLngBounds BOUNDS_PH = new LatLngBounds(
            new LatLng(4.6145711, 119.6272661), new LatLng(19.966096, 124.173694));

    private static final int RESULT_LOAD_IMG = 5;

    protected GoogleApiClient mGoogleApiClient;

    private File actualImage;
    private File compressedImage;

    private Place mPlace = null;

    private PlaceAutocompleteAdapter mAdapter;

    private ImageView compressedImageView, fromCamera, fromGallery, close;

    @Bind(R.id.moodSpinner)
    Spinner mSpinner;

    @Bind(R.id.et_location)
    AutoCompleteTextView mAutocompleteView;

    @Bind(R.id.et_tags)
    EditTag mEditTag;

    @Bind(R.id.et_title)
    EditText mTitle;

    @Bind(R.id.et_description)
    EditText mDesc;

    private Uri imageURI;

    private String mPlaceID, mPlacePrimary, mPlaceSecondary;

    private LatLng mLoc;

    ArrayList<String> moods;

    private DatabaseManager mDatabaseManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);
        mEditTag = (EditTag) findViewById(R.id.et_tags);

        moods = new ArrayList<>();

        mDatabaseManager = new DatabaseManager(this);

        //Image Stuff
        compressedImageView = (ImageView) findViewById(R.id.thumbnail);
        fromCamera = (ImageView) findViewById(R.id.img_take_a_photo);
        fromGallery = (ImageView) findViewById(R.id.img_choose_a_photo);
        close = (ImageView) findViewById(R.id.retake);


        //Setup mood spinner
        for (Mood m : Mood.values()) {
            moods.add(m.text);
        }
        //mSpinner = (Spinner) findViewById(R.id.moodSpinner);
        ArrayAdapter<String> spinnerObjects = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, moods);
        spinnerObjects.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mSpinner.setAdapter(spinnerObjects);


        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        mAutocompleteView = (AutoCompleteTextView) findViewById(R.id.et_location);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_PH,
                null);
        mAutocompleteView.setAdapter(mAdapter);

        getCurrentPlace();

        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            try {
                String filename = (String) b.get("filename");
                File f = new File(filename);
                actualImage = f;
                if (actualImage == null) {
                    showError("Please choose an image!");
                } else {
                    // Compress image in main thread using custom Compressor
                    setCompressedImage();
                }
            } catch (Exception e) {
                showError("Failed to read picture data!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */

            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            mPlacePrimary = item.getPrimaryText(null).toString();
            mPlaceSecondary = item.getSecondaryText(null).toString();
            mPlaceID = item.getPlaceId();

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            mPlace = places.get(0);
            mLoc = places.get(0).getLatLng();
            places.release();
        }
    };

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                final Uri imageUri = data.getData();
                setPath(imageUri);
                setCompressedImage();


            } else {
                Toast.makeText(PostActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private Place getCurrentPlace() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("myTag", "permission error");
            return null;
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                if (likelyPlaces != null && likelyPlaces.getCount() > 0) {
                    mPlace = likelyPlaces.get(0).getPlace();
                    mPlaceID = mPlace.getId();
                    mPlacePrimary = mPlace.getName().toString();
                    mPlaceSecondary = mPlace.getAddress().toString();

                    mAutocompleteView.setText(mPlace.getName());
                    mLoc = mPlace.getLatLng();
                    likelyPlaces.release();

                }

            }
        });
        return null;
    }

    public void onPostClick(View v) {
        if (checkInputs()) {
            final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(R.color.colorPrimary);
            pDialog.setTitleText("Posting");
            pDialog.setCancelable(false);
            pDialog.show();
            final Post post = new Post(FirebaseAuth.getInstance().getCurrentUser().getUid(), mTitle.getText().toString()
                    , mDesc.getText().toString(), mPlaceID, mPlacePrimary, mPlaceSecondary, Mood.getValue(mSpinner.getSelectedItemPosition())
                    , mEditTag.getTagList(), imageURI);
            final LatLng loc = mLoc;
            mDatabaseManager.uploadPost(post, new GeoLocation(loc.latitude, loc.longitude));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    pDialog.dismiss();
                    PostActivity.this.setResult(RESULT_OK);
                    finish();
                }
            }, 3000);

        }

    }

    private boolean checkInputs() {
        boolean flag = true;
        if (TextUtils.isEmpty(mTitle.getText())) {
            mTitle.setError("Please put a title");
            flag = false;
        }
        if (TextUtils.isEmpty(mDesc.getText())) {
            mTitle.setError("Please put a description");
            flag = false;
        }
        if (mPlace == null) {
            mTitle.setError("Please put a title");
            flag = false;
        }

        return flag;
    }

    private void deleteMediaFile() {
        actualImage.delete();
        compressedImage.delete();
    }

    public void retake(View v) {
        fromCamera.setVisibility(View.VISIBLE);
        fromGallery.setVisibility(View.VISIBLE);
        compressedImageView.setVisibility(View.INVISIBLE);
        close.setVisibility(View.INVISIBLE);
        deleteMediaFile();
        imageURI = null;
        compressedImage = null;
        actualImage = null;

    }

    public void takeFromGallery(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    public void takePhoto(View v) {
        Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);
        finish();
    }

    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    public void setPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(projection[0]);
        String picturePath = cursor.getString(columnIndex); // returns null
        actualImage = new File(picturePath);
        cursor.close();
    }

    private void setCompressedImage() {
        fromGallery.setVisibility(View.INVISIBLE);
        fromCamera.setVisibility(View.INVISIBLE);
        compressedImage = new Compressor.Builder(this)
                .setMaxWidth(640)
                .setMaxHeight(480)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.PNG)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .build()
                .compressToFile(actualImage);
        compressedImageView.setImageBitmap(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));

        imageURI = Uri.fromFile(compressedImage);

        compressedImageView.setVisibility(View.VISIBLE);
        close.setVisibility(View.VISIBLE);

        //Toast.makeText(this, "Compressed image save in " + compressedImage.getPath(), Toast.LENGTH_LONG).show();

        Log.d("Compressor", "Compressed image save in " + compressedImage.getPath());
    }
}



