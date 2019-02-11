package com.example.weinner.liron.happygardener;

import android.util.SparseBooleanArray;

import java.util.List;

/**
 * Created by Liron weinner
 */
class CheckedItems {
    private static CheckedItems ourInstance = null;
    public SparseBooleanArray checkedItems = null ;
    public List<Integer> checked_indexes = null ;
    public boolean isVisible = false;
    public boolean chooseMode = false;
    public boolean UpdateCustomerMode = false;

    static CheckedItems getInstance()
    {
        if(ourInstance == null){
            ourInstance = new CheckedItems();
        }
        return ourInstance;
    }

    private CheckedItems() {
    }

    public boolean isEmpty()
    {
        return checkedItems == null || checkedItems.size() <= 0;
    }
}
