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
import android.widget.Toast;

import com.example.weinner.liron.happygardener.databinding.PartCollectionItemBinding;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liron weinner
 */

public class PartsCollectionAdapter extends RecyclerView.Adapter<PartsCollectionAdapter.PartViewHolder>{
    private List<Part> partsList;
    private Context context;
    private StorageReference storageRef;
    private final WeakReference<Toolbar> toolbar;
    private final WeakReference<FloatingActionButton> add_part;
    private final String part_category;

    public PartsCollectionAdapter(List<Part> partsList, StorageReference storageRef, Toolbar toolbar, FloatingActionButton add_part, final String part_category) {
        this.partsList = partsList;
        this.storageRef = storageRef;
        this.toolbar = new WeakReference<>(toolbar);
        this.add_part = new WeakReference<>(add_part);
        this.part_category = part_category;
    }

    @Override
    public PartsCollectionAdapter.PartViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        context = parent.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final PartCollectionItemBinding binding = PartCollectionItemBinding.inflate(layoutInflater , parent , false);

        return new PartsCollectionAdapter.PartViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return partsList.size();
    }

    public static class PartViewHolder extends RecyclerView.ViewHolder {
        private final PartCollectionItemBinding binding;

        PartViewHolder(final PartCollectionItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    @Override
    public void onBindViewHolder(final PartsCollectionAdapter.PartViewHolder partViewHolder, final int position) {
        final int i = partViewHolder.getAdapterPosition();
        final Part part = partsList.get(i);
        partViewHolder.binding.textViewPartName.setText(context.getString(R.string.name)+": "+part.getName());
        partViewHolder.binding.textViewPartPrice.setText(context.getString(R.string.price)+": "+part.getPrice());

        toggleRowOnBind(partViewHolder, i, part);


        partViewHolder.binding.partCardCollectionView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!CheckedItems.getInstance().isVisible && !CheckedItems.getInstance().chooseMode) {
                    CheckedItems.getInstance().isVisible = true;
                    toggleRowClick(partViewHolder , i);
                }
                return true;
            }
        });
        partViewHolder.binding.partCardCollectionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckedItems.getInstance().isVisible){
                    toggleRowClick(partViewHolder , i);
                }
                else if(CheckedItems.getInstance().chooseMode && CheckedItems.getInstance().UpdateCustomerMode){
                    Intent intent = new Intent(context , AddCustomerPartActivity.class);
                    intent.putExtra("id",partsList.get(i).getId());
                    int customer_id = ((PartsByCategoryActivity)context).getIntent().getIntExtra("customer_id" , 0);
                    intent.putExtra("customer_id" , customer_id);
                    context.startActivity(intent);
                }
                // User parts mode
                else if(CheckedItems.getInstance().chooseMode)
                {
                    Intent intent = new Intent(context , AddCustomerPartActivity.class);
                    intent.putExtra("id",partsList.get(i).getId());
                    context.startActivity(intent);
                }
                // Update parts mode
                else if(!CheckedItems.getInstance().chooseMode){
                    Intent intent = new Intent(context , UpdatePartActivity.class);
                    intent.putExtra("id",partsList.get(i).getId());
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

    private void toggleRowOnBind(PartsCollectionAdapter.PartViewHolder partViewHolder, int i, Part part) {
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
                partViewHolder.binding.buttonPartDelete.setTextSize(24);
            }
        }
        else
        {
            partViewHolder.binding.buttonPartDelete.setBackgroundResource(R.drawable.ic_round);
            partViewHolder.binding.buttonPartDelete.setText(String.valueOf(part.getName().charAt(0)));
            partViewHolder.binding.buttonPartDelete.setTextSize(24);
        }
    }

    private void toggleRowClick(final PartsCollectionAdapter.PartViewHolder partViewHolder, int i) {
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
                    partViewHolder.binding.buttonPartDelete.setTextSize(20);
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

                    String delete_val = DBHelper.getInstance(context).deletePart(prevPartsList.get(index).getPart_id());
                    if(delete_val != null)
                    {
                        Toast.makeText(context , context.getString(R.string.toast_delete_used_part_1)+" "
                                +prevPartsList.get(index).getName()+" "+context.getString(R.string.toast_delete_used_part_2 )+" "+delete_val , Toast.LENGTH_LONG).show();
                        continue;
                    }
                    int adjusted_index = partsList.indexOf(prevPartsList.get(index));
                    new DeleteImage(storageRef).doInBackground(part_category+"/" + partsList.get(adjusted_index).getPart_id() + ".webp");
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

    public void filter(final ArrayList<Part> filteredPartsList){
        partsList = new ArrayList<>();
        partsList.addAll(filteredPartsList);
        notifyDataSetChanged();
    }
}

