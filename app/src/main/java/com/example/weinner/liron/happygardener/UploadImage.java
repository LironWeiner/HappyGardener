package com.example.weinner.liron.happygardener;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.util.Log;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.lang.ref.WeakReference;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Liron Weinner
 */

class UploadImage{
    private final StorageReference storageRef;
    private final WeakReference<ImageButton> view;
    private File file;

    public UploadImage(StorageReference storageRef , ImageButton view) {
        this.storageRef = storageRef;
        this.view = new WeakReference<>(view);
    }

    public void doInBackground(final String... strings) {
        if(view != null) {
            file = new File(getRealPathFromURI(strings[0]));

            new Compressor(view.get().getContext())
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(85)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .compressToFileAsFlowable(file)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void accept(File the_file) {
                            file = the_file;
                            uploadImage(strings[0], strings[1]);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable)
                        {
                            Log.d("ImageStupid", "Error with the stupid image");
                            throwable.printStackTrace();
                        }
                    });
        }
    }


    private void uploadImage(String filePath , final String part_id) {
        if(filePath != null && view.get() != null)
        {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            StorageMetadata metadata = null;
            // Create file metadata including the content type
            if(PartsHolder.getInstance().imageMetadata != null) {
                metadata = new StorageMetadata.Builder()
                        .setCustomMetadata(ExifInterface.TAG_DATETIME, PartsHolder.getInstance().imageMetadata.getDate_time())
                        .setCustomMetadata(ExifInterface.TAG_GPS_LATITUDE, PartsHolder.getInstance().imageMetadata.getGps_lat())
                        .setCustomMetadata(ExifInterface.TAG_GPS_LATITUDE_REF, PartsHolder.getInstance().imageMetadata.getGps_lat_ref())
                        .setCustomMetadata(ExifInterface.TAG_GPS_LONGITUDE, PartsHolder.getInstance().imageMetadata.getGps_lng())
                        .setCustomMetadata(ExifInterface.TAG_GPS_LONGITUDE_REF, PartsHolder.getInstance().imageMetadata.getGps_lng_ref())
                        .setContentType("image/jpg")
                        .build();
            }

            PartsHolder.getInstance().imageMetadata = null;

            StorageReference riversRef = storageRef.child(mAuth.getCurrentUser().getEmail()).child(view.get().getResources().getString(R.string.plants)+"/"+part_id+".webp");
            UploadTask uploadTask;
            if(metadata != null) uploadTask = riversRef.putFile(Uri.fromFile(file) , metadata);
            else{
                uploadTask = riversRef.putFile(Uri.fromFile(file));
            }

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    //Toast.makeText(view.get().getContext(), view.get().getContext().getResources().getString(R.string.upload_image_failure), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    //Toast.makeText(view.get().getContext(), view.get().getContext().getResources().getString(R.string.upload_image_success), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = view.get().getContext().getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String cursor_path_string = cursor.getString(index);
            cursor.close();
            return cursor_path_string;
        }
    }
}


