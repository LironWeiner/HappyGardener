package com.example.weinner.liron.happygardener;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.weinner.liron.happygardener.databinding.PartListItemBinding;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liron weinner
 */

public class PartsCustomerAdapter extends RecyclerView.Adapter<PartsCustomerAdapter.PartViewHolder>{
    private List<Part> partsList;
    private Context context;
    private final WeakReference<Toolbar> toolbar;
    private final WeakReference<FloatingActionButton> add_part;

    public PartsCustomerAdapter(List<Part> partsList , Toolbar toolbar , FloatingActionButton add_part) {
        this.partsList = partsList;
        this.toolbar = new WeakReference<>(toolbar);
        this.add_part = new WeakReference<>(add_part);
    }

    @Override
    public int getItemCount() {
        if(partsList != null)
            return partsList.size();
        return 0;
    }

    public class PartViewHolder extends RecyclerView.ViewHolder {
        private final PartListItemBinding binding;

        PartViewHolder(final PartListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    @Override
    public PartsCustomerAdapter.PartViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        context = parent.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final PartListItemBinding binding = PartListItemBinding.inflate(layoutInflater , parent , false);

        return new PartsCustomerAdapter.PartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final PartsCustomerAdapter.PartViewHolder partViewHolder, final int position) {
        final int i = partViewHolder.getAdapterPosition();
        final Part part = partsList.get(i);

        partViewHolder.binding.textViewPartName.setText(context.getString(R.string.name)+": "+part.getName());
        partViewHolder.binding.textViewPartQuantity.setText(context.getString(R.string.quantity)+": "+part.getQuantity());
        setIcons(partViewHolder, part , i);
        toggleRowOnBind(partViewHolder, i, part);

        partViewHolder.binding.partCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!CheckedItems.getInstance().isVisible) {
                    CheckedItems.getInstance().isVisible = true;
                    toggleRowClick(partViewHolder , i);
                }
                return true;
            }
        });
        partViewHolder.binding.partCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckedItems.getInstance().isVisible){
                    toggleRowClick(partViewHolder , i);
                }
                else if(!CheckedItems.getInstance().isVisible){
                    Intent intent = null;
                    if(CheckedItems.getInstance().UpdateCustomerMode){
                        PartsHolder.getInstance().bitmap = null;
                        int customer_id = ((CustomerParts) context).getIntent().getIntExtra("customer_id",0);
                        intent = new Intent(context, UpdateNewCustomerPartActivity.class);
                        intent.putExtra("parts_list_index", part.getPart_id());
                        intent.putExtra("customer_id" , customer_id);
                    }
                    else {
                        PartsHolder.getInstance().bitmap = null;
                        intent = new Intent(context, UpdateNewCustomerPartActivity.class);
                        intent.putExtra("parts_list_index", part.getPart_id());
                    }
                    context.startActivity(intent);
                }
            }
        });


        partViewHolder.binding.buttonPartDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRowClick(partViewHolder, i);
            }});



    }

    private void setIcons(PartViewHolder partViewHolder, final Part part , final int i) {
        final String the_category = part.getCategory().getCategory();
        int background_resource = -1;


        if(the_category.equals(context.getResources().getString(R.string.grass)))
            background_resource = R.drawable.ic_grass;
            //partViewHolder.binding.partCategoryIcon.setBackgroundResource(R.drawable.ic_grass);
        else if(the_category.equals(context.getResources().getString(R.string.fertilizer)))
            background_resource = R.drawable.ic_fertilizer;
            //partViewHolder.binding.partCategoryIcon.setBackgroundResource(R.drawable.ic_fertilizer);
        else if(the_category.equals(context.getResources().getString(R.string.tree)))
            background_resource = R.drawable.ic_palm_tree;
            //partViewHolder.binding.partCategoryIcon.setBackgroundResource(R.drawable.ic_palm_tree);
        else if(the_category.equals(context.getResources().getString(R.string.plants)))
            background_resource = R.drawable.ic_flower;
            //partViewHolder.binding.partCategoryIcon.setBackgroundResource(R.drawable.ic_flower);
        else if(the_category.equals(context.getResources().getString(R.string.pipes)))
            background_resource = R.drawable.ic_pipe;
            //partViewHolder.binding.partCategoryIcon.setBackgroundResource(R.drawable.ic_pipe);
        else if(the_category.equals(context.getResources().getString(R.string.fences)))
            background_resource = R.drawable.ic_fence;
            //partViewHolder.binding.partCategoryIcon.setBackgroundResource(R.drawable.ic_fence);
        else if(the_category.equals(context.getResources().getString(R.string.soil)))
            background_resource = R.drawable.ic_soil;
            //partViewHolder.binding.partCategoryIcon.setBackgroundResource(R.drawable.ic_soil);
        else if(the_category.equals(context.getResources().getString(R.string.decorations)))
            background_resource = R.drawable.ic_column;
            //partViewHolder.binding.partCategoryIcon.setBackgroundResource(R.drawable.ic_column);
        else if(the_category.equals(context.getResources().getString(R.string.general)))
            background_resource = R.drawable.ic_wheelbarrow;
        //partViewHolder.binding.partCategoryIcon.setBackgroundResource(R.drawable.ic_wheelbarrow);

        if (background_resource != -1) {
            GlideApp.with(partViewHolder.binding.partCategoryIcon).load(context.getDrawable(background_resource)).into(partViewHolder.binding.partCategoryIcon);
        }

    }

    private void toggleRowOnBind(PartsCustomerAdapter.PartViewHolder partViewHolder, int i, Part part) {
        if(!CheckedItems.getInstance().isEmpty())
        {
            boolean the_pos = CheckedItems.getInstance().checkedItems.get(i);
            if(the_pos) {
                partViewHolder.binding.buttonPartDelete.setBackgroundResource(R.drawable.ic_round_checked);
                partViewHolder.binding.buttonPartDelete.setTextSize(0);
            }
            else {
                partViewHolder.binding.buttonPartDelete.setBackgroundResource(R.drawable.ic_round);
                partViewHolder.binding.buttonPartDelete.setText(String.valueOf(part.getName().charAt(0)));
                partViewHolder.binding.buttonPartDelete.setTextSize(14);
            }
        }
        else
        {
            partViewHolder.binding.buttonPartDelete.setBackgroundResource(R.drawable.ic_round);
            partViewHolder.binding.buttonPartDelete.setText(String.valueOf(part.getName().charAt(0)));
            partViewHolder.binding.buttonPartDelete.setTextSize(14);
        }
    }

    private void toggleRowClick(final PartsCustomerAdapter.PartViewHolder partViewHolder, int i) {
        if(CheckedItems.getInstance().isVisible) {
            final Animation coin_flip = AnimationUtils.loadAnimation(context, R.anim.coin_flip);
            final Animation coin_flip_reverse = AnimationUtils.loadAnimation(context, R.anim.coin_flip_reverse);
            partViewHolder.binding.buttonPartDelete.startAnimation(coin_flip);
            coin_flip.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    partViewHolder.binding.buttonPartDelete.startAnimation(coin_flip_reverse);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            if (CheckedItems.getInstance().isEmpty()) // No checkboxes selected before
            {
                CheckedItems.getInstance().checked_indexes = new ArrayList<>();
                CheckedItems.getInstance().checkedItems = new SparseBooleanArray();
                CheckedItems.getInstance().checkedItems.append(i, true);
                CheckedItems.getInstance().checked_indexes.add(i);
                partViewHolder.binding.buttonPartDelete.setBackgroundResource(R.drawable.ic_round_checked);
                partViewHolder.binding.buttonPartDelete.setTextSize(0);
            } else { // Checkboxes selected before
                if (CheckedItems.getInstance().checkedItems.get(i)) { // If checkbox is true set to false
                    CheckedItems.getInstance().checkedItems.put(i, false);
                    CheckedItems.getInstance().checked_indexes.remove(CheckedItems.getInstance().checked_indexes.indexOf(i));
                    partViewHolder.binding.buttonPartDelete.setBackgroundResource(R.drawable.ic_round);
                    partViewHolder.binding.buttonPartDelete.setTextSize(14);
                } else { // If checkbox is false set to true
                    CheckedItems.getInstance().checkedItems.put(i, true);
                    CheckedItems.getInstance().checked_indexes.add(i);
                    partViewHolder.binding.buttonPartDelete.setBackgroundResource(R.drawable.ic_round_checked);
                    partViewHolder.binding.buttonPartDelete.setTextSize(0);
                }
            }

            PartsHolder.getInstance().animateFabAndToolbar(context , add_part , toolbar);
        }
    }

    // Remove selected parts from the recycler view
    public void removeItems() {
        if(!CheckedItems.getInstance().isEmpty())
        {
            ArrayList<Part> prevPartsList = new ArrayList<>(partsList);

            for(int i=0 ; i < CheckedItems.getInstance().checked_indexes.size() ; i++)
            {
                int index = CheckedItems.getInstance().checked_indexes.get(i);
                if(CheckedItems.getInstance().checkedItems.get(index))
                {
                    PartsHolder.getInstance().parts_list_changed = true;
                    //PartsHolder.parts_list.remove(prevPartsList.get(index));
                    PartsHolder.getInstance().parts_list.remove(prevPartsList.get(index).getPart_id());
                    int adjusted_index = partsList.indexOf(prevPartsList.get(index));
                    partsList.remove(adjusted_index);
                }
            }
            notifyDataSetChanged();
            CheckedItems.getInstance().checkedItems.clear();
            CheckedItems.getInstance().checked_indexes.clear();
            CheckedItems.getInstance().isVisible = false;
            PartsHolder.getInstance().animateFabAndToolbar(context , add_part , toolbar);
        }
    }
}
