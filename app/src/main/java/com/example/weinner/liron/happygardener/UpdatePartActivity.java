package com.example.weinner.liron.happygardener;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.weinner.liron.happygardener.databinding.ActivityAddPartBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;

public class UpdatePartActivity extends AppCompatActivity {
    private Part part ;
    private Part temp_part;

    private StorageReference storageRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final int PICK_IMAGE_REQUEST = 71;
    private final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 70;
    private boolean changed_picture_flag = false;
    private static Uri filePath = null;
    private boolean to_delete_image = false;
    private ActivityAddPartBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this , R.layout.activity_add_part);
        GlideApp.with(binding.framelayoutBackground).load(R.drawable.add_part_background).into(binding.framelayoutBackground);

        //Firebase
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        binding.buttonPartCreate.setText(R.string.update_part);
        // Get the current part before changing info
        temp_part = DBHelper.getInstance(UpdatePartActivity.this).getPartById(getIntent().getIntExtra("id",0));
        part = new Part(temp_part);
        binding.editTextPartName.setText(temp_part.getName());
        binding.editTextPartPrice.setText(temp_part.getPrice());

        final List<UploadTask> uploadTask = storageRef.child(mAuth.getCurrentUser().getEmail()).child(part.getCategory().getCategory()+"/" + part.getPart_id() + ".webp").getActiveUploadTasks();
        if(uploadTask != null && uploadTask.size() != 0) {
            uploadTask.get(0).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    new DownloadImage(storageRef, binding.imageButtonPartsPicture , binding.progressBar , getApplicationContext())
                            .doInBackground(part.getCategory().getCategory()+"/" + part.getPart_id() + ".webp");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

        if(part.getCategory().getCategory().equals(getString(R.string.plants))){
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(UpdatePartActivity.this ,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(UpdatePartActivity.this ,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    ActivityCompat.requestPermissions(UpdatePartActivity.this ,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(UpdatePartActivity.this ,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // Permission has already been granted
                makePictureLayoutVisible();
                initDownloadImage();
            }
        }

        binding.fabRemovePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.imageButtonPartsPicture.getDrawable() != null && binding.imageButtonPartsPicture.getBackground() != null) {
                    // Reset rotation
                    /*binding.imageButtonPartsPicture.setRotation(0);
                    binding.imageButtonPartsPicture.setImageResource(R.drawable.ic_picture);*/
                    GlideApp.with(binding.imageButtonPartsPicture).load(R.drawable.ic_picture).into(binding.imageButtonPartsPicture);
                    to_delete_image = true;
                    changed_picture_flag = true;
                    filePath = null;
                }
            }
        });

        binding.fabAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        binding.imageButtonPartsPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!to_delete_image && changed_picture_flag){
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(filePath, "image");
                    startActivity(intent);
                }
                else if(!to_delete_image && binding.imageButtonPartsPicture.getDrawable() != null) {
                    ExifData.getInstance().insertExifData(binding.imageButtonPartsPicture , part);

                    MediaScannerConnection.scanFile(UpdatePartActivity.this
                            , new String[]{ExifData.getInstance().imageFile.getAbsolutePath()}
                            ,null
                            , new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setDataAndType(uri, "image");
                                    startActivityForResult(intent , ExifData.getInstance().DELETE_IMAGE_FILE);
                                }
                            });
                }
            }
        });


        binding.editTextPartName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                part.setName(editable.toString());
            }
        });

        binding.editTextPartPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                part.setPrice(editable.toString());
            }
        });

        binding.buttonPartCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(part.getName() == null || part.getName().isEmpty())
                {
                    binding.editTextPartName.setError(getString(R.string.error_part_name));
                }
                else if(part.getPrice() == null || part.getPrice().isEmpty())
                {
                    binding.editTextPartPrice.setError(getString(R.string.error_part_price));
                }
                else if(part.getPrice() != null && part.getPrice().charAt(0) == '.' ){
                    binding.editTextPartPrice.setError(getString(R.string.error_valid_number));
                }
                else
                {
                    // Check if the price value is a valid number
                    try {
                        double number = Double.parseDouble(part.getPrice());
                        int point_index = binding.editTextPartPrice.getText().toString().indexOf('.');
                        if(binding.editTextPartPrice.getText().toString().length()-1 == point_index)
                            binding.editTextPartPrice.setError(getString(R.string.error_valid_number));
                        else if(number <= 0)
                            binding.editTextPartPrice.setError(getString(R.string.error_valid_number));
                        else if(part.isTheSame(temp_part) && !changed_picture_flag && !to_delete_image)
                            Toast.makeText(UpdatePartActivity.this, R.string.toast_equal, Toast.LENGTH_SHORT).show();
                        else {
                            if(filePath != null && !to_delete_image) {
                                new UploadImage(storageRef, binding.imageButtonPartsPicture)
                                        .doInBackground(String.valueOf(filePath), String.valueOf(part.getPart_id()));
                            }
                            else if(to_delete_image){
                                new DeleteImage(storageRef, binding.imageButtonPartsPicture)
                                        .doInBackground(part.getCategory().getCategory()+"/" + part.getPart_id() + ".webp");
                            }
                            part.setPrice(Utility.setRoundedNumber(part.getPrice()));
                            DBHelper.getInstance(UpdatePartActivity.this).updatePart(part);
                            /*Intent i = new Intent(getApplicationContext(), PartsByCategoryActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("title", part.getCategory().getCategory());
                            startActivity(i);*/
                            finish();
                        }
                    }
                    catch(NumberFormatException e){
                        binding.editTextPartPrice.setError(getString(R.string.error_valid_number));
                    }
                }
            }
        });

    }

    private void initDownloadImage() {
        if(binding.imageButtonPartsPicture.getDrawable() == null && !to_delete_image) {
            new DownloadImage(storageRef, binding.imageButtonPartsPicture , binding.progressBar , getApplicationContext())
                    .doInBackground(part.getCategory().getCategory()+"/" + part.getPart_id() + ".webp");
        }
    }

    @Override
    public void onBackPressed() {
        to_delete_image = false;
        super.onBackPressed();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.upload_picture)), PICK_IMAGE_REQUEST);
    }

    private boolean rotatePictureToPortrait(Uri filePath){
        final String realPath = getRealPathFromURI(filePath.toString());
        if(realPath != null) {
            try {
                ExifInterface exifReader = new ExifInterface(realPath);
                PartsHolder.getInstance().imageMetadata = new ImageMetadata(
                        exifReader.getAttribute(ExifInterface.TAG_DATETIME)
                        , exifReader.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                        , exifReader.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                        , exifReader.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
                        , exifReader.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
                );

                /*int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                int rotate = 0;
                if (orientation == ExifInterface.ORIENTATION_NORMAL) {
                    // Do nothing. The original image is fine.
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    rotate = 90;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    rotate = 180;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    rotate = 270;
                }
                binding.imageButtonPartsPicture.setImageDrawable(Drawable.createFromPath(realPath));
                binding.imageButtonPartsPicture.setRotation(rotate);*/

                GlideApp.with(binding.imageButtonPartsPicture)
                        .load(realPath)
                        .into(binding.imageButtonPartsPicture);

                return true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Image dosent exist or other possible problem
        else{
            Toast.makeText(this, R.string.toast_picture_invalid, Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = UpdatePartActivity.this.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String cursor_path_string = null;
            if(index != -1) {
                cursor_path_string = cursor.getString(index);
            }
            cursor.close();
            return cursor_path_string;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            if(data.getData().toString().contains("content://com.google.android.apps"))
            {
                Toast.makeText(this, R.string.toast_pick_image_from_phone, Toast.LENGTH_SHORT).show();
                filePath = null;
                changed_picture_flag = false;
            }
            else if(filePath == null){
                changed_picture_flag = true;
                filePath = data.getData();
            }
            else if(!filePath.equals(data.getData())){
                changed_picture_flag = true;
                filePath = data.getData();
            }
            else {
                changed_picture_flag = false;
                filePath = data.getData();
            }
            to_delete_image = false;
            if(filePath != null) {
                if (!rotatePictureToPortrait(filePath)){
                    filePath = null;
                    changed_picture_flag = false;
                    to_delete_image = false;
                }
            }
        }
        else if(requestCode == ExifData.getInstance().DELETE_IMAGE_FILE && ExifData.getInstance().imageFile != null)
        {
            ExifData.getInstance().imageFile.delete();
            ExifData.getInstance().imageFile = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePictureLayoutVisible();
                    initDownloadImage();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void makePictureLayoutVisible() {
        findViewById(R.id.textView_parts_picture).setVisibility(View.VISIBLE);
        binding.fabAddPicture.setVisibility(View.VISIBLE);
        binding.fabRemovePicture.setVisibility(View.VISIBLE);
        binding.imageButtonPartsPicture.setVisibility(View.VISIBLE);
    }
}
