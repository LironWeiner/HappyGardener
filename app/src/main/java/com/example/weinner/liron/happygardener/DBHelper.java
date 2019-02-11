package com.example.weinner.liron.happygardener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.reactivex.Single;

/**
 * Created by Liron weinner
 */

public class DBHelper extends SQLiteOpenHelper{
    private static DBHelper dbHelper = null;
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 3; // Change version if there's some problem
    private final WeakReference<Context> context;

    // Database Name
    private static final String DATABASE_NAME = "HappyGardener.db";

    // tables name
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String TABLE_CUSTOMERS_PARTS = "customers_parts";
    private static final String TABLE_PARTS = "parts";
    private static final String TABLE_PARTS_CATEGORIES = "parts_categories";
    private static final String TABLE_NOTIFICATIONS = "notifications";
    // End of tables name

    public static DBHelper getInstance(final Context context){
        if(dbHelper == null){
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    private DBHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = new WeakReference<>(context);
    }

    // Customers Table
    private static final String CREATE_CUSTOMER = "CREATE TABLE "+ TABLE_CUSTOMERS
            + "("
            + " 'id' INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " 'date' TEXT,"
            + " 'name' TEXT,"
            + " 'phone' TEXT,"
            + " 'address' TEXT,"
            + " 'time_estimate' TEXT,"
            + " 'price' TEXT,"
            + " 'payment' TEXT,"
            + " 'price_per_hour' TEXT"
            + " )";
    // END of Customers Table

    // Notifications table
    private static final String CREATE_NOTIFICATIONS = "CREATE TABLE "+ TABLE_NOTIFICATIONS
            + "("
            + " 'id' INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " 'customer_id' INTEGER,"
            + " 'notification_date' TEXT,"
            + " 'repeat' TEXT,"
            + " FOREIGN KEY ('customer_id') REFERENCES 'customers' ('id')"
            + " )";
    // End of Notifications table


    // Customers_parts Table Columns names
    private static final String CREATE_CUSTOMERS_PARTS = "CREATE TABLE "+ TABLE_CUSTOMERS_PARTS
            + "("
            + " 'id' INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " 'customer_id' INTEGER,"
            + " 'part_id' INTEGER,"
            + " 'quantity' TEXT,"
            + " FOREIGN KEY ('customer_id') REFERENCES 'customers' ('id'),"
            + " FOREIGN KEY ('part_id') REFERENCES 'parts' ('id')"
            + ")";
    // END of Customers_parts Table


    // Parts Table Columns names
    private static final String CREATE_PARTS = "CREATE TABLE "+ TABLE_PARTS
            + "("
            + " 'id' INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " 'name' TEXT,"
            + " 'price' TEXT "
            + ")";
    // END of Parts Table Columns names

    // Parts_categories Table Columns names
    private static final String CREATE_PARTS_CATEGORIES = "CREATE TABLE "+ TABLE_PARTS_CATEGORIES
            + "("
            + " 'id' INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " 'part_id' INTEGER,"
            + " 'category' TEXT,"
            + " FOREIGN KEY('part_id') REFERENCES 'parts'('id') "
            + ")";
    // END of Parts_categories Table Columns names

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creating required tables
        db.execSQL(CREATE_PARTS);
        db.execSQL(CREATE_PARTS_CATEGORIES);
        db.execSQL(CREATE_CUSTOMER);
        db.execSQL(CREATE_CUSTOMERS_PARTS);
        db.execSQL(CREATE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_CUSTOMERS_PARTS);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_PARTS_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_PARTS);
        // Create tables again
        onCreate(db);
    }

    public static String getDBName() {
        return DATABASE_NAME;
    }

    //----------------------------------------------------------|
    // CRUD operations: Create, Read, Update, Delete Operations |
    //----------------------------------------------------------|


    /**
     * Adding new Customer to the customers table
     * @param customer The customer to add to the table
     */
    public void addCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", customer.getName());
        if(customer.getPhone() != null && !customer.getPhone().isEmpty()) values.put("phone" , customer.getPhone());
        values.put("address" , customer.getAddress());
        values.put("date" , customer.getDate());
        if(customer.getTime_estimate() != null && !customer.getTime_estimate().isEmpty()) values.put("time_estimate" , customer.getTime_estimate());
        values.put("price" , customer.getPrice());
        values.put("payment" , customer.getPayment());
        values.put("price_per_hour" , customer.getPrice_per_hour());
        // Inserting row
        db.insert(TABLE_CUSTOMERS, null, values);

        db.close();
    }

    /**
     * Add a notification to the notifications table
     * @param notification a Notification object must not be null
     */
    public void addNotification(final Notification notification){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("customer_id" , notification.getCustomer_id());
        values.put("notification_date" , notification.getNotification_date());
        values.put("repeat" , notification.getRepeat());
        // Inserting row
        db.insert(TABLE_NOTIFICATIONS , null , values);

        db.close();
    }



    /**
     * Adding a new customer part to the customer_parts table
     * @param customer_part The part the needs to be added to the table
     * @param customer_id The customer's id
     */
    public void addCustomerPart(Part customer_part , int customer_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("customer_id" , customer_id);
        values.put("part_id" , customer_part.getPart_id());
        values.put("quantity" , customer_part.getQuantity());

        // Inserting Row
        db.insert(TABLE_CUSTOMERS_PARTS, null, values);
        db.close();
    }

    /**
     * Add a part to the parts table and then adds it to the parts_categories table
     * @param part The part to add must have a non null category object filled with data
     */
    public void addPart(Part part){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues parts_table_values = new ContentValues();
        parts_table_values.put("name" , part.getName());
        parts_table_values.put("price" , part.getPrice());
        // Inserting row to the parts table first
        db.insert(TABLE_PARTS , null , parts_table_values);

        String selectQuery =  " SELECT  seq FROM sqlite_sequence "
                + " WHERE name = 'parts' " ;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null)
            cursor.moveToFirst();

        ContentValues parts_categories_table_values = new ContentValues();
        parts_categories_table_values.put("part_id" , cursor.getString(0));
        parts_categories_table_values.put("category" , part.getCategory().getCategory());
        // Inserting row to the parts_categories table
        db.insert(TABLE_PARTS_CATEGORIES , null , parts_categories_table_values);

        db.close();
    }

    /**
     * Getting single customer
     *@param id The customer's id
     */
    public Customer getCustomer(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_CUSTOMERS + " WHERE "
                + "id =? ";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});

        if (cursor != null)
            cursor.moveToFirst();

        Customer customer = new Customer(
                Integer.parseInt(cursor.getString(0)) ,
                cursor.getString(1) ,
                cursor.getString(2) ,
                cursor.getString(3) ,
                cursor.getString(4) ,
                cursor.getString(5) ,
                cursor.getString(6) ,
                cursor.getString(7) ,
                cursor.getString(8)
        );

        db.close();
        // Return customer
        return customer;
    }

    /**
     * Get a notification by a customer's id
     * @param customer_id The customer
     * @return The customer's notification
     */
    public Notification getNotification(int customer_id){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery =   " SELECT customer_id , notification_date , repeat "
                + " FROM notifications "
                + " WHERE customer_id =? ";

        Cursor cursor = db.rawQuery(selectQuery , new String[]{String.valueOf(customer_id)});
        Notification notification = null;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            notification = new Notification(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2)
            );
        }

        db.close();
        // Return notification
        return notification;
    }

    /**
     * Get all the notifications
     * @return ArrayList of notifications
     */
    public ArrayList<Notification> getAllNotifications(){
        ArrayList<Notification> notificationsList = new ArrayList<>();

        // Select All Query
        String selectQuery =   " SELECT customer_id , notification_date , repeat, customers.name"
                + " FROM notifications , customers"
                + " WHERE notifications.customer_id = customers.id"
                ;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery , null);

        // looping through all rows and adding to list
        if(cursor!=null && cursor.getCount() > 0)
        {
            if (cursor.moveToFirst()) {
                do {
                    Notification notification = new Notification(
                            Integer.parseInt(cursor.getString(0)),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3)
                    );
                    notificationsList.add(notification);
                } while (cursor.moveToNext());
            }
        }

        db.close();

        return notificationsList;
    }


    /**
     * Getting all the customer parts
     * @param id The customer's id
     * @return all the customer's parts
     */
    public SparseArray<Part> getAllCustomerParts(int id) {
        SparseArray<Part> partsList = new SparseArray<>();
        // Select All Query

        String selectQuery = "SELECT parts.id , parts.name , parts.price , parts_categories.category , customers_parts.quantity"
                + " FROM parts , parts_categories , customers_parts , customers "
                + " WHERE customers.id =? "
                + " AND customers_parts.customer_id = customers.id "
                + " AND parts.id = customers_parts.part_id "
                + " AND parts.id = parts_categories.part_id "
                + " ORDER BY parts_categories.category ";


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery , new String[]{String.valueOf(id)});

        // looping through all rows and adding to list
        if(cursor!=null && cursor.getCount() > 0)
        {
            if (cursor.moveToFirst()) {
                do {
                    Part part = new Part();
                    part.setPart_id(Integer.parseInt(cursor.getString(0)));
                    part.setName(cursor.getString(1));
                    part.setPrice(cursor.getString(2));
                    part.setCategory(new Category(cursor.getString(3)));
                    part.setQuantity(cursor.getString(4));
                    // Adding a part to the list
                    partsList.put(part.getPart_id(),part);
                } while (cursor.moveToNext());
            }
        }

        db.close();
        // return a customers parts list
        return partsList;
    }


    /**
     * Getting all the customers for the adapter
     * @return List of all the customers
     */
    public Single<ArrayList<Customer>> getAllCustomers() {
        return Single.create(
                emitter -> {
                    Cursor cursor = null;
                    try {
                        ArrayList<Customer> customersList = new ArrayList<>();

                        SQLiteDatabase db = this.getReadableDatabase();
                        cursor = db.query(TABLE_CUSTOMERS, new String[]{"id , date , name , phone , address"}, null, null,
                                null, null, "datetime(date) ASC");

                        // looping through all rows and adding to list
                        if (cursor != null && cursor.getCount() > 0) {
                            if (cursor.moveToFirst()) {
                                do {
                                    Customer customer = new Customer();
                                    customer.setId(Integer.parseInt(cursor.getString(0)));
                                    customer.setDate(cursor.getString(1));
                                    customer.setName(cursor.getString(2));
                                    customer.setPhone(cursor.getString(3));
                                    customer.setAddress(cursor.getString(4));
                                    // Adding a customer to the list
                                    customersList.add(customer);
                                } while (cursor.moveToNext());
                            }
                        }
                        db.close();
                        emitter.onSuccess(customersList);
                    }
                    catch (Exception e){
                        if(cursor != null){
                            cursor.close();
                        }
                        emitter.onError(e);
                    }
                });
    }


    /**
     * Getting all the customers that contains name for the adapter
     * @param name String name a string containing customer name
     * @param isDate boolean flag if true name is a date else its a name
     * @return List of all the customers
     */
    public Single<ArrayList<Customer>> getAllCustomersByName(final String name , boolean isDate) {
        return Single.create(
                emitter -> {
                    Cursor cursor = null;
                    try {
                        ArrayList<Customer> customersList = new ArrayList<>();

                        SQLiteDatabase db = this.getReadableDatabase();
                        String query;

                        if (!isDate) {
                            query = " SELECT id , date , name , address , phone "
                                    + " FROM customers "
                                    + " WHERE name LIKE ? "
                                    + " ORDER BY datetime(date) ";
                            cursor = db.rawQuery(query, new String[]{'%' + name + '%'});
                        } else {
                            query = " SELECT id , date , name , address , phone "
                                    + " FROM customers "
                                    + " WHERE date LIKE ? OR date Like ? AND length(date) = 10"
                                    + " ORDER BY datetime(date) ";
                            cursor = db.rawQuery(query, new String[]{'%' + name + "%____", '%' + name + '%'});
                        }


                        // looping through all rows and adding to list
                        if (cursor != null && cursor.getCount() > 0) {
                            if (cursor.moveToFirst()) {
                                do {
                                    Customer customer = new Customer();
                                    customer.setId(Integer.parseInt(cursor.getString(0)));
                                    customer.setDate(cursor.getString(1));
                                    customer.setName(cursor.getString(2));
                                    customer.setPhone(cursor.getString(3));
                                    customer.setAddress(cursor.getString(4));
                                    // Adding a customer to the list
                                    customersList.add(customer);
                                } while (cursor.moveToNext());
                            }
                        }
                        db.close();
                        emitter.onSuccess(customersList);
                    }
                    catch (Exception e){
                        if(cursor != null){
                            cursor.close();
                        }
                        emitter.onError(e);
                    }
                });
    }

    /**
     * Get all the parts that belongs to category
     * @param category The category to select parts from must not be null
     * @return ArrayList of parts
     */
    public Single<ArrayList<Part>> getPartsByCategory(String category)
    {
        return Single.create(
                emitter -> {
                    Cursor cursor = null;
                    try {
                        ArrayList<Part> partsList = new ArrayList<>();

                        SQLiteDatabase db = this.getReadableDatabase();

                        String selectQuery = "SELECT parts.id , parts_categories.id , parts.name , parts.price "
                                + " FROM parts_categories , parts"
                                + " WHERE parts.id = parts_categories.part_id"
                                + " AND parts_categories.category =? "
                                + " ORDER BY parts.name";

                        cursor = db.rawQuery(selectQuery, new String[]{category});

                        // looping through all rows and adding to list
                        if (cursor != null && cursor.getCount() > 0) {
                            if (cursor.moveToFirst()) {
                                do {
                                    Part part = new Part();
                                    part.setPart_id(Integer.valueOf(cursor.getString(0)));
                                    part.setId(Integer.valueOf(cursor.getString(1)));
                                    part.setName(cursor.getString(2));
                                    part.setPrice(cursor.getString(3));
                                    // Adding a part to the list
                                    partsList.add(part);
                                } while (cursor.moveToNext());
                            }
                        }
                        db.close();
                        emitter.onSuccess(partsList);
                    }
                    catch (Exception e){
                        if(cursor != null){
                            cursor.close();
                        }
                        emitter.onError(e);
                    }
                });
    }

    /**
     * Get all the parts that belongs to the category that contains part_name
     * @param category The category to select parts from must not be null
     * @param part_name The part name
     * @return ArrayList of parts
     */
    public Single<ArrayList<Part>> getPartsByCategoryAndName(final String category , final String part_name)
    {
        return Single.create(
                emitter -> {
                    Cursor cursor = null;
                    try {
                        ArrayList<Part> partsList = new ArrayList<>();

                        SQLiteDatabase db = this.getReadableDatabase();

                        String selectQuery = "SELECT parts.id , parts_categories.id , parts.name , parts.price "
                                + " FROM parts_categories , parts"
                                + " WHERE parts.id = parts_categories.part_id "
                                + " AND parts_categories.category =? AND parts.name LIKE ? "
                                + " ORDER BY parts.name";

                        cursor = db.rawQuery(selectQuery, new String[]{category, '%' + part_name + '%'});

                        // looping through all rows and adding to list
                        if (cursor != null && cursor.getCount() > 0) {
                            if (cursor.moveToFirst()) {
                                do {
                                    Part part = new Part();
                                    part.setPart_id(Integer.valueOf(cursor.getString(0)));
                                    part.setId(Integer.valueOf(cursor.getString(1)));
                                    part.setName(cursor.getString(2));
                                    part.setPrice(cursor.getString(3));
                                    // Adding a part to the list
                                    partsList.add(part);
                                } while (cursor.moveToNext());
                            }
                        }
                        db.close();
                        emitter.onSuccess(partsList);
                    }
                    catch (Exception e){
                        if(cursor != null){
                            cursor.close();
                        }
                        emitter.onError(e);
                    }
                });
    }

    /**
     * Get a part by its id
     * @param id The parts id
     * @return The part
     */
    public Part getPartById(int id){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery =  " SELECT  parts.id , parts.name , parts.price , parts_categories.category "
                + " FROM parts , parts_categories"
                + " WHERE parts.id =? AND parts.id = parts_categories.part_id " ;


        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});

        if (cursor != null)
            cursor.moveToFirst();

        Part part = new Part();
        part.setPart_id(Integer.parseInt(cursor.getString(0)));
        part.setName(cursor.getString(1));
        part.setPrice(cursor.getString(2));
        part.setCategory(new Category(cursor.getString(3)));

        db.close();
        // return part
        return part;
    }

    /**
     * Getting last customers's auto generated id for adding customer's parts
     * @return Last generated id (Auto incrementation)
     */
    public int getCustomerId() {

        String countQuery =   " SELECT seq FROM sqlite_sequence "
                + " WHERE name = 'customers' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null)
            cursor.moveToLast();

        int id = cursor.getInt(0);
        db.close();
        return id;
    }

    /**
     * Getting last part's auto generated id for AddPartActivity
     * @return Last generated id (Auto incrementation)
     */
    public int getPartId() {

        String countQuery =   " SELECT seq FROM sqlite_sequence "
                + " WHERE name = 'parts' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null)
            cursor.moveToLast();

        int id = cursor.getInt(0);
        db.close();
        return id;
    }


    public boolean isDatabaseNew(){
        String query = "SELECT seq FROM sqlite_sequence "
                + " WHERE seq > 0 ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToLast();
            db.close();
            return false;
        }
        db.close();
        return true;
    }

    /**
     * Check if the notifications table is empty
     * @return true if empty and false otherwise
     */
    public boolean isNotificationsEmpty(){
        String countQuery =  " SELECT id FROM notifications "
                +" LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        // If not empty
        if (cursor != null && cursor.getCount() == 0) {
            db.close();
            return true;
        }
        // If empty
        db.close();
        return false;
    }

    /**
     * Updating single customer in the customers table
     * @param customer The customer to update
     * @return Number of rows affected
     */
    public int updateCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", customer.getName());
        if(customer.getPhone() != null && !customer.getPhone().isEmpty()) values.put("phone" , customer.getPhone());
        values.put("address" , customer.getAddress());
        values.put("date" , customer.getDate());
        if(customer.getTime_estimate() != null && !customer.getTime_estimate().isEmpty()) values.put("time_estimate" , customer.getTime_estimate());
        values.put("price" , customer.getPrice());
        values.put("payment" , customer.getPayment());
        values.put("price_per_hour" , customer.getPrice_per_hour());

        // updating row
        final int number_of_rows_affected = db.update(TABLE_CUSTOMERS, values, "id = ?",
                new String[]{String.valueOf(customer.getId())});

        db.close();

        return number_of_rows_affected ;
    }


    /**
     * Updating a single notification in the notifications table
     * @param notification The notification to update
     */
    public void updateNotification(Notification notification){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("customer_id" , notification.getCustomer_id());
        values.put("notification_date" , notification.getNotification_date());
        values.put("repeat" , notification.getRepeat());

        db.update(TABLE_NOTIFICATIONS , values , "customer_id = ?",
                new String[]{String.valueOf(notification.getCustomer_id())});

        db.close();
    }


    /**
     * Update the customer's part in the customer_parts table
     * @param customer_id The customer's id
     * @param old_part_id The old part's id that we will update
     * @param part The updated part
     */
    public void updateCustomerPart(int customer_id ,int old_part_id , Part part){
        String part_id = String.valueOf(part.getPart_id());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("part_id" , part_id); // Insert the new part's id
        values.put("quantity" , part.getQuantity()); // Insert the new quantity

        // Update the row with the same old parts id to the new part's id
        db.update(TABLE_CUSTOMERS_PARTS , values , "customer_id =? AND part_id =?" ,
                new String[]{String.valueOf(customer_id) , String.valueOf(old_part_id) });

        db.close();
    }

    /**
     * Update a part
     * @param part The part to update
     */
    public void updatePart(Part part){
        String part_id = String.valueOf(part.getPart_id());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name" , part.getName()); // Insert the new part's name
        values.put("price" , part.getPrice()); // Insert the new price

        db.update(TABLE_PARTS , values , "id =?" ,
                new String[]{String.valueOf(part_id) });
        db.close();
    }

    /**
     * Deleting a single Customer from the customers table
     * and from the customers_parts table
     * and delete the customer's notification from notifications table
     * @param id The customer's id
     */
    public void deleteCustomer(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // first delete the row/s that equals to id from the customers_parts table
        db.delete(TABLE_CUSTOMERS_PARTS, "customer_id = ?",
                new String[]{String.valueOf(id)});
        // Second delete the notification of that customer if there is one
        deleteNotification(id);
        db = this.getWritableDatabase();
        // Third delete the row of the customer that equals to id from the customer table
        db.delete(TABLE_CUSTOMERS, "id =?" , new String[]{String.valueOf(id)});

        db.close();
    }

    /**
     * Deleting a single notification from the notifications table by a customer's id
     * @param customer_id The customer's id
     */
    public void deleteNotification(int customer_id){
        SQLiteDatabase db = this.getWritableDatabase();
        int number_of_rows = db.delete(TABLE_NOTIFICATIONS , "customer_id = ?",
                new String[]{String.valueOf(customer_id)});
        // If a notification has been deleted
        if(number_of_rows > 0){
            new MessageNotification().cancel(context.get() , customer_id);
        }
        db.close();
    }

    /**
     * Delete a customer's part from the customers_parts table
     * @param customer_id The customer's id
     * @param part_id The parts id
     */
    public void deleteCustomerPart(int customer_id , int part_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_CUSTOMERS_PARTS, " customer_id =?  and part_id =? ",
                new String[]{String.valueOf(customer_id) , String.valueOf(part_id) });

        db.close();
    }

    /**
     * Delete a part from the parts , parts_categories IF AND ONLY IF the part is not in used in the customers_parts table
     * @param id The part's id
     * @return True if the part was deleted successfully and false if not deleted and in used
     */
    public final String deletePart(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        /*String query = "SELECT part_id"
                + " FROM customers_parts "
                + " WHERE part_id = ? ";*/

        String query = "SELECT customers.name"
                +" FROM parts , customers_parts , customers"
                +" WHERE customers_parts.customer_id = customers.id"
                +" AND parts.id =?"
                +" AND parts.id = customers_parts.part_id"
                +" ORDER BY parts.name"
                +" LIMIT 1";

        Cursor cursor = db.rawQuery(query , new String[]{String.valueOf(id)});
        if(cursor != null && cursor.getCount() > 0) { // If not null then the part is in used
            cursor.moveToFirst();
            return cursor.getString(0);
        }

        // If the part is not in used then it can be deleted
        db.delete(TABLE_PARTS_CATEGORIES, " part_id = ? ",
                new String[]{String.valueOf(id)});
        db.delete(TABLE_PARTS , " id = ? ",
                new String[]{String.valueOf(id)});

        db.close();
        return null;
    }
    //________________________________________________________________
    //                                                                |
    // END OF CRUD operations: Create, Read, Update, Delete Operations |
    //________________________________________________________________|
}
