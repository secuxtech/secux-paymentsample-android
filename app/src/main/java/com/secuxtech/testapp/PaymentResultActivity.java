package com.secuxtech.testapp;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;




public class PaymentResultActivity extends BaseActivity {

    private Context mContext = this;
    private boolean mPayResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        Boolean result = getIntent().getExtras().getBoolean(PaymentDetailsActivity.PAYMENT_RESULT);
        mPayResult = result;

        ImageView imgviewRet = findViewById(R.id.imageView_result);
        int color = ContextCompat.getColor(this, R.color.colorPaymentFail);
        String resultStr = "Payment Failed";
        if (result){
            resultStr = "Payment Successful";
            color = ContextCompat.getColor(this, R.color.colorPaymentSuccess);

            imgviewRet.setImageResource(R.drawable.payment_success);

            TextView textviewHis = findViewById(R.id.textView_history);
            textviewHis.setText("Receipt");

        }else{

            imgviewRet.setImageResource(R.drawable.payment_failed);

            String error = getIntent().getStringExtra(PaymentDetailsActivity.PAYMENT_ERROR);
            TextView textViewError = findViewById(R.id.textView_payment_error);
            textViewError.setText(error);
            textViewError.setVisibility(View.VISIBLE);

            if (error.compareTo("No payment device!") == 0 || error.compareTo("Can't find device") == 0){
                resultStr = "Bluetooth error!\nPlease reopen your phone's bluetooth.";


            }
        }
        TextView textviewRet = findViewById(R.id.textView_payment_result);
        textviewRet.setTextColor(color);
        textviewRet.setText(resultStr);

        String amountStr = getIntent().getStringExtra(PaymentDetailsActivity.PAYMENT_AMOUNT);
        TextView textviewAmt = findViewById(R.id.textView_payment_amount);
        textviewAmt.setTextColor(color);
        textviewAmt.setText(amountStr);

        String date = getIntent().getStringExtra(PaymentDetailsActivity.PAYMENT_DATE);
        TextView textviewDate = findViewById(R.id.textView_payment_date);
        textviewDate.setText(date);

        String storename = getIntent().getStringExtra(PaymentDetailsActivity.PAYMENT_STORENAME);
        TextView textviewStoreName = findViewById(R.id.textView_storename_value);
        textviewStoreName.setText(storename);

        LinearLayout payRetInfoLayout = findViewById(R.id.linearLayout_payment_result_history);
        payRetInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               //Show payment details
            }
        });



        new AlertDialog.Builder(mContext)
                .setMessage("Bluetooth error! Please reopen bluetooth setting!")
                .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                mContext.startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        }).show();
    }
}
