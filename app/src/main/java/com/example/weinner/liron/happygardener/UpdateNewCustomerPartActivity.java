package com.example.weinner.liron.happygardener;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.weinner.liron.happygardener.databinding.ActivityAddCustomerPartBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UpdateNewCustomerPartActivity extends AppCompatActivity {

    private Part part;
    private static String measurement_string;
    private ActivityAddCustomerPartBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_update_new_customer_part);
        binding = DataBindingUtil.setContentView(this , R.layout.activity_add_customer_part);
        GlideApp.with(binding.framelayoutBackground).load(R.drawable.add_part_background).into(binding.framelayoutBackground);

        //Firebase
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        binding.buttonPartCreate.setText(R.string.update_part);

        part = new Part(PartsHolder.getInstance().parts_list.get(getIntent().getIntExtra("parts_list_index" , 0)));
        binding.editTextPartName.setText(part.getName());
        binding.editTextPartPrice.setText(part.getPrice());

        if(part.getCategory().getCategory().equals(getString(R.string.plants))){
            makePictureLayoutVisible();
            initDownloadImage(storageRef);
        }

        ArrayAdapter<CharSequence> measurementAdapter = ArrayAdapter.createFromResource(this,
                R.array.measurement_options , android.R.layout.simple_spinner_item);
        measurementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPartMeasurement.setAdapter(measurementAdapter);

        int space_index = part.getQuantity().indexOf(' ');


        // It means that the quantity has a unit other then "unit"
        if(space_index != -1){
            binding.editTextPartQuantity.setText(part.getQuantity().substring(0 , space_index));
            measurement_string = part.getQuantity().substring(space_index+1);
            binding.spinnerPartMeasurement.setSelection(measurementAdapter.getPosition(measurement_string));
        }
        else if(space_index == -1){
            binding.editTextPartQuantity.setText(part.getQuantity());
        }





        binding.spinnerPartMeasurement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                measurement_string = parent.getItemAtPosition(position).toString();
                updateQuantityMeasurement();

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.imageButtonPartsPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.imageButtonPartsPicture.getDrawable() != null) {
                    ExifData.getInstance().insertExifData(binding.imageButtonPartsPicture , part);

                    MediaScannerConnection.scanFile(UpdateNewCustomerPartActivity.this
                            , new String[]{ExifData.getInstance().imageFile.getAbsolutePath()}
                            ,null
                            , new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setDataAndType(uri, "image");
                                    startActivityForResult(intent , ExifData.getInstance().DELETE_IMAGE_FILE);
                                }
                            });
                }

            }
        });

        binding.editTextPartQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                part.setQuantity(editable.toString());
                updateQuantityMeasurement();
            }
        });

        binding.buttonPartCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(part.getQuantity() == null || part.getQuantity().isEmpty()) {
                    binding.editTextPartQuantity.setError(getString(R.string.Required));
                }
                else if( part.getQuantity() != null && part.getQuantity().charAt(0) == '.')
                {
                    binding.editTextPartQuantity.setError(getString(R.string.valid_number));
                }
                else{
                    try {
                        double number = Double.parseDouble(binding.editTextPartQuantity.getText().toString());
                        int point_index = binding.editTextPartQuantity.getText().toString().indexOf('.');
                        if(binding.editTextPartQuantity.getText().length()-1 == point_index)
                            binding.editTextPartQuantity.setError(getString(R.string.error_valid_number));
                        else if(number <= 0)
                            binding.editTextPartQuantity.setError(getString(R.string.error_valid_number));
                        else if(part.isTheSame(PartsHolder.getInstance().parts_list.get(getIntent().getIntExtra("parts_list_index" , 0)))){
                            Toast.makeText(UpdateNewCustomerPartActivity.this, R.string.toast_equal, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            PartsHolder.getInstance().parts_list.put(getIntent().getIntExtra("parts_list_index" , 0) , part);
                            PartsHolder.getInstance().parts_list_changed = true;

                            final Intent intent = new Intent(getApplicationContext() , CustomerParts.class);

                            if(CheckedItems.getInstance().UpdateCustomerMode){

                                int customer_id = getIntent().getIntExtra("customer_id" , 0);
                                intent.putExtra("customer_id" , customer_id);
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        }
                    }
                    catch(NumberFormatException e){
                        binding.editTextPartQuantity.setError(getString(R.string.error_valid_number));
                    }
                }
            }
        });
    }

    private void initDownloadImage(StorageReference storageRef) {
        if(binding.imageButtonPartsPicture.getDrawable() == null) {
            new DownloadImage(storageRef, binding.imageButtonPartsPicture , binding.progressBar , getApplicationContext())
                    .doInBackground(part.getCategory().getCategory()+"/" + part.getPart_id() + ".webp");
        }
    }

    private void updateQuantityMeasurement(){
        if(binding.editTextPartQuantity.getText().toString() != null)
        {
            if(measurement_string != null && !measurement_string.equals(getResources().getString(R.string.unit))
                    && part.getQuantity() != null){
                try{
                    updateTotalPrice();
                    part.setQuantity(binding.editTextPartQuantity.getText().toString()+" "+measurement_string);
                }
                catch (NumberFormatException e){
                    part.setQuantity(null);
                }
            }
            else if(part.getQuantity() != null && measurement_string.equals(getResources().getString(R.string.unit))){
                try{
                    part.setQuantity(binding.editTextPartQuantity.getText().toString());
                    updateTotalPrice();
                }
                catch (NumberFormatException e){
                    binding.editTextPartTotalPrice.setText(null);
                }
            }
        }
    }

    private void updateTotalPrice() {
        double val = Double.valueOf(binding.editTextPartQuantity.getText().toString()) * Double.valueOf(part.getPrice());
        binding.editTextPartTotalPrice.setText(Utility.setRoundedNumber(String.valueOf(val)));
    }

    private void makePictureLayoutVisible() {
        binding.textViewPartsPicture.setVisibility(View.VISIBLE);
        binding.imageButtonPartsPicture.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ExifData.getInstance().DELETE_IMAGE_FILE && ExifData.getInstance().imageFile != null)
        {
            ExifData.getInstance().imageFile.delete();
            ExifData.getInstance().imageFile = null;
        }
    }
}
