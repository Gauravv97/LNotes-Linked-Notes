package com.links.gaurav.lnotes;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.support.v7.widget.Toolbar.*;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Created by Gaurav on 7/19/2017.
 */

public class FullscreenImage extends AppCompatActivity{
    String path;
    SubsamplingScaleImageView imgDisplay;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_image);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                path = null;
            } else {
                path = extras.getString("path");
            }
        } else {
            path = (String) savedInstanceState.getSerializable("path");
        }

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.VISIBLE);
        imgDisplay = (SubsamplingScaleImageView) findViewById(R.id.imgDisplay);//else write v.find....
        //PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        //Glide.with(this).load(path).into(imgDisplay);
        imgDisplay.setImage(ImageSource.uri(path));
        imgDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle_bar();
            }
        });
        
    }
    void toggle_bar(){
        if(toolbar.getVisibility()==View.VISIBLE)
            toolbar.setVisibility(View.GONE);
        else toolbar.setVisibility(View.VISIBLE);
    }
}
