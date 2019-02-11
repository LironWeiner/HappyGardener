package com.example.weinner.liron.happygardener;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Liron weinner
 */

class PartsHolder {
    private static PartsHolder ourInstance = null;
    public SparseArray<Part> parts_list = null ;
    public SparseArray<Part> original_parts_list = null;
    public ArrayList<Part> partsList = null;
    // The plants picture
    public Bitmap bitmap = null;
    public ImageMetadata imageMetadata= null;
    // This flag for updating a part
    public boolean parts_list_changed = false;
    // This flag is used for the UpdateCustomerActivity in order to load data from database only once
    public boolean update_customer_first_start = false;

    static PartsHolder getInstance(){
        if(ourInstance == null){
            ourInstance = new PartsHolder();
        }
        return ourInstance;
    }

    private PartsHolder() {
    }

    void animateFabAndToolbar(final Context context , final WeakReference<FloatingActionButton> add_part , final WeakReference<Toolbar> toolbar){
        if(add_part.get() != null && toolbar.get() != null) {
            setFabBackground(context, add_part);
            updateToolbar(toolbar);
        }
    }

    void animateFabAndToolbar(final Context context , final FloatingActionButton add_part , final Toolbar toolbar){
        setFabBackground(context, add_part);
        updateToolbar(toolbar);

    }

    /**
     * Change floating action button's background to show if your in delete mode or add mode
     * @param context Application's context
     * @param add_part Must be a FloatingActionButton
     */
    private void setFabBackground(final Context context , final WeakReference<FloatingActionButton> add_part)
    {

        if(CheckedItems.getInstance().isVisible) {
            add_part.get().setVisibility(View.VISIBLE);
            add_part.get().setBackgroundTintList(context.getResources().getColorStateList(R.color.my_red , null));
            add_part.get().setImageResource(R.drawable.ic_delete);
        }
        else if(!CheckedItems.getInstance().isVisible){
            add_part.get().setBackgroundTintList(context.getResources().getColorStateList(R.color.my_add_fab_color , null));
            add_part.get().setImageResource(R.drawable.ic_add);
        }

    }

    private void setFabBackground(final Context context , final FloatingActionButton add_part)
    {

        if(CheckedItems.getInstance().isVisible) {
            add_part.setVisibility(View.VISIBLE);
            add_part.setBackgroundTintList(context.getResources().getColorStateList(R.color.my_red , null));
            add_part.setImageResource(R.drawable.ic_delete);
        }
        else if(!CheckedItems.getInstance().isVisible){
            add_part.setBackgroundTintList(context.getResources().getColorStateList(R.color.my_add_fab_color , null));
            add_part.setImageResource(R.drawable.ic_add);
        }

    }

    /**
     * Update the toolbar's number as a referance to how many items the user selected
     * @param toolbar Must be a Toolbar
     */
    private void updateToolbar(final WeakReference<Toolbar> toolbar)
    {
        if(CheckedItems.getInstance().isVisible) {
            toolbar.get().setVisibility(View.VISIBLE);
            if (!CheckedItems.getInstance().isEmpty())
                toolbar.get().setTitle(String.valueOf(CheckedItems.getInstance().checked_indexes.size()));
        }
        else if(!CheckedItems.getInstance().isVisible)
            toolbar.get().setVisibility(View.GONE);
    }

    private void updateToolbar(final Toolbar toolbar)
    {
        if(CheckedItems.getInstance().isVisible) {
            toolbar.setVisibility(View.VISIBLE);
            if (!CheckedItems.getInstance().isEmpty())
                toolbar.setTitle(String.valueOf(CheckedItems.getInstance().checked_indexes.size()));
        }
        else if(!CheckedItems.getInstance().isVisible)
            toolbar.setVisibility(View.GONE);
    }

    /*void clearBitmapData(){
        if(bitmap != null){
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
    }*/

    ArrayList<Part> initFromPartsListArrayList(){
        ArrayList<Part> result_array = null;
        if(parts_list != null && parts_list.size() > 0) {
            result_array = new ArrayList<>(parts_list.size());
            for(int i=0 ; i < parts_list.size() ; i++){
                int key = parts_list.keyAt(i);
                result_array.add(parts_list.get(key));
            }
        }
        return result_array;
    }

    void initOriginalPartsList(){
        if(parts_list != null && parts_list.size() > 0){
            original_parts_list = new SparseArray<>(parts_list.size());
            for(int i=0 ; i < parts_list.size() ; i++){
                int key = parts_list.keyAt(i);
                original_parts_list.put(key , parts_list.get(key));
            }
        }
    }

    // todo delete when i am sure that update customer's parts is perfect
    /*void printToLog(){
        if(original_parts_list != null && original_parts_list.size() > 0){
            System.out.println("Original:");
            for(int i=0 ; i < original_parts_list.size() ; i++){
                int key = original_parts_list.keyAt(i);
                Part part = original_parts_list.get(key);
                //Log.d("myTestUOriginal" , "\npart_id:"+part.getPart_id()+"\nname:"+part.getName()+"\nquantity:"+part.getQuantity()+"\n");
                System.out.println("\npart_id:"+part.getPart_id()+" name:"+part.getName()+" quantity:"+part.getQuantity()+"\n");
            }
        }
        if(parts_list != null && parts_list.size() > 0){
            System.out.println("\nNewList:");
            for(int i=0 ; i < parts_list.size() ; i++){
                int key = parts_list.keyAt(i);
                Part part = parts_list.get(key);
                //Log.d("myTestUNew" , "\npart_id:"+part.getPart_id()+"\nname:"+part.getName()+"\nquantity:"+part.getQuantity()+"\n");
                System.out.println("\npart_id:"+part.getPart_id()+" name:"+part.getName()+" quantity:"+part.getQuantity()+"\n");
            }
        }
    }*/
}
