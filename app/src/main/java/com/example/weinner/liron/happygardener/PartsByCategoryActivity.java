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
import android.widget.SearchView;

import com.example.weinner.liron.happygardener.databinding.ActivityPartsByCategoryBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class PartsByCategoryActivity extends AppCompatActivity {
    private PartsCollectionAdapter partsCollectionAdapter;
    //
    private ArrayList<Part> partsList;

    private StorageReference storageRef;
    private ActivityPartsByCategoryBinding binding;
    private CompositeDisposable disposableList = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this , R.layout.activity_parts_by_category);

        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);

        //Firebase
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        if(getIntent().getStringExtra("choose") != null && getIntent().getStringExtra("choose").equals("yes"))
        {
            binding.fab.setVisibility(View.GONE);
            binding.fab.setEnabled(false);
            CheckedItems.getInstance().chooseMode = true;
        }

        binding.included.textViewCategory.setText(getIntent().getStringExtra("title"));

        binding.included.partCollectionList.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        binding.included.partCollectionList.setLayoutManager(mLayoutManager);
        //refresh();

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText.toLowerCase(Locale.getDefault());
                if(partsCollectionAdapter != null) {
                    // If parts name
                    if (text.matches("^[a-z0-9א-ת'][a-zא-ת0-9' ]*")) {
                        Disposable disposable = DBHelper.getInstance(getApplicationContext())
                                .getPartsByCategoryAndName(getIntent().getStringExtra("title"), text)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.computation())
                                .subscribeWith(getObserverSearchView());
                        disposableList.add(disposable);
                    }
                    else if (text.isEmpty()){
                        Disposable disposable = DBHelper.getInstance(getApplicationContext())
                                .getPartsByCategory(getIntent().getStringExtra("title"))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.computation())
                                .subscribeWith(getObserverSearchView());
                        disposableList.add(disposable);
                    }
                    else {
                        partsList.clear();
                        partsCollectionAdapter.filter(partsList);
                    }

                }
                return false;
            }

        });

        // Enable delete mode only when choose mode is off
        if(!CheckedItems.getInstance().chooseMode) {
            binding.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!CheckedItems.getInstance().isVisible) {
                        //disposeAll();
                        Intent i = new Intent(PartsByCategoryActivity.this, AddPartActivity.class);
                        i.putExtra("category", getIntent().getStringExtra("title"));
                        startActivity(i);
                    } else if (CheckedItems.getInstance().isVisible) {
                        openDialog();
                    }
                }
            });
        }
    }

    private void refresh()
    {
        Disposable disposable = DBHelper.getInstance(getApplicationContext())
                .getPartsByCategory(getIntent().getStringExtra("title"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribeWith(getObserverRefresh());
        disposableList.add(disposable);
    }

    @NonNull
    private DisposableSingleObserver<ArrayList<Part>> getObserverRefresh() {
        return new DisposableSingleObserver<ArrayList<Part>>() {
            @Override
            public void onSuccess(ArrayList<Part> parts) {
                partsList = parts;
                partsCollectionAdapter = new PartsCollectionAdapter(partsList , storageRef , binding.toolbar , binding.fab , getIntent().getStringExtra("title"));
                RecyclerView.Adapter mAdapter = partsCollectionAdapter;
                binding.included.partCollectionList.setAdapter(mAdapter);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
    }

    @NonNull
    private DisposableSingleObserver<ArrayList<Part>> getObserverSearchView() {
        return new DisposableSingleObserver<ArrayList<Part>>() {
            @Override
            public void onSuccess(ArrayList<Part> parts) {
                partsList = parts;
                if(partsList != null) partsCollectionAdapter.filter(partsList);
            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
    }

    private void resetFlags() {
        CheckedItems.getInstance().isVisible = false;
        binding.search.onActionViewCollapsed();
        if(!CheckedItems.getInstance().isEmpty()){
            CheckedItems.getInstance().checkedItems.clear();
            CheckedItems.getInstance().checked_indexes.clear();
            CheckedItems.getInstance().checkedItems = null;
            CheckedItems.getInstance().checked_indexes = null;
            partsCollectionAdapter.notifyDataSetChanged();
            PartsHolder.getInstance().animateFabAndToolbar(getApplicationContext() , binding.fab , binding.toolbar);
        }
    }

    /*@Override
    protected void onStop() {
        super.onStop();
        disposeAll();
    }*/

    private void disposeAll() {
        if (disposableList!=null && !disposableList.isDisposed()) {
            disposableList.clear();
        }
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        resetFlags();
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposeAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetFlags();
        refresh();
    }

    @Override
    public void onBackPressed() {
        CheckedItems.getInstance().isVisible = false;
        if(!CheckedItems.getInstance().isEmpty()){
            CheckedItems.getInstance().checkedItems.clear();
            CheckedItems.getInstance().checked_indexes.clear();
            CheckedItems.getInstance().checkedItems = null;
            CheckedItems.getInstance().checked_indexes = null;
            PartsHolder.getInstance().animateFabAndToolbar(getApplicationContext() , binding.fab , binding.toolbar);
            partsCollectionAdapter.notifyDataSetChanged();
        }
        else if(CheckedItems.getInstance().isEmpty()) {
            // If adding a part to collection
            /*if(!CheckedItems.getInstance().chooseMode){
                Intent i = new Intent(getApplicationContext() , ChooseCategoryActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
            else {
                super.onBackPressed();
            }*/
            super.onBackPressed();
        }
    }

    private void openDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PartsByCategoryActivity.this);
        alertDialogBuilder.setMessage(R.string.dialog_remove_part)
                .setPositiveButton(R.string.opendialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        partsCollectionAdapter.removeItems();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customers_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.select_all:
                int size = partsList.size();
                if(!CheckedItems.getInstance().isEmpty() && partsCollectionAdapter.getItemCount() == CheckedItems.getInstance().checked_indexes.size())
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
                partsCollectionAdapter.notifyDataSetChanged();
                break;
        }

        return true;
    }
}
