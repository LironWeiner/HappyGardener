package com.example.weinner.liron.happygardener;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.weinner.liron.happygardener.databinding.CustomerListItemBinding;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class CustomersAdapter extends RecyclerView.Adapter<CustomersAdapter.CustomerViewHolder> {

    private List<Customer> customersList;
    private Context context;
    private final WeakReference<Toolbar> toolbar;
    private final WeakReference<FloatingActionButton> add_customer;

    public CustomersAdapter(List<Customer> customersList , Toolbar toolbar , FloatingActionButton add_customer) {
        this.customersList = customersList;
        this.toolbar = new WeakReference<>(toolbar);
        this.add_customer = new WeakReference<>(add_customer);
    }

    @NonNull
    @Override
    public CustomersAdapter.CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        context = parent.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final CustomerListItemBinding binding = CustomerListItemBinding.inflate(layoutInflater , parent , false);

        return new CustomerViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return customersList.size();
    }

    public class CustomerViewHolder extends RecyclerView.ViewHolder {
        private final CustomerListItemBinding binding;

        CustomerViewHolder(final CustomerListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }


    @Override
    public void onBindViewHolder(final CustomerViewHolder customerViewHolder, final int position) {
        final int i = customerViewHolder.getAdapterPosition();
        final Customer customer = customersList.get(i);

        customerViewHolder.binding.textViewCustomerName.setText(customer.getName());
        customerViewHolder.binding.textViewCustomerDate.setText(customer.getDate());
        toggleRowOnBind(customerViewHolder, i, customer);

        customerViewHolder.binding.customerCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!CheckedItems.getInstance().isVisible) {
                    CheckedItems.getInstance().isVisible = true;
                    toggleRowClick(customerViewHolder , i);
                }

                return true;
            }
        });
        customerViewHolder.binding.customerCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckedItems.getInstance().isVisible){
                    toggleRowClick(customerViewHolder , i);
                }
                else if(!CheckedItems.getInstance().isVisible){
                    // If clicked on a customer from the CustomersActivity open UpdateCustomerActivity
                    Intent intent = new Intent(context , UpdateCustomerActivity.class);
                    intent.putExtra("customer_id" , customersList.get(i).getId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }
            }
        });

        customerViewHolder.binding.imageViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context,view);
                final MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.more_menu , popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        switch (id){
                            case R.id.customer_navigate:
                                // If no address just Toast the user
                                if(customer.getAddress() == null){
                                    Toast.makeText(context, R.string.toast_navigation, Toast.LENGTH_LONG).show();
                                }
                                else{
                                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+customer.getAddress());
                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                    mapIntent.setPackage("com.google.android.apps.maps");
                                    context.startActivity(mapIntent);
                                }
                                break;
                            case R.id.customer_call:
                                // If no phone just Toast the user
                                if(customer.getPhone() == null || customer.getPhone().isEmpty()){
                                    Toast.makeText(context, R.string.toast_call, Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts(
                                            "tel", customer.getPhone(), null));
                                    context.startActivity(phoneIntent);
                                }
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();

            }
        });



        customerViewHolder.binding.buttonFinishJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRowClick(customerViewHolder, i);
            }});



    }

    private void toggleRowOnBind(CustomerViewHolder customerViewHolder, int i, Customer customer) {
        if(!CheckedItems.getInstance().isEmpty())
        {
            boolean the_pos = CheckedItems.getInstance().checkedItems.get(i);
            if(the_pos) {
                customerViewHolder.binding.buttonFinishJob.setBackgroundResource(R.drawable.ic_round_checked);
                customerViewHolder.binding.buttonFinishJob.setTextSize(0);
            }
            else {
                customerViewHolder.binding.buttonFinishJob.setBackgroundResource(R.drawable.ic_round);
                customerViewHolder.binding.buttonFinishJob.setText(String.valueOf(customer.getName().charAt(0)));
                customerViewHolder.binding.buttonFinishJob.setTextSize(16);
            }
        }
        else
        {
            customerViewHolder.binding.buttonFinishJob.setBackgroundResource(R.drawable.ic_round);
            customerViewHolder.binding.buttonFinishJob.setText(String.valueOf(customer.getName().charAt(0)));
            customerViewHolder.binding.buttonFinishJob.setTextSize(16);
        }
    }

    private void toggleRowClick(final CustomerViewHolder customerViewHolder, int i) {
        if(CheckedItems.getInstance().isVisible) {
            final Animation coin_flip = AnimationUtils.loadAnimation(context, R.anim.coin_flip);
            final Animation coin_flip_reverse = AnimationUtils.loadAnimation(context, R.anim.coin_flip_reverse);
            customerViewHolder.binding.buttonFinishJob.startAnimation(coin_flip);
            coin_flip.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    customerViewHolder.binding.buttonFinishJob.startAnimation(coin_flip_reverse);
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
                customerViewHolder.binding.buttonFinishJob.setBackgroundResource(R.drawable.ic_round_checked);
                customerViewHolder.binding.buttonFinishJob.setTextSize(0);
            } else { // Checkboxes selected before
                if (CheckedItems.getInstance().checkedItems.get(i)) { // If checkbox is true set to false
                    CheckedItems.getInstance().checkedItems.put(i, false);
                    CheckedItems.getInstance().checked_indexes.remove(CheckedItems.getInstance().checked_indexes.indexOf(i));
                    customerViewHolder.binding.buttonFinishJob.setBackgroundResource(R.drawable.ic_round);
                    customerViewHolder.binding.buttonFinishJob.setTextSize(16);
                } else { // If checkbox is false set to true
                    CheckedItems.getInstance().checkedItems.put(i, true);
                    CheckedItems.getInstance().checked_indexes.add(i);
                    customerViewHolder.binding.buttonFinishJob.setBackgroundResource(R.drawable.ic_round_checked);
                    customerViewHolder.binding.buttonFinishJob.setTextSize(0);
                }
            }
            PartsHolder.getInstance().animateFabAndToolbar(context , add_customer , toolbar);
        }
    }

    // Remove selected customers from the recycler view
    public void removeItems() {
        if(!CheckedItems.getInstance().isEmpty())
        {
            // If CustomersActivity
            if(context.getClass().getCanonicalName().equals(CustomersActivity.class.getCanonicalName())) {
                ArrayList<Customer> prevCustomerList = new ArrayList<>(customersList);

                for (int i = 0; i < CheckedItems.getInstance().checked_indexes.size(); i++) {
                    int index = CheckedItems.getInstance().checked_indexes.get(i);
                    if (CheckedItems.getInstance().checkedItems.get(index)) {
                        DBHelper.getInstance(context).deleteCustomer(prevCustomerList.get(index).getId());
                        int adjusted_index = customersList.indexOf(prevCustomerList.get(index));
                        customersList.remove(adjusted_index);
                    }
                }
                notifyDataSetChanged();
                CheckedItems.getInstance().checkedItems.clear();
                CheckedItems.getInstance().checked_indexes.clear();
                CheckedItems.getInstance().isVisible = false;
                PartsHolder.getInstance().animateFabAndToolbar(context , add_customer , toolbar);
            }
        }
    }

    public void filter(final ArrayList<Customer> filteredCustomersList){
        customersList = new ArrayList<>();
        customersList.addAll(filteredCustomersList);
        notifyDataSetChanged();
    }
}