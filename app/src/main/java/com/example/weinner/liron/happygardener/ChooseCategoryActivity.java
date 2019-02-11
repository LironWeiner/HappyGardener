package com.example.weinner.liron.happygardener;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.weinner.liron.happygardener.databinding.ActivityChooseCategoryBinding;

public class ChooseCategoryActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityChooseCategoryBinding binding = DataBindingUtil.setContentView(this , R.layout.activity_choose_category);

        binding.buttonCategoryGrass.setOnClickListener(this);
        binding.buttonCategoryFertilizer.setOnClickListener(this);
        binding.buttonCategoryTree.setOnClickListener(this);
        binding.buttonCategoryPlant.setOnClickListener(this);
        binding.buttonCategoryPipe.setOnClickListener(this);
        binding.buttonCategoryFence.setOnClickListener(this);
        binding.buttonCategorySoil.setOnClickListener(this);
        binding.buttonCategoryDecoration.setOnClickListener(this);
        binding.buttonCategoryGeneral.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String _category = null;
        switch (view.getId()){
            case R.id.button_category_grass:
                _category = getString(R.string.grass);
                break;
            case R.id.button_category_fertilizer:
                _category = getString(R.string.fertilizer);
                break;
            case R.id.button_category_tree:
                _category = getString(R.string.tree);
                break;
            case R.id.button_category_plant:
                _category = getString(R.string.plants);
                break;
            case R.id.button_category_pipe:
                _category = getString(R.string.pipes);
                break;
            case R.id.button_category_fence:
                _category = getString(R.string.fences);
                break;
            case R.id.button_category_soil:
                _category = getString(R.string.soil);
                break;
            case R.id.button_category_decoration:
                _category = getString(R.string.decorations);
                break;
            case R.id.button_category_general:
                _category = getString(R.string.general);
                break;
        }
        if(_category != null && !_category.isEmpty()) {
            Intent i = new Intent(ChooseCategoryActivity.this, PartsByCategoryActivity.class);
            i.putExtra("title" , _category);
            if(CheckedItems.getInstance().chooseMode && CheckedItems.getInstance().UpdateCustomerMode)
            {
                int customer_id = getIntent().getIntExtra("customer_id" , 0);
                i.putExtra("customer_id" , customer_id);
                i.putExtra("choose" , "yes");
            }
            else if(CheckedItems.getInstance().chooseMode){
                i.putExtra("choose" , "yes");
            }
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = null;

        if(getIntent() != null && getIntent().getBooleanExtra("CustomersActivity", false)){
            i = new Intent(getApplicationContext(), CustomersActivity.class);

        }
        /*else if(!CheckedItems.getInstance().chooseMode) {
            //i = new Intent(getApplicationContext(), CustomersActivity.class);
            super.onBackPressed();
        }
        else if(CheckedItems.getInstance().chooseMode && CheckedItems.getInstance().UpdateCustomerMode){
            //i = new Intent(getApplicationContext(), CustomerParts.class);
            super.onBackPressed();
        }
        else if(CheckedItems.getInstance().chooseMode) {
            //i = new Intent(getApplicationContext(), CustomerParts.class);
            super.onBackPressed();
        }*/
        // set the new task and clear flags . Clears the stack
        if(i != null) {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        else{
            super.onBackPressed();
        }
    }

}
