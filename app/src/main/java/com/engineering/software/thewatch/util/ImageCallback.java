package com.engineering.software.thewatch.util;
import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Author: King
 * Date: 2/15/2017
 */

public class ImageCallback implements Callback {

    private Context context;
    private ImageView imageView;
    private String url;
    private int width;
    private int height;

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {
        if(width != 0)
            Picasso.with(context).load(url).resize(width, height).centerCrop().into((imageView));
        else
            Picasso.with(context).load(url).into((imageView));
    }

    public ImageCallback(Context context, ImageView imageView, String url, int width, int height) {
        this.context = context;
        this.imageView = imageView;
        this.url = url;
        this.width = width;
        this.height = height;
    }
}
