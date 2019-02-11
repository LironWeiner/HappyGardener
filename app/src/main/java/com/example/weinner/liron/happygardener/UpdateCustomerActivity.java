package com.example.weinner.liron.happygardener;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.weinner.liron.happygardener.databinding.ActivityAddCustomerBinding;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.Calendar;
import java.util.Locale;

public class UpdateCustomerActivity extends AppCompatActivity {

    private static final int WORK_HOURS_PER_DAY = 8;
    // Google place picker request code
    private final int PLACE_PICKER_REQUEST = 1;

    private ArrayAdapter<CharSequence> paymentAdapter;

    private Customer customer ;
    private Notification notification ;

    private int mHour;
    private int mMinute;
    private DatePickerDialog datePickerDialog; // for picking a date from the calendar
    private boolean isNotificationEmpty = true;
    private boolean original_notification_empty = true;
    private boolean enter_customer_parts_activity = false;

    // Job price details
    private double parts_price ;
    private String time_option ;
    private double price_before_discount;
    private String discount_option;
    private String final_price_after_discount;

    private int customer_id;
    private ActivityAddCustomerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this , R.layout.activity_add_customer);

        binding.buttonCustomerCreate.setText(R.string.update_customer);
        CheckedItems.getInstance().UpdateCustomerMode = true;
        // Init customer , notification , customer parts
        customer_id = getIntent().getIntExtra("customer_id" , 0);
        if(customer_id == 0){
            customer_id = Integer.parseInt(getIntent().getStringExtra("id"));
        }
        customer = DBHelper.getInstance(UpdateCustomerActivity.this).getCustomer(customer_id);
        notification = DBHelper.getInstance(UpdateCustomerActivity.this).getNotification(customer_id);

        binding.editTextCustomerName.setText(customer.getName());
        binding.editTextCustomerPhone.setText(customer.getPhone());
        binding.editTextCustomerAddress.setText(customer.getAddress());
        binding.editTextCustomerDate.setText(customer.getDate());

        // If the current customer doesn't have a notification
        if(notification == null) {
            notification = new Notification();
            notification.setRepeat(String.valueOf(R.id.radioButton_none));
            binding.radioButtonCustomerRepeat.check(R.id.radioButton_none);
        }
        // Fill all the notification related data
        else if(notification != null){
            isNotificationEmpty = false;
            original_notification_empty = false;
            binding.editTextCustomerNotification.setText(notification.getNotification_date());
            binding.clearNotification.setVisibility(View.VISIBLE);
            if(notification.getRepeat() != null){
                binding.radioButtonCustomerRepeat.check(Integer.parseInt(notification.getRepeat()));
            }
        }

        binding.editTextCustomerPricePerHour.setText(customer.getPrice_per_hour());
        if(customer.getTime_estimate() != null) {
            if(customer.getTime_estimate().contains("h")){
                int sign_index = customer.getTime_estimate().indexOf('h');
                String time_number = customer.getTime_estimate().substring(0 , sign_index);
                binding.editTextCustomerTimeEstimate.setText(time_number);
                binding.radioButtonCustomerTimeEstimate.check(R.id.radioButton_hours);
                customer.setTime_estimate(time_number);
            }
            else if(customer.getTime_estimate().contains("d")){
                int sign_index = customer.getTime_estimate().indexOf('d');
                String time_number = customer.getTime_estimate().substring(0 , sign_index);
                binding.editTextCustomerTimeEstimate.setText(time_number);
                binding.radioButtonCustomerTimeEstimate.check(R.id.radioButton_days);
                customer.setTime_estimate(time_number);
            }
        }

        // Init the parts price if this customer got any parts
        if(PartsHolder.getInstance().parts_list == null) {
            PartsHolder.getInstance().parts_list = DBHelper.getInstance(getApplicationContext())
                    .getAllCustomerParts(customer_id);

            if(PartsHolder.getInstance().parts_list != null){
                PartsHolder.getInstance().parts_list_changed = true;
                updatePartsPrice();
            }
        }



        binding.editTextCustomerFinalPrice.setText(customer.getPrice());

        time_option = String.valueOf(binding.radioButtonCustomerTimeEstimate.getCheckedRadioButtonId());
        discount_option = String.valueOf(binding.radioButtonCustomerDiscount.getCheckedRadioButtonId());


        // Create an ArrayAdapter using the string array and a default spinner layout
        paymentAdapter = ArrayAdapter.createFromResource(this,
                R.array.payment_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        binding.spinnerCustomerPayment.setAdapter(paymentAdapter);
        if(customer.getPayment() != null){
            binding.spinnerCustomerPayment.setSelection(paymentAdapter.getPosition(customer.getPayment()));
        }

        binding.radioButtonCustomerRepeat.setOnCheckedChangeListener(onCheckedChangeListenerRepeat);
        binding.radioButtonCustomerTimeEstimate.setOnCheckedChangeListener(onCheckedChangeListenerTimeOption);
        binding.radioButtonCustomerDiscount.setOnCheckedChangeListener(onCheckedChangeListenerDiscount);

        binding.spinnerCustomerPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                customer.setPayment(item);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        binding.editTextCustomerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                customer.setName(editable.toString());
            }
        });


        binding.editTextCustomerPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                customer.setPhone(editable.toString());
            }
        });

        binding.imageButtonCustomerDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editTextCustomerDate.setError(null);
                getDate();
            }
        });

        binding.clearNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editTextCustomerNotification.setText(null);
                notification.setNotification_date(null);
                binding.clearNotification.setVisibility(View.GONE);
            }
        });

        binding.imageButtonCustomerNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editTextCustomerNotification.setError(null);
                getDateNotification();
            }
        });

        binding.imageButtonCustomerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(UpdateCustomerActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        binding.editTextCustomerPricePerHour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                customer.setPrice_per_hour(editable.toString());
                updatePriceBeforeDiscount();
            }
        });

        binding.editTextCustomerTimeEstimate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                customer.setTime_estimate(editable.toString());
                updatePriceBeforeDiscount();
            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckedItems.getInstance().chooseMode = true;
                enter_customer_parts_activity = true;
                final Intent i = new Intent(getApplicationContext() , CustomerParts.class);
                i.putExtra("context","UpdateCustomerActivity");
                i.putExtra("customer_id" , customer_id);
                startActivity(i , ActivityOptions.makeSceneTransitionAnimation(
                        UpdateCustomerActivity.this
                        , binding.customerPartsLayout
                        , "partTransition"
                ).toBundle());
            }
        });

        binding.editTextCustomerFinalPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                customer.setPrice(editable.toString());
                binding.radioButtonCustomerDiscount.check(R.id.radioButton_none_discount);
                final_price_after_discount = null;
                binding.editTextCustomerFinalPriceAfterDiscount.setText(null);
            }
        });

        binding.imageButtonSendBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If final price after discount is not null
                String format_message = null;
                if(final_price_after_discount != null && !final_price_after_discount.isEmpty()){
                    format_message ="מחיר סופי: "+binding.editTextCustomerFinalPrice.getText().toString()+" שקלים\n"
                            +"מחיר לאחר הנחה: "+binding.editTextCustomerFinalPriceAfterDiscount.getText().toString()+" שקלים ";
                }
                // If not discount... only final price
                else if(customer.getPrice() != null && !customer.getPrice().isEmpty()) {
                    format_message ="מחיר סופי: "+binding.editTextCustomerFinalPrice.getText().toString()+" שקלים\n";
                }
                else{
                    Toast.makeText(getApplicationContext() , R.string.send_bill_toast, Toast.LENGTH_SHORT).show();
                }

                if(format_message != null){
                    if(PartsHolder.getInstance().parts_list != null && PartsHolder.getInstance().parts_list.size() > 0){
                        String format_parts_message = "הצעת מחיר עבור עבודת גינון של טל הגנן:\nרשימת חלקים:\n";
                        for(int i=0 ; i < PartsHolder.getInstance().parts_list.size() ; i++){
                            int key = PartsHolder.getInstance().parts_list.keyAt(i);
                            Part part = PartsHolder.getInstance().parts_list.get(key);
                            format_parts_message += "שם חלק: "+part.getName()+" כמות: "+part.getQuantity()+"\n";
                        }

                        /*for(Part part:PartsHolder.getInstance().parts_list){
                            format_parts_message += "שם חלק: "+part.getName()+" כמות: "+part.getQuantity()+"\n";
                        }*/
                        format_message = format_parts_message+format_message;
                        sendInvoiceIntent(format_message);
                    }
                    else {
                        sendInvoiceIntent("הצעת מחיר עבור עבודת גינון של טל הגנן:\n"+format_message);
                    }
                }
            }
        });

        binding.buttonCustomerCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open a dialog that asks the user if he/she are sure about updating the current customer
                openCustomerUpdateDialog();
            }
        });

    }


    private void saveActivityData() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("name", customer.getName());
        editor.putString("phone", customer.getPhone());
        editor.putString("payment", customer.getPayment());
        editor.putString("address", customer.getAddress());
        editor.putString("date", customer.getDate());
        editor.putString("notification_date", notification.getNotification_date());
        editor.putString("repeat" , notification.getRepeat());
        editor.putString("price_per_hour" , customer.getPrice_per_hour());
        editor.putString("time_estimate" , customer.getTime_estimate());
        editor.putString("time_option" , time_option);
        editor.putString("discount_option" , discount_option);
        editor.putString("final_price" , customer.getPrice());
        editor.putString("final_price_after_discount",final_price_after_discount);
        editor.putBoolean("enter_customer_parts_activity" , enter_customer_parts_activity);
        editor.putBoolean("original_notification",original_notification_empty);

        editor.apply();
    }

    private void restoreActivityData() {
        if(CheckedItems.getInstance().chooseMode)
        {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            customer.setName(sharedPref.getString("name",null));
            customer.setPhone(sharedPref.getString("phone",null));
            customer.setPayment(sharedPref.getString("payment",null));
            customer.setAddress(sharedPref.getString("address",null));
            customer.setDate(sharedPref.getString("date",null));
            notification.setNotification_date(sharedPref.getString("notification_date",null));
            notification.setRepeat(sharedPref.getString("repeat" , null));
            parts_price = Double.valueOf(sharedPref.getString("parts_price" , null));
            customer.setPrice_per_hour(sharedPref.getString("price_per_hour" , null));
            customer.setTime_estimate(sharedPref.getString("time_estimate" , null));
            time_option = sharedPref.getString("time_option" , null);
            String temp_discount_option = discount_option = sharedPref.getString("discount_option" , null);
            customer.setPrice(sharedPref.getString("final_price" , null));
            String temp_final_price_after_discount = final_price_after_discount = sharedPref.getString("final_price_after_discount" , null);
            if(notification.getNotification_date()!= null)
                binding.clearNotification.setVisibility(View.VISIBLE);

            binding.editTextCustomerName.setText(customer.getName());
            binding.spinnerCustomerPayment.setSelection(paymentAdapter.getPosition(customer.getPayment()));
            binding.editTextCustomerPhone.setText(customer.getPhone());
            binding.editTextCustomerAddress.setText(customer.getAddress());
            binding.editTextCustomerDate.setText(customer.getDate());
            binding.editTextCustomerNotification.setText(notification.getNotification_date());
            binding.radioButtonCustomerRepeat.check(Integer.parseInt(sharedPref.getString("repeat" , null)));
            binding.editTextCustomerPartsPrice.setText(Utility.setRoundedNumber(String.valueOf(parts_price)));
            binding.editTextCustomerPricePerHour.setText(customer.getPrice_per_hour());
            binding.editTextCustomerTimeEstimate.setText(customer.getTime_estimate());
            binding.radioButtonCustomerTimeEstimate.check(Integer.parseInt(time_option));
            binding.editTextCustomerFinalPrice.setText(customer.getPrice());
            binding.radioButtonCustomerDiscount.check(Integer.parseInt(temp_discount_option));
            discount_option = temp_discount_option;
            binding.editTextCustomerFinalPriceAfterDiscount.setText(temp_final_price_after_discount);

            enter_customer_parts_activity = sharedPref.getBoolean("enter_customer_parts_activity" , false);
            original_notification_empty = sharedPref.getBoolean("original_notification" , true);
        }
    }

    private void getDateNotification() {
        // calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR); // current year
        int mMonth = c.get(Calendar.MONTH); // current month
        int mDay = c.get(Calendar.DAY_OF_MONTH); // current day

        // date picker dialog
        datePickerDialog = new DatePickerDialog(UpdateCustomerActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String formated_yyyy_mm_dd = String.format(Locale.getDefault(),"%04d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth);
                        binding.editTextCustomerNotification.setText(formated_yyyy_mm_dd);
                        notification.setNotification_date(formated_yyyy_mm_dd);
                        ////////////
                        timePickerNotification();
                        ////////////
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void getDate() {
        // calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR); // current year
        int mMonth = c.get(Calendar.MONTH); // current month
        int mDay = c.get(Calendar.DAY_OF_MONTH); // current day

        // date picker dialog
        datePickerDialog = new DatePickerDialog(UpdateCustomerActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String formated_yyyy_mm_dd = String.format(Locale.getDefault(),"%04d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth);
                        binding.editTextCustomerDate.setText(formated_yyyy_mm_dd);
                        customer.setDate(formated_yyyy_mm_dd);
                        ////////////
                        timePicker();
                        ////////////
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }


    private void timePicker(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;

                        String formated_hour_minute = String.format(Locale.getDefault(),"%02d:%02d",hourOfDay,minute); // Should be hh:mm format
                        binding.editTextCustomerDate.append(" "+formated_hour_minute);

                        customer.setDate(String.valueOf(binding.editTextCustomerDate.getText()));
                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    private void timePickerNotification(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        binding.clearNotification.setVisibility(View.VISIBLE);
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;

                        String formated_hour_minute = String.format(Locale.getDefault(),"%02d:%02d",hourOfDay,minute); // Should be hh:mm format
                        binding.editTextCustomerNotification.append(" "+formated_hour_minute);

                        notification.setNotification_date(String.valueOf(binding.editTextCustomerNotification.getText()));
                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    // when returning from the google maps place picking we need to save the data
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                final EditText customer_address = findViewById(R.id.editText_customer_address);
                //Place place = PlacePicker.getPlace(data, this);
                Place place = PlacePicker.getPlace(UpdateCustomerActivity.this , data);
                customer.setAddress(place.getAddress().toString());
                customer_address.setText(place.getAddress().toString());
                String toastMsg = String.format("Place: %s", place.getAddress());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }

    }

    private void sendInvoiceIntent(String format_message) {
        Intent send_invoice_intent = new Intent();
        send_invoice_intent.setAction(Intent.ACTION_SEND);
        send_invoice_intent.setType("text/plain");
        send_invoice_intent.putExtra(Intent.EXTRA_TEXT, format_message);
        startActivity(Intent.createChooser(send_invoice_intent, "שלח חשבון ללקוח"));
    }

    @Override
    public void onBackPressed() {
        openDialog();
    }

    protected void onStart() {
        restoreActivityData();
        updatePartsPrice();
        updatePriceBeforeDiscount();
        super.onStart();
    }

    @Override
    protected void onStop() {
        saveActivityData();
        super.onStop();
    }

    /**
     * Update the price before discount field if the user filled the required fields
     */
    private void updatePriceBeforeDiscount() {
        // Checks if user added parts
        if(parts_price > 0 ){
            // Checks if user added a valid price_per_hour number
            if(customer.getPrice_per_hour() == null || customer.getPrice_per_hour().isEmpty()){
                price_before_discount = 0;
                binding.customerDiscountLayout.setVisibility(View.GONE);
                binding.editTextCustomerPriceBeforeDiscount.setText(null);
            }
            else if(customer.getPrice_per_hour() != null && !customer.getPrice_per_hour().isEmpty()
                    && customer.getPrice_per_hour().charAt(0) == '.'){
                price_before_discount = 0;
                binding.customerDiscountLayout.setVisibility(View.GONE);
                binding.editTextCustomerPricePerHour.setHint(R.string.enter_hourly_rate);
            }
            else
            {
                // Check if the price value is a valid number
                try {
                    final double price_per_hour = Double.parseDouble(customer.getPrice_per_hour());
                    int point_index = customer.getPrice_per_hour().indexOf('.');
                    if(customer.getPrice_per_hour().length()-1 == point_index) {
                        price_before_discount = 0;
                        binding.customerDiscountLayout.setVisibility(View.GONE);
                        binding.editTextCustomerPricePerHour.setHint(R.string.enter_hourly_rate);
                    }
                    else if(price_per_hour <= 0) {
                        price_before_discount = 0;
                        binding.customerDiscountLayout.setVisibility(View.GONE);
                        binding.editTextCustomerPricePerHour.setHint(R.string.enter_hourly_rate);
                    }
                    // If price_per_hour and parts_price are valid then check estimate time
                    else{
                        if(customer.getTime_estimate() == null || customer.getTime_estimate().isEmpty()){
                            price_before_discount = 0;
                            binding.customerDiscountLayout.setVisibility(View.GONE);
                            binding.editTextCustomerPriceBeforeDiscount.setText(null);

                        }
                        else if(customer.getTime_estimate() != null && !customer.getTime_estimate().isEmpty()
                                && customer.getTime_estimate().charAt(0) == '0'){
                            price_before_discount = 0;
                            binding.customerDiscountLayout.setVisibility(View.GONE);
                            binding.editTextCustomerTimeEstimate.setHint(R.string.hint_estimate_time);
                        }
                        // If everything is correct show the price before discount
                        else{
                            // If the user want to count by days then 8 hours per day * price_per_hour* getTimeEstimate
                            if(R.id.radioButton_days == Integer.valueOf(time_option)){
                                price_before_discount = parts_price + (Double.parseDouble(customer.getTime_estimate())*WORK_HOURS_PER_DAY*price_per_hour);
                            }
                            else if(R.id.radioButton_hours == Integer.valueOf(time_option)){
                                price_before_discount = parts_price + (Double.parseDouble(customer.getTime_estimate()) * price_per_hour);
                            }
                            binding.customerDiscountLayout.setVisibility(View.VISIBLE);
                            binding.editTextCustomerPriceBeforeDiscount.setText(Utility.setRoundedNumber(String.valueOf(price_before_discount)));
                            binding.editTextCustomerFinalPrice.setText(binding.editTextCustomerPriceBeforeDiscount.getText());
                        }
                    }
                }
                catch(NumberFormatException e){
                    price_before_discount = 0;
                    binding.customerDiscountLayout.setVisibility(View.GONE);
                    binding.editTextCustomerPricePerHour.setHint(R.string.enter_hourly_rate);
                }
            }
        }
        else if (parts_price == 0){
            price_before_discount = 0;
            binding.customerDiscountLayout.setVisibility(View.GONE);
            binding.editTextCustomerPriceBeforeDiscount.setText(null);
        }

    }

    private void updatePartsPrice() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(PartsHolder.getInstance().parts_list_changed){
            parts_price = 0;
            for(int i=0 ; i < PartsHolder.getInstance().parts_list.size() ; i++){
                int key = PartsHolder.getInstance().parts_list.keyAt(i);
                Part part = PartsHolder.getInstance().parts_list.get(key);
                int space_index = part.getQuantity().indexOf(" ");
                // If part quantity got a measurement different then unit
                if(space_index != -1){
                    String part_quantity = part.getQuantity().substring(0 , space_index);
                    parts_price+=Double.valueOf(part.getPrice()) * Double.valueOf(part_quantity);
                }
                else{
                    parts_price+=Double.valueOf(part.getPrice()) * Double.valueOf(part.getQuantity());
                }
            }
            binding.editTextCustomerPartsPrice.setText(Utility.setRoundedNumber(String.valueOf(parts_price)));
            PartsHolder.getInstance().parts_list_changed = false;
            editor.putString("parts_price" , String.valueOf(parts_price));
            editor.apply();
        }
        else if(!PartsHolder.getInstance().parts_list_changed){
            editor.putString("parts_price" , String.valueOf(parts_price));
            editor.commit();
        }
    }

    private void openDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UpdateCustomerActivity.this);
        alertDialogBuilder.setMessage(R.string.dialog_add_customer_quit)
                .setPositiveButton(R.string.opendialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        beforeOnDestroy();
                        Intent i = new Intent(UpdateCustomerActivity.this, CustomersActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                })
                .setNegativeButton(R.string.opendialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .setTitle(R.string.dialog_warning)
                .setIcon(R.drawable.ic_warning);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void openCustomerUpdateDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UpdateCustomerActivity.this);
        alertDialogBuilder.setMessage(R.string.dialog_update_customer)
                .setPositiveButton(R.string.opendialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Check Mandatory field
                        // If no customer name then show error
                        if(customer.getName() == null || customer.getName().isEmpty())
                        {
                            binding.scrollView.scrollTo(0 , binding.editTextCustomerName.getScrollY());
                            binding.editTextCustomerName.setError(getString(R.string.toast_empty_name));
                        }
                        // If the repeat is different then none and no notification date and no job date show error
                        else if(Integer.valueOf(notification.getRepeat()) != R.id.radioButton_none
                                && (notification.getNotification_date() == null
                                || customer.getDate() == null)){
                            binding.scrollView.scrollTo(0, binding.editTextCustomerDate.getTop());
                            if(customer.getDate() == null)
                                binding.editTextCustomerDate.setError(getString(R.string.error_customer_date));
                            if(notification.getNotification_date() == null)
                                binding.editTextCustomerNotification.setError(getString(R.string.error_customer_notification));
                        }
                        // If everything is valid
                        else
                        {
                            // If time estimate field is not null append the right char to the time estimate string for id'ing
                            if(customer.getTime_estimate()!= null && !customer.getTime_estimate().isEmpty()){
                                if(R.id.radioButton_days == Integer.valueOf(time_option)){
                                    customer.setTime_estimate(customer.getTime_estimate() + 'd');
                                }
                                else
                                    customer.setTime_estimate(customer.getTime_estimate() + 'h');
                            }
                            // If user entered a discount
                            if(final_price_after_discount != null && !final_price_after_discount.isEmpty()
                                    && Double.parseDouble(final_price_after_discount) > 0){
                                customer.setPrice(final_price_after_discount);
                            }

                            DBHelper.getInstance(UpdateCustomerActivity.this).updateCustomer(customer);
                            if(notification.getNotification_date() != null ){
                                notification.setCustomer_id(customer_id);
                                // If the original data from the table was empty (no notification)
                                if(isNotificationEmpty){
                                    DBHelper.getInstance(getApplicationContext()).addNotification(notification);
                                }
                                // Else update the existing notification
                                else if(!isNotificationEmpty){
                                    DBHelper.getInstance(getApplicationContext()).updateNotification(notification);
                                }
                                // Set/Update notification
                                new MessageNotification().notify(UpdateCustomerActivity.this ,
                                        customer.getName() ,
                                        5 ,
                                        notification.getCustomer_id(),
                                        notification.getNotification_date(),
                                        notification.getRepeat());
                            }
                            else{
                                if(!original_notification_empty){
                                    DBHelper.getInstance(getApplicationContext()).deleteNotification(customer_id);
                                }
                            }


                            /////////////////////
                            //todo delete when finish testing
                            //PartsHolder.getInstance().printToLog();

                            if(enter_customer_parts_activity) {
                                // If the original user's parts list was empty just add the parts to the database
                                if (PartsHolder.getInstance().original_parts_list == null) {
                                    for (Part part : PartsHolder.getInstance().partsList) {
                                        DBHelper.getInstance(UpdateCustomerActivity.this).addCustomerPart(part, customer_id);
                                    }
                                }
                                // If the new list for updating the customer's parts is empty then delete all customer's parts
                                else if (PartsHolder.getInstance().parts_list == null || PartsHolder.getInstance().parts_list.size() == 0
                                        && PartsHolder.getInstance().original_parts_list != null && PartsHolder.getInstance().original_parts_list.size() > 0) {
                                    for (int i = 0; i < PartsHolder.getInstance().original_parts_list.size(); i++) {
                                        int key = PartsHolder.getInstance().original_parts_list.keyAt(i);
                                        Part part = PartsHolder.getInstance().original_parts_list.get(key);
                                        DBHelper.getInstance(UpdateCustomerActivity.this).deleteCustomerPart(customer_id, part.getPart_id());
                                    }
                                }
                                // If the original and the new parts list not empty
                                else if (PartsHolder.getInstance().original_parts_list != null && PartsHolder.getInstance().original_parts_list.size() > 0
                                        && PartsHolder.getInstance().parts_list != null || PartsHolder.getInstance().parts_list.size() > 0) {
                                    for (int i = 0; i < PartsHolder.getInstance().original_parts_list.size(); i++) {
                                        int key = PartsHolder.getInstance().original_parts_list.keyAt(i);
                                        Part part = PartsHolder.getInstance().original_parts_list.get(key);
                                        // If the user deleted parts from the original list delete them
                                        if (PartsHolder.getInstance().parts_list.indexOfKey(key) < 0) {
                                            //System.out.println("delete from db-> part_id:"+part.getPart_id()+"\nname:"+part.getName()+"\nquantity:"+part.getQuantity()+"\n");
                                            DBHelper.getInstance(UpdateCustomerActivity.this).deleteCustomerPart(customer_id, part.getPart_id());
                                        }
                                    }
                                /*Go through the new list and check if an item is
                                  new or already existed in original list if new just add to database if existed just update database
                                */
                                    for (int i = 0; i < PartsHolder.getInstance().parts_list.size(); i++) {
                                        int key = PartsHolder.getInstance().parts_list.keyAt(i);
                                        Part part = PartsHolder.getInstance().parts_list.get(key);
                                        // Check if there's an updated part in the new parts list
                                        if (PartsHolder.getInstance().original_parts_list.get(part.getPart_id()) != null) {
                                            // If the same part exactly
                                            if (!PartsHolder.getInstance().original_parts_list.get(part.getPart_id()).isTheSame(part)) {
                                                //System.out.println("update db-> part_id:"+part.getPart_id()+"\nname:"+part.getName()+"\nquantity:"+part.getQuantity()+"\n");
                                                DBHelper.getInstance(UpdateCustomerActivity.this).updateCustomerPart(customer_id, part.getPart_id(), part);
                                            }
                                        } else {
                                            //System.out.println("add to db-> part_id:"+part.getPart_id()+"\nname:"+part.getName()+"\nquantity:"+part.getQuantity()+"\n");
                                            DBHelper.getInstance(UpdateCustomerActivity.this).addCustomerPart(part, customer_id);
                                        }
                                    }
                                }
                            }
                            /////////////////////                                       /////////////////////
                            Intent i = new Intent(UpdateCustomerActivity.this , CustomersActivity.class);
                            // Clear activity's stack
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            beforeOnDestroy();
                            startActivity(i);
                        }
                    }
                })
                .setNegativeButton(R.string.opendialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .setTitle(R.string.dialog_warning)
                .setIcon(R.drawable.ic_warning);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void beforeOnDestroy() {
        PartsHolder.getInstance().parts_list = null;
        PartsHolder.getInstance().partsList = null;
        PartsHolder.getInstance().parts_list_changed = false;
        PartsHolder.getInstance().original_parts_list = null;
    }


    private RadioGroup.OnCheckedChangeListener onCheckedChangeListenerRepeat = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int id) {
            // Check which radio button was clicked
            switch(id) {
                case R.id.radioButton_weekly:
                    notification.setRepeat(String.valueOf(id));
                    break;
                case R.id.radioButton_twice_weekly:
                    notification.setRepeat(String.valueOf(id));
                    break;
                case R.id.radioButton_monthly:
                    notification.setRepeat(String.valueOf(id));
                    break;
                default:
                    notification.setRepeat(String.valueOf(id));
                    binding.editTextCustomerDate.setError(null);
                    binding.editTextCustomerNotification.setError(null);
                    break;
            }

        }
    };
    private RadioGroup.OnCheckedChangeListener onCheckedChangeListenerTimeOption = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int id) {
            // Check which radio button was clicked
            switch(id) {
                case R.id.radioButton_days:
                    time_option = String.valueOf(id);
                    break;
                default:
                    time_option = String.valueOf(id);
                    break;
            }
            updatePriceBeforeDiscount();
        }
    };
    private RadioGroup.OnCheckedChangeListener onCheckedChangeListenerDiscount = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int id) {
            // Check which radio button was clicked
            double discount ;
            double price = 0;
            if(customer.getPrice() != null && !customer.getPrice().isEmpty()
                    && Double.parseDouble(customer.getPrice()) > 0){
                price = Double.parseDouble(customer.getPrice());
            }
            switch(id) {
                case R.id.radioButton_five_percent:
                    discount_option = String.valueOf(id);
                    discount = price - price*0.05;
                    break;
                case R.id.radioButton_ten_percent:
                    discount_option = String.valueOf(id);
                    discount = price - price*0.1;
                    break;
                case R.id.radioButton_fifteen_percent:
                    discount_option = String.valueOf(id);
                    discount = price - price*0.15;
                    break;
                default:
                    discount_option = String.valueOf(id);
                    binding.editTextCustomerFinalPriceAfterDiscount.setText(null);
                    final_price_after_discount = null;
                    discount = 0;
                    break;
            }
            if(discount > 0){
                final_price_after_discount = String.valueOf(discount);
                binding.editTextCustomerFinalPriceAfterDiscount.setText(Utility.setRoundedNumber(String.valueOf(discount)));

            }
        }
    };
}

