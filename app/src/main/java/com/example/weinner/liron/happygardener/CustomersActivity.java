package com.example.weinner.liron.happygardener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.weinner.liron.happygardener.databinding.ActivityCustomersBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CustomersActivity extends AppCompatActivity {
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private boolean isFabOpen = false; // Flag for the floating action button animation when long click
    private CustomersAdapter customersAdapter;
    private ArrayList<Customer> customersList;
    private ActivityCustomersBinding binding;
    private CompositeDisposable disposableList = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this , R.layout.activity_customers);
        GlideApp.with(binding.framelayoutBackground).load(R.drawable.forest1).into(binding.framelayoutBackground);
        // Get firebase current user and get database instance
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        // End of firebase init


        binding.customerList.setHasFixedSize(true);

        // Init animations
        fab_open = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        //

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        binding.customerList.setLayoutManager(mLayoutManager);

        binding.toolbar.setTitle("");
        binding.toolbar.setVisibility(View.GONE);
        setSupportActionBar(binding.toolbar);
        //refresh();

        List<FileDownloadTask> downloadTask = storageRef.child(mAuth.getCurrentUser().getEmail()).child("dbBackup").child(DBHelper.getDBName()).getActiveDownloadTasks();
        if(downloadTask != null && downloadTask.size() != 0) {
            Toast.makeText(this, "trying to download database", Toast.LENGTH_SHORT).show();
            downloadTask.get(0).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    refresh();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText.toLowerCase(Locale.getDefault());

                if(customersAdapter != null) {
                    // If a customer name
                    if (text.matches("^[a-z'א-ת][a-z' א-ת]*")) {
                        Disposable disposable = DBHelper.getInstance(CustomersActivity.this)
                                .getAllCustomersByName(text, false)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.computation())
                                .subscribeWith(getObserverSearchView());
                        disposableList.add(disposable);

                    }
                    // If a date
                    else if (text.matches("^[0-9][0-9]*")) {
                        Disposable disposable = DBHelper.getInstance(CustomersActivity.this)
                                .getAllCustomersByName(text, true)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.computation())
                                .subscribeWith(getObserverSearchView());
                        disposableList.add(disposable);
                    } else if (text.isEmpty()) {
                        Disposable disposable = DBHelper.getInstance(CustomersActivity.this)
                                .getAllCustomers()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.computation())
                                .subscribeWith(getObserverSearchView());
                        disposableList.add(disposable);
                    }
                    else {
                        customersList.clear();
                        customersAdapter.filter(customersList);
                    }
                }

                return false;
            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!CheckedItems.getInstance().isVisible && !isFabOpen) {
                    startActivity(new Intent(CustomersActivity.this, AddCustomerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                }
                else if(isFabOpen) {
                    animateFAB();
                }
                else if(CheckedItems.getInstance().isVisible)
                {
                    openDialog();
                }
            }
        });

        binding.fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                if(!CheckedItems.getInstance().isVisible){
                    animateFAB();
                }
                return true;
            }
        });

        binding.fabSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogSignOut();
            }
        });

        binding.fabParts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomersActivity.this, ChooseCategoryActivity.class);
                intent.putExtra("CustomersActivity",true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                animateFAB();
                startActivity(intent);
            }
        });
    }

    @NonNull
    private DisposableSingleObserver<ArrayList<Customer>> getObserverSearchView() {
        return new DisposableSingleObserver<ArrayList<Customer>>() {
            @Override
            public void onSuccess(ArrayList<Customer> customers) {
                customersList = customers;
                if(customersList != null) customersAdapter.filter(customersList);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
    }

    @NonNull
    private DisposableSingleObserver<ArrayList<Customer>> getObserverRefresh() {
        return new DisposableSingleObserver<ArrayList<Customer>>() {
            @Override
            public void onSuccess(ArrayList<Customer> customers) {
                customersList = customers;
                customersAdapter = new CustomersAdapter(customersList, binding.toolbar, binding.fab);
                RecyclerView.Adapter mAdapter = customersAdapter;
                binding.customerList.setAdapter(mAdapter);
            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
    }

    private void disposeAll() {
        if (disposableList!=null && !disposableList.isDisposed()) {
            disposableList.clear();
        }
    }

    // Sign out properly
    private void signOut()
    {
        new BackupDatabase(CustomersActivity.this)
                .doInBackground();
    }

    private void refresh()
    {
        Disposable disposable = DBHelper.getInstance(CustomersActivity.this)
                .getAllCustomers()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribeWith(getObserverRefresh());
        disposableList.add(disposable);
    }

    @Override
    public void onBackPressed() {
        resetFlags();
    }

    private void resetFlags() {
        binding.fab.setVisibility(View.VISIBLE);
        if(!CheckedItems.getInstance().isEmpty()){
            CheckedItems.getInstance().checkedItems.clear();
            CheckedItems.getInstance().checked_indexes.clear();
            customersAdapter.notifyDataSetChanged();
        }
        CheckedItems.getInstance().chooseMode = false;
        CheckedItems.getInstance().isVisible = false;
        CheckedItems.getInstance().UpdateCustomerMode = false;
        PartsHolder.getInstance().update_customer_first_start = false;
        binding.search.onActionViewCollapsed();
        if(isFabOpen)
            animateFAB();
        CheckedItems.getInstance().isVisible = false;
        PartsHolder.getInstance().animateFabAndToolbar(getApplicationContext() , binding.fab , binding.toolbar);
    }

    // Animation for fab action button
    private void animateFAB() {
        if (isFabOpen) {
            binding.fab.startAnimation(rotate_backward);
            binding.fabSignOut.startAnimation(fab_close);
            binding.fabParts.startAnimation(fab_close);
            binding.fabSignOutHint.startAnimation(fab_close);
            binding.fabPartsHint.startAnimation(fab_close);
            binding.fabSignOut.setClickable(false);
            binding.fabParts.setClickable(false);
            isFabOpen = false;
        } else {
            binding.fab.startAnimation(rotate_forward);
            binding.fabSignOut.startAnimation(fab_open);
            binding.fabParts.startAnimation(fab_open);
            binding.fabSignOutHint.startAnimation(fab_open);
            binding.fabPartsHint.startAnimation(fab_open);
            binding.fabSignOut.setClickable(true);
            binding.fabParts.setClickable(true);
            isFabOpen = true;
        }
    }

    private void openDialogSignOut()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CustomersActivity.this);
        alertDialogBuilder.setMessage(R.string.opendialog_sign_out);

        alertDialogBuilder.setPositiveButton(R.string.opendialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                signOut();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.opendialog_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetFlags();
        refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposeAll();
        // Clear all instances
        CheckedItems.getInstance().checkedItems = null;
        CheckedItems.getInstance().checked_indexes = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customers_activity_menu, menu);
        return true;
    }

    private void openDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CustomersActivity.this);
        alertDialogBuilder.setMessage(R.string.dialog_remove_customer)
                .setPositiveButton(R.string.opendialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        customersAdapter.removeItems();
                        CheckedItems.getInstance().isVisible = false;
                    }
                })
                .setNegativeButton(R.string.opendialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetFlags();
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .setTitle(R.string.dialog_warning)
                .setIcon(R.drawable.ic_warning);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.select_all:
                int size = customersList.size();
                if(!CheckedItems.getInstance().isEmpty() && customersAdapter.getItemCount() == CheckedItems.getInstance().checked_indexes.size())
                {
                    CheckedItems.getInstance().checked_indexes.clear();
                    CheckedItems.getInstance().checkedItems .clear();
                    binding.toolbar.setTitle("0");
                }
                else {
                    CheckedItems.getInstance().checked_indexes = new ArrayList<>();
                    CheckedItems.getInstance().checkedItems = new SparseBooleanArray();
                    for (int i = 0; i < size; i++) {
                        CheckedItems.getInstance().checkedItems.put(i, true);
                        CheckedItems.getInstance().checked_indexes.add(i);
                    }

                }
                PartsHolder.getInstance().animateFabAndToolbar(getApplicationContext() , binding.fab , binding.toolbar);
                customersAdapter.notifyDataSetChanged();
                break;
        }
        return true;
    }

}



