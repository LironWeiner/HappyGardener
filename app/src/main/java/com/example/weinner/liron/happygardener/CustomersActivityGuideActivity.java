package com.example.weinner.liron.happygardener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomersActivityGuideActivity extends AppCompatActivity {
    private boolean isFabOpen = false; // Flag for the floating action button animation when long click
    private boolean after_short_click = false; // Flag to check if user pressed a short click
    private TextView hint;
    private FloatingActionButton add_customer , fab_sign_out , fab_parts;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward , fade_in;
    private ImageView arrow_hint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_guide);
        hint = findViewById(R.id.textView_add_customer_hint);
        arrow_hint = findViewById(R.id.imageView_hint);
        add_customer = findViewById(R.id.fab);
        fab_sign_out = findViewById(R.id.fab_sign_out);
        fab_parts = findViewById(R.id.fab_parts);
        // Init animations
        fab_open = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        fade_in = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_in);
        //

        openDialog();

        fade_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                add_customer.setEnabled(false);
                add_customer.setClickable(false);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                add_customer.setEnabled(true);
                add_customer.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        add_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                after_short_click = true;
                hint.startAnimation(fade_in);
                arrow_hint.setVisibility(View.INVISIBLE);
                hint.setText(R.string.long_press_button);
            }
        });

        add_customer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                if(after_short_click){
                    animateFAB();
                }
                return true;
            }
        });

    }
    // Animation for fab action button
    private void animateFAB(){
        if(!isFabOpen) {
            add_customer.startAnimation(rotate_forward);
            fab_sign_out.startAnimation(fab_open);
            fab_parts.startAnimation(fab_open);
            fab_sign_out.setClickable(true);
            fab_parts.setClickable(true);
            isFabOpen = true;
            rotate_forward.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    add_customer.startAnimation(rotate_backward);
                    fab_sign_out.startAnimation(fab_close);
                    fab_parts.startAnimation(fab_close);
                    Intent i = new Intent(CustomersActivityGuideActivity.this , CustomersActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in , R.anim.fade_out);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void openDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CustomersActivityGuideActivity.this);
        alertDialogBuilder.setMessage(R.string.guide_dialog)
                .setPositiveButton(R.string.opendialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        arrow_hint.setVisibility(View.VISIBLE);
                        hint.setVisibility(View.VISIBLE);
                        arrow_hint.startAnimation(fade_in);
                        hint.startAnimation(fade_in);
                    }
                })
                .setNegativeButton(R.string.opendialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(CustomersActivityGuideActivity.this , CustomersActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.fade_in , R.anim.fade_out);
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
