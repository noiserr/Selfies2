package pl.droidsonroids.letmetakeaselfie.loader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import pl.droidsonroids.letmetakeaselfie.R;

public class EmptyImageLoader implements ImageLoader {

    @Override
    public void loadImage(@NonNull final ImageView imageView, @NonNull final String imageUrl) {

        Glide.with(imageView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_face_black_36dp)
                .fitCenter()
                .centerCrop()
                .into(imageView);


    }
}
