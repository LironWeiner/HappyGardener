package com.example.weinner.liron.happygardener;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;

/**
 * Created by Liron Weinner
 */

class DownloadImage{
    private final StorageReference storageRef;
    private final WeakReference<ImageButton> view;
    private final WeakReference<ProgressBar> progressBar;
    private final WeakReference<Context> context;

    public DownloadImage(StorageReference storageRef , ImageButton view , ProgressBar progressBar , final Context context) {
        this.storageRef = storageRef;
        this.view = new WeakReference<>(view);
        this.progressBar = new WeakReference<>(progressBar);
        this.context = new WeakReference<>(context);
    }

    public void doInBackground(final String string) {
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final StorageReference downloadRef = storageRef.child(mAuth.getCurrentUser().getEmail()).child(string);

        if(progressBar.get() != null) progressBar.get().setVisibility(View.VISIBLE);

        downloadRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if(progressBar.get() != null && context.get().getApplicationContext() != null) {
                    progressBar.get().setVisibility(View.GONE);
                    GlideApp.with(context.get().getApplicationContext()).load(uri).override(Target.SIZE_ORIGINAL).into(view.get());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(progressBar.get() != null) progressBar.get().setVisibility(View.GONE);
                e.printStackTrace();
            }
        });

        downloadRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                PartsHolder.getInstance().imageMetadata = null;
                PartsHolder.getInstance().imageMetadata = new ImageMetadata(
                        storageMetadata.getCustomMetadata(ExifInterface.TAG_DATETIME)
                        , storageMetadata.getCustomMetadata(ExifInterface.TAG_GPS_LATITUDE)
                        , storageMetadata.getCustomMetadata(ExifInterface.TAG_GPS_LATITUDE_REF)
                        , storageMetadata.getCustomMetadata(ExifInterface.TAG_GPS_LONGITUDE)
                        , storageMetadata.getCustomMetadata(ExifInterface.TAG_GPS_LONGITUDE_REF)
                );
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }
}
