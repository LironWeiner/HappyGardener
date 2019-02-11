package com.example.weinner.liron.happygardener;

import android.support.annotation.NonNull;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;

/**
 * Created by Liron weinner
 */

class DeleteImage{
    private final StorageReference storageRef;
    private final WeakReference<ImageButton> view;

    public DeleteImage(StorageReference storageRef , ImageButton view) {
        this.storageRef = storageRef;
        this.view = new WeakReference<>(view);
    }

    public DeleteImage(StorageReference storageRef) {
        this.storageRef = storageRef;
        view = null;
    }

    public void doInBackground(String string) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // Delete the file
        storageRef.child(mAuth.getCurrentUser().getEmail()).child(string).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                PartsHolder.getInstance().imageMetadata = null;
                PartsHolder.getInstance().bitmap = null;
                if(view != null) {
                    Toast.makeText(view.get().getContext(), R.string.delete_picture_success, Toast.LENGTH_SHORT).show();
                    //GlideApp.with(view.get()).load(resource).into(view.get());
                    //view.get().setImageResource(resource);
                    //view.get().setBackground(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }
}
