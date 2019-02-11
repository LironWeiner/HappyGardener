package com.example.weinner.liron.happygardener;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Liron Weinner
 */

class DownloadDatabase{
    private boolean is_other_user_name = false;
    private final WeakReference<Context> context;

    public DownloadDatabase(boolean is_other_user_name ,final Context context){
        this.is_other_user_name = is_other_user_name;
        this.context = new WeakReference<>(context);
    }
    // Empty c'tor
    public DownloadDatabase(final Context context){
        this.context = new WeakReference<>(context);
    }

    public void doInBackground() {
        downloadDatabaseFromFirebase();
    }

    private void downloadDatabaseFromFirebase()
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference();
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        StorageReference storageReference = storage.getReference().child(mAuth.getCurrentUser().getEmail()).child("dbBackup").child(DBHelper.getDBName());

        final File currentDB = context.get().getDatabasePath(DBHelper.getDBName());

        storageReference.getFile(currentDB).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                context.get().startActivity(new Intent(context.get() , CustomersActivityGuideActivity.class));
                //context.startActivity(new Intent(context , CustomersActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                if(is_other_user_name) {
                    currentDB.delete();
                }
                context.get().startActivity(new Intent(context.get() , CustomersActivityGuideActivity.class));
                //context.startActivity(new Intent(context , CustomersActivity.class));
            }
        });

    }
}
