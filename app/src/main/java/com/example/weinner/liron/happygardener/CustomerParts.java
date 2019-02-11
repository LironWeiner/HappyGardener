package com.example.weinner.liron.happygardener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.weinner.liron.happygardener.databinding.ActivityCustomerPartsBinding;

import java.util.ArrayList;

public class CustomerParts extends AppCompatActivity {
    private PartsCustomerAdapter partsCustomerAdapter;
    //
    private ActivityCustomerPartsBinding binding;
    private String intent_data = null;
    private int customer_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this , R.layout.activity_customer_parts);

        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);

        // use a linear layout manager
        //RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView.LayoutManager mLayoutManager =
                new GridLayoutManager
                        (getApplicationContext()
                                , 2
                                , GridLayoutManager.VERTICAL
                                , false);
        binding.partList.setLayoutManager(mLayoutManager);
        binding.partList.setItemViewCacheSize(20);
        binding.partList.setDrawingCacheEnabled(true);
        binding.partList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        customer_id = getIntent().getIntExtra("customer_id" , 0);

        if(getIntent().getStringExtra("context") != null && intent_data == null) intent_data = getIntent().getStringExtra("context");
        else restoreActivityData();


        if(intent_data != null && intent_data.equals("AddCustomerActivity")){
            refreshAddCustomerActivityOnly();
        }
        else {
            refreshUpdateCustomerActivity();
        }

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(intent_data != null && intent_data.equals("AddCustomerActivity")) {
                    if (!CheckedItems.getInstance().isVisible) {
                        Intent i = new Intent(getApplicationContext(), ChooseCategoryActivity.class);
                        CheckedItems.getInstance().chooseMode = true; //This change the way that partsByCategoryActivity works
                        startActivity(i);
                    } else if (CheckedItems.getInstance().isVisible) {
                        partsCustomerAdapter.removeItems();
                        CheckedItems.getInstance().isVisible = false;
                    }
                }
                else {
                    if (!CheckedItems.getInstance().isVisible) {
                        Intent i = new Intent(getApplicationContext(), ChooseCategoryActivity.class);
                        i.putExtra("customer_id", getIntent().getIntExtra("customer_id", 0));
                        CheckedItems.getInstance().chooseMode = true; //This change the way that partsByCategoryActivity works
                        CheckedItems.getInstance().UpdateCustomerMode = true;
                        startActivity(i);
                    } else if (CheckedItems.getInstance().isVisible) {
                        partsCustomerAdapter.removeItems();
                        CheckedItems.getInstance().isVisible = false;
                    }
                }

            }
        });
    }

    private void refreshAddCustomerActivityOnly()
    {
        if(PartsHolder.getInstance().parts_list != null) {
            //partsList = new ArrayList<>(PartsHolder.getInstance().parts_list.values());
            PartsHolder.getInstance().partsList = PartsHolder.getInstance().initFromPartsListArrayList();
        }
        else {
            PartsHolder.getInstance().partsList = new ArrayList<>();
        }
        partsCustomerAdapter = new PartsCustomerAdapter(PartsHolder.getInstance().partsList , binding.toolbar , binding.fab);
        RecyclerView.Adapter mAdapter = partsCustomerAdapter;
        binding.partList.setAdapter(mAdapter);
    }

    private void refreshUpdateCustomerActivity()
    {
        // Get data from database only once per lifecycle
        if(!PartsHolder.getInstance().update_customer_first_start) {
            if(PartsHolder.getInstance().parts_list == null) {
                PartsHolder.getInstance().parts_list = DBHelper.getInstance(getApplicationContext()).getAllCustomerParts(getIntent().getIntExtra("customer_id", 0));
            }
            PartsHolder.getInstance().initOriginalPartsList();
            PartsHolder.getInstance().update_customer_first_start = true;
        }
        if(PartsHolder.getInstance().parts_list != null && PartsHolder.getInstance().parts_list.size() > 0) {
            PartsHolder.getInstance().partsList = PartsHolder.getInstance().initFromPartsListArrayList();
        }
        else {
            PartsHolder.getInstance().partsList = new ArrayList<>();
        }

        partsCustomerAdapter = new PartsCustomerAdapter(PartsHolder.getInstance().partsList , binding.toolbar , binding.fab);
        RecyclerView.Adapter mAdapter = partsCustomerAdapter;
        binding.partList.setAdapter(mAdapter);

        if(PartsHolder.getInstance().partsList != null && PartsHolder.getInstance().partsList.size() > 0) {
            PartsHolder.getInstance().parts_list_changed = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("context" , intent_data);
        editor.putInt("customer_id",customer_id);
        editor.apply();
    }

    private void restoreActivityData() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        intent_data = sharedPref.getString("context" , null);
        customer_id = sharedPref.getInt("customer_id" , 0);
    }


    private void resetFlags() {
        CheckedItems.getInstance().isVisible = false;
        if(!CheckedItems.getInstance().isEmpty()){
            CheckedItems.getInstance().checkedItems.clear();
            CheckedItems.getInstance().checked_indexes.clear();
            partsCustomerAdapter.notifyDataSetChanged();
            PartsHolder.getInstance().animateFabAndToolbar(getApplicationContext() , binding.fab , binding.toolbar);
        }
    }

    @Override
    public void onBackPressed() {
        CheckedItems.getInstance().isVisible = false;
        if(!CheckedItems.getInstance().isEmpty()){
            CheckedItems.getInstance().checkedItems.clear();
            CheckedItems.getInstance().checked_indexes.clear();
            partsCustomerAdapter.notifyDataSetChanged();
            PartsHolder.getInstance().animateFabAndToolbar(getApplicationContext() , binding.fab , binding.toolbar);
        }
        else if(intent_data != null && intent_data.equals("AddCustomerActivity")){
            final Intent i = new Intent(getApplicationContext() , AddCustomerActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        else{
            final Intent i = new Intent(getApplicationContext() , UpdateCustomerActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra("customer_id",customer_id);
            startActivity(i);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        resetFlags();
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
                selectAll();
                break;
        }

        return true;
    }

    private void selectAll() {
        int size = PartsHolder.getInstance().partsList.size();
        if(!CheckedItems.getInstance().isEmpty() && partsCustomerAdapter.getItemCount() == CheckedItems.getInstance().checked_indexes.size())
        {
            CheckedItems.getInstance().checked_indexes.clear();
            CheckedItems.getInstance().checkedItems.clear();
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
        partsCustomerAdapter.notifyDataSetChanged();
    }

}
