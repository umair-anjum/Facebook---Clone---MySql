package com.example.smalldots.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.smalldots.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ViewImage extends AppCompatActivity {

    PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        final String imageURL = getIntent().getStringExtra("imageURL");
        photoView = findViewById(R.id.full_image_id);

    if(!imageURL.isEmpty()){
        Picasso.with(ViewImage.this).load(imageURL).networkPolicy(NetworkPolicy.OFFLINE)
                .into(photoView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ViewImage.this).load(imageURL).into(photoView);
                    }
                });
    }
    }
}
