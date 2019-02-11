package com.example.weinner.liron.happygardener;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Liron weinner
 */

class BackupDatabase{
    private final WeakReference<Context> context;

    public BackupDatabase(final Context context){
        this.context = new WeakReference<>(context);
    }


    public void doInBackground() {
        uploadDatabaseToFirebase();
    }

    private void uploadDatabaseToFirebase()
    {
        ProgressDialog progressDialog = new ProgressDialog(context.get());
        progressDialog.setMessage(context.get().getResources().getString(R.string.progress_dialog_backup_database));
        progressDialog.setCancelable(false);
        progressDialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference();
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        StorageReference storageReference = storage.getReference().child(mAuth.getCurrentUser().getEmail()).child("dbBackup").child(DBHelper.getDBName());

        final File currentDB = context.get().getDatabasePath(DBHelper.getDBName());
        UploadTask uploadTask = storageReference.putFile(Uri.fromFile(currentDB));

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Log.d("firebaseUpload", "onSuccess: ");
                // This intent will send the user's email to the main activity to check if there's a need to download database file
                // Or just leave the same database

                progressDialog.dismiss();
                Intent sign_out_intent = new Intent(context.get() , MainActivity.class);
                sign_out_intent.putExtra("userName",mAuth.getCurrentUser().getEmail());
                mAuth.signOut();
                GoogleSignIn.getClient(context.get() , GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                sign_out_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.get().startActivity(sign_out_intent);
            }
        });
    }
}
