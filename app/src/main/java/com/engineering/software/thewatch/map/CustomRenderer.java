package com.engineering.software.thewatch.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.model.feed.PostItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;


/**
 * Created by JKMT on 03/06/2017.
 */

public class CustomRenderer extends DefaultClusterRenderer<PostItem> {
    Context context;
    static Bitmap b = null;
    public CustomRenderer(Context context, GoogleMap map, ClusterManager<PostItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        if(b == null){
            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.blip), 80, 90, false);
        }

    }

    @Override
    protected void onClusterItemRendered(PostItem clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
        try {

            marker.setIcon(BitmapDescriptorFactory.fromBitmap(b));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOnClusterItemInfoWindowClickListener(ClusterManager.OnClusterItemInfoWindowClickListener<PostItem> listener) {
        super.setOnClusterItemInfoWindowClickListener(listener);
    }
}
